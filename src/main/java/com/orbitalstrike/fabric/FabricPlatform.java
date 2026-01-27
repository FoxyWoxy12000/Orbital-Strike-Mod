package com.orbitalstrike.fabric;

import com.orbitalstrike.platform.OrbitalPlatform;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class FabricPlatform implements OrbitalPlatform {

    @Override
    public void spawnPrimedTnt(ServerWorld world, Vec3d pos, int fuse) {
        TntEntity tnt = new TntEntity(world, pos.x, pos.y, pos.z, null);
        tnt.setFuse(fuse);
        world.spawnEntity(tnt);
    }
}
