package net.yasslify.eterna.gui.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.yasslify.eterna.gui.ModMenus;

public class ChorusAnchorMenu extends AbstractContainerMenu {
    public ChorusAnchorMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(2));
    }

    public ChorusAnchorMenu(int pContainerId, Inventory inv, BlockEntity blockEntity, SimpleContainerData simpleContainerData) {
        super(ModMenus.CHORUS_ANCHOR_MENU.get(), pContainerId);

    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return false;
    }
}
