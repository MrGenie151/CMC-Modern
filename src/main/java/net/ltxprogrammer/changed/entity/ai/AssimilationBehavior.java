package net.ltxprogrammer.changed.entity.ai;

import net.ltxprogrammer.changed.process.ProcessTransfur;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public interface AssimilationBehavior {
    void attack(float damageMultiplier);
    AssimilationBehavior appendTransfurLogic(Runnable transfurLogic);

    default void attack() {
        this.attack(1.0f);
    }

    static AssimilationBehavior damagePlayerThenTransfur(Player player, float damage, Runnable transfurLogic) {
        return new AssimilationBehavior() {
            @Override
            public void attack(float damageMultiplier) {
                boolean justHit = player.invulnerableTime == 20 && player.hurtDuration == 10;

                if (player.invulnerableTime > 10 && !justHit)
                    return;
                if (damage <= 0.0f)
                    return;

                player.invulnerableTime = 20;
                player.hurtDuration = 10;
                player.hurtTime = player.hurtDuration;
                player.setLastHurtByMob(null);

                float old = ProcessTransfur.getPlayerTransfurProgress(player);
                float next = old + (damage * damageMultiplier);
                float max = (float) ProcessTransfur.getEntityTransfurTolerance(player);
                ProcessTransfur.setPlayerTransfurProgress(player, next);
                if (next >= max && old < max)
                    transfurLogic.run();
            }

            @Override
            public AssimilationBehavior appendTransfurLogic(Runnable nextTransfurLogic) {
                return damagePlayerThenTransfur(player, damage, () -> {
                    transfurLogic.run();
                    nextTransfurLogic.run();
                });
            }
        };
    }

    static AssimilationBehavior damageThenTransfur(LivingEntity target, DamageSource source, float damage, Runnable transfurLogic) {
        if (target instanceof Player player) {
            return damagePlayerThenTransfur(player, damage, transfurLogic);
        } else {
            return new AssimilationBehavior() {
                @Override
                public void attack(float damageMultiplier) {
                    var health = target.getHealth();
                    if (health <= (damage * damageMultiplier) && health > 0.0F) {
                        transfurLogic.run();
                    } else {
                        target.hurt(source, damage * damageMultiplier);
                    }
                }

                @Override
                public AssimilationBehavior appendTransfurLogic(Runnable nextTransfurLogic) {
                    return damageThenTransfur(target, source, damage, () -> {
                        transfurLogic.run();
                        nextTransfurLogic.run();
                    });
                }
            };
        }
    }

    static AssimilationBehavior instant(Runnable transfurLogic) {
        return new AssimilationBehavior() {
            @Override
            public void attack(float damageMultiplier) {
                transfurLogic.run();
            }

            @Override
            public AssimilationBehavior appendTransfurLogic(Runnable nextTransfurLogic) {
                return instant(() -> {
                    transfurLogic.run();
                    nextTransfurLogic.run();
                });
            }
        };
    }
}
