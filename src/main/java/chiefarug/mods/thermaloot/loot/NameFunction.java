package chiefarug.mods.thermaloot.loot;

import chiefarug.mods.thermaloot.Thermaloot;
import chiefarug.mods.thermaloot.loot.ApplyAugmentDataFunction.LuckData;
import cofh.lib.util.constants.NBTTags;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static chiefarug.mods.thermaloot.Thermaloot.LGGR;
import static chiefarug.mods.thermaloot.ThermalootConfig.NEST_ADJECTIVES;
import static chiefarug.mods.thermaloot.ThermalootConfig.NUMBER_OF_TRANSLATIONS;
import static chiefarug.mods.thermaloot.loot.ApplyAugmentDataFunction.NORMAL;
import static chiefarug.mods.thermaloot.loot.NameFunction.NameTier.amazing;
import static chiefarug.mods.thermaloot.loot.NameFunction.NameTier.terrible;
import static cofh.core.util.helpers.AugmentDataHelper.hasAugmentData;

public class NameFunction extends LootItemConditionalFunction {

	public static final Serializer SERIALIZER = new Serializer();

	protected NameFunction(LootItemCondition[] pPredicates) {
		super(pPredicates);
	}

	@Override
	protected ItemStack run(ItemStack stack, LootContext ctx) {
		if (!canBeNamed(stack)) return stack;

		Component originalName = stack.getHoverName();
		Component name = originalName;
		CompoundTag nbt = stack.getOrCreateTag();
		LuckData luck = LuckData.deserialize(nbt.getCompound(Thermaloot.Tags.LUCK_DATA));

		float finalLuck = luck.getFinalLuck();
		NameTier tier = NameTier.getTier(finalLuck);
		name = getModifiedName(tier, finalLuck, name, new DebugData(luck,nbt.getCompound(NBTTags.TAG_AUGMENT_DATA)));

		if (NEST_ADJECTIVES.get()) {
			// if one stat is particularly lucky/unlucky then add that on as well. This means that you could end up with a rather long name, like Perfect Stinky Capacitor of Uselessness if you have a good roll, a bad roll and a medioka average.
			if (luck.maxLuck > 0.98) name = getModifiedName(amazing, luck.maxLuck + 1, name, null);
			if (luck.minLuck < 0.02) name = getModifiedName(terrible, luck.minLuck + 1, name, null);
		}

		if (originalName != name)
			stack.setHoverName(name);
		return stack;
	}

	private static boolean canBeNamed(ItemStack stack) {
		return hasAugmentData(stack);
	}

	private static Component getModifiedName(NameTier tier, float randomSeed, Component original, @Nullable DebugData dd) {
		int number = new Random(Float.hashCode(randomSeed)).nextInt(NUMBER_OF_TRANSLATIONS.get());
		return tier.toComponent(number, original, dd);
	}

	@Override
	public LootItemFunctionType getType() {
		return Thermaloot.AUGMENT_NAME.get();
	}

	static class Serializer extends LootItemConditionalFunction.Serializer<NameFunction> {

		@Override
		public NameFunction deserialize(JsonObject json, JsonDeserializationContext ctx, LootItemCondition[] conditions) {
			return new NameFunction(conditions);
		}
	}

	record DebugData(LuckData luck, CompoundTag augmentData) {
		void logEm() {
			LGGR.warn("Luck: {}, MaxLuck: {}, MinLuck: {}, PossibleLuck: {}, AugmentData: {}", luck.luck, luck.maxLuck, luck.minLuck, luck.possibleLuck, NbtUtils.structureToSnbt(augmentData));
		}
	}
	enum NameTier {

		terrible(0f, 0.25f, true),
		bad(0.25f, 0.5f, true),
		good(0.5f, 0.75f, true),
		amazing(0.75f, 1.0f, true),
		bugged(-1, -1) {
			@Override
			Component toComponent(int n, Component originalName, @Nullable DebugData dd) {
				LGGR.error("Something bugged generated for Thermaloot! Initializing emergency procedures");
				if (dd != null) dd.logEm();
				Thread.dumpStack();
				return super.toComponent(n, originalName, dd);
			}
		},
			normal(-1, -1) {
			@Override
			Component toComponent(int n, Component originalName, @Nullable DebugData dd) {
				return originalName;
			}
		};

		public static final NameTier[] regularValues = {terrible, bad, good, amazing};
		private final float min;
		private final float max;
		private final boolean supportsMultiple;

		NameTier(float min, float max) {
			this(min, max, false);
		}

		NameTier(float min, float max, boolean supportsMultiple) {
			this.min = min;
			this.max = max;
			this.supportsMultiple = supportsMultiple;
		}

		Component toComponent(int n, Component originalName, @Nullable DebugData dd) {
			return net.minecraft.network.chat.Component.translatable("attribute.thermaloot." + name() + (supportsMultiple ? "." + n : ""), originalName).withStyle(STYLE);
		}

		private static final Style STYLE = Style.EMPTY.withItalic(false);

		private static NameTier getTier(float luckiness) {
			if ((int) luckiness == NORMAL) return normal;

			for (NameTier tier : NameTier.regularValues) {
				if (luckiness <= tier.max && luckiness >= tier.min)
					return tier;
			}
			return bugged;
		}

	}
}
