package net.ltxprogrammer.changed.client.renderer;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.client.renderer.layers.CustomCoatLayer;
import net.ltxprogrammer.changed.client.renderer.layers.CustomEyesLayer;
import net.ltxprogrammer.changed.client.renderer.model.GasWolfPupModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorNoneModel;
import net.ltxprogrammer.changed.entity.beast.GasWolfPup;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;

public class GasWolfPupRenderer extends AdvancedHumanoidRenderer<GasWolfPup, GasWolfPupModel, ArmorNoneModel<GasWolfPup>> {
	public GasWolfPupRenderer(EntityRendererProvider.Context context) {
		super(context, new GasWolfPupModel(context.bakeLayer(GasWolfPupModel.LAYER_LOCATION)), ArmorNoneModel.MODEL_SET, 0.4F);
        this.addLayer(new CustomCoatLayer<>(this, this.getModel(), Changed.modResource("textures/gas_wolf_pup_coat")));
        this.addLayer(CustomEyesLayer.builder(this, context.getModelSet())
                .build().setHeadShape(CustomEyesLayer.HeadShape.PUP));
	}

	@Override
	public ResourceLocation getTextureLocation(GasWolfPup entity) {
		return Changed.modResource("textures/gas_wolf_pup.png");
	}

	@Override
	protected float getFlipDegrees(GasWolfPup entity) {
		return entity.getPose() == Pose.SLEEPING ? 0.0F : super.getFlipDegrees(entity);
	}

	@Override
	protected boolean isEntityUprightType(@NotNull GasWolfPup entity) {
		return false;
	}
}