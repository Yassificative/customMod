package net.yasslify.eterna.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yasslify.eterna.ModUtils;
import net.yasslify.eterna.blocks.ModBlockEntities;
import net.yasslify.eterna.particles.ModParticles;
import net.yasslify.eterna.sounds.ModSounds;
import net.yasslify.eterna.world.dimension.ModDimension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EnderChestGatewayBlockEntity extends BlockEntity {
    public EnderChestGatewayBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.ENDER_CHEST_GATEWAY_BLOCKENTITY.get(), pPos, pBlockState);
    }

    public boolean isOpen;
    public boolean isUsing;
    public boolean isFueling;

    public int openStage;
    public int usingTick;
    public int maxUsingTick = 100;
    public int circleTick = 0;

    List<Entity> EntitiesInRadius = new ArrayList<>();
    void updateEntitiesInRadius(Level pLevel, BlockPos pPos) {
        this.EntitiesInRadius = pLevel.getEntitiesOfClass(Entity.class, new AABB(
                pPos.getX()-4, pPos.getY(), pPos.getZ()-4,
                pPos.getX()+5, pPos.getY()+5, pPos.getZ()+5));
        this.EntitiesInRadius.removeIf(entity -> !(entity instanceof LivingEntity || entity instanceof ItemEntity) || entity instanceof EnderDragon);
    }

    List<Entity> EntitiesInside = new ArrayList<>();
    void updateEntitiesInside(Level pLevel, BlockPos pPos) {
        this.EntitiesInside = pLevel.getEntitiesOfClass(Entity.class, new AABB(
                pPos.getX()+0.8125, pPos.getY()+0.5625, pPos.getZ()+0.8125,
                pPos.getX()+0.1875, pPos.getY()+0.125, pPos.getZ()+0.1875));
        this.EntitiesInside.removeIf(entity -> !(entity instanceof LivingEntity || entity instanceof ItemEntity) || entity instanceof EnderDragon);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, EnderChestGatewayBlockEntity pBlockEntity) {
        if (!pBlockEntity.isOpen) {
            if (pBlockEntity.usingTick == 0) {
                pBlockEntity.openStage = 9;
                ModUtils.sound(pLevel, pPos, SoundEvents.ENDER_CHEST_OPEN, 0.5F, pLevel.random.nextFloat() * 0.125F);
            }
            pBlockEntity.usingTick++;
            if (pBlockEntity.usingTick >= 20) {
                pBlockEntity.usingTick = 0;
                pBlockEntity.isOpen = true;
            }
        } else if (pBlockEntity.isUsing) {
            if (pBlockEntity.usingTick == 0) {
                pBlockEntity.openStage--;
                pLevel.setBlockAndUpdate(pPos, pState.setValue(EnderChestGateway.POWERED, Boolean.TRUE));
                ModUtils.sound(pLevel, pPos, ModSounds.BOOM.get(), 1F, 0.75f);
            }
            if (pBlockEntity.usingTick == pBlockEntity.maxUsingTick - 10) ModUtils.sound(pLevel, pPos, ModSounds.SWOOSH.get(), 0.1F, 1F);
            pBlockEntity.usingTick++;
            pBlockEntity.updateEntitiesInRadius(pLevel, pPos);
            for (Entity entity : pBlockEntity.EntitiesInRadius) {
                if (!entity.isCrouching()) {
                    pLevel.addParticle(ParticleTypes.REVERSE_PORTAL, entity.position().x, entity.getEyeY() + 0.2, entity.position().z, 0, 0.2, 0);
                    Vec3 result = pPos.getCenter().subtract(entity.position()).scale(0.0125F / pPos.getCenter().distanceTo(entity.position()));
                    entity.push(result.x, result.y, result.z);
                }
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 2, 0, false, false, false));
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 2, 0, false, false, false));
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 0, false, false, false));
                }
            }

            if (pBlockEntity.usingTick >= pBlockEntity.maxUsingTick) {
                pLevel.setBlockAndUpdate(pPos, pState.setValue(EnderChestGateway.POWERED, Boolean.FALSE));
                pBlockEntity.usingTick = 0;
                pBlockEntity.isUsing = false;
                for (Entity entity : pBlockEntity.EntitiesInRadius) pBlockEntity.changeDimension(pLevel, pPos, entity, true);
                if (pBlockEntity.openStage == 0) {
                    ModUtils.sound(pLevel, pPos, SoundEvents.CONDUIT_DEACTIVATE, 1F, 1.25F);
                    ModUtils.clientEvent(pPos.getCenter().add(0, -0.5, 0), "eyeShattering");
                    pLevel.setBlockAndUpdate(pPos, Blocks.ENDER_CHEST.defaultBlockState().setValue(EnderChestGateway.FACING, pState.getValue(EnderChestGateway.FACING)).setValue(EnderChestGateway.WATERLOGGED, pState.getValue(EnderChestGateway.WATERLOGGED)));
                }
            }
        } else if (pBlockEntity.isFueling) {
            if (pBlockEntity.usingTick == 0) {
                pBlockEntity.openStage++;
                ModUtils.sound(pLevel, pPos, SoundEvents.GENERIC_EAT, 0.5F, pLevel.random.nextFloat() * 0.1F + 0.25F);
            }
            pBlockEntity.usingTick++;
            if (pBlockEntity.usingTick >= 20) {
                pBlockEntity.usingTick = 0;
                pBlockEntity.isFueling = false;
            }
        } else {
            if (pBlockEntity.circleTick % 2 == 0) {
                pBlockEntity.updateEntitiesInside(pLevel, pPos);
                for (Entity entity : pBlockEntity.EntitiesInside) {
                    //if (entity instanceof ItemEntity itemEntity && itemEntity.getItem().is(Items.CHORUS_FRUIT)) {
                    //    if (pBlockEntity.isOpen && !pBlockEntity.isFueling) {
                    //        if (pBlockEntity.openStage < 9 && !pBlockEntity.isUsing) {
                    //            pLevel.sendBlockUpdated(pPos, pState, pState, 3);
                    //            pBlockEntity.isFueling = true;
                    //            itemEntity.getItem().shrink(1);
                    //        } else pBlockEntity.changeDimension(pLevel, pPos, itemEntity, false);
                    //    }
                    //} else
                    pBlockEntity.changeDimension(pLevel, pPos, entity, true);
                }
            }
        }
        pBlockEntity.circleTick = pBlockEntity.circleTick < 359 ? pBlockEntity.circleTick + 1 : 0;
    }

    public void changeDimension(Level pLevel, BlockPos pPos, Entity pEntity, boolean returnIfCrouching) {
        if (returnIfCrouching && pEntity.isCrouching()) return;
        if (pLevel instanceof ServerLevel) {
            ResourceKey<Level> destination = pLevel.dimension().equals(ModDimension.INSIDETHEEYE_KEY) ? Level.OVERWORLD : ModDimension.INSIDETHEEYE_KEY;
            ServerLevel destinationLevel = ((ServerLevel) pLevel).getServer().getLevel(destination);
            if (destinationLevel == null) return;
            if (destination == ModDimension.INSIDETHEEYE_KEY) {
                for (BlockPos pos : BlockPos.betweenClosed(-2, 62, -2, 2, 62, 2)) destinationLevel.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
                for (BlockPos pos : BlockPos.betweenClosed(-2, 63, -2, 2, 64, 2)) destinationLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                pEntity.teleportTo(destinationLevel, 0.5, 64, 0.5, Set.of(), pEntity.yRotO, pEntity.xRotO);
                ModUtils.sound(destinationLevel, pEntity.blockPosition(), SoundEvents.PORTAL_TRAVEL, 0.25F, 1.25F);
            } else {
                if (pEntity instanceof ServerPlayer player) {
                    player.wonGame = true;
                    player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0));
                } else {
                    BlockPos pos = destinationLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, destinationLevel.getSharedSpawnPos());
                    pEntity.teleportTo(destinationLevel, pos.getX(), pos.getY(), pos.getCenter().z, Set.of(), pEntity.yRotO, pEntity.xRotO);
                }
            }
        }

        ModUtils.clientEvent(pEntity.position(), "eyeShattering");

        for (int i = 0; i < 8; i ++) {
            Vec3 vec3 = pEntity.position();
            double offsetX = 0.5 - RandomSource.create().nextDouble();
            double offsetY = 0.5 - RandomSource.create().nextDouble();
            double d0 = vec3.x + offsetX;
            double d1 = vec3.y;
            double d2 = vec3.z + offsetY;
            double d3 = RandomSource.create().nextFloat() * 0.5;
            pLevel.addParticle(ModParticles.WARP.get(), d0, d1, d2, 0, d3, 0);
        }
    }

    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.isOpen = pTag.getBoolean("isOpen");
        this.isUsing = pTag.getBoolean("isUsing");
        this.isFueling = pTag.getBoolean("isFueling");
        this.openStage = pTag.getInt("openStage");
        this.usingTick = pTag.getInt("usingTick");
    }

    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putBoolean("isOpen", this.isOpen);
        pTag.putBoolean("isUsing", this.isUsing);
        pTag.putBoolean("isFueling", this.isFueling);
        pTag.putInt("openStage", this.openStage);
        pTag.putInt("usingTick", this.usingTick);
    }

    public AABB getRenderBoundingBox() {
        return this.isUsing ? super.getRenderBoundingBox().inflate(4).setMaxY(256) : super.getRenderBoundingBox().inflate(1);
    }
}
