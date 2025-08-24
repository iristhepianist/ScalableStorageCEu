package com.iristhepianist.scalablestorageceu.common.recipes;

import com.iristhepianist.scalablestorageceu.common.items.ModItems;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ModRecipes {
    public static void registerRecipes() {
        // Basic Storage Upgrade - LV Tier (30 EU/t)
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Steel, 4)
                .input(OrePrefix.wireFine, Materials.RedAlloy, 16)
                .input(OrePrefix.gear, Materials.WroughtIron, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(144))
                .outputs(new ItemStack(ModItems.STORAGE_UPGRADE_TIER_1))
                .duration(200)
                .EUt(30)
                .buildAndRegister();

        // Advanced Storage Upgrade - MV Tier (120 EU/t)
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Aluminium, 4)
                .input(OrePrefix.wireFine, Materials.Aluminium, 16)
                .input(OrePrefix.plate, Materials.RoseGold, 4)
                .input(OrePrefix.gear, Materials.Steel, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(288))
                .outputs(new ItemStack(ModItems.STORAGE_UPGRADE_TIER_2))
                .duration(400)
                .EUt(120)
                .buildAndRegister();

        // Elite Storage Upgrade - HV Tier (480 EU/t)
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.StainlessSteel, 4)
                .input(OrePrefix.wireFine, Materials.Electrum, 8)
                .input(OrePrefix.gear, Materials.StainlessSteel, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(576))
                .outputs(new ItemStack(ModItems.STORAGE_UPGRADE_TIER_3))
                .duration(600)
                .EUt(480)
                .buildAndRegister();

        // Void Upgrade - MV Tier (120 EU/t)
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Iron, 4)
                .input(OrePrefix.wireFine, Materials.Copper, 8)
                .input(Items.GLASS_BOTTLE, 1)
                .input(OrePrefix.gear, Materials.Bronze, 1)
                .fluidInputs(Materials.SolderingAlloy.getFluid(144))
                .outputs(new ItemStack(ModItems.VOID_UPGRADE))
                .duration(300)
                .EUt(120)
                .buildAndRegister();
    }
}
