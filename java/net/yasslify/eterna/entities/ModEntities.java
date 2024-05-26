package net.yasslify.eterna.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.yasslify.eterna.Eterna;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Eterna.MOD_ID);

    //public static final RegistryObject<EntityType<EyePortal>> EyePortal = ENTITY_TYPES.register("eye_portal",
    //        () -> EntityType.Builder.<EyePortal>of(EyePortal::new, MobCategory.MISC).sized(1F, 1F)
    //                .build(new ResourceLocation(InsideTheEye.MODID, "eye_portal").toString()));
////
    //public static final RegistryObject<EntityType<Enduit>> ENDUIT = ENTITY_TYPES.register("enduit",
    //        () -> EntityType.Builder.<Enduit>of(Enduit::new, MobCategory.MISC).sized(0.75F, 0.75f)
    //                .build(new ResourceLocation(InsideTheEye.MODID, "enduit").toString()));

    public static void register(IEventBus eventBus) { ENTITY_TYPES.register(eventBus); }
}
