package net.yasslify.eterna.items.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.yasslify.eterna.ModUtils;
import net.yasslify.eterna.blocks.ModBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Navigator extends Item implements Vanishable {
    public Navigator() {
        super(new Properties().stacksTo(1).durability(16));
    }

    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pLevel instanceof ServerLevel serverLevel && pEntity instanceof Player player) {
            if (hasTarget(pStack)) {
                String[] s = getTarget(pStack);
                BlockPos pos = getPos(s);
                if (player.getMainHandItem() == pStack) {
                    if (isTargetValid(serverLevel, s)) {
                        if (ModUtils.isLookingAtBlock(player, pos)) {
                            ModUtils.actionBarMsg(player, "§d§oTarget Anchor: " + s[0] + " " + s[1] + " " + s[2]);
                        } else ModUtils.actionBarMsg(player, "§7§oTarget Anchor: " + s[0] + " " + s[1] + " " + s[2]);
                    } else {
                        if (Navigator.isValidDimension(serverLevel, s)) {
                            ModUtils.actionBarMsg(player, "§7§oTarget Anchor: §c§oNot found");
                        } else ModUtils.actionBarMsg(player, "§7§oTarget Anchor: §c§k§o" + s[0] + " " + s[1] + " " + s[2]);
                    }
                }
            } else ModUtils.actionBarMsg(player, "§7§oTarget Anchor: §8§oNone");
        }
    }

    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (hasTarget(pStack)) {
            String[] s = getTarget(pStack);
            pTooltipComponents.add(Component.nullToEmpty("§7§oPosition: §d§o" + s[0] + " " + s[1] + " " + s[2]));
            pTooltipComponents.add(Component.nullToEmpty("§7§oDimension: §5§o" + s[4]));
        }
    }

    public boolean isValidRepairItem(ItemStack pStack, ItemStack pRepairCandidate) {
        pStack.setRepairCost(0);
        return pRepairCandidate.is(Items.BLAZE_POWDER);
    }

    public boolean isRepairable(ItemStack stack) {
        return stack.is(Items.BLAZE_POWDER);
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    public static boolean hasTarget(ItemStack pStack) {
        return pStack.getOrCreateTag().get("target") != null;
    }

    public static void putTarget(ItemStack pStack, Level pLevel, BlockPos pPos) {
        pStack.getOrCreateTag().putString("target", pPos.getX() + " " + pPos.getY() + " " + pPos.getZ() + " " + pLevel.dimension().registry() + " " + pLevel.dimension().location());
    }

    public static String[] getTarget(ItemStack pStack) {
        return pStack.getOrCreateTag().getString("target").split(" ");
    }

    public static BlockPos getPos(String[] s) {
        return new BlockPos(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
    }

    public static boolean isTargetValid(ServerLevel pLevel, String[] s) {
        return isValidDimension(pLevel, s) && isValidAnchor(pLevel, s);
    }

    public static boolean isValidDimension(ServerLevel pLevel, String[] s) {
        return pLevel.dimension().registry().toString().equals(s[3]) && pLevel.dimension().location().toString().equals(s[4]);
    }

    public static boolean isValidAnchor(ServerLevel pLevel, String[] s) {
        ResourceKey<Level> dimension = ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation(s[3])), new ResourceLocation(s[4]));
        Level level = pLevel.getServer().getLevel(dimension);
        return level != null && level.getBlockState(getPos(s)).is(ModBlocks.CHORUS_ANCHOR.get());
    }
}
