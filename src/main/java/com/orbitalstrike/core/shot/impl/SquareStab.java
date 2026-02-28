package com.orbitalstrike.core.shot.impl;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class SquareStab implements OrbitalShot {

    private static final Random RANDOM = new Random();

    public static int DEPTH = 3;
    public static double OFFSET = 0.5;
    public static int AMOUNT_PER_PIECE = 3;
    public static double SPACING = 5.0;
    public static int GRID_SIZE = 5;

    @Override
    public String id() {
        return "squarestab";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {
        int halfGrid = GRID_SIZE / 2;

        for (int i = -halfGrid; i <= halfGrid; i++) {
            for (int j = -halfGrid; j <= halfGrid; j++) {
                double shotX = pos.x + i * SPACING;
                double shotZ = pos.z + j * SPACING;

                fireVerticalColumn(world, shotX, shotZ);
            }
        }
    }

    private void fireVerticalColumn(ServerWorld world, double x, double z) {
        for (int y = 319; y >= -64; y -= DEPTH) {
            for (int i = 0; i < AMOUNT_PER_PIECE; i++) {
                double offsetX = (RANDOM.nextDouble() - 0.5) * OFFSET;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * OFFSET;

                TntEntity tnt = new TntEntity(
                        world,
                        x + offsetX,
                        y,
                        z + offsetZ,
                        null
                );
                tnt.setFuse(0);
                world.spawnEntity(tnt);
            }
        }
    }
}