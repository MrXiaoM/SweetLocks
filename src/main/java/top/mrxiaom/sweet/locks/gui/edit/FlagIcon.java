package top.mrxiaom.sweet.locks.gui.edit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.IModifier;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.locks.gui.GuiEditLock;

import java.util.List;

public class FlagIcon {
    public final char id;
    public final LoadedIcon icon;
    public final String flag;
    public final String with;
    public final String none;

    public FlagIcon(char id, LoadedIcon icon, String flag, String with, String none) {
        this.id = id;
        this.icon = icon;
        this.flag = flag;
        this.with = with;
        this.none = none;
    }

    public ItemStack generateIcon(Player player, @Nullable IModifier<List<String>> loreModifier) {
        return GuiEditLock.generateIcon(icon, player, loreModifier);
    }
}
