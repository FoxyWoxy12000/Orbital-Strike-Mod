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
import net.minecraft.core.BlockPos;

public class OrbitalStrikeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("orbitalstrike")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            if (ctx.getSource().getLevel() == null) return builder.buildFuture();
                            if (!ctx.getSource().getLevel().getGameRules().getBoolean(OrbitalGameRules.ORBITAL_TAB_HELPER))
                                return builder.buildFuture();
                            boolean wemmbu = ctx.getSource().getLevel().getGameRules().getBoolean(OrbitalGameRules.ORBITAL_WEMMBU_ONLY);
                            if (wemmbu) return SharedSuggestionProvider.suggest(new String[]{"nuke", "stab"}, builder);
                            return SharedSuggestionProvider.suggest(new String[]{"nuke", "stab", "arrow", "worldDestroyer"}, builder);
                        })
                        .then(Commands.argument("value", IntegerArgumentType.integer(1))
                                // For worldDestroyer we may need a second value; that's handled by users using different command or using value2 optional - to keep command chain simpler here we allow value2 via value2 argument later if needed
                                .then(Commands.argument("delay", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                        .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                                .executes(ctx -> {
                                                                    String type = StringArgumentType.getString(ctx, "type").toLowerCase();
                                                                    int value = IntegerArgumentType.getInteger(ctx, "value");
                                                                    int delay = IntegerArgumentType.getInteger(ctx, "delay");
                                                                    double xd = DoubleArgumentType.getDouble(ctx, "x");
                                                                    double yd = DoubleArgumentType.getDouble(ctx, "y");
                                                                    double zd = DoubleArgumentType.getDouble(ctx, "z");

                                                                    CommandSourceStack source = ctx.getSource();

                                                                    // Wemmbu-only check
                                                                    boolean wemmbu = source.getLevel().getGameRules().getBoolean(OrbitalGameRules.ORBITAL_WEMMBU_ONLY);
                                                                    if (wemmbu && !(type.equals("nuke") || type.equals("stab"))) {
                                                                        source.sendFailure(Component.literal("Only nuke/stab allowed while orbitalWemmbuOnly is true"));
                                                                        return 0;
                                                                    }

                                                                    BlockPos pos = BlockPos.containing(xd, yd, zd);
                                                                    OrbitalEvents.scheduleShot(null, type, value, delay, pos);

                                                                    source.sendSuccess(() -> Component.literal("Scheduled " + type + " strike at " + pos + " in " + delay + " ticks."), true);
                                                                    return 1;
                                                                }))))))));

    }
}
