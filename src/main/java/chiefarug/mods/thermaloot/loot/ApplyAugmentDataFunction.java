package chiefarug.mods.thermaloot.loot;

import chiefarug.mods.thermaloot.Thermaloot;
import cofh.core.util.helpers.AugmentDataHelper;
import cofh.thermal.lib.item.AugmentItem;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static chiefarug.mods.thermaloot.Thermaloot.LGGR;
import static chiefarug.mods.thermaloot.Thermaloot.Tags.LUCK;
import static chiefarug.mods.thermaloot.Thermaloot.Tags.LUCK_DATA;
import static chiefarug.mods.thermaloot.Thermaloot.Tags.MAX_LUCK;
import static chiefarug.mods.thermaloot.Thermaloot.Tags.MIN_LUCK;
import static chiefarug.mods.thermaloot.Thermaloot.Tags.POSSIBLE_LUCK;
import static chiefarug.mods.thermaloot.Thermaloot.mergeTags;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_DATA;

public class ApplyAugmentDataFunction extends LootItemConditionalFunction {

	public static final	Serializer SERIALIZER = new Serializer();

	/**
	 * Used to signify non-existent luck. (ie when the entire thing is based on constants)
	 */
	static final int NORMAL = -256;

	protected ApplyAugmentDataFunction(LootItemCondition[] predicates, Map<String, NumberProvider> augmentDataProviders) {
		super(predicates);
		this.augmentDataProviders = augmentDataProviders;
		if (augmentDataProviders.isEmpty()) {
			throw new IllegalArgumentException("augments cannot be empty");
		}
	}

	private final Map<String, NumberProvider> augmentDataProviders;


	@Override
	protected ItemStack run(ItemStack stack, LootContext ctx) {
		if (stack.getItem() instanceof AugmentItem) {
			LGGR.error("Cannot add augment data to dedicated augment item: " + ForgeRegistries.ITEMS.getKey(stack.getItem()));
			return stack;
		}

		CompoundTag nbt = stack.getTag();
		LuckData luck = nbt == null ? new LuckData() : LuckData.deserialize(nbt.getCompound(LUCK_DATA));

		AugmentDataHelper.Builder dataBuilder = new AugmentDataHelper.Builder();
		augmentDataProviders.forEach((modifier, numberProvider) -> {
			NumberData numberData = NumberData.from(numberProvider, ctx);

//			if (numberData.min < 0) LGGR.error("Augment values cannot be negative! Please check your loot tables"); //todo add list of ones that can be negative, and allow them.
			// ones that can be negative: Radius, PotionAmp

			if (numberData.isLuckBased) {
				// adds a number between 1.0 and 0.0, where 0.0 means it's the lowest possible value (unlucky) and 1.0 is the greatest possible value.
				// if you pass negatives to this it is your fault if things go funky
				luck.addLuck(numberData.getLuck()); //todo invert luck number for things where being lower is better, ie machine energy. there is a list somewhere
			}
			dataBuilder.mod(modifier, numberData.value);
		});

		CompoundTag augmentData = dataBuilder.build();
		if (augmentData == null) return stack;

		nbt = stack.getOrCreateTag();
		CompoundTag existingAugmentData = nbt.contains(TAG_AUGMENT_DATA) ? nbt.getCompound(TAG_AUGMENT_DATA) : null;
		nbt.put(TAG_AUGMENT_DATA, mergeTags(augmentData, existingAugmentData));
		nbt.put(LUCK_DATA, luck.serialize(new CompoundTag()));

		return stack;
	}

	record NumberData(float min, float max, float value, boolean isLuckBased) {
		float getLuck() {
			return (value - min) / (max - min);
		}

		interface NumberDataGetter extends BiFunction<NumberProvider, LootContext, NumberData> {
		}

		static final Map<LootNumberProviderType, NumberDataGetter> getters = new HashMap<>();

		static {
			getters.put(NumberProviders.CONSTANT, NumberData::fromConstant);
			getters.put(NumberProviders.UNIFORM, NumberData::fromUniform);
			getters.put(NumberProviders.BINOMIAL, NumberData::fromBinomial);
		}

		public static NumberData from(NumberProvider provider, LootContext ctx) {
			LootNumberProviderType type = provider.getType();

			if (getters.containsKey(type)) {
				return getters.get(type).apply(provider, ctx);
			} else {
				return fromConstant(provider, ctx);
			}
		}

		private static NumberData fromConstant(NumberProvider provider, LootContext ctx) {
			float value = provider.getFloat(ctx);
			return new NumberData(value, value, value, false);
		}

		private static NumberData fromBinomial(NumberProvider np, LootContext ctx) {
			BinomialDistributionGenerator provider = ((BinomialDistributionGenerator) np);
			return new NumberData(0, from(provider.n, ctx).max, provider.getFloat(ctx), true);
		}

		private static NumberData fromUniform(NumberProvider np, LootContext ctx) {
			UniformGenerator provider = ((UniformGenerator) np);
			return new NumberData(from(provider.min, ctx).min, from(provider.max, ctx).max, provider.getFloat(ctx), true);
		}
	}


	@Override
	public LootItemFunctionType getType() {
		return Thermaloot.AUGMENT_AUGIFY.get();
	}

	static class LuckData {
		float luck = 0;
		float maxLuck = Float.NEGATIVE_INFINITY;
		float minLuck = Float.POSITIVE_INFINITY;
		int possibleLuck = 0;

		float getFinalLuck() {
			if (maxLuck == Float.NEGATIVE_INFINITY) return NORMAL;
			return luck / possibleLuck;
		}

		void addLuck(float luck) {
			if (luck < minLuck) minLuck = luck;
			if (luck > maxLuck) maxLuck = luck;
			this.luck += luck;
			possibleLuck++;
		}

		CompoundTag serialize(CompoundTag tag) {
			tag.putFloat(LUCK, luck);
			tag.putFloat(MAX_LUCK, maxLuck);
			tag.putFloat(MIN_LUCK, minLuck);
			tag.putInt(POSSIBLE_LUCK, possibleLuck);
			return tag;
		}

		static LuckData deserialize(CompoundTag tag) {
			LuckData luckData = new LuckData();
			if (tag.isEmpty()) return luckData;

			luckData.luck = tag.getFloat(LUCK);
			luckData.maxLuck = tag.getFloat(MAX_LUCK);
			luckData.minLuck = tag.contains(MIN_LUCK) ? tag.getFloat(MIN_LUCK) : 1;
			luckData.possibleLuck = tag.getInt(POSSIBLE_LUCK);
			return luckData;
		}
	}

	static class Serializer extends LootItemConditionalFunction.Serializer<ApplyAugmentDataFunction> {
		@SuppressWarnings("unchecked")
		private static final JsonDeserializer<NumberProvider> numberProviderDeserializer = (JsonDeserializer<NumberProvider>) NumberProviders.createGsonAdapter();

		@Override
		public ApplyAugmentDataFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootItemCondition[] conditions) {
			return new ApplyAugmentDataFunction(
					conditions,
					object.get("augments").getAsJsonObject().entrySet().stream()
							.collect(Collectors.toMap(Map.Entry::getKey, entry -> deserializeNumberProvider(deserializationContext, entry.getValue()))));
		}

		private static NumberProvider deserializeNumberProvider(JsonDeserializationContext deserializationContext, JsonElement element) {
			return numberProviderDeserializer.deserialize(element, NumberProvider.class, deserializationContext);
		}
	}
}