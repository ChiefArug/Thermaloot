package chiefarug.mods.thermaloot.loot;

import chiefarug.mods.thermaloot.ICodecifiedNumberProvidersForYouMojangHopeYoureHappy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import static chiefarug.mods.thermaloot.Thermaloot.MODID;

public class AddACapacitorModifier extends LootModifier {

    private static final NumberProvider SINGLE_COUNT = ConstantValue.exactly(1);
    public static final ResourceLocation SINGLE_CAPACITOR = new ResourceLocation(MODID, "single_capacitor");


    public static final Codec<AddACapacitorModifier> CODEC = RecordCodecBuilder.create(
            instance -> LootModifier.codecStart(instance).and(
                    ICodecifiedNumberProvidersForYouMojangHopeYoureHappy.NUMBER_PROVIDER.optionalFieldOf("count", SINGLE_COUNT).forGetter(AddACapacitorModifier::getCountProvider)//).and( //TODO: use NullableFieldCodec from https://github.com/Commoble/databuddy/blob/b52374e6b8177ab1a360b74c3b31c7b268741473/src/main/java/commoble/databuddy/codec/NullableFieldCodec.java#L46 here so that it doesn't just SILENTLY fail
//                    Codec.FLOAT.fieldOf("replace_chance").forGetter(AddACapacitorModifier::getReplaceChance)
            ).apply(instance, AddACapacitorModifier::new)
    );

    private final NumberProvider countProvider;
//    private final float replaceChance;
    protected AddACapacitorModifier(LootItemCondition[] conditionsIn, NumberProvider countProvider/*, float replaceChance*/) {
        super(conditionsIn);
        this.countProvider = countProvider;
//        this.replaceChance = replaceChance;
    }

    private boolean noRecurse = false;
    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (noRecurse || context.getQueriedLootTableId().equals(SINGLE_CAPACITOR)) return generatedLoot;
         
        int repeats = countProvider.getInt(context);
//        List<ItemStack> stackedStacks = generatedLoot.stream().collect(Collectors.groupingBy(ItemStack::getItem)).values().stream().map(itemStacks -> {
//            ItemStack stack = itemStacks.get(0).copy();
//            stack.setCount(itemStacks.size());
//            return stack;
//        }).sorted(Comparator.comparingInt(ItemStack::getCount)).toList();

        noRecurse = true;
        while (repeats-- >= 1)
            generatedLoot.addAll(context.getLevel().getServer().getLootTables().get(SINGLE_CAPACITOR).getRandomItems(context));
//            if (context.getRandom().nextFloat() < replaceChance) {
//                stackedStacks.
//            } else
//                generatedLoot.addAll(context.getLevel().getServer().getLootTables().get(SINGLE_CAPACITOR).getRandomItems(context));
        noRecurse = false;

        return generatedLoot;
    }

    public NumberProvider getCountProvider() {
        return countProvider;
    }

//    public float getReplaceChance() {
//        return replaceChance;
//    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
