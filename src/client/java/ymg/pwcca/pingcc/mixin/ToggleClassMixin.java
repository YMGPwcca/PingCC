package ymg.pwcca.pingcc.mixin;

import io.wispforest.owo.config.ui.component.ConfigToggleButton;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("unused")
@Mixin(ConfigToggleButton.class)
public class ToggleClassMixin {

  @Final
  @Shadow
  @Mutable
  protected static Text ENABLED_MESSAGE = Text.translatable("text.config.pingcc-config.boolean.enabled");

  @Final
  @Shadow
  @Mutable
  protected static Text DISABLED_MESSAGE = Text.translatable("text.config.pingcc-config.boolean.disabled");
}
