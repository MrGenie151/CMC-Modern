package net.ltxprogrammer.changed.mixin.compatibility.SleepTight;

import net.ltxprogrammer.changed.client.renderer.animate.HumanoidAnimator;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.ltxprogrammer.changed.extension.RequiredMods;
import net.mehvahdjukaar.sleep_tight.common.entities.BedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = HumanoidAnimator.AnimateStage.class, remap = false)
@RequiredMods("sleep_tight")
public abstract class HumanoidAnimatorMixin {

    @Inject(method = "test(Lnet/ltxprogrammer/changed/client/renderer/animate/HumanoidAnimator;Lnet/ltxprogrammer/changed/entity/ChangedEntity;)Z", at = @At("HEAD"), cancellable = true)
    public void cancelAnimation(HumanoidAnimator<?, ?> animator, ChangedEntity latex, CallbackInfoReturnable<Boolean> cir) {
        HumanoidAnimator.AnimateStage self = (HumanoidAnimator.AnimateStage) (Object) this;
        if (self == HumanoidAnimator.AnimateStage.RIDE) {
            if (latex.getVehicle() instanceof BedEntity) {
                cir.setReturnValue(false);
            }
        } else if (self == HumanoidAnimator.AnimateStage.SLEEP) {
            if (latex.getVehicle() instanceof BedEntity) {
                cir.setReturnValue(true);
            }
        } // Following the Order -> INIT, RIDE, SLEEP, ATTACK, CROUCH, STAND, BOB, CREATIVE_FLY, FALL_FLY, SWIM, FINAL
    }
}
