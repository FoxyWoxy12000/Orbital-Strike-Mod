package com.orbitalstrike.core.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class OrbitalCommand {

    public static boolean MISS_FAILSAFE = true;

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            dispatcher.register(
                    CommandManager.literal("orbital")

                            .then(CommandManager.literal("give")
                                    .then(CommandManager.argument("shot", StringArgumentType.word())
                                            .then(CommandManager.argument("delay", IntegerArgumentType.integer(0))
                                                    .then(CommandManager.argument("size", IntegerArgumentType.integer(0))
                                                            .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                                    .executes(OrbitalCommand::give)
                                                            )
                                                    )
                                            )
                                    )
                            )

                            .then(CommandManager.literal("strike")
                                    .then(CommandManager.argument("shot", StringArgumentType.word())
                                            .then(CommandManager.argument("delay", IntegerArgumentType.integer(0))
                                                    .then(CommandManager.argument("size", IntegerArgumentType.integer(0))
                                                            .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                                    .executes(OrbitalCommand::strikePos)
                                                            )
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("crosshair")
                                            .then(CommandManager.argument("shot", StringArgumentType.word())
                                                    .then(CommandManager.argument("delay", IntegerArgumentType.integer(0))
                                                            .then(CommandManager.argument("size", IntegerArgumentType.integer(0))
                                                                    .executes(OrbitalCommand::strikeCrosshair)
                                                            )
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("give")
                                            .then(CommandManager.argument("shot", StringArgumentType.word())
                                                    .then(CommandManager.argument("delay", IntegerArgumentType.integer(0))
                                                            .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                                    .executes(OrbitalCommand::giveFixedStrikeRod)
                                                            )
                                                    )
                                            )
                                    )
                            )

                            .then(CommandManager.literal("setting")
                                    .then(CommandManager.argument("setting", StringArgumentType.word())
                                            .then(CommandManager.argument("value", StringArgumentType.word())
                                                    .executes(OrbitalCommand::setting)
                                            )
                                    )
                            )

                            .then(CommandManager.literal("help")
                                    .executes(OrbitalCommand::help)
                                    .then(CommandManager.literal("setting")
                                            .executes(OrbitalCommand::helpSetting)
                                    )
                            )

                            .then(CommandManager.literal("craft")
                                    .executes(OrbitalCommand::craft)
                            )

                            .then(CommandManager.literal("auth")
                                    .then(CommandManager.argument("player", EntityArgumentType.player())
                                            .executes(OrbitalCommand::auth)
                                    )
                            )
            );
        });
    }

    private static int give(CommandContext<ServerCommandSource> ctx) {
        return 1;
    }

    private static int strikePos(CommandContext<ServerCommandSource> ctx) {
        return 1;
    }

    private static int strikeCrosshair(CommandContext<ServerCommandSource> ctx) {
        return 1;
    }

    private static int giveFixedStrikeRod(CommandContext<ServerCommandSource> ctx) {
        return 1;
    }

    private static int setting(CommandContext<ServerCommandSource> ctx) {
        return 1;
    }

    private static int help(CommandContext<ServerCommandSource> ctx) {
        return 1;
    }

    private static int helpSetting(CommandContext<ServerCommandSource> ctx) {
        return 1;
    }

    private static int craft(CommandContext<ServerCommandSource> ctx) {
        return 1;
    }

    private static int auth(CommandContext<ServerCommandSource> ctx) {
        return 1;
    }
}
