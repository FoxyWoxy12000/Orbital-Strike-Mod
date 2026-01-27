package com.orbitalstrike.core.command;

import com.orbitalstrike.core.shot.OrbitalShot;
import com.orbitalstrike.core.shot.ShotRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

public class OrbitalCommand {

    public static boolean MISS_FAILSAFE = true;

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            dispatcher.register(
                    CommandManager.literal("orbital")
                            .then(CommandManager.literal("strike")
                                    .then(CommandManager.argument("shot", StringArgumentType.word())
                                            .then(CommandManager.argument("delay", IntegerArgumentType.integer(0))
                                                    .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                            .executes(ctx -> {
                                                                ServerCommandSource src = ctx.getSource();
                                                                String shotId = StringArgumentType.getString(ctx, "shot");
                                                                int delay = IntegerArgumentType.getInteger(ctx, "delay");
                                                                BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "pos");
                                                                OrbitalShot shot = ShotRegistry.get(shotId);
                                                                if (shot == null) return 0;
                                                                shot.fire(src.getWorld(), pos.toCenterPos(), delay, src.getName());
                                                                return 1;
                                                            })
                                                    )
                                            )
                                    )
                            )
                            .then(CommandManager.literal("setting")
                                    .then(CommandManager.literal("gamerule")
                                            .then(CommandManager.literal("missfailsafe")
                                                    .then(CommandManager.argument("value", BoolArgumentType.bool())
                                                            .executes(ctx -> {
                                                                MISS_FAILSAFE = BoolArgumentType.getBool(ctx, "value");
                                                                return 1;
                                                            })
                                                    )
                                            )
                                    )
                            )
            );
        });
    }
}
