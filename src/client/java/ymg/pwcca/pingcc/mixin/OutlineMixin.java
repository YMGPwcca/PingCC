package ymg.pwcca.pingcc.mixin;

import com.google.common.collect.Iterables;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ymg.pwcca.pingcc.PingCCClient;
import ymg.pwcca.pingcc.util.PingData;

import static ymg.pwcca.pingcc.PingCCClient.pingList;

@Mixin(MinecraftClient.class)
public abstract class OutlineMixin {
  @Shadow
  public abstract RenderTickCounter getRenderTickCounter();

  @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
  private void outlineEntities(Entity entity, CallbackInfoReturnable<Boolean> ci) {
    if (PingCCClient.CONFIG.vision.showEntityOutline()) {
      ClientWorld world = MinecraftClient.getInstance().world;

      for (PingData ping : pingList) {
        if (ping.hitEntity != null) {
          Entity ent = Iterables.tryFind(world.getEntities(), e -> e.getUuid().equals(ping.hitEntity)).orNull();
          float tickDelta = getRenderTickCounter().getTickDelta(false);
          Vec3d cameraPosVec = MinecraftClient.getInstance().player.getCameraPosVec(tickDelta);

          if (ent != null && entity.getUuid().equals(ent.getUuid()) && PingCCClient.canOutlineEntity(ent) && cameraPosVec.distanceTo(ping.hitPos) < 64) ci.setReturnValue(true);
        }
      }
    }
  }
}
