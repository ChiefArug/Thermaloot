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
                    ICodecifiedNumberProvidersForYouMojangHopeYoureHappy.NUMBER_PROVIDER.optionalFieldOf("count", SINGLE_COUNT).forGetter(AddACapacitorModifier::getCountProvider)
            ).apply(instance, (a, b) -> new AddACapacitorModifier(a, b))
    );

    private final NumberProvider countProvider;
    protected AddACapacitorModifier(LootItemCondition[] conditionsIn, NumberProvider countProvider) {
        super(conditionsIn);
        this.countProvider = countProvider;
    }

    private boolean noRecurse = false;
    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
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

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
