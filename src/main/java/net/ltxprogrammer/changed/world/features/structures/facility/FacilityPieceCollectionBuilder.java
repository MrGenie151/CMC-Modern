package net.ltxprogrammer.changed.world.features.structures.facility;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.random.Weight;

import java.util.Set;

public class FacilityPieceCollectionBuilder {
    private final ImmutableList.Builder<ConfiguredFacilityPiece> builder = ImmutableList.builder();

    public static final int WEIGHT_COMMON = 20;
    public static final int WEIGHT_LESSCOMMON = 14;
    public static final int WEIGHT_UNCOMMON = 10;
    public static final int WEIGHT_RARE = 5;
    public static final int WEIGHT_VERY_RARE = 1;

    public FacilityPieceCollectionBuilder register(FacilityPiece piece) {
        this.register(WEIGHT_COMMON, piece);
        return this;
    }

    public FacilityPieceCollectionBuilder register(ConfiguredFacilityPiece piece) {
        builder.add(piece);
        return this;
    }

    public FacilityPieceCollectionBuilder register(int weight, FacilityPiece piece) {
        builder.add(new ConfiguredFacilityPiece(piece, Weight.of(weight), 0, 10, Set.of()));
        return this;
    }

    public FacilityPieceCollectionBuilder register(Weight weight, FacilityPiece piece) {
        builder.add(new ConfiguredFacilityPiece(piece, weight, 0, 10, Set.of()));
        return this;
    }

    public FacilityPieceCollectionBuilder registerAll(Iterable<ConfiguredFacilityPiece> pieces) {
        builder.addAll(pieces);
        return this;
    }

    public FacilityPieceCollection build() {
        return new FacilityPieceCollection(builder);
    }
}
