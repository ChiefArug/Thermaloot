package chiefarug.mods.thermaloot.loot;

import chiefarug.mods.thermaloot.Thermaloot;
import chiefarug.mods.thermaloot.ThermalootConfig;
import cofh.lib.util.constants.NBTTags;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Supplier;

import static chiefarug.mods.thermaloot.Thermaloot.LGGR;
import static chiefarug.mods.thermaloot.ThermalootConfig.BAD_GOOD;
import static chiefarug.mods.thermaloot.ThermalootConfig.GOOD_AMAZING;
import static chiefarug.mods.thermaloot.ThermalootConfig.NEST_ADJECTIVES;
import static chiefarug.mods.thermaloot.ThermalootConfig.NUMBER_OF_TRANSLATIONS;
import static chiefarug.mods.thermaloot.ThermalootConfig.TERRIBLE_BAD;
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
		DebugData dd = new DebugData(luck, nbt.getCompound(NBTTags.TAG_AUGMENT_DATA));
		name = getModifiedName(tier, finalLuck, name, dd);

		if (NEST_ADJECTIVES.get()) {
			// if one stat is particularly lucky/unlucky then add that on as well. This means that you could end up with a rather long name, like Perfect Stinky Capacitor of Uselessness if you have a good roll, a bad roll and a medioka average.
			if (luck.maxLuck > ThermalootConfig.EXTRA_LUCKY.get()) name = getModifiedName(amazing, luck.maxLuck + 1, name, dd);
			if (luck.minLuck < ThermalootConfig.EXTRA_UNLUCKY.get()) name = getModifiedName(terrible, luck.minLuck + 1, name, dd);
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

		terrible(() -> 0d, TERRIBLE_BAD, true),
		bad(TERRIBLE_BAD, BAD_GOOD, true),
		good(BAD_GOOD, GOOD_AMAZING, true),
		amazing(GOOD_AMAZING, () -> 1d, true),
		normal() { // only used when it generates with only set stat modifiers, ie dynamo throttle, auxiliary cactus
			static final Random r = new Random();

			@Override
			Component toComponent(int n, Component originalName, @Nullable DebugData dd) {
				if (r.nextBoolean()) return bad.toComponent(n, originalName, dd);
				return good.toComponent(n, originalName, dd);
			}
		},
		bugged() {
			@Override
			Component toComponent(int n, Component originalName, @Nullable DebugData dd) {
				LGGR.error("Something bugged generated for Thermaloot! Initializing emergency procedures");
				if (dd != null) dd.logEm();
				Thread.dumpStack();
				return super.toComponent(n, originalName, dd);
			}
		};

		public static final NameTier[] regularValues = {terrible, bad, good, amazing};
		private final Supplier<Double> min;
		private final Supplier<Double> max;
		private final boolean supportsMultiple;

		NameTier() {
			this(() -> -1d, () -> -1d);
		}

		NameTier(Supplier<Double> min, Supplier<Double> max) {
			this(min, max, false);
		}

		NameTier(Supplier<Double> min, Supplier<Double> max, boolean supportsMultiple) {
			this.min = min;
			this.max = max;
			this.supportsMultiple = supportsMultiple;
		}

		Component toComponent(int n, Component originalName, @Nullable DebugData dd) {
			return new TranslatableComponent("attribute.thermaloot." + name() + (supportsMultiple ? "." + n : ""), originalName).withStyle(STYLE);
		}

		private static final Style STYLE = Style.EMPTY.withItalic(false);

		private static NameTier getTier(float luckiness) {
			if ((int) luckiness == AddAugmentDataFunction.NORMAL) return normal;

			for (NameTier tier : NameTier.regularValues) {
				if (luckiness <= tier.getMax() && luckiness >= tier.getMin())
					return tier;
			}
			return bugged;
		}

		public double getMin() {
			return min.get();
		}

		public double getMax() {
			return max.get();
		}
	}
}
