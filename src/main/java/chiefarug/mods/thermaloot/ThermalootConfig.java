package chiefarug.mods.thermaloot;

import net.minecraftforge.common.ForgeConfigSpec;

public class ThermalootConfig {

	public static final ForgeConfigSpec.ConfigValue<Integer> NUMBER_OF_TRANSLATIONS;
	public static final ForgeConfigSpec.ConfigValue<Boolean> NEST_ADJECTIVES;

	static final ForgeConfigSpec spec;
	static {
		var builder = new ForgeConfigSpec.Builder();

		builder.comment("Thermaloot Config", "---------------");

		NUMBER_OF_TRANSLATIONS = builder.comment("The number of translations provided in a resource pack. Note that if this is higher than the number of actual translation keys per naming tier that a client has then the names will show up as raw lang keys.")
				.define("number_of_name_variants", 14);
		NEST_ADJECTIVES = builder.comment("Used to disable the nesting of names, like Amazing (Soggy (Thing)), because that may not work in some languages")
				.define("nest_adjectives", true);

		spec = builder.build();
	}
}
