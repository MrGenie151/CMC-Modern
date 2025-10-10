package net.ltxprogrammer.changed.world.features.structures.facility;

import net.ltxprogrammer.changed.init.ChangedFacilityPieceTypes;
import net.ltxprogrammer.changed.world.features.structures.LootTables;
import net.ltxprogrammer.changed.world.features.structures.facility.types.PieceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;

public class FacilityStaircaseStart extends FacilitySinglePiece {
    private static final WeightedRandomList<WeightedPieceNeighborSupplier> VALID_NEIGHBORS = WeightedRandomList.create(
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.STAIRCASE_SECTION, 1));

    public FacilityStaircaseStart(ResourceLocation templateName) {
        super(ChangedFacilityPieceTypes.STAIRCASE_START.get(), templateName, LootTables.LOW_TIER_LAB);
    }

    public FacilityStaircaseStart(ResourceLocation templateName, ResourceLocation lootTable) {
        super(ChangedFacilityPieceTypes.STAIRCASE_START.get(), templateName, lootTable);
    }

    @Override
    public WeightedRandomList<WeightedPieceNeighborSupplier> getValidNeighbors(FacilityGenerationStack stack) {
        return VALID_NEIGHBORS;
    }
}
