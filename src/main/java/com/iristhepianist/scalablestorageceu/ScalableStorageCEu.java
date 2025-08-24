package com.iristhepianist.scalablestorageceu;

import com.iristhepianist.scalablestorageceu.common.metatileentities.SSMetaTileEntities;
import com.iristhepianist.scalablestorageceu.common.recipes.ModRecipes;
import gregtech.api.recipes.ModHandler;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.BlockMachineCasing.MachineCasingType;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = Tags.MODID,
        name = Tags.MODNAME,
        version = Tags.VERSION,
        dependencies = "required-after:gregtech")

public class ScalableStorageCEu {


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        SSMetaTileEntities.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        registerRecipes();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ModRecipes.registerRecipes();
    }

    private void registerRecipes() {

        ModHandler.addShapedRecipe(true, "scalable_storage_controller",
                SSMetaTileEntities.SCALABLE_STORAGE.getStackForm(),
                "ChC",
                "CHC",
                "CwC",
                'C', MetaBlocks.MACHINE_CASING.getItemVariant(MachineCasingType.ULV),
                'H', new ItemStack(Blocks.CHEST)); // Simple recipe for testing
    }
}
