package top.mrxiaom.sweet.locks.gui.edit;

import top.mrxiaom.pluginbase.func.gui.LoadedIcon;

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
}
