package dev.nyon.compactspawners.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(
        method = "appendHoverText",
        at = @At("HEAD")
    )
    public void getName(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced, CallbackInfo ci) {
        CompoundTag tag = stack.getTag();
        if (stack.getItem() != Items.SPAWNER || tag == null) return;
        CompoundTag entityTag = tag.getCompound("BlockEntityTag")
            .getCompound("SpawnData")
            .getCompound("entity");
        if (entityTag.isEmpty()) return;
        String[] entityParts = entityTag
            .getString("id")
            .split(":")[1]
            .split("_");
        for (int i = 0; i < entityParts.length; ++i) {
            entityParts[i] = entityParts[i].substring(0, 1).toUpperCase() + entityParts[i].substring(1);
        }
        tooltipComponents.add(Component.literal("Type: " + String.join(" ", entityParts)));
    }
}
