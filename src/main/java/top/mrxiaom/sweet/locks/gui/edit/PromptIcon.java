package top.mrxiaom.sweet.locks.gui.edit;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.IModifier;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.sweet.locks.gui.GuiEditLock;

import java.util.List;

public class PromptIcon {
    public final LoadedIcon icon;
    public final String promptTips;
    public final String promptCancel;

    public PromptIcon(LoadedIcon icon, String promptTips, String promptCancel) {
        this.icon = icon;
        this.promptTips = promptTips;
        this.promptCancel = promptCancel;
    }

    public ItemStack generateIcon(Player player, @Nullable IModifier<List<String>> loreModifier) {
        return GuiEditLock.generateIcon(icon, player, loreModifier);
    }
}
