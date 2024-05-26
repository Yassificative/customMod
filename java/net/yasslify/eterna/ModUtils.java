package net.yasslify.eterna;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


public class ModUtils {
    public static void sound(Level pLevel, BlockPos pPos, SoundEvent pSound, float pVolumn, float pPitch) {
        pLevel.playSound(null, pPos.getX() + 0.5D, pPos.getY() + 0.5D, pPos.getZ() + 0.5D, pSound, SoundSource.AMBIENT, pVolumn, pPitch);
    }

    public static void actionBarMsg(String pString) {
        Minecraft.getInstance().player.displayClientMessage(Component.nullToEmpty(pString), true);
    }

    public static void actionBarMsg(Player pPlayer, String pString) {
        pPlayer.displayClientMessage(Component.nullToEmpty(pString), true);
    }

    public static void chatMsg(String pString) {
        Minecraft.getInstance().player.displayClientMessage(Component.nullToEmpty(pString), false);
    }

    public static void chatMsg(Player pPlayer, String pString) {
        pPlayer.displayClientMessage(Component.nullToEmpty(pString), false);
    }

    public static boolean isLookingAtBlock(Player pPlayer, BlockPos pPos) {
        HitResult hitResult = pPlayer.pick(pPlayer.getBlockReach(), 0, false);
        if (hitResult.getType().equals(HitResult.Type.ENTITY)) return false;
        return pPos.equals(((BlockHitResult) hitResult).getBlockPos());
    }

    public static BlockState getLookingAtBlock(Player pPlayer, Level pLevel) {
        HitResult hitResult = pPlayer.pick(pPlayer.getBlockReach(), 0, false);
        if (hitResult.getType().equals(HitResult.Type.ENTITY)) return Blocks.AIR.defaultBlockState();
        return pLevel.getBlockState(((BlockHitResult) hitResult).getBlockPos());
    }

    public static void clientEvent(Position pPos, String pString) {
        if (Minecraft.getInstance().level == null) return;
        ClientLevel level = Minecraft.getInstance().level;
        double x = pPos.x();
        double y = pPos.y();
        double z = pPos.z();
        if (pString.equals("eyeShattering")) {
            for (int i = 0; i < 8; i++) {
                level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE)), x, y, z, RandomSource.create().nextGaussian() * 0.15D, RandomSource.create().nextDouble() * 0.2D, RandomSource.create().nextGaussian() * 0.15D);
            }

            for (double d = 0.0D; d < (Math.PI * 2D); d += 0.15707963267948966D) {
                level.addParticle(ParticleTypes.PORTAL, x + Math.cos(d) * 5.0D, y - 0.25, z + Math.sin(d) * 5.0D, Math.cos(d) * -5.0D, 0.0D, Math.sin(d) * -5.0D);
                level.addParticle(ParticleTypes.PORTAL, x + Math.cos(d) * 5.0D, y - 0.25, z + Math.sin(d) * 5.0D, Math.cos(d) * -7.0D, 0.0D, Math.sin(d) * -7.0D);
            }
        }
    }

    public static String name(Item pItem) {
        String color = "§";
        switch (pItem.getRarity(pItem.asItem().getDefaultInstance())) {
            case EPIC : color += "d";
                break;
            case RARE : color += "b";
                break;
            case UNCOMMON : color += "e";
                break;
            case COMMON : color += "f";
        }
        return color + I18n.get(pItem.getDescriptionId());
    }
}
