package net.ltxprogrammer.changed.fluid;

import net.ltxprogrammer.changed.init.ChangedBlocks;
import net.ltxprogrammer.changed.util.Color3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public abstract class Gas extends ForgeFlowingFluid {
    protected Gas(Properties properties) {
        super(properties);
    }

    @NotNull
    public static FluidType.Properties createProperties() {
        return FluidType.Properties.create()
                .density(200)
                .viscosity(200)
                .motionScale(0.0D)
                .fallDistanceModifier(1.0f)
                .canDrown(false)
                .canPushEntity(false)
                .canSwim(false);
    }

    public static class GasFluidType extends FluidType {
        public GasFluidType(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isAir() { // Requires accesstransformer to work
            return true;
        }
    }

    public abstract Color3 getColor();

    @Override
    public float getOwnHeight(FluidState state) {
        return Mth.map(super.getOwnHeight(state), 0.0f, 1.0f, 0.65f, 1.0f);
    }

    @Override
    protected FluidState getNewLiquid(Level level, BlockPos pos, BlockState state) {
        // Overwritten from FlowingFluid.getNewLiquid()

        int maxNeighborAmount = 0;
        int sourceNeighbors = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.relative(direction);
            BlockState blockstate = level.getBlockState(blockpos);
            FluidState fluidstate = blockstate.getFluidState();
            if (fluidstate.getType().isSame(this) && this.canPassThroughWall(direction, level, pos, state, blockpos, blockstate)) {
                if (fluidstate.isSource() && net.minecraftforge.event.ForgeEventFactory.canCreateFluidSource(level, blockpos, blockstate, this.canConvertToSource(level))) {
                    ++sourceNeighbors;
                }

                maxNeighborAmount = Math.max(maxNeighborAmount, fluidstate.getAmount());
            }
        }

        if (sourceNeighbors >= 2) {
            BlockState surfaceBlockState = level.getBlockState(pos.below());
            FluidState surfaceFluidState = surfaceBlockState.getFluidState();
            if (surfaceBlockState.isSolid() || this.isSourceBlockOfThisType(surfaceFluidState)) {
                return this.getSource(false);
            }
        }

        BlockPos abovePos = pos.above();
        BlockState aboveBlockState = level.getBlockState(abovePos);
        FluidState aboveFluidState = aboveBlockState.getFluidState();
        if (!aboveFluidState.isEmpty() && aboveFluidState.getType().isSame(this) && this.canPassThroughWall(Direction.UP, level, pos, state, abovePos, aboveBlockState)) {
            return this.getFlowing(Math.min(aboveFluidState.getAmount(), 7), false); // Prevents amount from resetting to 8 when going down
        } else {
            int k = maxNeighborAmount - this.getDropOff(level);
            return k <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(k, false);
        }
    }

    @Override
    protected boolean isWaterHole(BlockGetter level, Fluid fluid, BlockPos pos, BlockState state, BlockPos otherPos, BlockState otherState) {
        return false; // Allows gas to spread on top of itself
    }

    @Override
    protected boolean canSpreadTo(BlockGetter level, BlockPos pos, BlockState state, Direction direction, BlockPos otherPos, BlockState otherState, FluidState otherFluidState, Fluid fluid) {
        return super.canSpreadTo(level, pos, state, direction, otherPos, otherState, otherFluidState, fluid)
                && !otherState.is(ChangedBlocks.FRESH_AIR.get())
                && (otherState.is(Blocks.AIR) || otherState.is(Blocks.CAVE_AIR))
                && (otherFluidState.isEmpty() || otherFluidState.is(this));
    }

    @Override
    protected void animateTick(Level level, BlockPos blockPos, FluidState state, RandomSource random) {
        super.animateTick(level, blockPos, state, random);

        float fluidLevel = state.getAmount() / 8f;
        if (random.nextFloat() < 0.1F * fluidLevel) {
            Color3 color = getColor().lerp(0.2f + random.nextFloat() * 0.2f, Color3.fromInt(0xbfbfbf));

            float x = blockPos.getX();
            float y = blockPos.getY();
            float z = blockPos.getZ();
            float dx = random.nextFloat();
            float dy = random.nextFloat() * fluidLevel;
            float dz = random.nextFloat();
            level.addParticle(
                    new DustParticleOptions(new Vector3f(color.red(), color.green(), color.blue()), 1.0F),
                    x + dx, y + dy, z + dz,
                    0.0D, 0.0D, 0.0D);
        }
    }
}
