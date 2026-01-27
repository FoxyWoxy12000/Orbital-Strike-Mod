package com.orbitalstrike.mixin;

import com.orbitalstrike.core.rod.OrbitalRodUtil;
import com.orbitalstrike.core.shot.OrbitalShot;
import com.orbitalstrike.core.shot.ShotRegistry;
import com.orbitalstrike.core.util.RaycastUtil;
import com.orbitalstrike.core.util.StrikeScheduler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = user.getStackInHand(hand);

        if (!OrbitalRodUtil.isOrbitalRod(stack)) return;
        if (world.isClient()) {
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) user;
        ServerWorld serverWorld = (ServerWorld) world;
        String owner = OrbitalRodUtil.getOwner(stack);

        if (!owner.equals(player.getUuidAsString())) {
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }

        String shotId = OrbitalRodUtil.getShot(stack);
        int delay = OrbitalRodUtil.getDelay(stack);
        int size = OrbitalRodUtil.getSize(stack);
        BlockPos fixedPos = OrbitalRodUtil.getFixedPos(stack);

        OrbitalShot shot = ShotRegistry.get(shotId);
        if (shot == null) {
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }

        if (fixedPos != null) {
            StrikeScheduler.schedule(delay, () -> {
                shot.fire(serverWorld, fixedPos.toCenterPos(), size);
            });
            stack.decrement(1);
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        BlockHitResult hit = RaycastUtil.raycast(player);
        if (hit == null) {
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }

        StrikeScheduler.schedule(delay, () -> {
            shot.fire(serverWorld, hit.getBlockPos().toCenterPos(), size);
        });

        stack.decrement(1);
        cir.setReturnValue(ActionResult.SUCCESS);
    }
}