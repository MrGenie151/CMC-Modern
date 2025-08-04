package net.ltxprogrammer.changed.network.syncher;

import net.ltxprogrammer.changed.entity.BasicPlayerInfo;
import net.ltxprogrammer.changed.entity.decoration.WallSignVariant;
import net.ltxprogrammer.changed.init.ChangedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.decoration.PaintingVariant;

public class ChangedEntityDataSerializers {
    public static final EntityDataSerializer<BasicPlayerInfo> BASIC_PLAYER_INFO = new EntityDataSerializer<BasicPlayerInfo>() {
        public void write(FriendlyByteBuf buffer, BasicPlayerInfo info) {
            var tag = new CompoundTag();
            info.save(tag);
            buffer.writeNbt(tag);
        }

        public BasicPlayerInfo read(FriendlyByteBuf buffer) {
            BasicPlayerInfo info = new BasicPlayerInfo();
            info.load(buffer.readNbt());
            return info;
        }

        public BasicPlayerInfo copy(BasicPlayerInfo info) {
            BasicPlayerInfo newInfo = new BasicPlayerInfo();
            newInfo.copyFrom(info);
            return newInfo;
        }
    };

    public static final EntityDataSerializer<WallSignVariant> WALL_SIGN_VARIANT = EntityDataSerializer.simpleId(ChangedRegistry.WALL_SIGN_VARIANT.asIdMap());

    static {
        EntityDataSerializers.registerSerializer(BASIC_PLAYER_INFO);
        EntityDataSerializers.registerSerializer(WALL_SIGN_VARIANT);
    }
}
