package com.orbitalstrike.core.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.orbitalstrike.core.rod.OrbitalRodUtil;
import com.orbitalstrike.core.rod.RodTriggerStyle;
import com.orbitalstrike.core.shot.OrbitalShot;
import com.orbitalstrike.core.shot.ShotRegistry;
import com.orbitalstrike.core.rod.RodNameUpdater;
import com.orbitalstrike.core.shot.impl.NukeMk2Shot;
import com.orbitalstrike.core.shot.impl.NukeMk4Shot;
import com.orbitalstrike.core.shot.impl.NukeMk6Shot;
import com.orbitalstrike.core.shot.impl.StabShot;
import com.orbitalstrike.core.util.RaycastUtil;
import com.orbitalstrike.core.util.StrikeScheduler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class OrbitalCommand {

    public static boolean ORBITAL_CHAT_ANNOUNCEMENT = false;
    public static boolean ADVANCED_ROD_NAMES = false;
    public static boolean MISS_FAILSAFE = true;
    public static boolean ONE_USE_CREATIVE = true;
    public static boolean FANCY_RODS = false;
    public static boolean MK2_CLOUD_ONLY = false;

    private static final SuggestionProvider<ServerCommandSource> SHOT_SUGGESTIONS = (context, builder) ->
            CommandSource.suggestMatching(ShotRegistry.getAllIds(), builder);

    private static final SuggestionProvider<ServerCommandSource> TRIGGER_STYLE_SUGGESTIONS = (context, builder) ->
            CommandSource.suggestMatching(new String[]{"instant", "cast", "reel"}, builder);

    private static final SuggestionProvider<ServerCommandSource> SMART_SIZE_SUGGESTIONS = (context, builder) -> {
        try {
            String shotId = context.getArgument("shot", String.class);
            if (shotId.equals("nukemk4") || shotId.equals("nukemk2") || shotId.equals("nukemk6")) {
                return CommandSource.suggestMatching(new String[]{"12", "16"}, builder);
            }
        } catch (IllegalArgumentException e) {
            // Shot argument not found, return empty
        }
        return builder.buildFuture();
    };

    private static String getNukeDisplayName(String shotId) {
        if (shotId.equals("nukemk4") || shotId.equals("nukemk2") || shotId.equals("nukemk6")) {
            return "nuke";
        }
        return shotId;
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            dispatcher.register(
                    CommandManager.literal("orbital")

                            .then(CommandManager.literal("give")
                                    .then(CommandManager.argument("shot", StringArgumentType.word())
                                            .suggests(SHOT_SUGGESTIONS)
                                            .then(CommandManager.argument("delay", IntegerArgumentType.integer(0))
                                                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                            .executes(OrbitalCommand::giveNoSize)
                                                    )
                                                    .then(CommandManager.argument("size", IntegerArgumentType.integer(0, 32))
                                                            .suggests(SMART_SIZE_SUGGESTIONS)
                                                            .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                                    .executes(OrbitalCommand::give)
                                                            )
                                                    )
                                            )
                                    )
                            )

                            .then(CommandManager.literal("strike")
                                    .then(CommandManager.argument("shot", StringArgumentType.word())
                                            .suggests(SHOT_SUGGESTIONS)
                                            .then(CommandManager.argument("delay", IntegerArgumentType.integer(0))
                                                    .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                            .executes(OrbitalCommand::strikePosNoSize)
                                                    )
                                                    .then(CommandManager.argument("size", IntegerArgumentType.integer(0, 32))
                                                            .suggests(SMART_SIZE_SUGGESTIONS)
                                                            .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                                    .executes(OrbitalCommand::strikePos)
                                                            )
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("crosshair")
                                            .then(CommandManager.argument("shot", StringArgumentType.word())
                                                    .suggests(SHOT_SUGGESTIONS)
                                                    .then(CommandManager.argument("delay", IntegerArgumentType.integer(0))
                                                            .executes(OrbitalCommand::strikeCrosshairNoSize)
                                                            .then(CommandManager.argument("size", IntegerArgumentType.integer(0, 32))
                                                                    .suggests(SMART_SIZE_SUGGESTIONS)
                                                                    .executes(OrbitalCommand::strikeCrosshair)
                                                            )
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("give")
                                            .then(CommandManager.argument("shot", StringArgumentType.word())
                                                    .suggests(SHOT_SUGGESTIONS)
                                                    .then(CommandManager.argument("delay", IntegerArgumentType.integer(0))
                                                            .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                                    .executes(OrbitalCommand::giveFixedStrikeRodNoSize)
                                                            )
                                                            .then(CommandManager.argument("size", IntegerArgumentType.integer(0, 32))
                                                                    .suggests(SMART_SIZE_SUGGESTIONS)
                                                                    .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                                            .executes(OrbitalCommand::giveFixedStrikeRod)
                                                                    )
                                                            )
                                                    )
                                            )
                                    )
                            )

                            .then(CommandManager.literal("setting")
                                    .then(CommandManager.literal("gamerule")
                                            .then(CommandManager.literal("OrbitalChatAnnouncement")
                                                    .then(CommandManager.argument("value", BoolArgumentType.bool())
                                                            .executes(ctx -> {
                                                                ORBITAL_CHAT_ANNOUNCEMENT = BoolArgumentType.getBool(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("Orbital chat announcement set to " + ORBITAL_CHAT_ANNOUNCEMENT), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            .then(CommandManager.literal("AdvancedRodNames")
                                                    .then(CommandManager.argument("value", BoolArgumentType.bool())
                                                            .executes(ctx -> {
                                                                ADVANCED_ROD_NAMES = BoolArgumentType.getBool(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("Advanced rod names set to " + ADVANCED_ROD_NAMES), false);

                                                                for (ServerPlayerEntity player : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
                                                                    RodNameUpdater.updateAllPlayerRods(player);
                                                                }

                                                                return 1;
                                                            })
                                                    )
                                            )
                                            .then(CommandManager.literal("MissFailsafe")
                                                    .then(CommandManager.argument("value", BoolArgumentType.bool())
                                                            .executes(ctx -> {
                                                                MISS_FAILSAFE = BoolArgumentType.getBool(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("Miss failsafe set to " + MISS_FAILSAFE), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            .then(CommandManager.literal("OneUseCreative")
                                                    .then(CommandManager.argument("value", BoolArgumentType.bool())
                                                            .executes(ctx -> {
                                                                ONE_USE_CREATIVE = BoolArgumentType.getBool(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("One use creative set to " + ONE_USE_CREATIVE), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            .then(CommandManager.literal("FancyRods")
                                                    .then(CommandManager.argument("value", BoolArgumentType.bool())
                                                            .executes(ctx -> {
                                                                FANCY_RODS = BoolArgumentType.getBool(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("Fancy rods set to " + FANCY_RODS), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            .then(CommandManager.literal("Mk2NukeCloudOnly")
                                                    .then(CommandManager.argument("value", BoolArgumentType.bool())
                                                            .executes(ctx -> {
                                                                MK2_CLOUD_ONLY = BoolArgumentType.getBool(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("Mk2 Cloud Only set to " + MK2_CLOUD_ONLY), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("triggerstyle")
                                            .then(CommandManager.argument("style", StringArgumentType.word())
                                                    .suggests(TRIGGER_STYLE_SUGGESTIONS)
                                                    .executes(ctx -> {
                                                        String style = StringArgumentType.getString(ctx, "style");
                                                        try {
                                                            RodTriggerStyle.current = RodTriggerStyle.valueOf(style.toUpperCase());
                                                            ctx.getSource().sendFeedback(() -> Text.literal("Trigger style set to " + style), false);
                                                            return 1;
                                                        } catch (IllegalArgumentException e) {
                                                            ctx.getSource().sendError(Text.literal("Invalid trigger style. Use: instant, cast, reel"));
                                                            return 0;
                                                        }
                                                    })
                                            )
                                    )

                                    .then(CommandManager.literal("nukemk4")
                                            .then(CommandManager.literal("OFFSET_HEIGHT")
                                                    .then(CommandManager.argument("value", IntegerArgumentType.integer(0))
                                                            .executes(ctx -> {
                                                                NukeMk4Shot.OFFSET_HEIGHT = IntegerArgumentType.getInteger(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("NukeMk4 offset height set to " + NukeMk4Shot.OFFSET_HEIGHT), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            .then(CommandManager.literal("INNER_RING_MULTIPLIER")
                                                    .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0))
                                                            .executes(ctx -> {
                                                                NukeMk4Shot.INNER_RING_MULTIPLIER = DoubleArgumentType.getDouble(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("NukeMk4 inner ring multiplier set to " + NukeMk4Shot.INNER_RING_MULTIPLIER), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            .then(CommandManager.literal("OUTER_RING_MULTIPLIER")
                                                    .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0))
                                                            .executes(ctx -> {
                                                                NukeMk4Shot.OUTER_RING_MULTIPLIER = DoubleArgumentType.getDouble(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("NukeMk4 outer ring multiplier set to " + NukeMk4Shot.OUTER_RING_MULTIPLIER), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            .then(CommandManager.literal("IMPERFECTION_PERCENT")
                                                    .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                                            .executes(ctx -> {
                                                                NukeMk4Shot.IMPERFECTION_PERCENT = DoubleArgumentType.getDouble(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("NukeMk4 imperfection percent set to " + NukeMk4Shot.IMPERFECTION_PERCENT), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            .then(CommandManager.literal("TNT_FUSE")
                                                    .then(CommandManager.argument("value", IntegerArgumentType.integer(0))
                                                            .executes(ctx -> {
                                                                NukeMk4Shot.TNT_FUSE = IntegerArgumentType.getInteger(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("NukeMk4 TNT fuse set to " + NukeMk4Shot.TNT_FUSE), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("nukemk2")
                                            .then(CommandManager.literal("RING_BASE_SPEED")
                                                    .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0))
                                                            .executes(ctx -> {
                                                                NukeMk2Shot.RING_BASE_SPEED = DoubleArgumentType.getDouble(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("NukeMk2 ring base speed set to " + NukeMk2Shot.RING_BASE_SPEED), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            .then(CommandManager.literal("TNT_FUSE")
                                                    .then(CommandManager.argument("value", IntegerArgumentType.integer(0))
                                                            .executes(ctx -> {
                                                                NukeMk2Shot.TNT_FUSE = IntegerArgumentType.getInteger(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("NukeMk2 TNT fuse set to " + NukeMk2Shot.TNT_FUSE), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("nukemk6")
                                            .then(CommandManager.literal("OFFSET_HEIGHT")
                                                    .then(CommandManager.argument("value", IntegerArgumentType.integer(0))
                                                            .executes(ctx -> {
                                                                NukeMk6Shot.OFFSET_HEIGHT = IntegerArgumentType.getInteger(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("NukeMk6 offset height set to " + NukeMk6Shot.OFFSET_HEIGHT), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            .then(CommandManager.literal("TNT_FUSE")
                                                    .then(CommandManager.argument("value", IntegerArgumentType.integer(0))
                                                            .executes(ctx -> {
                                                                NukeMk6Shot.TNT_FUSE = IntegerArgumentType.getInteger(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("NukeMk6 TNT fuse set to " + NukeMk6Shot.TNT_FUSE), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                    )

                                    .then(CommandManager.literal("stab")
                                            .then(CommandManager.literal("DEPTH")
                                                    .then(CommandManager.argument("value", IntegerArgumentType.integer(1))
                                                            .executes(ctx -> {
                                                                StabShot.DEPTH = IntegerArgumentType.getInteger(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("Stab depth set to " + StabShot.DEPTH), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            .then(CommandManager.literal("OFFSET")
                                                    .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0))
                                                            .executes(ctx -> {
                                                                StabShot.OFFSET = DoubleArgumentType.getDouble(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("Stab offset set to " + StabShot.OFFSET), false);
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            .then(CommandManager.literal("AMOUNT_PER_PIECE")
                                                    .then(CommandManager.argument("value", IntegerArgumentType.integer(1))
                                                            .executes(ctx -> {
                                                                StabShot.AMOUNT_PER_PIECE = IntegerArgumentType.getInteger(ctx, "value");
                                                                ctx.getSource().sendFeedback(() -> Text.literal("Stab amount per piece set to " + StabShot.AMOUNT_PER_PIECE), false);
                                                                return 1;
                                                            })
                                                    )
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

        if (size > 32) {
            if (ORBITAL_CHAT_ANNOUNCEMENT) {
                ctx.getSource().sendError(Text.literal("Warning: Size capped at 32 to prevent server issues"));
            }
            size = 32;
        }

        for (int i = 0; i < amount; i++) {
            ItemStack rod = new ItemStack(Items.FISHING_ROD, 1);
            rod.setDamage(rod.getMaxDamage() - 1);
            OrbitalRodUtil.tag(rod, shotId, delay, size, null, player.getUuidAsString());

            String displayName = getNukeDisplayName(shotId);
            Text rodName;

            if (ADVANCED_ROD_NAMES) {
                rodName = Text.literal(shotId + " " + delay + " " + size).styled(style -> style.withItalic(true));
            } else {
                rodName = Text.literal(displayName + " shot").styled(style -> style.withItalic(true));
            }

            if (FANCY_RODS) {
                rodName = rodName.copy().styled(style -> style.withBold(true).withColor(0xFFD700));
                rod.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            }

            rod.set(DataComponentTypes.CUSTOM_NAME, rodName);
            player.giveItemStack(rod);
        }

        if (ORBITAL_CHAT_ANNOUNCEMENT) {
            int finalAmount = amount;
            ctx.getSource().sendFeedback(() -> Text.literal("Gave " + finalAmount + "x " + shotId + " orbital rod(s)"), false);
        }
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

            String displayName = getNukeDisplayName(shotId);
            Text rodName;

            if (ADVANCED_ROD_NAMES) {
                rodName = Text.literal(shotId + " " + delay + " " + "0").styled(style -> style.withItalic(true));
            } else {
                rodName = Text.literal(displayName + " shot").styled(style -> style.withItalic(true));
            }

            if (FANCY_RODS) {
                rodName = rodName.copy().styled(style -> style.withBold(true).withColor(0xFFD700));
                rod.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            }

            rod.set(DataComponentTypes.CUSTOM_NAME, rodName);

            player.giveItemStack(rod);
        }

        if (ORBITAL_CHAT_ANNOUNCEMENT) {
            int finalAmount = amount;
            ctx.getSource().sendFeedback(() -> Text.literal("Gave " + finalAmount + "x " + shotId + " orbital rod(s)"), false);
        }
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

        if (size > 32) {
            if (ORBITAL_CHAT_ANNOUNCEMENT) {
                ctx.getSource().sendError(Text.literal("Warning: Size capped at 32 to prevent server issues"));
            }
            size = 32;
        }

        int finalSize = size;
        StrikeScheduler.schedule(delay, () -> {
            shot.fire(ctx.getSource().getWorld(), pos.toCenterPos(), finalSize);
        });

        if (ORBITAL_CHAT_ANNOUNCEMENT) {
            ctx.getSource().sendFeedback(() -> Text.literal("Orbital strike incoming..."), false);
        }
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

        if (ORBITAL_CHAT_ANNOUNCEMENT) {
            ctx.getSource().sendFeedback(() -> Text.literal("Orbital strike incoming..."), false);
        }
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

        if (size > 32) {
            if (ORBITAL_CHAT_ANNOUNCEMENT) {
                ctx.getSource().sendError(Text.literal("Warning: Size capped at 32 to prevent server issues"));
            }
            size = 32;
        }

        int finalSize = size;
        StrikeScheduler.schedule(delay, () -> {
            shot.fire(ctx.getSource().getWorld(), hit.getBlockPos().toCenterPos(), finalSize);
        });

        if (ORBITAL_CHAT_ANNOUNCEMENT) {
            ctx.getSource().sendFeedback(() -> Text.literal("Orbital strike incoming..."), false);
        }
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

        if (ORBITAL_CHAT_ANNOUNCEMENT) {
            ctx.getSource().sendFeedback(() -> Text.literal("Orbital strike incoming..."), false);
        }
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

        if (size > 32) {
            if (ORBITAL_CHAT_ANNOUNCEMENT) {
                ctx.getSource().sendError(Text.literal("Warning: Size capped at 32 to prevent server issues"));
            }
            size = 32;
        }

        ItemStack rod = new ItemStack(Items.FISHING_ROD, 1);
        rod.setDamage(rod.getMaxDamage() - 1);
        OrbitalRodUtil.tag(rod, shotId, delay, size, pos, player.getUuidAsString());

        String displayName = getNukeDisplayName(shotId);
        Text rodName;

        if (ADVANCED_ROD_NAMES) {
            rodName = Text.literal(shotId + " " + delay + " " + size).styled(style -> style.withItalic(true));
        } else {
            rodName = Text.literal(displayName + " shot").styled(style -> style.withItalic(true));
        }

        if (FANCY_RODS) {
            rodName = rodName.copy().styled(style -> style.withBold(true).withColor(0xFFD700));
            rod.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        rod.set(DataComponentTypes.CUSTOM_NAME, rodName);

        player.giveItemStack(rod);

        if (ORBITAL_CHAT_ANNOUNCEMENT) {
            ctx.getSource().sendFeedback(() -> Text.literal("Gave fixed-target " + shotId + " orbital rod"), false);
        }
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

        String displayName = getNukeDisplayName(shotId);
        Text rodName;

        if (ADVANCED_ROD_NAMES) {
            rodName = Text.literal(shotId + " " + delay + " " + "no size :(").styled(style -> style.withItalic(true));
        } else {
            rodName = Text.literal(displayName + " shot").styled(style -> style.withItalic(true));
        }

        if (FANCY_RODS) {
            rodName = rodName.copy().styled(style -> style.withBold(true).withColor(0xFFD700));
            rod.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        rod.set(DataComponentTypes.CUSTOM_NAME, rodName);

        player.giveItemStack(rod);

        if (ORBITAL_CHAT_ANNOUNCEMENT) {
            ctx.getSource().sendFeedback(() -> Text.literal("Gave fixed-target " + shotId + " orbital rod"), false);
        }
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