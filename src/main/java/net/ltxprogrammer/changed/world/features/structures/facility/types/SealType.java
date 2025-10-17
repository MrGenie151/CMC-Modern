package net.ltxprogrammer.changed.world.features.structures.facility.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ltxprogrammer.changed.world.features.structures.facility.FacilitySealPiece;
import net.minecraft.resources.ResourceLocation;

public class SealType extends PieceType<FacilitySealPiece> {
    public static final Codec<FacilitySealPiece> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("template").forGetter(entrance -> entrance.templateName)
    ).apply(instance, FacilitySealPiece::new));

    @Override
    public Codec<FacilitySealPiece> getCodec() {
        return CODEC;
    }

    @Override
    public boolean connectionsMeetExpectations(int connections) {
        return connections == 1;
    }
}
