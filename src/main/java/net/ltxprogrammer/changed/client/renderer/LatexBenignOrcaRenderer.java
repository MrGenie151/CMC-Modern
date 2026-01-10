package net.ltxprogrammer.changed.client.renderer;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.client.renderer.layers.LatexParticlesLayer;
import net.ltxprogrammer.changed.client.renderer.layers.TransfurCapeLayer;
import net.ltxprogrammer.changed.client.renderer.model.LatexBenignOrcaModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorLatexOrcaModel;
import net.ltxprogrammer.changed.entity.beast.LatexBenignOrca;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class LatexBenignOrcaRenderer extends AdvancedHumanoidRenderer<LatexBenignOrca, LatexBenignOrcaModel> {
    public static final ResourceLocation DEFAULT_SKIN_LOCATION = Changed.modResource("textures/latex_benign_orca.png");

    public LatexBenignOrcaRenderer(EntityRendererProvider.Context context) {
        super(context, new LatexBenignOrcaModel(context.bakeLayer(LatexBenignOrcaModel.LAYER_LOCATION)), ArmorLatexOrcaModel.MODEL_SET, 0.5f);
        this.addLayer(new LatexParticlesLayer<>(this, getModel()));
        this.addLayer(TransfurCapeLayer.normalCape(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(LatexBenignOrca entity) {
        return DEFAULT_SKIN_LOCATION;
    }
}