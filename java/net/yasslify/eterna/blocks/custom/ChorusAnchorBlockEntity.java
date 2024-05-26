package net.yasslify.eterna.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.yasslify.eterna.blocks.ModBlockEntities;
import net.yasslify.eterna.items.ModItems;
import net.yasslify.eterna.items.custom.Navigator;

public class ChorusAnchorBlockEntity extends BlockEntity {
    public ChorusAnchorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.CHORUS_ANCHOR_BLOCKENTITY.get(), pPos, pBlockState);
    }

    public NonNullList<ItemStack> navigator = NonNullList.withSize(1, ItemStack.EMPTY);

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, ChorusAnchorBlockEntity pBlockEntity) {
        if (pLevel instanceof ServerLevel serverLevel && pBlockEntity.isLinked() && !Navigator.isTargetValid(serverLevel, Navigator.getTarget(pBlockEntity.getNavigator()))) {
            ChorusAnchor.unLink(pLevel, pPos, pState, pBlockEntity, true);
        }
    }

    public boolean isLinked() {
        return getNavigator().is(ModItems.NAVIGATOR.get());
    }

    public boolean hasFuel() { return getNavigator().getDamageValue() < getNavigator().getMaxDamage(); }

    public ItemStack getNavigator() {
        return this.navigator.get(0);
    }

    public void setNavigator(Player pPlayer, ItemStack pStack) {
        ItemStack itemStack = getNavigator();
        this.navigator.set(0, pStack.split(1));
        pPlayer.addItem(itemStack);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag() {
        CompoundTag compoundtag = new CompoundTag();
        ContainerHelper.saveAllItems(compoundtag, this.navigator);
        return compoundtag;
    }

    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.navigator.clear();
        ContainerHelper.loadAllItems(pTag, this.navigator);
    }

    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        ContainerHelper.saveAllItems(pTag, this.navigator);
    }

    public AABB getRenderBoundingBox() {
        return super.getRenderBoundingBox().inflate(1);
    }
}
