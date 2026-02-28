package com.orbitalstrike;

import com.orbitalstrike.core.command.OrbitalCommand;
import com.orbitalstrike.core.shot.ShotRegistry;
import com.orbitalstrike.core.shot.impl.OSC.*;
import com.orbitalstrike.core.shot.impl.*;
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


        //ShotRegistry.register(new AccurateStabShot());
        ShotRegistry.register(new StabShot());
        ShotRegistry.register(new SquareStabShot());
        ShotRegistry.register(new CircleStabShot());
        ShotRegistry.register(new StarStabShot());
        ShotRegistry.register(new NukeMk4Shot());
        ShotRegistry.register(new NukeMk2Shot());
        ShotRegistry.register(new NukeMk6Shot());

        OrbitalCommand.register();
        StrikeScheduler.init();
    }
}