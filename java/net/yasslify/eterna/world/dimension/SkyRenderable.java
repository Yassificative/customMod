package net.yasslify.eterna.world.dimension;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.yasslify.eterna.Eterna;

public record SkyRenderable(
        ResourceLocation texture,
        float scale,
        Vec3 globalRotation,
        Vec3 localRotation,
        MovementType movementType,
        boolean blend,
        int backLightColor,
        float backLightScale) {

    public static SkyRenderable create(ResourceLocation texture, float scale, Vec3 globalRotation, Vec3 localRotation, MovementType movementType, int backLightColor) {
        return create(texture, scale, globalRotation, localRotation, movementType, false, backLightColor, scale * 3);
    }

    public static SkyRenderable create(ResourceLocation texture, float scale, Vec3 globalRotation, Vec3 localRotation, MovementType movementType, boolean blend, int backLightColor) {
        return create(texture, scale, globalRotation, localRotation, movementType, blend, backLightColor, scale * 3);
    }

    public static SkyRenderable create(ResourceLocation texture, float scale, Vec3 globalRotation, Vec3 localRotation, MovementType movementType, boolean blend, int backLightColor, float backLightScale) {
        return new SkyRenderable(texture, scale, globalRotation, localRotation, movementType, blend, backLightColor, backLightScale);
    }

    public enum MovementType {
        STATIC,
        TIME_OF_DAY,
        TIME_OF_DAY_REVERSED,
    }

    public static final ResourceLocation BACKLIGHT = new ResourceLocation(Eterna.MOD_ID, "textures/environment/backlight_opacity75.png");

    public static final ResourceLocation MOON = new ResourceLocation("textures/environment/moon_phases.png");
    public static final ResourceLocation SUN = new ResourceLocation("textures/environment/sun.png");
    public static final ResourceLocation EYE = new ResourceLocation(Eterna.MOD_ID, "textures/environment/eye_static.png");
    public static final Material EYE1 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(Eterna.MOD_ID, "block/eye_blink.png"));
}
