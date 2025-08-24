package com.iristhepianist.scalablestorageceu.common.metatileentities;

import com.iristhepianist.scalablestorageceu.common.metatileentities.MetaTileEntityScalableStorage;
import net.minecraft.util.ResourceLocation;

import static com.iristhepianist.scalablestorageceu.Tags.MODID;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class SSMetaTileEntities {
    public static MetaTileEntityScalableStorage SCALABLE_STORAGE;

    public static void init() {
        System.out.println("[DEBUG] Starting MetaTileEntity registration for ScalableStorageCEU");

        try {
            SCALABLE_STORAGE = registerMetaTileEntity(32000,
                    new MetaTileEntityScalableStorage(
                            new ResourceLocation(MODID, "scalable_storage")));

            System.out.println("[DEBUG] Successfully registered Scalable Storage with ID 32000");
            System.out.println("[DEBUG] MetaTileEntity: " + SCALABLE_STORAGE);

        } catch (Exception e) {
            System.out.println("[ERROR] Failed to register Scalable Storage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
