package net.ltxprogrammer.changed.world.features.structures.facility.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ltxprogrammer.changed.init.ChangedFacilityPieceTypes;
import net.ltxprogrammer.changed.world.features.structures.facility.FacilityCorridorSection;
import net.minecraft.resources.ResourceLocation;

public class CorridorType extends PieceType<FacilityCorridorSection> {
    public static final Codec<FacilityCorridorSection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("template").forGetter(entrance -> entrance.templateName),
            Codec.optionalField("loot_table", ResourceLocation.CODEC).forGetter(entrance -> entrance.lootTable)
    ).apply(instance, (template, lootTable) -> new FacilityCorridorSection(template, lootTable.orElse(null))));

    @Override
    public Codec<FacilityCorridorSection> getCodec() {
        return CODEC;
    }

    @Override
    public boolean canBeReplacedBy(PieceType<?> other) {
        return other == ChangedFacilityPieceTypes.TRANSITION.get() || other == ChangedFacilityPieceTypes.SPLIT.get();
    }
}
