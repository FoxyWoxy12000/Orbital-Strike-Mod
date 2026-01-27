package com.orbitalstrike;

import com.orbitalstrike.core.command.OrbitalCommand;
import com.orbitalstrike.core.shot.ShotRegistry;
import com.orbitalstrike.core.shot.impl.StabShot;
import com.orbitalstrike.fabric.FabricPlatform;
import com.orbitalstrike.platform.OrbitalPlatform;
import net.fabricmc.api.ModInitializer;
import com.orbitalstrike.core.util.StrikeScheduler;
import com.orbitalstrike.core.shot.impl.NukeMk4Shot;

public class OrbitalStrike implements ModInitializer {

    public static OrbitalPlatform PLATFORM;

    @Override
    public void onInitialize() {

        PLATFORM = new FabricPlatform();
        StrikeScheduler.init();
        ShotRegistry.register(new StabShot());
        ShotRegistry.register(new NukeMk4Shot());
        OrbitalCommand.register();

    }
}
