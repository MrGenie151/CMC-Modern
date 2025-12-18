package net.ltxprogrammer.changed.mixin.forge;

import net.ltxprogrammer.changed.fluid.Gas;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidType.class)
public class FluidTypeMixin {

    @Inject(method = "isAir", at = @At("RETURN"), remap = false, cancellable = true)
    private void hookIsAir(CallbackInfoReturnable<Boolean> cir) {
        FluidType self = (FluidType) (Object) this;
        if (self instanceof Gas.GasFluidType) {
            cir.setReturnValue(true);
        }
    }
}
