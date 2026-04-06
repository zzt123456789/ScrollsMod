package com.zzt.eternal_abyss.tileentity;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;

public class TileEntityXPDrain extends TileEntity implements ITickable {

    private static final int XP_TO_MB = 20; // 1 XP = 20 mB xpjuice

    @Override
    public void update() {
        if (world.isRemote) return;

        TileEntity below = world.getTileEntity(pos.down());
        if (below == null || !below.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP)) return;

        IFluidHandler tank = below.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP);
        if (tank == null) return;

        Fluid xpFluid = FluidRegistry.getFluid("xpjuice");
        if (xpFluid == null) return;

        // 吸经验球
        for (EntityXPOrb orb : getXPOrbs(world, pos)) {
            if (orb.isDead) continue;

            int mb = orb.getXpValue() * XP_TO_MB;
            FluidStack stack = new FluidStack(xpFluid, mb);
            int filled = tank.fill(stack, false);

            if (filled == mb) {
                tank.fill(stack, true);
                orb.setDead();
            }
        }

        // 吸玩家经验
        for (EntityPlayer player : getPlayers(world, pos)) {
            if (!player.isSneaking()) continue;

            int totalXP = getPlayerXP(player);
            if (totalXP <= 0) continue;

            int drainXP = Math.min(20, totalXP); // 每 tick 最多吸 4 XP
            int mb = drainXP * XP_TO_MB;
            FluidStack stack = new FluidStack(xpFluid, mb);

            int filled = tank.fill(stack, false);
            int acceptedXP = filled / XP_TO_MB;

            if (acceptedXP > 0) {
                stack.amount = acceptedXP * XP_TO_MB;
                tank.fill(stack, true);
                drainPlayerXP(player, acceptedXP);

                world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        player.getSoundCategory(), 0.1F, 1.0F);
            }
        }
    }

    private List<EntityXPOrb> getXPOrbs(World world, BlockPos pos) {
        return world.getEntitiesWithinAABB(EntityXPOrb.class,
                new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 0.3, pos.getZ() + 1));
    }

    private List<EntityPlayer> getPlayers(World world, BlockPos pos) {
        return world.getEntitiesWithinAABB(EntityPlayer.class,
                new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1));
    }

    // ==== 经验处理工具 ====

    // 获取玩家所有等级与条形图之和的经验
    private int getPlayerXP(EntityPlayer player) {
        int xp = 0;
        for (int i = 0; i < player.experienceLevel; i++) {
            xp += getXpForLevel(i);
        }
        xp += Math.round(player.experience * getXpForLevel(player.experienceLevel));
        return xp;
    }

    // 安全地设置玩家新的经验总量（会自动重建等级条）
    private void setPlayerXP(EntityPlayer player, int xp) {
        player.experienceTotal = xp;
        player.experienceLevel = 0;
        player.experience = 0f;

        int remaining = xp;
        while (true) {
            int xpForLevel = getXpForLevel(player.experienceLevel);
            if (remaining >= xpForLevel) {
                remaining -= xpForLevel;
                player.experienceLevel++;
            } else {
                player.experience = (float) remaining / (float) xpForLevel;
                break;
            }
        }
    }

    // 从玩家总经验中扣除 xp 点数
    private void drainPlayerXP(EntityPlayer player, int xpToDrain) {
        int currentXP = getPlayerXP(player);
        setPlayerXP(player, Math.max(0, currentXP - xpToDrain));
    }

    // 等级所需经验（Mojang 原始算法）
    private int getXpForLevel(int level) {
        return level >= 30 ? 112 + (level - 30) * 9 :
                level >= 15 ? 37 + (level - 15) * 5 :
                        7 + level * 2;
    }
}
