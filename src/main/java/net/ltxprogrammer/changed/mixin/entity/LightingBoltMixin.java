package net.ltxprogrammer.changed.mixin.entity;

import net.ltxprogrammer.changed.init.ChangedLatexTypes;
import net.ltxprogrammer.changed.world.LatexCoverState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
public abstract class LightingBoltMixin extends Entity {
    @Shadow protected abstract BlockPos getStrikePosition();

    public LightingBoltMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "powerLightningRod", at = @At("TAIL"), cancellable = false)
    private void latexCoverIsStruckByLighting(CallbackInfo ci) {
        // Latex is Weak to Shock, and a LightingBolt is a very powerful shock soo it die when struck by it
        BlockPos strikePosition = this.getStrikePosition();
        Level level = level();
        LatexCoverState strikePositionCoverState = LatexCoverState.getAt(level, strikePosition);
        if (strikePositionCoverState.isAir()) return;
        LatexCoverState.setAtAndUpdate(level, strikePosition, ChangedLatexTypes.NONE.get().defaultCoverState());
    }
}