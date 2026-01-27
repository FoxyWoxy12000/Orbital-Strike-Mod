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
    public void fire(ServerWorld world, Vec3d pos, int delay, String owner) {
        int minY = world.getBottomY();
        int maxY = world.getTopYInclusive();

        for (int y = minY; y <= maxY; y += 3) {
            TntEntity tnt = new TntEntity(world, pos.x, y, pos.z, null);
            tnt.setFuse(delay);
            world.spawnEntity(tnt);
        }
    }
}
