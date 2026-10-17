package net.brilliantw.richpresence.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
	@Accessor("hoveredSlot")
	Slot brilliant$hoveredSlot();

	@Accessor("leftPos")
	int brilliant$leftPos();

	@Accessor("topPos")
	int brilliant$topPos();

	//? if >=26.1 {
	@Invoker("slotClicked")
	void brilliant$slotClicked(Slot slot, int index, int button, net.minecraft.world.inventory.ContainerInput input);
	//?} else {
	/*@Invoker("slotClicked")
	void brilliant$slotClicked(Slot slot, int index, int button, net.minecraft.world.inventory.ClickType input);
	*///?}
}
