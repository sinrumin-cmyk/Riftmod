package rift;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

public class RiftRenderer extends GeoEntityRenderer<RiftEntity, EntityRenderState> {
    public RiftRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<RiftEntity>(Identifier.fromNamespaceAndPath("rift", "rift")));
    }

    @Override
    public void extractRenderState(RiftEntity entity, EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.lightCoords = 15728880; // full bright, rift glows
    }
}
