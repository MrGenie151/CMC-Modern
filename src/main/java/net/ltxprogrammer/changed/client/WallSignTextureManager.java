package net.ltxprogrammer.changed.client;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.entity.decoration.WallSignVariant;
import net.ltxprogrammer.changed.init.ChangedRegistry;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;

public class WallSignTextureManager extends TextureAtlasHolder {
    private static final ResourceLocation BACK_SPRITE_LOCATION = Changed.modResource("back");

    public WallSignTextureManager(TextureManager textureManager) {
        super(textureManager, Changed.modResource("textures/atlas/wall_signs.png"), Changed.modResource("wall_signs"));
    }

    public TextureAtlasSprite get(WallSignVariant variant) {
        return this.getSprite(ChangedRegistry.WALL_SIGN_VARIANT.getKey(variant));
    }

    public TextureAtlasSprite getBackSprite() {
        return this.getSprite(BACK_SPRITE_LOCATION);
    }
}
