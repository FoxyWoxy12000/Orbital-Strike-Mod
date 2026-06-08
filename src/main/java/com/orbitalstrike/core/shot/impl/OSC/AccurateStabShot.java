package com.orbitalstrike.core.shot.impl.OSC;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class AccurateStabShot implements OrbitalShot {

    public static int PIERCER_COUNT = 200;
    public static int ACCELERATOR_COUNT = 450;
    public static int Y_HEIGHT = 319;
    public static int PERICER_FUSE = 2;
    public static int ACCELERATOR_FUSE = 2;
    public static int TRIGGER_FUSE = 1;
    //public static int REPITIONS = 20;

    @Override
    public String id() {
        return "accuratestab";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {

        //for (int cm = 0; cm < REPITIONS; cm++) {


            for (int bm = 0; bm < ACCELERATOR_COUNT; bm++) {
                TntEntity accelerator = new TntEntity(world, pos.x, Y_HEIGHT, pos.z, null);
                accelerator.setFuse(ACCELERATOR_FUSE);
                accelerator.setVelocity(0.0, 0.0, 0.0);
                world.spawnEntity(accelerator);
            }

                TntEntity trigger = new TntEntity(world, pos.x, Y_HEIGHT, pos.z, null);
                trigger.setFuse(TRIGGER_FUSE);
                trigger.setVelocity(0.0, 0.0, 0.0);
                world.spawnEntity(trigger);

                for (int am = 0; am < PIERCER_COUNT; am++) {
                    TntEntity piercer = new TntEntity(world, pos.x, Y_HEIGHT, pos.z, null);
                    piercer.setFuse(PERICER_FUSE);
                    piercer.setVelocity(0.0, 0.0, 0.0);
                    world.spawnEntity(piercer);
                }

        //}
    }
}