package com.orbitalstrike.core.rod;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;

import java.util.Optional;

public class OrbitalRodUtil {

    private static NbtCompound get(ItemStack stack) {
        NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
        return comp == null ? null : comp.copyNbt();
    }

    private static void set(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    public static boolean isOrbitalRod(ItemStack stack) {
        NbtCompound nbt = get(stack);
        return nbt != null && nbt.contains("OrbitalShot");
    }

    public static String getShot(ItemStack stack) {
        NbtCompound nbt = get(stack);
        if (nbt == null) return "";
        Optional<String> v = nbt.getString("OrbitalShot");
        return v.orElse("");
    }

    public static int getDelay(ItemStack stack) {
        NbtCompound nbt = get(stack);
        if (nbt == null) return 0;
        Optional<Integer> v = nbt.getInt("OrbitalDelay");
        return v.orElse(0);
    }

    public static String getOwner(ItemStack stack) {
        NbtCompound nbt = get(stack);
        if (nbt == null) return "";
        Optional<String> v = nbt.getString("OrbitalOwner");
        return v.orElse("");
    }

    public static void tag(ItemStack stack, String shot, int delay, String owner) {
        NbtCompound nbt = get(stack);
        if (nbt == null) nbt = new NbtCompound();
        nbt.putString("OrbitalShot", shot);
        nbt.putInt("OrbitalDelay", delay);
        nbt.putString("OrbitalOwner", owner);
        set(stack, nbt);
    }
}
