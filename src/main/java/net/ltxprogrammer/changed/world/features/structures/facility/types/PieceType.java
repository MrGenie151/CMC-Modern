package net.ltxprogrammer.changed.world.features.structures.facility.types;

import com.mojang.serialization.Codec;
import net.ltxprogrammer.changed.init.ChangedRegistry;
import net.ltxprogrammer.changed.world.features.structures.facility.FacilityPiece;

public abstract class PieceType<T extends FacilityPiece> {
    public abstract Codec<T> getCodec();

    public boolean shouldConsumeSpan() {
        return true;
    }

    public boolean canBeReplacedBy(PieceType<?> other) {
        return false;
    }

    @Override
    public String toString() {
        var key = ChangedRegistry.FACILITY_PIECE_TYPES.getKey(this);
        if (key == null)
            return super.toString();
        return key.toString();
    }
}
