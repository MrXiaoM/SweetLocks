package top.mrxiaom.sweet.locks.nms;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.TileEntitySign;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;

public class Block_v1_12_R1 implements IBlock {
    @Override
    public void update(Block block) {
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        CraftWorld world = (CraftWorld) block.getWorld();
        BlockState state = block.getState();
        IBlockData newBlock = CraftMagicNumbers.getBlock(state.getType()).fromLegacyData(state.getRawData());
        world.getHandle().notify(position, CraftMagicNumbers.getBlock(block).fromLegacyData(block.getData()), newBlock, 3);
        TileEntity tile = world.getHandle().getTileEntity(position);
        tile.update();
    }
}
