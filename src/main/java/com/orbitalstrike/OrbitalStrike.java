package com.orbitalstrike;

import com.orbitalstrike.core.command.OrbitalCommand;
import com.orbitalstrike.core.shot.ShotRegistry;
import com.orbitalstrike.core.shot.impl.NukeMk2Shot;
import com.orbitalstrike.core.shot.impl.NukeMk4Shot;
import com.orbitalstrike.core.shot.impl.NukeMk6Shot;
import com.orbitalstrike.core.shot.impl.StabShot;
import com.orbitalstrike.core.util.StrikeScheduler;
import com.orbitalstrike.platform.OrbitalPlatform;
import com.orbitalstrike.fabric.FabricPlatform;
import net.fabricmc.api.ModInitializer;

public class OrbitalStrike implements ModInitializer {

    public static final String MOD_ID = "orbital-strike";
    public static OrbitalPlatform PLATFORM;

    @Override
    public void onInitialize() {
        PLATFORM = new FabricPlatform();

        ShotRegistry.register(new StabShot());
        ShotRegistry.register(new NukeMk4Shot());
        ShotRegistry.register(new NukeMk2Shot());
        ShotRegistry.register(new NukeMk6Shot());

        OrbitalCommand.register();
        StrikeScheduler.init();
    }
}