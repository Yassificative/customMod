package net.yasslify.eterna;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Eterna.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue EMISSIVE_ENDER_CHEST_GATEWAY_TEXTURE = BUILDER
            .comment("False recommended if you are using shaders. If you are using Complementary Shader and need it to be emissive, add a new line \"block.60013=insidetheeye:ender_chest_gateway\" to \"\\shaders\\block.properties\" and change line 36 at \"shaders\\lib\\materials\\materialHandling\\blockEntityMaterials.glsl\" from \"if (blockEntityId == 60012) { // Ender Chest\" to \"if (blockEntityId == 60012 || blockEntityId == 60013) { // Ender Chest\".")
            .define("emissiveEnderChestGatewayTexture", true);

    private static final ForgeConfigSpec.BooleanValue RENDER_ENDER_CHEST_GATEWAY_RUNE = BUILDER
            .define("renderEnderChestGatewayRune", true);

    private static final ForgeConfigSpec.BooleanValue RENDER_ENDER_CHEST_GATEWAY_PORTAL = BUILDER
            .define("renderEnderChestGatewayPortal", true);

    private static final ForgeConfigSpec.BooleanValue RENDER_ENDER_CHEST_GATEWAY_BEAM = BUILDER
            .define("renderEnderChestGatewayBeam", true);

    private static final ForgeConfigSpec.BooleanValue RENDER_ENDER_CHEST_GATEWAY_AURA = BUILDER
            .define("renderEnderChestGatewayAura", true);

    private static final ForgeConfigSpec.BooleanValue DIMENSION_STARS = BUILDER
            .define("dimensionStars", true);

    private static final ForgeConfigSpec.BooleanValue DIMENSION_OVERWORLD_SKY_EFFECTS = BUILDER
            .define("dimensionOverworldEffects", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean emissiveEnderChestGatewayTexture;
    public static boolean renderEnderChestGatewayRune;
    public static boolean renderEnderChestGatewayPortal;
    public static boolean renderEnderChestGatewayBeam;
    public static boolean renderEnderChestGatewayAura;
    public static boolean dimensionStars;
    public static boolean dimensionOverworldEffects;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        emissiveEnderChestGatewayTexture = EMISSIVE_ENDER_CHEST_GATEWAY_TEXTURE.get();
        renderEnderChestGatewayRune = RENDER_ENDER_CHEST_GATEWAY_RUNE.get();
        renderEnderChestGatewayPortal = RENDER_ENDER_CHEST_GATEWAY_PORTAL.get();
        renderEnderChestGatewayBeam = RENDER_ENDER_CHEST_GATEWAY_BEAM.get();
        renderEnderChestGatewayAura = RENDER_ENDER_CHEST_GATEWAY_AURA.get();
        dimensionStars = DIMENSION_STARS.get();
        dimensionOverworldEffects = DIMENSION_OVERWORLD_SKY_EFFECTS.get();
    }
}
