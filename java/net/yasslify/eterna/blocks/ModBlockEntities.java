package net.yasslify.eterna.blocks;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yasslify.eterna.Eterna;
import net.yasslify.eterna.blocks.custom.ChorusAnchorBlockEntity;
import net.yasslify.eterna.blocks.custom.EnderChestGatewayBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Eterna.MOD_ID);

    public static final RegistryObject<BlockEntityType<EnderChestGatewayBlockEntity>> ENDER_CHEST_GATEWAY_BLOCKENTITY =
            BLOCK_ENTITIES.register("ender_chest_gateway_blockentity",
                    () -> BlockEntityType.Builder.of(EnderChestGatewayBlockEntity::new, ModBlocks.ENDER_CHEST_GATEWAY.get())
                            .build(null));
    public static final RegistryObject<BlockEntityType<ChorusAnchorBlockEntity>> CHORUS_ANCHOR_BLOCKENTITY =
            BLOCK_ENTITIES.register("chorus_anchor_blockentity",
                    () -> BlockEntityType.Builder.of(ChorusAnchorBlockEntity::new, ModBlocks.CHORUS_ANCHOR.get())
                            .build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
