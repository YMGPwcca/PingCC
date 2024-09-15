package ymg.pwcca.pingcc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ymg.pwcca.pingcc.networking.PingPayload;

public class PingCC implements ModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger("pingcc");

  public static final Identifier PING_ID = Identifier.of("pingcc:ping");

  @Override
  public void onInitialize() {
    LOGGER.info("Initialized PingCC on Server");

    PayloadTypeRegistry.playC2S().register(PingPayload.ID, PingPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(PingPayload.ID, PingPayload.CODEC);

    ServerPlayNetworking.registerGlobalReceiver(PingPayload.ID, (payload, context) -> {


      for (ServerPlayerEntity p : PlayerLookup.world(context.player().getServerWorld()))
        ServerPlayNetworking.send(p, payload);
    });
  }
}