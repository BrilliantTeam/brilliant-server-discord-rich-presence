package net.brilliantw.richpresence.mixin;

import java.util.ArrayList;
import java.util.List;
import net.brilliantw.richpresence.ExactStackedContents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StackedItemContents.class)
public abstract class StackedItemContentsMixin implements ExactStackedContents {
	@Unique
	private final StackedContents<ItemStack> brilliant$exact = new StackedContents<>();
	@Unique
	private final List<ItemStack> brilliant$keys = new ArrayList<>();

	@Override
	public StackedContents<ItemStack> brilliant$exact() {
		return brilliant$exact;
	}

	@Inject(method = "accountSimpleStack", at = @At("HEAD"))
	private void brilliant$accountUnusable(ItemStack stack, CallbackInfo ci) {
		if (!Inventory.isUsableForCrafting(stack)) {
			brilliant$account(stack, stack.getMaxStackSize());
		}
	}

	@Inject(method = "accountStack(Lnet/minecraft/world/item/ItemStack;I)V", at = @At("HEAD"))
	private void brilliant$accountStack(ItemStack stack, int maxCount, CallbackInfo ci) {
		brilliant$account(stack, maxCount);
	}

	@Inject(method = "clear", at = @At("TAIL"))
	private void brilliant$clear(CallbackInfo ci) {
		brilliant$exact.clear();
		brilliant$keys.clear();
	}

	@Unique
	private void brilliant$account(ItemStack stack, int maxCount) {
		if (stack.isEmpty()) {
			return;
		}
		ItemStack key = brilliant$keys.stream().filter(k -> ItemStack.isSameItemSameComponents(k, stack)).findFirst().orElse(null);
		if (key == null) {
			key = stack.copyWithCount(1);
			brilliant$keys.add(key);
		}
		brilliant$exact.account(key, Math.min(maxCount, stack.getCount()));
	}
}
