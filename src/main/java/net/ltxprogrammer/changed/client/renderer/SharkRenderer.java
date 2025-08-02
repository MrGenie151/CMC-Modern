package net.ltxprogrammer.changed.client.renderer;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.client.renderer.layers.LatexParticlesLayer;
import net.ltxprogrammer.changed.client.renderer.model.SharkModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorNoneModel;
import net.ltxprogrammer.changed.entity.beast.FeralShark;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SharkRenderer extends AdvancedFeralRenderer<FeralShark, SharkModel<FeralShark>, ArmorNoneModel<FeralShark>> {
    private static final ResourceLocation SHARK_LOCATION = Changed.modResource("textures/shark.png");

    public SharkRenderer(EntityRendererProvider.Context context) {
        super(context, new SharkModel<>(context.bakeLayer(SharkModel.LAYER_LOCATION)), ArmorNoneModel.MODEL_SET, 0.7F);
        this.addLayer(new LatexParticlesLayer<>(this, this.model));
    }

    public ResourceLocation getTextureLocation(FeralShark shark) {
        return SHARK_LOCATION;
    }
}