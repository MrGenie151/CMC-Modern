package net.ltxprogrammer.changed.client.renderer;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.client.renderer.layers.*;
import net.ltxprogrammer.changed.client.renderer.model.LatexGnollTaurModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorLatexCentaurLowerModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorLatexFemaleTaurUpperModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorModelPicker;
import net.ltxprogrammer.changed.entity.beast.LatexGnollTaur;
import net.ltxprogrammer.changed.util.Color3;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.resources.ResourceLocation;

public class LatexGnollTaurRenderer extends AdvancedHumanoidRenderer<LatexGnollTaur, LatexGnollTaurModel, ArmorLatexFemaleTaurUpperModel<LatexGnollTaur>> {
    public LatexGnollTaurRenderer(EntityRendererProvider.Context context) {
        super(context, new LatexGnollTaurModel(context.bakeLayer(LatexGnollTaurModel.LAYER_LOCATION)),
                ArmorModelPicker.centaur(context.getModelSet(), ArmorLatexFemaleTaurUpperModel.MODEL_SET, ArmorLatexCentaurLowerModel.MODEL_SET_WITH_TORSO), 0.7f);
        this.addLayer(new LatexParticlesLayer<>(this, getModel()));
        this.addLayer(new CustomEyesLayer<>(this, context.getModelSet(),
                CustomEyesLayer.fixedColor(Color3.parseHex("#ffffff")),
                CustomEyesLayer.fixedColor(Color3.parseHex("#b3e53a"))));
        this.addLayer(new SaddleLayer<>(this, getModel(), Changed.modResource("textures/latex_gnoll_taur_saddle.png")));
        this.addLayer(new TaurChestPackLayer<>(this, context.getModelSet()));
        this.addLayer(TransfurCapeLayer.shortCape(this, context.getModelSet()));
        this.addLayer(GasMaskLayer.forSnouted(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(LatexGnollTaur p_114482_) {
        return Changed.modResource("textures/latex_gnoll_taur.png");
    }
}