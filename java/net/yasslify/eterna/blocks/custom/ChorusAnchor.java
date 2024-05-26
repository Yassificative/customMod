package net.yasslify.eterna.blocks.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.network.simple.SimpleChannel;
import net.yasslify.eterna.ModUtils;
import net.yasslify.eterna.blocks.ModBlockEntities;
import net.yasslify.eterna.items.ModItems;
import net.yasslify.eterna.items.custom.Navigator;
import net.yasslify.eterna.particles.ModParticles;

public class ChorusAnchor extends BaseEntityBlock {
    protected final RandomSource random = RandomSource.create();
    protected ClientLevel clientLevel = Minecraft.getInstance().level;
    public static final BooleanProperty LINKED = BooleanProperty.create("linked");
    public static final BooleanProperty WAXED = BooleanProperty.create("waxed");

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(LINKED, WAXED);
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(LINKED, Boolean.FALSE).setValue(WAXED, Boolean.FALSE);
    }

    public ChorusAnchor(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LINKED, Boolean.FALSE).setValue(WAXED, Boolean.FALSE));
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ChorusAnchorBlockEntity(pPos, pState);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ModBlockEntities.CHORUS_ANCHOR_BLOCKENTITY.get(), ChorusAnchorBlockEntity::tick);
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack itemStack = pPlayer.getItemInHand(pHand);
        if (pLevel.getBlockEntity(pPos) instanceof ChorusAnchorBlockEntity blockEntity) {
            if (itemStack.is(ModItems.NAVIGATOR.get())) {
                if (!Navigator.hasTarget(itemStack)) {
                    Navigator.putTarget(itemStack, pLevel, pPos);
                    ModUtils.sound(pLevel, pPos, SoundEvents.LODESTONE_COMPASS_LOCK, 1, 1);
                    return InteractionResult.SUCCESS;
                } else {
                    String[] s = Navigator.getTarget(itemStack);
                    BlockPos pos = Navigator.getPos(s);
                    if (pLevel instanceof ServerLevel serverLevel) {
                        if (Navigator.isTargetValid(serverLevel, s)) {
                            if (!pos.equals(pPos)) {
                                blockEntity.setNavigator(pPlayer, itemStack);
                                pLevel.setBlockAndUpdate(pPos, pState.setValue(LINKED, Boolean.TRUE));
                                if (!blockEntity.hasFuel()) pLevel.setBlockAndUpdate(pPos, pState.setValue(LINKED, Boolean.FALSE));
                                else ModUtils.sound(pLevel, pPos, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, 0.5F, 1);
                                return InteractionResult.SUCCESS;
                            } else return InteractionResult.CONSUME;
                        } else {
                            if (!Navigator.isValidDimension(serverLevel, s)) {
                                blockEntity.setNavigator(pPlayer, itemStack);
                                pLevel.removeBlock(pPos, false);
                                pLevel.explode(null, pLevel.damageSources().badRespawnPointExplosion(pPos.getCenter()), null, pPos.getCenter(), 5.0F, true, Level.ExplosionInteraction.BLOCK);
                                return InteractionResult.SUCCESS;
                            } else return InteractionResult.CONSUME;
                        }
                    } else return InteractionResult.CONSUME;
                }
            } else if (itemStack.is(Items.HONEYCOMB)) {
                if (!pState.getValue(WAXED)) {
                    itemStack.shrink(1);
                    pLevel.setBlockAndUpdate(pPos, pState.setValue(WAXED, Boolean.TRUE));
                    pLevel.levelEvent(pPlayer, 3003, pPos, 0);
                    return InteractionResult.SUCCESS;
                } else return InteractionResult.PASS;
            } else if (itemStack.getItem() instanceof AxeItem) {
                if (pState.getValue(WAXED)) {
                    pLevel.setBlockAndUpdate(pPos, pState.setValue(WAXED, Boolean.FALSE));
                    ModUtils.sound(pLevel, pPos, SoundEvents.AXE_WAX_OFF, 1, 1);
                    pLevel.levelEvent(pPlayer, 3004, pPos, 0);
                    return InteractionResult.SUCCESS;
                } else return InteractionResult.PASS;
            } else if (itemStack.is(Items.BLAZE_POWDER)) {
                if (blockEntity.isLinked() && blockEntity.getNavigator().getDamageValue() > 0) {
                    if (!pPlayer.getAbilities().instabuild) itemStack.shrink(1);
                    ModUtils.sound(pLevel, pPos, blockEntity.hasFuel() ? SoundEvents.CHORUS_FLOWER_GROW : SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, 1, 1);
                    blockEntity.getNavigator().setDamageValue(blockEntity.getNavigator().getDamageValue() - blockEntity.getNavigator().getMaxDamage() / 4);
                    pLevel.setBlockAndUpdate(pPos, pState.setValue(LINKED, blockEntity.hasFuel()));
                    return InteractionResult.SUCCESS;
                } else return InteractionResult.CONSUME;
            } else {
                if (pPlayer.isCrouching()) {
                    if (blockEntity.isLinked()) {
                        unLink(pLevel, pPos, pState, blockEntity, true);
                        return InteractionResult.SUCCESS;
                    } else return InteractionResult.PASS;
                } else return InteractionResult.PASS;
            }
        } else return InteractionResult.CONSUME;
    }

    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
        if (!(pLevel instanceof ServerLevel)) return;
        if (!(pEntity instanceof LivingEntity || pEntity instanceof ItemEntity) || pEntity instanceof EnderDragon || pEntity.isCrouching()) return;
        if (!pState.getValue(WAXED) && pEntity.getBoundingBox().intersects(new AABB(pPos.getX() + 0.125, pPos.getY() + 1, pPos.getZ() + 0.125, pPos.getX() + 0.875, pPos.getY() + 2, pPos.getZ() + 0.875))) {
            if (pLevel.getBlockEntity(pPos) instanceof ChorusAnchorBlockEntity blockEntity && blockEntity.isLinked() && blockEntity.hasFuel()) {
                String[] s = Navigator.getTarget(blockEntity.getNavigator());
                BlockPos blockPos = Navigator.getPos(s);
                if (pLevel.getBlockState(blockPos).is(this)) {
                    if (!pEntity.isOnPortalCooldown()) {
                        Vec3 vec3 = blockPos.above().getCenter().add(0, -0.5, 0);
                        Vec3 originalPos = pEntity.position();
                        pEntity.moveTo(vec3);
                        if (!pLevel.collidesWithSuffocatingBlock(pEntity, pEntity.getBoundingBox()) || pEntity instanceof ItemEntity) {
                            if (pEntity instanceof LivingEntity) blockEntity.getNavigator().hurt(1, RandomSource.create(), null);
                            if (!blockEntity.hasFuel()) unLink(pLevel, pPos, pState, blockEntity, false);
                            pEntity.setDeltaMovement(0, 0, 0);
                            pEntity.teleportTo(vec3.x, vec3.y, vec3.z);
                            pEntity.setPortalCooldown(10);
                            for (int j = 0; j < 128; ++j) {
                                double d0 = (double) j / 127.0D;
                                float f = (random.nextFloat() - 0.5F) * 0.2F;
                                float f1 = (random.nextFloat() - 0.5F) * 0.2F;
                                float f2 = (random.nextFloat() - 0.5F) * 0.2F;
                                double d1 = Mth.lerp(d0, vec3.x, vec3.x) + (random.nextDouble() - 0.5D) * (double) pEntity.getBbWidth() * 2.0D;
                                double d2 = Mth.lerp(d0, vec3.y - 0.5, vec3.y - 0.5) + random.nextDouble() * (double) pEntity.getBbHeight();
                                double d3 = Mth.lerp(d0, vec3.z, vec3.z) + (random.nextDouble() - 0.5D) * (double) pEntity.getBbWidth() * 2.0D;
                                if (Minecraft.getInstance().level != null) Minecraft.getInstance().level.addParticle(ParticleTypes.PORTAL, d1, d2, d3, f, f1, f2);
                            }
                            ModUtils.sound(pLevel, pPos, SoundEvents.CHORUS_FRUIT_TELEPORT, 0.5F, 1);
                            ModUtils.sound(pLevel, blockPos, SoundEvents.CHORUS_FRUIT_TELEPORT, 0.5F, 1);
                        } else {
                            if (pEntity instanceof Player player) ModUtils.actionBarMsg(player, "Destination is obstructed");
                            pEntity.moveTo(originalPos);
                        }
                    } else pEntity.setPortalCooldown(10);
                } else unLink(pLevel, pPos, pState, blockEntity, false);
            }
        }
        super.stepOn(pLevel, pPos, pState, pEntity);
    }

    public boolean isRandomlyTicking(BlockState pState) {
        return true;
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pLevel.getBlockState(pPos.below()).is(Blocks.END_STONE)) {
            if (pLevel.getBlockEntity(pPos) instanceof ChorusAnchorBlockEntity blockEntity && blockEntity.isLinked()) {
                if (ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, true) && blockEntity.getNavigator().getDamageValue() > 0) {
                    ModUtils.sound(pLevel, pPos, blockEntity.hasFuel() ? SoundEvents.CHORUS_FLOWER_GROW : SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, 1, 1);
                    blockEntity.getNavigator().setDamageValue(blockEntity.getNavigator().getDamageValue() - 1);
                    pLevel.setBlockAndUpdate(pPos, pState.setValue(LINKED, blockEntity.hasFuel()));
                }
            }
        }
    }

    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        if (pLevel.getBlockEntity(pPos) instanceof ChorusAnchorBlockEntity blockEntity && blockEntity.isLinked()) unLink(pLevel, pPos, pState, blockEntity, true);
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    public static void unLink(Level pLevel, BlockPos pPos, BlockState pState, ChorusAnchorBlockEntity pBlockEntity, boolean pDrop) {
        if (pDrop) Containers.dropItemStack(pLevel, pPos.getX() + 0.5, pPos.getY() + 1, pPos.getZ() + 0.5, pBlockEntity.getNavigator());
        if (pState.getValue(LINKED)) ModUtils.sound(pLevel, pPos, SoundEvents.RESPAWN_ANCHOR_DEPLETE.get(), 0.5F, 1);
        pLevel.setBlockAndUpdate(pPos, pState.setValue(LINKED, Boolean.FALSE));
        pLevel.sendBlockUpdated(pPos, pState, pState, 3);
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pState.getValue(LINKED)) {
            if (pRandom.nextInt(100) == 0) ModUtils.sound(pLevel, pPos, SoundEvents.CHORUS_FLOWER_GROW, 1, 1);
            Vec3 vec3 = pPos.getCenter();
            double x = vec3.x + 0.7 * (0.5 - pRandom.nextDouble());
            double y = vec3.y;
            double z = vec3.z + 0.7 * (0.5 - pRandom.nextDouble());
            pLevel.addParticle(ModParticles.CHORUS.get(), x, y, z, 0, pRandom.nextFloat() * 0.05, 0);
        }
    }

    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LINKED) ? 15 : 0;
    }

    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }
}
