package com.orbitalstrike.mixin;

import com.orbitalstrike.core.rod.OrbitalRodUtil;
import com.orbitalstrike.core.rod.RodTriggerStyle;
import com.orbitalstrike.core.shot.OrbitalShot;
import com.orbitalstrike.core.shot.ShotRegistry;
import com.orbitalstrike.core.util.RaycastUtil;
import com.orbitalstrike.core.util.StrikeScheduler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {

    @Unique
    private static final Map<UUID, Boolean> CAST_STATE = new HashMap<>();

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

        RodTriggerStyle style = RodTriggerStyle.current;

        if (style == RodTriggerStyle.INSTANT) {
            handleInstant(player, serverWorld, stack, shot, delay, size, fixedPos);
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (style == RodTriggerStyle.CAST) {
            handleCast(player, serverWorld, stack, shot, delay, size, fixedPos);
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if (style == RodTriggerStyle.REEL) {
            handleReel(player, serverWorld, stack, shot, delay, size, fixedPos);
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        cir.setReturnValue(ActionResult.FAIL);
    }

    @Unique
    private void handleInstant(ServerPlayerEntity player, ServerWorld world, ItemStack stack, OrbitalShot shot, int delay, int size, BlockPos fixedPos) {
        if (fixedPos != null) {
            StrikeScheduler.schedule(delay, () -> shot.fire(world, fixedPos.toCenterPos(), size));
        } else {
            BlockHitResult hit = RaycastUtil.raycast(player);
            if (hit == null) return;
            StrikeScheduler.schedule(delay, () -> shot.fire(world, hit.getBlockPos().toCenterPos(), size));
        }
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);
        stack.decrement(1);
    }

    @Unique
    private void handleCast(ServerPlayerEntity player, ServerWorld world, ItemStack stack, OrbitalShot shot, int delay, int size, BlockPos fixedPos) {
        FishingBobberEntity bobber = new FishingBobberEntity(player, world, 0, 0);
        world.spawnEntity(bobber);

        world.getServer().execute(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            bobber.discard();

            if (fixedPos != null) {
                StrikeScheduler.schedule(delay, () -> shot.fire(world, fixedPos.toCenterPos(), size));
            } else {
                BlockHitResult hit = RaycastUtil.raycast(player);
                if (hit == null) return;
                StrikeScheduler.schedule(delay, () -> shot.fire(world, hit.getBlockPos().toCenterPos(), size));
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);
            stack.decrement(1);
        });
    }


    @Unique
    private void handleReel(ServerPlayerEntity player, ServerWorld world, ItemStack stack, OrbitalShot shot, int delay, int size, BlockPos fixedPos) {
        UUID playerId = player.getUuid();
        boolean hasCast = CAST_STATE.getOrDefault(playerId, false);

        if (!hasCast) {
            FishingBobberEntity bobber = new FishingBobberEntity(player, world, 0, 0);
            world.spawnEntity(bobber);
            CAST_STATE.put(playerId, true);
        } else {
            player.fishHook.discard();
            CAST_STATE.remove(playerId);

            if (fixedPos != null) {
                StrikeScheduler.schedule(delay, () -> shot.fire(world, fixedPos.toCenterPos(), size));
            } else {
                BlockHitResult hit = RaycastUtil.raycast(player);
                if (hit == null) return;
                StrikeScheduler.schedule(delay, () -> shot.fire(world, hit.getBlockPos().toCenterPos(), size));
            }
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);
            stack.decrement(1);
        }
    }
}