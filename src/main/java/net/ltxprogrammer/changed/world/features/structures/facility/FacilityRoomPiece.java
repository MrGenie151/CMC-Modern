package net.ltxprogrammer.changed.world.features.structures.facility;

import net.ltxprogrammer.changed.init.ChangedFacilityPieceTypes;
import net.ltxprogrammer.changed.world.features.structures.facility.types.PieceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class FacilityRoomPiece extends FacilitySinglePiece {
    private static final WeightedRandomList<WeightedPieceNeighborSupplier> VALID_NEIGHBORS = WeightedRandomList.create(
            WeightedPieceNeighborSupplier.of(ChangedFacilityPieceTypes.ROOM, 1));

    public FacilityRoomPiece(ResourceLocation templateName, @Nullable ResourceLocation lootTable) {
        super(ChangedFacilityPieceTypes.ROOM.get(), templateName, lootTable);
    }

    @Override
    public WeightedRandomList<WeightedPieceNeighborSupplier> getValidNeighbors(FacilityGenerationStack stack) {
        return VALID_NEIGHBORS;
    }
}
