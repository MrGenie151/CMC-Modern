package net.ltxprogrammer.changed.mixin.compatibility.SleepTight;

import net.ltxprogrammer.changed.client.renderer.animate.HumanoidAnimator;
import net.ltxprogrammer.changed.client.renderer.animate.arm.ArmRideAnimator;
import net.ltxprogrammer.changed.client.renderer.model.AdvancedHumanoidModel;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.ltxprogrammer.changed.extension.RequiredMods;
import net.mehvahdjukaar.sleep_tight.common.entities.BedEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ArmRideAnimator.class, remap = false)
@RequiredMods("sleep_tight")
public abstract class ArmRideAnimatorMixin<T extends ChangedEntity, M extends AdvancedHumanoidModel<T>> extends HumanoidAnimator.Animator<T, M> {

    @Inject(method = "setupAnim", at = @At("HEAD"), cancellable = true)
    public void cancelAnimation(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity.getVehicle() instanceof BedEntity) {
            ci.cancel();
        }
    }
}
