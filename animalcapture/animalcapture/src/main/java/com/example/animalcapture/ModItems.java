package com.example.animalcapture;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AnimalCapture.MOD_ID);

    public static final DeferredItem<CapturedAnimalItem> CAPTURED_ANIMAL = ITEMS.registerItem(
            "captured_animal",
            CapturedAnimalItem::new,
            new Item.Properties().stacksTo(1));

    private ModItems() {
    }
}
