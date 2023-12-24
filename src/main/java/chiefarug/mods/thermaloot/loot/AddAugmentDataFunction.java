package chiefarug.mods.thermaloot.loot;

import chiefarug.mods.thermaloot.Thermaloot;
import cofh.core.util.helpers.AugmentDataHelper;
import cofh.thermal.lib.common.item.AugmentItem;
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
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.stream.Collectors;

import static chiefarug.mods.thermaloot.Thermaloot.LGGR;
import static chiefarug.mods.thermaloot.Thermaloot.Tags.LUCK_DATA;
import static chiefarug.mods.thermaloot.Thermaloot.mergeTags;
import static cofh.lib.util.constants.NBTTags.TAG_AUGMENT_DATA;

public class AddAugmentDataFunction extends LootItemConditionalFunction {

	public static final	Serializer SERIALIZER = new Serializer();

	/**
	 * Used to signify non-existent luck. (ie when the entire thing is based on constants)
	 */
	static final int NORMAL = -256;

	protected AddAugmentDataFunction(LootItemCondition[] predicates, Map<String, NumberProvider> augmentDataProviders) {
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
			NumberData numberData = NumberData.from(modifier, numberProvider, ctx);

//			if (numberData.min < 0) LGGR.error("Augment values cannot be negative! Please check your loot tables"); //todo add list of ones that can be negative, and allow them.
			// ones that can be negative: Radius, PotionAmp

			if (numberData.isLuckBased()) {
				// if you pass negatives to this it is your fault if things go funky
				luck.addLuck(numberData.getLuck());
			}
			dataBuilder.mod(modifier, numberData.getValue());
		});

		CompoundTag augmentData = dataBuilder.build();
		if (augmentData == null) return stack;

		nbt = stack.getOrCreateTag();
		CompoundTag existingAugmentData = nbt.contains(TAG_AUGMENT_DATA) ? nbt.getCompound(TAG_AUGMENT_DATA) : null;
		nbt.put(TAG_AUGMENT_DATA, mergeTags(augmentData, existingAugmentData));

		CompoundTag serializedLuck = luck.serialize();
		if (serializedLuck != null) nbt.put(LUCK_DATA, serializedLuck);

		return stack;
	}


	@Override
	public LootItemFunctionType getType() {
		return Thermaloot.AUGMENT_AUGIFY.get();
	}

	static class Serializer extends LootItemConditionalFunction.Serializer<AddAugmentDataFunction> {
		@SuppressWarnings("unchecked")
		public static final JsonDeserializer<NumberProvider> numberProviderDeserializer = (JsonDeserializer<NumberProvider>) NumberProviders.createGsonAdapter();

		@Override
		public AddAugmentDataFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootItemCondition[] conditions) {
			return new AddAugmentDataFunction(
					conditions,
					object.get("augments").getAsJsonObject().entrySet().stream()
							.collect(Collectors.toMap(Map.Entry::getKey, entry -> deserializeNumberProvider(deserializationContext, entry.getValue()))));
		}

		private static NumberProvider deserializeNumberProvider(JsonDeserializationContext deserializationContext, JsonElement element) {
			return numberProviderDeserializer.deserialize(element, NumberProvider.class, deserializationContext);
		}
	}
}