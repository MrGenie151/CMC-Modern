package net.ltxprogrammer.changed.entity;

import net.ltxprogrammer.changed.ability.IAbstractChangedEntity;
import net.ltxprogrammer.changed.entity.ai.AssimilationBehavior;
import net.ltxprogrammer.changed.entity.variant.TransfurVariant;
import net.ltxprogrammer.changed.init.ChangedAttributes;
import net.ltxprogrammer.changed.init.ChangedDamageSources;
import net.ltxprogrammer.changed.process.ProcessTransfur;
import net.ltxprogrammer.changed.world.enchantments.LatexProtectionEnchantment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

/**
 * The transfur decision decided by a transfurring entity.
 * @param strength
 * @param transfurVariant
 * @param postTransfurListener called when the target successfully transfurs into transfurVariant
 */
public record TransfurDecision<T extends ChangedEntity>(Strength strength, Method method, TransfurVariant<T> transfurVariant,
                                                        Consumer<IAbstractChangedEntity> postTransfurListener) {
    public enum Strength {
        LOW,
        HIGH
    }

    public enum Method {
        REPLICATION,
        ABSORPTION
    }

    public static <T extends ChangedEntity> TransfurDecision<T> weak(Method method, TransfurVariant<T> transfurVariant) {
        return weak(method, transfurVariant, entity -> {});
    }

    public static <T extends ChangedEntity> TransfurDecision<T> weak(Method method, TransfurVariant<T> transfurVariant, Consumer<IAbstractChangedEntity> postTransfurListener) {
        return new TransfurDecision<>(Strength.LOW, method, transfurVariant, postTransfurListener);
    }

    public static <T extends ChangedEntity> TransfurDecision<T> strong(Method method, TransfurVariant<T> transfurVariant) {
        return strong(method, transfurVariant, entity -> {});
    }

    public static <T extends ChangedEntity> TransfurDecision<T> strong(Method method, TransfurVariant<T> transfurVariant, Consumer<IAbstractChangedEntity> postTransfurListener) {
        return new TransfurDecision<>(Strength.HIGH, method, transfurVariant, postTransfurListener);
    }

    public float getTransfurDamage(LivingEntity target, IAbstractChangedEntity transfurSource) {
        float damage = (float)transfurSource.getEntity().getAttributeValue(ChangedAttributes.TRANSFUR_DAMAGE.get());
        damage = ProcessTransfur.difficultyAdjustTransfurAmount(target.level().getDifficulty(), damage, transfurSource);
        float attackStrengthScale = transfurSource.isPlayer() ? ((Player)transfurSource.getEntity()).getAttackStrengthScale(0.5F) : 1.0F;
        damage *= 0.2F + attackStrengthScale * attackStrengthScale * 0.8F;

        if (target instanceof Player player) {
            return LatexProtectionEnchantment.getLatexProtection(player, damage);
        } else {
            damage = LatexProtectionEnchantment.getLatexProtection(target, damage);
            float scale = 20.0f / Math.max(0.1f, (float)ProcessTransfur.getEntityTransfurTolerance(target));

            return damage * scale;
        }
    }

    public AssimilationBehavior transfurByReplication(LivingEntity target, IAbstractChangedEntity transfurSource) {
        return AssimilationBehavior.damageThenTransfur(target,
                ChangedDamageSources.entityTransfur(target.level().registryAccess(), transfurSource.getEntity()),
                this.getTransfurDamage(target, transfurSource),
                () -> {
                    ProcessTransfur.transfur(target, target.level(), transfurVariant, false, transfurSource.replicate());
                    // TODO edit .transfur to return the IAbstractChangedEntity
                    //ProcessTransfur.onReplicateEntity(transfurSource);
                    //postTransfurListener.accept(newEntity);
                });
    }

    public AssimilationBehavior transfurByAbsorption(LivingEntity target, IAbstractChangedEntity transfurSource) {
        return AssimilationBehavior.damageThenTransfur(target,
                ChangedDamageSources.entityAbsorb(target.level().registryAccess(), transfurSource.getEntity()),
                this.getTransfurDamage(target, transfurSource),
                () -> {
                    if (target instanceof Player player) {
                        ProcessTransfur.killPlayerByAbsorption(player, transfurSource.getEntity());
                    } else {
                        target.discard();
                    }

                    ProcessTransfur.onAbsorbEntity(transfurSource);
                    postTransfurListener.accept(transfurSource);
                });
    }

    public AssimilationBehavior entityAssimilateVictimBehavior(LivingEntity target, IAbstractChangedEntity transfurSource) {
        return switch (method) {
            case REPLICATION -> this.transfurByReplication(target, transfurSource);
            case ABSORPTION -> this.transfurByAbsorption(target, transfurSource);
        };
    }
}
