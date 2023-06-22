package ymg.pwcca.pingcc.render;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4f;
import ymg.pwcca.pingcc.PingCCClient;
import ymg.pwcca.pingcc.util.PingData;

public class PingHUD implements HudRenderCallback {

  private static final Identifier PING_STANDARD = new Identifier("pingcc", "textures/ping_standard.png");

  @Override
  public void onHudRender(DrawContext context, float tickDelta) {
    MinecraftClient client = MinecraftClient.getInstance();
    MatrixStack stack = context.getMatrices();
    double uiScale = client.getWindow().getScaleFactor();
    assert client.player != null;
    Vec3d cameraPosVec = client.player.getCameraPosVec(tickDelta);

    for (PingData ping : PingCCClient.pingList) {
      PlayerEntity player = (PlayerEntity) MinecraftClient.getInstance().cameraEntity;

      if (ping.pingEntity != null && ping.pingEntity.equals(player.getUuid())) {
        player.sendMessage(
            MutableText.of(TextContent.EMPTY)
                .append(Text.literal(ping.senderName).formatted(ping.pingColor))
                .append(Text.translatable("text.message.ping"))
                .append(Text.translatable("text.message.ping.you")),
            true
        );
        continue;
      }

      stack.push();

      // ping information
      Formatting pingColor = ping.pingColor;
      Color toRGB = Color.ofRgb(pingColor.getColorValue());
      boolean isTooDark = isColorTooDark(pingColor);
      int shadow = ColorHelper.Argb.getArgb(255, 255, 255, 255);

      // probably pos data
      double distance = cameraPosVec.distanceTo(ping.pos);
      Vector4f screenPos = screenPosWindowed(ping.screenPos, client.getWindow());
      boolean onScreen = screenPos == ping.screenPos;

      // centralize ping
      stack.translate(screenPos.x / uiScale, screenPos.y / uiScale, 0); // stack to ping center
      stack.scale((float) (2 / uiScale), (float) (2 / uiScale), 1); // constant scale
      stack.scale(1.2f, 1.2f, 1); // config scale

      // draw ping icon
      RenderSystem.setShader(GameRenderer::getPositionTexProgram);
      RenderSystem.setShaderColor(toRGB.red(), toRGB.green(), toRGB.blue(), toRGB.alpha());
      context.drawTexture(PING_STANDARD, -4, -2, 0, 0, 8, 8, 8, 8);

      // skip drawing text if ping is not on screen
      if (!onScreen) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        stack.pop();
        continue;
      }

      // display entity/block name and its position when the player is looking at the ping, otherwise display ping sender name
      boolean isCrosshairInlineWithPing = isScreenCenter(ping.screenPos, 10, client.getWindow());
      boolean isAbleToShowEntityOrBlockName = isCrosshairInlineWithPing && (PingCCClient.CONFIG.vision.getBlockInfo() || PingCCClient.CONFIG.vision.getEntityInfo());

      // NEED TO FIX THIS
      String entityOrBlockName = null, entityOrBlockPos = null;
      if (ping.pingBlock != null && PingCCClient.CONFIG.vision.getBlockInfo()) {
        entityOrBlockName = client.world.getBlockState(ping.pingBlock.getBlockPos()).getBlock().getTranslationKey();
        entityOrBlockPos = ping.pingBlock.getBlockPos().toShortString();
      } else if (ping.pingBlock == null && PingCCClient.CONFIG.vision.getEntityInfo()) {
        Entity entity = Iterables.tryFind(client.world.getEntities(), e -> e.getUuid().equals(ping.pingEntity)).orNull();
        entityOrBlockName = entity != null ? entity.getName().getString() : Text.translatable(Blocks.AIR.getTranslationKey()).getString();
        entityOrBlockPos = entity != null ? entity.getBlockPos().toShortString() : String.format("%dm", (int) distance);
      }

      String nameText = isAbleToShowEntityOrBlockName ? Text.translatable(entityOrBlockName).getString() : ping.senderName;
      int nameTextWidth = client.textRenderer.getWidth(nameText);
      stack.scale(0.65f, 0.65f, 1f);
      stack.translate(-nameTextWidth / 2f, -14f, 0);

      // set background according to ping/text color
      if (isTooDark) RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
      else RenderSystem.setShaderColor(0 / 255.0f, 0 / 255.0f, 0 / 255.0f, 1f);
      context.fill(-2, -2, nameTextWidth + 1, client.textRenderer.fontHeight, shadow);

      // set the actual username text
      RenderSystem.setShaderColor(toRGB.red(), toRGB.green(), toRGB.blue(), toRGB.alpha());
      context.drawText(client.textRenderer, nameText, 0, 0, -1, false);
      stack.translate(nameTextWidth / 2f, 0, 0);

      // distance text
      String distanceText = isAbleToShowEntityOrBlockName ? entityOrBlockPos : String.format("%dm", (int) distance);
      int distanceTextWidth = client.textRenderer.getWidth(distanceText);
      stack.translate(-distanceTextWidth / 2f, 27f, 0);

      // set background according to ping/text color
      if (isTooDark) RenderSystem.setShaderColor(175 / 255.0f, 175 / 255.0f, 175 / 255.0f, 1f);
      else RenderSystem.setShaderColor(0 / 255.0f, 0 / 255.0f, 0 / 255.0f, 1f);
      context.fill(-2, -2, distanceTextWidth + 1, client.textRenderer.fontHeight, shadow);

      // set the actual distance text
      RenderSystem.setShaderColor(toRGB.red(), toRGB.green(), toRGB.blue(), toRGB.alpha());
      context.drawText(client.textRenderer, distanceText, 0, 0, -1, false);
      stack.translate(distanceTextWidth / 2f, 0, 0);

      // end
      RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
      stack.pop();
    }
  }

  private Vector4f screenPosWindowed(Vector4f screenPos, Window wnd) {
    Vector4f newPos = null;
    int width = wnd.getWidth();
    int height = wnd.getHeight();

    if (screenPos.w < 0) newPos = new Vector4f(width - screenPos.x, height - 16, screenPos.z, -screenPos.w);

    if (screenPos.x > width - 16) newPos = new Vector4f(width - 16, screenPos.y, screenPos.z, screenPos.w);
    else if (screenPos.x < 16) newPos = new Vector4f(16, screenPos.y, screenPos.z, screenPos.w);

    if (screenPos.y > height - 16) newPos = new Vector4f(screenPos.x, height - 16, screenPos.z, screenPos.w);
    else if (screenPos.y < 16) newPos = new Vector4f(screenPos.x, 16, screenPos.z, screenPos.w);

    return newPos != null ? newPos : screenPos;
  }

  private boolean isScreenCenter(Vector4f screenPos, int margin, Window window) {
    int width = window.getWidth();
    int height = window.getHeight();

    boolean isHorizontalCenter = screenPos.x > (width / 2f - margin) && screenPos.x < (width / 2f + margin);
    boolean isVerticalCenter = screenPos.y > (height / 2f - margin) && screenPos.y < (height / 2f + margin);

    return isHorizontalCenter && isVerticalCenter;
  }

  private boolean isColorTooDark(Formatting pingColor) {
    String hexColor = String.format("%06X", (0xFFFFFF & pingColor.getColorValue()));
    int red = Integer.parseInt(hexColor.substring(0, 2), 16);
    int green = Integer.parseInt(hexColor.substring(2, 4), 16);
    int blue = Integer.parseInt(hexColor.substring(4, 6), 16);

    double luma = (0.2126 * red) + (0.7152 * green) + (0.0722 * blue);
    return luma < 100;
  }
}