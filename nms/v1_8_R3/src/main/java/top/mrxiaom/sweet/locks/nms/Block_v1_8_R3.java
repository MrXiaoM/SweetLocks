package top.mrxiaom.sweet.locks.nms;

import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.TileEntitySign;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;

public class Block_v1_8_R3 implements IBlock {
    @Override
    public void update(Block block) {
        CraftWorld world = (CraftWorld) block.getWorld();
        TileEntity tile = world.getTileEntityAt(block.getX(), block.getY(), block.getZ());
        tile.update();
    }
}
