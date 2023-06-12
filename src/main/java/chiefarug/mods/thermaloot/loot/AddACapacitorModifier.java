package chiefarug.mods.thermaloot.loot;

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

import static chiefarug.mods.thermaloot.ICodecifiedNumberProvidersForYouMojangHopeYoureHappy.NUMBER_PROVIDER;
import static chiefarug.mods.thermaloot.Thermaloot.MODID;

public class AddACapacitorModifier extends LootModifier {

    private static final NumberProvider SINGLE_COUNT = ConstantValue.exactly(1);
    public static final ResourceLocation SINGLE_CAPACITOR = new ResourceLocation(MODID, "single_capacitor");




    public static final Codec<AddACapacitorModifier> CODEC = RecordCodecBuilder.create(
            instance -> LootModifier.codecStart(instance).and(
                    NUMBER_PROVIDER.optionalFieldOf("repeats", SINGLE_COUNT).forGetter(AddACapacitorModifier::getCountProvider)
            ).apply(instance, AddACapacitorModifier::new)
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

        noRecurse = true;
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
