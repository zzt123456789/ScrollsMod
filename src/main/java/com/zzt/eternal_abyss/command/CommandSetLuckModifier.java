package com.zzt.eternal_abyss.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CommandSetLuckModifier extends CommandBase {

    private static final UUID LUCK_MODIFIER_ID = UUID.fromString("2c77c3c0-3cfd-4d45-b7db-1e0ddf77a951");

    @Override
    public String getName() {
        return "setLuckModifier";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/setLuckModifier <player> <luck>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {

        if (args.length != 2)
            throw new WrongUsageException(getUsage(sender));

        EntityPlayerMP target = getPlayer(server, sender, args[0]);
        double luck = parseDouble(args[1]);

        if (luck < 0 || luck > 1024)
            throw new CommandException("§c幸运值必须在0到1024之间！");

        // ⭐ 核心改动：只调用 helper
        com.zzt.eternal_abyss.util.LuckModifierHelper
                .setLuck(target, luck);

        sender.sendMessage(
                new TextComponentString("§a成功将 "
                        + target.getName()
                        + " 的幸运值设置为：" + luck)
        );
    }



    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
