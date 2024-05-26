package net.yasslify.eterna.items;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yasslify.eterna.Eterna;
import net.yasslify.eterna.items.custom.Harmony;
import net.yasslify.eterna.items.custom.Navigator;
import net.yasslify.eterna.items.custom.WorldForge;
import net.yasslify.eterna.items.custom.WorldReflector;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Eterna.MOD_ID);

    public static final RegistryObject<Item> HARMONY = ITEMS.register("harmony", Harmony::new);
    public static final RegistryObject<Item> NAVIGATOR = ITEMS.register("navigator", Navigator::new);
    public static final RegistryObject<Item> WORLD_FORGE = ITEMS.register("world_forge", WorldForge::new);
    public static final RegistryObject<Item> WORLD_REFLECTOR = ITEMS.register("world_reflector", WorldReflector::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
