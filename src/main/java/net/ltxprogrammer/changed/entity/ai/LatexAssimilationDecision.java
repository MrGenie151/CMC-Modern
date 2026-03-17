package net.ltxprogrammer.changed.entity.ai;

import com.mojang.datafixers.util.Either;
import net.ltxprogrammer.changed.ability.IAbstractChangedEntity;
import net.ltxprogrammer.changed.ability.ILatexAssimilatedEntity;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.ltxprogrammer.changed.entity.TransfurContext;
import net.ltxprogrammer.changed.entity.variant.TransfurVariant;
import net.ltxprogrammer.changed.init.ChangedDamageSources;
import net.ltxprogrammer.changed.process.ProcessTransfur;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

/**
 * The assimilation decided by a latex transfurring source (entity, block, item).
 * @param decisionStrength
 * @param transfurVariant
 * @param postTransfurListener called when the target successfully transfurs into transfurVariant
 */
public record LatexAssimilationDecision<T extends ChangedEntity>(DecisionStrength decisionStrength,
                                                                 Method method,
                                                                 TransfurVariant<T> transfurVariant,
                                                                 TransfurContext context,
                                                                 float transfurProgress,
                                                                 Consumer<IAbstractChangedEntity> postTransfurListener) {
    public enum DecisionStrength {
        LOW,
        HIGH
    }

    public enum Method {
        REPLICATION,
        ABSORPTION
    }

    public static <T extends ChangedEntity> LatexAssimilationDecision<T> weak(Method method, TransfurVariant<T> transfurVariant, TransfurContext context, float damage) {
        return weak(method, transfurVariant, context, damage, entity -> {});
    }

    public static <T extends ChangedEntity> LatexAssimilationDecision<T> weak(Method method, TransfurVariant<T> transfurVariant, TransfurContext context, float damage, Consumer<IAbstractChangedEntity> postTransfurListener) {
        return new LatexAssimilationDecision<>(DecisionStrength.LOW, method, transfurVariant, context, damage, postTransfurListener);
    }

    public static <T extends ChangedEntity> LatexAssimilationDecision<T> strong(Method method, TransfurVariant<T> transfurVariant, TransfurContext context, float damage) {
        return strong(method, transfurVariant, context, damage, entity -> {});
    }

    public static <T extends ChangedEntity> LatexAssimilationDecision<T> strong(Method method, TransfurVariant<T> transfurVariant, TransfurContext context, float damage, Consumer<IAbstractChangedEntity> postTransfurListener) {
        return new LatexAssimilationDecision<>(DecisionStrength.HIGH, method, transfurVariant, context, damage, postTransfurListener);
    }

    protected AssimilationBehavior transfurByReplication(LivingEntity target, IAbstractChangedEntity transfurSource) {
        return AssimilationBehavior.progressThenTransfur(target,
                ChangedDamageSources.entityTransfur(target.level().registryAccess(), transfurSource.getEntity()),
                transfurProgress,
                () -> {
                    var newEntity = transfurVariant.replaceEntity(target, transfurSource);
                    postTransfurListener.accept(newEntity);
                    return newEntity;
                });
    }

    protected AssimilationBehavior transfurByAbsorption(LivingEntity target, IAbstractChangedEntity transfurSource) {
        return AssimilationBehavior.progressThenTransfur(target,
                ChangedDamageSources.entityAbsorb(target.level().registryAccess(), transfurSource.getEntity()),
                transfurProgress,
                () -> {
                    if (target instanceof Player player) {
                        ProcessTransfur.killPlayerByAbsorption(player, transfurSource.getEntity());
                    } else {
                        target.discard();
                    }

                    transfurSource.replaceVariant(transfurVariant);
                    ProcessTransfur.onAbsorbEntity(transfurSource);
                    postTransfurListener.accept(transfurSource);
                    return transfurSource;
                });
    }

    protected AssimilationBehavior transfurByAbsorption(LivingEntity target, ILatexAssimilatedEntity transfurSource) {
        return AssimilationBehavior.progressThenTransfur(target,
                ChangedDamageSources.entityAbsorb(target.level().registryAccess(), transfurSource.getEntity()),
                transfurProgress,
                () -> {
                    var newEntity = transfurVariant.replaceEntity(target, transfurSource);
                    ProcessTransfur.onAbsorbEntity(newEntity);
                    postTransfurListener.accept(newEntity);
                    return newEntity;
                });
    }

    protected AssimilationBehavior entityLatexAssimilateVictimBehavior(LivingEntity target, Either<IAbstractChangedEntity, ILatexAssimilatedEntity> transfurSource) {
        if (transfurSource.right().isPresent())
            return transfurByAbsorption(target, transfurSource.right().get());

        return switch (method) {
            case REPLICATION -> this.transfurByReplication(target, transfurSource.left().get());
            case ABSORPTION -> this.transfurByAbsorption(target, transfurSource.left().get());
        };
    }

    public AssimilationBehavior latexAssimilateVictimBehavior(LivingEntity target) {
        if (context.source() != null)
            return entityLatexAssimilateVictimBehavior(target, context.source());

        return AssimilationBehavior.progressThenTransfur(target,
                ChangedDamageSources.entityTransfur(target.level().registryAccess(), (LivingEntity)null),
                transfurProgress,
                () -> {
                    var newEntity = transfurVariant.replaceEntity(target, (LivingEntity)null);
                    postTransfurListener.accept(newEntity);
                    return newEntity;
                });
    }

    public LatexAssimilationDecision<T> withTransfurProgress(float newProgress) {
        return new LatexAssimilationDecision<>(decisionStrength, method, transfurVariant, context, newProgress, postTransfurListener);
    }
}
