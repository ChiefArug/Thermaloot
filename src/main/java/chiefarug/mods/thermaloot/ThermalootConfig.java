package chiefarug.mods.thermaloot;

import net.minecraftforge.common.ForgeConfigSpec;

public class ThermalootConfig {

	public static final ForgeConfigSpec.ConfigValue<Integer> NUMBER_OF_TRANSLATIONS;

	public static final ForgeConfigSpec.ConfigValue<Double> TERRIBLE_BAD;
	public static final ForgeConfigSpec.ConfigValue<Double> BAD_GOOD;
	public static final ForgeConfigSpec.ConfigValue<Double> GOOD_AMAZING;

	public static final ForgeConfigSpec.ConfigValue<Boolean> NEST_ADJECTIVES;
	public static final ForgeConfigSpec.ConfigValue<Double> EXTRA_LUCKY;
	public static final ForgeConfigSpec.ConfigValue<Double> EXTRA_UNLUCKY;

	static final ForgeConfigSpec spec;
	static {
		var builder = new ForgeConfigSpec.Builder();

		builder.comment("Thermaloot Config", "---------------");

		NUMBER_OF_TRANSLATIONS = builder.comment("The number of translations provided in a resource pack. Note that if this is higher than the number of actual translation keys per naming tier that a client has then the names will show up as raw lang keys.")
				.define("number_of_name_variants", 14);

		builder.push("name_qualities")
				.comment("How lucky/unlucky something needs to be to get a name from that tier.");
		TERRIBLE_BAD = builder.comment("The amount of luck to go from a terrible name to a bad name")
				.defineInRange("terrible_bad", 0.25, 0, 1);
		BAD_GOOD = builder.comment("The amount of luck to go from a terrible name to a bad name")
				.defineInRange("bad_good", 0.5, 0, 1);
		GOOD_AMAZING = builder.comment("The amount of luck to go from a terrible name to a bad name")
				.defineInRange("good_amazing", 0.75, 0, 1);
		builder.pop();

		builder.push("extra_names");
			NEST_ADJECTIVES = builder.comment("Used to disable the nesting of names, like Amazing Soggy Thing, because that may not work in some languages")
					.define("nest_adjectives", true);
			EXTRA_LUCKY = builder.comment("How lucky a capacitor needs to be to get a second name")
					.defineInRange("extra_lucky", 0.98, 0, 1);
			EXTRA_UNLUCKY = builder.comment("How unlucky a capacitor needs to be to get a second (or third) name")
					.defineInRange("extra_unlucky", 0.02, 0, 1);
		builder.pop();


		spec = builder.build();
	}
}
