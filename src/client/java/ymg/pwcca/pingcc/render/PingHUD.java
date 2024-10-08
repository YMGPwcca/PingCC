package ymg.pwcca.pingcc.render;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4f;
import ymg.pwcca.pingcc.PingCCClient;
import ymg.pwcca.pingcc.util.PingData;

import java.util.Objects;
import java.util.UUID;

public class PingHUD implements HudRenderCallback {

  private static final Identifier PING_ICON = Identifier.of("pingcc", "textures/ping_icon.png");

  @Override
  public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
    MinecraftClient client = MinecraftClient.getInstance();
    MatrixStack stack = context.getMatrices();
    float tickDelta = tickCounter.getTickDelta(false);
    double uiScale = client.getWindow().getScaleFactor();
    Vec3d cameraPosVec = client.player.getCameraPosVec(tickDelta);

    for (PingData ping : PingCCClient.pingList) {
      if (ping.screenPos == null) continue;

      PlayerEntity player = MinecraftClient.getInstance().player;
      if (ping.hitEntity != null && ping.hitEntity.equals(player.getUuid())) {
        player.sendMessage(
          MutableText.of(PlainTextContent.EMPTY)
            .append(Text.literal(ping.senderUsername).formatted(ping.pingColor))
            .append(Text.translatable("text.message.ping"))
            .append(Text.translatable("text.message.ping.you")),
          true
        );
        continue;
      }

      stack.push();

      // ping color information
      Formatting pingColor = ping.pingColor;
      Color toColorObject = Color.ofRgb(pingColor.getColorValue());
      boolean isTooDark = isColorTooDark(pingColor);
      int shadow = ColorHelper.Argb.getArgb(200, 255, 255, 255);

      // probably hitPos data
      double distance = cameraPosVec.distanceTo(ping.hitPos);
      Vector4f screenPos = screenPosWindowed(ping.screenPos, client.getWindow());

      // centralize ping
      stack.translate(screenPos.x / uiScale, screenPos.y / uiScale, 0); // stack to ping center
      stack.scale((float) (2 / uiScale), (float) (2 / uiScale), 1); // constant scale
      stack.scale(1.2f, 1.2f, 1); // config scale

      // draw ping icon
      RenderSystem.setShader(GameRenderer::getPositionTexProgram);
      RenderSystem.setShaderColor(toColorObject.red(), toColorObject.green(), toColorObject.blue(), toColorObject.alpha());
      context.drawTexture(PING_ICON, -4, -2, 0, 0, 8, 8, 8, 8);

      // skip drawing text if ping is not on screen
      if (screenPos != ping.screenPos) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        stack.pop();
        continue;
      }

      // display entity/block name and its position when the player is looking at the ping, otherwise display ping sender name
      boolean isCrosshairInlineWithPing = isScreenCenter(ping.screenPos, client.getWindow());
      boolean canShowInfo = (Objects.equals(ping.hitEntity, new UUID(0, 0)) && PingCCClient.CONFIG.general.getBlockInfo()) || (ping.hitEntity != null && PingCCClient.CONFIG.general.getEntityInfo());
      boolean showInfo = isCrosshairInlineWithPing && canShowInfo;

      String pos = String.format("%d, %d, %d", ((int) ping.hitPos.getX()), ((int) ping.hitPos.getY()), ((int) ping.hitPos.getZ()));

      String nameText = showInfo ? ping.hitName : ping.senderUsername;
      int nameTextWidth = client.textRenderer.getWidth(nameText);
      stack.scale(0.65f, 0.65f, 1f);
      stack.translate(-nameTextWidth / 2f, -14f, 0);

      // set background according to ping/text color
      if (isTooDark) RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
      else RenderSystem.setShaderColor(0 / 255.0f, 0 / 255.0f, 0 / 255.0f, 1f);
      context.fill(-2, -2, nameTextWidth + 1, client.textRenderer.fontHeight, shadow);

      // set the actual username text
      RenderSystem.setShaderColor(toColorObject.red(), toColorObject.green(), toColorObject.blue(), toColorObject.alpha());
      context.drawText(client.textRenderer, nameText, 0, 0, -1, false);
      stack.translate(nameTextWidth / 2f, 0, 0);

      // distance text
      String distanceText = showInfo ? pos : String.format("%dm", (int) distance);
      int distanceTextWidth = client.textRenderer.getWidth(distanceText);
      stack.translate(-distanceTextWidth / 2f, 27f, 0);

      // set background according to ping/text color
      if (isTooDark) RenderSystem.setShaderColor(175 / 255.0f, 175 / 255.0f, 175 / 255.0f, 1f);
      else RenderSystem.setShaderColor(0 / 255.0f, 0 / 255.0f, 0 / 255.0f, 1f);
      context.fill(-2, -2, distanceTextWidth + 1, client.textRenderer.fontHeight, shadow);

      // set the actual distance text
      RenderSystem.setShaderColor(toColorObject.red(), toColorObject.green(), toColorObject.blue(), toColorObject.alpha());
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

  private boolean isScreenCenter(Vector4f screenPos, Window window) {
    int width = window.getWidth();
    int height = window.getHeight();
    int margin = 15;

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
