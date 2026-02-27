package com.orbitalstrike.core.shot.impl;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NukeMk4Shot implements OrbitalShot {

    public static int OFFSET_HEIGHT = 70; //can be set to 1) 100 or 2) 70
    public static double INNER_RING_MULTIPLIER = 70.0;
    public static double OUTER_RING_MULTIPLIER = 0.15;
    public static double IMPERFECTION_PERCENT = 0.0;
    public static int TNT_FUSE = 80; // can be set to 1) 110 or 2) 80

    @Override
    public String id() {
        return "nukeMk4";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {
        if (size > 32) {
            size = 32;
        }

        double spawnY = pos.y + OFFSET_HEIGHT;
        Map<UUID, Vec3d> delayedVectors = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            TntEntity tnt = new TntEntity(world, pos.x, spawnY, pos.z, null);
            tnt.setFuse(TNT_FUSE);
            world.spawnEntity(tnt);
            delayedVectors.put(tnt.getUuid(), new Vec3d(0.0, 0.0, 0.0));
        }

        int totalRings = Math.max(size, 1);
        double baseSpeed = 0.025;

        for (int ring = 1; ring <= totalRings; ring++) {
            double ringSpeed = baseSpeed + ring * OUTER_RING_MULTIPLIER;
            int tntCount = (int)(INNER_RING_MULTIPLIER + (ring - 1) * 1);

            for (int i = 0; i < tntCount; i++) {
                if (world.random.nextDouble() < IMPERFECTION_PERCENT) continue;

                double angle = world.random.nextDouble() * Math.PI * 2.0;
                double vX = Math.cos(angle) * ringSpeed;
                double vZ = Math.sin(angle) * ringSpeed;

                TntEntity tnt = new TntEntity(world, pos.x, spawnY, pos.z, null);
                tnt.setFuse(TNT_FUSE);
                world.spawnEntity(tnt);
                delayedVectors.put(tnt.getUuid(), new Vec3d(vX, 0.0, vZ));
            }
        }

        world.getServer().execute(() -> {
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            applyVectors(world, delayedVectors);
        });
    }

    private void applyVectors(ServerWorld world, Map<UUID, Vec3d> delayedVectors) {
        for (Map.Entry<UUID, Vec3d> entry : delayedVectors.entrySet()) {
            Entity entity = world.getEntity(entry.getKey());
            if (entity instanceof TntEntity) {
                TntEntity tnt = (TntEntity) entity;
                Vec3d velocity = entry.getValue();
                tnt.setVelocity(velocity.x, -0.5, velocity.z);
            }
        }
    }
}