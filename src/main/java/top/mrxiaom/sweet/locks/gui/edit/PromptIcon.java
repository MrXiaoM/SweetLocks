package top.mrxiaom.sweet.locks.gui.edit;

import top.mrxiaom.pluginbase.func.gui.LoadedIcon;

public class PromptIcon {
    public final LoadedIcon icon;
    public final String promptTips;
    public final String promptCancel;

    public PromptIcon(LoadedIcon icon, String promptTips, String promptCancel) {
        this.icon = icon;
        this.promptTips = promptTips;
        this.promptCancel = promptCancel;
    }
}
