package com.orbitalstrike.core.rod;

import com.orbitalstrike.core.command.OrbitalCommand;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class RodNameUpdater {

    public static void updateRodName(ItemStack rod, ServerPlayerEntity player) {
        if (!OrbitalRodUtil.isOrbitalRod(rod)) return;

        String shotId = OrbitalRodUtil.getShot(rod);
        int delay = OrbitalRodUtil.getDelay(rod);
        int size = OrbitalRodUtil.getSize(rod);

        String displayName = getShotDisplayName(shotId);
        Text rodName;

        if (OrbitalCommand.ADVANCED_ROD_NAMES) {
            rodName = Text.literal(shotId + " " + delay + " " + size).styled(style -> style.withItalic(true));
        } else {
            rodName = Text.literal(displayName + " shot").styled(style -> style.withItalic(true));
        }

        if (OrbitalCommand.FANCY_RODS) {
            rodName = rodName.copy().styled(style -> style.withBold(true).withColor(0xFFD700));
            rod.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            rod.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
        }

        rod.set(DataComponentTypes.CUSTOM_NAME, rodName);
    }

    private static String getShotDisplayName(String shotId) {
        if (shotId.equals("nukemk4") || shotId.equals("nukemk2") || shotId.equals("nukemk6")) {
            return "nuke";
        }
        if (shotId.equals("stab") || shotId.equals("straightstab") || shotId.equals("accuratestab") ||
                shotId.equals("squarestab") || shotId.equals("circlestab") || shotId.equals("starstab")) {
            return "stab";
        }
        return shotId;
    }

    public static void updateAllPlayerRods(ServerPlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            updateRodName(stack, player);
        }
    }
}