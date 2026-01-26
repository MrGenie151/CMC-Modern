package net.ltxprogrammer.changed.client;

import net.ltxprogrammer.changed.client.animations.AnimationContainer;
import net.ltxprogrammer.changed.entity.animation.AnimationCategory;
import net.ltxprogrammer.changed.client.animations.AnimationDefinition;
import net.ltxprogrammer.changed.client.animations.AnimationInstance;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface ClientLivingEntityExtender {
    Optional<AnimationContainer> getAnimations();

    AnimationContainer getAnimationsOrCreate(Supplier<AnimationContainer> factory);
}
