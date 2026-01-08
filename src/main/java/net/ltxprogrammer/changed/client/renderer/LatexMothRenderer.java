package net.ltxprogrammer.changed.client.renderer;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.client.renderer.layers.CustomEyesLayer;
import net.ltxprogrammer.changed.client.renderer.layers.GasMaskLayer;
import net.ltxprogrammer.changed.client.renderer.layers.LatexParticlesLayer;
import net.ltxprogrammer.changed.client.renderer.layers.TransfurCapeLayer;
import net.ltxprogrammer.changed.client.renderer.model.LatexMothModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorLatexMothModel;
import net.ltxprogrammer.changed.entity.beast.LatexMoth;
import net.ltxprogrammer.changed.util.Color3;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class LatexMothRenderer extends AdvancedHumanoidRenderer<LatexMoth, LatexMothModel> {
    public static final ResourceLocation DEFAULT_SKIN_LOCATION = Changed.modResource("textures/latex_moth.png");

    public LatexMothRenderer(EntityRendererProvider.Context context) {
        super(context, new LatexMothModel(context.bakeLayer(LatexMothModel.LAYER_LOCATION)), ArmorLatexMothModel.MODEL_SET, 0.5f);
        this.addLayer(new LatexParticlesLayer<>(this, getModel()));
        this.addLayer(TransfurCapeLayer.normalCape(this, context.getModelSet()));
        this.addLayer(CustomEyesLayer.builder(this, context.getModelSet())
                .withSclera(Color3.fromInt(0x1b1b1b)).build());
        this.addLayer(GasMaskLayer.forNormal(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(LatexMoth entity) {
        return DEFAULT_SKIN_LOCATION;
    }
}