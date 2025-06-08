package top.mrxiaom.sweet.locks.nms;

import net.minecraft.server.v1_11_R1.TileEntity;
import net.minecraft.server.v1_11_R1.TileEntitySign;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.block.CraftBlock;

public class Block_v1_11_R1 implements IBlock {
    @Override
    public void update(Block block) {
        CraftWorld world = (CraftWorld) block.getWorld();
        TileEntity tile = world.getTileEntityAt(block.getX(), block.getY(), block.getZ());
        tile.update();
    }
}
