package net.yasslify.eterna.items.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.*;
import net.yasslify.eterna.ModUtils;
import net.yasslify.eterna.items.ModItems;
import net.yasslify.eterna.world.dimension.ModDimension;

import javax.annotation.Nullable;
import java.util.List;

public class WorldForge extends Item {
    public WorldForge() {
        super(new Properties().durability(256).fireResistant().rarity(Rarity.EPIC));
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack worldForger = pPlayer.getItemInHand(pUsedHand);
        if (pLevel.getServer() != null) {
            ResourceKey<Level> dimension = pLevel.dimension();
            ServerLevel serverLevel = pLevel.getServer().getLevel(dimension);
            if (serverLevel != null && dimension == ModDimension.INSIDETHEEYE_KEY) {
                ItemStack offhandItem = pPlayer.getOffhandItem();
                if (offhandItem.getItem() == ModItems.WORLD_REFLECTOR.get()) {
                    if (WorldReflector.hasBiome(offhandItem)) {
                        String[] s = WorldReflector.getBiome(offhandItem);
                        ResourceKey<Biome> biome = ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation(s[0])), new ResourceLocation(s[1]));
                        if (!serverLevel.getBiome(pPlayer.blockPosition()).is(biome)) {
                            Holder<Biome> biomeHolder = serverLevel.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(biome);
                            ChunkAccess chunkAccess = serverLevel.getChunk(pPlayer.blockPosition());
                            LevelChunkSection[] sections = serverLevel.getChunk(pPlayer.blockPosition()).getSections();
                            for (LevelChunkSection section : sections) {
                                PalettedContainer<Holder<Biome>> container = section.getBiomes().recreate();
                                for (int j = 0; j < 4; ++j) {
                                    for (int k = 0; k < 4; ++k) {
                                        for (int l = 0; l < 4; ++l) {
                                            container.getAndSetUnchecked(j, k, l, biomeHolder);
                                        }
                                    }
                                }
                            }
                            //chunkAccess.fillBiomesFromNoise(makeResolver(biomeHolder), serverLevel.getChunkSource().randomState().sampler());
                            chunkAccess.setUnsaved(true);
                            serverLevel.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunkAccess));
                            worldForger.hurtAndBreak(32, pPlayer, e -> e.broadcastBreakEvent(pUsedHand));
                            pPlayer.displayClientMessage(Component.literal("§7Chunk §6" + chunkAccess.getPos() + " §7has been forged into §6\"" + s[1] + "\"§7. \n" + ModUtils.name(ModItems.WORLD_FORGE.get()) + " §7- §6[" + (worldForger.getMaxDamage() - worldForger.getDamageValue()) + "] §7uses left."), false);
                            return InteractionResultHolder.success(worldForger);
                        } else {
                            pPlayer.displayClientMessage(Component.literal("§7Current biome is the same as the biome reflected by " + ModUtils.name(ModItems.WORLD_REFLECTOR.get())), false);
                            return InteractionResultHolder.fail(worldForger);
                        }
                    } else {
                        pPlayer.displayClientMessage(Component.literal(ModUtils.name(ModItems.WORLD_REFLECTOR.get()) + " §7has no biome."), false);
                        return InteractionResultHolder.fail(worldForger);
                    }
                } else {
                    pPlayer.displayClientMessage(Component.literal(ModUtils.name(ModItems.WORLD_REFLECTOR.get()) + " §7required in offhand."), false);
                    return InteractionResultHolder.fail(worldForger);
                }
            } else {
                pPlayer.displayClientMessage(Component.literal("§7Can only be used in §6\"InsideTheEye\" §7dimension"), false);
                return InteractionResultHolder.fail(worldForger);
            }
        } else return InteractionResultHolder.pass(worldForger);
    }



    public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
        return pRepair.is(Items.DRAGON_BREATH);
    }

    private static BiomeResolver makeResolver(Holder<Biome> pReplacementBiome) {
        return (p_262550_, p_262551_, p_262552_, p_262553_) -> pReplacementBiome;
    }

    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.literal("§7Hold " + ModUtils.name(ModItems.WORLD_REFLECTOR.get()) + " §7in offhand and right click to change current chunk's biome."));
    }
}
