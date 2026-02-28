package com.orbitalstrike.core.shot.impl.OSC;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NukeMk6Shot implements OrbitalShot {

    public static int OFFSET_HEIGHT = 150;
    public static double BASE_SPEED = 0.15;
    public static double SPEED_STEP = 0.5;
    public static double START_VY = -7.0;
    public static double END_VY = -7.75;
    public static int TNT_FUSE = 60;

    @Override
    public String id() {
        return "nukemk6";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {
        if (size > 32) {
            size = 32;
        }

        double spawnY = pos.y + OFFSET_HEIGHT;
        int totalRings = Math.max(size, 2);
        Map<UUID, Vec3d> vectors = new HashMap<>();

        for (int r = 1; r <= totalRings; r++) {
            double ringSpeed = BASE_SPEED + (r - 1) * SPEED_STEP;
            int tntCount = r == totalRings ? (15 + (r - 2) * 15) * 2 : 15 + (r - 1) * 15;
            double progress = (double)r / totalRings;
            double vY = outQuad(START_VY, END_VY, progress);

            for (int i = 0; i < tntCount; i++) {
                double angle = world.random.nextDouble() * Math.PI * 2.0;
                double vX = Math.cos(angle) * ringSpeed;
                double vZ = Math.sin(angle) * ringSpeed;

                TntEntity tnt = new TntEntity(world, pos.x, spawnY, pos.z, null);
                tnt.setFuse(TNT_FUSE);
                world.spawnEntity(tnt);
                vectors.put(tnt.getUuid(), new Vec3d(vX, vY, vZ));
            }
        }

        world.getServer().execute(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            applyVectors(world, vectors);
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