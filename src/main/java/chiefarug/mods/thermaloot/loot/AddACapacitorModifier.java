package chiefarug.mods.thermaloot.loot;

import chiefarug.mods.thermaloot.ICodecifiedNumberProvidersForYouMojangHopeYoureHappy;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.common.loot.LootModifierManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static chiefarug.mods.thermaloot.Thermaloot.MODID;
import static chiefarug.mods.thermaloot.loot.LootConditionsCodec.LOOT_CONDITIONS_CODEC;

public class AddACapacitorModifier extends LootModifier {

    private static final NumberProvider SINGLE_COUNT = ConstantValue.exactly(1);
    public static final ResourceLocation SINGLE_CAPACITOR = new ResourceLocation(MODID, "single_capacitor");


    public static final Codec<AddACapacitorModifier> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions),
                    ICodecifiedNumberProvidersForYouMojangHopeYoureHappy.NUMBER_PROVIDER.optionalFieldOf("count", SINGLE_COUNT).forGetter(AddACapacitorModifier::getCountProvider) //TODO: use NullableFieldCodec from https://github.com/Commoble/databuddy/blob/b52374e6b8177ab1a360b74c3b31c7b268741473/src/main/java/commoble/databuddy/codec/NullableFieldCodec.java#L46 here so that it doesn't just SILENTLY fail
            ).apply(instance, AddACapacitorModifier::new)
    );

    private final NumberProvider countProvider;
    protected AddACapacitorModifier(LootItemCondition[] conditionsIn, NumberProvider countProvider) {
        super(conditionsIn);
        this.countProvider = countProvider;
    }

    private boolean noRecurse = false;

    public static GlobalLootModifierSerializer<AddACapacitorModifier> serializer() {
        return SERIALIZER;
    }
    private static final GlobalLootModifierSerializer<AddACapacitorModifier> SERIALIZER = new GlobalLootModifierSerializer<AddACapacitorModifier>() {
        @Override
        public AddACapacitorModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] ailootcondition) {
            DataResult<AddACapacitorModifier> result = CODEC.parse(JsonOps.INSTANCE, object);
            return result.result().get();
        }

        @Override
        public JsonObject write(AddACapacitorModifier instance) {
            DataResult<JsonElement> result = CODEC.encodeStart(JsonOps.INSTANCE, instance);
            return result.result().get().getAsJsonObject();
        }
    };

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        if (noRecurse || context.getQueriedLootTableId().equals(SINGLE_CAPACITOR)) return generatedLoot;
         
        int repeats = countProvider.getInt(context);
        noRecurse = true;
        while (repeats-- >= 1)
            generatedLoot.addAll(context.getLevel().getServer().getLootTables().get(SINGLE_CAPACITOR).getRandomItems(context));
        noRecurse = false;

        return generatedLoot;
    }

    public NumberProvider getCountProvider() {
        return countProvider;
    }

    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
