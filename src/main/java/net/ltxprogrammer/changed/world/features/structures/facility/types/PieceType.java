package net.ltxprogrammer.changed.world.features.structures.facility.types;

import com.mojang.serialization.Codec;
import net.ltxprogrammer.changed.world.features.structures.facility.FacilityPiece;

public abstract class PieceType<T extends FacilityPiece> {
    public abstract Codec<T> getCodec();

    public boolean shouldConsumeSpan() {
        return true;
    }

    public boolean connectionsMeetExpectations(int connections) {
        return connections > 1;
    }

    public boolean canBeReplacedBy(PieceType<?> other) {
        return false;
    }
}
