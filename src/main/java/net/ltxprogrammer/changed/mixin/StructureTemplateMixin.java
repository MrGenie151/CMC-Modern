package net.ltxprogrammer.changed.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.world.features.structures.HangingBlockFixerProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin {
    @WrapMethod(method = "load")
    public void updateWithCDFU(HolderGetter<Block> registry, CompoundTag tag, Operation<Void> original) {
        if (Changed.dataFixer != null)
            Changed.dataFixer.updateCompoundTag(DataFixTypes.STRUCTURE, tag);
        original.call(registry, tag);
    }

    @Inject(method = "lambda$addEntitiesToWorld$5",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;moveTo(DDDFF)V", shift = At.Shift.AFTER))
    private static void fixPaintingPlacement(StructurePlaceSettings placementIn, Vec3 vec31, ServerLevelAccessor level, CompoundTag compoundtag, Entity entity, CallbackInfo ci) {
        if (placementIn.getProcessors().stream().noneMatch(HangingBlockFixerProcessor.INSTANCE::equals))
            return; // Require HangingBlockFixerProcessor.INSTANCE for compatibility

        // Code below from BluSpring (2/16/2024) at https://bugs.mojang.com/browse/MC/issues/MC-102223
        if (!(entity instanceof Painting painting)) {
            return;
        }

        var pos = new BlockPos.MutableBlockPos();
        pos.set(painting.getPos());
        var variant = painting.getVariant().value();

        var width = variant.getWidth() / 16;
        var height = variant.getHeight() / 16;
        var direction = painting.getDirection();

        // paintings with an even height seem to always be moved upwards...
        if (height % 2 == 0) {
            pos.move(0, -1, 0);
        }

        // paintings with an even width seem to be moved in the clockwise direction of their facing direction,
        // if they're west or south.
        if (width % 2 == 0 && (direction == Direction.WEST || direction == Direction.SOUTH)) {
            var moveTo = direction.getClockWise().getNormal();
            pos.move(moveTo);
        }

        painting.setPos(pos.getCenter());
    }
}
