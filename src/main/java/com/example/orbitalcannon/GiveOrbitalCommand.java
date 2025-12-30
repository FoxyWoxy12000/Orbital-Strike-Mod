package com.example.orbitalcannon;
import net.minecraft.server.level.ServerLevel;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;

public class GiveOrbitalCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("giveorbital")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.players())
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
                                                .then(Commands.argument("delay", IntegerArgumentType.integer(0))
                                                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                                .executes(ctx -> {
                                                                    String type = StringArgumentType.getString(ctx, "type").toLowerCase();
                                                                    int value = IntegerArgumentType.getInteger(ctx, "value");
                                                                    int delay = IntegerArgumentType.getInteger(ctx, "delay");
                                                                    int count = IntegerArgumentType.getInteger(ctx, "count");

                                                                    CommandSourceStack source = ctx.getSource();
                                                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");

                                                                    for (ServerPlayer player : players) {
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

                                                                            if (player.level() instanceof ServerLevel sl) {
                                                                                OrbitalEvents.applyRodVisuals(rod, sl);
                                                                            }
                                                                        }
                                                                    }

                                                                    source.sendSuccess(() -> Component.literal("Given " + count + " orbital rod(s) to " + players.size() + " player(s): " + type), true);
                                                                    return 1;
                                                                })))))));
    }
}
