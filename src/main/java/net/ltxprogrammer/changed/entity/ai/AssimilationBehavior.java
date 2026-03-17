package net.ltxprogrammer.changed.entity.ai;

import net.ltxprogrammer.changed.ability.IAbstractChangedEntity;
import net.ltxprogrammer.changed.process.ProcessTransfur;
import net.ltxprogrammer.changed.world.enchantments.LatexProtectionEnchantment;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface AssimilationBehavior {
    void stepAssimilate();
    boolean willAssimilate();
    AssimilationBehavior appendTransfurListener(Consumer<IAbstractChangedEntity> transfurLogic);

    static AssimilationBehavior progressPlayerThenTransfur(Player player, float transfurProgress, Supplier<IAbstractChangedEntity> transfurLogic) {
        return new AssimilationBehavior() {
            @Override
            public void stepAssimilate() {
                boolean justHit = player.invulnerableTime == 20 && player.hurtDuration == 10;

                if (player.invulnerableTime > 10 && !justHit)
                    return;

                float scaledDamage = LatexProtectionEnchantment.getLatexProtection(player, transfurProgress);
                if (scaledDamage <= 0.0f)
                    return;

                player.invulnerableTime = 20;
                player.hurtDuration = 10;
                player.hurtTime = player.hurtDuration;
                player.setLastHurtByMob(null);

                float old = ProcessTransfur.getPlayerTransfurProgress(player);
                float next = old + scaledDamage;
                float max = (float) ProcessTransfur.getEntityTransfurTolerance(player);
                ProcessTransfur.setPlayerTransfurProgress(player, next);
                if (next >= max && old < max)
                    transfurLogic.get();
            }

            @Override
            public boolean willAssimilate() {
                boolean justHit = player.invulnerableTime == 20 && player.hurtDuration == 10;

                if (player.invulnerableTime > 10 && !justHit)
                    return false;

                float scaledDamage = LatexProtectionEnchantment.getLatexProtection(player, transfurProgress);
                if (scaledDamage <= 0.0f)
                    return false;

                float old = ProcessTransfur.getPlayerTransfurProgress(player);
                float next = old + scaledDamage;
                float max = (float) ProcessTransfur.getEntityTransfurTolerance(player);
                return next >= max && old < max;
            }

            @Override
            public AssimilationBehavior appendTransfurListener(Consumer<IAbstractChangedEntity> nextTransfurLogic) {
                return progressPlayerThenTransfur(player, transfurProgress, () -> {
                    var variant = transfurLogic.get();
                    nextTransfurLogic.accept(variant);
                    return variant;
                });
            }
        };
    }

    static AssimilationBehavior progressThenTransfur(LivingEntity target, DamageSource source, float transfurProgress, Supplier<IAbstractChangedEntity> transfurLogic) {
        if (target instanceof Player player) {
            return progressPlayerThenTransfur(player, transfurProgress, transfurLogic);
        } else {
            return new AssimilationBehavior() {
                @Override
                public void stepAssimilate() {
                    float scaledDamage = LatexProtectionEnchantment.getLatexProtection(target, transfurProgress);
                    float scale = 20.0f / Math.max(0.1f, (float)ProcessTransfur.getEntityTransfurTolerance(target));

                    scaledDamage *= scale;

                    var health = target.getHealth();
                    if (health <= (scaledDamage) && health > 0.0F) {
                        transfurLogic.get();
                    } else {
                        target.hurt(source, scaledDamage);
                    }
                }

                @Override
                public boolean willAssimilate() {
                    float scaledDamage = LatexProtectionEnchantment.getLatexProtection(target, transfurProgress);
                    float scale = 20.0f / Math.max(0.1f, (float)ProcessTransfur.getEntityTransfurTolerance(target));

                    scaledDamage *= scale;

                    var health = target.getHealth();
                    if (health <= (scaledDamage) && health > 0.0F) {
                        return true;
                    } else {
                        return false;
                    }
                }

                @Override
                public AssimilationBehavior appendTransfurListener(Consumer<IAbstractChangedEntity> nextTransfurLogic) {
                    return progressThenTransfur(target, source, transfurProgress, () -> {
                        var variant = transfurLogic.get();
                        nextTransfurLogic.accept(variant);
                        return variant;
                    });
                }
            };
        }
    }

    static AssimilationBehavior instant(Supplier<IAbstractChangedEntity> transfurLogic) {
        return new AssimilationBehavior() {
            @Override
            public void stepAssimilate() {
                transfurLogic.get();
            }

            @Override
            public boolean willAssimilate() {
                return true;
            }

            @Override
            public AssimilationBehavior appendTransfurListener(Consumer<IAbstractChangedEntity> nextTransfurLogic) {
                return instant(() -> {
                    var variant = transfurLogic.get();
                    nextTransfurLogic.accept(variant);
                    return variant;
                });
            }
        };
    }
}
