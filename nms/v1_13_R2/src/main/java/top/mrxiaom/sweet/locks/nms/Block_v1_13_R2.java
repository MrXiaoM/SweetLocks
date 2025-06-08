package top.mrxiaom.sweet.locks.nms;

import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.TileEntity;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;

public class Block_v1_13_R2 implements IBlock {
    @Override
    public void update(Block block) {
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        CraftWorld world = (CraftWorld) block.getWorld();
        CraftBlockState state = (CraftBlockState) block.getState();
        world.getHandle().notify(position, ((CraftBlock) block).getNMS(), state.getHandle(), 3);
        TileEntity tile = world.getHandle().getTileEntity(position);
        tile.update();
    }
}
