# Thermaloot
Randomised loot capacitors for use in Thermal Series machines!

Adds randomly generated and procedurally named capacitors to all sorts of loot chests. These Capacitors have a wide range of augment stats to make your machines go faster, make them more efficient, increase the range of various devices such as the Vacuumulator, or even amplifying potion effects emitted by the Decotive Defuser!

 

By default capacitors are added to these structure chests:

 - Buried Treasure
 - Desert Temple
 - Dungeon
 - End City
 - Jungle Temple
 - Mineshaft
 - Ruined Portal
 - Shipwreck Treasure
 - Stronghold Corridor
 - Woodland Mansion

With varying chances and amounts. The names are chosen based on how good the capacitor is, with better capacitors getting names that communicate this, and worse capacitors getting names that sound worse.

 

 

The mod is highly configurable. Using a Datapack, you can change the ranges of all augment values by editing the loot table thermaloot:single_capacitor. You can change what chests get added to and at what amounts using Loot Modifiers. Examples can be found here: [data/thermaloot/loot_modifiers](src/main/resources/data/thermaloot/loot_modifiers). Make sure to also register them using this file: [data/forge/loot_modifiers/global_loot_modifiers.json](src/main/resources/data/forge/loot_modifiers/global_loot_modifiers.json).

 

You can add custom names through a Resourcepack and a quick config edit! Add lang keys for them (like these existing ones: [assets/thermaloot/lang/en_us.json](src/main/resources/assets/thermaloot/lang/en_us.json#L6-L62)) in a resource pack, incrementing the number at the end by one per new name. Any that use a number that is already set by the mod (ie 7) will override that existing one. Make sure to have the same number of names for each attribute level! Once you add some, increment the number_of_name_variants number in the server config (located inside the serverconfig folder inside your world file) so it is the same as the highest lang key + 1 (because the lang keys start at 0). You can copy this config file to the defaultconfigs directory to have it apply to every new world.

 

If you are playing in a non-English language, and using a translation for that language you can disable the nest_adjectives config setting to stop generating names that nest adjectives. This works in English, but may not work for other languages. The config file can be found in the serverconfig folder inside your worlds folder. If playing on a server the server owner will need to edit it.
