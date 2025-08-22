package net.ltxprogrammer.changed.entity.beast;

import net.ltxprogrammer.changed.entity.TamableLatexEntity;
import net.ltxprogrammer.changed.entity.ai.*;
import net.ltxprogrammer.changed.entity.latex.LatexType;
import net.ltxprogrammer.changed.init.ChangedAttributes;
import net.ltxprogrammer.changed.init.ChangedCriteriaTriggers;
import net.ltxprogrammer.changed.init.ChangedItems;
import net.ltxprogrammer.changed.init.ChangedLatexTypes;
import net.ltxprogrammer.changed.network.syncher.ChangedEntityDataSerializers;
import net.ltxprogrammer.changed.world.inventory.TamedDarkLatexMenu;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class AbstractDarkLatexEntity extends AbstractLatexWolf implements DarkLatexEntity, TamableLatexEntity {
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(AbstractDarkLatexEntity.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(AbstractDarkLatexEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<DarkLatexTargetType> DATA_TARGET_TYPE_ID = SynchedEntityData.defineId(AbstractDarkLatexEntity.class, ChangedEntityDataSerializers.DARK_LATEX_TARGET_TYPE);
    protected static final EntityDataAccessor<DarkLatexAttackType> DATA_ATTACK_TYPE_ID = SynchedEntityData.defineId(AbstractDarkLatexEntity.class, ChangedEntityDataSerializers.DARK_LATEX_ATTACK_TYPE);
    protected static final EntityDataAccessor<DarkLatexAttackCondition> DATA_ATTACK_CONDITION_ID = SynchedEntityData.defineId(AbstractDarkLatexEntity.class, ChangedEntityDataSerializers.DARK_LATEX_ATTACK_CONDITION);
    protected static final EntityDataAccessor<DarkLatexFavor> DATA_FAVOR_ID = SynchedEntityData.defineId(AbstractDarkLatexEntity.class, ChangedEntityDataSerializers.DARK_LATEX_FAVOR);
    protected @Nullable DarkLatexInventory inventory; // Inventory doesn't exist until DL is tamed

    public static final int OWNER_HOSTILE_DURATION_TICKS = 600;

    public AbstractDarkLatexEntity(EntityType<? extends AbstractLatexWolf> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        this.inventory = null;
    }

    public DarkLatexInventory createInventory() {
        return new DarkLatexInventory(this);
    }

    public @Nullable DarkLatexInventory getInventory() {
        return inventory;
    }

    @Override
    public ItemStack getItemInHand(InteractionHand hand) {
        if (inventory == null)
            return super.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND)
            return inventory.getItem(DarkLatexInventory.SLOT_OFFHAND);
        else {
            return inventory.getSelected();
        }
    }

    @Override
    public SlotAccess getSlot(int slotIndex) {
        if (this.inventory == null)
            return super.getSlot(slotIndex);
        else {
            if (slotIndex >= 0 && slotIndex < this.inventory.items.size()) {
                return SlotAccess.forContainer(this.inventory, slotIndex);
            } else {
                return super.getSlot(slotIndex);
            }
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new DarkLatexFishingGoal(this, 0.3, 8, 5));
        this.goalSelector.addGoal(6, new LatexFollowOwnerGoal<>(this, 0.35D, 10.0F, 2.0F, false));
        this.targetSelector.addGoal(1, new LatexOwnerHurtByTargetGoal<>(this));
        this.targetSelector.addGoal(2, new LatexOwnerHurtTargetGoal<>(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
        this.entityData.define(DATA_TARGET_TYPE_ID, DarkLatexTargetType.TRANSFURABLE_ENTITIES);
        this.entityData.define(DATA_ATTACK_TYPE_ID, DarkLatexAttackType.TRY_TRANSFUR);
        this.entityData.define(DATA_ATTACK_CONDITION_ID, DarkLatexAttackCondition.ALWAYS);
        this.entityData.define(DATA_FAVOR_ID, DarkLatexFavor.NONE);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_OWNERUUID_ID.equals(accessor)) {
            if (this.inventory == null)
                this.inventory = createInventory();
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        UUID uuid;
        if (tag.hasUUID("Owner")) {
            uuid = tag.getUUID("Owner");
        } else {
            String s = tag.getString("Owner");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
        }

        if (tag.contains("FollowOwner"))
            this.setFollowOwner(tag.getBoolean("FollowOwner"));

        if (uuid != null) {
            try {
                this.setOwnerUUID(uuid);
                this.setTame(true);
            } catch (Throwable throwable) {
                this.setTame(false);
            }
        }

        if (tag.contains("Inventory")) {
            ListTag listtag = tag.getList("Inventory", 10);
            this.inventory = this.createInventory();
            this.inventory.load(listtag);
            this.inventory.selected = tag.getInt("SelectedItemSlot");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (this.getOwnerUUID() != null) {
            tag.putUUID("Owner", this.getOwnerUUID());
        }

        tag.putBoolean("FollowOwner", this.isFollowingOwner());
        if (this.inventory != null) {
            tag.put("Inventory", this.inventory.save(new ListTag()));
            tag.putInt("SelectedItemSlot", this.inventory.selected);
        }
    }

    public boolean isMaskless() {
        return false;
    }

    @Override
    public LatexType getLatexType() {
        return ChangedLatexTypes.DARK_LATEX.get();
    }

    @Override
    protected boolean targetSelectorTest(LivingEntity livingEntity) {
        final var owner = this.getOwner();
        if (livingEntity == owner)
            return false;

        Predicate<LivingEntity> superPredicate = super::targetSelectorTest;
        if (owner != null) {
            superPredicate = switch (getAttackCondition()) {
                case NEVER -> target -> false;
                case ALWAYS -> getTargetType().forEntity(this);
                case OWNER_IS_HOSTILE -> getTargetType().forEntity(this)
                        .and(target -> owner.tickCount - owner.getLastHurtMobTimestamp() < OWNER_HOSTILE_DURATION_TICKS);
            };
        }
        
        if (!this.isMaskless()) {// Check if masked DL can see entity
            if (livingEntity.distanceToSqr(this) <= 1.0)
                return superPredicate.test(livingEntity);
            if (getLevelBrightnessAt(livingEntity.blockPosition()) >= 5)
                return superPredicate.test(livingEntity);

            var delta = livingEntity.getDeltaMovement();
            var xyMovement = delta.subtract(0, delta.y, 0);
            if (livingEntity.getPose() == Pose.CROUCHING || xyMovement.lengthSqr() < Mth.EPSILON)
                return false;
        }

        return superPredicate.test(livingEntity);
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    public DarkLatexTargetType getTargetType() {
        return this.entityData.get(DATA_TARGET_TYPE_ID);
    }

    public DarkLatexAttackType getAttackType() {
        return this.entityData.get(DATA_ATTACK_TYPE_ID);
    }

    public DarkLatexAttackCondition getAttackCondition() {
        return this.entityData.get(DATA_ATTACK_CONDITION_ID);
    }

    public DarkLatexFavor getCurrentFavor() {
        return this.entityData.get(DATA_FAVOR_ID);
    }

    public void setTargetType(DarkLatexTargetType value) {
        this.entityData.set(DATA_TARGET_TYPE_ID, value);
    }

    public void setAttackType(DarkLatexAttackType value) {
        this.entityData.set(DATA_ATTACK_TYPE_ID, value);
    }

    public void setAttackCondition(DarkLatexAttackCondition value) {
        this.entityData.set(DATA_ATTACK_CONDITION_ID, value);
    }

    public void setFavor(DarkLatexFavor value) {
        this.entityData.set(DATA_FAVOR_ID, value);
    }

    public boolean isPreventingPlayerRest(Player player) {
        if (isTame() && player.getUUID().equals(getOwnerUUID()))
            return false;
        return super.isPreventingPlayerRest(player);
    }

    protected void spawnTamingParticles(boolean success) {
        ParticleOptions particleoptions = ParticleTypes.HEART;
        if (!success) {
            particleoptions = ParticleTypes.SMOKE;
        }

        for(int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level().addParticle(particleoptions, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
        }
    }

    protected void spawnHeartParticles() {
        this.spawnTamingParticles(true);
    }

    public void handleEntityEvent(byte event) {
        if (event == 7) {
            this.spawnTamingParticles(true);
        } else if (event == 6) {
            this.spawnTamingParticles(false);
        } else if (event == 18) {
            this.spawnHeartParticles();
        } else {
            super.handleEntityEvent(event);
        }

    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        try {
            UUID uuid = this.getOwnerUUID();
            return uuid == null ? null : this.level().getPlayerByUUID(uuid);
        } catch (IllegalArgumentException illegalargumentexception) {
            return null;
        }
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(uuid));
    }

    public void tame(Player player) {
        this.setTame(true);
        this.setFollowOwner(true);
        this.setOwnerUUID(player.getUUID());
        if (player instanceof ServerPlayer serverPlayer) {
            ChangedCriteriaTriggers.TAME_LATEX.trigger(serverPlayer, this);
        }

    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        Item item = itemstack.getItem();
        if (this.level().isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isTame();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            if (this.isTame()) {
                if (this.isTame() && this.isTameItem(itemstack) && this.getHealth() < this.getMaxHealth()) {
                    itemstack.shrink(1);
                    this.heal(2.0F);
                    this.level().broadcastEntityEvent(this, (byte)7); // Spawn hearts
                    return InteractionResult.SUCCESS;
                } else {
                    InteractionResult interactionresult = super.mobInteract(player, hand);
                    if ((!interactionresult.consumesAction() || this.isBaby()) && this.isOwnedBy(player)) {
                        /*boolean shouldFollow = !this.isFollowingOwner();
                        this.setFollowOwner(shouldFollow);

                        player.displayClientMessage(Component.translatable(shouldFollow ? "text.changed.tamed.follow" : "text.changed.tamed.wander", this.getDisplayName()), true);
                        this.jumping = false;
                        this.navigation.stop();
                        this.setTarget((LivingEntity) null);*/
                        if (player instanceof ServerPlayer serverPlayer)
                            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                                    (id, inv, viewer) -> new TamedDarkLatexMenu(id, inv, this),
                                    this.getDisplayName()
                            ), extraData -> {
                                extraData.writeInt(this.getId());
                            });
                        return InteractionResult.sidedSuccess(player.level().isClientSide);
                    }

                    return interactionresult;
                }
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFollowingOwner() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    @Override
    public void setFollowOwner(boolean value) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (value) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 1));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -2));
        }

    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof AbstractDarkLatexEntity) {
            return false;
        }

        if (getAttackCondition() == DarkLatexAttackCondition.NEVER) {
            return false;
        }

        return TamableLatexEntity.super.wantsToAttack(target, owner);
    }

    @Override
    public void checkDespawn() {
        if (isTame())
            return;
        super.checkDespawn();
    }

    public boolean isTame() {
        return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
    }

    public void setTame(boolean tame) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (tame) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 4));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -5));
        }

        this.reassessTameGoals();
        if (tame && this.inventory == null)
            this.inventory = this.createInventory();
    }

    protected void reassessTameGoals() {
    }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity == this.getOwner();
    }

    public boolean canAttack(LivingEntity entity) {
        return !this.isOwnedBy(entity) && super.canAttack(entity);
    }

    public Team getTeam() {
        if (this.isTame()) {
            LivingEntity livingentity = this.getOwner();
            if (livingentity != null) {
                return livingentity.getTeam();
            }
        }

        return super.getTeam();
    }

    public boolean isAlliedTo(Entity entity) {
        if (this.isTame()) {
            LivingEntity livingentity = this.getOwner();
            if (entity == livingentity) {
                return true;
            }

            if (livingentity != null) {
                return livingentity.isAlliedTo(entity);
            }
        }

        return super.isAlliedTo(entity);
    }

    public void die(DamageSource source) {
        // FORGE: Super moved to top so that death message would be cancelled properly
        net.minecraft.network.chat.Component deathMessage = this.getCombatTracker().getDeathMessage();
        super.die(source);

        if (this.dead)
            if (!this.level().isClientSide && this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getOwner() instanceof ServerPlayer) {
                this.getOwner().sendSystemMessage(deathMessage);
            }
    }

    protected void dropEquipment() {
        super.dropEquipment();
        if (this.inventory != null)
            this.inventory.dropAll();
    }

    protected boolean isTameItem(ItemStack stack) {
        return stack.is(ChangedItems.WHITE_LATEX_GOO.get()) || stack.is(ChangedItems.ORANGE.get());
    }

    @Override
    public void onDamagedBy(LivingEntity source) {
        super.onDamagedBy(source);
        if (source instanceof Player player && player.isCreative())
            return;
        if (getAttackCondition() == DarkLatexAttackCondition.NEVER)
            return;

        double d0 = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB aabb = AABB.unitCubeFromLowerCorner(this.position()).inflate(d0, 10.0D, d0);
        this.level().getEntitiesOfClass(AbstractDarkLatexEntity.class, aabb, EntitySelector.NO_SPECTATORS).forEach(nearby -> {
            if (nearby.getTarget() == null && !nearby.isAlliedTo(source))
                nearby.setTarget(source);
        });
    }

    @Override
    protected void setAttributes(AttributeMap attributes) {
        super.setAttributes(attributes);
        attributes.getInstance(Attributes.FOLLOW_RANGE).setBaseValue(25.0);
    }

    @Override
    public boolean tryTransfurTarget(Entity entity) {
        if (entity instanceof LivingEntity livingEntity && this.getUnderlyingPlayer() == null) {
            if (!getAttackType().test(this, livingEntity))
                return false; // Cancel attempt to transfur
        }

        return super.tryTransfurTarget(entity);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            updateHeldItemChoice();
        }
    }

    protected int findSlotForTransfur() {
        return this.inventory == null ? -1 : this.inventory.getFreeSlot();
    }

    protected int findSlotForCombat() {
        // Maybe add bow AI?
        if (this.inventory == null)
            return -1;

        double bestScore = 0;
        int bestSlot = this.inventory.selected;

        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            var itemStack = this.inventory.getItem(i);
            if (itemStack.isEmpty())
                continue;
            double score = 0;
            var modifiers = this.inventory.getItem(i).getAttributeModifiers(EquipmentSlot.MAINHAND);
            if (modifiers.containsKey(Attributes.ATTACK_DAMAGE))
                score += modifiers.get(Attributes.ATTACK_DAMAGE).stream().mapToDouble(AttributeModifier::getAmount).sum();
            if (modifiers.containsKey(Attributes.ATTACK_SPEED))
                score += modifiers.get(Attributes.ATTACK_SPEED).stream().mapToDouble(AttributeModifier::getAmount).sum();

            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    protected int findSlotForNonCombat() {
        // TODO find a book, food, fishing rod, etc.
        return this.inventory == null ? -1 : this.inventory.getFreeSlot();
    }

    public void updateHeldItemChoice() {
        if (this.inventory == null)
            return;

        LivingEntity target = this.getTarget();
        boolean inCombat = target != null;
        boolean wantTransfur = inCombat && getAttackType().test(this, target); // Find empty slot, or else a strong weapon

        if (inCombat) {
            if (wantTransfur) {
                this.inventory.selected = this.findSlotForTransfur();
            }

            if (!wantTransfur || this.inventory.selected == -1) { // No Free slot,
                this.inventory.selected = this.findSlotForCombat();
            }
        } else {
            if (getAttackCondition() == DarkLatexAttackCondition.ALWAYS) {
                this.inventory.selected = this.findSlotForCombat();
            }

            if (getAttackCondition() != DarkLatexAttackCondition.ALWAYS || this.inventory.selected == -1) {
                this.inventory.selected = this.findSlotForNonCombat();
            }
        }

        if (this.inventory.selected == -1) {
            this.inventory.selected = this.findSlotForNonCombat();
        }

        if (this.inventory.selected == -1) {
            this.inventory.selected = 0; // Fail :(
        }
    }
}
