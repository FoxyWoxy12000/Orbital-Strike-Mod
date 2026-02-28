package com.orbitalstrike.mixin;

import com.orbitalstrike.core.rod.RodNameUpdater;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class PlayerTickMixin {

    private int tickCounter = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        tickCounter++;
        if (tickCounter >= 20) {
            tickCounter = 0;
            RodNameUpdater.updateAllPlayerRods((ServerPlayerEntity)(Object)this);
        }
    }
}