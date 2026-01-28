package com.orbitalstrike.core.shot.impl;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class NukeMk4Shot implements OrbitalShot {

    private static final Random RANDOM = new Random();

    public static int OFFSET_HEIGHT = 100;
    public static double INNER_RING_MULTIPLIER = 14.0;
    public static double OUTER_RING_MULTIPLIER = 3.0;
    public static double IMPERFECTION_PERCENT = 0.25;

    @Override
    public String id() {
        return "nuke";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {
        if (size > 32) {
            size = 32;
        }

        double spawnY = pos.y + OFFSET_HEIGHT;

        TntEntity center = new TntEntity(world, pos.x, spawnY, pos.z, null);
        center.setFuse(110);
        center.setVelocity(0, -0.5, 0);
        world.spawnEntity(center);

        int rings = size;
        for (int ring = 1; ring <= rings; ring++) {
            double targetRadius = ring * OUTER_RING_MULTIPLIER;
            int tntInThisRing = (int)(INNER_RING_MULTIPLIER * ring);

            for (int i = 0; i < tntInThisRing; i++) {
                if (RANDOM.nextDouble() < IMPERFECTION_PERCENT) continue;

                double angle = (2 * Math.PI * i) / tntInThisRing;

                double offsetX = Math.cos(angle) * 0.3;
                double offsetZ = Math.sin(angle) * 0.3;

                double velocityX = Math.cos(angle) * (targetRadius / 25.0);
                double velocityZ = Math.sin(angle) * (targetRadius / 25.0);

                TntEntity tnt = new TntEntity(world, pos.x + offsetX, spawnY, pos.z + offsetZ, null);
                tnt.setFuse(110);
                tnt.setVelocity(velocityX, -0.5, velocityZ);
                world.spawnEntity(tnt);
            }
        }
    }
}