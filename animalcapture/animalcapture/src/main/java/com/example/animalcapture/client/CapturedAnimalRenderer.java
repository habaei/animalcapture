package com.example.animalcapture.client;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 捕獲アイテムのアイコン。PNGは使わず、保存されている動物そのものを
 * 斜め前から見た姿でアイテム枠の中に描画する(バニラ・他MODの動物にそのまま対応)。
 */
public class CapturedAnimalRenderer extends BlockEntityWithoutLevelRenderer {

    // ---- 見た目の調整用(ここだけいじれば角度・大きさが変わる) ----
    /** 体の向き(度)。0で真正面、負の値で動物がプレイヤーから見て右前を向く。 */
    private static final float BODY_YAW = -35.0F;
    /** 頭の向き(度)。体より少し正面寄りにして、こちらを見ている感じにする。 */
    private static final float HEAD_YAW = -10.0F;
    /** 少し上から見下ろす角度(度)。0で真横から。 */
    private static final float TILT = 10.0F;
    /** アイテム枠(1x1)に対する動物の大きさの割合。 */
    private static final float FIT = 0.78F;

    private static final int CACHE_SIZE = 64;

    /** NBTごとのダミー動物。毎フレーム作り直さないためのキャッシュ。 */
    private static final Map<CustomData, Optional<Entity>> CACHE =
            new LinkedHashMap<CustomData, Optional<Entity>>(16, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<CustomData, Optional<Entity>> eldest) {
                    return size() > CACHE_SIZE;
                }
            };
    private static WeakReference<ClientLevel> cachedLevel = new WeakReference<>(null);

    public CapturedAnimalRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static void clearCache() {
        CACHE.clear();
        cachedLevel = new WeakReference<>(null);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (level == null || data == null) {
            return;
        }

        Entity entity = getEntity(level, data);
        if (entity == null) {
            return;
        }

        float height = entity.getBbHeight();
        float size = Math.max(Math.max(height, entity.getBbWidth()), 0.25F);
        float scale = FIT / size;

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        boolean hitBoxes = dispatcher.shouldRenderHitBoxes();

        poseStack.pushPose();
        // アイテム枠(0..1)の中心に置き、少し見下ろす角度をつけてから動物の中心を合わせる
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(TILT));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0, -height / 2.0, 0.0);

        dispatcher.setRenderShadow(false);
        dispatcher.setRenderHitBoxes(false);
        try {
            dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 0.0F, poseStack, buffer, packedLight);
        } finally {
            dispatcher.setRenderShadow(true);
            dispatcher.setRenderHitBoxes(hitBoxes);
            poseStack.popPose();
        }
    }

    private static Entity getEntity(ClientLevel level, CustomData data) {
        if (cachedLevel.get() != level) {
            CACHE.clear();
            cachedLevel = new WeakReference<>(level);
        }
        Optional<Entity> cached = CACHE.get(data);
        if (cached == null) {
            cached = Optional.ofNullable(createEntity(level, data));
            CACHE.put(data, cached);
        }
        return cached.orElse(null);
    }

    private static Entity createEntity(ClientLevel level, CustomData data) {
        CompoundTag tag = data.copyTag();
        // アイコンに不要・邪魔なものは外す
        tag.remove("Passengers");
        tag.remove("Fire");
        tag.remove("leash");
        tag.remove("CustomName");
        tag.remove("CustomNameVisible");

        Entity entity = EntityType.create(tag, level).orElse(null);
        if (entity == null) {
            return null;
        }

        entity.setCustomNameVisible(false);
        entity.setYRot(BODY_YAW);
        entity.yRotO = BODY_YAW;
        entity.setXRot(0.0F);
        entity.xRotO = 0.0F;
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = BODY_YAW;
            living.yBodyRotO = BODY_YAW;
            living.yHeadRot = HEAD_YAW;
            living.yHeadRotO = HEAD_YAW;
        }
        return entity;
    }
}
