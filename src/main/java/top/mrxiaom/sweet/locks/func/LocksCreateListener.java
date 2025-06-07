package top.mrxiaom.sweet.locks.func;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.locks.SignEditor;
import top.mrxiaom.sweet.locks.SweetLocks;
import top.mrxiaom.sweet.locks.data.LockData;

import java.util.List;

@AutoRegister
public class LocksCreateListener extends AbstractModule implements Listener {
    private String createSignLine;
    private double createDefaultPrice;
    public LocksCreateListener(SweetLocks plugin) {
        super(plugin);
        registerEvents();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        this.createSignLine = config.getString("create.sign-line", "$lock");
        this.createDefaultPrice = config.getDouble("create.default-price", 0.0);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignEdit(SignChangeEvent e) {
        if (e.isCancelled() || plugin.isInDisabledWorld(e)) return;
        Block block = e.getBlock();
        BlockState state = block.getState();
        if (!(state instanceof Sign)) return;

        Sign sign = (Sign) state;
        Player player = e.getPlayer();
        String line = e.getLine(0);
        if (createSignLine.equals(line)) {
            BlockFace facing = SignEditor.getWallSignFacing(sign);
            if (facing.equals(BlockFace.UP)) {
                // 需要放置贴在墙上的告示牌
                return;
            }
            InteractDoorListener door = InteractDoorListener.inst();
            Block baseBlock = block.getRelative(facing.getOppositeFace());
            Block doorBlock = baseBlock.getRelative(BlockFace.DOWN);
            if (!door.isDoorBlock(doorBlock) || door.isDoorBlock(baseBlock)) {
                // 需要告示牌放置在铁门上面的方块上，且铁门上面的方块不能是铁门
                return;
            }
            SignLinesFormatter formatter = SignLinesFormatter.inst();
            LockData data = new LockData(sign, player, createDefaultPrice);
            data.addFlags("can-enter", "can-leave");
            data.save(formatter.generateLockSignLines(data));
            e.setCancelled(true);
            // TODO: 提示玩家创建成功
        }
    }
}
