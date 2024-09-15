package ymg.pwcca.pingcc.config;

import io.wispforest.owo.config.annotation.*;

@SuppressWarnings("unused")
@Modmenu(modId = "pingcc")
@Config(name = "pingcc-config", wrapperName = "PingCCConfig")
public class PingCCConfigModel {

  @Nest
  @Expanded
  public General general = new General();

  public static class General {
    public boolean getBlockInfo = true;
    public boolean getEntityInfo = true;
    public boolean pingThruGrass = true;
  }

  @Nest
  @Expanded
  public Vision vision = new Vision();

  public static class Vision {
    public Colors pingColor = Colors.WHITE;
    public boolean showEntityOutline = true;
  }

  @Nest
  @Expanded
  public Audio audio = new Audio();

  public static class Audio {
    @RangeConstraint(min = 0, max = 100)
    public int pingVolume = 100;

    public Agents agent = Agents.Sova;
  }

  public enum Colors {
    // dark colors without black
    DARK_BLUE(1),
    DARK_GREEN(2),
    DARK_AQUA(3),
    DARK_RED(4),
    DARK_PURPLE(5),
    DARK_GRAY(8),

    // normal/light colors
    GOLD(6),
    GRAY(7),
    BLUE(9),
    GREEN(10),
    AQUA(11),
    RED(12),
    LIGHT_PURPLE(13),
    YELLOW(14),
    WHITE(15);

    private final int number;

    Colors(int number) {
      this.number = number;
    }

    public int getNumber() {
      return number;
    }
  }

  public enum Agents {
    Astra,
    Breach,
    Brimstone,
    Chamber,
    Clove,
    Cypher,
    Deadlock,
    Fade,
    Gekko,
    Harbor,
    Iso,
    Jett,
    Kayo,
    Killjoy,
    Neon,
    Omen,
    Phoenix,
    Raze,
    Reyna,
    Sage,
    Skye,
    Sova,
    Viper,
    Vyse,
    Yoru
  }
}
