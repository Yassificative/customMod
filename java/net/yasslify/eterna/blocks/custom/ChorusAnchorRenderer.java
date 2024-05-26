package net.yasslify.eterna.blocks.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.yasslify.eterna.Eterna;
import net.yasslify.eterna.items.custom.Navigator;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class ChorusAnchorRenderer<T extends ChorusAnchorBlockEntity> implements BlockEntityRenderer<T> {
    private final Font font;
    protected final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    public static final ModelLayerLocation MAIN_LOCATION = new ModelLayerLocation(new ResourceLocation(Eterna.MOD_ID, "warp_anchor_blockentity"), "main");

    public ChorusAnchorRenderer(BlockEntityRendererProvider.Context pContext) {
        pContext.bakeLayer(MAIN_LOCATION);
        this.font = pContext.getFont();
        this.blockEntityRenderDispatcher = pContext.getBlockEntityRenderDispatcher();
    }

    public static LayerDefinition createMainLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void render(T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        BlockState blockState = pBlockEntity.getBlockState();
        if (blockState.getValue(ChorusAnchor.WAXED)) return;

        Material material = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(Eterna.MOD_ID, "block/chorus_anchor_light"));
        if (blockState.getValue(ChorusAnchor.LINKED)) {
            vertexWall(pPoseStack, material.buffer(pBuffer, RenderType::entityTranslucentEmissive), new Color(255, 255, 255, 16), 1, 1.375F, 0.125F, 0.875F, 0.875F, 0.875F, 0.125F, 0.125F, 0.875F, 0.125F, 0, 1, 0, 1, 240);
        }

        HitResult hitResult = this.blockEntityRenderDispatcher.cameraHitResult;
        if (!hitResult.getType().equals(HitResult.Type.ENTITY)) {
            if (pBlockEntity.isLinked() && ((BlockHitResult) hitResult).getBlockPos().equals(pBlockEntity.getBlockPos())) {
                ItemStack itemStack = pBlockEntity.getNavigator();
                String[] s = Navigator.getTarget(itemStack);
                if (itemStack.hasCustomHoverName()) {
                    String string = itemStack.getHoverName().getString().replace("@color=", "§");
                    boolean hidden = string.contains("@hideName");
                    boolean noBG = string.contains("@noBG");
                    if (!hidden) renderNameTag(this.font, this.blockEntityRenderDispatcher, Component.literal(string.replace("@hideName", "").replace("@noBG", "")), pPoseStack, pBuffer, pPackedLight, !noBG);
                } else renderNameTag(this.font, this.blockEntityRenderDispatcher, Component.literal("To: " + s[0] + " " + s[1] + " " + s[2]), pPoseStack, pBuffer, pPackedLight, true);
            }
        }
    }

    public void renderNameTag(Font pFont, BlockEntityRenderDispatcher pDispatcher, Component pDisplayName, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, boolean pBackground) {
        pPoseStack.pushPose();
        pPoseStack.translate(0.5F, 1.5F, 0.5F);
        pPoseStack.mulPose(pDispatcher.camera.rotation());
        pPoseStack.scale(-0.015F, -0.015F, 0.015F);
        Matrix4f matrix4f = pPoseStack.last().pose();
        float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int j = (int) (f1 * 255.0F) << 24;
        float f2 = (float) (-pFont.width(pDisplayName) / 2);
        if (pBackground) pFont.drawInBatch(pDisplayName, f2, 0, 553648127, false, matrix4f, pBuffer, Font.DisplayMode.SEE_THROUGH, j, 240);
        pFont.drawInBatch(pDisplayName, f2, 0, -1, false, matrix4f, pBuffer, Font.DisplayMode.NORMAL, 0, 240);
        pPoseStack.popPose();
    }

    private void vertexWall(PoseStack pPoseStack, VertexConsumer pConsumer, Color color, float pMinY, float pMaxY, float pX0, float pZ0, float pX1, float pZ1, float pX2, float pZ2, float pX3, float pZ3, float pMinU, float pMaxU, float pMinV, float pMaxV, int light) {
        vertexSide(pPoseStack, pConsumer, color, pMinY, pMaxY, pX0, pZ0, pX1, pZ1, pMinU, pMaxU, pMinV, pMaxV, light);
        vertexSide(pPoseStack, pConsumer, color, pMinY, pMaxY, pX3, pZ3, pX2, pZ2, pMinU, pMaxU, pMinV, pMaxV, light);
        vertexSide(pPoseStack, pConsumer, color, pMinY, pMaxY, pX1, pZ1, pX3, pZ3, pMinU, pMaxU, pMinV, pMaxV, light);
        vertexSide(pPoseStack, pConsumer, color, pMinY, pMaxY, pX2, pZ2, pX0, pZ0, pMinU, pMaxU, pMinV, pMaxV, light);
    }

    private void vertexSide(PoseStack pPoseStack, VertexConsumer pConsumer, Color pColor, float pMinY, float pMaxY, float pMinX, float pMinZ, float pMaxX, float pMaxZ, float pMinU, float pMaxU, float pMinV, float pMaxV, int light) {
        vertex(pPoseStack, pConsumer, pColor, pMinX, pMaxY, pMinZ, pMaxU, pMinV, light);
        vertex(pPoseStack, pConsumer, pColor, pMinX, pMinY, pMinZ, pMaxU, pMaxV, light);
        vertex(pPoseStack, pConsumer, pColor, pMaxX, pMinY, pMaxZ, pMinU, pMaxV, light);
        vertex(pPoseStack, pConsumer, pColor, pMaxX, pMaxY, pMaxZ, pMinU, pMinV, light);
    }

    private void vertex(PoseStack pPoseStack, VertexConsumer pConsumer, Color color, float pX, float pY, float pZ, float pU, float pV, int light) {
        Matrix4f matrix4f = pPoseStack.last().pose();
        Matrix3f matrix3f = pPoseStack.last().normal();
        pConsumer.vertex(matrix4f, pX, pY, pZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).uv(pU, pV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
    }
}
