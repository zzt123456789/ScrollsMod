package com.zzt.eternal_abyss.blocks;


import com.zzt.eternal_abyss.EternalAbyss;
import com.zzt.eternal_abyss.tileentity.TileEntityAutoAnvil;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;


public class BlockAutoAnvil extends BlockBase {

    private static final AxisAlignedBB ANVIL_AABB = new AxisAlignedBB(
            0.125D, 0.0D, 0.0D, // minX=0.125（12 像素宽，居中），minY=0.0，minZ=0.0
            0.875D, 1.0D, 1.0D  // maxX=0.875，maxY=1.0，maxZ=1.0
    );


    public BlockAutoAnvil() {
        super(Material.ANVIL, "auto_anvil"); // 传两个参数
        this.setHardness(5.0F);
        this.setResistance(1200.0F);
        setSoundType(SoundType.ANVIL);
    }


    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return ANVIL_AABB;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityAutoAnvil();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false; // 不完全遮挡
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false; // 不是完整方块
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL; // 使用 JSON 模型渲染
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileEntityAutoAnvil) {
            TileEntityAutoAnvil anvil = (TileEntityAutoAnvil) tileentity;

            for (int i = 0; i < anvil.getSizeInventory(); ++i) {
                ItemStack stack = anvil.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    spawnAsEntity(worldIn, pos, stack);
                }
            }
        }

        super.breakBlock(worldIn, pos, state); // 最后调用父类方法（通知更新等）
    }


    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
                                    EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof TileEntityAutoAnvil) {
                TileEntityAutoAnvil anvil = (TileEntityAutoAnvil) tile;
                if (playerIn.isSneaking()) {
                    // Shift+右键 ➔ 绑定自己
                    if (anvil.getBoundPlayerUUID() == null) {
                        anvil.bindPlayer(playerIn);
                        playerIn.sendStatusMessage(new TextComponentString("§a成功绑定到你！"), true);
                    } else {
                        playerIn.sendStatusMessage(new TextComponentString("§c已经绑定，无法重新绑定！"), true);
                    }
                    return true;
                } else {
                    // 不是蹲下 ➔ 尝试打开界面
                    if (anvil.getBoundPlayerUUID() == null || anvil.getBoundPlayerUUID().equals(playerIn.getUniqueID())) {
                        playerIn.openGui(EternalAbyss.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
                    } else {
                        playerIn.sendStatusMessage(new TextComponentString("§c你不是绑定者！"), true);
                    }
                    return true;
                }
            }
        }
        return true;
    }

}
