package toops.tsteelworks.common.plugins.tconstruct;

import cpw.mods.fml.common.registry.GameRegistry;
import mantle.Mantle;
import mantle.utils.ItemMetaWrapper;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import nf.fr.ephys.cookiecore.helpers.InventoryHelper;
import nf.fr.ephys.cookiecore.util.HashedItemStack;
import tconstruct.TConstruct;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.FluidType;
import tconstruct.library.crafting.LiquidCasting;
import tconstruct.library.crafting.Smeltery;
import tconstruct.smeltery.TinkerSmeltery;
import tconstruct.tools.TinkerTools;
import toops.tsteelworks.api.event.IRegistry;
import toops.tsteelworks.api.event.IRegistryListener;
import toops.tsteelworks.api.highoven.IMixerRegistry;
import toops.tsteelworks.api.highoven.ISmeltingRegistry;
import toops.tsteelworks.common.core.TSContent;
import toops.tsteelworks.common.core.TSLogger;
import toops.tsteelworks.common.core.TSRecipes;
import toops.tsteelworks.lib.ModsData.Fluids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static toops.tsteelworks.api.event.IRegistry.IRegistryEvent.TYPE.DELETE;

class TCSmeltery {
	private List<Alloy> alloys = new ArrayList<>();
	private Map<HashedItemStack, ItemStack> itemRenderList = new HashMap<>();

	private IRegistryListener<ItemStack, ISmeltingRegistry.IMeltData> smeltListener = new IRegistryListener<ItemStack, ISmeltingRegistry.IMeltData>() {
		@Override
		public void onRegistryChange(IRegistry.IRegistryEvent<ItemStack, ISmeltingRegistry.IMeltData> event) {
			if (event.getType() == DELETE) {
				return; // TiC smeltery doesn't support removing meltings.
			}

			ItemStack toMelt = event.getItem();
			ISmeltingRegistry.IMeltData meltData = event.getData();

			ItemStack renderAs;
			if (toMelt.getItem() instanceof ItemBlock) {
				renderAs = toMelt;
			} else {
				renderAs = itemRenderList.get(new HashedItemStack(toMelt));

				if (renderAs == null) {
					TSLogger.error("item " + toMelt.getDisplayName() + " does not have a registered renderer.");
					renderAs = new ItemStack(Blocks.iron_block);
				}
			}

			Block renderAsBlock = ((ItemBlock) renderAs.getItem()).field_150939_a;
			int renderAsMetadata = renderAs.getItemDamage();

			FluidType fluid = FluidType.getFluidType(meltData.getResult().getFluid());
			int temp = fluid == null ? (meltData.getMeltingPoint() / 2) : fluid.baseTemperature;

			Smeltery.addMelting(toMelt, renderAsBlock, renderAsMetadata, temp, meltData.getResult());
		}
	};

	public void preInit() {
		adjustFluidValues();
		// copy high oven smelting list to smeltery smelting list
		ISmeltingRegistry.INSTANCE.addEventListener(smeltListener);
	}

	public void init() {
		craftPigIron();
		craftManual();
		registerCasting();
		meltSeared();
	}

	public void postInit() {
		registerAlloysDiffer();

		ISmeltingRegistry.INSTANCE.removeEventListener(smeltListener);
		itemRenderList = null;

		// copy smeltery smelting list to high oven smelting list
		copySmeltingList();
	}

	private void adjustFluidValues() {
		TSRecipes.INGOT_LIQUID_VALUE = TConstruct.ingotLiquidValue;
		TSRecipes.NUGGET_LIQUID_VALUE = TConstruct.nuggetLiquidValue;
		TSRecipes.BLOCK_LIQUID_VALUE = TConstruct.blockLiquidValue;
	}

	private void copySmeltingList() {
		Map<ItemMetaWrapper, FluidStack> smeltingList = Smeltery.getSmeltingList();
		Map<ItemMetaWrapper, Integer> temperatureList = Smeltery.getTemperatureList();

		ISmeltingRegistry localRegistry = ISmeltingRegistry.INSTANCE;

		for (Map.Entry<ItemMetaWrapper, FluidStack> set : smeltingList.entrySet()) {
			ItemStack key = new ItemStack(set.getKey().item, 1, set.getKey().meta);

			if (localRegistry.getMeltable(key) != null) continue;

			boolean isOre = InventoryHelper.itemIsOre(key);

			String fluidName = getFluidName(set.getValue().getFluid());
			int temp = fluidName == null ? 0 : getFluidTempOverride(fluidName);
			if (temp == 0) temp = temperatureList.get(set.getKey()) * 2;

			FluidStack output = set.getValue().copy();
			if (isOre) {
				// TConstruct registry stores the double output directly in the fluid amount, we compute it right
				// before outputing it to the high oven, so divide to get back to a neutral value.
				output.amount = TSRecipes.INGOT_LIQUID_VALUE;
			}

			localRegistry.addMeltable(key, isOre, output, temp);
		}
	}

	private String getFluidName(Fluid needle) {
		for (Object o : FluidType.fluidTypes.entrySet()) {
			Map.Entry pairs = (Map.Entry) o;

			if (((FluidType) pairs.getValue()).fluid.equals(needle))
				return (String) pairs.getKey();
		}

		return null;
	}

	private void craftPigIron() {
		OreDictionary.registerOre("dustBone", new ItemStack(Items.dye, 1, 15));
		IMixerRegistry.INSTANCE.registerMix(new FluidStack(TinkerSmeltery.pigIronFluid, 1000), null, TinkerSmeltery.moltenIronFluid, "dustSugar", "dustBone", "hambone");
	}

	private void registerCasting() {
		final LiquidCasting basinCasting = TConstructRegistry.getBasinCasting();
		final LiquidCasting tableCasting = TConstructRegistry.getTableCasting();

		final ItemStack itemScorchedBrick = new ItemStack(TSContent.materialsTS, 1, 0);
		final ItemStack blockScorchedBrick = new ItemStack(TSContent.highoven, 1, 2);
		tableCasting.addCastingRecipe(itemScorchedBrick, new FluidStack(TinkerSmeltery.moltenStoneFluid, 8), new ItemStack(Items.brick), true, 50);
		basinCasting.addCastingRecipe(blockScorchedBrick, new FluidStack(TinkerSmeltery.moltenStoneFluid, 32), new ItemStack(Blocks.brick_block), true, 100);

		final Fluid limestoneFluid = TSContent.moltenLimestoneFluid;
		tableCasting.addCastingRecipe(new ItemStack(TSContent.materialsTS, 1, 1), new FluidStack(limestoneFluid, TSRecipes.INGOT_LIQUID_VALUE), new ItemStack(TinkerSmeltery.metalPattern), false, 25);
		basinCasting.addCastingRecipe(new ItemStack(TSContent.limestoneBlock), new FluidStack(limestoneFluid, TSRecipes.INGOT_LIQUID_VALUE * 4), 100);

		ItemStack bucket = new ItemStack(Items.bucket);
		tableCasting.addCastingRecipe(Fluids.bucketSteam, new FluidStack(Fluids.steamFluid, FluidContainerRegistry.BUCKET_VOLUME), bucket, true, 10);
		tableCasting.addCastingRecipe(Fluids.bucketLimestone, new FluidStack(Fluids.moltenLimestoneFluid, FluidContainerRegistry.BUCKET_VOLUME), bucket, true, 10);
		tableCasting.addCastingRecipe(Fluids.bucketCement, new FluidStack(Fluids.liquidCementFluid, FluidContainerRegistry.BUCKET_VOLUME), bucket, true, 10);
	}

	/**
	 * We only need the manual if TiCon is available otherwise it's going to be full of lies
	 */
	private void craftManual() {
		final ItemStack manual = new ItemStack(TSContent.bookManual, 1, 0);

		final LiquidCasting tableCasting = TConstructRegistry.getTableCasting();
		final FluidStack fluidStoneMinor = new FluidStack(TinkerSmeltery.moltenStoneFluid, 8);

		tableCasting.addCastingRecipe(manual, fluidStoneMinor, new ItemStack(TinkerTools.manualBook, 1, 0), true, 50);
		tableCasting.addCastingRecipe(manual, fluidStoneMinor, new ItemStack(TinkerTools.manualBook, 1, 1), true, 50);
		tableCasting.addCastingRecipe(manual, fluidStoneMinor, new ItemStack(TinkerTools.manualBook, 1, 2), true, 50);
		tableCasting.addCastingRecipe(manual, fluidStoneMinor, new ItemStack(TinkerTools.manualBook, 1, 3), true, 50);

		tableCasting.addCastingRecipe(manual, fluidStoneMinor, new ItemStack(Mantle.mantleBook, 1), true, 50);
		tableCasting.addCastingRecipe(manual, fluidStoneMinor, new ItemStack(Items.book, 1), true, 50);
	}

	public void registerAlloy(FluidStack input1, FluidStack input2, FluidStack output) {
		if (input1 == null) throw new IllegalArgumentException("Alloy fluid 1 cannot be null");
		if (input2 == null) throw new IllegalArgumentException("Alloy fluid 2 cannot be null");
		if (output == null) throw new IllegalArgumentException("Alloy output cannot be null");

		if (alloys == null) {
			throw new IllegalStateException("Alloys should be registered before postinit is called on the TConstruct compat plugin.");
		}

		alloys.add(new Alloy(input1, input2, output));
	}

	private void registerAlloysDiffer() {
		for (Alloy alloy : alloys) {
			Smeltery.addAlloyMixing(alloy.output, alloy.f1, alloy.f2);
		}

		alloys = null;
	}

	private void meltSeared() {
		final Fluid fluid = TinkerSmeltery.moltenStoneFluid;
		final ISmeltingRegistry advancedSmelting = ISmeltingRegistry.INSTANCE;

		final ItemStack searedBrickItem = new ItemStack(TinkerTools.materials, 1, 2);
		final ItemStack searedBrickBlock = new ItemStack(TinkerSmeltery.searedBlock, 1, 2);
		TConstructPlugin.registerSmelteryItemRenderer(searedBrickItem, searedBrickBlock);
		advancedSmelting.addMeltable(searedBrickItem, false, new FluidStack(fluid, TSRecipes.INGOT_LIQUID_VALUE), 600);

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

		for (int meta = 2; meta <= 11; meta++) {
			advancedSmelting.addMeltable(new ItemStack(TinkerSmeltery.smeltery, 1, meta), false, new FluidStack(fluid, TSRecipes.INGOT_LIQUID_VALUE), 600);
		}
	}

	private int getFluidTempOverride(String fluidName) {
		switch (fluidName) {
			case "Water":
				return 10;
			case "Iron":
				return 913;
			case "Gold":
				return 663;
			case "Tin":
				return 163;
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
				return 129;
			case "Manyullyn":
				return 534;
			case "Bronze":
				return 380;
			case "Steel":
				return 840;
			case "Nickel":
				return 1053;
			case "Lead":
				return 73;
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
			case "Glass":
				return 975;
			case "Stone":
				return 600;
			case "Emerald":
				return 1025;
			case "PigIron":
				return 983;
			// case "Ender":
			// case "Slime":
			// case "Glue":
			default:
				return 0;
		}
	}

	public void registerItemRenderer(ItemStack toRender, ItemStack renderAs) {
		if (itemRenderList == null) {
			throw new IllegalStateException("Renderers must be registered before postinit.");
		}

		if (toRender == null) {
			throw new IllegalArgumentException("toRender cannot be null");
		}

		if (toRender.getItem() instanceof ItemBlock) {
			throw new IllegalArgumentException("toRender should not be an ItemBlock");
		}

		if (renderAs == null || !(renderAs.getItem() instanceof ItemBlock)) {
			throw new IllegalArgumentException("renderAs should be an ItemBlock");
		}

		ItemStack out = itemRenderList.put(new HashedItemStack(toRender), renderAs);
		if (out != null) {
			throw new IllegalStateException("Item " + toRender.getDisplayName() + " was already registered with renderer " + out.getDisplayName());
		}
	}

	private static class Alloy {
		private FluidStack f1;
		private FluidStack f2;
		private FluidStack output;

		public Alloy(FluidStack f1, FluidStack f2, FluidStack output) {
			this.f1 = f1;
			this.f2 = f2;
			this.output = output;
		}

		@Override
		public String toString() {
			return "Alloy{" +
					"f1=" + f1.getLocalizedName() +
					", f2=" + f2.getLocalizedName() +
					", output=" + output.getLocalizedName() +
					'}';
		}
	}
}
