package net.ltxprogrammer.changed.world.features.structures.facility;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ltxprogrammer.changed.init.ChangedRegistry;
import net.ltxprogrammer.changed.world.features.structures.facility.types.PieceType;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;

import java.util.List;
import java.util.Set;

/**
 * Defines a spawn entry for facility generation
 * @param facilityPiece parameterized facility piece that gets placed down
 * @param spawnWeight weight for random selection
 * @param connectsTo set of zones the piece can connect to, for optimization (empty = all)
 */
public record ConfiguredFacilityPiece(FacilityPiece facilityPiece,
                                   int spawnWeight,
                                   Set<Zone> connectsTo) implements WeightedEntry {
    public static Codec<ConfiguredFacilityPiece> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ((MapCodec<FacilityPiece>) ChangedRegistry.FACILITY_PIECE_TYPES.get().getCodec().dispatch("type", FacilityPiece::getType, PieceType::getCodec)
                    .fieldOf("piece")).forGetter(ConfiguredFacilityPiece::facilityPiece),
            Codec.INT.fieldOf("spawn_weight").forGetter(ConfiguredFacilityPiece::spawnWeight),
            ChangedRegistry.FACILITY_ZONES.get().getCodec().listOf().xmap(Set::copyOf, List::copyOf)
                    .fieldOf("neighboring_zones").forGetter(ConfiguredFacilityPiece::connectsTo)
    ).apply(instance, ConfiguredFacilityPiece::new));

    @Override
    public Weight getWeight() {
        return Weight.of(spawnWeight);
    }
}
