package net.ltxprogrammer.changed.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.ltxprogrammer.changed.client.ChangedClient;
import net.ltxprogrammer.changed.client.WallSignTextureManager;
import net.ltxprogrammer.changed.entity.decoration.WallSign;
import net.ltxprogrammer.changed.entity.decoration.WallSignVariant;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class WallSignRenderer extends EntityRenderer<WallSign> {
    public WallSignRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(WallSign wallSign, float yRot, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yRot));
        WallSignVariant variant = wallSign.getVariant();
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entitySolid(this.getTextureLocation(wallSign)));
        WallSignTextureManager textureManager = ChangedClient.wallSigns.get();
        this.renderSign(poseStack, vertexconsumer, wallSign, variant.getWidth(), variant.getHeight(), textureManager.get(variant), textureManager.getBackSprite());
        poseStack.popPose();
        super.render(wallSign, yRot, partialTicks, poseStack, bufferSource, packedLight);
    }

    public ResourceLocation getTextureLocation(WallSign wallSign) {
        return ChangedClient.wallSigns.get().getBackSprite().atlasLocation();
    }

    private void renderSign(PoseStack p_115559_, VertexConsumer buffer, WallSign wallSign, int width, int height, TextureAtlasSprite front, TextureAtlasSprite back) {
        PoseStack.Pose posestack$pose = p_115559_.last();
        Matrix4f pose = posestack$pose.pose();
        Matrix3f normal = posestack$pose.normal();
        float f = (float)(-width) / 2.0F;
        float f1 = (float)(-height) / 2.0F;
        float f2 = 0.5F;
        float f3 = back.getU0();
        float f4 = back.getU1();
        float f5 = back.getV0();
        float f6 = back.getV1();
        float f7 = back.getU0();
        float f8 = back.getU1();
        float f9 = back.getV0();
        float f10 = back.getV(1.0D);
        float f11 = back.getU0();
        float f12 = back.getU(1.0D);
        float f13 = back.getV0();
        float f14 = back.getV1();
        int blockWidth = width / 16;//((width / 16) - 1) * 2 + 1;
        int blockHeight = height / 16;//((height / 16) - 1) * 2 + 1;
        float blockRemainderWidth = (width / 16.0f) - blockWidth;
        float blockRemainderHeight = (height / 16.0f) - blockHeight;
        double sectionWidth = 16.0D / (width / 16.0f);
        double sectionHeight = 16.0D / (height / 16.0f);

        this.vertex(pose, normal, buffer, f, f1 + height, front.getU1(), front.getV0(), -0.5F, 0, 0, -1, LightTexture.FULL_BRIGHT);
        this.vertex(pose, normal, buffer, f + width, f1 + height, front.getU0(), front.getV0(), -0.5F, 0, 0, -1, LightTexture.FULL_BRIGHT);
        this.vertex(pose, normal, buffer, f + width, f1, front.getU0(), front.getV1(), -0.5F, 0, 0, -1, LightTexture.FULL_BRIGHT);
        this.vertex(pose, normal, buffer, f, f1, front.getU1(), front.getV1(), -0.5F, 0, 0, -1, LightTexture.FULL_BRIGHT);

        for(int blockX = 0; blockX < blockWidth; ++blockX) {
            for(int blockY = 0; blockY < blockHeight; ++blockY) {
                float f15 = f + (float)((blockX + 1) * 16);
                float f16 = f + (float)(blockX * 16);
                float f17 = f1 + (float)((blockY + 1) * 16);
                float f18 = f1 + (float)(blockY * 16);
                int i1 = wallSign.getBlockX();
                int j1 = Mth.floor(wallSign.getY() + (double)((f17 + f18) / 2.0F / 16.0F));
                int k1 = wallSign.getBlockZ();
                Direction direction = wallSign.getDirection();
                if (direction == Direction.NORTH) {
                    i1 = Mth.floor(wallSign.getX() + (double)((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.WEST) {
                    k1 = Mth.floor(wallSign.getZ() - (double)((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.SOUTH) {
                    i1 = Mth.floor(wallSign.getX() - (double)((f15 + f16) / 2.0F / 16.0F));
                }

                if (direction == Direction.EAST) {
                    k1 = Mth.floor(wallSign.getZ() + (double)((f15 + f16) / 2.0F / 16.0F));
                }

                int l1 = LevelRenderer.getLightColor(wallSign.level(), new BlockPos(i1, j1, k1));
                float frontU0 = front.getU(sectionWidth * (double)(blockWidth - blockX));
                float frontU1 = front.getU(sectionWidth * (double)(blockWidth - (blockX + 1)));
                float frontV0 = front.getV(sectionHeight * (double)(blockHeight - blockY));
                float frontV1 = front.getV(sectionHeight * (double)(blockHeight - (blockY + 1)));
                //this.vertex(pose, normal, buffer, f15, f18, frontU1, frontV0, -0.5F, 0, 0, -1, l1);
                //this.vertex(pose, normal, buffer, f16, f18, frontU0, frontV0, -0.5F, 0, 0, -1, l1);
                //this.vertex(pose, normal, buffer, f16, f17, frontU0, frontV1, -0.5F, 0, 0, -1, l1);
                //this.vertex(pose, normal, buffer, f15, f17, frontU1, frontV1, -0.5F, 0, 0, -1, l1);

                this.vertex(pose, normal, buffer, f15, f17, f4, f5, 0.5F, 0, 0, 1, l1);
                this.vertex(pose, normal, buffer, f16, f17, f3, f5, 0.5F, 0, 0, 1, l1);
                this.vertex(pose, normal, buffer, f16, f18, f3, f6, 0.5F, 0, 0, 1, l1);
                this.vertex(pose, normal, buffer, f15, f18, f4, f6, 0.5F, 0, 0, 1, l1);

                this.vertex(pose, normal, buffer, f15, f17, f7, f9, -0.5F, 0, 1, 0, l1);
                this.vertex(pose, normal, buffer, f16, f17, f8, f9, -0.5F, 0, 1, 0, l1);
                this.vertex(pose, normal, buffer, f16, f17, f8, f10, 0.5F, 0, 1, 0, l1);
                this.vertex(pose, normal, buffer, f15, f17, f7, f10, 0.5F, 0, 1, 0, l1);

                this.vertex(pose, normal, buffer, f15, f18, f7, f9, 0.5F, 0, -1, 0, l1);
                this.vertex(pose, normal, buffer, f16, f18, f8, f9, 0.5F, 0, -1, 0, l1);
                this.vertex(pose, normal, buffer, f16, f18, f8, f10, -0.5F, 0, -1, 0, l1);
                this.vertex(pose, normal, buffer, f15, f18, f7, f10, -0.5F, 0, -1, 0, l1);

                this.vertex(pose, normal, buffer, f15, f17, f12, f13, 0.5F, -1, 0, 0, l1);
                this.vertex(pose, normal, buffer, f15, f18, f12, f14, 0.5F, -1, 0, 0, l1);
                this.vertex(pose, normal, buffer, f15, f18, f11, f14, -0.5F, -1, 0, 0, l1);
                this.vertex(pose, normal, buffer, f15, f17, f11, f13, -0.5F, -1, 0, 0, l1);
                this.vertex(pose, normal, buffer, f16, f17, f12, f13, -0.5F, 1, 0, 0, l1);
                this.vertex(pose, normal, buffer, f16, f18, f12, f14, -0.5F, 1, 0, 0, l1);
                this.vertex(pose, normal, buffer, f16, f18, f11, f14, 0.5F, 1, 0, 0, l1);
                this.vertex(pose, normal, buffer, f16, f17, f11, f13, 0.5F, 1, 0, 0, l1);
            }
        }

    }

    private void vertex(Matrix4f pose, Matrix3f normal, VertexConsumer p_254114_, float x, float y, float u, float v, float z, int normalX, int normalY, int normalZ, int packedLight) {
        p_254114_.vertex(pose, x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, (float)normalX, (float)normalY, (float)normalZ).endVertex();
    }
}
