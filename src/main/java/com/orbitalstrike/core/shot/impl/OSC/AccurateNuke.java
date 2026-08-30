package com.orbitalstrike.core.shot.impl.OSC;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class AccurateNuke implements OrbitalShot {

    public static int Y_OFFSET = 120;
    public static int ACCELERATOR_COUNT = 1;
    public static int ACCELERATOR_FUSE = 65;
    public static int NUKE_COUNT = 48;
    public static int NUKE_ADDITIVE = 16;
    public static int NUKE_FUSE = 80;
    public static int SWING_COUNT = 9;
    public static int SWING_FUSE = 64;
    public static double SPREAD = 0.1;

    @Override
    public String id() {
        return "accuratenuke";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {

        if (size > 32) size = 32;
        int RINGS = Math.max(size, 1);

        for (int i = 0; i < RINGS; i++) {
            int nukeCountThisRing = NUKE_COUNT + i * NUKE_ADDITIVE;

            for (int bm = 0; bm < ACCELERATOR_COUNT; bm++) {
                TntEntity accelerator = new TntEntity(world, pos.x, pos.y + Y_OFFSET, pos.z, null);
                accelerator.setFuse(ACCELERATOR_FUSE);
                accelerator.setVelocity(0.0, 0.0, 0.0);
                world.spawnEntity(accelerator);
            }

            for (int am = 0; am < nukeCountThisRing; am++) {
                double angle = world.random.nextDouble() * Math.PI * 2;
                double radius = Math.sqrt(world.random.nextDouble()) * SPREAD;

                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;

                TntEntity nuke = new TntEntity(world, pos.x + offsetX, pos.y + Y_OFFSET, pos.z + offsetZ, null);
                nuke.setFuse(NUKE_FUSE);
                nuke.setVelocity(0.0, 0.0, 0.0);
                world.spawnEntity(nuke);
            }
        }
        for (int bm = 0; bm < SWING_COUNT; bm++) {
            TntEntity swing = new TntEntity(world, pos.x, pos.y + Y_OFFSET, pos.z, null);
            swing.setFuse(SWING_FUSE);
            swing.setVelocity(0.0, 0.0, 0.0);
            world.spawnEntity(swing);
        }
    }
}