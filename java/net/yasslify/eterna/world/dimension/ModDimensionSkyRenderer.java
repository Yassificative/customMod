package net.yasslify.eterna.world.dimension;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.yasslify.eterna.ClientConfig;
import net.yasslify.eterna.Eterna;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.function.BiFunction;

public class ModDimensionSkyRenderer {
    private final static SimpleWeightedRandomList<Color> starColors = SimpleWeightedRandomList.<Color>builder()
            .add(new Color((int) (220 * 0.9f + 40 * 0.1f), (int) (225 * 0.9f + 20 * 0.1f), (int) (240 * 0.9f + 80 * 0.1f)), 3)
            .add(new Color((int) (220 * 0.8f + 40 * 0.2f), (int) (225 * 0.8f + 20 * 0.2f), (int) (240 * 0.8f + 80 * 0.2f)), 3)
            .add(new Color((int) (220 * 0.7f + 40 * 0.3f), (int) (225 * 0.7f + 20 * 0.3f), (int) (240 * 0.7f + 80 * 0.3f)), 3)
            .add(new Color((int) (220 * 0.6f + 40 * 0.4f), (int) (225 * 0.6f + 20 * 0.4f), (int) (240 * 0.6f + 80 * 0.4f)), 3)
            .add(new Color((int) (220 * 0.5f + 40 * 0.5f), (int) (225 * 0.5f + 20 * 0.5f), (int) (240 * 0.5f + 80 * 0.5f)), 2)
            .add(new Color((int) (220 * 0.4f + 40 * 0.6f), (int) (225 * 0.4f + 20 * 0.6f), (int) (240 * 0.4f + 80 * 0.6f)), 1)
            .add(new Color((int) (220 * 0.3f + 40 * 0.7f), (int) (225 * 0.3f + 20 * 0.7f), (int) (240 * 0.3f + 80 * 0.7f)), 1)
            .add(new Color((int) (220 * 0.2f + 40 * 0.8f), (int) (225 * 0.2f + 20 * 0.8f), (int) (240 * 0.2f + 80 * 0.8f)), 1)
            .add(new Color((int) (220 * 0.1f + 40 * 0.9f), (int) (225 * 0.1f + 20 * 0.9f), (int) (240 * 0.1f + 80 * 0.9f)), 1)
            .build();
    private static java.util.List<SkyRenderable> skyRenderables;

    @Nullable
    private static VertexBuffer starBuffer;

    public static void renderSky(ClientLevel level, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        setupFog.run();
        if (!isFoggy) {
            FogType fogtype = camera.getFluidInCamera();
            if (fogtype != FogType.POWDER_SNOW && fogtype != FogType.LAVA && !doesMobEffectBlockSky(camera)) {
                if (starBuffer == null) createStars();
                renderSkyTexture(poseStack);
                if (ClientConfig.dimensionStars) renderStars(level, partialTick, poseStack, projectionMatrix, setupFog);
            }
        }
    }

    public static boolean doesMobEffectBlockSky(Camera pCamera) {
        Entity entity = pCamera.getEntity();
        if (!(entity instanceof LivingEntity livingentity)) {
            return false;
        } else {
            return livingentity.hasEffect(MobEffects.BLINDNESS) || livingentity.hasEffect(MobEffects.DARKNESS);
        }
    }

    private static void renderSkyTexture(PoseStack pPoseStack) {
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, new ResourceLocation(Eterna.MOD_ID, "textures/environment/end_sky.png"));
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        for(int i = 0; i < 6; ++i) {
            pPoseStack.pushPose();
            if (i == 1) pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            if (i == 2) pPoseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            if (i == 3) pPoseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            if (i == 4) pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            if (i == 5) pPoseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));

            Matrix4f matrix4f = pPoseStack.last().pose();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex(matrix4f, -100f, -100f, -100f).uv(0.0F, 0.0F).color(40, 40, 40, 255).endVertex();
            bufferbuilder.vertex(matrix4f, -100f, -100f, 100f).uv(0.0F, 16.0F).color(40, 40, 40, 255).endVertex();
            bufferbuilder.vertex(matrix4f, 100f, -100f, 100f).uv(16.0F, 16.0F).color(40, 40, 40, 255).endVertex();
            bufferbuilder.vertex(matrix4f, 100f, -100f, -100f).uv(16.0F, 0.0F).color(40, 40, 40, 255).endVertex();
            tesselator.end();
            pPoseStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    public static void renderSkyRenderable(BufferBuilder bufferBuilder, PoseStack poseStack, Vec3 localRotation, Vec3 globalRotation, float scale, ResourceLocation texture, boolean blend) {
        if (blend) RenderSystem.enableBlend();
        poseStack.pushPose();

        poseStack.mulPose(Axis.XP.rotationDegrees((float) globalRotation.x));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) globalRotation.y));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) globalRotation.z));

        poseStack.translate(0, 99, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) localRotation.x));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) localRotation.y));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) localRotation.z));
        poseStack.translate(0, -99, 0);

        var matrix = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, -scale, 99, -scale).uv(1, 0).endVertex();
        bufferBuilder.vertex(matrix, scale, 99, -scale).uv(0, 0).endVertex();
        bufferBuilder.vertex(matrix, scale, 99, scale).uv(0, 1).endVertex();
        bufferBuilder.vertex(matrix, -scale, 99, scale).uv(1, 1).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        poseStack.popPose();
        RenderSystem.disableBlend();
    }

    private static void setSkyRenderableColor(ClientLevel level, float partialTick, int color) {
        float r = FastColor.ARGB32.red(color) / 255f;
        float g = FastColor.ARGB32.green(color) / 255f;
        float b = FastColor.ARGB32.blue(color) / 255f;
        float a = 1;
        RenderSystem.setShaderColor(r, g, b, a);
    }

    private static void renderStars(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix, Runnable setupFog) {
        BiFunction<ClientLevel, Float, Float> starBrightness = (a,b) -> 0.9f;
        float starLight = starBrightness.apply(level, partialTick);
        if (starLight <= 0) return;
        if (starBuffer == null) return;
        RenderSystem.setShaderColor(starLight, starLight, starLight, starLight);
        FogRenderer.setupNoFog();
        starBuffer.bind();
        var shader = GameRenderer.getPositionColorShader();
        if (shader == null) return;
        starBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shader);
        VertexBuffer.unbind();
        setupFog.run();
    }

    private static void createStars() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        if (starBuffer != null) {
            starBuffer.close();
        }

        starBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder.RenderedBuffer renderedBuffer = drawStars(bufferBuilder);
        starBuffer.bind();
        starBuffer.upload(renderedBuffer);
        VertexBuffer.unbind();
    }

    private static BufferBuilder.RenderedBuffer drawStars(BufferBuilder builder) {
        var random = RandomSource.create(10842);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int starCount = 1024;
        for (int i = 0; i < starCount; i++) {
            double x = random.nextFloat() * 2 - 1;
            double y = random.nextFloat() * 2 - 1;
            double z = random.nextFloat() * 2 - 1;
            double scale = 0.12 + random.nextFloat() * 0.1;

            double distance = x * x + y * y + z * z;

            // ensure that the stars are within the sphere and not too close to the center
            if (distance >= 1 || distance <= 0.01) continue;

            distance = 1 / Math.sqrt(distance);
            x *= distance;
            y *= distance;
            z *= distance;

            double xScale = x * 100;
            double yScale = y * 100;
            double zScale = z * 100;

            double theta = Math.atan2(x, z);
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);

            double phi = Math.atan2(Math.sqrt(x * x + z * z), y);
            double sinPhi = Math.sin(phi);
            double cosPhi = Math.cos(phi);

            double rot = random.nextDouble() * Math.PI * 2;
            double sinRot = Math.sin(0);
            double cosRot = Math.cos(0);

            Color color = starColors.getRandomValue(random).orElse(new Color(220, 225, 240));

            for (int j = 0; j < 4; j++) {
                double xOffset = ((j & 2) - 1) * scale;
                double yOffset = ((j + 1 & 2) - 1) * scale;

                double rotatedX = xOffset * cosRot - yOffset * sinRot;
                double rotatedY = yOffset * cosRot + xOffset * sinRot;

                double transformedX = rotatedX * sinPhi;
                double transformedY = -rotatedX * cosPhi;

                builder.vertex(
                                xScale + transformedY * sinTheta - rotatedY * cosTheta,
                                yScale + transformedX,
                                zScale + rotatedY * sinTheta + transformedY * cosTheta)
                        .color(getIntFromColor(color.getRed(), color.getGreen(), color.getBlue()))
                        .endVertex();
            }
        }

        return builder.end();
    }

    public static int getIntFromColor(int Red, int Green, int Blue){
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }
}
