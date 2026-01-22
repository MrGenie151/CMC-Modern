package net.ltxprogrammer.changed.world.features.structures;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.block.GluBlock;
import net.ltxprogrammer.changed.init.ChangedFacilityPieceTypes;
import net.ltxprogrammer.changed.init.ChangedRegistry;
import net.ltxprogrammer.changed.util.CollectionUtil;
import net.ltxprogrammer.changed.util.ResourceUtil;
import net.ltxprogrammer.changed.world.features.structures.facility.*;
import net.ltxprogrammer.changed.world.features.structures.facility.types.PieceType;
import net.minecraft.Util;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FacilityPieces extends SimplePreparableReloadListener<Set<ConfiguredFacilityPiece>> {
    public static FacilityPieces INSTANCE = new FacilityPieces();

    private final Map<PieceType<?>, FacilityPieceCollection> facilityPieceCollections = new HashMap<>();
    private final Set<Zone> zonesWithDefinedPieces = new HashSet<>();

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
        facilityPieceCollections.clear();
        zonesWithDefinedPieces.clear();

        for (var pieceType : ChangedRegistry.FACILITY_PIECE_TYPES.get().getValues()) {
            FacilityPieceCollectionBuilder builder = new FacilityPieceCollectionBuilder();
            output.stream().filter(piece -> piece.facilityPiece().getType() == pieceType).forEach(builder::register);

            Changed.postModLoadingEvent(new GatherFacilityPiecesEvent(pieceType, builder));

            facilityPieceCollections.put(pieceType, builder.build());
            facilityPieceCollections.get(pieceType).stream()
                    .map(ConfiguredFacilityPiece::connectsTo)
                    .flatMap(Set::stream)
                    .forEach(zonesWithDefinedPieces::add);
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

    private static Predicate<ConfiguredFacilityPiece> pieceConnectsToZones(Zone zone, Zone wantedZone) {
        if (zone == wantedZone)
            return pieceConnectsToZone(zone);

        return configuredFacilityPiece -> {
            return configuredFacilityPiece.connectsTo().isEmpty() || (
                    configuredFacilityPiece.connectsTo().contains(zone) && configuredFacilityPiece.connectsTo().contains(wantedZone));
        };
    }

    public static class FacilityGenerationContext {
        public final StructurePiecesBuilder builder;
        public final Structure.GenerationContext structureContext;
        public final Map<ConfiguredFacilityPiece, Integer> configuredPieceCounts = new HashMap<>();
        public final Map<Zone, List<StructurePiece>> piecesByZone = new HashMap<>();

        public FacilityGenerationContext(StructurePiecesBuilder builder, Structure.GenerationContext structureContext) {
            this.builder = builder;
            this.structureContext = structureContext;
        }

        public void addPieceToCount(ConfiguredFacilityPiece configuredFacilityPiece) {
            configuredPieceCounts.compute(configuredFacilityPiece, (configuredPiece, count) -> {
                if (count == null)
                    return 1;
                return count + 1;
            });
        }

        public Stream<Zone> getRemainingZonesToGenerate() {
            return INSTANCE.zonesWithDefinedPieces.stream().filter(zone -> {
                return !zone.isUnique() || !piecesByZone.containsKey(zone);
            });
        }
    }

    private static int sequentialMatch(Stack<ConfiguredFacilityPiece> stack, Predicate<ConfiguredFacilityPiece> predicate, boolean includingNonSpan) {
        int nonSpan = 0;
        for (int i = stack.size() - 1; i >= 0; --i) {
            var element = stack.elementAt(i);
            if (!includingNonSpan && !element.facilityPiece().getType().shouldConsumeSpan()) {
                nonSpan++;
                continue;
            }
            if (!predicate.test(element))
                return stack.size() - (i + 1) - nonSpan;
        }

        return stack.size() - nonSpan;
    }

    private static void treeGenerate(FacilityGenerationContext facilityContext,
                                     Stack<ConfiguredFacilityPiece> stack, StructurePiece parentStructure,
                                     GenStep start, int genDepth, int span, BoundingBox allowedRegion, int zoneProtection) {
        var configuredParent = stack.peek();
        var parent = configuredParent.facilityPiece();
        var zone = start.getZone();
        var wantedZone = zone;

        int zoneLength = sequentialMatch(stack, configuredPiece -> {
            return configuredPiece.connectsTo().isEmpty() || configuredPiece.connectsTo().contains(zone);
        }, false);

        int reroll = 25;
        while (reroll > 0) {
            PieceType<?> pieceType;
            if (parent.type == ChangedFacilityPieceTypes.SPLIT.get() && reroll < 10) { // Split is struggling to generate neighbor, put a room/seal instead.
                if (reroll > 2)
                    pieceType = ChangedFacilityPieceTypes.ROOM.get();
                else
                    pieceType = ChangedFacilityPieceTypes.SEAL.get();
            } else if ((parent.type == ChangedFacilityPieceTypes.STAIRCASE_START.get() ||
                    parent.type == ChangedFacilityPieceTypes.STAIRCASE_SECTION.get()) && reroll < 10) { // Stairs are struggling to generate neighbor
                pieceType = ChangedFacilityPieceTypes.STAIRCASE_END.get();
            } else if (span <= 0) {
                pieceType = ChangedFacilityPieceTypes.ROOM.get();
            } else {
                var type = start.validTypes().getRandom(facilityContext.structureContext.random());
                if (type.isEmpty())
                    break;
                if (zoneProtection <= 0 && reroll > 5 && zone != null && zoneLength > zone.getMinimumLength()) { // Coerce the next piece to be a transition to another zone
                    int zoneDelta = zoneLength - zone.getMinimumLength();

                    if (facilityContext.structureContext.random().nextInt(4) < zoneDelta) {
                        var remainingZones = facilityContext.getRemainingZonesToGenerate().collect(Collectors.toCollection(ArrayList::new));
                        wantedZone = Util.getRandom(remainingZones, facilityContext.structureContext.random());
                    }

                    /*WeightedRandomList<WeightedEntry.Wrapper<Zone>> possibleZones = WeightedRandomList.create(
                            ChangedRegistry.FACILITY_ZONES.get().getValues().stream()
                                    .map(facilityZone -> {
                                        int weight = (int) (facilityZone.getGenerationWeight(36 - span) * 100);
                                        if (weight <= 0)
                                            return null;
                                        return WeightedEntry.wrap(facilityZone, weight);
                                    })
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList())
                    );

                    wantedZone = possibleZones.getRandom(facilityContext.structureContext.random()).map(WeightedEntry.Wrapper::getData).orElse(zone);*/
                }

                pieceType = type.get().getPieceType();

                if (zoneProtection > 0 && pieceType == ChangedFacilityPieceTypes.TRANSITION.get()) {
                    pieceType = ChangedFacilityPieceTypes.CORRIDOR.get();
                    wantedZone = zone;
                }

                if (zoneProtection <= 0 && wantedZone != zone && pieceType.canBeReplacedBy(ChangedFacilityPieceTypes.TRANSITION.get())) {
                    pieceType = ChangedFacilityPieceTypes.TRANSITION.get();
                }
            }

            final PieceType<?> resolvedPieceType = pieceType;

            var placedPiece = INSTANCE.facilityPieceCollections.get(pieceType).shuffledStream(facilityContext.structureContext.random())
                    .filter(nextConfiguredPiece -> facilityContext.configuredPieceCounts.getOrDefault(nextConfiguredPiece, 0) < nextConfiguredPiece.maximum())
                    .filter(pieceConnectsToZones(zone, wantedZone)).map(nextConfiguredPiece -> {
                        var nextPiece = nextConfiguredPiece.facilityPiece();
                        var nextStructure = nextPiece.createStructurePiece(facilityContext.structureContext.structureTemplateManager(), genDepth);
                        if (!nextStructure.setupBoundingBox(facilityContext.builder, start.blockInfo(), facilityContext.structureContext.random(), allowedRegion))
                            return null;

                        var startPos = gluNeighbor(start.blockInfo().pos(), start.blockInfo().state());
                                facilityContext.builder.addPiece(nextStructure);

                        int nextSpan = resolvedPieceType.shouldConsumeSpan() ? span - 1 : span;
                        stack.push(nextConfiguredPiece);

                        var genStack = new FacilityGenerationStack(stack, nextStructure.getBoundingBox(), facilityContext.structureContext, nextSpan);
                        ObjectArrayList<GenStep> starts = new ObjectArrayList<>();
                        nextStructure.addSteps(genStack, starts);
                        Util.shuffle(starts, facilityContext.structureContext.random());

                        int piecesStart = ((StructurePiecesBuilderExtender)facilityContext.builder).pieceCount();
                        AtomicBoolean firstStart = new AtomicBoolean(true);
                        starts.stream().filter(next -> !next.blockInfo().pos().equals(startPos)).forEach(next -> {
                            boolean isFirstStart = firstStart.getAndSet(false);
                            boolean isMinorBranch = zoneProtection <= 0 && !isFirstStart;
                            int piecesBefore = ((StructurePiecesBuilderExtender)facilityContext.builder).pieceCount();
                            treeGenerate(facilityContext, stack, nextStructure, next, genDepth,
                                    isFirstStart ? nextSpan : nextSpan - 5,
                                    allowedRegion,
                                    isFirstStart ? Math.max(zoneProtection - 1, 0) : 5);
                            int piecesAfter = ((StructurePiecesBuilderExtender)facilityContext.builder).pieceCount();
                            if (piecesAfter <= piecesStart) { // Start failed to generate sufficient pieces, allow new branch to assume zone generation
                                firstStart.set(true);
                            }
                        });
                        int piecesAfter = ((StructurePiecesBuilderExtender)facilityContext.builder).pieceCount();

                        stack.pop();

                        if (resolvedPieceType.connectionsMeetExpectations((piecesAfter - piecesStart) + 1)) {
                            if (resolvedPieceType != ChangedFacilityPieceTypes.SEAL.get())
                                facilityContext.addPieceToCount(nextConfiguredPiece);
                            return nextStructure;
                        }

                        // Attempt to regenerate this piece as a room, to prevent a dead end
                        ((StructurePiecesBuilderExtender)facilityContext.builder).removePiece(nextStructure);

                        StructurePiece pieceToPut = Stream.concat(
                                INSTANCE.facilityPieceCollections.get(ChangedFacilityPieceTypes.ROOM.get()).shuffledStream(facilityContext.structureContext.random()),
                                INSTANCE.facilityPieceCollections.get(ChangedFacilityPieceTypes.SEAL.get()).shuffledStream(facilityContext.structureContext.random())
                        ).filter(pieceConnectsToZone(zone)).map(ConfiguredFacilityPiece::facilityPiece).map(nextRoom -> {
                            var nextRoomStructure = nextRoom.createStructurePiece(facilityContext.structureContext.structureTemplateManager(), genDepth);
                            if (!nextRoomStructure.setupBoundingBox(facilityContext.builder, start.blockInfo(), facilityContext.structureContext.random(), allowedRegion))
                                return null;

                            // Success
                            return nextRoomStructure;
                        }).filter(Objects::nonNull).findFirst().orElse(nextStructure);

                        if (pieceToPut == nextStructure)
                            Changed.LOGGER.debug("Failed to seal dead end in facility, startPos {}", startPos);
                        else
                            Changed.LOGGER.debug("Sealed dead end in facility, startPos {}", startPos);
                        facilityContext.builder.addPiece(pieceToPut);

                        return pieceToPut;
                    }).filter(Objects::nonNull).findFirst();


            if (placedPiece.isPresent()) {
                facilityContext.piecesByZone.computeIfAbsent(wantedZone, toMap -> new ArrayList<>()).add(placedPiece.get());
                break;
            }

            reroll--;
        }

        return;
    }

    public static FacilityKeystone generateFacility(StructurePiecesBuilder builder, Structure.GenerationContext context, int genDepth, int span, BoundingBox allowedRegion) {
        BlockPos blockPos = new BlockPos(
                context.chunkPos().getBlockX(8), 0,
                context.chunkPos().getBlockZ(8));
        blockPos = blockPos.atY(context.chunkGenerator().getBaseHeight(blockPos.getX(), blockPos.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState()));

        Stack<ConfiguredFacilityPiece> stack = new Stack<>();
        List<GenStep> starts = new ArrayList<>();
        ConfiguredFacilityPiece entranceNew = INSTANCE.facilityPieceCollections.get(ChangedFacilityPieceTypes.ENTRANCE.get()).findNextPiece(context.random())
                .orElseThrow();
        FacilityPieceInstance entrancePiece = entranceNew.facilityPiece().createStructurePiece(context.structureTemplateManager(), genDepth);

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

        var facilityGenerationContext = new FacilityGenerationContext(builder, context);

        if (span > 0) {
            starts.forEach(start -> {
                treeGenerate(facilityGenerationContext, stack, entrancePiece, start, genDepth, span - 1, allowedRegion, 0);
            });
        }

        stack.pop();

        Map<Zone, List<BoundingBox>> zoneBoundingBoxes = new HashMap<>();
        facilityGenerationContext.piecesByZone.forEach((zone, pieces) -> {
            zoneBoundingBoxes.put(zone, pieces.stream().map(StructurePiece::getBoundingBox).toList());
        });

        return new FacilityKeystone(genDepth, zoneBoundingBoxes, entrancePiece.getBoundingBox(), context.random());
    }
}
