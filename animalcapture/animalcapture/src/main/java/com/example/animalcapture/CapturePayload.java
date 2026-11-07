package com.example.animalcapture;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** クライアント -> サーバー: 「このエンティティを捕獲したい」。 */
public record CapturePayload(int entityId) implements CustomPacketPayload {

    public static final Type<CapturePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AnimalCapture.MOD_ID, "capture"));

    public static final StreamCodec<ByteBuf, CapturePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, CapturePayload::entityId, CapturePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, CapturePayload::handle);
    }

    /** クライアントの申告は信用せず、サーバー側で必ず検証する。 */
    private static void handle(CapturePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        Entity target = level.getEntity(payload.entityId());
        if (!(target instanceof Animal animal) || !animal.isAlive()) {
            return;
        }
        // バニラの攻撃・インタラクトと同じ到達距離チェック
        if (!player.canInteractWithEntity(animal, 1.0)) {
            return;
        }

        ItemStack stack = CapturedAnimalItem.capture(animal);
        if (stack.isEmpty()) {
            return;
        }

        level.sendParticles(ParticleTypes.POOF,
                animal.getX(), animal.getY() + animal.getBbHeight() / 2.0, animal.getZ(),
                8, animal.getBbWidth() / 3.0, animal.getBbHeight() / 4.0, animal.getBbWidth() / 3.0, 0.02);
        level.playSound(null, animal.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, 1.0F);

        animal.discard();
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
