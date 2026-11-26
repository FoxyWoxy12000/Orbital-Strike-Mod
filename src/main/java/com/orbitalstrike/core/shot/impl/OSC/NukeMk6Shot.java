package com.orbitalstrike.core.shot.impl.OSC;

import com.orbitalstrike.core.shot.OrbitalShot;
import com.orbitalstrike.core.util.TickScheduler;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class NukeMk6Shot implements OrbitalShot {

    public static int Y_OFFSET = 120;
    public static int TNT_FUSE = 80;
    public static int BLAST_DELAY = 64;      // ticks of free fall before the "blast out"

    public static double BASE_SPEED = 0.15;  // horizontal speed of ring 1
    public static double SPEED_STEP = 0.5;   // added per ring
    public static int BASE_COUNT = 15;       // TNT in ring 1
    public static int COUNT_STEP = 15;       // extra TNT per ring

    // extra vertical velocity added on top of the fall speed, ring 1 -> last ring
    public static double START_VY = 0.0;
    public static double END_VY = -0.5;

    private record Payload(TntEntity tnt, double vx, double vyExtra, double vz) {}

    @Override
    public String id() {
        return "nukemk6";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {
        if (size > 32) size = 32;
        int totalRings = Math.max(size, 1);
        double spawnY = pos.y + Y_OFFSET;

        List<Payload> payloads = new ArrayList<>();

        for (int r = 1; r <= totalRings; r++) {
            double ringSpeed = BASE_SPEED + (r - 1) * SPEED_STEP;
            int count = BASE_COUNT + (r - 1) * COUNT_STEP;
            double progress = (double) r / totalRings;
            double vyExtra = outQuad(START_VY, END_VY, progress);

            for (int i = 0; i < count; i++) {
                double angle = world.random.nextDouble() * Math.PI * 2.0;

                TntEntity tnt = new TntEntity(world, pos.x, spawnY, pos.z, null);
                tnt.setVelocity(0.0, 0.0, 0.0);   // no hop, they all start stacked
                tnt.setFuse(TNT_FUSE);
                world.spawnEntity(tnt);

                payloads.add(new Payload(tnt,
                        Math.cos(angle) * ringSpeed,
                        vyExtra,
                        Math.sin(angle) * ringSpeed));
            }
        }

        // payload falls normally, then everything gets "blasted" on the same tick
        TickScheduler.schedule(BLAST_DELAY, () -> {
            System.out.println("[Mk6] blast fired, payloads=" + payloads.size());
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