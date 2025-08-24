package com.iristhepianist.scalablestorageceu;

import com.iristhepianist.scalablestorageceu.common.items.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(value = Side.CLIENT)
public class ClientProxy {

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        registerModel(ModItems.STORAGE_UPGRADE_TIER_1);
        registerModel(ModItems.STORAGE_UPGRADE_TIER_2);
        registerModel(ModItems.STORAGE_UPGRADE_TIER_3);
        registerModel(ModItems.VOID_UPGRADE);
    }

    private static void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, 
            new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
