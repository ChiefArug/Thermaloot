package chiefarug.mods.thermaloot;

import chiefarug.mods.thermaloot.loot.ApplyAugmentDataFunction;
import chiefarug.mods.thermaloot.loot.NameFunction;
import cofh.thermal.lib.common.ThermalItemGroups;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Mod("thermaloot")
@Mod.EventBusSubscriber(modid = Thermaloot.MODID)
public class Thermaloot {
    @SuppressWarnings("unused")
    public static final Logger LGGR = LogUtils.getLogger();
    public static final String MODID = "thermaloot";

    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTION_REGISTRY = DeferredRegister.create(Registry.LOOT_FUNCTION_REGISTRY, Thermaloot.MODID);
    public static final RegistryObject<LootItemFunctionType> AUGMENT_AUGIFY = LOOT_FUNCTION_REGISTRY.register("apply_augment_data", () -> new LootItemFunctionType(ApplyAugmentDataFunction.SERIALIZER));
    public static final RegistryObject<LootItemFunctionType> AUGMENT_NAME = LOOT_FUNCTION_REGISTRY.register("name" ,() -> new LootItemFunctionType(NameFunction.SERIALIZER));
    public static final DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> VARIABLE_CAPACITOR = ITEM_REGISTRY.register("variable_capacitor", () -> new Item(new Item.Properties().tab(ThermalItemGroups.THERMAL_ITEMS)));

    public Thermaloot() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ThermalootConfig.spec, "thermaloot-server.toml");

        LOOT_FUNCTION_REGISTRY.register(modBus);
        ITEM_REGISTRY.register(modBus);
        //todo add stuff to loot tables by default by loot modifier
    }

    /**
     * @param first  The first tag
     * @param second The second tag. Will override any values in first
     * @return A new tag
     */
    @Nullable
    @Contract("null,null -> null;!null,_ -> !null;_,!null -> !null")
    public static CompoundTag mergeTags(@Nullable CompoundTag first, @Nullable CompoundTag second) {
        if (first == null && second == null) return null;
        if (first == null)
            return second.copy();
        else if (second == null)
            return first.copy();
        return first.copy().merge(second);
    }

    public static class Tags {
        public static final String LUCK_DATA = "LuckData";
        public static final String LUCK = "Luck";
        public static final String MAX_LUCK = "MaxLuck";
        public static final String MIN_LUCK = "MinLuck";
        public static final String POSSIBLE_LUCK = "PossibleLuck";
    }
}
