package net.ltxprogrammer.changed.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.ltxprogrammer.changed.client.renderer.accessory.AccessoryRenderer;
import net.ltxprogrammer.changed.data.AccessorySlotContext;
import net.ltxprogrammer.changed.data.AccessorySlots;
import net.ltxprogrammer.changed.entity.AccessoryEntities;
import net.ltxprogrammer.changed.util.Cacheable;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class AccessoryLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> implements FirstPersonLayer<T> {
    private static final Map<ItemLike, Cacheable<AccessoryRenderer>> RENDERERS = new HashMap<>();

    public static void registerRenderer(ItemLike item, Supplier<AccessoryRenderer> renderer) {
        RENDERERS.put(item, Cacheable.of(renderer));
    }

    public static Optional<AccessoryRenderer> getRenderer(ItemLike item) {
        return Optional.ofNullable(RENDERERS.get(item)).map(Supplier::get);
    }

    private final RenderLayerParent<T, M> parent;

    public AccessoryLayer(RenderLayerParent<T, M> parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        final var slotTypePredicate = AccessoryEntities.INSTANCE.canEntityTypeUseSlot(AccessoryEntities.getApparentEntityType(entity));
        AccessorySlots.getForEntity(entity).ifPresent(slots -> {
            slots.forEachSlot((slotType, stack) -> {
                if (stack.isEmpty())
                    return;
                if (!RENDERERS.containsKey(stack.getItem()))
                    return;
                if (!slotTypePredicate.test(slotType) || !slotType.canHoldItem(stack, entity))
                    return; // Ensure lag doesn't crash with an invalid slot

                var context = new AccessorySlotContext<>(entity, slotType, stack);
                RENDERERS.get(stack.getItem()).get().render(context, poseStack, this.parent, buffers, packedLight, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            });
        });
    }

    @Override
    public void renderFirstPersonOnArms(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity, HumanoidArm arm, PartPose armPose, PoseStack stackCorrector, float partialTick) {
        final var slotTypePredicate = AccessoryEntities.INSTANCE.canEntityTypeUseSlot(AccessoryEntities.getApparentEntityType(entity));
        AccessorySlots.getForEntity(entity).ifPresent(slots -> {
            slots.forEachSlot((slotType, stack) -> {
                if (stack.isEmpty())
                    return;
                if (!RENDERERS.containsKey(stack.getItem()))
                    return;
                if (!slotTypePredicate.test(slotType) || !slotType.canHoldItem(stack, entity))
                    return; // Ensure lag doesn't crash with an invalid slot

                var context = new AccessorySlotContext<>(entity, slotType, stack);
                RENDERERS.get(stack.getItem()).get().renderFirstPersonOnArms(context, poseStack, this.parent, buffers, packedLight, arm, armPose, stackCorrector, partialTick);
            });
        });
    }
}
