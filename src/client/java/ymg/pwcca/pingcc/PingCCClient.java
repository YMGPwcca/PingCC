package ymg.pwcca.pingcc;

import com.google.common.collect.Iterables;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import ymg.pwcca.pingcc.config.PingCCConfig;
import ymg.pwcca.pingcc.config.PingCCConfigModel.Agents;
import ymg.pwcca.pingcc.networking.PingPayload;
import ymg.pwcca.pingcc.render.PingHUD;
import ymg.pwcca.pingcc.util.DirectionalSoundInstance;
import ymg.pwcca.pingcc.util.MathHelper;
import ymg.pwcca.pingcc.util.PingData;
import ymg.pwcca.pingcc.util.RayCasting;

import java.util.*;

public class PingCCClient implements ClientModInitializer {

  public static final PingCCConfig CONFIG = PingCCConfig.createAndLoad();

  public static List<PingData> pingList = new ArrayList<>();
  private static boolean queuePing = false;
  private static Timer tempTimer = null;

  @Override
  public void onInitializeClient() {
    PingCC.LOGGER.info("Initialized PingCC on Client");

    registerSoundEvents();

    KeyBinding keyPing = KeyBindingHelper.registerKeyBinding(new KeyBinding("pingcc.key.mark-location", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, "pingcc.hitName"));

    ClientPlayNetworking.registerGlobalReceiver(PingPayload.ID, PingCCClient::onReceivePing);
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (keyPing.wasPressed() && tempTimer == null) {
        queuePing = true;
        tempTimer = new Timer();
        tempTimer.schedule(new TimerTask() {
          @Override
          public void run() {
            tempTimer.cancel();
            tempTimer = null;
          }
        }, 500);
      }
    });

    HudRenderCallback.EVENT.register(new PingHUD());
  }

  private static void registerSoundEvents() {
    Agents[] agents = Agents.values();

    for (Agents agent : agents) {
      String agentName = agent.toString();
      registerSounds(Identifier.of("pingcc:%s1".formatted(agentName.toLowerCase())));
      registerSounds(Identifier.of("pingcc:%s2".formatted(agentName.toLowerCase())));
    }
  }

  private static void registerSounds(Identifier identifier) {
    Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
  }

  private static void processPing(float tickDelta) {
    if (!queuePing) return;
    else queuePing = false;

    MinecraftClient client = MinecraftClient.getInstance();
    HitResult hitResult = RayCasting.traceDirectional(client.player.getRotationVec(tickDelta), tickDelta, 256, client.player.isSneaking(), false);

    if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
      tempTimer.cancel();
      tempTimer = null;
      return;
    }

    UUID senderEntityUUID = client.player.getUuid();
    Vector3f hitPos = hitResult.getPos().toVector3f();
    int pingColor = PingCCClient.CONFIG.vision.pingColor().getNumber();
    int agent = PingCCClient.CONFIG.audio.agent().ordinal();

    PingPayload payload;
    if (hitResult.getType() == HitResult.Type.ENTITY) {
      payload = new PingPayload(
        senderEntityUUID,
        hitPos,
        ((EntityHitResult) hitResult).getEntity().getUuid(),
        ((EntityHitResult) hitResult).getEntity().getName().getString(),
        pingColor,
        agent
      );
    }
    else {
      BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
      BlockEntity blockEntity = client.world.getBlockEntity(blockPos);
      Block block = client.world.getBlockState(blockPos).getBlock();

      String hitName;
      if (blockEntity instanceof BannerBlockEntity && ((BannerBlockEntity) blockEntity).hasCustomName())
        hitName = Objects.requireNonNull(((BannerBlockEntity) blockEntity).getCustomName()).getString();
      else hitName = block.getName().getString();

      payload = new PingPayload(
        senderEntityUUID,
        hitPos,
        new UUID(0, 0),
        hitName,
        pingColor,
        agent
      );
    }

    ClientPlayNetworking.send(payload);
  }

  private static void onReceivePing(PingPayload payload, ClientPlayNetworking.Context context) {
    // get sender entity
    PlayerEntity sender = context.player().getWorld().getPlayerByUuid(payload.senderEntity());
    String senderUsername = sender.getName().getString();
    Vec3d senderPos = sender.getPos();

    // ping configuration
    Formatting pingColor = Formatting.values()[payload.pingColor()];
    Agents agent = Agents.values()[payload.agent()];

    // hitResult hitPos
    Vec3d pingPos = new Vec3d(payload.hitPos().x, payload.hitPos().y, payload.hitPos().z);
    UUID pingEntity = payload.hitEntity();
    String hitName = payload.hitName();

    context.client().execute(() -> {
      pingList.add(new PingData(senderUsername, pingColor, agent, pingPos, pingEntity, hitName, context.client().world.getTime()));

      DirectionalSoundInstance directionalSoundInstance = new DirectionalSoundInstance(
        SoundEvent.of(Identifier.of("pingcc", agent.toString().toLowerCase() + (new Random().nextInt(1, 3)))),
        SoundCategory.PLAYERS,
        PingCCClient.CONFIG.audio.pingVolume() / 100f,
        1f,
        0,
        senderPos
      );

      if (directionalSoundInstance.getMappedDistance() >= 15.0) return;

      context.client().getSoundManager().play(directionalSoundInstance);
    });
  }

  public static void onRenderWorld(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float tickDelta) {
    ClientWorld world = MinecraftClient.getInstance().world;

    processPing(tickDelta);

    for (PingData ping : pingList) {
      if (ping.hitEntity != null) {
        Entity ent = Iterables.tryFind(world.getEntities(), entity -> entity.getUuid().equals(ping.hitEntity)).orNull();

        if (canOutlineEntity(ent))
          ping.hitPos = ent.getLerpedPos(tickDelta).add(0.0, ent.getBoundingBox().getLengthY(), 0.0);
      }

      ping.screenPos = MathHelper.project3Dto2D(ping.hitPos, modelViewMatrix, projectionMatrix);
      ping.aliveTime = Math.toIntExact(world.getTime() - ping.spawnTime);
    }

    pingList.removeIf(p -> p.aliveTime > 5 * 20); // 5 seconds * 20tick
  }

  public static boolean canOutlineEntity(Entity entity) {
    return entity instanceof LivingEntity
      || entity instanceof ItemEntity
      || entity instanceof BoatEntity
      || entity instanceof AbstractMinecartEntity
      || entity instanceof TntEntity;
  }
}