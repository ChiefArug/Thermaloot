package chiefarug.mods.thermaloot.loot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifierManager;

public class LootConditionsCodec {

	/*
    CODE BELOW COPIED FROM FORGE 1.19.2 AND AS SUCH IS LICENSED DIFFERENTLY
    Original Source: https://github.com/MinecraftForge/MinecraftForge/blob/1.19.x/src/main/java/net/minecraftforge/common/loot/IGlobalLootModifier.java#L34-L69
      * Copyright (c) Forge Development LLC and contributors
      * SPDX-License-Identifier: LGPL-2.1-only
     */
    private static final Gson GSON_INSTANCE = Deserializers.createFunctionSerializer().create();
    public static final Codec<LootItemCondition[]> LOOT_CONDITIONS_CODEC = Codec.PASSTHROUGH.flatXmap(d -> {
        try {
            LootItemCondition[] conditions = GSON_INSTANCE.fromJson(getJson(d), LootItemCondition[].class);
            return DataResult.success(conditions);
        } catch (JsonSyntaxException e) {
            LootModifierManager.LOGGER.warn("Unable to decode loot conditions", e);
            return DataResult.error(e.getMessage());
        }
    }, conditions -> {
        try {
            JsonElement element = GSON_INSTANCE.toJsonTree(conditions);
            return DataResult.success(new Dynamic<>(JsonOps.INSTANCE, element));
        } catch (JsonSyntaxException e) {
            LootModifierManager.LOGGER.warn("Unable to encode loot conditions", e);
            return DataResult.error(e.getMessage());
        }
    });

    static <U> JsonElement getJson(Dynamic<?> dynamic) {
        Dynamic<U> typed = (Dynamic<U>) dynamic;
        return typed.getValue() instanceof JsonElement ?
                (JsonElement) typed.getValue() :
                typed.getOps().convertTo(JsonOps.INSTANCE, typed.getValue());
    }
}
