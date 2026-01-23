package net.ltxprogrammer.changed.world.features.structures.facility.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ltxprogrammer.changed.world.features.structures.facility.FacilityTransitionSection;
import net.minecraft.resources.ResourceLocation;

public class TransitionType extends PieceType<FacilityTransitionSection> {
    public static final Codec<FacilityTransitionSection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("template").forGetter(entrance -> entrance.templateName),
            Codec.optionalField("loot_table", ResourceLocation.CODEC).forGetter(entrance -> entrance.lootTable)
    ).apply(instance, FacilityTransitionSection::new));

    @Override
    public Codec<FacilityTransitionSection> getCodec() {
        return CODEC;
    }

    @Override
    public boolean canBeReplacedBy(PieceType<?> other) {
        return other == this;
    }
}
