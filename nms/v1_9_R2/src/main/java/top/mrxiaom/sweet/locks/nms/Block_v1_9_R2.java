package top.mrxiaom.sweet.locks.nms;

import net.minecraft.server.v1_9_R2.TileEntity;
import net.minecraft.server.v1_9_R2.TileEntitySign;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.block.CraftBlock;

public class Block_v1_9_R2 implements IBlock {
    @Override
    public void update(Block block) {
        CraftWorld world = (CraftWorld) block.getWorld();
        TileEntity tile = world.getTileEntityAt(block.getX(), block.getY(), block.getZ());
        tile.update();
    }
}
