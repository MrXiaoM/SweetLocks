package top.mrxiaom.sweet.locks.nms;

import org.bukkit.block.Block;

public class BlockBukkit implements IBlock {
    @Override
    public void update(Block block) {
        block.getState().update();
    }
}
