package top.mrxiaom.sweet.locks.nms;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.TileEntity;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

public class Block_v1_8_R3 implements IBlock {
    @Override
    public void update(Block block) {
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        CraftWorld world = (CraftWorld) block.getWorld();
        world.getHandle().notify(position);
        TileEntity tile = world.getHandle().getTileEntity(position);
        tile.update();
    }
}
