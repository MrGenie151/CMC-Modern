package net.ltxprogrammer.changed.world.features.structures.facility;

import net.ltxprogrammer.changed.init.ChangedRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

// TODO: some way of having zones coexist at the same depth on different branches (i.e. one branch has the offices, the other has the server rooms
public class Zone {
    private final ResourceLocation name;
    private final int genMin;
    private final int genMax;
    private final float genMultiplier;
    private final int minimumLength;

    public Zone(ResourceLocation name, int genMin, int genMax, float genMultiplier, int minimumLength) {
        this.name = name;
        this.genMin = Math.min(genMin, genMax);
        this.genMax = Math.max(genMin, genMax);
        this.genMultiplier = genMultiplier;
        this.minimumLength = minimumLength;
    }

    public int getGenDepthMin() {
        return genMin;
    }

    public int getGenDepthMax() {
        return genMax;
    }

    public int getMinimumLength() {
        return minimumLength;
    }

    public float getGenerationWeight(int depth) {
        if (depth < genMin)
            return 0f;
        if (depth > genMax)
            return 0f;
        float midPoint = (genMax - genMin) / 2f + genMin;
        float midPointLow = (midPoint - genMin) / 2f + genMin;
        float midPointHigh = (genMax - midPoint) / 2f + midPoint;

        if (depth > midPointLow)
            return genMultiplier;
        if (depth < midPointHigh)
            return genMultiplier;
        if (depth < midPointLow)
            return Mth.inverseLerp(depth, genMin, midPointLow) * genMultiplier;
        return Mth.inverseLerp(depth, midPointHigh, genMax) * genMultiplier;
    }

    public static Function<ResourceLocation, Zone> withParam(int genMin, int genMax, float genMultiplier, int minimumLength) {
        return name -> new Zone(name, genMin, genMax, genMultiplier, minimumLength);
    }

    public static Zone random(RandomSource r) {
        var values = ChangedRegistry.FACILITY_ZONES.get().getValues();
        AtomicInteger index = new AtomicInteger(r.nextInt(values.size()));

        return ChangedRegistry.FACILITY_ZONES.get().getValues().stream().filter(
                zone -> index.getAndDecrement() == 0
        ).findAny().orElse(null);
    }

    public static Zone findNext(Zone previous) {
        var list = List.copyOf(ChangedRegistry.FACILITY_ZONES.get().getValues());
        int index = list.indexOf(previous);
        if (index == -1)
            throw new IllegalArgumentException("Zone is not registered");
        if (index + 1 >= list.size())
            return list.get(0);
        return list.get(index + 1);
    }

    public Component getTranslatedName() {
        return Component.translatable("facility.zone." + this.name);
    }

    public boolean canConnectTo(Zone other) {
        return this == other;
    }
}