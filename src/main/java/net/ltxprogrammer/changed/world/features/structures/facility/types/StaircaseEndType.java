package net.ltxprogrammer.changed.world.features.structures.facility.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ltxprogrammer.changed.world.features.structures.facility.FacilityStaircaseEnd;
import net.minecraft.resources.ResourceLocation;

public class StaircaseEndType extends PieceType<FacilityStaircaseEnd> {
    public static final Codec<FacilityStaircaseEnd> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("template").forGetter(entrance -> entrance.templateName),
            Codec.optionalField("loot_table", ResourceLocation.CODEC).forGetter(entrance -> entrance.lootTable)
    ).apply(instance, FacilityStaircaseEnd::new));

    @Override
    public Codec<FacilityStaircaseEnd> getCodec() {
        return CODEC;
    }

    @Override
    public boolean shouldConsumeSpan() {
        return false;
    }
}
