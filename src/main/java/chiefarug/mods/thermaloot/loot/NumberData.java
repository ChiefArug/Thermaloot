package chiefarug.mods.thermaloot.loot;

import cofh.thermal.lib.common.ThermalAugmentRules;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Map;

record NumberData(String modifier, float min, float max, float value, boolean isLuckBased) {

	float getLuck() {
		float luck = (value - min) / (max - min);
		if (ThermalAugmentRules.isInverse(modifier)) luck = 1 - luck;
		return luck;
	}

	float getValue() {
		if (ThermalAugmentRules.isInteger(modifier)) return Math.round(value);
		return value;
	}

	interface NumberDataGetter extends TriFunction<String, NumberProvider, LootContext, NumberData> {}

	static final Map<LootNumberProviderType, NumberDataGetter> getters = Map.of(
			NumberProviders.CONSTANT, NumberData::fromConstant,
			NumberProviders.UNIFORM, NumberData::fromUniform,
			NumberProviders.BINOMIAL, NumberData::fromBinomial
	);

	public static NumberData from(String modifier, NumberProvider provider, LootContext ctx) {
		LootNumberProviderType type = provider.getType();

		if (getters.containsKey(type)) {
			return getters.get(type).apply(modifier, provider, ctx);
		} else {
			return fromConstant(modifier, provider, ctx);
		}
	}

	private static NumberData fromConstant(String modifier, NumberProvider provider, LootContext ctx) {
		float value = provider.getFloat(ctx);
		return new NumberData(modifier, value, value, value, false);
	}

	private static NumberData fromBinomial(String modifier, NumberProvider np, LootContext ctx) {
		BinomialDistributionGenerator provider = ((BinomialDistributionGenerator) np);
		return new NumberData(modifier, 0, from(modifier, provider.n, ctx).max, provider.getFloat(ctx), true);
	}

	private static NumberData fromUniform(String modifier, NumberProvider np, LootContext ctx) {
		UniformGenerator provider = ((UniformGenerator) np);
		return new NumberData(modifier, from(modifier, provider.min, ctx).min, from(modifier, provider.max, ctx).max, provider.getFloat(ctx), true);
	}
}
