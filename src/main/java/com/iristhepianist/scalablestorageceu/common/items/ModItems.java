package com.iristhepianist.scalablestorageceu.common.items;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModItems {

    public static ItemStorageUpgrade STORAGE_UPGRADE_TIER_1;
    public static ItemStorageUpgrade STORAGE_UPGRADE_TIER_2;
    public static ItemStorageUpgrade STORAGE_UPGRADE_TIER_3;
    public static ItemVoidUpgrade VOID_UPGRADE;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        STORAGE_UPGRADE_TIER_1 = new ItemStorageUpgrade("storage_upgrade_tier_1", 1, 10000);
        STORAGE_UPGRADE_TIER_2 = new ItemStorageUpgrade("storage_upgrade_tier_2", 2, 100000);
        STORAGE_UPGRADE_TIER_3 = new ItemStorageUpgrade("storage_upgrade_tier_3", 3, 0); // 0 = unbreakable
        VOID_UPGRADE = new ItemVoidUpgrade("void_upgrade");

        event.getRegistry().register(STORAGE_UPGRADE_TIER_1);
        event.getRegistry().register(STORAGE_UPGRADE_TIER_2);
        event.getRegistry().register(STORAGE_UPGRADE_TIER_3);
        event.getRegistry().register(VOID_UPGRADE);
    }
}
