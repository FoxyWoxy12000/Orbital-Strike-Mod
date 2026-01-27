package com.orbitalstrike.platform;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public interface OrbitalPlatform {

    void spawnPrimedTnt(ServerWorld world, Vec3d pos, int fuse);
}
