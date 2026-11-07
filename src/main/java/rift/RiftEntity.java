package rift;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Marker subclass so no abstract Entity methods have to be implemented. */
public class RiftEntity extends Marker implements GeoEntity {
    public static final int BURST = 40;
    public static final int FADE = 240;
    public static final int END = 300;
    private static final double R = 26.0;

    public static final Set<RiftEntity> LIVE = ConcurrentHashMap.newKeySet();

    private static final RawAnimation ANIM = RawAnimation.begin()
        .thenPlay("animation.rift.spawn")
        .thenLoop("animation.rift.idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Random rnd = new Random();
    private int life = 0;

    public RiftEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<RiftEntity>("main", state -> state.setAndContinue(ANIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void tick() {
        super.tick();
        life++;
        if (level().isClientSide()) {
            clientTick();
            return;
        }
        if (!(level() instanceof ServerLevel sl)) return;
        LIVE.removeIf(RiftEntity::isRemoved);
        LIVE.add(this);
        serverTick(sl);
        if (life >= END) discard();
    }

    private void clientTick() {
        Player p = level().getNearestPlayer(this, 90.0);
        if (p == null) return;
        double d = distanceTo(p);
        float s = (float) Math.max(0.0, 1.0 - d / 80.0);
        float fade = life < BURST ? life / (float) BURST
            : life > FADE ? Math.max(0f, (END - life) / (float) (END - FADE)) : 1f;
        ClientRiftState.ping(s * fade);
    }

    private void dust(ServerLevel sl, int color, float scale, double x, double y, double z) {
        sl.sendParticles(new DustParticleOptions(color, scale), x, y, z, 1, 0, 0, 0, 0);
    }

    private void sound(ServerLevel sl, SoundEvent s, float v, float p) {
        sl.playSound(null, BlockPos.containing(getX(), getY() + 5, getZ()), s, SoundSource.AMBIENT, v, p);
    }

    private void serverTick(ServerLevel sl) {
        double gx = getX(), gy = getY(), gz = getZ();
        boolean fading = life >= FADE;

        if (life == 1) sound(sl, SoundEvents.ENDER_DRAGON_GROWL, 3.0f, 0.5f);

        if (life == BURST) {
            sound(sl, SoundEvents.GLASS_BREAK, 4.0f, 0.5f);
            sound(sl, SoundEvents.LIGHTNING_BOLT_THUNDER, 4.0f, 0.6f);
            sound(sl, SoundEvents.GENERIC_EXPLODE.value(), 3.0f, 0.5f);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, gx, gy + 5, gz, 1, 0, 0, 0, 0);
        }

        // charge: red dust sucked into the orb
        if (life < BURST) {
            for (int i = 0; i < 12; i++) {
                double[] d = randDir();
                double dist = 7.0 * (1.0 - life / (double) BURST) + 1.0;
                dust(sl, 0xFF1A1A, 1.0f, gx + d[0] * dist, gy + 5 + d[1] * dist, gz + d[2] * dist);
            }
        }

        // shockwave ring on the ground
        if (life >= BURST && life < BURST + 28) {
            double rr = life - BURST;
            int n = (int) (rr * 6) + 12;
            for (int i = 0; i < n; i++) {
                double a = i / (double) n * Math.PI * 2;
                dust(sl, 0xFF3030, 1.4f, gx + Math.cos(a) * rr, gy + 0.3, gz + Math.sin(a) * rr);
            }
        }

        // falling shards from the sky cracks
        if (life >= BURST + 10 && life % 2 == 0 && !fading) {
            for (int k = 0; k < 3; k++) {
                double theta = rnd.nextDouble() * Math.PI * 2;
                double elev = 0.3 + rnd.nextDouble() * 1.1;
                double x = gx + R * Math.cos(elev) * Math.cos(theta);
                double y = gy + R * Math.sin(elev);
                double z = gz + R * Math.cos(elev) * Math.sin(theta);
                var block = rnd.nextBoolean() ? Blocks.CRYING_OBSIDIAN : Blocks.OBSIDIAN;
                sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, block.defaultBlockState()),
                    x, y, z, 3, 0.5, 0.2, 0.5, 0.0);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0.2, 0.2, 0.2, 0.05);
            }
        }

        if (life > BURST && life % 25 == 0 && !fading) {
            sound(sl, SoundEvents.GLASS_BREAK, 2.0f, 0.5f + rnd.nextFloat() * 0.4f);
        }
    }

    private double[] randDir() {
        double x = rnd.nextGaussian(), y = rnd.nextGaussian(), z = rnd.nextGaussian();
        double l = Math.sqrt(x * x + y * y + z * z);
        if (l < 0.0001) return new double[]{0, 1, 0};
        return new double[]{x / l, y / l, z / l};
    }
}
