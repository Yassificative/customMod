package net.yasslify.eterna.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.yasslify.eterna.ModUtils;
import net.yasslify.eterna.blocks.ModBlockEntities;

public class EnderChestGateway extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, WATERLOGGED, POWERED);
    }

    public EnderChestGateway(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.FALSE).setValue(POWERED, Boolean.FALSE));
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new EnderChestGatewayBlockEntity(pPos, pState);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ModBlockEntities.ENDER_CHEST_GATEWAY_BLOCKENTITY.get(), EnderChestGatewayBlockEntity::tick);
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.getBlockEntity(pPos) instanceof EnderChestGatewayBlockEntity blockEntity && blockEntity.isOpen && !blockEntity.isUsing && !blockEntity.isFueling) {
            ItemStack itemStack = pPlayer.getItemInHand(pHand);
            if (itemStack.is(Items.CHORUS_FRUIT)) {
                if (blockEntity.openStage < 9) {
                    blockEntity.isFueling = true;
                    if (!pPlayer.isCreative()) itemStack.shrink(1);
                    return InteractionResult.SUCCESS;
                } else {
                    ModUtils.sound(pLevel, pPos, SoundEvents.PLAYER_BURP, 1, 1);
                    return InteractionResult.CONSUME;
                }
            } else {
                if (pPlayer.isCrouching()) {}
                else blockEntity.isUsing = true;
                return InteractionResult.SUCCESS;
            }
        } else return InteractionResult.CONSUME;
    }

    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
        if (pEntity.isCrouching()) return;
        Vec3 destination = pPos.getCenter().add(0, 0.125, 0);
        Vec3 result = destination.subtract(pEntity.position()).scale(0.0125F / destination.distanceTo(pEntity.position()));
        pEntity.push(result.x, result.y, result.z);
        super.stepOn(pLevel, pPos, pState, pEntity);
    }

    public boolean isSignalSource(BlockState pState) {
        return true;
    }

    public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.getValue(POWERED) ? 15 : 0;
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        for (int i = 0; i < 3; ++i) {
            int j = pRandom.nextInt(2) * 2 - 1;
            int k = pRandom.nextInt(2) * 2 - 1;
            double d0 = (double)pPos.getX() + 0.5D + 0.25D * (double)j;
            double d1 = (double)((float)pPos.getY() + pRandom.nextFloat() + 0.75);
            double d2 = (double)pPos.getZ() + 0.5D + 0.25D * (double)k;
            double d3 = (double)(pRandom.nextFloat() * (float)j);
            double d4 = ((double)pRandom.nextFloat() + 1.5) * 0.125D;
            double d5 = (double)(pRandom.nextFloat() * (float)k);
            pLevel.addParticle(ParticleTypes.ENCHANT, d0, d1, d2, d3, d4, d5);
        }
    }

    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(POWERED) ? 0 : 9;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.join(Block.box(1.0D, 0.0D, 1.0D, 15.0D, 10.0D, 15.0D), Block.box(3.0D, 2.0D, 3.0D, 13.0D, 10.0D, 13.0D), BooleanOp.ONLY_FIRST);
    }

    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.getValue(WATERLOGGED)) pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }
}
