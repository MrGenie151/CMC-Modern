package net.ltxprogrammer.changed.client.renderer;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.client.renderer.model.BehemothHandLeftModel;
import net.ltxprogrammer.changed.entity.beast.boss.BehemothHandLeft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BehemothHandLeftRenderer extends MobRenderer<BehemothHandLeft, BehemothHandLeftModel> {
    public static final ResourceLocation DEFAULT_SKIN_LOCATION = Changed.modResource("textures/behemoth_hand.png");
    
    public BehemothHandLeftRenderer(EntityRendererProvider.Context context) {
        super(context, new BehemothHandLeftModel(context.bakeLayer(BehemothHandLeftModel.LAYER_LOCATION)), 0.8f);
    }

    @Override
    public ResourceLocation getTextureLocation(BehemothHandLeft entity) {
        return DEFAULT_SKIN_LOCATION;
    }
}
