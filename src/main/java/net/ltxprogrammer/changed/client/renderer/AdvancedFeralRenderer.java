package net.ltxprogrammer.changed.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.ltxprogrammer.changed.client.renderer.model.AdvancedHumanoidModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorModelPicker;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorModelSet;
import net.ltxprogrammer.changed.client.renderer.model.armor.LatexHumanoidArmorModel;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public abstract class AdvancedFeralRenderer<T extends ChangedEntity, M extends AdvancedHumanoidModel<T>, A extends LatexHumanoidArmorModel<T, ?>> extends AdvancedHumanoidRenderer<T, M, A> {
    public AdvancedFeralRenderer(EntityRendererProvider.Context context, M main, ArmorModelPicker<? super T> modelPicker, float shadowSize) {
        super(context, main, modelPicker, shadowSize);
    }

    public AdvancedFeralRenderer(EntityRendererProvider.Context context, M main, ArmorModelSet<? super T, ?> modelSet, float shadowSize) {
        super(context, main, modelSet, shadowSize);
    }

    @Override
    protected void setupRotations(@NotNull T entity, PoseStack poseStack, float bob, float bodyYRot, float partialTicks) {
        float swimAmount = entity.getSwimAmount(partialTicks);
        boolean upright = isEntityUprightType(entity);
        if (!upright && swimAmount > 0.0F) {
            super.setupRotations(entity, poseStack, bob, bodyYRot, partialTicks);
            float f3 = entity.isInWater() ? -entity.getXRot() : 0.0F;
            float f4 = Mth.lerp(swimAmount, 0.0F, f3 * 0.75f);
            poseStack.mulPose(Axis.XP.rotationDegrees(f4));
        } else {
            super.setupRotations(entity, poseStack, bob, bodyYRot, partialTicks);
        }
    }

    @Override
    protected boolean isEntityUprightType(@NotNull T entity) {
        return false;
    }
}
