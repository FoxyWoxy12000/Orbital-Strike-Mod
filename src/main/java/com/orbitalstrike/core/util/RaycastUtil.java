package com.orbitalstrike.core.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class RaycastUtil {

    public static BlockHitResult raycast(PlayerEntity player) {
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(player.getRotationVec(1.0f).multiply(512));
        HitResult result = player.getWorld().raycast(
                new RaycastContext(
                        start,
                        end,
                        RaycastContext.ShapeType.OUTLINE,
                        RaycastContext.FluidHandling.NONE,
                        player
                )
        );
        if (result.getType() != HitResult.Type.BLOCK) return null;
        return (BlockHitResult) result;
    }
}
