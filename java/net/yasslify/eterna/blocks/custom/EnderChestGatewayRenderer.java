package net.yasslify.eterna.blocks.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.yasslify.eterna.ClientConfig;
import net.yasslify.eterna.Eterna;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class EnderChestGatewayRenderer<T extends EnderChestGatewayBlockEntity> implements BlockEntityRenderer<T> {
    private final ModelPart lid;
    private final ModelPart lock;
    private final ModelPart bottom;

    public static final ModelLayerLocation MAIN_LOCATION = new ModelLayerLocation(new ResourceLocation(Eterna.MOD_ID, "ender_chest_gateway_blockentity"), "main");

    public EnderChestGatewayRenderer(BlockEntityRendererProvider.Context pContext) {
        ModelPart modelPart = pContext.bakeLayer(MAIN_LOCATION);
        this.lid = modelPart.getChild("lid");
        this.lock = modelPart.getChild("lock");
        this.bottom = modelPart.getChild("bottom");
    }

    public static LayerDefinition createMainLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(1, 9, 1, 14, 5, 14), PartPose.ZERO);
        partdefinition.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(7, 7, 15, 2, 4, 1), PartPose.ZERO);
        partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1, 0, 1, 14, 10, 14), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void render(T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        float facing = pBlockEntity.getBlockState().getValue(EnderChestGateway.FACING).toYRot();
        boolean isOpen = pBlockEntity.isOpen;
        boolean isUsing = pBlockEntity.isUsing;
        boolean isFueling = pBlockEntity.isFueling;
        boolean emissive = ClientConfig.emissiveEnderChestGatewayTexture;
        boolean renderRune = ClientConfig.renderEnderChestGatewayRune;
        boolean renderBeam = ClientConfig.renderEnderChestGatewayBeam;
        boolean renderPortal = ClientConfig.renderEnderChestGatewayPortal;
        boolean renderAura = ClientConfig.renderEnderChestGatewayAura;

        if (isUsing && renderBeam) renderBeaconBeam(pBlockEntity, pPartialTick, pPoseStack, pBuffer);

        pPoseStack.pushPose();
        pPoseStack.rotateAround(Axis.YP.rotationDegrees(-facing), 0.5F, 0.5F, 0.5F);

        int blinkStage = switch (pBlockEntity.circleTick) { case 175, 179, 355, 359 -> 1; case 176, 178, 356, 358 -> 2; case 177, 357 -> 3; default -> 0; };
        if (renderPortal) vertexTop(pPoseStack, pBuffer.getBuffer(RenderType.endPortal()), Color.WHITE, 0.1875F, 0.8125F, 0.5625F, 0.1875F, 0.8125F, 0, 0, 0, 0, 0);

        this.bottom.render(pPoseStack, chestConsumer(isUsing, "main", pBuffer), pPackedLight, pPackedOverlay);
        this.bottom.render(pPoseStack, chestConsumer(isUsing, "overlay" + blinkStage, pBuffer), emissive ? Math.min(pPackedLight + (3 - blinkStage) * 24, LightTexture.FULL_BRIGHT) : pPackedLight, pPackedOverlay);
        vertexWall(pPoseStack, chestConsumer(isUsing, "main", pBuffer), Color.WHITE, 0.125F, 0.625F, 0.1875F, 0.1875F, 0.8125f, 0.1875F, 0.1875F, 0.8125f, 0.8125f, 0.8125f, 0, 0.15625f, 0.828125f, 0.953125f, pPackedLight);
        vertexTop(pPoseStack, chestConsumer(isUsing, "main", pBuffer), Color.WHITE, 0.1875F, 0.8125f, 0.125f, 0.1875F, 0.8125f, 0, 0.15625f, 0.671875F, 0.828125f, pPackedLight);

        float f = isOpen ? isUsing || isFueling ?
                isUsing ? (pBlockEntity.openStage + 1) * 10 - (pBlockEntity.usingTick + pPartialTick) / pBlockEntity.maxUsingTick * 10
                        : (pBlockEntity.openStage - 1) * 10 + (pBlockEntity.usingTick + pPartialTick) / 2
                : pBlockEntity.openStage * 10 : 90 * (float) Math.pow((Math.sin((pBlockEntity.usingTick + pPartialTick) * Math.PI / 40)), 1.25);

        pPoseStack.rotateAround(new Quaternionf().rotateX((float) Math.toRadians(-f * (1 + 0.0125F * Math.sin(pBlockEntity.circleTick * Math.PI / 60)))), 0F, 0.5625F, 0.0625F);

        this.lid.render(pPoseStack, chestConsumer(isUsing, "main", pBuffer), pPackedLight, pPackedOverlay);
        this.lid.render(pPoseStack, chestConsumer(isUsing, "overlay0", pBuffer), emissive ? 240 : pPackedLight, pPackedOverlay);
        this.lock.render(pPoseStack, chestConsumer(isUsing, "main", pBuffer), pPackedLight, pPackedOverlay);
        if (renderRune) vertexTop(pPoseStack, new Material(Sheets.CHEST_SHEET, new ResourceLocation(Eterna.MOD_ID, "entity/chest/sga_alphabet")).buffer(pBuffer, RenderType::entityCutout), new Color(130, 160, 150), 0.34375f, 0.65625F, 0.56125F, 0.71875F, 0.28125F, 0, 1, 0, 1, 240);

        pPoseStack.popPose();

        if (isUsing && renderAura) renderAura(pBlockEntity, pPoseStack, pBuffer, pPartialTick);
    }

    private VertexConsumer chestConsumer(boolean isUsing, String pString, MultiBufferSource pBuffer) {
        return isUsing ? pBuffer.getBuffer(RenderType.eyes(new ResourceLocation(Eterna.MOD_ID, "textures/entity/chest/" + pString + ".png"))) : new Material(Sheets.CHEST_SHEET, new ResourceLocation(Eterna.MOD_ID, "entity/chest/" + pString)).buffer(pBuffer, RenderType::entityCutout);
    }

    private void renderAura(T pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick) {
        pPoseStack.pushPose();

        float f = pBlockEntity.usingTick + pPartialTick;

        pPoseStack.translate(0.5f, 0, 0.5f);
        pPoseStack.mulPose(new Quaternionf().scale(pBlockEntity.maxUsingTick - pBlockEntity.usingTick <= 6 ? 1 - (f / pBlockEntity.maxUsingTick) - (float) (Math.cos((pBlockEntity.maxUsingTick - f) * Math.PI * (0.5f / 6))) : 1));
        pPoseStack.translate(-0.5f, 0, -0.5f);
        pPoseStack.rotateAround(Axis.YP.rotationDegrees(-pBlockEntity.getBlockState().getValue(EnderChestGateway.FACING).toYRot()), 0.5F, 0.5F, 0.5F);

        int c = pBlockEntity.usingTick <= 6 ? pBlockEntity.usingTick * 40 :
                pBlockEntity.maxUsingTick - pBlockEntity.usingTick <= 6 ? (pBlockEntity.maxUsingTick - pBlockEntity.usingTick) * 40 : 240;

        float uv = 0.3F + 0.0001f * f;

        vertexTop(pPoseStack, pBuffer.getBuffer(RenderType.eyes(new ResourceLocation(Eterna.MOD_ID, "textures/environment/end_portal/portal.png"))), new Color(c, c, c), -4, 5, 0.001F, -4, 5, uv, 0.1F + uv, uv, 0.1F + uv, 240);

        pPoseStack.popPose();

        for (int i = 0; i < pBlockEntity.maxUsingTick + 10; i = i + 2) {
            pPoseStack.pushPose();

            float f1 = pBlockEntity.usingTick < i ? 0 : pBlockEntity.usingTick + pPartialTick - i;

            pPoseStack.translate(0.5f, 0, 0.5f);
            pPoseStack.mulPose(new Quaternionf().scale(pBlockEntity.maxUsingTick - pBlockEntity.usingTick <= 6 ? 1 - (f1 / pBlockEntity.maxUsingTick) - (float) (Math.cos((pBlockEntity.maxUsingTick - f) * Math.PI * (0.5f / 6))) : 1 - (f1 / pBlockEntity.maxUsingTick)));
            pPoseStack.translate(-0.5f, pBlockEntity.usingTick < 4 ? 0.1F * (4 - f) : Math.sin(0.0125f * f1 * Math.PI), -0.5f);
            pPoseStack.rotateAround(Axis.YP.rotationDegrees(-pBlockEntity.getBlockState().getValue(EnderChestGateway.FACING).toYRot()), 0.5F, 0.5F, 0.5F);
            pPoseStack.rotateAround(Axis.YP.rotationDegrees(pBlockEntity.maxUsingTick - pBlockEntity.usingTick <= 6 ? f1 - (6 - (pBlockEntity.maxUsingTick - f)) * 20 : f1), 0.5F, 0.5F, 0.5F);

            int c1 = pBlockEntity.usingTick <= 6 ? pBlockEntity.usingTick * 40 :
                    pBlockEntity.maxUsingTick - pBlockEntity.usingTick <= 6 ? (pBlockEntity.maxUsingTick - pBlockEntity.usingTick) * 20 : (int) Math.max(Math.min((pBlockEntity.maxUsingTick - f1) * 2.5, 240), 0);

            vertexTop(pPoseStack, pBuffer.getBuffer(RenderType.eyes(new ResourceLocation(Eterna.MOD_ID, "textures/environment/end_portal/portal_wave3.png"))), new Color(c1, c1, c1), -4, 5, 0.001F, -4, 5, 0, 1, 0, 1, 240);

            pPoseStack.popPose();
        }
    }

    private void renderBeaconBeam(T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer) {
        pPoseStack.pushPose();
        pPoseStack.translate(0.5F, 0.5625F, 0.5F);
        float f = (float)Math.floorMod(pBlockEntity.getLevel().getGameTime(), 40) + pPartialTick;
        float f1 = Mth.frac(f * 0.2F - (float)Mth.floor(f * 0.1F));
        float f2 = -1.0F + f1;
        float f3 = f2 / 20;

        float radius;
        float height = (pBlockEntity.usingTick + pPartialTick) * pBlockEntity.usingTick * pBlockEntity.usingTick;
        if (pBlockEntity.usingTick >= pBlockEntity.maxUsingTick - 4) radius = 0.0625F * (pBlockEntity.maxUsingTick - pBlockEntity.usingTick - pPartialTick);
        else radius = 0.3125F - (pBlockEntity.usingTick + pPartialTick) * 0.0625F / pBlockEntity.maxUsingTick;
        float radiusInner = radius - 0.00125F;
        vertexWall(pPoseStack, pBuffer.getBuffer(RenderType.endGateway()), Color.black, 0, height, -radius, radius, radius, radius, -radius, -radius, radius, -radius, 0.0F, 1.0F, f2, f3, 240);
        vertexWall(pPoseStack, pBuffer.getBuffer(RenderType.eyes(new ResourceLocation(Eterna.MOD_ID, "textures/environment/end_portal/portal3.png"))), new Color(255, 255, 255), 0, height,
                -radiusInner, radiusInner, radiusInner, radiusInner, -radiusInner, -radiusInner, radiusInner, -radiusInner, 0.0F, 1.0F, f2, f3, 240);
        pPoseStack.popPose();
    }

    private void renderBeam(T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer) {
        if (!pBlockEntity.EntitiesInRadius.isEmpty()) {
            for (Entity entity : pBlockEntity.EntitiesInRadius) {
                if (entity != null && entity.canChangeDimensions()) {
                    pPoseStack.pushPose();
                    pPoseStack.translate(0.5F, 0.5F, 0.5F);
                    Vec3 vec3 = getPosition(entity, entity.getEyeHeight() * 0.5D + 0.125D, pPartialTick).subtract(pBlockEntity.getBlockPos().getCenter());
                    float f = (float) (vec3.length());
                    float f1 = (float) Math.atan2(vec3.normalize().z, vec3.normalize().x);
                    pPoseStack.mulPose(Axis.YP.rotationDegrees((((float) Math.PI / 2F) - f1) * (180F / (float) Math.PI)));
                    pPoseStack.mulPose(Axis.XP.rotationDegrees((float) Math.acos(vec3.normalize().y) * (180F / (float) Math.PI)));
                    float f2 = (float) pBlockEntity.usingTick * 0.05F * -1.5F;
                    float f3 = Mth.cos(f2 + (float) Math.PI) * 0.2F;
                    float f4 = Mth.sin(f2 + (float) Math.PI) * 0.2F;
                    float f5 = Mth.cos(f2) * 0.2F;
                    float f6 = Mth.sin(f2) * 0.2F;
                    float f7 = Mth.cos(f2 + ((float) Math.PI / 2F)) * 0.2F;
                    float f8 = Mth.sin(f2 + ((float) Math.PI / 2F)) * 0.2F;
                    float f9 = Mth.cos(f2 + ((float) Math.PI * 1.5F)) * 0.2F;
                    float f10 = Mth.sin(f2 + ((float) Math.PI * 1.5F)) * 0.2F;
                    float f11 = 1F - (pBlockEntity.usingTick + pPartialTick) * 0.2F;

                    float f12 = f11 - 2.5f * f;

                    VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityTranslucent(new ResourceLocation("textures/entity/guardian_beam.png")));
                    Color color = pBlockEntity.usingTick * 5 < 200 ? new Color(15 + pBlockEntity.usingTick * 5, 40 + pBlockEntity.usingTick * 5, 40 + pBlockEntity.usingTick * 5, 255) : new Color(240, 255, 255, 128);

                    //float radius = 0.0625f / 1.25F;
                    //float outerRadius = 1.5f * radius;
                    //renderPart(pPoseStack, pBuffer.getBuffer(RenderType.endGateway()), new Color(255,255,255,255), 0, f, -radius, radius, radius, radius, -radius, -radius, radius, -radius, 0, 1, f11, f12);
                    //renderPart(pPoseStack, pBuffer.getBuffer(glowTransparent(PORTAL_TEXTURE)), new Color(255,255,255,255), 0, f, -outerRadius, outerRadius, outerRadius, outerRadius, -outerRadius, -outerRadius, outerRadius, -outerRadius, 0, 1, f11, f12);
                    vertex(pPoseStack, vertexconsumer, color, f3, f, f4, 0.5F, f12, 240);
                    vertex(pPoseStack, vertexconsumer, color, f3, 0, f4, 0.5F, f11, 240);
                    vertex(pPoseStack, vertexconsumer, color, f5, 0, f6, 0.0F, f11, 240);
                    vertex(pPoseStack, vertexconsumer, color, f5, f, f6, 0.0F, f12, 240);
                    vertex(pPoseStack, vertexconsumer, color, f7, f, f8, 0.5F, f12, 240);
                    vertex(pPoseStack, vertexconsumer, color, f7, 0, f8, 0.5F, f11, 240);
                    vertex(pPoseStack, vertexconsumer, color, f9, 0, f10, 0.0F, f11, 240);
                    vertex(pPoseStack, vertexconsumer, color, f9, f, f10, 0.0F, f12, 240);
                    pPoseStack.popPose();
                }
            }
        }
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

    private void vertexTop(PoseStack pPoseStack, VertexConsumer pConsumer, Color pColor, float pMinX, float pMaxX, float pY, float pMinZ, float pMaxZ, float pMinU, float pMaxU, float pMinV, float pMaxV, int pPackedLight) {
        vertex(pPoseStack, pConsumer, pColor, pMinX, pY, pMinZ, pMaxU, pMinV, pPackedLight);
        vertex(pPoseStack, pConsumer, pColor, pMinX, pY, pMaxZ, pMaxU, pMaxV, pPackedLight);
        vertex(pPoseStack, pConsumer, pColor, pMaxX, pY, pMaxZ, pMinU, pMaxV, pPackedLight);
        vertex(pPoseStack, pConsumer, pColor, pMaxX, pY, pMinZ, pMinU, pMinV, pPackedLight);
    }

    private void vertex(PoseStack pPoseStack, VertexConsumer pConsumer, Color color, float pX, float pY, float pZ, float pU, float pV, int light) {
        Matrix4f matrix4f = pPoseStack.last().pose();
        Matrix3f matrix3f = pPoseStack.last().normal();
        pConsumer.vertex(matrix4f, pX, pY, pZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).uv(pU, pV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
    }

    private Vec3 getPosition(Entity pEntity, double pYOffset, float pPartialTick) {
        double d0 = Mth.lerp(pPartialTick, pEntity.xOld, pEntity.getX());
        double d1 = Mth.lerp(pPartialTick, pEntity.yOld, pEntity.getY()) + pYOffset;
        double d2 = Mth.lerp(pPartialTick, pEntity.zOld, pEntity.getZ());
        return new Vec3(d0, d1, d2);
    }
}
