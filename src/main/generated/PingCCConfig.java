package ymg.pwcca.pingcc.config;

import blue.endless.jankson.Jankson;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.util.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PingCCConfig extends ConfigWrapper<ymg.pwcca.pingcc.config.PingCCConfigModel> {

    public final Keys keys = new Keys();

    private final Option<java.lang.Boolean> general_getBlockInfo = this.optionForKey(this.keys.general_getBlockInfo);
    private final Option<java.lang.Boolean> general_getEntityInfo = this.optionForKey(this.keys.general_getEntityInfo);
    private final Option<java.lang.Boolean> general_pingThruGrass = this.optionForKey(this.keys.general_pingThruGrass);
    private final Option<ymg.pwcca.pingcc.config.PingCCConfigModel.Colors> vision_pingColor = this.optionForKey(this.keys.vision_pingColor);
    private final Option<java.lang.Boolean> vision_showEntityOutline = this.optionForKey(this.keys.vision_showEntityOutline);
    private final Option<java.lang.Integer> audio_pingVolume = this.optionForKey(this.keys.audio_pingVolume);
    private final Option<ymg.pwcca.pingcc.config.PingCCConfigModel.Agents> audio_agent = this.optionForKey(this.keys.audio_agent);

    private PingCCConfig() {
        super(ymg.pwcca.pingcc.config.PingCCConfigModel.class);
    }

    private PingCCConfig(Consumer<Jankson.Builder> janksonBuilder) {
        super(ymg.pwcca.pingcc.config.PingCCConfigModel.class, janksonBuilder);
    }

    public static PingCCConfig createAndLoad() {
        var wrapper = new PingCCConfig();
        wrapper.load();
        return wrapper;
    }

    public static PingCCConfig createAndLoad(Consumer<Jankson.Builder> janksonBuilder) {
        var wrapper = new PingCCConfig(janksonBuilder);
        wrapper.load();
        return wrapper;
    }

    public final General_ general = new General_();
    public class General_ implements General {
        public boolean getBlockInfo() {
            return general_getBlockInfo.value();
        }

        public void getBlockInfo(boolean value) {
            general_getBlockInfo.set(value);
        }

        public boolean getEntityInfo() {
            return general_getEntityInfo.value();
        }

        public void getEntityInfo(boolean value) {
            general_getEntityInfo.set(value);
        }

        public boolean pingThruGrass() {
            return general_pingThruGrass.value();
        }

        public void pingThruGrass(boolean value) {
            general_pingThruGrass.set(value);
        }

    }
    public final Vision_ vision = new Vision_();
    public class Vision_ implements Vision {
        public ymg.pwcca.pingcc.config.PingCCConfigModel.Colors pingColor() {
            return vision_pingColor.value();
        }

        public void pingColor(ymg.pwcca.pingcc.config.PingCCConfigModel.Colors value) {
            vision_pingColor.set(value);
        }

        public boolean showEntityOutline() {
            return vision_showEntityOutline.value();
        }

        public void showEntityOutline(boolean value) {
            vision_showEntityOutline.set(value);
        }

    }
    public final Audio_ audio = new Audio_();
    public class Audio_ implements Audio {
        public int pingVolume() {
            return audio_pingVolume.value();
        }

        public void pingVolume(int value) {
            audio_pingVolume.set(value);
        }

        public ymg.pwcca.pingcc.config.PingCCConfigModel.Agents agent() {
            return audio_agent.value();
        }

        public void agent(ymg.pwcca.pingcc.config.PingCCConfigModel.Agents value) {
            audio_agent.set(value);
        }

    }
    public interface Audio {
        int pingVolume();
        void pingVolume(int value);
        ymg.pwcca.pingcc.config.PingCCConfigModel.Agents agent();
        void agent(ymg.pwcca.pingcc.config.PingCCConfigModel.Agents value);
    }
    public interface Vision {
        ymg.pwcca.pingcc.config.PingCCConfigModel.Colors pingColor();
        void pingColor(ymg.pwcca.pingcc.config.PingCCConfigModel.Colors value);
        boolean showEntityOutline();
        void showEntityOutline(boolean value);
    }
    public interface General {
        boolean getBlockInfo();
        void getBlockInfo(boolean value);
        boolean getEntityInfo();
        void getEntityInfo(boolean value);
        boolean pingThruGrass();
        void pingThruGrass(boolean value);
    }
    public static class Keys {
        public final Option.Key general_getBlockInfo = new Option.Key("general.getBlockInfo");
        public final Option.Key general_getEntityInfo = new Option.Key("general.getEntityInfo");
        public final Option.Key general_pingThruGrass = new Option.Key("general.pingThruGrass");
        public final Option.Key vision_pingColor = new Option.Key("vision.pingColor");
        public final Option.Key vision_showEntityOutline = new Option.Key("vision.showEntityOutline");
        public final Option.Key audio_pingVolume = new Option.Key("audio.pingVolume");
        public final Option.Key audio_agent = new Option.Key("audio.agent");
    }
}

