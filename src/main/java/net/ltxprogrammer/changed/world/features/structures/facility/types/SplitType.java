package net.ltxprogrammer.changed.world.features.structures.facility.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ltxprogrammer.changed.world.features.structures.facility.FacilitySplitSection;
import net.minecraft.resources.ResourceLocation;

public class SplitType extends PieceType<FacilitySplitSection> {
    public static final Codec<FacilitySplitSection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("template").forGetter(piece -> piece.templateName),
            Codec.INT.fieldOf("expected_dependents").orElse(2).forGetter(piece -> piece.expectedDependents),
            Codec.optionalField("loot_table", ResourceLocation.CODEC).forGetter(piece -> piece.lootTable)
    ).apply(instance, FacilitySplitSection::new));

    @Override
    public Codec<FacilitySplitSection> getCodec() {
        return CODEC;
    }
}
