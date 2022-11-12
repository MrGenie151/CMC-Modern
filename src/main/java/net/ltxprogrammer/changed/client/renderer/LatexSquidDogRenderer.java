package net.ltxprogrammer.changed.client.renderer;

import net.ltxprogrammer.changed.client.renderer.model.LatexSquidDogModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorLatexWolfModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorNoneModel;
import net.ltxprogrammer.changed.entity.beast.LatexSquidDog;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class LatexSquidDogRenderer extends LatexHumanoidRenderer<LatexSquidDog, LatexSquidDogModel, ArmorNoneModel<LatexSquidDog>> {
    public LatexSquidDogRenderer(EntityRendererProvider.Context context) {
        super(context, new LatexSquidDogModel(context.bakeLayer(LatexSquidDogModel.LAYER_LOCATION)),
                ArmorNoneModel::new, ArmorNoneModel.INNER_ARMOR, ArmorNoneModel.OUTER_ARMOR, 0.65f);
    }

    @Override
    public ResourceLocation getTextureLocation(LatexSquidDog p_114482_) {
        return new ResourceLocation("changed:textures/latex_squid_dog.png");
    }
}