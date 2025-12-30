package com.example.orbitalcannon;

import net.minecraft.server.level.ServerLevel;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class OrbitalCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("orbital")
                        .requires(src -> src.hasPermission(2))

                        // help
                        .then(Commands.literal("help")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    source.sendSuccess(() -> Component.literal("--- Orbital Cannon Help ---")
                                            .withStyle(ChatFormatting.GOLD), false);
                                    source.sendSuccess(() -> Component.literal("/orbital <type> <value> <delay> <count>")
                                            .withStyle(ChatFormatting.YELLOW), false);
                                    source.sendSuccess(() -> Component.literal("  type: 'nuke', 'stab', 'arrow', 'worldDestroyer'"), false);
                                    source.sendSuccess(() -> Component.literal("  value: strength"), false);
                                    source.sendSuccess(() -> Component.literal("  delay: ticks before strike"), false);
                                    source.sendSuccess(() -> Component.literal("  count: rods"), false);
                                    source.sendSuccess(() -> Component.literal(""), false);

                                    sendGameruleLine(source, "orbitalMasterToggle",
                                            "Controls if orbital rods trigger at all");
                                    sendGameruleLine(source, "orbitalChatAnnouncement",
                                            "Show a chat message when rods are given");
                                    sendGameruleLine(source, "orbitalTabHelper",
                                            "Toggle the autofill helper");
                                    sendGameruleLine(source, "orbital1useCreative",
                                            "If rods are 1-use in creative like survival");
                                    sendGameruleLine(source, "orbitalMultiplyDamageAll",
                                            "Multiplies damage to all (players + mobs)");
                                    sendGameruleLine(source, "orbitalMultiplyDamagePlayersOnly",
                                            "Multiplies damage to players only");
                                    sendGameruleLine(source, "orbitalMultiplyDamageEntitiesOnly",
                                            "Multiplies damage to entities (mobs + players)");
                                    sendGameruleLine(source, "orbitalMultiplyDamageMobsOnly",
                                            "Multiplies damage to mobs only (not players)");
                                    sendGameruleLine(source, "orbitalMultiplyDamageBlocksOnly",
                                            "Multiplies damage to blocks only");
                                    sendGameruleLine(source, "orbitalAdvancedTooltips",
                                            "Rods show extra info (value / delay)");
                                    sendGameruleLine(source, "orbitalFancyRods",
                                            "Rods show gold bold name + enchantment glint");
                                    sendGameruleLine(source, "orbitalConfigCommand",
                                            "Enable /orbital config");
                                    sendGameruleLine(source, "orbitalWemmbuOnly",
                                            "Restricts types to only nuke/stab");

                                    return 1;
                                })
                        )

                        // config subcommand (only visible if gamerule enabled)
                        .then(Commands.literal("config")
                                .requires(src -> {
                                    try {
                                        ServerLevel lvl = src.getLevel();
                                        return lvl != null && lvl.getGameRules().getBoolean(OrbitalGameRules.ORBITAL_CONFIG_COMMAND);
                                    } catch (Exception e) {
                                        return false;
                                    }
                                })
                                .then(Commands.literal("stab")
                                        .then(Commands.argument("param", StringArgumentType.word())
                                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"spacing", "tntCount", "offset", "fuse"}, b))
                                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                        .executes(ctx -> {
                                                            String param = StringArgumentType.getString(ctx, "param");
                                                            int v = IntegerArgumentType.getInteger(ctx, "value");
                                                            switch (param) {
                                                                case "spacing": OrbitalConfig.setStabSpacing(v); break;
                                                                case "tntCount": OrbitalConfig.setStabTntCount(v); break;
                                                                case "offset": OrbitalConfig.setStabOffset(v); break;
                                                                case "fuse": OrbitalConfig.setStabFuse(v); break;
                                                            }
                                                            ctx.getSource().sendSuccess(() -> Component.literal("Stab config updated: " + param + " = " + v), true);
                                                            return 1;
                                                        })) ))
                                .then(Commands.literal("nuke")
                                        .then(Commands.argument("param", StringArgumentType.word())
                                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"tntPerRingBase", "baseFuse", "ringSpacing", "offsetHeight"}, b))
                                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                        .executes(ctx -> {
                                                            String param = StringArgumentType.getString(ctx, "param");
                                                            int v = IntegerArgumentType.getInteger(ctx, "value");
                                                            switch (param) {
                                                                case "tntPerRingBase": OrbitalConfig.setNukeTntPerRingBase(v); break;
                                                                case "baseFuse": OrbitalConfig.setNukeBaseFuse(v); break;
                                                                case "ringSpacing": OrbitalConfig.setNukeRingSpacing(v); break;
                                                                case "offsetHeight": OrbitalConfig.setNukeOffsetHeight(v); break;
                                                            }
                                                            ctx.getSource().sendSuccess(() -> Component.literal("Nuke config updated: " + param + " = " + v), true);
                                                            return 1;
                                                        })) ))
                                .then(Commands.literal("multiplierDamage")
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.1))
                                                .executes(ctx -> {
                                                    double v = DoubleArgumentType.getDouble(ctx, "value");
                                                    OrbitalConfig.setMultiplyDamage(v);
                                                    ctx.getSource().sendSuccess(() -> Component.literal("Damage multiplier set to " + v), true);
                                                    return 1;
                                                })))
                        )

                        // main /orbital <type> <value> <delay> <count> -> gives rods
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    if (ctx.getSource().getLevel() == null) return builder.buildFuture();
                                    if (!ctx.getSource().getLevel().getGameRules().getBoolean(OrbitalGameRules.ORBITAL_TAB_HELPER)) {
                                        return builder.buildFuture();
                                    }
                                    boolean wemmbu = ctx.getSource().getLevel().getGameRules().getBoolean(OrbitalGameRules.ORBITAL_WEMMBU_ONLY);
                                    if (wemmbu) {
                                        return SharedSuggestionProvider.suggest(new String[]{"nuke", "stab"}, builder);
                                    } else {
                                        return SharedSuggestionProvider.suggest(new String[]{"nuke", "stab", "arrow", "worldDestroyer"}, builder);
                                    }
                                })
                                .then(Commands.argument("value", IntegerArgumentType.integer(1))
                                        .suggests((ctx, builder) -> {
                                            if (ctx.getSource().getLevel() == null) return builder.buildFuture();
                                            if (!ctx.getSource().getLevel().getGameRules().getBoolean(OrbitalGameRules.ORBITAL_TAB_HELPER)) {
                                                return builder.buildFuture();
                                            }
                                            String typed = "";
                                            try { typed = StringArgumentType.getString(ctx, "type").toLowerCase(); } catch (Exception ignored) {}
                                            if ("stab".equals(typed)) return SharedSuggestionProvider.suggest(new String[]{"20", "300"}, builder);
                                            if ("nuke".equals(typed)) return SharedSuggestionProvider.suggest(new String[]{"10", "20"}, builder);
                                            if ("arrow".equals(typed)) return SharedSuggestionProvider.suggest(new String[]{"10", "20"}, builder);
                                            if ("worlddestroyer".equals(typed)) return SharedSuggestionProvider.suggest(new String[]{"5", "10"}, builder);
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("delay", IntegerArgumentType.integer(0))
                                                .suggests((ctx, builder) -> {
                                                    if (ctx.getSource().getLevel() == null) return builder.buildFuture();
                                                    if (!ctx.getSource().getLevel().getGameRules().getBoolean(OrbitalGameRules.ORBITAL_TAB_HELPER)) {
                                                        return builder.buildFuture();
                                                    }
                                                    return SharedSuggestionProvider.suggest(new String[]{"1", "80"}, builder);
                                                })
                                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                        .suggests((ctx, builder) -> {
                                                            if (ctx.getSource().getLevel() == null) return builder.buildFuture();
                                                            if (!ctx.getSource().getLevel().getGameRules().getBoolean(OrbitalGameRules.ORBITAL_TAB_HELPER)) {
                                                                return builder.buildFuture();
                                                            }
                                                            return SharedSuggestionProvider.suggest(new String[]{"1", "20"}, builder);
                                                        })
                                                        .executes(ctx -> {
                                                            String type = StringArgumentType.getString(ctx, "type").toLowerCase();
                                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                                            int delay = IntegerArgumentType.getInteger(ctx, "delay");
                                                            int count = IntegerArgumentType.getInteger(ctx, "count");

                                                            CommandSourceStack source = ctx.getSource();
                                                            try {
                                                                Player player = source.getPlayerOrException();

                                                                // Wemmbu-only check
                                                                boolean wemmbu = source.getLevel().getGameRules().getBoolean(OrbitalGameRules.ORBITAL_WEMMBU_ONLY);
                                                                if (wemmbu && !(type.equals("nuke") || type.equals("stab"))) {
                                                                    source.sendFailure(Component.literal("Only nuke/stab allowed while orbitalWemmbuOnly is true"));
                                                                    return 0;
                                                                }

                                                                // Warn for large nukes but still give them
                                                                if (type.equals("nuke") && value >= 60) {
                                                                    source.sendSuccess(() -> Component.literal(
                                                                            "WARNING: Nuke sizes of 60+ can cause severe lag or crashes."
                                                                    ).withStyle(ChatFormatting.RED), false);
                                                                }

                                                                for (int i = 0; i < count; i++) {
                                                                    ItemStack rod = new ItemStack(Items.FISHING_ROD);
                                                                    rod.setDamageValue(rod.getMaxDamage() - 1);

                                                                    var nbt = rod.getOrCreateTag();
                                                                    nbt.putString("orbital_type", type);
                                                                    nbt.putInt("orbital_value", value);
                                                                    nbt.putInt("orbital_delay", delay);
                                                                    nbt.putBoolean("orbital_special", true);
                                                                    nbt.putBoolean("orbital_casted", false);
                                                                    nbt.putBoolean("orbital_oneuse", true);

                                                                    if (!player.getInventory().add(rod)) {
                                                                        player.drop(rod, false);
                                                                    }

                                                                    // update visuals immediately
                                                                    if (player.level() instanceof ServerLevel sl) {
                                                                        OrbitalEvents.applyRodVisuals(rod, sl);
                                                                    }
                                                                }

                                                                if (source.getLevel().getGameRules().getBoolean(OrbitalGameRules.ORBITAL_CHAT_ANNOUNCEMENT)) {
                                                                    source.sendSuccess(() ->
                                                                            Component.literal("Given " + count + " orbital rod(s): " +
                                                                                    type + " " + value + " " + delay), true);
                                                                }

                                                                return 1;
                                                            } catch (Exception e) {
                                                                source.sendFailure(Component.literal("Player required"));
                                                                return 0;
                                                            }
                                                        }))))));
    }

    private static void sendGameruleLine(CommandSourceStack src, String rule, String desc) {
        src.sendSuccess(() -> Component.literal("  /gamerule " + rule + " - ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(desc).withStyle(ChatFormatting.YELLOW)), false);
    }

    // Helper used by other command classes when they call into the OrbitalCommand class
    public static boolean checkTypeAllowed(CommandSourceStack source, String type) {
        boolean wemmbu = false;
        try {
            wemmbu = source.getLevel().getGameRules().getBoolean(OrbitalGameRules.ORBITAL_WEMMBU_ONLY);
        } catch (Exception ignored) {}

        if (wemmbu) {
            return type.equals("nuke") || type.equals("stab");
        }
        return type.equals("nuke") || type.equals("stab") || type.equals("arrow") || type.equals("worlddestroyer");
    }
}
