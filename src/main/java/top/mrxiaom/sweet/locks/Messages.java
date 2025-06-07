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
            ""),
    create__need_wall_sign("&7[&b收费门&7]&e 需要将告示牌贴在墙上才可创建收费门"),
    create__need_door("&7[&b收费门&7]&e 需要将告示牌贴在铁门上方才可创建收费门"),
    create__success("&7[&b收费门&7]&a 收费门创建成功!&e Shift+右键 &a点击告示牌以编辑收费门"),

    door__entering("&7[&a收费门&7]&f 这是&e %player% &f的收费门，按下&e Shift+右键 &f花费&e %price% &f金币进入"),
    door__leaving("&7[&a收费门&7]&f 这是&e %player% &f的收费门，按下&e Shift+右键 &f离开"),

    door__can_not_enter("&7[&a收费门&7]&e 这个收费门不能进入"),
    door__can_not_leave("&7[&a收费门&7]&e 这个收费门不能离开"),
    door__has_items("&7[&a收费门&7]&e 你的身上有物品，不能进入这个收费门"),
    door__has_money("&7[&a收费门&7]&e 你的账户上有金币，不能进入这个收费门"),
    door__has_potions("&7[&a收费门&7]&e 你的身上有药水效果，不能进入这个收费门"),
    door__money_not_enough("&7[&a收费门&7]&e 你没有足够的金币进入这个收费门"),

    door__information("",
            "  &b&l收费门信息&r",
            "  &f创建者: &e%player%",
            "  &f价格: &e%price%",
            "  &f标志:",
            "flags",
            ""
    ),
    door__information_owner("",
            "  &b&l收费门信息&r",
            "  &f创建者: &e%player%",
            "  &f价格: &e%price%",
            "  &f标志:",
            "flags",
            "  &bShift+右键 &f点击告示牌编辑",
            ""
    ),
    door__flag("  &8● &f%flag%"),

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
