package ymg.pwcca.pingcc.mixin;

import com.google.common.collect.Iterables;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ymg.pwcca.pingcc.PingCCClient;
import ymg.pwcca.pingcc.util.PingData;

import static ymg.pwcca.pingcc.PingCCClient.pingList;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

  @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/client/render/Camera;)V", shift = At.Shift.AFTER))
  private void onRenderPostWorldBorder(
      MatrixStack matrices,
      float tickDelta,
      long limitTime,
      boolean renderBlockOutline,
      Camera camera,
      GameRenderer gameRenderer,
      LightmapTextureManager lightmapTextureManager,
      Matrix4f positionMatrix,
      CallbackInfo ci
  ) {PingCCClient.onRenderWorld(matrices, positionMatrix, tickDelta);}

  @Inject(method = "renderEntity", at = @At("HEAD"))
  private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
    if (vertexConsumers instanceof OutlineVertexConsumerProvider) {
      ClientWorld world = MinecraftClient.getInstance().world;

      for (PingData ping : pingList) {
        if (ping.pingEntity != null) {
          Entity ent = Iterables.tryFind(world.getEntities(), e -> e.getUuid().equals(ping.pingEntity)).orNull();
          if (ent != null && entity.getUuid().equals(ent.getUuid()) && PingCCClient.canOutlineEntity(ent)) {
            Color color = Color.ofRgb(ping.pingColor.getColorValue());

            OutlineVertexConsumerProvider outlineVertexConsumers = (OutlineVertexConsumerProvider) vertexConsumers;
            outlineVertexConsumers.setColor(floatColorToInt(color.red()), floatColorToInt(color.green()), floatColorToInt(color.blue()), 255);
          }
        }
      }
    }
  }

  @Unique
  private int floatColorToInt(float f) {
    return (int) (f * 255.0F);
  }
}
