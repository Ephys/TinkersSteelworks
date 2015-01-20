package toops.tsteelworks.common.core;

import cpw.mods.fml.common.registry.GameRegistry;
import mantle.Mantle;
import mantle.utils.RecipeRemover;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import tconstruct.TConstruct;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.Detailing;
import tconstruct.library.crafting.FluidType;
import tconstruct.library.crafting.LiquidCasting;
import tconstruct.library.crafting.Smeltery;
import tconstruct.smeltery.TinkerSmeltery;
import tconstruct.tools.TinkerTools;
import toops.tsteelworks.api.highoven.IHighOvenFuelRegistry;
import toops.tsteelworks.common.blocks.HighOvenBlock;
import toops.tsteelworks.lib.ModsData;
import toops.tsteelworks.lib.registry.AdvancedSmelting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TSRecipes {
	/*
	 * Common Patterns
	 */
	public static final String[] PAT_BLOCK = {"###", "###", "###"};
	public static final String[] PAT_SMALL_BLOCK = {"##", "##"};
	public static final String[] PAT_SLAB = {"###"};
	public static final String[] PAT_HOLLOW = {"###", "# #", "###"};
	public static final String[] PAT_SURROUND = {"###", "#m#", "###"};

	public static final String[] PAT_HEAD = {"###", "# #"};
	public static final String[] PAT_CHEST = {"# #", "###", "###"};
	public static final String[] PAT_LEGS = {"###", "# #", "# #"};
	public static final String[] PAT_BOOTS = {"# #", "# #"};

	public static final int INGOT_LIQUID_VALUE = TConstruct.ingotLiquidValue;
	public static int ORE_LIQUID_VALUE = INGOT_LIQUID_VALUE;
	public static final int BLOCK_LIQUID_VALUE = TConstruct.blockLiquidValue;
	public static final int NUGGET_LIQUID_VALUE = TConstruct.nuggetLiquidValue;

	public static void setupRecipes() {
		addHighOvenSmelts();
		addHighOvenFuels();
		createAlloys();

		createRecipes();
	}

	private static void addHighOvenFuels() {
		IHighOvenFuelRegistry.INSTANCE.addFuel(new ItemStack(Items.coal, 1, 1), 420, 4);
		for(final ItemStack charcoalBlock : OreDictionary.getOres("blockCharcoal")){
			IHighOvenFuelRegistry.INSTANCE.addFuel(charcoalBlock, 4200, 7);
		}

		if (ModsData.Railcraft.isLoaded) {
			IHighOvenFuelRegistry.INSTANCE.addFuel(ModsData.Railcraft.coalCoke, 840, 10);
			IHighOvenFuelRegistry.INSTANCE.addFuel(ModsData.Railcraft.coalCokeBlock, 8400, 15);
		}

		if (ModsData.Thaumcraft.isLoaded) {
			IHighOvenFuelRegistry.INSTANCE.addFuel(ModsData.Thaumcraft.alumentum, 420 * 4, 4);
		}
	}

	public static void addHighOvenSmelts() {
		final List<String> exceptions = Arrays.asList("Water", "Stone", "Ender", "Glass", "Slime", "Obsidian");

		for (Map.Entry<String, FluidType> entry : FluidType.fluidTypes.entrySet()) {
			if (exceptions.contains(entry.getKey())) continue;

			final int temperatureMod = getFluidTempMod(entry.getKey());
			AdvancedSmelting.addDictionaryMelting("nugget" + entry.getKey(), entry.getValue(), temperatureMod, NUGGET_LIQUID_VALUE);
			AdvancedSmelting.addDictionaryMelting("ingot" + entry.getKey(), entry.getValue(), temperatureMod, INGOT_LIQUID_VALUE);
			AdvancedSmelting.addDictionaryMelting("dust" + entry.getKey(), entry.getValue(), temperatureMod, INGOT_LIQUID_VALUE);
			AdvancedSmelting.addDictionaryMelting("crystalline" + entry.getKey(), entry.getValue(), temperatureMod, INGOT_LIQUID_VALUE);
			AdvancedSmelting.addDictionaryMelting("ore" + entry.getKey(), entry.getValue(), temperatureMod, ORE_LIQUID_VALUE);
			AdvancedSmelting.addDictionaryMelting("oreNether" + entry.getKey(), entry.getValue(), temperatureMod, ORE_LIQUID_VALUE * 2);
			AdvancedSmelting.addDictionaryMelting("block" + entry.getKey(), entry.getValue(), temperatureMod, BLOCK_LIQUID_VALUE);
		}

		FluidType obsidian = FluidType.getFluidType("Obsidian");
		if (obsidian != null) {
			String name = "Obsidian";

			final int temperatureMod = getFluidTempMod(name);
			AdvancedSmelting.addDictionaryMelting("nugget" + name, obsidian, temperatureMod, NUGGET_LIQUID_VALUE);
			AdvancedSmelting.addDictionaryMelting("ingot" + name, obsidian, temperatureMod, INGOT_LIQUID_VALUE);
			AdvancedSmelting.addDictionaryMelting("dust" + name, obsidian, temperatureMod, INGOT_LIQUID_VALUE / 4);
			AdvancedSmelting.addDictionaryMelting("crystalline" + name, obsidian, temperatureMod, INGOT_LIQUID_VALUE);
			AdvancedSmelting.addDictionaryMelting("ore" + name, obsidian, temperatureMod, ORE_LIQUID_VALUE);
			AdvancedSmelting.addDictionaryMelting("oreNether" + name, obsidian, temperatureMod, ORE_LIQUID_VALUE * 2);
			AdvancedSmelting.addDictionaryMelting("block" + name, obsidian, temperatureMod, BLOCK_LIQUID_VALUE);
		}

		AdvancedSmelting.addMelting(Items.emerald, 0, getFluidTempMod("Emerald"), new FluidStack(TinkerSmeltery.moltenEmeraldFluid, INGOT_LIQUID_VALUE), false);
		AdvancedSmelting.addMelting(TinkerSmeltery.glueBlock, 0, getFluidTempMod("Glue"), new FluidStack(TinkerSmeltery.glueFluid, BLOCK_LIQUID_VALUE), false);

		FluidType limestoneFluid = FluidType.getFluidType("Limestone");
		Smeltery.addDictionaryMelting("blockLimestone", limestoneFluid, 825, INGOT_LIQUID_VALUE * 4);
		AdvancedSmelting.addDictionaryMelting("blockLimestone", limestoneFluid, 825, INGOT_LIQUID_VALUE * 4);

		Smeltery.addMelting(new ItemStack(TSContent.materialsTS, 1, 1), TSContent.limestoneBlock, 1, 0, new FluidStack(ModsData.Fluids.moltenLimestoneFluid, INGOT_LIQUID_VALUE));
		AdvancedSmelting.addMelting(new ItemStack(TSContent.materialsTS, 1, 1), 825, new FluidStack(ModsData.Fluids.moltenLimestoneFluid, INGOT_LIQUID_VALUE), false);
		Smeltery.addMelting(new ItemStack(TSContent.materialsTS, 1, 2), TSContent.limestoneBlock, 1, 0, new FluidStack(ModsData.Fluids.moltenLimestoneFluid, INGOT_LIQUID_VALUE));
		AdvancedSmelting.addMelting(new ItemStack(TSContent.materialsTS, 1, 2), 825, new FluidStack(ModsData.Fluids.moltenLimestoneFluid, INGOT_LIQUID_VALUE), false);
	}

	public static void createAlloys() {
		Smeltery.addAlloyMixing(
				new FluidStack(TSContent.liquidCementFluid, 1),
				new FluidStack(TinkerSmeltery.moltenStoneFluid, 1),
				new FluidStack(TSContent.moltenLimestoneFluid, 1)
		);
	}

	public static void createRecipes() {
		craftManual();
		craftScorchedStone();
		craftLimestone();
		craftStone();
		craftStorageBlocks();
		craftSteel();
		craftPigIron();
		craftObsidian();
		craftGlass();
		craftWater();
		craftMachines();

		if (ConfigCore.hardcorePiston)
			TSRecipes.changePiston();

		if (ConfigCore.hardcoreFlintAndSteel)
			TSRecipes.changeFlintAndSteel();

		if (ConfigCore.hardcoreAnvil)
			TSRecipes.changeAnvil();


		final ItemStack netherQuartz = new ItemStack(Items.quartz, 2);
		AdvancedSmelting.registerMixComboForSolidOutput(netherQuartz, TinkerSmeltery.moltenGlassFluid, "dustGunpowder", "oreberryEssence", "blockGraveyardDirt");
	}

	public static void craftMachines() {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(TSContent.steamTurbine), "aca", "#d#", "#r#", '#', "ingotBronze", 'a', "ingotAluminumBrass", 'c', "ingotSteel", 'r', new ItemStack(Items.redstone), 'd', new ItemStack(Blocks.piston)));
	}

	public static void craftManual() {
		final LiquidCasting tableCasting = TConstructRegistry.getTableCasting();

		final ItemStack manual = new ItemStack(TSContent.bookManual, 1, 0);
		final FluidStack fluidStoneMinor = new FluidStack(TinkerSmeltery.moltenStoneFluid, 8);

		tableCasting.addCastingRecipe(manual, fluidStoneMinor, new ItemStack(TinkerTools.manualBook, 1, 0), true, 50);
		tableCasting.addCastingRecipe(manual, fluidStoneMinor, new ItemStack(TinkerTools.manualBook, 1, 1), true, 50);
		tableCasting.addCastingRecipe(manual, fluidStoneMinor, new ItemStack(TinkerTools.manualBook, 1, 2), true, 50);
		tableCasting.addCastingRecipe(manual, fluidStoneMinor, new ItemStack(TinkerTools.manualBook, 1, 3), true, 50);

		tableCasting.addCastingRecipe(manual, fluidStoneMinor, new ItemStack(Mantle.mantleBook, 1), true, 50);
		tableCasting.addCastingRecipe(manual, fluidStoneMinor, new ItemStack(Items.book, 1), true, 50);
	}

	public static void craftScorchedStone() {
		final LiquidCasting basinCasting = TConstructRegistry.getBasinCasting();
		final LiquidCasting tableCasting = TConstructRegistry.getTableCasting();

		final Detailing chiseling = TConstructRegistry.getChiselDetailing();

		final ItemStack itemScorchedBrick = new ItemStack(TSContent.materialsTS, 1, 0);
		final ItemStack blockScorchedBrick = new ItemStack(TSContent.highoven, 1, 2);
		final FluidStack fluidStoneMinor = new FluidStack(TinkerSmeltery.moltenStoneFluid, 8);
		final FluidStack fluidStoneChunk = new FluidStack(TinkerSmeltery.moltenStoneFluid, 32);
		// High Oven / Deep Tank Components
		GameRegistry.addRecipe(new ItemStack(TSContent.highoven, 1, HighOvenBlock.META_HIGHOVEN), PAT_HOLLOW, '#', itemScorchedBrick);
		GameRegistry.addRecipe(new ItemStack(TSContent.highoven, 1, HighOvenBlock.META_DRAIN), "b b", "b b", "b b", 'b', itemScorchedBrick);
		GameRegistry.addRecipe(blockScorchedBrick, PAT_SMALL_BLOCK, '#', itemScorchedBrick);
		GameRegistry.addRecipe(new ItemStack(TSContent.highoven, 1, HighOvenBlock.META_DUCT), "bbb", "   ", "bbb", 'b', itemScorchedBrick);
		GameRegistry.addRecipe(new ItemStack(TSContent.highoven, 1, HighOvenBlock.META_TANK), PAT_SURROUND, '#', itemScorchedBrick, 'm', new ItemStack(Items.dye, 1, 4));
		GameRegistry.addSmelting(new ItemStack(TSContent.highoven, 1, HighOvenBlock.META_COBBLE), new ItemStack(TSContent.highoven, 1, HighOvenBlock.META_STONE), 2);
		GameRegistry.addSmelting(new ItemStack(TSContent.highoven, 1, HighOvenBlock.META_BRICK), new ItemStack(TSContent.highoven, 1, HighOvenBlock.META_CRACKED), 2);

		// Slabs
		GameRegistry.addRecipe(new ItemStack(TSContent.scorchedSlab, 6, 0), PAT_SLAB, '#', new ItemStack(TSContent.highoven, 1, 2));
		GameRegistry.addRecipe(new ItemStack(TSContent.scorchedSlab, 6, 1), PAT_SLAB, '#', new ItemStack(TSContent.highoven, 1, 4));
		GameRegistry.addRecipe(new ItemStack(TSContent.scorchedSlab, 6, 2), PAT_SLAB, '#', new ItemStack(TSContent.highoven, 1, 5));
		GameRegistry.addRecipe(new ItemStack(TSContent.scorchedSlab, 6, 3), PAT_SLAB, '#', new ItemStack(TSContent.highoven, 1, 6));
		GameRegistry.addRecipe(new ItemStack(TSContent.scorchedSlab, 6, 4), PAT_SLAB, '#', new ItemStack(TSContent.highoven, 1, 8));
		GameRegistry.addRecipe(new ItemStack(TSContent.scorchedSlab, 6, 5), PAT_SLAB, '#', new ItemStack(TSContent.highoven, 1, 9));
		GameRegistry.addRecipe(new ItemStack(TSContent.scorchedSlab, 6, 6), PAT_SLAB, '#', new ItemStack(TSContent.highoven, 1, 10));
		GameRegistry.addRecipe(new ItemStack(TSContent.scorchedSlab, 6, 7), PAT_SLAB, '#', new ItemStack(TSContent.highoven, 1, 11));
		// Recipes to obtain bricks from high oven
		String[] oxidizers = {"fuelCoal", "coal", "dustCoal"};
		String[] purifiers = {"blockSand", "Sandblock", "sand"};

		for (String o : oxidizers)
			for (String p : purifiers)
				AdvancedSmelting.registerMixComboForSolidOutput(itemScorchedBrick, TinkerSmeltery.moltenStoneFluid, o, null, p);

		// Casting
		tableCasting.addCastingRecipe(itemScorchedBrick, fluidStoneMinor, new ItemStack(Items.brick), true, 50);
		basinCasting.addCastingRecipe(blockScorchedBrick, fluidStoneChunk, new ItemStack(Blocks.brick_block), true, 100);

		// Chiseling
		chiseling.addDetailing(TSContent.highoven, 4, TSContent.highoven, 6, TinkerTools.chisel);
		chiseling.addDetailing(TSContent.highoven, 6, TSContent.highoven, 11, TinkerTools.chisel);
		chiseling.addDetailing(TSContent.highoven, 11, TSContent.highoven, 2, TinkerTools.chisel);
		chiseling.addDetailing(TSContent.highoven, 2, TSContent.highoven, 8, TinkerTools.chisel);
		chiseling.addDetailing(TSContent.highoven, 8, TSContent.highoven, 9, TinkerTools.chisel);
		chiseling.addDetailing(TSContent.highoven, 9, TSContent.highoven, 10, TinkerTools.chisel);
		chiseling.addDetailing(TSContent.highoven, 10, TSContent.highoven, 4, TinkerTools.chisel);
	}

	public static void craftLimestone() {
		final Fluid fluid = TSContent.moltenLimestoneFluid;

		GameRegistry.addRecipe(new ItemStack(TSContent.limestoneBlock, 1, 2), PAT_SMALL_BLOCK, '#', new ItemStack(TSContent.materialsTS, 1, 1));

		// add to smelt
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(TSContent.limestoneBlock, 1, 1), new ItemStack(TSContent.limestoneBlock, 1, 0), 2f);
		FurnaceRecipes.smelting().func_151394_a(new ItemStack(TSContent.limestoneBlock, 1, 0), new ItemStack(TSContent.materialsTS, 4, 1), 2f);

		AdvancedSmelting.registerMixComboForSolidOutput(new ItemStack(TSContent.materialsTS, 1, 1), TinkerSmeltery.moltenStoneFluid, "dyeLime", null, "blockSand");

		GameRegistry.addRecipe(new ItemStack(TSContent.limestoneSlab, 6, 0), PAT_SLAB, '#', new ItemStack(TSContent.limestoneBlock, 1, 0));
		GameRegistry.addRecipe(new ItemStack(TSContent.limestoneSlab, 6, 1), PAT_SLAB, '#', new ItemStack(TSContent.limestoneBlock, 1, 1));
		GameRegistry.addRecipe(new ItemStack(TSContent.limestoneSlab, 6, 2), PAT_SLAB, '#', new ItemStack(TSContent.limestoneBlock, 1, 2));
		GameRegistry.addRecipe(new ItemStack(TSContent.limestoneSlab, 6, 3), PAT_SLAB, '#', new ItemStack(TSContent.limestoneBlock, 1, 4));
		GameRegistry.addRecipe(new ItemStack(TSContent.limestoneSlab, 6, 4), PAT_SLAB, '#', new ItemStack(TSContent.limestoneBlock, 1, 5));
		GameRegistry.addRecipe(new ItemStack(TSContent.limestoneSlab, 6, 5), PAT_SLAB, '#', new ItemStack(TSContent.limestoneBlock, 1, 6));
		GameRegistry.addRecipe(new ItemStack(TSContent.limestoneSlab, 6, 6), PAT_SLAB, '#', new ItemStack(TSContent.limestoneBlock, 1, 7));
		GameRegistry.addRecipe(new ItemStack(TSContent.limestoneSlab, 6, 7), PAT_SLAB, '#', new ItemStack(TSContent.limestoneBlock, 1, 8));

		TConstructRegistry.getTableCasting().addCastingRecipe(new ItemStack(TSContent.materialsTS, 1, 1), new FluidStack(fluid, INGOT_LIQUID_VALUE), new ItemStack(TinkerSmeltery.metalPattern), false, 25);
		TConstructRegistry.getBasinCasting().addCastingRecipe(new ItemStack(TSContent.limestoneBlock), new FluidStack(fluid, INGOT_LIQUID_VALUE * 4), 100);

		final Detailing chiseling = TConstructRegistry.getChiselDetailing();
		chiseling.addDetailing(TSContent.limestoneBlock, 2, TSContent.limestoneBlock, 3, TinkerTools.chisel);
		chiseling.addDetailing(TSContent.limestoneBlock, 3, TSContent.limestoneBlock, 4, TinkerTools.chisel);
		chiseling.addDetailing(TSContent.limestoneBlock, 4, TSContent.limestoneBlock, 5, TinkerTools.chisel);
		chiseling.addDetailing(TSContent.limestoneBlock, 5, TSContent.limestoneBlock, 6, TinkerTools.chisel);
		chiseling.addDetailing(TSContent.limestoneBlock, 6, TSContent.limestoneBlock, 7, TinkerTools.chisel);
		chiseling.addDetailing(TSContent.limestoneBlock, 7, TSContent.limestoneBlock, 8, TinkerTools.chisel);
		chiseling.addDetailing(TSContent.limestoneBlock, 8, TSContent.limestoneBlock, 3, TinkerTools.chisel);
	}

	public static void craftStone() {
		final String fluidName = "Stone";
		final Fluid fluid = TinkerSmeltery.moltenStoneFluid;

		AdvancedSmelting.addMelting(Blocks.stone, 0, getFluidTempMod(fluidName), new FluidStack(fluid, INGOT_LIQUID_VALUE / 18), false);
		AdvancedSmelting.addMelting(Blocks.cobblestone, 0, getFluidTempMod(fluidName), new FluidStack(fluid, INGOT_LIQUID_VALUE / 18), false);
		AdvancedSmelting.addMelting(TinkerTools.craftedSoil, 1, getFluidTempMod(fluidName), new FluidStack(fluid, INGOT_LIQUID_VALUE / 4), false);

		Smeltery.addMelting(FluidType.getFluidType(fluidName), new ItemStack(TinkerTools.materials, 1, 2), 0, INGOT_LIQUID_VALUE);
		AdvancedSmelting.addMelting(new ItemStack(TinkerTools.materials, 1, 2), getFluidTempMod(fluidName), new FluidStack(fluid, INGOT_LIQUID_VALUE), false);

		final String[] dyes = new String[] { "dyeWhite", "dyeOrange", "dyeMagenta", "dyeLightBlue", "dyeYellow", "dyeLime", "dyePink", "dyeGray", "dyeLightGray", "dyeCyan", "dyePurple", "dyeBlue", "dyeBrown", "dyeGreen", "dyeRed", "dyeBlack" };

		for (int i = 0; i < dyes.length; i++) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(TSContent.cementBlock, 8, i),
					"ccc",
					"cdc",
					"ccc",
					'c', TSContent.cementBlock,
					'd', dyes[i]
			));
		}

		for (int meta = 2; meta <= 11; meta ++) {
			if (meta == 3)
				continue;

			Smeltery.addMelting(new ItemStack(TinkerSmeltery.smeltery, 1, meta), getFluidTempMod(fluidName), new FluidStack(fluid, INGOT_LIQUID_VALUE));
			AdvancedSmelting.addMelting(new ItemStack(TinkerSmeltery.smeltery, 1, meta), getFluidTempMod(fluidName), new FluidStack(fluid, INGOT_LIQUID_VALUE), false);
		}
	}

	public static void craftStorageBlocks() {
		GameRegistry.addRecipe(ModsData.Shared.charcoalBlock, PAT_BLOCK, '#', new ItemStack(Items.coal, 1, 1));
		GameRegistry.addRecipe(new ItemStack(TSContent.dustStorageBlock, 1, 0), PAT_BLOCK, '#', new ItemStack(Items.gunpowder, 1));
		GameRegistry.addRecipe(new ItemStack(TSContent.dustStorageBlock, 1, 1), PAT_BLOCK, '#', new ItemStack(Items.sugar, 1));
		GameRegistry.addRecipe(new ItemStack(Items.coal, 9, 1), "#", '#', ModsData.Shared.charcoalBlock);
		GameRegistry.addRecipe(new ItemStack(Items.gunpowder, 9), "#", '#', new ItemStack(TSContent.dustStorageBlock, 1, 0));
		GameRegistry.addRecipe(new ItemStack(Items.sugar, 9), "#", '#', new ItemStack(TSContent.dustStorageBlock, 1, 1));
	}

	public static void craftSteel() {
		if (ConfigCore.enableSteelArmor) {
			final ItemStack ingotSteel = TConstructRegistry.getItemStack("ingotSteel");

			GameRegistry.addRecipe(new ShapedOreRecipe(TSContent.helmetSteel, PAT_HEAD, '#', ingotSteel));
			GameRegistry.addRecipe(new ShapedOreRecipe(TSContent.chestplateSteel, PAT_CHEST, '#', ingotSteel));
			GameRegistry.addRecipe(new ShapedOreRecipe(TSContent.leggingsSteel, PAT_LEGS, '#', ingotSteel));
			GameRegistry.addRecipe(new ShapedOreRecipe(TSContent.bootsSteel, PAT_BOOTS, '#', ingotSteel));
		}

		String[] oxidizers = {"dustGunpowder", "dustSulphur", "dustSulfur", "dustSaltpeter", "dustCoal"};
		String[] reducers = {"dustRedstone", "dustManganese", "dustAluminum", "dustAluminium"};
		String[] purifiers = {"blockSand"};

		FluidStack steel = new FluidStack(TinkerSmeltery.moltenSteelFluid, 1000);
		for (String o : oxidizers)
			for (String r : reducers)
				for (String p : purifiers)
					AdvancedSmelting.registerMixComboForFluidOutput(steel, TinkerSmeltery.moltenIronFluid, o, r, p);
	}

	public static void craftPigIron() {
		AdvancedSmelting.registerMixComboForFluidOutput(new FluidStack(TinkerSmeltery.pigIronFluid, 1000), TinkerSmeltery.moltenIronFluid, "dustSugar", "dyeWhite", "hambone");
	}

	public static void craftObsidian() {
		AdvancedSmelting.addMelting(Blocks.obsidian, 0, getFluidTempMod("Obsidian"), new FluidStack(TinkerSmeltery.moltenObsidianFluid, INGOT_LIQUID_VALUE * 2), false);
	}

	public static void craftGlass() {
		final String fluidName = "Glass";
		final Fluid glassFluid = TinkerSmeltery.moltenGlassFluid;

		AdvancedSmelting.addMelting(Blocks.sand, 0, getFluidTempMod(fluidName), new FluidStack(glassFluid, FluidContainerRegistry.BUCKET_VOLUME), false);
		AdvancedSmelting.addMelting(Blocks.glass, 0, getFluidTempMod(fluidName), new FluidStack(glassFluid, FluidContainerRegistry.BUCKET_VOLUME), false);
		AdvancedSmelting.addMelting(Blocks.glass_pane, 0, getFluidTempMod(fluidName), new FluidStack(glassFluid, 250), false);

		AdvancedSmelting.addMelting(TinkerSmeltery.clearGlass, 0, getFluidTempMod(fluidName), new FluidStack(glassFluid, 1000), false);
		AdvancedSmelting.addMelting(TinkerSmeltery.glassPane, 0, getFluidTempMod(fluidName), new FluidStack(glassFluid, 250), false);
	}

	public static void craftWater() {
		final String fluidName = "Water";
		final Fluid fluidWater = FluidRegistry.WATER;

		AdvancedSmelting.addMelting(Blocks.ice, 0, getFluidTempMod(fluidName), new FluidStack(fluidWater, 1000), false);
		AdvancedSmelting.addMelting(Blocks.snow, 0, getFluidTempMod(fluidName), new FluidStack(fluidWater, 500), false);
		AdvancedSmelting.addMelting(Items.snowball, 0, getFluidTempMod(fluidName), new FluidStack(fluidWater, 125), false);
	}

	public static void changeAnvil() {
		RecipeRemover.removeShapedRecipe(new ItemStack(Blocks.anvil));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.anvil), "bbb", " i ", "iii", 'i', "ingotSteel", 'b', "blockSteel"));

		FluidType steel = FluidType.getFluidType("Steel");
		Smeltery.addMelting(steel, new ItemStack(Blocks.anvil, 1, 0), 0, INGOT_LIQUID_VALUE * 31);
		Smeltery.addMelting(steel, new ItemStack(Blocks.anvil, 1, 1), 0, INGOT_LIQUID_VALUE * 31);
		Smeltery.addMelting(steel, new ItemStack(Blocks.anvil, 1, 2), 0, INGOT_LIQUID_VALUE * 31);
	}

	public static void changeFlintAndSteel() {
		RecipeRemover.removeShapedRecipe(new ItemStack(Items.flint_and_steel));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.flint_and_steel), "s ", " f", 's', "ingotSteel", 'f', new ItemStack(Items.flint)));

		Items.flint_and_steel.setMaxDamage(128);

		FluidType steel = FluidType.getFluidType("Steel");
		Smeltery.addMelting(steel, new ItemStack(Items.flint_and_steel, 1, 0), 0, INGOT_LIQUID_VALUE);
	}

	public static void changePiston() {
		final ItemStack rod = new ItemStack(TinkerTools.toughRod, 1, 2);

		RecipeRemover.removeAnyRecipe(new ItemStack(Blocks.piston));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.piston), "WWW", "CTC", "CRC", 'C', "cobblestone", 'T', rod, 'R', "dustRedstone", 'W', "plankWood"));
	}

	public static int getFluidTempMod(String fluidName) {
		switch (fluidName) {
			case "Water":
				return 10;
			case "Iron":
				return 913;
			case "Gold":
				return 663;
			case "Tin":
				return -163;
			case "Copper":
				return 534;
			case "Aluminum":
				return 310;
			case "NaturalAluminum":
				return 310;
			case "Cobalt":
				return 845;
			case "Ardite":
				return 910;
			case "AluminumBrass":
				return 305;
			case "Alumite":
				return -129;
			case "Manyullyn":
				return 534;
			case "Bronze":
				return 380;
			case "Steel":
				return 840;
			case "Nickel":
				return 1053;
			case "Lead":
				return -73;
			case "Silver":
				return 563;
			case "Platinum":
				return 1370;
			case "Invar":
				return 840;
			case "Electrum":
				return 663;
			case "Obsidian":
				return 330;
			case "Ender":
				return 0;
			case "Glass":
				return 975;
			case "Stone":
				return 600;
			case "Emerald":
				return 1025;
			case "Slime":
				return 0;
			case "PigIron":
				return 983;
			case "Glue":
				return 0;
			default:
				return 0;
		}
	}
}
