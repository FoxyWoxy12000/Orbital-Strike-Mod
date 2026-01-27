package com.orbitalstrike.core.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.orbitalstrike.core.rod.OrbitalRodUtil;
import com.orbitalstrike.core.shot.OrbitalShot;
import com.orbitalstrike.core.shot.ShotRegistry;
import com.orbitalstrike.core.util.RaycastUtil;
import com.orbitalstrike.core.util.StrikeScheduler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class OrbitalCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            dispatcher.register(
                    CommandManager.literal("orbital")

                            .then(CommandManager.literal("give")
                                    .then(CommandManager.argument("shot", StringArgumentType.word())
                                            .then(CommandManager.argument("delay", IntegerArgumentType.integer(0))
                                                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                            .executes(OrbitalCommand::giveNoSize)
                                                    )
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
                                                    .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                            .executes(OrbitalCommand::strikePosNoSize)
                                                    )
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
                                                            .executes(OrbitalCommand::strikeCrosshairNoSize)
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
                                                                    .executes(OrbitalCommand::giveFixedStrikeRodNoSize)
                                                            )
                                                            .then(CommandManager.argument("size", IntegerArgumentType.integer(0))
                                                                    .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                                            .executes(OrbitalCommand::giveFixedStrikeRod)
                                                                    )
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
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        String shotId = StringArgumentType.getString(ctx, "shot");
        int delay = IntegerArgumentType.getInteger(ctx, "delay");
        int size = IntegerArgumentType.getInteger(ctx, "size");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        OrbitalShot shot = ShotRegistry.get(shotId);
        if (shot == null) {
            ctx.getSource().sendError(Text.literal("Unknown shot type: " + shotId));
            return 0;
        }

        for (int i = 0; i < amount; i++) {
            ItemStack rod = new ItemStack(Items.FISHING_ROD, 1);
            rod.setDamage(rod.getMaxDamage() - 1);
            OrbitalRodUtil.tag(rod, shotId, delay, size, null, player.getUuidAsString());
            player.giveItemStack(rod);
        }

        ctx.getSource().sendFeedback(() -> Text.literal("Gave " + amount + "x " + shotId + " orbital rod(s)"), false);
        return 1;
    }

    private static int giveNoSize(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        String shotId = StringArgumentType.getString(ctx, "shot");
        int delay = IntegerArgumentType.getInteger(ctx, "delay");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        OrbitalShot shot = ShotRegistry.get(shotId);
        if (shot == null) {
            ctx.getSource().sendError(Text.literal("Unknown shot type: " + shotId));
            return 0;
        }

        for (int i = 0; i < amount; i++) {
            ItemStack rod = new ItemStack(Items.FISHING_ROD, 1);
            rod.setDamage(rod.getMaxDamage() - 1);
            OrbitalRodUtil.tag(rod, shotId, delay, 0, null, player.getUuidAsString());
            player.giveItemStack(rod);
        }

        ctx.getSource().sendFeedback(() -> Text.literal("Gave " + amount + "x " + shotId + " orbital rod(s)"), false);
        return 1;
    }

    private static int strikePos(CommandContext<ServerCommandSource> ctx) {
        String shotId = StringArgumentType.getString(ctx, "shot");
        int delay = IntegerArgumentType.getInteger(ctx, "delay");
        int size = IntegerArgumentType.getInteger(ctx, "size");
        BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "pos");

        OrbitalShot shot = ShotRegistry.get(shotId);
        if (shot == null) {
            ctx.getSource().sendError(Text.literal("Unknown shot type: " + shotId));
            return 0;
        }

        StrikeScheduler.schedule(delay, () -> {
            shot.fire(ctx.getSource().getWorld(), pos.toCenterPos(), size);
        });

        ctx.getSource().sendFeedback(() -> Text.literal("Orbital strike incoming..."), false);
        return 1;
    }

    private static int strikePosNoSize(CommandContext<ServerCommandSource> ctx) {
        String shotId = StringArgumentType.getString(ctx, "shot");
        int delay = IntegerArgumentType.getInteger(ctx, "delay");
        BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "pos");

        OrbitalShot shot = ShotRegistry.get(shotId);
        if (shot == null) {
            ctx.getSource().sendError(Text.literal("Unknown shot type: " + shotId));
            return 0;
        }

        StrikeScheduler.schedule(delay, () -> {
            shot.fire(ctx.getSource().getWorld(), pos.toCenterPos(), 0);
        });

        ctx.getSource().sendFeedback(() -> Text.literal("Orbital strike incoming..."), false);
        return 1;
    }

    private static int strikeCrosshair(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        String shotId = StringArgumentType.getString(ctx, "shot");
        int delay = IntegerArgumentType.getInteger(ctx, "delay");
        int size = IntegerArgumentType.getInteger(ctx, "size");

        BlockHitResult hit = RaycastUtil.raycast(player);
        if (hit == null) {
            ctx.getSource().sendError(Text.literal("No valid target found"));
            return 0;
        }

        OrbitalShot shot = ShotRegistry.get(shotId);
        if (shot == null) {
            ctx.getSource().sendError(Text.literal("Unknown shot type: " + shotId));
            return 0;
        }

        StrikeScheduler.schedule(delay, () -> {
            shot.fire(ctx.getSource().getWorld(), hit.getBlockPos().toCenterPos(), size);
        });

        ctx.getSource().sendFeedback(() -> Text.literal("Orbital strike incoming..."), false);
        return 1;
    }

    private static int strikeCrosshairNoSize(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        String shotId = StringArgumentType.getString(ctx, "shot");
        int delay = IntegerArgumentType.getInteger(ctx, "delay");

        BlockHitResult hit = RaycastUtil.raycast(player);
        if (hit == null) {
            ctx.getSource().sendError(Text.literal("No valid target found"));
            return 0;
        }

        OrbitalShot shot = ShotRegistry.get(shotId);
        if (shot == null) {
            ctx.getSource().sendError(Text.literal("Unknown shot type: " + shotId));
            return 0;
        }

        StrikeScheduler.schedule(delay, () -> {
            shot.fire(ctx.getSource().getWorld(), hit.getBlockPos().toCenterPos(), 0);
        });

        ctx.getSource().sendFeedback(() -> Text.literal("Orbital strike incoming..."), false);
        return 1;
    }

    private static int giveFixedStrikeRod(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        String shotId = StringArgumentType.getString(ctx, "shot");
        int delay = IntegerArgumentType.getInteger(ctx, "delay");
        int size = IntegerArgumentType.getInteger(ctx, "size");
        BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "pos");

        OrbitalShot shot = ShotRegistry.get(shotId);
        if (shot == null) {
            ctx.getSource().sendError(Text.literal("Unknown shot type: " + shotId));
            return 0;
        }

        ItemStack rod = new ItemStack(Items.FISHING_ROD, 1);
        rod.setDamage(rod.getMaxDamage() - 1);
        OrbitalRodUtil.tag(rod, shotId, delay, size, pos, player.getUuidAsString());
        player.giveItemStack(rod);

        ctx.getSource().sendFeedback(() -> Text.literal("Gave fixed-target " + shotId + " orbital rod"), false);
        return 1;
    }

    private static int giveFixedStrikeRodNoSize(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        String shotId = StringArgumentType.getString(ctx, "shot");
        int delay = IntegerArgumentType.getInteger(ctx, "delay");
        BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "pos");

        OrbitalShot shot = ShotRegistry.get(shotId);
        if (shot == null) {
            ctx.getSource().sendError(Text.literal("Unknown shot type: " + shotId));
            return 0;
        }

        ItemStack rod = new ItemStack(Items.FISHING_ROD, 1);
        rod.setDamage(rod.getMaxDamage() - 1);
        OrbitalRodUtil.tag(rod, shotId, delay, 0, pos, player.getUuidAsString());
        player.giveItemStack(rod);

        ctx.getSource().sendFeedback(() -> Text.literal("Gave fixed-target " + shotId + " orbital rod"), false);
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