package net.yasslify.eterna.world.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.yasslify.eterna.Eterna;

public class ModDimension {
    public static final ResourceKey<Level> INSIDETHEEYE_KEY =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(Eterna.MOD_ID, "sub_end"));
    public static final ResourceKey<DimensionType> INSIDETHEEYE_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, INSIDETHEEYE_KEY.location());

    public static final ResourceKey<Biome> THE_START_KEY =
            ResourceKey.create(Registries.BIOME, new ResourceLocation(Eterna.MOD_ID, "sub_space"));
    public static void register() {
        System.out.println("Registering ModDimension for " + Eterna.MOD_ID);
    }
}
