package chiefarug.mods.thermaloot.loot;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import static chiefarug.mods.thermaloot.Thermaloot.Tags.LUCK;
import static chiefarug.mods.thermaloot.Thermaloot.Tags.MAX_LUCK;
import static chiefarug.mods.thermaloot.Thermaloot.Tags.MIN_LUCK;
import static chiefarug.mods.thermaloot.Thermaloot.Tags.POSSIBLE_LUCK;
import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;

public class LuckData {
	float luck = 0;
	float maxLuck = NEGATIVE_INFINITY;
	float minLuck = Float.POSITIVE_INFINITY;
	int possibleLuck = 0;

	float getFinalLuck() {
		if (luckless()) return AddAugmentDataFunction.NORMAL;
		return luck / possibleLuck;
	}

	void addLuck(float luck) {
		if (luck < minLuck) minLuck = luck;
		if (luck > maxLuck) maxLuck = luck;
		this.luck += luck;
		possibleLuck++;
	}

	private boolean luckless() {
		return maxLuck == NEGATIVE_INFINITY && minLuck == POSITIVE_INFINITY && possibleLuck == 0;
	}

	@Nullable
	public CompoundTag serialize() {
		if (luckless()) return null;
		CompoundTag tag = new CompoundTag();
		tag.putFloat(LUCK, luck);
		tag.putFloat(MAX_LUCK, maxLuck);
		tag.putFloat(MIN_LUCK, minLuck);
		tag.putInt(POSSIBLE_LUCK, possibleLuck);
		return tag;
	}

	public static LuckData deserialize(@Nullable CompoundTag tag) {
		LuckData luckData = new LuckData();
		if (tag == null || tag.isEmpty()) return luckData;

		luckData.luck = tag.getFloat(LUCK);
		luckData.maxLuck = tag.getFloat(MAX_LUCK);
		luckData.minLuck = tag.contains(MIN_LUCK) ? tag.getFloat(MIN_LUCK) : 1;
		luckData.possibleLuck = tag.getInt(POSSIBLE_LUCK);
		return luckData;
	}
}
