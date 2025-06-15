package top.mrxiaom.sweet.locks.func.entry;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public enum ClickAction {
    LEFT(Action.LEFT_CLICK_BLOCK, false),
    RIGHT(Action.RIGHT_CLICK_BLOCK, false),
    SHIFT_LEFT(Action.LEFT_CLICK_BLOCK, true),
    SHIFT_RIGHT(Action.RIGHT_CLICK_BLOCK, true)

    ;
    private final Action action;
    private final boolean sneaking;
    ClickAction(Action action, boolean sneaking) {
        this.action = action;
        this.sneaking = sneaking;
    }

    public boolean isMatch(Action action, Player player) {
        return this.action.equals(action) && player.isSneaking() == sneaking;
    }
}
