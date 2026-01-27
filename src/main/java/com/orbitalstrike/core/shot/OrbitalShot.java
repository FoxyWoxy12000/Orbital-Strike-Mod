package com.orbitalstrike.core.shot;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public interface OrbitalShot {

    String id();

    void fire(ServerWorld world, Vec3d target, int delay, String owner);
}
