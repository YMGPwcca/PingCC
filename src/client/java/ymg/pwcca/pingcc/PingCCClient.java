package ymg.pwcca.pingcc;

import com.google.common.collect.Iterables;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import ymg.pwcca.pingcc.config.PingCCConfig;
import ymg.pwcca.pingcc.config.PingCCConfigModel.Agents;
import ymg.pwcca.pingcc.render.PingHUD;
import ymg.pwcca.pingcc.util.DirectionalSoundInstance;
import ymg.pwcca.pingcc.util.MathHelper;
import ymg.pwcca.pingcc.util.PingData;
import ymg.pwcca.pingcc.util.RayCasting;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PingCCClient implements ClientModInitializer {

  public static final PingCCConfig CONFIG = PingCCConfig.createAndLoad();

  public static List<PingData> pingList = new ArrayList<>();
  private static boolean queuePing = false;

  private static boolean pinged = false;
  private static Timer tempTimer;

  @Override
  public void onInitializeClient() {
    PingCC.LOGGER.info("Initialized PingCC on Client");

    registerSoundEvents();

    KeyBinding keyPing = KeyBindingHelper.registerKeyBinding(new KeyBinding("pingcc.key.mark-location", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, "pingcc.name"));

    ClientPlayNetworking.registerGlobalReceiver(PingCC.SERVER_TO_CLIENT, PingCCClient::onReceivePing);
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (keyPing.wasPressed()) {
        if (!pinged) {
          queuePing = true;
          pinged = true;

          if (tempTimer == null) {
            tempTimer = new Timer();
            tempTimer.schedule(new TimerTask() {
              @Override
              public void run() {
                pinged = false;
                tempTimer.cancel();
                tempTimer = null;
              }
            }, 1000);
          }
        }
      }
    });

    HudRenderCallback.EVENT.register(new PingHUD());
  }

  private static void registerSoundEvents() {
    Agents[] agents = Agents.values();

    for (Agents agent : agents) {
      String agentName = agent.toString();
      registerSounds(new Identifier("pingcc:%s1".formatted(agentName.toLowerCase())));
      registerSounds(new Identifier("pingcc:%s2".formatted(agentName.toLowerCase())));
    }
  }

  private static void registerSounds(Identifier identifier) {
    Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
  }

  private static void processPing(float tickDelta) {
    if (!queuePing) return;
    else queuePing = false;

    ClientPlayerEntity clientEntity = (ClientPlayerEntity) MinecraftClient.getInstance().cameraEntity;
    HitResult hitResult = RayCasting.traceDirectional(clientEntity.getRotationVec(tickDelta), tickDelta, 256, clientEntity.isSneaking(), false);

    if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
      pinged = false;
      tempTimer.cancel();
      tempTimer = null;
      return;
    }
    PacketByteBuf packet = PacketByteBufs.create(); // create packet

    // sender information
    packet.writeString(clientEntity.getGameProfile().getName());
    packet.writeDouble(clientEntity.getPos().x);
    packet.writeDouble(clientEntity.getPos().y);
    packet.writeDouble(clientEntity.getPos().z);

    // ping configuration
    packet.writeInt(PingCCClient.CONFIG.vision.pingColor().getNumber());
    packet.writeInt(PingCCClient.CONFIG.audio.agent().ordinal());

    // hitResult information
    packet.writeDouble(hitResult.getPos().x);
    packet.writeDouble(hitResult.getPos().y);
    packet.writeDouble(hitResult.getPos().z);

    if (hitResult instanceof EntityHitResult) {
      packet.writeInt(1);
      packet.writeUuid(((EntityHitResult) hitResult).getEntity().getUuid());
    } else if (hitResult instanceof BlockHitResult) {
      packet.writeInt(2);
      packet.writeBlockHitResult((BlockHitResult) hitResult);
    } else packet.writeInt(0);

    ClientPlayNetworking.send(PingCC.CLIENT_TO_SERVER, packet);
  }

  private static void onReceivePing(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {

    // sender information
    String pingSender = buf.readString();
    Vec3d senderPos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());

    // ping configuration
    Formatting pingColor = Formatting.values()[buf.readInt()];
    Agents agent = Agents.values()[buf.readInt()];

    // hitResult pos
    Vec3d pingPos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    int pingType = buf.readInt();
    UUID pingEntity = pingType == 1 ? buf.readUuid() : null;
    BlockHitResult pingBlock = pingType == 2 ? buf.readBlockHitResult() : null;

    client.execute(() -> {
      pingList.add(new PingData(pingSender, pingColor, agent, pingPos, pingEntity, pingBlock, client.world.getTime()));

      DirectionalSoundInstance directionalSoundInstance = new DirectionalSoundInstance(
          SoundEvent.of(Identifier.of("pingcc", agent.toString().toLowerCase() + (Math.random() > 0.5 ? 1 : 2))),
          SoundCategory.PLAYERS,
          PingCCClient.CONFIG.audio.pingVolume() / 100f,
          1f,
          0,
          senderPos
      );

      if (directionalSoundInstance.getMappedDistance() >= 15.0) return;

      client.getSoundManager().play(directionalSoundInstance);
    });
  }

  public static void onRenderWorld(MatrixStack stack, Matrix4f projectionMatrix, float tickDelta) {
    ClientWorld world = MinecraftClient.getInstance().world;
    Matrix4f modelViewMatrix = stack.peek().getPositionMatrix();

    processPing(tickDelta);

    for (PingData ping : pingList) {
      if (ping.pingEntity != null) {
        Entity ent = Iterables.tryFind(world.getEntities(), entity -> entity.getUuid().equals(ping.pingEntity)).orNull();

        if (ent != null) {
          if (ent instanceof ItemEntity itemEnt) ping.itemStack = itemEnt.getStack().copy();
          ping.pos = ent.getLerpedPos(tickDelta).add(0.0, ent.getBoundingBox().getYLength(), 0.0);
        }
      }

      ping.screenPos = MathHelper.project3Dto2D(ping.pos, modelViewMatrix, projectionMatrix);
      ping.aliveTime = Math.toIntExact(world.getTime() - ping.spawnTime);
    }

    pingList.removeIf(p -> p.aliveTime > 5 * 20);
  }
}