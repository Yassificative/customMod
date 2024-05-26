package net.yasslify.eterna.particles;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yasslify.eterna.Eterna;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Eterna.MOD_ID);

    public static final RegistryObject<SimpleParticleType> WARP =
            PARTICLE_TYPES.register("warp", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> CHORUS =
            PARTICLE_TYPES.register("chorus", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
