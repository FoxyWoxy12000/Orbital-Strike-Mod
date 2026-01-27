package com.orbitalstrike.mixin;

import com.orbitalstrike.core.command.OrbitalCommand;
import com.orbitalstrike.core.rod.OrbitalRodUtil;
import com.orbitalstrike.core.shot.OrbitalShot;
import com.orbitalstrike.core.shot.ShotRegistry;
import com.orbitalstrike.core.util.RaycastUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = user.getStackInHand(hand);
        if (!OrbitalRodUtil.isOrbitalRod(stack)) return;
        if (world.isClient()) return;

        BlockHitResult hit = RaycastUtil.raycast(user);
        if (hit == null) {
            if (OrbitalCommand.MISS_FAILSAFE) {
                cir.setReturnValue(stack);
                return;
            }
        }

        String shotId = OrbitalRodUtil.getShot(stack);
        int delay = OrbitalRodUtil.getDelay(stack);
        String owner = OrbitalRodUtil.getOwner(stack);

        if (!owner.equals(user.getName().getString())) {
            cir.setReturnValue(stack);
            return;
        }

        OrbitalShot shot = ShotRegistry.get(shotId);
        if (shot == null) {
            cir.setReturnValue(stack);
            return;
        }

        shot.fire((net.minecraft.server.world.ServerWorld) world, hit.getBlockPos().toCenterPos(), delay, owner);
        stack.decrement(1);
        cir.setReturnValue(stack);
    }
}
