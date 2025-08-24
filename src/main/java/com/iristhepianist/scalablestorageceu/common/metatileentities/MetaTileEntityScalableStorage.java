package com.iristhepianist.scalablestorageceu.common.metatileentities;

import java.util.List;

import com.iristhepianist.scalablestorageceu.common.items.ItemStorageUpgrade;
import com.iristhepianist.scalablestorageceu.common.items.ItemVoidUpgrade;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.common.blocks.BlockMetalCasing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.MetaBlocks;

public class MetaTileEntityScalableStorage extends MultiblockWithDisplayBase {

    private ItemStackHandler storageHandler;
    private ItemStack storedItemType = ItemStack.EMPTY;
    private int virtualStoredCount = 0;
    private int maxStorageCapacity = 4000000;
    private int currentUpgradeTier = 0;
    private boolean hasValidUpgrade = true;
    private boolean hasVoidUpgrade = false;
    private int durabilityTimer = 0;
    private static final int BASE_CAPACITY = 4000000;
    private static final int DURABILITY_INTERVAL = 200;

    public MetaTileEntityScalableStorage(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        initializeStorageHandler();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityScalableStorage(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        checkForUpgrades();

        if (currentUpgradeTier > 0 && hasValidUpgrade) {
            durabilityTimer++;
            if (durabilityTimer >= DURABILITY_INTERVAL) {
                durabilityTimer = 0;
                damageDurableUpgrades();
            }
        }

        if (hasValidUpgrade) {
            processItemInput();
            processItemOutput();
        }
    }

    private void processItemInput() {
        if (importItems != null) {
            for (int i = 0; i < importItems.getSlots(); i++) {
                ItemStack inputStack = importItems.getStackInSlot(i);
                if (!inputStack.isEmpty()) {
                    if (inputStack.getItem() instanceof ItemStorageUpgrade ||
                            inputStack.getItem() instanceof ItemVoidUpgrade) {
                        continue;
                    }

                    boolean storageFull = (virtualStoredCount >= maxStorageCapacity);
                    boolean outputFull = true;
                    if (exportItems != null) {
                        for (int j = 0; j < exportItems.getSlots(); j++) {
                            ItemStack outputStack = exportItems.getStackInSlot(j);
                            if (outputStack.isEmpty() ||
                                    outputStack.getCount() < outputStack.getMaxStackSize()) {
                                outputFull = false;
                                break;
                            }
                        }
                    }

                    if (hasVoidUpgrade && storageFull && outputFull) {
                        importItems.setStackInSlot(i, ItemStack.EMPTY);
                        continue;
                    }

                    ItemStack remaining = storageHandler.insertItem(0, inputStack, false);
                    importItems.setStackInSlot(i, remaining);
                    if (remaining.getCount() != inputStack.getCount()) {
                        markDirty();
                        break;
                    }
                }
            }
        }
    }

    private void processItemOutput() {
        if (exportItems != null && virtualStoredCount > 0 && !storedItemType.isEmpty()) {
            for (int i = 0; i < exportItems.getSlots(); i++) {
                int maxToExtract = Math.min(virtualStoredCount, storedItemType.getMaxStackSize());
                if (maxToExtract > 0) {
                    ItemStack exportStack = storedItemType.copy();
                    exportStack.setCount(maxToExtract);
                    ItemStack remaining = exportItems.insertItem(i, exportStack, true);
                    int exportedCount = maxToExtract - remaining.getCount();
                    if (exportedCount > 0) {
                        exportStack.setCount(exportedCount);
                        exportItems.insertItem(i, exportStack, false);
                        virtualStoredCount -= exportedCount;
                        if (virtualStoredCount <= 0) {
                            storedItemType = ItemStack.EMPTY;
                            virtualStoredCount = 0;
                        }
                        markDirty();
                        break;
                    }
                }
            }
        }
    }

    private void damageDurableUpgrades() {
        if (importItems != null) {
            for (int i = 0; i < importItems.getSlots(); i++) {
                ItemStack stack = importItems.getStackInSlot(i);
                if (stack != null && !stack.isEmpty() && stack.getItem() != null) {
                    if (stack.getItem() instanceof ItemStorageUpgrade) {
                        ItemStorageUpgrade upgrade = (ItemStorageUpgrade) stack.getItem();
                        if (upgrade.getTier() != 3 && stack.getMaxDamage() > 0) {
                            if (stack.getItemDamage() < stack.getMaxDamage()) {
                                try {
                                    int newDamage = stack.getItemDamage() + 1;
                                    stack.setItemDamage(newDamage);
                                    if (newDamage >= stack.getMaxDamage()) {
                                        importItems.setStackInSlot(i, ItemStack.EMPTY);
                                    }
                                } catch (Exception e) {
                                    System.err.println("Failed to damage upgrade item: " + e.getMessage());
                                    importItems.setStackInSlot(i, ItemStack.EMPTY);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkForUpgrades() {
        int highestTier = 0;
        boolean foundValidUpgrade = true;
        boolean foundVoidUpgrade = false;

        if (importItems != null) {
            for (int i = 0; i < importItems.getSlots(); i++) {
                ItemStack stack = importItems.getStackInSlot(i);
                if (stack != null && !stack.isEmpty() && stack.getItem() != null) {
                    if (stack.getItem() instanceof ItemVoidUpgrade) {
                        foundVoidUpgrade = true;
                    } else if (stack.getItem() instanceof ItemStorageUpgrade) {
                        ItemStorageUpgrade upgrade = (ItemStorageUpgrade) stack.getItem();

                        if (upgrade.getTier() == 3 ||
                                (stack.getMaxDamage() > 0 && stack.getItemDamage() < stack.getMaxDamage())) {
                            highestTier = Math.max(highestTier, upgrade.getTier());
                        } else {
                            foundValidUpgrade = false;
                        }
                    }
                }
            }
        }

        if (highestTier == 0 && currentUpgradeTier > 0) {
            foundValidUpgrade = false;
        }

        boolean tierChanged = highestTier != currentUpgradeTier;
        boolean validityChanged = foundValidUpgrade != hasValidUpgrade;
        boolean voidChanged = foundVoidUpgrade != hasVoidUpgrade;

        if (tierChanged || validityChanged || voidChanged) {
            currentUpgradeTier = highestTier;
            hasValidUpgrade = foundValidUpgrade;
            hasVoidUpgrade = foundVoidUpgrade;
            calculateStorageCapacity();
            reinitializeStorageHandler();
            markDirty();
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(hasValidUpgrade, !storedItemType.isEmpty())
                .addCustom(list -> {
                    // Storage capacity and percentage
                    float storagePercent = maxStorageCapacity > 0 ?
                            ((float) virtualStoredCount / maxStorageCapacity) * 100.0f : 0.0f;

                    list.add(new TextComponentString(TextFormatting.GRAY + "Max Capacity: " +
                            TextFormatting.WHITE + TextFormattingUtil.formatNumbers(maxStorageCapacity) +
                            TextFormatting.GRAY + " items"));

                    // Upgrade status
                    if (!hasValidUpgrade && currentUpgradeTier > 0) {
                        list.add(new TextComponentString(TextFormatting.RED + "⚠ UPGRADE DEPLETED - I/O DISABLED ⚠"));
                        list.add(new TextComponentString(TextFormatting.YELLOW + "Insert fresh upgrade to restore functionality"));
                    } else if (hasValidUpgrade && currentUpgradeTier > 0) {
                        list.add(new TextComponentString(TextFormatting.GREEN + "✓ Tier " + currentUpgradeTier + " Active"));
                    } else {
                        list.add(new TextComponentString(TextFormatting.GRAY + "Base tier - No upgrades required"));
                    }

                    // Voiding status
                    if (hasVoidUpgrade) {
                        list.add(new TextComponentString(TextFormatting.DARK_RED + "⚡ Voiding Mode Active"));
                        list.add(new TextComponentString(TextFormatting.YELLOW + "Excess items will be destroyed"));
                    }

                    // Storage information with percentage
                    if (!storedItemType.isEmpty()) {
                        list.add(new TextComponentString(TextFormatting.WHITE + "Stored: " +
                                TextFormatting.DARK_AQUA + storedItemType.getDisplayName()));
                        list.add(new TextComponentString(TextFormatting.WHITE + "Count: " +
                                TextFormatting.GREEN + TextFormattingUtil.formatNumbers(virtualStoredCount) +
                                TextFormatting.GRAY + " / " + TextFormattingUtil.formatNumbers(maxStorageCapacity) +
                                TextFormatting.YELLOW + String.format(" (%.1f%% full)", storagePercent)));
                    } else {
                        list.add(new TextComponentString(TextFormatting.GRAY + "Empty - Insert any item to set type"));
                    }

                    // Enhanced upgrade display with percentages and time
                    if (importItems != null) {
                        boolean foundAnyUpgrade = false;
                        for (int i = 0; i < importItems.getSlots(); i++) {
                            ItemStack stack = importItems.getStackInSlot(i);
                            if (stack != null && !stack.isEmpty()) {
                                if (stack.getItem() instanceof ItemVoidUpgrade) {
                                    list.add(new TextComponentString(TextFormatting.DARK_RED + "Void Upgrade: " +
                                            TextFormatting.RED + "Active"));
                                    foundAnyUpgrade = true;
                                } else if (stack.getItem() instanceof ItemStorageUpgrade) {
                                    ItemStorageUpgrade upgrade = (ItemStorageUpgrade) stack.getItem();
                                    foundAnyUpgrade = true;
                                    if (upgrade.getTier() == 3) {
                                        list.add(new TextComponentString(TextFormatting.AQUA + "Elite Upgrade: " +
                                                TextFormatting.GREEN + "∞ Permanent"));
                                    } else if (stack.getMaxDamage() > 0) {
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

                                        String tierName = upgrade.getTier() == 1 ? "Basic" : "Advanced";
                                        TextFormatting color = percent > 50 ? TextFormatting.GREEN :
                                                percent > 25 ? TextFormatting.YELLOW : TextFormatting.RED;

                                        list.add(new TextComponentString(color + tierName + " Upgrade: " +
                                                String.format("%.1f%% (-%s)", percent, timeStr)));
                                    }
                                }
                            }
                        }
                        if (!foundAnyUpgrade && currentUpgradeTier == 0) {
                            list.add(new TextComponentString(TextFormatting.GRAY + "No upgrades installed"));
                        }
                    }
                });
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCC", "CCC", "CCC")
                .aisle("CCC", "CCC", "CCC")
                .aisle("CCC", "CSC", "CCC")
                .where('S', selfPredicate())
                .where('C', states(getCasingState())
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(4)))
                .build();
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.importItems = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.exportItems = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        calculateStorageCapacity();
        reinitializeStorageHandler();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.importItems = null;
        this.exportItems = null;
        this.currentUpgradeTier = 0;
        this.hasValidUpgrade = true;
        this.hasVoidUpgrade = false;
        this.durabilityTimer = 0;
        calculateStorageCapacity();
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    private void calculateStorageCapacity() {
        switch (currentUpgradeTier) {
            case 0: this.maxStorageCapacity = BASE_CAPACITY; break;
            case 1: this.maxStorageCapacity = 32000000; break;
            case 2: this.maxStorageCapacity = 64000000; break;
            case 3: this.maxStorageCapacity = 256000000; break;
            default: this.maxStorageCapacity = BASE_CAPACITY;
        }
    }

    private void initializeStorageHandler() {
        this.storageHandler = new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return maxStorageCapacity;
            }

            @Override
            @NotNull
            public ItemStack getStackInSlot(int slot) {
                if (virtualStoredCount > 0 && !storedItemType.isEmpty()) {
                    ItemStack display = storedItemType.copy();
                    display.setCount(virtualStoredCount);
                    return display;
                }
                return ItemStack.EMPTY;
            }

            @Override
            @NotNull
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (stack.isEmpty()) return stack;
                if (stack.getItem() instanceof ItemStorageUpgrade ||
                        stack.getItem() instanceof ItemVoidUpgrade) {
                    return stack;
                }

                if (!hasValidUpgrade) {
                    return stack;
                }

                if (storedItemType.isEmpty()) {
                    if (!simulate) {
                        storedItemType = stack.copy();
                        storedItemType.setCount(1);
                    }
                } else if (!ItemStack.areItemsEqual(storedItemType, stack) ||
                        !ItemStack.areItemStackTagsEqual(storedItemType, stack)) {
                    return stack;
                }

                int spaceLeft = maxStorageCapacity - virtualStoredCount;
                int toStore = Math.min(stack.getCount(), spaceLeft);

                if (toStore > 0 && !simulate) {
                    virtualStoredCount += toStore;
                    onContentsChanged(slot);
                }

                if (stack.getCount() > toStore) {
                    ItemStack remainder = stack.copy();
                    remainder.setCount(stack.getCount() - toStore);
                    return remainder;
                }

                return ItemStack.EMPTY;
            }

            @Override
            @NotNull
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (virtualStoredCount <= 0 || storedItemType.isEmpty()) {
                    return ItemStack.EMPTY;
                }

                if (!hasValidUpgrade) {
                    return ItemStack.EMPTY;
                }

                int toExtract = Math.min(amount, virtualStoredCount);

                if (toExtract > 0) {
                    ItemStack extracted = storedItemType.copy();
                    extracted.setCount(toExtract);

                    if (!simulate) {
                        virtualStoredCount -= toExtract;
                        if (virtualStoredCount <= 0) {
                            storedItemType = ItemStack.EMPTY;
                            virtualStoredCount = 0;
                        }
                        onContentsChanged(slot);
                    }

                    return extracted;
                }

                return ItemStack.EMPTY;
            }
        };
    }

    private void reinitializeStorageHandler() {
        int existingCount = virtualStoredCount;
        ItemStack existingType = storedItemType.isEmpty() ? ItemStack.EMPTY : storedItemType.copy();

        initializeStorageHandler();

        virtualStoredCount = Math.min(existingCount, maxStorageCapacity);
        if (!existingType.isEmpty()) {
            storedItemType = existingType;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                !storedItemType.isEmpty(), isStructureFormed());
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BENDER_OVERLAY;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("scalablestorageceu.machine.scalable_storage.tooltip1"));
        tooltip.add(I18n.format("scalablestorageceu.machine.scalable_storage.tooltip2"));
        tooltip.add("");
        tooltip.add(TextFormatting.GRAY + "Author: " + TextFormatting.WHITE + "iristhpianist");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("MaxCapacity", maxStorageCapacity);
        data.setInteger("VirtualStoredCount", virtualStoredCount);
        data.setInteger("CurrentUpgradeTier", currentUpgradeTier);
        data.setBoolean("HasValidUpgrade", hasValidUpgrade);
        data.setBoolean("HasVoidUpgrade", hasVoidUpgrade);
        data.setInteger("DurabilityTimer", durabilityTimer);
        if (!storedItemType.isEmpty()) {
            data.setTag("StoredItemType", storedItemType.writeToNBT(new NBTTagCompound()));
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.maxStorageCapacity = data.getInteger("MaxCapacity");
        this.virtualStoredCount = data.getInteger("VirtualStoredCount");
        this.currentUpgradeTier = data.getInteger("CurrentUpgradeTier");
        this.hasValidUpgrade = data.getBoolean("HasValidUpgrade");
        this.hasVoidUpgrade = data.getBoolean("HasVoidUpgrade");
        this.durabilityTimer = data.getInteger("DurabilityTimer");
        if (data.hasKey("StoredItemType")) {
            this.storedItemType = new ItemStack(data.getCompoundTag("StoredItemType"));
        }
        initializeStorageHandler();
    }


}
