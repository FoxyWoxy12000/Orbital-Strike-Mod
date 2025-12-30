package com.example.orbitalcannon;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GiveOrbitalStrikeRodCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("giveorbitalstrikerod")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests((ctx, builder) -> GiveOrbitalStrikeRodCommandSuggestions.suggestTypes(ctx, builder))
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1))
                                                .then(Commands.argument("delay", IntegerArgumentType.integer(0))
                                                        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                                                .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                                        .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                                                .executes(ctx -> execute(ctx))))))))));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player target = EntityArgument.getPlayer(ctx, "target");
        String type = StringArgumentType.getString(ctx, "type").toLowerCase();
        int value = IntegerArgumentType.getInteger(ctx, "value");
        int delay = IntegerArgumentType.getInteger(ctx, "delay");
        double x = DoubleArgumentType.getDouble(ctx, "x");
        double y = DoubleArgumentType.getDouble(ctx, "y");
        double z = DoubleArgumentType.getDouble(ctx, "z");

        CommandSourceStack source = ctx.getSource();

        boolean wemmbu = target.level().getGameRules().getBoolean(OrbitalGameRules.ORBITAL_WEMMBU_ONLY);
        if (wemmbu && !(type.equals("nuke") || type.equals("stab"))) {
            source.sendFailure(Component.literal("Only nuke/stab allowed while orbitalWemmbuOnly is true"));
            return 0;
        }

        ItemStack rod = new ItemStack(Items.FISHING_ROD);
        rod.setDamageValue(rod.getMaxDamage() - 1);

        var nbt = rod.getOrCreateTag();
        nbt.putBoolean("orbital_special", true);
        nbt.putString("orbital_type", type);
        nbt.putInt("orbital_value", value);
        nbt.putInt("orbital_delay", delay);
        nbt.putDouble("orbital_x", x);
        nbt.putDouble("orbital_y", y);
        nbt.putDouble("orbital_z", z);
        nbt.putBoolean("orbital_strike_fixedpos", true);
        nbt.putBoolean("orbital_oneuse", true);

        if (!target.getInventory().add(rod)) {
            target.drop(rod, false);
        }

        if (target.level() instanceof ServerLevel serverLevel) {
            OrbitalEvents.applyRodVisuals(rod, serverLevel);
        }

        source.sendSuccess(() -> Component.literal(
                "Given orbital strike rod (" + type + ") to " +
                        target.getName().getString() +
                        " targeting " + x + " " + y + " " + z +
                        " (value=" + value + ", delay=" + delay + ")"), true);

        return 1;
    }

    private static class GiveOrbitalStrikeRodCommandSuggestions {
        public static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestTypes(
                CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
            boolean wemmbu = false;
            try {
                wemmbu = ctx.getSource().getLevel().getGameRules().getBoolean(OrbitalGameRules.ORBITAL_WEMMBU_ONLY);
            } catch (Exception ignored) {}
            if (wemmbu) {
                return SharedSuggestionProvider.suggest(new String[]{"nuke", "stab"}, builder);
            } else {
                return SharedSuggestionProvider.suggest(new String[]{"nuke", "stab", "arrow", "worlddestroyer"}, builder);
            }
        }
    }
}
