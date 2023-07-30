package chiefarug.mods.thermaloot;

import com.google.common.base.Suppliers;
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
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("SpellCheckingInspection")
// Do I only use this class once? Yes. Was it worth it? Maybe...
public class ICodecifiedNumberProvidersForYouMojangHopeYoureHappy {

	public static void loadEarlyPls() {}

	public static final Codec<NumberProvider> NUMBER_PROVIDER = Codec.either(
            Codec.FLOAT.xmap(ConstantValue::exactly, (ConstantValue cv) -> cv.value),
            NumberProviderTypeCodecs.MAIN
    // Get the right, else get the left (it should never throw).
	// Always serialise to the right, as that is just NumberProvider
    ).xmap(ICodecifiedNumberProvidersForYouMojangHopeYoureHappy::unwrapDoubleSidedEither, Either::right);

	public static class NumberProviderTypeCodecs {
		public static final Codec<LootNumberProviderType> TYPE = Registry.LOOT_NUMBER_PROVIDER_TYPE.byNameCodec();

		// These are all suppliers because some of them reference the main NP codec, which is still being initialised when these are being initialised, so results in a NPE
		public static final Supplier<Codec<ConstantValue>> CONSTANT = Suppliers.memoize(() -> RecordCodecBuilder.create(instance -> instance.group(
				Codec.FLOAT.fieldOf("value").forGetter((ConstantValue cv) -> cv.getFloat(null))
		).apply(instance, ConstantValue::exactly)));
		public static final Supplier<Codec<BinomialDistributionGenerator>> BINOMIAL = Suppliers.memoize(() -> RecordCodecBuilder.create(instance -> instance.group(
				NUMBER_PROVIDER.fieldOf("n").forGetter((BinomialDistributionGenerator bdg) -> bdg.n),
				NUMBER_PROVIDER.fieldOf("p").forGetter((BinomialDistributionGenerator bdg) -> bdg.p)
		).apply(instance, BinomialDistributionGenerator::new)));
		public static final Supplier<Codec<UniformGenerator>> UNIFORM = Suppliers.memoize(() -> RecordCodecBuilder.create(instance -> instance.group(
				NUMBER_PROVIDER.fieldOf("min").forGetter((UniformGenerator ug) -> ug.min),
				NUMBER_PROVIDER.fieldOf("max").forGetter((UniformGenerator ug) -> ug.max)
		).apply(instance, UniformGenerator::new)));
		public static final Supplier<Codec<ScoreboardValue>> SCORE = Suppliers.memoize(() -> RecordCodecBuilder.create(instance -> instance.group(
				NameProviderCodecs.MAIN.fieldOf("target").forGetter(sv -> sv.target),
				Codec.STRING.fieldOf("score").forGetter((ScoreboardValue sv) -> sv.score),
				Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter((ScoreboardValue sv) -> sv.scale)
		).apply(instance, ScoreboardValue::new)));
		public static final Map<LootNumberProviderType, Supplier<? extends Codec<? extends NumberProvider>>> ALL = Map.of(
				NumberProviders.CONSTANT, CONSTANT,
				NumberProviders.BINOMIAL, BINOMIAL,
				NumberProviders.UNIFORM, UNIFORM,
				NumberProviders.SCORE, SCORE
				);
		private static final Codec<NumberProvider> actualMainOne = TYPE.dispatch(NumberProvider::getType, type -> NumberProviderTypeCodecs.ALL.get(type).get());
		public static final Codec<NumberProvider> MAIN = actualMainOne;/*Codec.either(
				actualMainOne,
				UNIFORM.get()
		).xmap(ICodecifiedNumberProvidersForYouMojangHopeYoureHappy::unwrapDoubleSidedEither, Either::left);*/


	}
	private static <T> T unwrapDoubleSidedEither(Either<? extends T, ? extends T> e) {
		Optional<? extends T> l = e.left();
		if (l.isPresent()) return l.get();
		return e.right().orElseThrow();
	}

	public static class NameProviderCodecs {
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
		public static final Codec<ScoreboardNameProvider> MAIN = TYPE.dispatch(ScoreboardNameProvider::getType, NameProviderCodecs.ALL::get);
	}
}
