package com.iristhepianist.scalablestorageceu;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JsonModelGenerator {
    
    public static void main(String[] args) {
        generateItemModels();
        generateLangFile();
    }
    
    public static void generateItemModels() {
        String baseDir = "src/main/resources/assets/scalablestorageceu/models/item";
        createDirectories(baseDir);
        
        String[] tiers = {"storage_upgrade_tier_1", "storage_upgrade_tier_2", "storage_upgrade_tier_3"};
        
        for (String tier : tiers) {
            String jsonContent = "{\n" +
                "  \"parent\": \"item/generated\",\n" +
                "  \"textures\": {\n" +
                "    \"layer0\": \"scalablestorageceu:items/" + tier + "\"\n" +
                "  }\n" +
                "}";
            
            writeFile(baseDir + "/" + tier + ".json", jsonContent);
        }
        
        System.out.println("Generated all item model JSON files!");
    }
    
    public static void generateLangFile() {
        String langDir = "src/main/resources/assets/scalablestorageceu/lang";
        createDirectories(langDir);
        
        String langContent = "# Items\n" +
            "item.storage_upgrade_tier_1.name=Basic Storage Upgrade\n" +
            "item.storage_upgrade_tier_2.name=Advanced Storage Upgrade\n" +
            "item.storage_upgrade_tier_3.name=Elite Storage Upgrade\n" +
            "\n" +
            "# Multiblock Display Text\n" +
            "scalablestorageceu.machine.scalable_storage.tier=Upgrade Tier: %s\n" +
            "scalablestorageceu.machine.scalable_storage.capacity=Max Capacity: %s items\n" +
            "scalablestorageceu.machine.scalable_storage.stored=Stored: %s %s (%s/%s)\n" +
            "scalablestorageceu.machine.scalable_storage.empty=Empty - Insert any item to set type\n" +
            "\n" +
            "# Tooltips\n" +
            "scalablestorageceu.machine.scalable_storage.tooltip1=A massive storage system for a single item type\n" +
            "scalablestorageceu.machine.scalable_storage.tooltip2=Right-click controller with upgrade items to increase capacity\n" +
            "scalablestorageceu.machine.scalable_storage.tooltip3=Controller must face UP to form the multiblock\n" +
            "\n" +
            "# Controller Block\n" +
            "tile.scalable_storage_controller.name=Scalable Storage Controller\n" +
            "\n" +
            "# Item Tooltips\n" +
            "scalablestorageceu.storage_upgrade_tier_1.tooltip=Upgrades storage to 128,000 items (2x capacity)\n" +
            "scalablestorageceu.storage_upgrade_tier_2.tooltip=Upgrades storage to 256,000 items (4x capacity)\n" +
            "scalablestorageceu.storage_upgrade_tier_3.tooltip=Upgrades storage to 512,000 items (8x capacity)\n";
        
        writeFile(langDir + "/en_us.lang", langContent);
        System.out.println("Generated lang file!");
    }
    
    private static void createDirectories(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("Created directory: " + path);
        }
    }
    
    private static void writeFile(String filePath, String content) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(content);
            System.out.println("Generated: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to write " + filePath);
            e.printStackTrace();
        }
    }
}
