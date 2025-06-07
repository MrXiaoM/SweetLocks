package top.mrxiaom.sweet.locks;

import top.mrxiaom.pluginbase.func.language.AbstractLanguageHolder;
import top.mrxiaom.pluginbase.func.language.IHolderAccessor;
import top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder;

import java.util.List;

import static top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder.wrap;

public enum Messages implements IHolderAccessor {
    commands__reload("&a配置文件已重载"),
    commands__help__player("",
            "&e&lSweetLocks &b&l收费门帮助&r",
            "&a要创建一个收费门，需要在铁门上方放置一个告示牌，内容如下",
            "  &7第1行    &f$lock",
            "  &7第2行留空",
            "  &7第3行留空",
            "  &7第4行留空",
            "&a与记忆中的不同? 收费门已完全使用菜单来配置，",
            "&a无需再记忆繁琐的创建格式!",
            "&bShift+右键&a 点击木牌即可打开编辑菜单",
            ""),
    commands__help__operator("",
            "&e&lSweetLocks &b&l收费门帮助&r",
            "&a要创建一个收费门，需要在铁门上方放置一个告示牌，内容如下",
            "  &7第1行    &f$lock",
            "  &7第2行留空",
            "  &7第3行留空",
            "  &7第4行留空",
            "&a与记忆中的不同? 收费门已完全使用菜单来配置，",
            "&a无需再记忆繁琐的创建格式!",
            "&bShift+右键&a 点击木牌即可打开编辑菜单",
            "",
            "&f/locks reload &e重载插件配置文件",
            "")


    ;
    Messages(String defaultValue) {
        holder = wrap(this, defaultValue);
    }
    Messages(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }
    Messages(List<String> defaultValue) {
        holder = wrap(this, defaultValue);
    }
    private final LanguageEnumAutoHolder<Messages> holder;
    public LanguageEnumAutoHolder<Messages> holder() {
        return holder;
    }
}
