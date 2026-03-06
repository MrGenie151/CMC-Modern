package net.ltxprogrammer.changed.entity.ai;

import net.ltxprogrammer.changed.ability.IAbstractChangedEntity;
import net.ltxprogrammer.changed.entity.TransfurCause;
import net.ltxprogrammer.changed.entity.TransfurContext;
import net.ltxprogrammer.changed.entity.TransfurDecision;
import net.ltxprogrammer.changed.entity.variant.TransfurVariant;
import net.ltxprogrammer.changed.init.ChangedTransfurVariants;
import net.ltxprogrammer.changed.process.ProcessTransfur;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;

/**
 * Defines what happens when this mob gets assimilated.
 * @param <T>
 */
public interface EntityAssimilationBehavior<T extends LivingEntity> {
    class InjectEntityWithTransfurGoals<T extends PathfinderMob> implements EntityAssimilationBehavior<T> {
        public final double speedModifier;
        public final boolean followingTargetEvenIfNotSeen;
        public final TransfurDecider<T> decider;

        public InjectEntityWithTransfurGoals(double speedModifier, boolean followingTargetEvenIfNotSeen, TransfurDecider<T> decider) {
            this.speedModifier = speedModifier;
            this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
            this.decider = decider;
        }

        public void assimilate(T entity) {
            // TODO add an assim/transfur target selector goal and melee attack TF goal to entity.
            // TODO have the assimilated mob drip latex particles
        }

        @Override
        public AssimilationBehavior entityAssimilateVictimBehavior(TransfurCause cause, T assimVictim, IAbstractChangedEntity transfurSource) {
            return null;
        }
    }

    class ReplaceSelfWithVariant<T extends LivingEntity> implements EntityAssimilationBehavior<T> {
        public final TransfurVariant<?> variant;
        public final BiConsumer<T, IAbstractChangedEntity> traitMapper;

        public ReplaceSelfWithVariant(TransfurVariant<?> variant, BiConsumer<T, IAbstractChangedEntity> traitMapper) {
            this.variant = variant;
            this.traitMapper = traitMapper;
        }

        @Override
        public AssimilationBehavior entityAssimilateVictimBehavior(TransfurCause cause, T assimVictim, IAbstractChangedEntity transfurSource) {
            var decision = transfurSource.getTransfurDecision(cause, assimVictim);
            if (decision == null)
                return null;
            if (decision.strength() == TransfurDecision.Strength.LOW) {
                var newDecision = new TransfurDecision<>(TransfurDecision.Strength.HIGH, decision.method(), variant,
                        replaced -> traitMapper.accept(assimVictim, replaced));

                return newDecision.entityAssimilateVictimBehavior(assimVictim, transfurSource);
            } else {
                return decision.entityAssimilateVictimBehavior(assimVictim, transfurSource);
            }
        }
    }

    class HumanoidAssimilationBehavior<T extends LivingEntity> implements EntityAssimilationBehavior<T> {
        static final HumanoidAssimilationBehavior<LivingEntity> INSTANCE = new HumanoidAssimilationBehavior<>();

        @Override
        public AssimilationBehavior entityAssimilateVictimBehavior(TransfurCause cause, T assimVictim, IAbstractChangedEntity transfurSource) {
            var decision = transfurSource.getTransfurDecision(cause, assimVictim);
            return decision == null ? null : decision.entityAssimilateVictimBehavior(assimVictim, transfurSource);
        }
    }

    class PlayerAssimilationBehavior extends HumanoidAssimilationBehavior<Player> {
        static final PlayerAssimilationBehavior INSTANCE = new PlayerAssimilationBehavior();

        @Override
        public AssimilationBehavior entityAssimilateVictimBehavior(TransfurCause cause, Player assimVictim, IAbstractChangedEntity transfurSource) {
            if (transfurSource.isPlayer())
                return super.entityAssimilateVictimBehavior(cause, assimVictim, transfurSource);

            var decision = transfurSource.getTransfurDecision(cause, assimVictim);
            if (decision == null)
                return null;

            if (decision.method() == TransfurDecision.Method.ABSORPTION) {
                return AssimilationBehavior.damagePlayerThenTransfur(assimVictim, decision.getTransfurDamage(assimVictim, transfurSource), () -> {
                    ProcessTransfur.transfur(assimVictim, assimVictim.level(), decision.transfurVariant(), false, transfurSource.absorb());
                    transfurSource.getEntity().discard();
                });
            }

            return decision.entityAssimilateVictimBehavior(assimVictim, transfurSource);
        }
    }

    AssimilationBehavior entityAssimilateVictimBehavior(TransfurCause cause, T assimVictim, IAbstractChangedEntity transfurSource);

    static <T extends PathfinderMob> EntityAssimilationBehavior<T> latexAssimilation(double speedModifier, boolean followingTargetEvenIfNotSeen, TransfurDecider<T> decider) {
        return new InjectEntityWithTransfurGoals<>(speedModifier, followingTargetEvenIfNotSeen, decider);
    }

    static <T extends LivingEntity> EntityAssimilationBehavior<T> uniqueVariant(TransfurVariant<?> variant) {
        return uniqueVariant(variant, (oldEntity, newEntity) -> {});
    }

    static <T extends LivingEntity> EntityAssimilationBehavior<T> uniqueVariant(TransfurVariant<?> variant, BiConsumer<T, IAbstractChangedEntity> traitMapper) {
        return new ReplaceSelfWithVariant<>(variant, traitMapper);
    }

    static EntityAssimilationBehavior<LivingEntity> defaultHumanoid() {
        return HumanoidAssimilationBehavior.INSTANCE;
    }

    static EntityAssimilationBehavior<Player> defaultPlayer() {
        return PlayerAssimilationBehavior.INSTANCE;
    }
}
