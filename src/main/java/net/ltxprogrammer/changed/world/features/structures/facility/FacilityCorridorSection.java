package net.ltxprogrammer.changed.world.features.structures.facility;

import net.ltxprogrammer.changed.init.ChangedFacilityPieceTypes;
import net.ltxprogrammer.changed.world.features.structures.LootTables;
import net.ltxprogrammer.changed.world.features.structures.facility.types.PieceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import org.jetbrains.annotations.Nullable;

public class  FacilityCorridorSection extends FacilitySinglePiece {
    private static final WeightedRandomList<WeightedPieceNeighborSupplier> VALID_NEIGHBORS = WeightedRandomList.create(
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.STAIRCASE_START, 3),
            //WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.TRANSITION, 3),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.SPLIT, 12),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.CORRIDOR, 8),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.ROOM, 5));
    private static final WeightedRandomList<WeightedPieceNeighborSupplier> VALID_NEIGHBORS_MIN = WeightedRandomList.create(
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.STAIRCASE_START, 1),
            //WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.TRANSITION, 1),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.SPLIT, 15),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.CORRIDOR, 15),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.ROOM, 5));

    private static final WeightedRandomList<WeightedPieceNeighborSupplier> VALID_NEIGHBORS_HIGH_SPAN = WeightedRandomList.create(
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.STAIRCASE_START, 3),
            //WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.TRANSITION, 3),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.SPLIT, 12),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.CORRIDOR, 8));
    private static final WeightedRandomList<WeightedPieceNeighborSupplier> VALID_NEIGHBORS_MIN_HIGH_SPAN = WeightedRandomList.create(
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.STAIRCASE_START, 1),
            //WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.TRANSITION, 1),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.SPLIT, 15),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.CORRIDOR, 15));

    public FacilityCorridorSection(ResourceLocation templateName) {
        super(ChangedFacilityPieceTypes.CORRIDOR.get(), templateName, LootTables.LOW_TIER_LAB);
    }

    public FacilityCorridorSection(ResourceLocation templateName, @Nullable ResourceLocation lootTable) {
        super(ChangedFacilityPieceTypes.CORRIDOR.get(), templateName, lootTable);
    }

    @Override
    public WeightedRandomList<WeightedPieceNeighborSupplier> getValidNeighbors(FacilityGenerationStack stack) {
        int corridors = stack.sequentialMatch(piece -> piece.facilityPiece().type == ChangedFacilityPieceTypes.CORRIDOR.get());
        if (stack.getDepthRemaining() > 4) {
            if (corridors < 5)
                return VALID_NEIGHBORS_MIN_HIGH_SPAN;
            return VALID_NEIGHBORS_HIGH_SPAN;
        }

        if (corridors < 5)
            return VALID_NEIGHBORS_MIN;
        return VALID_NEIGHBORS;
    }
}
