package chiefarug.mods.thermaloot;

import cofh.core.util.helpers.AugmentDataHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static chiefarug.mods.thermaloot.Thermaloot.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class Tooltips {

	@SubscribeEvent
	public static void addToggleAugmentTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		CompoundTag augData = AugmentDataHelper.getAugmentData(stack);
		if (augData == null) return;

	}
}
