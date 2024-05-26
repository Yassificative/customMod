package net.yasslify.eterna.items.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.yasslify.eterna.blocks.ModBlocks;
import net.yasslify.eterna.blocks.custom.EnderChestGateway;
import net.yasslify.eterna.blocks.custom.EnderChestGatewayBlockEntity;

public class KeyOfEnder extends Item {
    public KeyOfEnder() {
        super(new Properties().durability(64).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.is(Blocks.ENDER_CHEST)) {
            level.setBlockAndUpdate(blockpos, ModBlocks.ENDER_CHEST_GATEWAY.get().defaultBlockState()
                    .setValue(EnderChestGateway.FACING, blockstate.getValue(EnderChestGateway.FACING))
                    .setValue(EnderChestGateway.WATERLOGGED, blockstate.getValue(EnderChestGateway.WATERLOGGED)));

            if (level.getBlockEntity(blockpos) instanceof EnderChestGatewayBlockEntity blockEntity) blockEntity.isUsing = true;

            pContext.getItemInHand().hurtAndBreak(1, pContext.getPlayer(), e -> e.broadcastBreakEvent(pContext.getHand()));
            return InteractionResult.SUCCESS;
        } else return InteractionResult.FAIL;
    }
}
