package net.yasslify.eterna.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.yasslify.eterna.Eterna;
import net.yasslify.eterna.blocks.ModBlocks;

public class CreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Eterna.MOD_ID);

    public static final RegistryObject<CreativeModeTab> INSIDETHEEYE_TAB = CREATIVE_MODE_TABS.register("insidetheeye_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(Items.ENDER_EYE))
                    .title(Component.translatable("creativetab.eterna_tab"))
                    .displayItems((pParameters, pOutput) -> {

                        pOutput.accept(Blocks.ENDER_CHEST);
                        pOutput.accept(ModItems.HARMONY.get());
                        pOutput.accept(ModBlocks.CHORUS_ANCHOR.get());
                        pOutput.accept(ModItems.NAVIGATOR.get());
                        pOutput.accept(ModItems.WORLD_FORGE.get());
                        pOutput.accept(ModItems.WORLD_REFLECTOR.get());

                    }).build());
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
