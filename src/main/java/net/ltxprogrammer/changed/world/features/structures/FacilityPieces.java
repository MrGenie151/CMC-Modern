package net.ltxprogrammer.changed.world.features.structures;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.block.GluBlock;
import net.ltxprogrammer.changed.init.ChangedFacilityPieceTypes;
import net.ltxprogrammer.changed.init.ChangedRegistry;
import net.ltxprogrammer.changed.util.CollectionUtil;
import net.ltxprogrammer.changed.util.ResourceUtil;
import net.ltxprogrammer.changed.world.features.structures.facility.*;
import net.ltxprogrammer.changed.world.features.structures.facility.types.PieceType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;

public class FacilityPieces extends SimplePreparableReloadListener<Set<ConfiguredFacilityPiece>> {
    public static FacilityPieces INSTANCE = new FacilityPieces();

    private final Map<PieceType<?>, FacilityPieceCollection> facilityPieceCollections = new HashMap<>();

    private ConfiguredFacilityPiece processJSONFile(JsonObject root) {
        return ConfiguredFacilityPiece.CODEC.decode(JsonOps.INSTANCE, root)
                .getOrThrow(false, error -> { throw new RuntimeException(error); }).getFirst();
    }

    @Override
    @NotNull
    public Set<ConfiguredFacilityPiece> prepare(ResourceManager resources, @Nonnull ProfilerFiller profiler) {
        return ResourceUtil.processJSONResources(new HashSet<>(), resources, "worldgen/changed/facility", (list, filename, id, json) -> {
            list.add(processJSONFile(json));
        }, (exception, filename) -> Changed.LOGGER.error("Failed to load facility piece configuration from \"{}\" : {}", filename, exception));
    }

    @Override
    protected void apply(@NotNull Set<ConfiguredFacilityPiece> output, @NotNull ResourceManager resources, @NotNull ProfilerFiller profiler) {
        for (var pieceType : ChangedRegistry.FACILITY_PIECE_TYPES.get().getValues()) {
            FacilityPieceCollectionBuilder builder = new FacilityPieceCollectionBuilder();
            output.stream().filter(piece -> piece.facilityPiece().getType() == pieceType).forEach(builder::register);

            Changed.postModLoadingEvent(new GatherFacilityPiecesEvent(pieceType, builder));

            facilityPieceCollections.put(pieceType, builder.build());
        }
    }

    public static FacilityPieceCollection getPiecesOfType(PieceType<?> pieceType) {
        return INSTANCE.facilityPieceCollections.get(pieceType);
    }

    private static BlockPos gluNeighbor(BlockPos gluPos, BlockState gluState) {
        return gluPos.relative(gluState.getValue(GluBlock.ORIENTATION).front());
    }
    
    public static boolean isNotCompletelyInsideRegion(BoundingBox boundingBox, BoundingBox region) {
        return boundingBox.minX() < region.minX() || boundingBox.minY() < region.minY() || boundingBox.minZ() < region.minZ() ||
                boundingBox.maxX() > region.maxX() || boundingBox.maxY() > region.maxY() || boundingBox.maxZ() > region.maxZ();
    }

    private static Predicate<ConfiguredFacilityPiece> pieceConnectsToZone(Zone zone) {
        return configuredFacilityPiece -> {
            return configuredFacilityPiece.connectsTo().isEmpty() || configuredFacilityPiece.connectsTo().contains(zone);
        };
    }

    private static void treeGenerate(StructurePiecesBuilder builder, Structure.GenerationContext context,
                                     Stack<FacilityPiece> stack, StructurePiece parentStructure,
                                     GenStep start, int genDepth, int span, BoundingBox allowedRegion) {
        var parent = stack.peek();
        var zone = start.getZone();

        int reroll = 10;
        while (reroll > 0) {
            PieceType<?> pieceType;
            BoundingBox allowedRegionForPiece;
            if (parent.type == ChangedFacilityPieceTypes.SPLIT.get() && reroll == 1) { // Split pieces will dead-end if it's too close to the gen region
                pieceType = ChangedFacilityPieceTypes.SEAL.get();
                allowedRegionForPiece = BoundingBox.infinite();
            } else if (span == 0) {
                pieceType = ChangedFacilityPieceTypes.ROOM.get();
                allowedRegionForPiece = allowedRegion;
            } else {
                var type = start.validTypes().getRandom(context.random());
                if (type.isEmpty())
                    break;
                pieceType = type.get().getPieceType();
                allowedRegionForPiece = allowedRegion;
            }

            boolean placed = INSTANCE.facilityPieceCollections.get(pieceType).shuffledStream(context.random())
                    .filter(pieceConnectsToZone(zone)).map(ConfiguredFacilityPiece::facilityPiece).anyMatch(nextPiece -> {
                var nextStructure = nextPiece.createStructurePiece(context.structureTemplateManager(), genDepth);
                if (!nextStructure.setupBoundingBox(builder, start.blockInfo(), context.random(), allowedRegionForPiece))
                    return false;

                var startPos = gluNeighbor(start.blockInfo().pos(), start.blockInfo().state());
                builder.addPiece(nextStructure);

                if (span <= 0)
                    return false;

                int nextSpan = pieceType.shouldConsumeSpan() ? span - 1 : span;
                stack.push(nextPiece);

                var genStack = new FacilityGenerationStack(stack, nextStructure.getBoundingBox(), context, nextSpan);
                List<GenStep> starts = new ArrayList<>();
                nextStructure.addSteps(genStack, starts);

                int piecesBefore = ((StructurePiecesBuilderExtender)builder).pieceCount();
                starts.stream().filter(next -> !next.blockInfo().pos().equals(startPos)).forEach(next -> {
                    treeGenerate(builder, context, stack, nextStructure, next, genDepth, nextSpan, allowedRegion);
                });
                int piecesAfter = ((StructurePiecesBuilderExtender)builder).pieceCount();

                stack.pop();

                if (piecesAfter > piecesBefore) // Successfully generated pieces that attach to this one
                    return true;

                // No piece was generated to attach to this one
                if (pieceType == ChangedFacilityPieceTypes.ROOM.get() || pieceType == ChangedFacilityPieceTypes.SEAL.get())
                    return true; // This behaviour is expected for a room

                // Attempt to regenerate this piece as a room, to prevent a dead end
                ((StructurePiecesBuilderExtender)builder).removePiece(nextStructure);

                StructurePiece pieceToPut = INSTANCE.facilityPieceCollections.get(ChangedFacilityPieceTypes.ROOM.get()).shuffledStream(context.random())
                        .filter(pieceConnectsToZone(zone)).map(ConfiguredFacilityPiece::facilityPiece).map(nextRoom -> {
                    var nextRoomStructure = nextRoom.createStructurePiece(context.structureTemplateManager(), genDepth);
                    if (!nextRoomStructure.setupBoundingBox(builder, start.blockInfo(), context.random(), allowedRegion))
                        return null;

                    // Success
                    return nextRoomStructure;
                }).filter(Objects::nonNull).findFirst().orElse(nextStructure);

                if (pieceToPut == nextStructure) {
                    pieceToPut = INSTANCE.facilityPieceCollections.get(ChangedFacilityPieceTypes.SEAL.get()).shuffledStream(context.random())
                            .filter(pieceConnectsToZone(zone)).map(ConfiguredFacilityPiece::facilityPiece).map(nextSeal -> {
                        var nextRoomStructure = nextSeal.createStructurePiece(context.structureTemplateManager(), genDepth);
                        if (!nextRoomStructure.setupBoundingBox(builder, start.blockInfo(), context.random(), allowedRegion))
                            return null;

                        // Success
                        return nextRoomStructure;
                    }).filter(Objects::nonNull).findFirst().orElse(nextStructure);
                }

                if (pieceToPut == nextStructure)
                    Changed.LOGGER.debug("Failed to seal dead end in facility, startPos {}", startPos);
                else
                    Changed.LOGGER.debug("Sealed dead end in facility, startPos {}", startPos);
                builder.addPiece(pieceToPut);

                return true;
            });

            if (placed)
                break;

            reroll--;
        }

        return;
    }

    public static void generateFacility(StructurePiecesBuilder builder, Structure.GenerationContext context, int genDepth, int span, BoundingBox allowedRegion) {
        BlockPos blockPos = new BlockPos(
                context.chunkPos().getBlockX(8), 0,
                context.chunkPos().getBlockZ(8));
        blockPos = blockPos.atY(context.chunkGenerator().getBaseHeight(blockPos.getX(), blockPos.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState()));

        Stack<FacilityPiece> stack = new Stack<>();
        List<GenStep> starts = new ArrayList<>();
        FacilityPiece entranceNew = INSTANCE.facilityPieceCollections.get(ChangedFacilityPieceTypes.ENTRANCE.get()).findNextPiece(context.random())
                .map(ConfiguredFacilityPiece::facilityPiece).orElseThrow();
        FacilityPieceInstance entrancePiece = entranceNew.createStructurePiece(context.structureTemplateManager(), genDepth);

        var directions = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
        CollectionUtil.shuffle(directions, context.random());

        for (Direction dir : directions) {
            entrancePiece.setRotation(dir);
            entrancePiece.setupBoundingBoxOnBottomCenter(blockPos);
            BoundingBox entranceBB = entrancePiece.getBoundingBox();

            int minXminZ = context.chunkGenerator().getBaseHeight(entranceBB.minX() + 1, entranceBB.minZ() + 1, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            int minXmaxZ = context.chunkGenerator().getBaseHeight(entranceBB.minX() + 1, entranceBB.maxZ() - 1, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            int maxXminZ = context.chunkGenerator().getBaseHeight(entranceBB.maxX() - 1, entranceBB.minZ() + 1, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            int maxXmaxZ = context.chunkGenerator().getBaseHeight(entranceBB.maxX() - 1, entranceBB.maxZ() - 1, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            int min = Math.min(Math.min(minXminZ, minXmaxZ), Math.min(maxXminZ, maxXmaxZ));
            int max = Math.max(Math.max(minXminZ, minXmaxZ), Math.max(maxXminZ, maxXmaxZ));

            entrancePiece.setupBoundingBoxOnBottomCenter(new BlockPos(blockPos.getX(), min, blockPos.getZ()));

            if (max - min < 3) break; // Surface is flat enough to not worry about rotating the entrance

            BlockPos testPos = entrancePiece.getRandomStart(context.random());
            double minX = Mth.lerp((double)(testPos.getZ() - entranceBB.minZ()) / (double)entranceBB.getZSpan(), (double)minXminZ, (double)minXmaxZ);
            double maxX = Mth.lerp((double)(testPos.getZ() - entranceBB.minZ()) / (double)entranceBB.getZSpan(), (double)maxXminZ, (double)maxXmaxZ);
            double height = Mth.lerp((double)(testPos.getX() - entranceBB.minX()) / (double)entranceBB.getXSpan(), minX, maxX);

            if (testPos.getY() < height) break; // Next structure piece is in the surface
        }

        stack.push(entranceNew);
        builder.addPiece(entrancePiece);

        entrancePiece.addSteps(new FacilityGenerationStack(stack, entrancePiece.getBoundingBox(), context, span), starts);

        if (span > 0) {
            starts.forEach(start -> {
                treeGenerate(builder, context, stack, entrancePiece, start, genDepth, span - 1, allowedRegion);
            });
        }

        stack.pop();
    }
}
