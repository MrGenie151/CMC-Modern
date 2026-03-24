package net.ltxprogrammer.changed.process;

import net.ltxprogrammer.changed.ability.IAbstractChangedEntity;
import net.ltxprogrammer.changed.ability.ILatexAssimilatedEntity;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.ltxprogrammer.changed.entity.TransfurCause;
import net.ltxprogrammer.changed.entity.TransfurContext;
import net.ltxprogrammer.changed.entity.ai.ImmediateTransfurDecision;
import net.ltxprogrammer.changed.entity.ai.LatexAssimilationDecision;
import net.ltxprogrammer.changed.entity.ai.NonLatexAssimilationDecision;
import net.ltxprogrammer.changed.entity.variant.TransfurVariant;
import net.ltxprogrammer.changed.entity.variant.TransfurVariantInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TransfurEvents {
    /**
     * Fired whenever a mob becomes assimilated from an unassimilated state.
     * Canceling this event will prevent the entity from receiving the default buffs.
     */
    public static class NewlyAssimilatedEntityEvent extends Event {
        public final @NotNull ILatexAssimilatedEntity entity;

        public NewlyAssimilatedEntityEvent(@NotNull ILatexAssimilatedEntity entity) {
            this.entity = entity;
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    /**
     * Fired whenever an entity becomes transfurred from an untransfurred state.
     * Canceling this event will prevent the entity from receiving the default buffs.
     */
    public static class NewlyTransfurredEntityEvent extends Event {
        public final @NotNull IAbstractChangedEntity entity;

        public NewlyTransfurredEntityEvent(@NotNull IAbstractChangedEntity entity) {
            this.entity = entity;
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    /**
     * Fired whenever a latex entity absorbs another entity (2 entities become 1).
     * Canceling this event will prevent the entity from receiving the default buffs.
     */
    public static class AbsorbedEntityEvent extends Event {
        public final @NotNull IAbstractChangedEntity entity;

        public AbsorbedEntityEvent(@NotNull IAbstractChangedEntity entity) {
            this.entity = entity;
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    /**
     * Fired whenever a latex entity assimilates another entity (replicate themselves, assimilated a mob).
     * Canceling this event will prevent the entity from receiving the default buffs.
     */
    public static class AssimilatedEntityEvent extends Event {
        public final @NotNull IAbstractChangedEntity entity;

        public AssimilatedEntityEvent(@NotNull IAbstractChangedEntity entity) {
            this.entity = entity;
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    /**
     * Fired every tick for untransfurred players to compute transfur progression or regression.
     * Canceling this event will cause the player's transfur progress to remain unchanged.
     * Setting delta progress to a negative value will regress the transfur, while a positive value will progress it.
     */
    public static class TickPlayerTransfurProgressEvent extends Event {
        public final @NotNull Player player;
        public final float currentProgress;
        public final float originalDeltaProgress;
        protected float deltaProgress;

        public TickPlayerTransfurProgressEvent(@NotNull Player player, float currentProgress, float originalDeltaProgress) {
            this.player = player;
            this.currentProgress = currentProgress;
            this.originalDeltaProgress = originalDeltaProgress;
            this.deltaProgress = originalDeltaProgress;
        }

        public @NotNull Player getPlayer() {
            return player;
        }

        public float getCurrentProgress() {
            return currentProgress;
        }

        public float getOriginalDeltaProgress() {
            return originalDeltaProgress;
        }

        public void setDeltaProgress(float deltaProgress) {
            this.deltaProgress = deltaProgress;
        }

        public float getDeltaProgress() {
            return deltaProgress;
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    /**
     * Fired any time an entity is computing an assimilation/transfur decision.
     * Canceling this event will deny the assimilation/transfur decision.
     * Write an event listener for {@link LatexAssimilationDecisionEvent}, {@link NonLatexAssimilationDecisionEvent}, or {@link ImmediateTransfurDecisionEvent}
     * if you need precise control over decision-making.
     */
    public static abstract class AssimilationDecisionEvent extends Event {
        public final @NotNull LivingEntity entity;

        public AssimilationDecisionEvent(@NotNull LivingEntity entity) {
            this.entity = entity;
        }

        public @NotNull LivingEntity getEntity() {
            return entity;
        }

        public abstract @NotNull TransfurVariant<?> getTransfurVariant();
        public abstract void setTransfurVariant(@NotNull TransfurVariant<?> transfurVariant);
        public abstract @NotNull TransfurCause getTransfurCause();
        public abstract @Nullable LivingEntity getSourceEntity();

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    /**
     * Fired after a latex assimilating source made its decision to assimilate,
     * and before the target entity handles the decision.
     * Canceling this event will deny the assimilation decision.
     */
    public static class LatexAssimilationDecisionEvent extends AssimilationDecisionEvent {
        public final @NotNull LatexAssimilationDecision<?> originalDecision;
        protected @NotNull LatexAssimilationDecision<?> decision;

        public LatexAssimilationDecisionEvent(@NotNull LivingEntity entity, @NotNull LatexAssimilationDecision<?> originalDecision) {
            super(entity);
            this.originalDecision = originalDecision;
            this.decision = originalDecision;
        }

        @Override
        public @NotNull TransfurVariant<?> getTransfurVariant() {
            return decision.transfurVariant();
        }

        @Override
        public void setTransfurVariant(@NotNull TransfurVariant<?> transfurVariant) {
            decision = decision.withTransfurVariant(transfurVariant);
        }

        @Override
        public @NotNull TransfurCause getTransfurCause() {
            return decision.context().cause();
        }

        @Override
        public @Nullable LivingEntity getSourceEntity() {
            return decision.context().source() == null ? null :
                    decision.context().source().map(IAbstractChangedEntity::getEntity, ILatexAssimilatedEntity::getEntity);
        }

        public @NotNull LatexAssimilationDecision<?> getOriginalDecision() {
            return originalDecision;
        }

        public @NotNull LatexAssimilationDecision<?> getDecision() {
            return decision;
        }

        public void setDecision(@NotNull LatexAssimilationDecision<?> decision) {
            Objects.requireNonNull(decision);
            this.decision = decision;
        }
    }

    /**
     * Fired after a non-latex assimilating source made its decision to assimilate,
     * and before the target entity handles the decision.
     * Canceling this event will deny the assimilation decision.
     */
    public static class NonLatexAssimilationDecisionEvent extends AssimilationDecisionEvent {
        public final @NotNull NonLatexAssimilationDecision<?> originalDecision;
        protected @NotNull NonLatexAssimilationDecision<?> decision;

        public NonLatexAssimilationDecisionEvent(@NotNull LivingEntity entity, @NotNull NonLatexAssimilationDecision<?> originalDecision) {
            super(entity);
            this.originalDecision = originalDecision;
            this.decision = originalDecision;
        }

        @Override
        public @NotNull TransfurVariant<?> getTransfurVariant() {
            return decision.transfurVariant();
        }

        @Override
        public void setTransfurVariant(@NotNull TransfurVariant<?> transfurVariant) {
            decision = decision.withTransfurVariant(transfurVariant);
        }

        @Override
        public @NotNull TransfurCause getTransfurCause() {
            return decision.cause();
        }

        @Override
        public @Nullable LivingEntity getSourceEntity() {
            return decision.source() == null ? null :
                    decision.source().getEntity();
        }

        public @NotNull NonLatexAssimilationDecision<?> getOriginalDecision() {
            return originalDecision;
        }

        public @NotNull NonLatexAssimilationDecision<?> getDecision() {
            return decision;
        }

        public void setDecision(@NotNull NonLatexAssimilationDecision<?> decision) {
            Objects.requireNonNull(decision);
            this.decision = decision;
        }
    }

    /**
     * Fired after a transfurring source made its decision to transfur,
     * and before the target entity handles the decision.
     * Canceling this event will deny the transfur decision.
     */
    public static class ImmediateTransfurDecisionEvent extends AssimilationDecisionEvent {
        public final @NotNull ImmediateTransfurDecision<?> originalDecision;
        protected @NotNull ImmediateTransfurDecision<?> decision;

        public ImmediateTransfurDecisionEvent(@NotNull LivingEntity entity, @NotNull ImmediateTransfurDecision<?> originalDecision) {
            super(entity);
            this.originalDecision = originalDecision;
            this.decision = originalDecision;
        }

        @Override
        public @NotNull TransfurVariant<?> getTransfurVariant() {
            return decision.transfurVariant();
        }

        @Override
        public void setTransfurVariant(@NotNull TransfurVariant<?> transfurVariant) {
            decision = decision.withTransfurVariant(transfurVariant);
        }

        @Override
        public @NotNull TransfurCause getTransfurCause() {
            return decision.cause();
        }

        @Override
        public @Nullable LivingEntity getSourceEntity() {
            return decision.source() == null ? null :
                    decision.source().getEntity();
        }

        public @NotNull ImmediateTransfurDecision<?> getOriginalDecision() {
            return originalDecision;
        }

        public @NotNull ImmediateTransfurDecision<?> getDecision() {
            return decision;
        }

        public void setDecision(@NotNull ImmediateTransfurDecision<?> decision) {
            Objects.requireNonNull(decision);
            this.decision = decision;
        }
    }

    /**
     * Fired before a TransfurVariantInstance is assigned to a player.
     */
    public static class PreProcessTransfurVariantInstanceEvent extends Event {
        public final @NotNull Player player;
        public final @NotNull TransfurVariant<?> transfurVariant;
        public final @Nullable TransfurContext transfurContext;
        public final @NotNull TransfurVariantInstance<?> transfurVariantInstance;
        public final float transfurProgress;
        public final boolean temporaryFromSuit;

        public PreProcessTransfurVariantInstanceEvent(@NotNull Player player, @NotNull TransfurVariant<?> variant, @Nullable TransfurContext context, @NotNull TransfurVariantInstance<?> instance, float progress, boolean temporaryFromSuit) {
            this.player = player;
            this.transfurVariant = variant;
            this.transfurContext = context;
            this.transfurVariantInstance = instance;
            this.transfurProgress = progress;
            this.temporaryFromSuit = temporaryFromSuit;
        }

        public @NotNull Player getPlayer() {
            return player;
        }

        public @NotNull TransfurVariant<?> getTransfurVariant() {
            return transfurVariant;
        }

        public @Nullable TransfurContext getTransfurContext() {
            return transfurContext;
        }

        public @NotNull TransfurVariantInstance<?> getTransfurVariantInstance() {
            return transfurVariantInstance;
        }

        public float getTransfurProgress() {
            return transfurProgress;
        }

        public boolean isTemporaryFromSuit() {
            return temporaryFromSuit;
        }
    }

    /**
     * Fired whenever a transfur process replaces a living entity with a changed entity.
     */
    public static class ReplaceEntityEvent extends Event {
        public final @NotNull LivingEntity entityToReplace;
        public final @NotNull TransfurVariant<?> transfurVariant;
        public final @Nullable LivingEntity originalCauseOfReplacement;
        protected @Nullable LivingEntity causeOfReplacement;
        public final @NotNull ChangedEntity replacementEntity;

        public ReplaceEntityEvent(@NotNull LivingEntity entityToReplace, @NotNull TransfurVariant<?> transfurVariant, @Nullable LivingEntity originalCauseOfReplacement, @NotNull ChangedEntity replacementEntity) {
            this.entityToReplace = entityToReplace;
            this.transfurVariant = transfurVariant;
            this.originalCauseOfReplacement = originalCauseOfReplacement;
            this.causeOfReplacement = originalCauseOfReplacement;
            this.replacementEntity = replacementEntity;
        }

        public @NotNull LivingEntity getEntityToReplace() {
            return entityToReplace;
        }

        public @NotNull TransfurVariant<?> getTransfurVariant() {
            return transfurVariant;
        }

        public @Nullable LivingEntity getOriginalCauseOfReplacement() {
            return originalCauseOfReplacement;
        }

        public @Nullable LivingEntity getCauseOfReplacement() {
            return causeOfReplacement;
        }

        public void setCauseOfReplacement(@Nullable LivingEntity causeOfReplacement) {
            this.causeOfReplacement = causeOfReplacement;
        }

        public @NotNull ChangedEntity getReplacementEntity() {
            return replacementEntity;
        }

        @Override
        public boolean isCancelable() {
            return false;
        }
    }
}
