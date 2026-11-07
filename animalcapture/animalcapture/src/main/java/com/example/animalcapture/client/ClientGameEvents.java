package com.example.animalcapture.client;

import com.example.animalcapture.AnimalCapture;
import com.example.animalcapture.CapturePayload;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = AnimalCapture.MOD_ID, value = Dist.CLIENT)
public final class ClientGameEvents {

    private ClientGameEvents() {
    }

    /** キーを押しながら動物を右クリックしたら、捕獲リクエストをサーバーに送る。 */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide()) {
            return; // シングルプレイでは内部サーバー側でも発火するので除外
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return; // 両手分のイベントで二重送信しない
        }
        if (!(event.getTarget() instanceof Animal animal)) {
            return;
        }
        if (!ClientModEvents.CAPTURE_KEY.isDown()) {
            return;
        }

        PacketDistributor.sendToServer(new CapturePayload(animal.getId()));
        // 通常の右クリック動作(餌やり・乗馬など)は止める
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        CapturedAnimalRenderer.clearCache();
    }
}
