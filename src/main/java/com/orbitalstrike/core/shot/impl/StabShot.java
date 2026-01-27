package com.orbitalstrike.core.shot.impl;

import com.orbitalstrike.OrbitalStrike;
import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class StabShot implements OrbitalShot {

    @Override
    public String id() {
        return "stab";
    }

    @Override
    public void fire(ServerWorld world, Vec3d target, int delay, String owner) {
        for (int y = world.getBottomY(); y <= world.getTopY(); y += 3) {
            OrbitalStrike.PLATFORM.spawnPrimedTnt(world, new Vec3d(target.x, y, target.z), 40);
        }
    }
}
