package net.ltxprogrammer.changed.world.features.structures.facility.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ltxprogrammer.changed.world.features.structures.facility.FacilityEntrance;
import net.minecraft.resources.ResourceLocation;

public class EntranceType extends PieceType<FacilityEntrance> {
    public static final Codec<FacilityEntrance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("template").forGetter(entrance -> entrance.templateName),
            Codec.optionalField("loot_table", ResourceLocation.CODEC).forGetter(entrance -> entrance.lootTable)
    ).apply(instance, FacilityEntrance::new));

    @Override
    public Codec<FacilityEntrance> getCodec() {
        return CODEC;
    }
}
