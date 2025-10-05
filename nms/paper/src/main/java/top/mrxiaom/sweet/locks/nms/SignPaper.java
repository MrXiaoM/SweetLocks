package top.mrxiaom.sweet.locks.nms;

import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;

import java.util.List;

public class SignPaper implements ISign {
    public SignPaper() throws ReflectiveOperationException {
        Sign.class.getDeclaredMethod("lines");
        Sign.class.getDeclaredMethod("line", int.class, Component.class);
    }
    @Override
    public boolean setLines(Sign sign, List<Component> lines) {
        for (int i = 0; i < lines.size() && i < 4; i++) {
            sign.line(i, lines.get(i));
        }
        sign.update();
        return false;
    }
}
