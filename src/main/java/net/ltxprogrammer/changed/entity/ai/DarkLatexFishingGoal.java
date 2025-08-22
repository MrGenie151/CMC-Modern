package net.ltxprogrammer.changed.entity.ai;

import net.ltxprogrammer.changed.entity.beast.AbstractDarkLatexEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class DarkLatexFishingGoal extends MoveToBlockGoal {
    public final AbstractDarkLatexEntity entity;
    public final Level level;
    private BlockPos targetWaterSurface = BlockPos.ZERO;
    private final int searchRange;
    private final int verticalSearchRange;

    public DarkLatexFishingGoal(AbstractDarkLatexEntity entity, double speedModifier, int searchRange, int verticalSearchRange) {
        super(entity, speedModifier, searchRange, verticalSearchRange);
        this.entity = entity;
        this.level = entity.level();
        this.searchRange = searchRange;
        this.verticalSearchRange = verticalSearchRange;
    }

    public int findSlotForFishingRod() {
        var inventory = entity.getInventory();
        if (inventory == null)
            return -1;

        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            var slot = inventory.getItem(i);
            if (slot.isEmpty())
                continue;

            if (slot.is(Tags.Items.TOOLS_FISHING_RODS))
                return i;
        }

        return inventory.selected;
    }

    @Override
    public boolean canUse() {
        if (entity.getTarget() != null)
            return false;
        var inventory = entity.getInventory();
        if (inventory == null)
            return false;
        if (entity.getCurrentFavor() != DarkLatexFavor.FISHING)
            return false;

        inventory.selected = this.findSlotForFishingRod();
        if (!entity.getMainHandItem().is(Tags.Items.TOOLS_FISHING_RODS))
            return false;

        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (entity.getTarget() != null)
            return false;
        if (!entity.getMainHandItem().is(Tags.Items.TOOLS_FISHING_RODS))
            return false;
        if (!this.isValidWaterSurface(level, targetWaterSurface))
            return false;

        return super.canContinueToUse();
    }

    @Override
    protected boolean findNearestBlock() {
        if (!super.findNearestBlock())
            return false;

        BlockPos blockpos = this.blockPos;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for(int y = this.verticalSearchStart; y <= this.verticalSearchRange; y = y > 0 ? -y : 1 - y) {
            for(int r = 0; r < this.searchRange; ++r) {
                for(int x = 0; x <= r; x = x > 0 ? -x : 1 - x) {
                    for(int z = x < r && x > -r ? r : 0; z <= r; z = z > 0 ? -z : 1 - z) {
                        blockpos$mutableblockpos.setWithOffset(blockpos, x, y - 1, z);
                        if (this.mob.isWithinRestriction(blockpos$mutableblockpos) && this.isValidWaterSurface(this.mob.level(), blockpos$mutableblockpos)) {
                            this.targetWaterSurface = blockpos$mutableblockpos;
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static Stream<BlockPos> getBlocksInLine(BlockPos start, BlockPos end, float traceThickness) {
        BlockPos delta = end.subtract(start);
        Vec3 deltaCenter = delta.getCenter();
        double checkIndex = 0;
        double deltaCheck = 1d / deltaCenter.length();
        Set<BlockPos> uniqueBlockPos = new HashSet<>();
        while (checkIndex <= 1d) {
            Vec3 checkCenter = deltaCenter.multiply(checkIndex, checkIndex, checkIndex);

            BlockPos.betweenClosedStream(new AABB(
                    checkCenter.subtract(traceThickness, traceThickness, traceThickness),
                    checkCenter.add(traceThickness, traceThickness, traceThickness)
            )).forEach(uniqueBlockPos::add);

            checkIndex += deltaCheck;
        }

        return uniqueBlockPos.stream();
    }

    protected boolean isValidWaterSurface(LevelReader level, BlockPos blockPos) {
        if (blockPos.getX() == this.blockPos.getX() && blockPos.getZ() == this.blockPos.getZ())
            return false; // Water cannot be directly below
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.is(Blocks.WATER))
            return false;
        if (!level.isEmptyBlock(blockPos.above()))
            return false;
        if (!level.isEmptyBlock(blockPos.above().above()))
            return false;

        // Trace from surface to where the entity will fish from
        return Stream.concat(
                getBlocksInLine(this.blockPos.above(), blockPos.above(), 0.5f),
                getBlocksInLine(this.blockPos.above().above(), blockPos.above().above(), 0.5f)
        ).allMatch(tracePos -> {
            if (tracePos.getX() == this.blockPos.getX() && tracePos.getZ() == this.blockPos.getZ())
                return true; // Ignore perch block
            return level.isEmptyBlock(tracePos);
        });
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos blockPos) {
        if (!level.isEmptyBlock(blockPos.above()))
            return false;
        if (!level.isEmptyBlock(blockPos.above().above()))
            return false;
        if (!level.getBlockState(blockPos).isFaceSturdy(level, blockPos, Direction.UP))
            return false;

        return Direction.Plane.HORIZONTAL.stream().anyMatch(direction -> { // Is block on ledge
            BlockPos neighborPos = blockPos.relative(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            return neighbor.isAir() || neighbor.is(Blocks.WATER);
        });
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isReachedTarget()) {
            this.tryTicks = 0; // Causes the DL to stay until other it has a combat target, or has a different favor

            // TODO go fishing
        }
    }
}
