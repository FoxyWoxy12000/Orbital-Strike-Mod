package com.orbitalstrike.core.shot.impl;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NukeMk2Shot implements OrbitalShot {

    public static int OFFSET_HEIGHT = 150;
    public static double RING_BASE_SPEED = 0.025;
    public static double RING_SPEED_STEP_CONE = 0.1;
    public static double RING_SPEED_STEP_OUTER = 0.225;
    public static double BATCH2_BASE_SPEED = 0.4;
    public static double BATCH2_SPEED_STEP = 0.35;
    public static double CONE_BASE_DOWN_SPEED = 0.4;
    public static double CONE_DOWN_STEP = 0.35;

    @Override
    public String id() {
        return "nukeMk2";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {
        if (size > 32) {
            size = 32;
        }

        double spawnY = pos.y + OFFSET_HEIGHT;
        int totalRings = Math.max(size, 1);
        int coneCutoff = totalRings / 2;
        double currentRingSpeed = RING_BASE_SPEED;

        for (int r = 1; r <= totalRings; r++) {
            boolean inCone = r <= coneCutoff;
            currentRingSpeed += inCone ? RING_SPEED_STEP_CONE : RING_SPEED_STEP_OUTER;
            int b1Count = 70 + (r - 1) * 5;
            double b1Vert = inCone ? -(CONE_BASE_DOWN_SPEED + (r - 1) * CONE_DOWN_STEP) : 0.0;

            for (int i = 0; i < b1Count; i++) {
                double angle = world.random.nextDouble() * Math.PI * 2.0;
                double vX = Math.cos(angle) * currentRingSpeed;
                double vZ = Math.sin(angle) * currentRingSpeed;

                TntEntity tnt = new TntEntity(world, pos.x, spawnY, pos.z, null);
                tnt.setFuse(80);
                tnt.setVelocity(vX, b1Vert, vZ);
                world.spawnEntity(tnt);
            }
        }

        Map<UUID, Vec3d> batch2Vectors = new HashMap<>();
        int batch2Rings = (int)(totalRings * 1.75);
        double startVY = -7.0;
        double endVY = -7.5;

        for (int r = 1; r <= batch2Rings; r++) {
            double progress = (double)r / batch2Rings;
            double speed = BATCH2_BASE_SPEED + r * BATCH2_SPEED_STEP;
            double currentVY = outQuad(startVY, endVY, progress);
            int b2Count = 80 + (r - 1);

            for (int i = 0; i < b2Count; i++) {
                double angle = world.random.nextDouble() * Math.PI * 2.0;
                double vX = Math.cos(angle) * speed;
                double vZ = Math.sin(angle) * speed;

                TntEntity tnt = new TntEntity(world, pos.x, spawnY, pos.z, null);
                tnt.setFuse(80);
                world.spawnEntity(tnt);
                batch2Vectors.put(tnt.getUuid(), new Vec3d(vX, currentVY, vZ));
            }
        }

        world.getServer().execute(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            applyVectors(world, batch2Vectors);
        });
    }

    private double outQuad(double start, double end, double progress) {
        return start + (end - start) * (1.0 - (1.0 - progress) * (1.0 - progress));
    }

    private void applyVectors(ServerWorld world, Map<UUID, Vec3d> vectors) {
        for (Map.Entry<UUID, Vec3d> entry : vectors.entrySet()) {
            Entity entity = world.getEntity(entry.getKey());
            if (entity instanceof TntEntity) {
                TntEntity tnt = (TntEntity) entity;
                Vec3d velocity = entry.getValue();
                tnt.setVelocity(velocity.x, velocity.y, velocity.z);
            }
        }
    }
}