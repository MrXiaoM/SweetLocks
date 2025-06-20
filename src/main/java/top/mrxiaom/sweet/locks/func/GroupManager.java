package top.mrxiaom.sweet.locks.func;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.locks.SweetLocks;
import top.mrxiaom.sweet.locks.func.entry.Group;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@AutoRegister
public class GroupManager extends AbstractModule {
    private Group defaultGroup;
    private final List<Group> groups = new ArrayList<>();
    public GroupManager(SweetLocks plugin) {
        super(plugin);
    }

    @Override
    public int priority() {
        return 999;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        defaultGroup = new Group("default", Integer.MAX_VALUE);
        defaultGroup.loadDefault(config);

        groups.clear();
        ConfigurationSection section = config.getConfigurationSection("groups");
        if (section != null) for (String key : section.getKeys(false)) {
            ConfigurationSection section1 = section.getConfigurationSection(key);
            if (section1 == null) continue;
            int priority = section1.getInt("priority", 0);
            Group group = new Group(key, priority);
            group.load(section1, defaultGroup);
            groups.add(group);
        }
        groups.sort(Comparator.comparingInt(Group::priority));

        info("加载了 " + groups.size() + " 个权限组配置");
    }

    @NotNull
    public Group getGroup(Permissible p) {
        for (Group group : groups) {
            if (group.isInGroup(p)) {
                return group;
            }
        }
        return defaultGroup;
    }

    public static GroupManager inst() {
        return instanceOf(GroupManager.class);
    }
}
