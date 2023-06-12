package chiefarug.mods.thermaloot;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.level.storage.loot.providers.number.ScoreboardValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.level.storage.loot.providers.score.ContextScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.FixedScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;

import java.util.Map;

@SuppressWarnings("SpellCheckingInspection")
// Do i only use this class once? Yes. Was it worth it? Maybe...
public class ICodecifiedNumberProvidersForYouMojangHopeYoureHappy {

	public static final Codec<NumberProvider> NUMBER_PROVIDER = Codec.either(
            Codec.FLOAT.xmap(ConstantValue::exactly, (ConstantValue cv) -> cv.value),
            NumberProviderTypeCodecs.MAIN
    // get the right, else get the left (it should never throw). Always serialize to the right, as that is just NumberProvider
    ).xmap(either -> either.right().orElse(either.orThrow()), Either::right);

	public static class NumberProviderTypeCodecs {
		private static final Codec<LootNumberProviderType> TYPE = Registry.LOOT_NUMBER_PROVIDER_TYPE.byNameCodec();

		public static final Codec<ConstantValue> CONSTANT = RecordCodecBuilder.create(instance -> instance.group(
				Codec.FLOAT.fieldOf("value").forGetter((ConstantValue cv) -> cv.getFloat(null))
		).apply(instance, ConstantValue::exactly));
		public static final Codec<BinomialDistributionGenerator> BINOMIAL = RecordCodecBuilder.create(instance -> instance.group(
				NUMBER_PROVIDER.fieldOf("n").forGetter((BinomialDistributionGenerator bdg) -> bdg.n),
				NUMBER_PROVIDER.fieldOf("p").forGetter((BinomialDistributionGenerator bdg) -> bdg.p)
		).apply(instance, BinomialDistributionGenerator::new));
		public static final Codec<UniformGenerator> UNIFORM = RecordCodecBuilder.create(instance -> instance.group(
				NUMBER_PROVIDER.fieldOf("min").forGetter((UniformGenerator ug) -> ug.min),
				NUMBER_PROVIDER.fieldOf("max").forGetter((UniformGenerator ug) -> ug.max)
		).apply(instance, UniformGenerator::new));
		public static final Codec<ScoreboardValue> SCORE = RecordCodecBuilder.create(instance -> instance.group(
				NameProviderCodecs.MAIN.fieldOf("target").forGetter(sv -> sv.target),
				Codec.STRING.fieldOf("score").forGetter((ScoreboardValue sv) -> sv.score),
				Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter((ScoreboardValue sv) -> sv.scale)
		).apply(instance, ScoreboardValue::new));
		public static final Map<LootNumberProviderType, Codec<? extends NumberProvider>> ALL = Map.of(
				NumberProviders.CONSTANT, CONSTANT,
				NumberProviders.BINOMIAL, BINOMIAL,
				NumberProviders.UNIFORM, UNIFORM,
				NumberProviders.SCORE, SCORE
				);
		private static final Codec<NumberProvider> MAIN = TYPE.dispatch(NumberProvider::getType, NumberProviderTypeCodecs.ALL::get);
	}

	static class NameProviderCodecs {
		private static final Codec<LootScoreProviderType> TYPE = Registry.LOOT_SCORE_PROVIDER_TYPE.byNameCodec();

		public static final Codec<FixedScoreboardNameProvider> FIXED = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("name").forGetter(FixedScoreboardNameProvider::getName)
		).apply(instance, s -> (FixedScoreboardNameProvider) FixedScoreboardNameProvider.forName(s)));
		public static final Codec<LootContext.EntityTarget> ENTITY_TARGET = Codec.STRING.xmap(LootContext.EntityTarget::getByName, LootContext.EntityTarget::getName);
		public static final Codec<ContextScoreboardNameProvider> CONTEXT = RecordCodecBuilder.create(instance -> instance.group(
				ENTITY_TARGET.fieldOf("target").forGetter(csnp -> csnp.target)
		).apply(instance, t -> (ContextScoreboardNameProvider)ContextScoreboardNameProvider.forTarget(t)));
		public static final Map<LootScoreProviderType, Codec<? extends ScoreboardNameProvider>> ALL = Map.of(
				ScoreboardNameProviders.CONTEXT, CONTEXT,
				ScoreboardNameProviders.FIXED, FIXED
		);
		private static final Codec<ScoreboardNameProvider> MAIN = TYPE.dispatch(ScoreboardNameProvider::getType, NameProviderCodecs.ALL::get);
	}
}
