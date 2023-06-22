package ymg.pwcca.pingcc.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4f;
import ymg.pwcca.pingcc.config.PingCCConfigModel.Agents;

import java.util.UUID;


public class PingData {
  public String senderName;
  public Formatting pingColor;
  public Agents agent;
  public Vec3d pos;
  public Vector4f screenPos;
  public UUID pingEntity;
  public BlockHitResult pingBlock;
  public ItemStack itemStack;
  public Integer spawnTime;
  public Integer aliveTime;

  public PingData(String senderName, Formatting color, Agents agent, Vec3d pos, UUID pingEntity, BlockHitResult pingBlock, long spawnTime) {
    this.senderName = senderName;
    this.pingColor = color;
    this.agent = agent;
    this.pos = pos;
    this.pingEntity = pingEntity;
    this.pingBlock = pingBlock;
    this.spawnTime = (int) spawnTime;
  }
}