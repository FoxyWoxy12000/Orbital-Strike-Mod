package com.orbitalstrike.core.shot.impl.OSC;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class AccurateNuke implements OrbitalShot {

    public static int Y_OFFSET = 120;
    public static int ACCELERATOR_COUNT = 1;
    public static int ACCELERATOR_FUSE = 65;
    public static int NUKE_COUNT = 64;
    public static int NUKE_FUSE = 80;
    public static int SWING_COUNT = 9;
    public static int SWING_FUSE = 64;

    @Override
    public String id() {
        return "accuratenuke";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {

        int RINGS = Math.max(size, 1);

        if (size > 32) {
            size = 32;
        }

        for (int i = 0; i < RINGS; i++) {

            for (int bm = 0; bm < ACCELERATOR_COUNT; bm++) {
                TntEntity accelerator = new TntEntity(world, pos.x, pos.y + Y_OFFSET, pos.z, null);
                accelerator.setFuse(ACCELERATOR_FUSE);
                accelerator.setVelocity(0.0, 0.0, 0.0);
                world.spawnEntity(accelerator);
            }

            for (int am = 0; am < NUKE_COUNT; am++) {
                TntEntity nuke = new TntEntity(world, pos.x, pos.y + Y_OFFSET, pos.z, null);
                nuke.setFuse(NUKE_FUSE);
                Vec3d vel = nuke.getVelocity();
                nuke.setVelocity(vel.x, 0.0, vel.z);
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