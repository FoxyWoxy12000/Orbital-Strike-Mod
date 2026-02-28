package com.orbitalstrike.core.shot.impl;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.fabricmc.loader.impl.metadata.AbstractModMetadata;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import com.orbitalstrike.core.command.OrbitalCommand;

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
        if (OrbitalCommand.STRAIGHT_STAB_SHOT)
        {
            DEPTH = 1;
            OFFSET = 0;
            AMOUNT_PER_PIECE = 1;
        } else {
            DEPTH = 3;
            OFFSET = 0.5;
            AMOUNT_PER_PIECE = 3;
        }

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