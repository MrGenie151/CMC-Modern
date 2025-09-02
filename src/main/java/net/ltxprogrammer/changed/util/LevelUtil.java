package net.ltxprogrammer.changed.util;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.stream.Stream;

public class LevelUtil {
    public static Stream<BlockPos> getBlocksInLine(BlockPos start, BlockPos end, float traceThickness) {
        BlockPos delta = end.subtract(start);
        Vec3 deltaCenter = Vec3.atLowerCornerOf(delta);
        double deltaCheck = 1d / deltaCenter.length();

        final Set<BlockPos> checkedBlocks = new ObjectArraySet<>();
        return Stream.iterate(0d, distance -> distance <= 1d, distance -> distance + deltaCheck)
                .flatMap(checkDistance -> {
                    Vec3 checkCenter = deltaCenter.multiply(checkDistance, checkDistance, checkDistance).add(0.5, 0.5, 0.5);

                    return BlockPos.betweenClosedStream(new AABB(
                            checkCenter.subtract(traceThickness, traceThickness, traceThickness),
                            checkCenter.add(traceThickness, traceThickness, traceThickness)
                    )).map(checkPos -> checkPos.offset(start));
                }).filter(checkedBlocks::add);
    }
}
