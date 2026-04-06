package com.zzt.eternal_abyss.items;

import com.zzt.eternal_abyss.init.ModCreativeTabs;
import com.zzt.eternal_abyss.recordbag.RecordBagEffectRegistry;
import com.zzt.eternal_abyss.recordbag.RecordBagRuntimeUtil;
import com.zzt.eternal_abyss.util.RecordBagUtil;
import com.zzt.eternal_abyss.inventory.RecordBagHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class RecordBag extends Item {

    public static final int GUI_ID = 20;

    public RecordBag(String name) {
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.CelestialArtifactsTab);
        setMaxStackSize(1);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, net.minecraft.entity.player.EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!world.isRemote) {
            RecordBagUtil.ensureBagId(stack);
            player.openGui(com.zzt.eternal_abyss.EternalAbyss.instance, GUI_ID, world,
                    (int) player.posX, (int) player.posY, (int) player.posZ);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("tooltip.eternal_abyss.pref"));
        tooltip.add("");
        RecordBagHandler handler = RecordBagUtil.getHandler(stack);
        int count = 0;

        Set<String> recordIds = new HashSet<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack record = handler.getStackInSlot(i);
            if (!record.isEmpty()) {
                count++;
                if (record.getItem().getRegistryName() != null) {
                    recordIds.add(record.getItem().getRegistryName().toString());
                }
            }
        }



        if(GuiScreen.isShiftKeyDown()){
            tooltip.add("§6收纳唱片，享受乐章带来的力量");
            tooltip.add("§d每种唱片只可放入一个");

            int totalRecords = getTotalRegisteredRecordCount();
            int maxDisplay = Math.min(totalRecords, handler.getSlots());

            tooltip.add("§3已收纳唱片： §f" + count + "§7 / " + maxDisplay);

        }else{
            tooltip.add(I18n.format("tooltip.eternal_abyss.holdShift"));
        }

        if (GuiScreen.isAltKeyDown()) {
            tooltip.add(" ");
            tooltip.add("§6§l可激活效果：");

            for (RecordBagEffectRegistry.Rule rule : RecordBagEffectRegistry.getRules()) {
                addRuleTooltip(tooltip, rule, recordIds);
            }

        } else {
            tooltip.add(" ");
            tooltip.add(I18n.format("tooltip.eternal_abyss.holdAlt"));
        }
    }
    @SideOnly(Side.CLIENT)
    private void addRuleTooltip(List<String> tooltip, RecordBagEffectRegistry.Rule rule, Set<String> recordIds) {

        boolean triggered = rule.isTriggered(recordIds);

        String title = (rule.tooltip != null && !rule.tooltip.trim().isEmpty())
                ? rule.tooltip
                : rule.name;

        if (triggered) {
            tooltip.add("§e§l- §6" + title);
        } else {
            tooltip.add("§7- " + title);
        }

        // any(n) 规则
        if (rule.anyCount > 0) {
            int current = recordIds.size();
            int displayCurrent = Math.min(current, rule.anyCount);

            if (triggered) {
                tooltip.add("  §e[§6唱片数量 " + displayCurrent + "/" + rule.anyCount + "§e]");
            } else {
                tooltip.add("  §8[§7唱片数量 " + displayCurrent + "/" + rule.anyCount + "§8]");
            }
            return;
        }

        // 指定唱片规则
        if (!rule.requiredRecords.isEmpty()) {
            StringBuilder sb = new StringBuilder();

            String bracketColor = triggered ? "§e" : "§8";
            String separatorColor = triggered ? "§6" : "§7";

            sb.append("  ").append(bracketColor).append("[");

            boolean first = true;
            for (String id : rule.requiredRecords) {
                if (!first) {
                    sb.append(separatorColor).append("，");
                }
                first = false;

                boolean has = recordIds.contains(id);
                String displayName = getRecordShortName(id);

                if (has) {
                    sb.append("§6").append(displayName);
                } else {
                    sb.append("§8").append(displayName);
                }
            }

            sb.append(bracketColor).append("]");
            tooltip.add(sb.toString());
        }
    }

    @SideOnly(Side.CLIENT)
    private String getRecordShortName(String id) {
        if (id == null || id.isEmpty()) return "unknown";

        int index = id.indexOf(':');
        String name = (index >= 0 && index + 1 < id.length()) ? id.substring(index + 1) : id;

        return name.replace('_', ' ');
    }


    private static int getTotalRegisteredRecordCount() {
        Set<String> ids = new HashSet<>();

        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            if (item instanceof net.minecraft.item.ItemRecord && item.getRegistryName() != null) {
                ids.add(item.getRegistryName().toString());
            }
        }

        return ids.size();
    }
}