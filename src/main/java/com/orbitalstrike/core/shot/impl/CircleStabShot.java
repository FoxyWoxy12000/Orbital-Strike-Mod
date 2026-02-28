package com.orbitalstrike.core.shot.impl;

import com.orbitalstrike.core.command.OrbitalCommand;
import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class CircleStabShot implements OrbitalShot {

    private static final Random RANDOM = new Random();

    public static int VERTICAL_DEPTH = 3;
    public static double OFFSET = 0.5;
    public static int AMOUNT_PER_PIECE = 3;
    public static double DENSITY = 4.0;
    public static double RADIUS_PER_SIZE = 5.0;
    public static int START_Y = 319;
    public static int END_Y = -64;

    @Override
    public String id() {
        return "circlestab";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {
        if (OrbitalCommand.STRAIGHT_CIRCLE_STAB_SHOT) {
            AMOUNT_PER_PIECE = 1;
            VERTICAL_DEPTH = 1;
            OFFSET = 0;
        } else {
            AMOUNT_PER_PIECE = 3;
            VERTICAL_DEPTH = 3;
            OFFSET = 0.5;
        }
        double maxRadius = Math.max(size, 1) * RADIUS_PER_SIZE;

        fireVerticalColumn(world, pos.x, pos.z);

        for (double r = DENSITY; r <= maxRadius; r += DENSITY) {
            double circumference = (Math.PI * 2) * r;
            int points = (int)Math.max(1.0, Math.ceil(circumference / DENSITY));

            for (int i = 0; i < points; i++) {
                double angle = (Math.PI * 2) * i / points;
                double shotX = pos.x + Math.cos(angle) * r;
                double shotZ = pos.z + Math.sin(angle) * r;

                fireVerticalColumn(world, shotX, shotZ);
            }
        }
    }

    private void fireVerticalColumn(ServerWorld world, double x, double z) {
        for (int y = START_Y; y >= END_Y; y -= VERTICAL_DEPTH) {
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