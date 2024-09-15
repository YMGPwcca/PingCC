package ymg.pwcca.pingcc.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;
import org.joml.Vector3f;
import ymg.pwcca.pingcc.PingCC;

import java.util.UUID;

public record PingPayload(
  UUID senderEntity,
  Vector3f hitPos,
  UUID hitEntity,
  String hitName,
  int pingColor,
  int agent
) implements CustomPayload {
  public static final Id<PingPayload> ID = new Id<>(PingCC.PING_ID);

  public static final PacketCodec<ByteBuf, PingPayload> CODEC = PacketCodec.tuple(
    Uuids.PACKET_CODEC, PingPayload::senderEntity,
    PacketCodecs.VECTOR3F, PingPayload::hitPos,
    Uuids.PACKET_CODEC, PingPayload::hitEntity,
    PacketCodecs.STRING, PingPayload::hitName,
    PacketCodecs.INTEGER, PingPayload::pingColor,
    PacketCodecs.INTEGER, PingPayload::agent,
    PingPayload::new
  );

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
