package net.ltxprogrammer.changed.entity.ai;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.ability.IAbstractChangedEntity;
import net.ltxprogrammer.changed.ability.ILatexAssimilatedEntity;
import net.ltxprogrammer.changed.entity.TransfurContext;
import net.ltxprogrammer.changed.entity.variant.TransfurVariant;
import net.ltxprogrammer.changed.init.ChangedAnimationEvents;
import net.ltxprogrammer.changed.init.ChangedGameRules;
import net.ltxprogrammer.changed.init.ChangedSounds;
import net.ltxprogrammer.changed.process.ProcessTransfur;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.ltxprogrammer.changed.init.ChangedGameRules.RULE_KEEP_BRAIN;

/**
 * Defines what happens when this mob gets assimilated.
 * @param <T> type of entity being assimilated
 */
public interface EntityAssimilationBehavior<T extends LivingEntity> {
    class InjectEntityWithTransfurGoals<T extends PathfinderMob> implements EntityAssimilationBehavior<T> {
        public final boolean mustSeeTarget;
        public final boolean mustReachTarget;
        public final double speedModifier;
        public final boolean followingTargetEvenIfNotSeen;
        public final TransfurDecider<T> decider;

        public InjectEntityWithTransfurGoals(boolean mustSeeTarget, boolean mustReachTarget, double speedModifier, boolean followingTargetEvenIfNotSeen, TransfurDecider<T> decider) {
            this.mustSeeTarget = mustSeeTarget;
            this.mustReachTarget = mustReachTarget;
            this.speedModifier = speedModifier;
            this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
            this.decider = decider;
        }

        protected void assimilate(T entity) {
            // TODO have the assimilated mob drip latex particles

            final ILatexAssimilatedEntity abstracted = ILatexAssimilatedEntity.forEntity(entity, decider);

            final Function<LivingEntity, LatexAssimilationDecision<?>> qualifiedDecider = decider.withAssimilatedMob(entity);

            final Predicate<LivingEntity> canTransfurTarget = target -> {
                if (target == null)
                    return false;
                var decision = qualifiedDecider.apply(target);
                if (decision == null)
                    return false;
                return ProcessTransfur.computeAssimilationBehavior(entity, decision) != null;
            };

            var targetGoal = new NearestAttackableTargetGoal<>(entity, LivingEntity.class, 10, mustSeeTarget, mustReachTarget, canTransfurTarget);

            var assimilateGoal = new MeleeAttackGoal(entity, speedModifier, followingTargetEvenIfNotSeen) {
                @Override
                public boolean canUse() {
                    return super.canUse() && canTransfurTarget.test(mob.getTarget());
                }

                @Override
                public boolean canContinueToUse() {
                    return super.canContinueToUse() && canTransfurTarget.test(mob.getTarget());
                }

                @Override
                protected void checkAndPerformAttack(@NotNull LivingEntity target, double distanceSquared) {
                    double d0 = this.getAttackReachSqr(target);
                    var decision = qualifiedDecider.apply(target);
                    var behavior = ProcessTransfur.computeAssimilationBehavior(target, decision);
                    if (behavior != null && distanceSquared <= d0 && this.getTicksUntilNextAttack() <= 0) {
                        this.resetAttackCooldown();
                        this.mob.swing(InteractionHand.MAIN_HAND);
                        behavior.stepAssimilate();
                    }
                }
            };
        }

        @Override
        public @Nullable AssimilationBehavior latexAssimilateVictimBehavior(T assimilationVictim, @NotNull LatexAssimilationDecision<?> decision) {
            return AssimilationBehavior.instant(() -> {
                assimilate(assimilationVictim);
                return null;
            });
        }

        @Override
        public @Nullable AssimilationBehavior nonLatexAssimilateVictimBehavior(T assimilationVictim, @NotNull NonLatexAssimilationDecision<?> decision) {
            // TODO defer behavior choice to next-in-line behavior (humanoid)
            return null;
        }

        @Override
        public @Nullable AssimilationBehavior immediateTransfurTargetBehavior(T assimilateTarget, @NotNull ImmediateTransfurDecision<?> decision) {
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
        public @Nullable AssimilationBehavior latexAssimilateVictimBehavior(T assimilationVictim, @NotNull LatexAssimilationDecision<?> decision) {
            if (decision.decisionStrength() == LatexAssimilationDecision.DecisionStrength.LOW) {
                // Override a low strength latex transfur decision with a unique variant for this entity
                var newDecision = LatexAssimilationDecision.strong(decision.method(), variant, decision.context(), decision.transfurProgress(),
                        replaced -> traitMapper.accept(assimilationVictim, replaced));

                return newDecision.latexAssimilateVictimBehavior(assimilationVictim);
            } else {
                // TODO defer behavior choice to next-in-line behavior (humanoid/assimilate)
                return null;
            }
        }

        @Override
        public @Nullable AssimilationBehavior nonLatexAssimilateVictimBehavior(T assimilationVictim, @NotNull NonLatexAssimilationDecision<?> decision) {
            // TODO defer behavior choice to next-in-line behavior (humanoid/assimilate)
            return null;
        }

        @Override
        public @Nullable AssimilationBehavior immediateTransfurTargetBehavior(T assimilateTarget, @NotNull ImmediateTransfurDecision<?> decision) {
            return null;
        }
    }

    class HumanoidAssimilationBehavior<T extends LivingEntity> implements EntityAssimilationBehavior<T> {
        static final HumanoidAssimilationBehavior<LivingEntity> INSTANCE = new HumanoidAssimilationBehavior<>();

        @Override
        public @Nullable AssimilationBehavior latexAssimilateVictimBehavior(T assimilationVictim, @NotNull LatexAssimilationDecision<?> decision) {
            return decision.latexAssimilateVictimBehavior(assimilationVictim);
        }

        @Override
        public @Nullable AssimilationBehavior nonLatexAssimilateVictimBehavior(T assimilationVictim, @NotNull NonLatexAssimilationDecision<?> decision) {
            return decision.assimilateVictimBehavior(assimilationVictim);
        }

        @Override
        public @Nullable AssimilationBehavior immediateTransfurTargetBehavior(T assimilateTarget, @NotNull ImmediateTransfurDecision<?> decision) {
            return decision.transfurTargetBehavior(assimilateTarget);
        }
    }

    class PlayerAssimilationBehavior extends HumanoidAssimilationBehavior<Player> {
        static final PlayerAssimilationBehavior INSTANCE = new PlayerAssimilationBehavior();

        /**
         * Handles setting the player's variant, or replacing them with the variant's entity.
         * Plays the transfur sound and animates the player conditionally.
         * @param victim player to transfur
         * @param variant transfur variant to use
         * @param context context of the transfur
         * @return newly transfurred entity
         */
        protected IAbstractChangedEntity transfurPlayer(Player victim, TransfurVariant<?> variant, TransfurContext context, boolean initialKeepConscious) {
            final var level = victim.level();

            final boolean doAnimation = level.getGameRules().getBoolean(ChangedGameRules.RULE_DO_TRANSFUR_ANIMATION);
            final boolean keepConscious;

            if (initialKeepConscious || level.getGameRules().getBoolean(RULE_KEEP_BRAIN))
                keepConscious = true;
            else {
                if (victim.isCreative())
                    keepConscious = true;
                else {
                    ProcessTransfur.KeepConsciousEvent event = new ProcessTransfur.KeepConsciousEvent(victim, variant, context, false);
                    Changed.postModEvent(event);
                    keepConscious = event.shouldKeepConscious;
                }
            }

            ChangedSounds.broadcastSound(victim, variant.sound, 1.0f, 1.0f);
            IAbstractChangedEntity newAbstractedEntity;

            if (keepConscious || doAnimation) {
                var instance = ProcessTransfur.setPlayerTransfurVariant(victim, variant, context, doAnimation ? 0.0f : 1.0f);
                if (instance == null)
                    return null; // Event canceled

                instance.willSurviveTransfur = keepConscious;
                instance.transfurContext = context;
                newAbstractedEntity = IAbstractChangedEntity.forPlayerWithVariant(victim, instance);

                if (doAnimation)
                    ChangedAnimationEvents.broadcastTransfurAnimation(victim, instance.getParent(), context);
            }

            else {
                newAbstractedEntity = variant.replaceEntity(victim, context.source());
            }

            ProcessTransfur.onNewlyTransfurred(newAbstractedEntity);
            /*if (context.source() != null && context.source().left().isPresent() && context.cause() == TransfurCause.GRAB_REPLICATE) {
                var sourceEntity = context.source().left().get();
                ProcessTransfur.onReplicateEntity(sourceEntity);
            }*/

            return newAbstractedEntity;
        }

        @Override
        public @Nullable AssimilationBehavior latexAssimilateVictimBehavior(Player assimilationVictim, @NotNull LatexAssimilationDecision<?> decision) {
            // Cannot override absorption behavior if absorbing latex is actually a player.
            if (decision.context().isFromPlayer() && decision.method() == LatexAssimilationDecision.Method.ABSORPTION)
                return super.latexAssimilateVictimBehavior(assimilationVictim, decision);

            // Override latex assimilation to animate player or possibly allow them to "survive" the transfur
            return switch (decision.method()) {
                case REPLICATION -> AssimilationBehavior.progressPlayerThenTransfur(assimilationVictim, decision.transfurProgress(), () -> {
                    var newEntity = this.transfurPlayer(assimilationVictim, decision.transfurVariant(), decision.context(), false);
                    decision.postTransfurListener().accept(newEntity);
                    return newEntity;
                });
                case ABSORPTION -> AssimilationBehavior.progressPlayerThenTransfur(assimilationVictim, decision.transfurProgress(), () -> {
                    var newEntity = this.transfurPlayer(assimilationVictim, decision.transfurVariant(), decision.context(), false);
                    decision.postTransfurListener().accept(newEntity);
                    if (decision.context().source() != null)
                        decision.context().source().map(IAbstractChangedEntity::getEntity, ILatexAssimilatedEntity::getEntity).discard();
                    return newEntity;
                });
            };
        }

        @Override
        public @Nullable AssimilationBehavior nonLatexAssimilateVictimBehavior(Player assimilationVictim, @NotNull NonLatexAssimilationDecision<?> decision) {
            return AssimilationBehavior.progressPlayerThenTransfur(assimilationVictim, decision.transfurProgress(), () -> {
                var newEntity = this.transfurPlayer(assimilationVictim, decision.transfurVariant(), TransfurContext.latexHazard(decision.source(), decision.cause()), false);
                decision.postTransfurListener().accept(newEntity);
                return newEntity;
            });
        }

        @Override
        public @Nullable AssimilationBehavior immediateTransfurTargetBehavior(Player assimilateTarget, @NotNull ImmediateTransfurDecision<?> decision) {
            return AssimilationBehavior.instant(() -> {
                var newEntity = this.transfurPlayer(assimilateTarget, decision.transfurVariant(), TransfurContext.latexHazard(decision.source(), decision.cause()), decision.initialKeepConscious());
                decision.postTransfurListener().accept(newEntity);
                return newEntity;
            });
        }
    }

    /**
     * Determines the behavior that this entity handles a decision from a latex source in steps (latex entity, WL puddle, worn traffic cone, worn DL mask).
     * This function does NOT consider fusions.
     * @apiNote Use {@link ProcessTransfur#computeAssimilationBehavior(LivingEntity, LatexAssimilationDecision)} to consider fusions.
     * @param assimilationVictim victim that will be assimilated
     * @param decision incoming decision to handle
     * @return an AssimilationBehavior intended to be run once.
     */
    @ApiStatus.Internal
    @Nullable AssimilationBehavior latexAssimilateVictimBehavior(T assimilationVictim, @NotNull LatexAssimilationDecision<?> decision);

    /**
     * Determines the behavior that this entity handles a decision from a non-latex source in steps (contagious entity, TF gas, dropped syringe).
     * @apiNote Use {@link ProcessTransfur#computeAssimilationBehavior(LivingEntity, NonLatexAssimilationDecision)}.
     * @param assimilationVictim victim that will be assimilated
     * @param decision incoming decision to handle
     * @return an AssimilationBehavior intended to be run once.
     */
    @ApiStatus.Internal
    @Nullable AssimilationBehavior nonLatexAssimilateVictimBehavior(T assimilationVictim, @NotNull NonLatexAssimilationDecision<?> decision);

    /**
     * Determines the behavior that this entity handles a decision from a transfurring source (self-injected syringe, stasis chamber, command).
     * Bypasses the transfur progress meter. This behavior is intended to only affect #changed:humanoids and the player.
     * @apiNote Use {@link ProcessTransfur#computeAssimilationBehavior(LivingEntity, ImmediateTransfurDecision)}.
     * @param assimilateTarget target that will be transfurred
     * @param decision incoming decision to handle
     * @return an AssimilationBehavior intended to be run once.
     */
    @ApiStatus.Internal
    @Nullable AssimilationBehavior immediateTransfurTargetBehavior(T assimilateTarget, @NotNull ImmediateTransfurDecision<?> decision);

    static <T extends PathfinderMob> EntityAssimilationBehavior<T> latexAssimilation(double speedModifier, boolean followingTargetEvenIfNotSeen, TransfurDecider<T> decider) {
        return latexAssimilation(true, false, speedModifier, followingTargetEvenIfNotSeen, decider);
    }

    static <T extends PathfinderMob> EntityAssimilationBehavior<T> latexAssimilation(boolean mustSeeTarget, boolean mustReachTarget, double speedModifier, boolean followingTargetEvenIfNotSeen, TransfurDecider<T> decider) {
        return new InjectEntityWithTransfurGoals<>(mustSeeTarget, mustReachTarget, speedModifier, followingTargetEvenIfNotSeen, decider);
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
