package net.ltxprogrammer.changed.mixin.compatibility.Curios;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.ltxprogrammer.changed.extension.RequiredMods;
import net.ltxprogrammer.changed.util.EntityUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Map;

@Mixin(value = CuriosApi.class, remap = false)
@RequiredMods("curios")
public abstract class CuriosApiMixin {
    @WrapMethod(method = "getEntitySlots(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Map;")
    private static Map<String, ISlotType> changed$getEntitySlots(LivingEntity livingEntity, Operation<Map<String, ISlotType>> original) {
        return original.call(EntityUtil.maybeGetUnderlying(livingEntity));
    }

    @WrapMethod(method = "getItemStackSlots(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Map;")
    private static Map<String, ISlotType> changed$getItemStackSlots(ItemStack stack, LivingEntity livingEntity, Operation<Map<String, ISlotType>> original) {
        return original.call(stack, EntityUtil.maybeGetUnderlying(livingEntity));
    }

    @WrapMethod(method = "getCuriosInventory")
    private static LazyOptional<ICuriosItemHandler> changed$getCuriosInventory(LivingEntity livingEntity, Operation<LazyOptional<ICuriosItemHandler>> original) {
        return original.call(EntityUtil.maybeGetUnderlying(livingEntity));
    }
}
