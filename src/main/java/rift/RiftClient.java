package rift;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RiftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(RiftMod.RIFT, RiftRenderer::new);
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientRiftState.tick());
        RiftHud.register(); // delete this line + RiftHud.java if the HUD API differs
    }
}
