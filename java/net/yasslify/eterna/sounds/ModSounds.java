package net.yasslify.eterna.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yasslify.eterna.Eterna;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Eterna.MOD_ID);

    public static final RegistryObject<SoundEvent> AMBIENT = registerSoundEvents("ambient");
    public static final RegistryObject<SoundEvent> BOOM = registerSoundEvents("boom"); // BOOM Geomorphism | Cinematic Trailer Sound Effects by SUBMORITY on pixabay.com
    public static final RegistryObject<SoundEvent> BRAM = registerSoundEvents("bram");
    public static final RegistryObject<SoundEvent> BURST = registerSoundEvents("burst");
    public static final RegistryObject<SoundEvent> DROP = registerSoundEvents("drop"); // 067723_Bass Drop by pixabay on pixabay.com
    public static final RegistryObject<SoundEvent> HIT = registerSoundEvents("hit"); // HIT Sound Effect by PremswaroopKasukurthi on pixabay.com
    public static final RegistryObject<SoundEvent> METALWHOOSH = registerSoundEvents("metalwhoosh");
    public static final RegistryObject<SoundEvent> PORTALIDLE = registerSoundEvents("portalidle");
    public static final RegistryObject<SoundEvent> SWOOSH = registerSoundEvents("swoosh"); // Swoosh Transition With Metal Overtones by UNIVERSFIELD on pixabay.com
    public static final RegistryObject<SoundEvent> SUS = registerSoundEvents("sus");

    private static RegistryObject<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Eterna.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
