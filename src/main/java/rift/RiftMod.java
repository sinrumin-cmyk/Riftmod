package rift;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.Vec3;

public class RiftMod implements ModInitializer {
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

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
                Commands.literal("rift")
                    .executes(ctx -> {
                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                        ServerLevel lvl = (ServerLevel) p.level();
                        Vec3 look = p.getLookAngle();
                        double hx = look.x, hz = look.z;
                        double len = Math.sqrt(hx * hx + hz * hz);
                        if (len < 0.001) { hx = 0; hz = 1; len = 1; }
                        hx /= len; hz /= len;
                        RiftEntity e = new RiftEntity(RIFT, lvl);
                        e.setPos(p.getX() + hx * 14.0, p.getY(), p.getZ() + hz * 14.0);
                        lvl.addFreshEntity(e);
                        return 1;
                    })
                    .then(Commands.literal("clear").executes(ctx -> {
                        for (RiftEntity e : RiftEntity.LIVE) e.discard();
                        RiftEntity.LIVE.clear();
                        return 1;
                    }))
            )
        );
    }
}
