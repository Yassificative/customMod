package net.yasslify.eterna.world.dimension;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.yasslify.eterna.ClientConfig;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

@OnlyIn(Dist.CLIENT)
public class ModDimensionEffects extends DimensionSpecialEffects {
    public static float cloudLevel = 192;
    public static boolean hasGround = false;
    public static SkyType skyType = SkyType.END;
    public static boolean forceBrightLightmap = true;
    public static boolean constantAmbientLight = false;

    public static void registerModDimensionEffects(BiConsumer<ResourceKey<Level>, ModDimensionEffects> consumer) {
        consumer.accept(ModDimension.INSIDETHEEYE_KEY, new ModDimensionEffects(cloudLevel, hasGround, skyType, forceBrightLightmap, constantAmbientLight));
    }

    public ModDimensionEffects(float pCloudLevel, boolean pHasGround, SkyType pSkyType, boolean pForceBrightLightmap, boolean pConstantAmbientLight) {
        super(pCloudLevel, pHasGround, pSkyType, pForceBrightLightmap, pConstantAmbientLight);
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        return level.getBiome(new BlockPos((int) camX, (int) camY, (int) camZ)).is(ModDimension.THE_START_KEY);
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        if (level.getBiome(camera.getBlockPosition()).is(ModDimension.THE_START_KEY) || !ClientConfig.dimensionOverworldEffects) {
            hasGround = false;
            skyType = SkyType.END;
            forceBrightLightmap = true;
            ModDimensionSkyRenderer.renderSky(level, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
        } else {
            hasGround = true;
            skyType = SkyType.NORMAL;
            forceBrightLightmap = false;
            ModDimensionOverworldSkyRenderer.renderSky(level, poseStack, projectionMatrix, partialTick, camera, isFoggy, setupFog);
        }
        return true;
    }

    @Override
    public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ) {
        return level.getBiome(new BlockPos((int) camX, (int) camY, (int) camZ)).is(ModDimension.THE_START_KEY);
    }

    @Override
    public boolean tickRain(ClientLevel level, int ticks, Camera camera) {
        return level.getBiome(camera.getBlockPosition()).is(ModDimension.THE_START_KEY);
    }

    private final float[] sunriseCol = new float[4];

    @Nullable
    public float[] getSunriseColor(float pTimeOfDay, float pPartialTicks) {
        if (skyType == SkyType.NORMAL) {
            float f1 = Mth.cos(pTimeOfDay * ((float)Math.PI * 2F)) - 0.0F;
            if (f1 >= -0.4F && f1 <= 0.4F) {
                float f3 = (f1 - -0.0F) / 0.4F * 0.5F + 0.5F;
                float f4 = 1.0F - (1.0F - Mth.sin(f3 * (float)Math.PI)) * 0.99F;
                f4 *= f4;
                this.sunriseCol[0] = f3 * 0.3F + 0.7F;
                this.sunriseCol[1] = f3 * f3 * 0.7F + 0.2F;
                this.sunriseCol[2] = f3 * f3 * 0.0F + 0.2F;
                this.sunriseCol[3] = f4;
                return this.sunriseCol;
            } else {
                return null;
            }
        } else return null;
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 pFogColor, float pBrightness) {
        return skyType == SkyType.END ? pFogColor.scale(0.15F) : pFogColor.multiply(pBrightness * 0.94F + 0.06F, pBrightness * 0.94F + 0.06F, pBrightness * 0.91F + 0.09F);
    }

    @Override
    public boolean isFoggyAt(int pX, int pY) {
        return false;
    }

    @Override
    public float getCloudHeight() {
        return cloudLevel;
    }

    @Override
    public boolean hasGround() {
        return hasGround;
    }

    @Override
    public DimensionSpecialEffects.SkyType skyType() {
        return skyType;
    }

    @Override
    public boolean forceBrightLightmap() {
        return forceBrightLightmap;
    }
}
