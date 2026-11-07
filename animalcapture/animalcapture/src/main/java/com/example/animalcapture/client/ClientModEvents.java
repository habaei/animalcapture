package com.example.animalcapture.client;

import com.example.animalcapture.AnimalCapture;
import com.example.animalcapture.ModItems;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

@EventBusSubscriber(modid = AnimalCapture.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    /**
     * 捕獲キー。デフォルトは I。
     * T はバニラのチャット欄を開くキーなので、押しっぱなしにするとチャットが開いてしまい使えない。
     * T にしたい場合は下の InputConstants.KEY_I を KEY_T に変えたうえで、
     * コントロール設定でチャットのキーを別のキーに変更すること。
     */
    public static final KeyMapping CAPTURE_KEY = new KeyMapping(
            "key.animalcapture.capture",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_I,
            "key.categories.animalcapture");

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(CAPTURE_KEY);
    }

    @SubscribeEvent
    public static void registerItemExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private CapturedAnimalRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new CapturedAnimalRenderer();
                }
                return renderer;
            }
        }, ModItems.CAPTURED_ANIMAL.get());
    }
}
