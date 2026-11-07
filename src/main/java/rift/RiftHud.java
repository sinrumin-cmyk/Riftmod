package rift;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/** Red screen tint + dark red edges while near a rift. */
public final class RiftHud {
    private RiftHud() {}

    public static void register() {
        HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath("rift", "overlay"), (graphics, tracker) -> {
            float c = ClientRiftState.current;
            if (c <= 0.002f) return;
            Minecraft mc = Minecraft.getInstance();
            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();
            double pulse = 0.85 + 0.15 * Math.sin(System.currentTimeMillis() / 220.0);
            int a = (int) Math.min(255, 70 * c * pulse);
            graphics.fill(0, 0, w, h, (a << 24) | 0x9B0000);
            int edge = Math.max(8, (int) (60 * c));
            int ea = (int) Math.min(255, 130 * c * pulse);
            int col = (ea << 24) | 0x200000;
            graphics.fill(0, 0, w, edge, col);
            graphics.fill(0, h - edge, w, h, col);
            graphics.fill(0, 0, edge, h, col);
            graphics.fill(w - edge, 0, w, h, col);
        });
    }
}
