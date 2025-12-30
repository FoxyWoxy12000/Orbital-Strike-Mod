package com.example.orbitalcannon;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(OrbitalCannon.MODID)
public class OrbitalCannon {
    public static final String MODID = "orbitalcannon";

    public OrbitalCannon() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(OrbitalGameRules.class);

        MinecraftForge.EVENT_BUS.register(OrbitalEvents.class);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        OrbitalCommand.register(event.getDispatcher());
        OrbitalStrikeCommand.register(event.getDispatcher());
        GiveOrbitalCommand.register(event.getDispatcher());
        OrbitalStrikeRodCommand.register(event.getDispatcher());
        GiveOrbitalStrikeRodCommand.register(event.getDispatcher());
    }
}
