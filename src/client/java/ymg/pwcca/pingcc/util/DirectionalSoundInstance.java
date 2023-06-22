package ymg.pwcca.pingcc.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class DirectionalSoundInstance extends MovingSoundInstance {
  private final Vec3d pos;

  public DirectionalSoundInstance(SoundEvent soundEvent, SoundCategory soundCategory, Float volume, Float pitch, long seed, Vec3d pos) {
    super(soundEvent, soundCategory, Random.create(seed));
    this.volume = volume;
    this.pitch = pitch;
    this.pos = pos;

    updateSoundPos();
  }

  @Override
  public void tick() {
    updateSoundPos();
  }

  private void updateSoundPos() {
    Vec3d soundPos = getPlayerPos().add(getVecBetween().normalize().multiply(getMappedDistance()));

    this.x = soundPos.x;
    this.y = soundPos.y;
    this.z = soundPos.z;
  }

  public double getMappedDistance() {
    return Math.min(getVecBetween().length(), 64.0) / 64.0 * 15;
  }

  private Vec3d getPlayerPos() {
    return MinecraftClient.getInstance().player.getPos();
  }

  private Vec3d getVecBetween() {
    return getPlayerPos().relativize(this.pos);
  }
}
