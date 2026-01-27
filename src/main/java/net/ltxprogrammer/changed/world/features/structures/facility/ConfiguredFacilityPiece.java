package net.ltxprogrammer.changed.world.features.structures.facility;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ltxprogrammer.changed.init.ChangedRegistry;
import net.ltxprogrammer.changed.world.features.structures.facility.types.PieceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;

import java.util.List;
import java.util.Set;

public class ConfiguredFacilityPiece implements WeightedEntry {
    public static Codec<ConfiguredFacilityPiece> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ((MapCodec<FacilityPiece>) ChangedRegistry.FACILITY_PIECE_TYPES.get().getCodec().dispatch("type", FacilityPiece::getType, PieceType::getCodec)
                    .fieldOf("piece")).forGetter(ConfiguredFacilityPiece::getFacilityPiece),
            Weight.CODEC.fieldOf("spawn_weight").forGetter(ConfiguredFacilityPiece::getWeight),
            Codec.INT.fieldOf("minimum").orElse(0).forGetter(ConfiguredFacilityPiece::getMinimum),
            Codec.INT.fieldOf("maximum").orElse(20).forGetter(ConfiguredFacilityPiece::getMaximum),
            ChangedRegistry.FACILITY_ZONES.get().getCodec().listOf().xmap(Set::copyOf, List::copyOf)
                    .fieldOf("neighboring_zones").forGetter(ConfiguredFacilityPiece::getConnectsTo)
    ).apply(instance, ConfiguredFacilityPiece::new));

    public final FacilityPiece facilityPiece;
    public final Weight spawnWeight;
    public final int minimum;
    public final int maximum;
    public final Set<Zone> connectsTo;
    private ResourceLocation name;

    /**
     * Defines a spawn entry for facility generation
     * @param facilityPiece parameterized facility piece that gets placed down
     * @param spawnWeight weight for random selection
     * @param connectsTo set of zones the piece can connect to, for optimization (empty = all)
     */
    public ConfiguredFacilityPiece(FacilityPiece facilityPiece,
                                   Weight spawnWeight,
                                   int minimum,
                                   int maximum,
                                   Set<Zone> connectsTo) {
        this.facilityPiece = facilityPiece;
        this.spawnWeight = spawnWeight;
        this.minimum = minimum;
        this.maximum = maximum;
        this.connectsTo = connectsTo;
    }

    public FacilityPiece getFacilityPiece() {
        return facilityPiece;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getMaximum() {
        return maximum;
    }

    @Override
    public Weight getWeight() {
        return spawnWeight;
    }

    public Set<Zone> getConnectsTo() {
        return connectsTo;
    }

    public ResourceLocation getName() {
        return name;
    }

    public ConfiguredFacilityPiece setName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
