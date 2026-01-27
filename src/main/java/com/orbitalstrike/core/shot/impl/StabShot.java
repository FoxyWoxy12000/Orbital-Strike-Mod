package com.orbitalstrike.core.shot.impl;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class StabShot implements OrbitalShot {

    private static final Random RANDOM = new Random();

    public static int DEPTH = 3;
    public static double OFFSET = 0.5;
    public static int AMOUNT_PER_PIECE = 3;

    @Override
    public String id() {
        return "stab";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {
        for (int y = 319; y >= -64; y -= DEPTH) {
            for (int i = 0; i < AMOUNT_PER_PIECE; i++) {
                double offsetX = (RANDOM.nextDouble() - 0.5) * OFFSET;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * OFFSET;

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