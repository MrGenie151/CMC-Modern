package net.ltxprogrammer.changed.mixin.compatibility.SleepTight;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.ltxprogrammer.changed.client.renderer.animate.HumanoidAnimator;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.ltxprogrammer.changed.extension.RequiredMods;
import net.mehvahdjukaar.sleep_tight.common.entities.BedEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = HumanoidAnimator.AnimateStage.class, remap = false)
@RequiredMods("sleep_tight")
public abstract class HumanoidAnimatorMixin {
    @WrapMethod(method = "isRiding(Lnet/ltxprogrammer/changed/client/renderer/animate/HumanoidAnimator;Lnet/ltxprogrammer/changed/entity/ChangedEntity;)Z")
    private static boolean isRidingAndNotSleeping(HumanoidAnimator<?, ?> animator, ChangedEntity entity, Operation<Boolean> original) {
        if (entity.getVehicle() instanceof BedEntity)
            return false;
        return original.call(animator, entity);
    }

    @WrapMethod(method = "isSleeping(Lnet/ltxprogrammer/changed/client/renderer/animate/HumanoidAnimator;Lnet/ltxprogrammer/changed/entity/ChangedEntity;)Z")
    private static boolean isSleepingAndNotRiding(HumanoidAnimator<?, ?> animator, ChangedEntity entity, Operation<Boolean> original) {
        if (entity.getVehicle() instanceof BedEntity)
            return true;
        return original.call(animator, entity);
    }
}
