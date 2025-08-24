package com.iristhepianist.scalablestorageceu.common.items;

import com.iristhepianist.scalablestorageceu.Tags;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemStorageUpgrade extends Item {

    private final int tier;

    public ItemStorageUpgrade(String name, int tier, int maxDamage) {
        this.tier = tier;
        setRegistryName(new ResourceLocation(Tags.MODID, name));
        setTranslationKey(name);
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(1);
        if (maxDamage > 0) {
            setMaxDamage(maxDamage);
        }
    }

    public int getTier() {
        return tier;
    }

    @Override
    public boolean isDamageable() {
        return getMaxDamage() > 0;
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return tier == 3;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        if (tier == 3) {
            return itemStack.copy();
        }
        return ItemStack.EMPTY;
    }

    public boolean isUnbreakable() {
        return tier == 3;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, ITooltipFlag flag) {
        // Add main tooltip based on tier
        String tooltipKey = "scalablestorageceu.storage_upgrade_tier_" + tier + ".tooltip";
        tooltip.add(TextFormatting.GRAY + I18n.format(tooltipKey));

        // Add durability information if applicable
        if (tier != 3 && stack.getMaxDamage() > 0) {
            int remaining = stack.getMaxDamage() - stack.getItemDamage();
            float percent = ((float) remaining / stack.getMaxDamage()) * 100.0f;

            // Calculate real-world time (10 seconds per durability point)
            int totalSeconds = remaining * 10;
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;

            String timeStr;
            if (hours > 0) {
                timeStr = String.format("%dh %dm", hours, minutes);
            } else {
                timeStr = String.format("%dm", minutes);
            }

            TextFormatting color = percent > 50 ? TextFormatting.GREEN :
                    percent > 25 ? TextFormatting.YELLOW : TextFormatting.RED;

            tooltip.add(color + String.format("Durability: %.1f%% (%s remaining)", percent, timeStr));
        } else if (tier == 3) {
            tooltip.add(TextFormatting.AQUA + "Permanent - Never breaks");
        }
    }
}
