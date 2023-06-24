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

    private final Option<ymg.pwcca.pingcc.config.PingCCConfigModel.Colors> vision_pingColor = this.optionForKey(this.keys.vision_pingColor);
    private final Option<java.lang.Boolean> vision_getBlockInfo = this.optionForKey(this.keys.vision_getBlockInfo);
    private final Option<java.lang.Boolean> vision_getEntityInfo = this.optionForKey(this.keys.vision_getEntityInfo);
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

    public final Vision_ vision = new Vision_();
    public class Vision_ implements Vision {
        public ymg.pwcca.pingcc.config.PingCCConfigModel.Colors pingColor() {
            return vision_pingColor.value();
        }

        public void pingColor(ymg.pwcca.pingcc.config.PingCCConfigModel.Colors value) {
            vision_pingColor.set(value);
        }

        public boolean getBlockInfo() {
            return vision_getBlockInfo.value();
        }

        public void getBlockInfo(boolean value) {
            vision_getBlockInfo.set(value);
        }

        public boolean getEntityInfo() {
            return vision_getEntityInfo.value();
        }

        public void getEntityInfo(boolean value) {
            vision_getEntityInfo.set(value);
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
    public interface Vision {
        ymg.pwcca.pingcc.config.PingCCConfigModel.Colors pingColor();
        void pingColor(ymg.pwcca.pingcc.config.PingCCConfigModel.Colors value);
        boolean getBlockInfo();
        void getBlockInfo(boolean value);
        boolean getEntityInfo();
        void getEntityInfo(boolean value);
        boolean showEntityOutline();
        void showEntityOutline(boolean value);
    }
    public interface Audio {
        int pingVolume();
        void pingVolume(int value);
        ymg.pwcca.pingcc.config.PingCCConfigModel.Agents agent();
        void agent(ymg.pwcca.pingcc.config.PingCCConfigModel.Agents value);
    }
    public static class Keys {
        public final Option.Key vision_pingColor = new Option.Key("vision.pingColor");
        public final Option.Key vision_getBlockInfo = new Option.Key("vision.getBlockInfo");
        public final Option.Key vision_getEntityInfo = new Option.Key("vision.getEntityInfo");
        public final Option.Key vision_showEntityOutline = new Option.Key("vision.showEntityOutline");
        public final Option.Key audio_pingVolume = new Option.Key("audio.pingVolume");
        public final Option.Key audio_agent = new Option.Key("audio.agent");
    }
}

