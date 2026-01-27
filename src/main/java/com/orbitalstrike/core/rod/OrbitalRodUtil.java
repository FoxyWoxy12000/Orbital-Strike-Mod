package com.orbitalstrike.core.rod;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.util.math.BlockPos;

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
        return nbt.getString("OrbitalShot").orElse("");
    }

    public static int getDelay(ItemStack stack) {
        NbtCompound nbt = get(stack);
        if (nbt == null) return 0;
        return nbt.getInt("OrbitalDelay").orElse(0);
    }

    public static int getSize(ItemStack stack) {
        NbtCompound nbt = get(stack);
        if (nbt == null) return 0;
        return nbt.getInt("OrbitalSize").orElse(0);
    }

    public static String getOwner(ItemStack stack) {
        NbtCompound nbt = get(stack);
        if (nbt == null) return "";
        return nbt.getString("OrbitalOwner").orElse("");
    }

    public static BlockPos getFixedPos(ItemStack stack) {
        NbtCompound nbt = get(stack);
        if (nbt == null || !nbt.contains("FixedX")) return null;

        int x = nbt.getInt("FixedX").orElse(0);
        int y = nbt.getInt("FixedY").orElse(0);
        int z = nbt.getInt("FixedZ").orElse(0);

        return new BlockPos(x, y, z);
    }

    public static void tag(ItemStack stack, String shot, int delay, int size, BlockPos fixedPos, String owner) {
        NbtCompound nbt = get(stack);
        if (nbt == null) nbt = new NbtCompound();

        nbt.putString("OrbitalShot", shot);
        nbt.putInt("OrbitalDelay", delay);
        nbt.putInt("OrbitalSize", size);
        nbt.putString("OrbitalOwner", owner);

        if (fixedPos != null) {
            nbt.putInt("FixedX", fixedPos.getX());
            nbt.putInt("FixedY", fixedPos.getY());
            nbt.putInt("FixedZ", fixedPos.getZ());
        }

        set(stack, nbt);
    }
}