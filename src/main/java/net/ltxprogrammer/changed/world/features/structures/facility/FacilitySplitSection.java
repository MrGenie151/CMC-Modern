package net.ltxprogrammer.changed.world.features.structures.facility;

import net.ltxprogrammer.changed.init.ChangedFacilityPieceTypes;
import net.ltxprogrammer.changed.world.features.structures.LootTables;
import net.ltxprogrammer.changed.world.features.structures.facility.types.PieceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;

public class FacilitySplitSection extends FacilitySinglePiece {
    private static final WeightedRandomList<WeightedPieceNeighborSupplier> VALID_NEIGHBORS = WeightedRandomList.create(
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.STAIRCASE_START, 3),
            //WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.TRANSITION, 3),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.CORRIDOR, 8),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.ROOM, 5));
    private static final WeightedRandomList<WeightedPieceNeighborSupplier> VALID_NEIGHBORS_MIN = WeightedRandomList.create(
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.STAIRCASE_START, 1),
            //WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.TRANSITION, 1),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.CORRIDOR, 15),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.ROOM, 1));

    private static final WeightedRandomList<WeightedPieceNeighborSupplier> VALID_NEIGHBORS_HIGH_SPAN = WeightedRandomList.create(
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.STAIRCASE_START, 3),
            //WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.TRANSITION, 3),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.CORRIDOR, 8));
    private static final WeightedRandomList<WeightedPieceNeighborSupplier> VALID_NEIGHBORS_MIN_HIGH_SPAN = WeightedRandomList.create(
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.STAIRCASE_START, 1),
            //WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.TRANSITION, 1),
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.CORRIDOR, 15));

    public FacilitySplitSection(ResourceLocation templateName) {
        super(ChangedFacilityPieceTypes.SPLIT.get(), templateName, LootTables.LOW_TIER_LAB);
    }

    public FacilitySplitSection(ResourceLocation templateName, ResourceLocation lootTable) {
        super(ChangedFacilityPieceTypes.SPLIT.get(), templateName, lootTable);
    }

    @Override
    public WeightedRandomList<WeightedPieceNeighborSupplier> getValidNeighbors(FacilityGenerationStack stack) {
        int corridors = stack.sequentialMatch(piece -> piece.getFacilityPiece().type == ChangedFacilityPieceTypes.CORRIDOR.get());
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
