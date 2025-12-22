package net.ltxprogrammer.changed.mixin.compatibility.SleepTight;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.extension.RequiredMods;
import net.ltxprogrammer.changed.process.ProcessTransfur;
import net.mehvahdjukaar.sleep_tight.common.blocks.IModBed;
import net.mehvahdjukaar.sleep_tight.common.entities.BedEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.mehvahdjukaar.sleep_tight.common.entities.BedEntity.getDoubleBedOffset;

@Mixin(value = BedEntity.class, remap = false)
@RequiredMods("sleep_tight")
public abstract class BedEntityMixin {

    @Shadow
    private Direction dir;

    @Shadow
    public abstract boolean isDoubleBed();

    @Shadow
    private BlockState lastBedState;

    @Inject(
            method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void applyHeadOffset(
            Entity passenger,
            Entity.MoveFunction callback,
            CallbackInfo ci
    ) {
        BedEntity self = (BedEntity) (Object) this;
        if (!(passenger instanceof LivingEntity living)) return;
        if (!self.hasPassenger(passenger)) return;

        // only apply if is a transfured player
        if (living instanceof Player player && !ProcessTransfur.isPlayerTransfurred(player)) return;

        // base position
        Vec3 basePos;

        if (this.lastBedState.getBlock() instanceof IModBed bed) {
            basePos = bed.getSleepingPosition(this.lastBedState, self.blockPosition());
        } else {
            basePos = self.position();
            if (this.isDoubleBed()) {
                basePos = getDoubleBedOffset(this.dir.getOpposite(), basePos);
            }
        }

        // OFFSET
        Direction dir = this.dir;

        Vec3 adjusted = basePos.add(
                -dir.getStepX() * -0.25D,
                0.0D,
                -dir.getStepZ() * -0.25D
        );

        callback.accept(passenger, adjusted.x, adjusted.y, adjusted.z);

        ci.cancel();
    }
}
