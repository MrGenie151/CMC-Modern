package net.ltxprogrammer.changed.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class BeehiveWall extends Block {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty[] FACES = { NORTH, EAST, SOUTH, WEST, UP, DOWN };
    public static final Map<Direction, BooleanProperty> BY_DIRECTION = Map.of(
            Direction.UP, UP, Direction.DOWN, DOWN, Direction.NORTH, NORTH, Direction.SOUTH, SOUTH, Direction.EAST, EAST, Direction.WEST, WEST);

    public BeehiveWall() {
        super(Properties.copy(Blocks.BEE_NEST));
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case NONE -> state;
            case CLOCKWISE_90 ->
                    state.setValue(NORTH, state.getValue(WEST))
                            .setValue(EAST, state.getValue(NORTH))
                            .setValue(SOUTH, state.getValue(EAST))
                            .setValue(WEST, state.getValue(SOUTH));
            case CLOCKWISE_180 ->
                    state.setValue(NORTH, state.getValue(SOUTH))
                            .setValue(EAST, state.getValue(WEST))
                            .setValue(SOUTH, state.getValue(NORTH))
                            .setValue(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90 ->
                    state.setValue(NORTH, state.getValue(EAST))
                            .setValue(EAST, state.getValue(SOUTH))
                            .setValue(SOUTH, state.getValue(WEST))
                            .setValue(WEST, state.getValue(NORTH));
        };
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return switch (mirror) {
            case NONE -> state;
            case LEFT_RIGHT -> state.setValue(NORTH, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(NORTH));
            case FRONT_BACK -> state.setValue(EAST, state.getValue(WEST)).setValue(WEST, state.getValue(EAST));
        };
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if (toolAction != ToolActions.AXE_STRIP)
            return super.getToolModifiedState(state, context, toolAction, simulate);

        var property = BY_DIRECTION.get(context.getClickedFace());
        if (state.getValue(property))
            return super.getToolModifiedState(state, context, toolAction, simulate);

        return state.setValue(property, true);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of(new ItemStack(this.asItem()));
    }
}
