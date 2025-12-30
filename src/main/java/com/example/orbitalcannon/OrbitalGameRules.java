package com.example.orbitalcannon;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OrbitalCannon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class OrbitalGameRules {
    // Master toggle: enable/disable orbital system entirely
    public static final GameRules.Key<GameRules.BooleanValue> ORBITAL_MASTER_TOGGLE =
            GameRules.register("orbitalMasterToggle", GameRules.Category.MISC, BooleanValue.create(true));

    // Chat announcement when giving rods
    public static final GameRules.Key<GameRules.BooleanValue> ORBITAL_CHAT_ANNOUNCEMENT =
            GameRules.register("orbitalChatAnnouncement", GameRules.Category.MISC, BooleanValue.create(true));

    // Damage multipliers (gamerules)
    public static final GameRules.Key<GameRules.BooleanValue> ORBITAL_MULTIPLY_DAMAGE_ALL =
            GameRules.register("orbitalMultiplyDamageAll", GameRules.Category.MISC, BooleanValue.create(false));

    public static final GameRules.Key<GameRules.BooleanValue> ORBITAL_MULTIPLY_DAMAGE_PLAYERS =
            GameRules.register("orbitalMultiplyDamagePlayersOnly", GameRules.Category.MISC, BooleanValue.create(false));

    public static final GameRules.Key<GameRules.BooleanValue> ORBITAL_MULTIPLY_DAMAGE_ENTITIES =
            GameRules.register("orbitalMultiplyDamageEntitiesOnly", GameRules.Category.MISC, BooleanValue.create(false));

    public static final GameRules.Key<GameRules.BooleanValue> ORBITAL_MULTIPLY_DAMAGE_MOBS =
            GameRules.register("orbitalMultiplyDamageMobsOnly", GameRules.Category.MISC, BooleanValue.create(false));

    public static final GameRules.Key<GameRules.BooleanValue> ORBITAL_MULTIPLY_DAMAGE_BLOCKS =
            GameRules.register("orbitalMultiplyDamageBlocksOnly", GameRules.Category.MISC, BooleanValue.create(false));

    // Creative one-use behaviour: if true, rods in creative disappear after 1 use (like survival)
    public static final GameRules.Key<GameRules.BooleanValue> ORBITAL_1USE_CREATIVE =
            GameRules.register("orbital1useCreative", GameRules.Category.MISC, BooleanValue.create(true));

    // Tab helper for commands
    public static final GameRules.Key<GameRules.BooleanValue> ORBITAL_TAB_HELPER =
            GameRules.register("orbitalTabHelper", GameRules.Category.MISC, BooleanValue.create(true));

    // Advanced tooltips (show type/value/delay above hotbar)
    public static final GameRules.Key<GameRules.BooleanValue> ORBITAL_ADVANCED_TOOLTIPS =
            GameRules.register("orbitalAdvancedTooltips", GameRules.Category.MISC, BooleanValue.create(false));

    // Fancy rods (gold + hidden enchant)
    public static final GameRules.Key<GameRules.BooleanValue> ORBITAL_FANCY_RODS =
            GameRules.register("orbitalFancyRods", GameRules.Category.MISC, BooleanValue.create(false));

    // Allow the /orbital config command
    public static final GameRules.Key<GameRules.BooleanValue> ORBITAL_CONFIG_COMMAND =
            GameRules.register("orbitalConfigCommand", GameRules.Category.MISC, BooleanValue.create(false));

    // Wemmbu-only: restrict types to only nuke and stab
    public static final GameRules.Key<GameRules.BooleanValue> ORBITAL_WEMMBU_ONLY =
            GameRules.register("orbitalWemmbuOnly", GameRules.Category.MISC, BooleanValue.create(false));

    // Old names (accepted earlier) kept for reference only (no auto-mapping implemented)
    private static final String[] OLD_NAMES = {
            "doOrbital", "doOrbital5xDamageAll", "doOrbital5xDamageOnlyPlayers",
            "doOrbital5xDamageOnlyBlocks", "doOrbital1useCreative"
    };

    public static boolean getBooleanSafe(net.minecraft.server.level.ServerLevel lvl, GameRules.Key<BooleanValue> key) {
        if (lvl == null || key == null) return false;
        return lvl.getGameRules().getBoolean(key);
    }
}
