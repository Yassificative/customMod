package net.yasslify.eterna.blocks;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.yasslify.eterna.Eterna;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yasslify.eterna.blocks.custom.ChorusAnchor;
import net.yasslify.eterna.blocks.custom.EnderChestGateway;
import net.yasslify.eterna.items.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Eterna.MOD_ID);

    public static final RegistryObject<Block> ENDER_CHEST_GATEWAY = registerBlock("ender_chest_gateway",
            () -> new EnderChestGateway(BlockBehaviour.Properties.copy(Blocks.ENDER_CHEST)), false);
    public static final RegistryObject<Block> CHORUS_ANCHOR = registerBlock("chorus_anchor",
            () -> new ChorusAnchor(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)), true);

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, boolean item) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        if (item) registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}


