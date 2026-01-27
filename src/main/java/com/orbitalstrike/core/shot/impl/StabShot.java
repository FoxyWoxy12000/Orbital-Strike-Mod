package com.orbitalstrike.core.shot.impl;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.TntEntity;

public class StabShot implements OrbitalShot {

    @Override
    public String id() {
        return "stab";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {
        for (int y = 319; y >= -64; y -= 3) {
            TntEntity tnt = new TntEntity(world, pos.x, y, pos.z, null);
            tnt.setFuse(0);
            world.spawnEntity(tnt);
        }
    }
}
