package ymg.pwcca.pingcc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingCC implements ModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger("pingcc");

  public static final Identifier CLIENT_TO_SERVER = new Identifier("pingcc:c2s");
  public static final Identifier SERVER_TO_CLIENT = new Identifier("pingcc:s2c");

  @Override
  public void onInitialize() {
    LOGGER.info("Initialized PingCC on Server");

    ServerPlayNetworking.registerGlobalReceiver(CLIENT_TO_SERVER, (server, player, handler, buf, responseSender) -> {
      PacketByteBuf packet = PacketByteBufs.copy(buf);

      for (ServerPlayerEntity p : PlayerLookup.world(player.getServerWorld()))
        ServerPlayNetworking.send(p, SERVER_TO_CLIENT, packet);
    });
  }
}