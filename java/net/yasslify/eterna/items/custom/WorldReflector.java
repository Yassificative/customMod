package net.yasslify.eterna.items.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.yasslify.eterna.ModUtils;
import net.yasslify.eterna.items.ModItems;
import net.yasslify.eterna.world.dimension.ModDimension;

import javax.annotation.Nullable;
import java.util.List;

public class WorldReflector extends Item {
    public WorldReflector() {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if (!hasBiome(stack)) {
            if (pLevel.dimension() == Level.OVERWORLD || pLevel.dimension() == ModDimension.INSIDETHEEYE_KEY) {
                String registryName = pLevel.getBiome(pPlayer.blockPosition()).unwrapKey().get().registry().toString();
                String location = pLevel.getBiome(pPlayer.blockPosition()).unwrapKey().get().location().toString();
                stack.getOrCreateTag().putString("biome", registryName + " " + location);
                return InteractionResultHolder.success(stack);
            } else return InteractionResultHolder.fail(stack);
        } else {
            if (pPlayer.isCrouching()) {
                stack.getOrCreateTag().remove("biome");
            } else {
                if (pLevel instanceof ServerLevel serverLevel) {
                    String[] s = WorldReflector.getBiome(stack);
                    ResourceKey<Biome> biome = ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation(s[0])), new ResourceLocation(s[1]));
                    if (!serverLevel.getBiome(pPlayer.blockPosition()).is(biome)) {
                        Holder<Biome> biomeHolder = serverLevel.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(biome);
                        ChunkAccess chunkAccess = serverLevel.getChunk(pPlayer.blockPosition());
                        LevelChunkSection[] sections = serverLevel.getChunk(pPlayer.blockPosition()).getSections();
                        //for (LevelChunkSection section : sections) {
                        //    PalettedContainer<Holder<Biome>> container = section.getBiomes().recreate();
                        //    BiomeResolver biomeResolver = (p_262550_, p_262551_, p_262552_, p_262553_) -> biomeHolder;
                        //    for (int j = 0; j < 4; ++j) {
                        //        for (int k = 0; k < 4; ++k) {
                        //            for (int l = 0; l < 4; ++l) {
                        //                container.getAndSetUnchecked(j, k, l, biomeResolver.getNoiseBiome(j, k, l, Climate.empty()));
                        //            }
                        //        }
                        //    }
                        //}
                        BiomeResolver biomeResolver = (p_262550_, p_262551_, p_262552_, p_262553_) -> biomeHolder;
                        chunkAccess.fillBiomesFromNoise(biomeResolver, Climate.empty());
                        chunkAccess.setUnsaved(true);
                        serverLevel.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunkAccess));
                    }
                }
            }
            return InteractionResultHolder.success(stack);
        }
    }

    public boolean isFoil(ItemStack pStack) {
        return pStack.getOrCreateTag().get("biome") != null;
    }

    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pEntity instanceof Player player) {
            if (player.getMainHandItem() == pStack || player.getOffhandItem() == pStack) {
                String biome = hasBiome(pStack) ? getBiome(pStack)[1] : "none";
                ModUtils.actionBarMsg(player, "§bBiome Reflected: " + biome);
            }
        }
    }

    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (hasBiome(pStack)) {
            pTooltipComponents.add(Component.literal("§4Bounded with: §b" + getBiome(pStack)[1]));
        }
        pTooltipComponents.add(Component.literal("§7Right click to reflect current biome's reality, can be used with " + ModUtils.name(ModItems.WORLD_FORGE.get()) + " §7to change biomes in §6\"InsideTheEye\" §7dimension."));
        if (!hasBiome(pStack)) {
            pTooltipComponents.add(Component.literal("§cCan only reflect ONCE!"));
        }
    }

    public static boolean hasBiome(ItemStack pItemStack) {
        return pItemStack.getOrCreateTag().get("biome") != null;
    }

    public static String[] getBiome(ItemStack pItemStack) {
        return pItemStack.getOrCreateTag().getString("biome").split(" ");
    }
}
