package com.orbitalstrike.core.shot.impl.OSC;

import com.orbitalstrike.core.command.OrbitalCommand;
import com.orbitalstrike.core.shot.OrbitalShot;
import com.orbitalstrike.core.util.TickScheduler;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class NukeMk2Shot implements OrbitalShot {

    public static double RING_BASE_SPEED = 0.025;
    public static double RING_SPEED_STEP_CONE = 0.1;
    public static double RING_SPEED_STEP_OUTER = 0.225;
    public static double CONE_BASE_DOWN_SPEED = 0.4;
    public static double CONE_DOWN_STEP = 0.35;
    public static int TNT_FUSE = 80;
    public static int CLOUD_SPAWN_OFFSET = 150;

    // nuke blast
    public static int NUKE_BLAST_DELAY = 64;
    public static double BATCH2_BASE_SPEED = 0.4;
    public static double BATCH2_SPEED_STEP = 0.35;
    public static double NUKE_START_VY = -5.2;
    public static double NUKE_END_VY = -5.7;
    public static int NUKE_SPAWN_HEIGHT = 130;

    private record Payload(TntEntity tnt, double vx, double vyExtra, double vz) {}

    @Override
    public String id() {
        return "nukemk2";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {
        if (size > 32) {
            size = 32;
        }

        int totalRings = Math.max(size, 1);
        double coneHeight = CLOUD_SPAWN_OFFSET + ((size - 10) * 12.5);
        double spawnY = pos.y + coneHeight;
        double nukeSpawnY = pos.y + NUKE_SPAWN_HEIGHT;

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
                tnt.setFuse(TNT_FUSE);
                tnt.setVelocity(vX, b1Vert, vZ);
                world.spawnEntity(tnt);
            }
        }

        if (OrbitalCommand.MK2_CLOUD_ONLY) return;

        List<Payload> payloads = new ArrayList<>();

        for (int r = 1; r <= totalRings; r++) {
            double progress = (double) r / totalRings;
            double speed = BATCH2_BASE_SPEED + r * BATCH2_SPEED_STEP;
            double vyExtra = outQuad(NUKE_START_VY, NUKE_END_VY, progress);
            int b2Count = 80 + (r - 1);

            for (int i = 0; i < b2Count; i++) {
                double angle = world.random.nextDouble() * Math.PI * 2.0;

                TntEntity tnt = new TntEntity(world, pos.x, nukeSpawnY, pos.z, null);
                tnt.setVelocity(0.0, 0.0, 0.0);
                tnt.setFuse(TNT_FUSE);
                world.spawnEntity(tnt);

                payloads.add(new Payload(tnt,
                        Math.cos(angle) * speed,
                        vyExtra,
                        Math.sin(angle) * speed));
            }
        }

        TickScheduler.schedule(NUKE_BLAST_DELAY, () -> {
            for (Payload p : payloads) {
                if (p.tnt().isRemoved()) continue;
                Vec3d cur = p.tnt().getVelocity();
                p.tnt().setVelocity(p.vx(), cur.y + p.vyExtra(), p.vz());
            }
        });
    }

    private double outQuad(double start, double end, double progress) {
        return start + (end - start) * (1.0 - (1.0 - progress) * (1.0 - progress));
    }
}