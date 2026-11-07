package com.example.animalcapture;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

/**
 * 捕獲した動物を1個のアイテムとして持ち運ぶ。
 * 動物のNBT全体を CUSTOM_DATA に保存するので、名前・体力・子供/大人・懐き状態・色なども復元される。
 */
public class CapturedAnimalItem extends Item {

    public CapturedAnimalItem(Properties properties) {
        super(properties);
    }

    /** 動物を保存した捕獲アイテムを作る。保存できない場合は ItemStack.EMPTY。 */
    public static ItemStack capture(Animal animal) {
        if (animal.isRemoved() || !animal.getType().canSerialize()) {
            return ItemStack.EMPTY;
        }

        // 乗客・リードは持ち運べないので外しておく
        animal.ejectPassengers();
        animal.stopRiding();
        animal.dropLeash(true, true);

        CompoundTag tag = new CompoundTag();
        if (!animal.saveAsPassenger(tag)) {
            return ItemStack.EMPTY;
        }
        tag.remove("UUID");
        tag.remove("Passengers");

        ItemStack stack = new ItemStack(ModItems.CAPTURED_ANIMAL.get());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        CustomData data = context.getItemInHand().get(DataComponents.CUSTOM_DATA);
        if (data == null || data.isEmpty()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        // 草や花のようにぶつからないブロックはその位置に、それ以外は触った面の外側に出す
        BlockPos clickedPos = context.getClickedPos();
        BlockState state = level.getBlockState(clickedPos);
        BlockPos spawnPos = state.getCollisionShape(level, clickedPos).isEmpty()
                ? clickedPos
                : clickedPos.relative(context.getClickedFace());

        Entity entity = EntityType.create(data.copyTag(), serverLevel).orElse(null);
        if (entity == null) {
            return InteractionResult.FAIL;
        }

        float yaw = Mth.wrapDegrees(context.getRotation() + 180.0F); // プレイヤーの方を向く
        entity.setUUID(UUID.randomUUID());
        entity.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, yaw, 0.0F);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.fallDistance = 0.0F;
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = yaw;
            living.yHeadRot = yaw;
        }

        if (!serverLevel.addFreshEntity(entity)) {
            return InteractionResult.FAIL;
        }

        Player player = context.getPlayer();
        serverLevel.playSound(null, spawnPos, SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 0.6F, 1.0F);
        serverLevel.gameEvent(player, GameEvent.ENTITY_PLACE, spawnPos);
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public Component getName(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            ResourceLocation id = ResourceLocation.tryParse(data.copyTag().getString("id"));
            if (id != null) {
                Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
                if (type.isPresent()) {
                    return Component.translatable(
                            "item.animalcapture.captured_animal.named", type.get().getDescription());
                }
            }
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.animalcapture.captured_animal.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
