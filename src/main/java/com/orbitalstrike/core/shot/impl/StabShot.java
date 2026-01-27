package com.orbitalstrike.core.shot.impl;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.TntEntity;

import java.util.Random;

public class StabShot implements OrbitalShot {

    private static final Random RANDOM = new Random();

    @Override
    public String id() {
        return "stab";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {
        for (int y = 319; y >= -64; y -= 3) {
            for (int i = 0; i < 3; i++) {
                double offsetX = (RANDOM.nextDouble() - 0.5);
                double offsetZ = (RANDOM.nextDouble() - 0.5);

                TntEntity tnt = new TntEntity(
                        world,
                        pos.x + offsetX,
                        y,
                        pos.z + offsetZ,
                        null
                );
                tnt.setFuse(0);
                world.spawnEntity(tnt);
            }
        }
    }
}