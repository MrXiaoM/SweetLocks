package top.mrxiaom.sweet.locks.nms;

import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;

import java.util.List;

public interface ISign {
    void setLines(Sign sign, List<Component> lines);
}
