package com.example.animalcapture;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(AnimalCapture.MOD_ID)
public class AnimalCapture {
    public static final String MOD_ID = "animalcapture";

    public AnimalCapture(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(CapturePayload::register);
    }
}
