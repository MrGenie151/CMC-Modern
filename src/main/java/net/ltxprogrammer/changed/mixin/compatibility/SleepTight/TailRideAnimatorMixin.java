package net.ltxprogrammer.changed.mixin.compatibility.SleepTight;

import net.ltxprogrammer.changed.client.renderer.animate.bipedal.BipedalRideAnimator;
import net.ltxprogrammer.changed.client.renderer.animate.tail.AbstractTailAnimator;
import net.ltxprogrammer.changed.client.renderer.animate.tail.TailRideAnimator;
import net.ltxprogrammer.changed.client.renderer.model.AdvancedHumanoidModel;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.ltxprogrammer.changed.extension.RequiredMods;
import net.mehvahdjukaar.sleep_tight.common.entities.BedEntity;
import net.minecraft.client.model.geom.ModelPart;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = TailRideAnimator.class, remap = false)
@RequiredMods("sleep_tight")
public abstract class TailRideAnimatorMixin<T extends ChangedEntity, M extends AdvancedHumanoidModel<T>> extends AbstractTailAnimator<T, M> {

    public TailRideAnimatorMixin(ModelPart tail, List<ModelPart> tailJoints) {
        super(tail, tailJoints);
    }

    @Inject(method = "setupAnim", at = @At("HEAD"), cancellable = true)
    public <T extends ChangedEntity> void cancelAnimation(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity.getVehicle() instanceof BedEntity) {
            ci.cancel();
            tail.xRot = -1.4137167F;
        }
    }
}
