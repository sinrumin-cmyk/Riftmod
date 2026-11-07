package rift.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rift.ClientRiftState;

/** Tints sky/cloud color red near a rift. Silently does nothing if the methods do not exist (require = 0). */
@Mixin(ClientLevel.class)
public abstract class SkyMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void rift$sky(CallbackInfoReturnable<Object> cir) {
        tint(cir);
    }

    @Inject(method = "getCloudColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void rift$clouds(CallbackInfoReturnable<Object> cir) {
        tint(cir);
    }

    private static void tint(CallbackInfoReturnable<Object> cir) {
        float k = Math.min(1f, ClientRiftState.current * 0.9f);
        if (k <= 0.001f) return;
        Object v = cir.getReturnValue();
        if (v instanceof Integer c) {
            int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, b = c & 0xFF;
            r = (int) (r + (110 - r) * k);
            g = (int) (g + (0 - g) * k);
            b = (int) (b + (10 - b) * k);
            cir.setReturnValue((c & 0xFF000000) | (r << 16) | (g << 8) | b);
        } else if (v instanceof Vec3 vec) {
            cir.setReturnValue(new Vec3(
                vec.x + (0.45 - vec.x) * k,
                vec.y + (0.0 - vec.y) * k,
                vec.z + (0.04 - vec.z) * k));
        }
    }
}
