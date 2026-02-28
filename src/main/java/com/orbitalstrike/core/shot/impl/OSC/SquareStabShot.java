package com.orbitalstrike.core.shot.impl.OSC;

import com.orbitalstrike.core.shot.OrbitalShot;
import com.orbitalstrike.core.util.StrikeScheduler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SquareStabShot implements OrbitalShot {

    private static final Random RANDOM = new Random();

    public static int VERTICAL_DEPTH = 3;
    public static double OFFSET = 0.5;
    public static int AMOUNT_PER_PIECE = 3;
    public static double SPACING = 5.0;
    public static int START_Y = 319;
    public static int END_Y = -64;
    public static String STAB_STYLE = "GAPPED"; // GAPPED, STRAIGHT, ACCURATE

    @Override
    public String id() {
        return "squarestab";
    }

    @Override
    public void fire(ServerWorld world, Vec3d pos, int size) {
        int gridSize = Math.max(size, 1);
        int halfGrid = gridSize / 2;

        List<Vec3d> positions = new ArrayList<>();
        for (int i = -halfGrid; i <= halfGrid; i++) {
            for (int j = -halfGrid; j <= halfGrid; j++) {
                double shotX = pos.x + i * SPACING;
                double shotZ = pos.z + j * SPACING;
                positions.add(new Vec3d(shotX, 0, shotZ));
            }
        }

        spawnColumnsAsync(world, positions, 0);
    }

    private void spawnColumnsAsync(ServerWorld world, List<Vec3d> positions, int index) {
        if (index >= positions.size()) return;

        int batchSize = Math.min(5, positions.size() - index);
        for (int i = 0; i < batchSize; i++) {
            Vec3d position = positions.get(index + i);
            fireVerticalColumn(world, position.x, position.z);
        }

        if (index + batchSize < positions.size()) {
            StrikeScheduler.schedule(1, () -> spawnColumnsAsync(world, positions, index + batchSize));
        }
    }

    private void fireVerticalColumn(ServerWorld world, double x, double z) {
        if (STAB_STYLE.equals("ACCURATE")) {
            fireAccurateColumn(world, x, z);
            return;
        }

        int depth = STAB_STYLE.equals("STRAIGHT") ? 1 : VERTICAL_DEPTH;
        double offset = STAB_STYLE.equals("STRAIGHT") ? 0.0 : OFFSET;
        int amount = STAB_STYLE.equals("STRAIGHT") ? 1 : AMOUNT_PER_PIECE;

        for (int y = START_Y; y >= END_Y; y -= depth) {
            for (int i = 0; i < amount; i++) {
                double offsetX = (RANDOM.nextDouble() - 0.5) * offset;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * offset;

                TntEntity tnt = new TntEntity(world, x + offsetX, y, z + offsetZ, null);
                tnt.setFuse(0);
                world.spawnEntity(tnt);
            }
        }
    }

    private void fireAccurateColumn(ServerWorld world, double x, double z) {
        int pusherY = START_Y;
        int strikeY = START_Y - 3;

        for (int i = 0; i < 3; i++) {
            TntEntity pusher = new TntEntity(world, x, pusherY, z, null);
            pusher.setFuse(0);
            world.spawnEntity(pusher);
        }

        BlockPos currentPos = new BlockPos((int)x, strikeY, (int)z);
        while (currentPos.getY() >= END_Y) {
            BlockState state = world.getBlockState(currentPos);

            if (state.isOf(Blocks.WATER) || state.isOf(Blocks.OBSIDIAN) || state.isOf(Blocks.BEDROCK) ||
                    state.getBlock().getBlastResistance() > 6.0f) {
                break;
            }

            TntEntity tnt = new TntEntity(world, currentPos.getX() + 0.5, currentPos.getY(), currentPos.getZ() + 0.5, null);
            tnt.setFuse(20);
            world.spawnEntity(tnt);

            currentPos = currentPos.down(3);
        }
    }
}