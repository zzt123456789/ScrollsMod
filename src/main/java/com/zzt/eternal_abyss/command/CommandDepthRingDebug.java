package com.zzt.eternal_abyss.command;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.zzt.eternal_abyss.init.ModItems;
import com.zzt.eternal_abyss.items.DepthRing;
import com.zzt.eternal_abyss.util.BaubleSyncUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.*;

public class CommandDepthRingDebug extends CommandBase {

    private static final List<String> CURSES = Arrays.asList(
            "DAMAGE_SPEED",
            "EXPERIENCE_DROP",
            "HAUNTED_SHADOWS",
            "VOID_RESISTANCE",
            "LUCKY",
            "FINAL"
    );

    @Override
    public String getName() {
        return "depthring";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/depthring <list|get|set|toggle|resetkills> [curseKey] [true|false]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP调试
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            sender.sendMessage(new TextComponentString("Only players can use this command."));
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) sender;

        if (args.length < 1) throw new CommandException(getUsage(sender));
        String sub = args[0].toLowerCase(Locale.ROOT);

        RingSlot rs = getEquippedRing(player);
        if (rs == null) {
            sender.sendMessage(new TextComponentString("You are not wearing DepthRing."));
            return;
        }

        if ("list".equals(sub)) {
            sender.sendMessage(new TextComponentString("=== DepthRing curses ==="));
            for (String k : CURSES) {
                boolean rev = DepthRing.isCurseReversed(rs.ring, k);
                int kills = DepthRing.getKillCount(rs.ring, k);
                sender.sendMessage(new TextComponentString(" - " + k + " : " + (rev ? "REVERSED" : "CURSED") + " | kills=" + kills));
            }
            return;
        }

        if (args.length < 2) throw new CommandException(getUsage(sender));
        String curseKey = args[1];

        if (!CURSES.contains(curseKey)) {
            sender.sendMessage(new TextComponentString("Unknown curseKey: " + curseKey));
            sender.sendMessage(new TextComponentString("Available: " + String.join(", ", CURSES)));
            return;
        }

        if ("get".equals(sub)) {
            boolean rev = DepthRing.isCurseReversed(rs.ring, curseKey);
            int kills = DepthRing.getKillCount(rs.ring, curseKey);
            sender.sendMessage(new TextComponentString(curseKey + " => reversed=" + rev + ", kills=" + kills));
            return;
        }

        if ("set".equals(sub)) {
            if (args.length < 3) throw new CommandException(getUsage(sender));
            boolean value = parseBool(args[2]);
            setReversed(rs.ring, curseKey, value);
            BaubleSyncUtil.syncNBTToClient(player, rs.slot, rs.ring);
            sender.sendMessage(new TextComponentString("Set " + curseKey + " reversed=" + value));
            return;
        }

        if ("toggle".equals(sub)) {
            boolean cur = DepthRing.isCurseReversed(rs.ring, curseKey);
            setReversed(rs.ring, curseKey, !cur);
            BaubleSyncUtil.syncNBTToClient(player, rs.slot, rs.ring);
            sender.sendMessage(new TextComponentString("Toggle " + curseKey + " => reversed=" + (!cur)));
            return;
        }

        if ("resetkills".equals(sub)) {
            // 只清击杀计数，不动是否反转（调试很常用）
            rs.ring.getOrCreateSubCompound("PurifyData").setInteger("Kills_" + curseKey, 0);
            BaubleSyncUtil.syncNBTToClient(player, rs.slot, rs.ring);
            sender.sendMessage(new TextComponentString("Reset kills for " + curseKey));
            return;
        }

        throw new CommandException(getUsage(sender));
    }

    private void setReversed(ItemStack ring, String curseKey, boolean value) {
        // 你现在的反转状态 key 都在 PurifyData，并且每个都有独立字段
        // 这里严格按你 DepthRing 的字段名来写
        switch (curseKey) {
            case "DAMAGE_SPEED":
                ring.getOrCreateSubCompound("PurifyData").setBoolean("DamageReversed", value);
                break;
            case "EXPERIENCE_DROP":
                ring.getOrCreateSubCompound("PurifyData").setBoolean("ExpBoostReversed", value);
                break;
            case "HAUNTED_SHADOWS":
                ring.getOrCreateSubCompound("PurifyData").setBoolean("HauntedReversed", value);
                break;
            case "VOID_RESISTANCE":
                ring.getOrCreateSubCompound("PurifyData").setBoolean("VoidResistReversed", value);
                break;
            case "LUCKY":
                ring.getOrCreateSubCompound("PurifyData").setBoolean("LuckyReversed", value);
                break;
            case "FINAL":
                ring.getOrCreateSubCompound("PurifyData").setBoolean("FinalReversed", value);
                break;
        }
    }

    private boolean parseBool(String s) throws CommandException {
        if ("true".equalsIgnoreCase(s) || "1".equals(s) || "yes".equalsIgnoreCase(s)) return true;
        if ("false".equalsIgnoreCase(s) || "0".equals(s) || "no".equalsIgnoreCase(s)) return false;
        throw new CommandException("Invalid boolean: " + s);
    }

    private RingSlot getEquippedRing(EntityPlayerMP player) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        if (handler == null) return null;

        int slot = BaublesApi.isBaubleEquipped(player, ModItems.DEPTH_RING);
        if (slot == -1) return null;

        ItemStack ring = handler.getStackInSlot(slot);
        if (ring.isEmpty() || !(ring.getItem() instanceof DepthRing)) return null;

        return new RingSlot(slot, ring);
    }

    private static class RingSlot {
        final int slot;
        final ItemStack ring;
        RingSlot(int slot, ItemStack ring) {
            this.slot = slot;
            this.ring = ring;
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable net.minecraft.util.math.BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "list", "get", "set", "toggle", "resetkills");
        }
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, CURSES);
        }
        if (args.length == 3 && "set".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, "true", "false");
        }
        return Collections.emptyList();
    }
}