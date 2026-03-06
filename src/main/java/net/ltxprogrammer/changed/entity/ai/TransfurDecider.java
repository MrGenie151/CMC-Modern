package net.ltxprogrammer.changed.entity.ai;

import net.ltxprogrammer.changed.entity.TransfurDecision;
import net.minecraft.world.entity.LivingEntity;

public interface TransfurDecider<T extends LivingEntity> {
    TransfurDecision<?> apply(T assimilatedMob, LivingEntity target);
}
