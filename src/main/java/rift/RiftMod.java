package rift;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RiftMod implements ModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("rift");
    public static EntityType<RiftEntity> RIFT;

    @Override
    public void onInitialize() {
        Identifier id = Identifier.fromNamespaceAndPath("rift", "rift");
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        RIFT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, id,
            EntityType.Builder.<RiftEntity>of(RiftEntity::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .clientTrackingRange(10)
                .updateInterval(20)
                .fireImmune()
                .build(key)
        );
        LOG.info("[rift] entity type registered as {}", id);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LOG.info("[rift] registering /rift command");
            dispatcher.register(
                Commands.literal("rift")
                    .executes(ctx -> run(ctx.getSource()))
                    .then(Commands.literal("clear").executes(ctx -> {
                        for (RiftEntity e : RiftEntity.LIVE) e.discard();
                        RiftEntity.LIVE.clear();
                        return 1;
                    }))
            );
        });
    }

    /** Spawns a rift 14 blocks ahead of the player. Any failure is printed to chat AND the log. */
    private static int run(CommandSourceStack src) {
        try {
            ServerPlayer p = src.getPlayerOrException();
            ServerLevel lvl = (ServerLevel) p.level();
            Vec3 look = p.getLookAngle();
            double hx = look.x, hz = look.z;
            double len = Math.sqrt(hx * hx + hz * hz);
            if (len < 0.001) { hx = 0; hz = 1; len = 1; }
            hx /= len; hz /= len;

            LOG.info("[rift] step 1: creating entity (RIFT={})", RIFT);
            RiftEntity e = new RiftEntity(RIFT, lvl);
            LOG.info("[rift] step 2: positioning");
            e.setPos(p.getX() + hx * 14.0, p.getY(), p.getZ() + hz * 14.0);
            LOG.info("[rift] step 3: adding to level");
            boolean ok = lvl.addFreshEntity(e);
            LOG.info("[rift] step 4: addFreshEntity returned {}", ok);
            if (!ok) {
                src.sendFailure(Component.literal("[rift] addFreshEntity returned false"));
                return 0;
            }
            src.sendSuccess(() -> Component.literal("[rift] spawned"), false);
            return 1;
        } catch (Throwable t) {
            LOG.error("[rift] /rift failed", t);
            StackTraceElement[] st = t.getStackTrace();
            StringBuilder sb = new StringBuilder("[rift] ").append(t);
            for (int i = 0; i < Math.min(4, st.length); i++) sb.append("\n  at ").append(st[i]);
            src.sendFailure(Component.literal(sb.toString()));
            return 0;
        }
    }
}
