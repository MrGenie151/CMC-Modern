package net.ltxprogrammer.changed.world.features.structures.facility;

import net.ltxprogrammer.changed.init.ChangedRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Zone {
    private final ResourceLocation name;

    public Zone(ResourceLocation name) {
        this.name = name;
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