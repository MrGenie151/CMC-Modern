package net.ltxprogrammer.changed.world.features.structures.facility.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ltxprogrammer.changed.world.features.structures.facility.FacilityRoomPiece;
import net.minecraft.resources.ResourceLocation;

public class RoomType extends PieceType<FacilityRoomPiece> {
    public static final Codec<FacilityRoomPiece> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("template").forGetter(entrance -> entrance.templateName),
            Codec.optionalField("loot_table", ResourceLocation.CODEC).forGetter(entrance -> entrance.lootTable)
    ).apply(instance, (template, lootTable) -> new FacilityRoomPiece(template, lootTable.orElse(null))));

    @Override
    public Codec<FacilityRoomPiece> getCodec() {
        return CODEC;
    }
}
