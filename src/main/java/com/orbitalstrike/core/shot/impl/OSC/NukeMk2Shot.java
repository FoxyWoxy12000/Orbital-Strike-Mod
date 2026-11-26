package com.orbitalstrike.core.shot.impl.OSC;

import com.orbitalstrike.core.shot.OrbitalShot;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import com.orbitalstrike.core.command.OrbitalCommand;
import com.orbitalstrike.core.util.StrikeScheduler;

public class NukeMk2Shot implements OrbitalShot {

    public static double RING_BASE_SPEED = 0.025;
    public static double RING_SPEED_STEP_CONE = 0.1;
    public static double RING_SPEED_STEP_OUTER = 0.225;
    public static double BATCH2_BASE_SPEED = 0.4;
    public static double BATCH2_SPEED_STEP = 0.35;
    public static double CONE_BASE_DOWN_SPEED = 0.4;
    public static double CONE_DOWN_STEP = 0.35;
    public static int TNT_FUSE = 80;

    // nuke timing
    public static int NUKE_RELEASE_DELAY = 50;   // ticks the cloud spreads before the nuke spawns
    public static int NUKE_FUSE = 30;            // fuse of the nuke after it spawns (old behavior: 80 - 50)
    public static int NUKE_SPAWN_HEIGHT = 114;   // ~150 minus the ~36 blocks it used to free-fall before release

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
        double coneHeight = 150 + (size - 10) * 12.5;
        double spawnY = pos.y + coneHeight;

        int coneCutoff = totalRings / 2;
        double currentRingSpeed = RING_BASE_SPEED;

        // ---- mushroom cloud: spawns immediately ----
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

        // ---- nuke: spawns after the cloud has spread ----
        if (OrbitalCommand.MK2_CLOUD_ONLY) return;

        StrikeScheduler.schedule(NUKE_RELEASE_DELAY, () -> spawnNuke(world, pos, totalRings));
    }

    private void spawnNuke(ServerWorld world, Vec3d pos, int batch2Rings) {
        double startVY = -7.0;
        double endVY = -7.5;
        double batch2SpawnY = pos.y + NUKE_SPAWN_HEIGHT;

        for (int r = 1; r <= batch2Rings; r++) {
            double progress = (double) r / batch2Rings;
            double speed = BATCH2_BASE_SPEED + r * BATCH2_SPEED_STEP;
            double currentVY = outQuad(startVY, endVY, progress);
            int b2Count = 80 + (r - 1);

            for (int i = 0; i < b2Count; i++) {
                double angle = world.random.nextDouble() * Math.PI * 2.0;
                double vX = Math.cos(angle) * speed;
                double vZ = Math.sin(angle) * speed;

                TntEntity tnt = new TntEntity(world, pos.x, batch2SpawnY, pos.z, null);
                tnt.setFuse(NUKE_FUSE);
                tnt.setVelocity(vX, currentVY, vZ);
                world.spawnEntity(tnt);
            }
        }
    }

    private double outQuad(double start, double end, double progress) {
        return start + (end - start) * (1.0 - (1.0 - progress) * (1.0 - progress));
    }
}