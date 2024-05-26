package net.yasslify.eterna;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.yasslify.eterna.blocks.ModBlockEntities;
import net.yasslify.eterna.blocks.ModBlocks;
import net.yasslify.eterna.blocks.custom.EnderChestGatewayRenderer;
import net.yasslify.eterna.blocks.custom.ChorusAnchorRenderer;
import net.yasslify.eterna.entities.ModEntities;
import net.yasslify.eterna.items.CreativeModeTabs;
import net.yasslify.eterna.items.ModItems;
import net.yasslify.eterna.gui.ModMenus;
import net.yasslify.eterna.items.custom.Navigator;
import net.yasslify.eterna.particles.ModParticles;
import net.yasslify.eterna.particles.custom.ChorusParticle;
import net.yasslify.eterna.particles.custom.WarpParticle;
import net.yasslify.eterna.sounds.ModSounds;
import net.yasslify.eterna.world.dimension.ModDimensionEffects;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Eterna.MOD_ID)
public class Eterna {
    public static final String MOD_ID = "eterna";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Eterna() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        CreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);
        ModParticles.register(modEventBus);
        ModMenus.register(modEventBus);
        ModSounds.register(modEventBus);

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(final FMLClientSetupEvent event) {
        ItemProperties.register(ModItems.NAVIGATOR.get(), new ResourceLocation("angle"), new CompassItemPropertyFunction((pLevel, pStack, pEntity) -> {
            if (Navigator.hasTarget(pStack)) {
                String[] s = Navigator.getTarget(pStack);
                return GlobalPos.of(ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation(s[3])), new ResourceLocation(s[4])), Navigator.getPos(s));
            } else return null;
        }));
        ItemProperties.register(ModItems.NAVIGATOR.get(), new ResourceLocation("broken"), (pStack, pLevel, pEntity, i) -> pStack.getDamageValue() >= pStack.getMaxDamage() ? 1 : 0);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        //ItemBlockRenderTypes.setRenderLayer(ModBlocks.PORTAL_PROJECTOR.get(), RenderType.cutoutMipped());
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = Eterna.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeModEvents {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = Eterna.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.ENDER_CHEST_GATEWAY_BLOCKENTITY.get(), EnderChestGatewayRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.CHORUS_ANCHOR_BLOCKENTITY.get(), ChorusAnchorRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(EnderChestGatewayRenderer.MAIN_LOCATION, EnderChestGatewayRenderer::createMainLayer);
            event.registerLayerDefinition(ChorusAnchorRenderer.MAIN_LOCATION, ChorusAnchorRenderer::createMainLayer);
        }

        @SubscribeEvent
        public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
            ModDimensionEffects.registerModDimensionEffects((dimension, effects) -> event.register(dimension.location(), effects));
        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.WARP.get(), WarpParticle.Provider::new);
            event.registerSpriteSet(ModParticles.CHORUS.get(), ChorusParticle.Provider::new);
        }
    }
}
