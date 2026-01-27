package com.orbitalstrike.core.rod;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class OrbitalRodUtil {

    public static boolean isOrbitalRod(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().contains("OrbitalShot");
    }

    public static String getShot(ItemStack stack) {
        return stack.getNbt().getString("OrbitalShot");
    }

    public static int getDelay(ItemStack stack) {
        return stack.getNbt().getInt("OrbitalDelay");
    }

    public static String getOwner(ItemStack stack) {
        return stack.getNbt().getString("OrbitalOwner");
    }

    public static void tag(ItemStack stack, String shot, int delay, String owner) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putString("OrbitalShot", shot);
        nbt.putInt("OrbitalDelay", delay);
        nbt.putString("OrbitalOwner", owner);
    }
}
