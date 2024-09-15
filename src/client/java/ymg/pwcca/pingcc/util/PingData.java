package ymg.pwcca.pingcc.util;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4f;
import ymg.pwcca.pingcc.config.PingCCConfigModel.Agents;

import java.util.UUID;

public class PingData {
  public String senderUsername;
  public Formatting pingColor;
  public Agents agent;
  public Vec3d hitPos;
  public Vector4f screenPos;
  public UUID hitEntity;
  public String hitName;
  public Long spawnTime;
  public Integer aliveTime;

  public PingData(String senderUsername, Formatting pingColor, Agents agent, Vec3d hitPos, UUID hitEntity, String hitName, long spawnTime) {
    this.senderUsername = senderUsername;
    this.pingColor = pingColor;
    this.agent = agent;
    this.hitPos = hitPos;
    this.hitEntity = hitEntity;
    this.hitName = hitName;
    this.spawnTime = spawnTime;
  }
}