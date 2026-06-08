package com.orbitalstrike.core.shot.impl.OSC;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class BobmShot implements OrbitalShot {

    public static int BOBM_COUNT = 200;
    public static int BOBM_FUSE = 1;


    @Override
    public String id() {
        return "bobm";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {


        for (int am = 0; am < BOBM_COUNT; am++) {
            TntEntity piercer = new TntEntity(world, pos.x, pos.y, pos.z, null);
            piercer.setFuse(BOBM_FUSE);
            piercer.setVelocity(0.0, 0.0, 0.0);
            world.spawnEntity(piercer);
        }
    }
}