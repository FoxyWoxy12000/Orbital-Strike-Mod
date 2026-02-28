package com.orbitalstrike.core.shot.impl;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import com.orbitalstrike.core.command.OrbitalCommand;

import java.util.Random;

public class StarStabShot implements OrbitalShot {

    private static final Random RANDOM = new Random();

    public static int VERTICAL_DEPTH = 3;
    public static double OFFSET = 0.5;
    public static int AMOUNT_PER_PIECE = 3;
    public static double DENSITY = 4.0;
    public static double OUTER_RADIUS_PER_SIZE = 6.0;
    public static double INNER_RADIUS_PER_SIZE = 2.4;
    public static int STAR_POINTS = 5;
    public static int START_Y = 319;
    public static int END_Y = -64;

    @Override
    public String id() {
        return "starstab";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {
        if (OrbitalCommand.STRAIGHT_STAR_STAB_SHOT) {
            AMOUNT_PER_PIECE = 1;
            VERTICAL_DEPTH = 1;
                    OFFSET = 0;
        } else {
            AMOUNT_PER_PIECE = 3;
            VERTICAL_DEPTH = 3;
            OFFSET = 0.5;
        }
        double maxOuter = Math.max(size, 1) * OUTER_RADIUS_PER_SIZE;
        double maxInner = Math.max(size, 1) * INNER_RADIUS_PER_SIZE;

        fireVerticalColumn(world, pos.x, pos.z);

        int layers = (int)(maxOuter / DENSITY);

        for (int l = 1; l <= layers; l++) {
            double scale = (double)l / layers;
            double currentOuter = maxOuter * scale;
            double currentInner = maxInner * scale;

            for (int i = 0; i < STAR_POINTS; i++) {
                double angleOuter = (Math.PI / 2) + (Math.PI * 2) * i / STAR_POINTS;
                double angleInner = (Math.PI / 2) + (Math.PI * 2) * i / STAR_POINTS + Math.PI / STAR_POINTS;
                double angleNextOuter = (Math.PI / 2) + (Math.PI * 2) * (i + 1) / STAR_POINTS;

                Vec3d pOuter = new Vec3d(
                        pos.x + Math.cos(angleOuter) * currentOuter,
                        pos.y,
                        pos.z + Math.sin(angleOuter) * currentOuter
                );
                Vec3d pInner = new Vec3d(
                        pos.x + Math.cos(angleInner) * currentInner,
                        pos.y,
                        pos.z + Math.sin(angleInner) * currentInner
                );
                Vec3d pNextOuter = new Vec3d(
                        pos.x + Math.cos(angleNextOuter) * currentOuter,
                        pos.y,
                        pos.z + Math.sin(angleNextOuter) * currentOuter
                );

                addInterpolatedPoints(world, pOuter, pInner);
                addInterpolatedPoints(world, pInner, pNextOuter);
            }
        }
    }

    private void addInterpolatedPoints(ServerWorld world, Vec3d start, Vec3d end) {
        double distance = Math.sqrt(
                Math.pow(end.x - start.x, 2) +
                        Math.pow(end.z - start.z, 2)
        );
        int steps = (int)Math.ceil(distance / DENSITY);

        for (int i = 0; i <= steps; i++) {
            double t = (double)i / steps;
            double x = start.x + (end.x - start.x) * t;
            double z = start.z + (end.z - start.z) * t;

            fireVerticalColumn(world, x, z);
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