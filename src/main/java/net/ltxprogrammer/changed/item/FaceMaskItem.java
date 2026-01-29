package net.ltxprogrammer.changed.item;

import net.ltxprogrammer.changed.data.AccessorySlotType;
import net.ltxprogrammer.changed.init.ChangedSounds;
import net.ltxprogrammer.changed.process.ProcessTransfur;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class FaceMaskItem extends ClothingItem {
    public FaceMaskItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public boolean allowedInSlot(ItemStack itemStack, LivingEntity wearer, AccessorySlotType slot) {
        return ProcessTransfur.getEntityVariant(wearer).isEmpty();
    }
    @Override
    public SoundEvent getEquipSound(ItemStack itemStack) {
        return ChangedSounds.FACE_MASK_EQUIP.get();
    }

    @Override
    public SoundEvent getBreakSound(ItemStack itemStack) {
        return ChangedSounds.FACE_MASK_BREAK.get();
    }
}
