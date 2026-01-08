package net.ltxprogrammer.changed.mixin.render;

import net.ltxprogrammer.changed.client.ClientLivingEntityExtender;
import net.ltxprogrammer.changed.client.renderer.AdvancedHumanoidRenderer;
import net.ltxprogrammer.changed.client.animations.Limb;
import net.ltxprogrammer.changed.client.renderer.ExoskeletonRenderer;
import net.ltxprogrammer.changed.client.renderer.accessory.WornExoskeletonRenderer;
import net.ltxprogrammer.changed.client.renderer.layers.AccessoryLayer;
import net.ltxprogrammer.changed.client.tfanimations.TransfurAnimator;
import net.ltxprogrammer.changed.entity.robot.Exoskeleton;
import net.ltxprogrammer.changed.init.ChangedAccessoryRenderers;
import net.ltxprogrammer.changed.item.SpecializedAnimations;
import net.ltxprogrammer.changed.process.ProcessTransfur;
import net.ltxprogrammer.changed.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {
    @Shadow @Final public ModelPart leftLeg;

    @Shadow @Final public ModelPart rightLeg;

    @Unique
    protected SpecializedAnimations.AnimationHandler.EntityStateContext entityContextOf(T entity, float partialTicks) {
        return new SpecializedAnimations.AnimationHandler.EntityStateContext(entity, this.attackTime, partialTicks) {
            @Override
            public HumanoidArm getAttackArm() {
                HumanoidArm humanoidarm = entity.getMainArm();
                return entity.swingingArm == InteractionHand.MAIN_HAND ? humanoidarm : humanoidarm.getOpposite();
            }
        };
    }

    @Unique
    protected SpecializedAnimations.AnimationHandler.UpperModelContext upperModelContext() {
        HumanoidModel<T> self = (HumanoidModel<T>)(Object)this;
        return new SpecializedAnimations.AnimationHandler.UpperModelContext(self.leftArm, self.rightArm, self.body, self.head) {
            @Override
            public ModelPart getArm(HumanoidArm humanoidArm) {
                return humanoidArm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
            }
        };
    }

    @Inject(method = "setupAttackAnimation", at = @At("HEAD"), cancellable = true)
    protected void setupAttackAnimation(T entity, float ageInTicks, CallbackInfo callback) {
        var entityContext = entityContextOf(entity, ageInTicks - entity.tickCount);
        var upperModelContext = upperModelContext();

        var mainHandItem = entity.getItemBySlot(EquipmentSlot.MAINHAND);
        var offHandItem = entity.getItemBySlot(EquipmentSlot.OFFHAND);
        if ((!entity.isUsingItem() || entity.getUsedItemHand() == InteractionHand.MAIN_HAND) &&
                !mainHandItem.isEmpty() && mainHandItem.getItem() instanceof SpecializedAnimations specialized) {
            var handler = specialized.getAnimationHandler();
            if (handler != null && handler.setupAnimation(mainHandItem, entityContext, upperModelContext, InteractionHand.MAIN_HAND)) {
                callback.cancel();
            }
        }

        else if ((!entity.isUsingItem() || entity.getUsedItemHand() == InteractionHand.OFF_HAND) &&
                !offHandItem.isEmpty() && offHandItem.getItem() instanceof SpecializedAnimations specialized) {
            var handler = specialized.getAnimationHandler();
            if (handler != null && handler.setupAnimation(offHandItem, entityContext, upperModelContext, InteractionHand.OFF_HAND)) {
                callback.cancel();
            }
        }
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;copyFrom(Lnet/minecraft/client/model/geom/ModelPart;)V"))
    public void setupAnimAndForceAnimation(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!TransfurAnimator.isCapturing()) {
            ((ClientLivingEntityExtender) entity).getOrderedAnimations().forEach(instance -> {
                instance.animate((HumanoidModel<?>) (Object) this, Mth.positiveModulo(ageInTicks, 1.0f));
            });
        }

        if (limbSwing == 0.0f && limbSwingAmount == 0.0f && ageInTicks == 0.0f && netHeadYaw == 0.0f && headPitch == 0.0f) {
            // Exception case when rendering hand, ignore positioning other limbs
            ((ClientLivingEntityExtender)entity).getOrderedAnimations().forEach(instance -> {
                instance.resetToBaseline((HumanoidModel<?>)(Object) this, entity, identifier -> {
                    return identifier.limb() != Limb.LEFT_ARM && identifier.limb() != Limb.RIGHT_ARM;
                });
            });
        }

        ProcessTransfur.ifPlayerTransfurred(EntityUtil.playerOrNull(entity), variant -> {
            if (variant.transfurProgression < 1f) {
                final Minecraft minecraft = Minecraft.getInstance();
                final EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
                final var latexRenderer = dispatcher.getRenderer(variant.getChangedEntity());

                if (!(latexRenderer instanceof AdvancedHumanoidRenderer<?,?> latexHumanoidRenderer)) return;

                final var latexModel = latexHumanoidRenderer.getModel(variant.getChangedEntity());

                Arrays.stream(Limb.values()).map(latexModel::getTransfurHelperModel).filter(Objects::nonNull).forEach(helper -> {
                    helper.transitionOriginal((HumanoidModel<?>)(Object)this, TransfurAnimator.getPreMorphProgression(variant.getTransfurProgression(ageInTicks)));
                });
            }
        });

        Exoskeleton.getEntityExoskeleton(entity).ifPresent(pair -> {
            this.leftLeg.visible = false;
            this.rightLeg.visible = false;
            if ((Object)this instanceof PlayerModel<?> playerModel) {
                playerModel.leftPants.visible = false;
                playerModel.rightPants.visible = false;
            }

            AccessoryLayer.getRenderer(pair.getSecond()).ifPresent(renderer -> {
                if (renderer instanceof WornExoskeletonRenderer exoRenderer) {
                    exoRenderer.getModel().animateWearerLimbs(this, pair.getFirst());
                }
            });
        });
    }
}
