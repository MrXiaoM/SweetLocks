package top.mrxiaom.sweet.locks.commands;
        
import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.locks.Messages;
import top.mrxiaom.sweet.locks.SweetLocks;
import top.mrxiaom.sweet.locks.func.AbstractModule;

import java.util.*;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter, Listener {
    public CommandMain(SweetLocks plugin) {
        super(plugin);
        registerCommand("sweetlocks", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && "reload".equalsIgnoreCase(args[0]) && sender.isOp()) {
            plugin.reloadConfig();
            return Messages.commands__reload.tm(sender);
        }
        return (sender.isOp() ? Messages.commands__help__operator : Messages.commands__help__player).tm(sender);
    }

    private static final List<String> emptyList = Collections.emptyList();
    private static final List<String> listArg0 = emptyList;
    private static final List<String> listOpArg0 = Lists.newArrayList("reload");
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return startsWith(sender.isOp() ? listOpArg0 : listArg0, args[0]);
        }
        return emptyList;
    }

    public List<String> startsWith(Collection<String> list, String s) {
        return startsWith(null, list, s);
    }
    public List<String> startsWith(String[] addition, Collection<String> list, String s) {
        String s1 = s.toLowerCase();
        List<String> stringList = new ArrayList<>(list);
        if (addition != null) stringList.addAll(0, Lists.newArrayList(addition));
        stringList.removeIf(it -> !it.toLowerCase().startsWith(s1));
        return stringList;
    }
}
