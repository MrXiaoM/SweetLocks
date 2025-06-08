package top.mrxiaom.sweet.locks.func;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ListPair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.locks.Messages;
import top.mrxiaom.sweet.locks.SweetLocks;

import java.io.File;
import java.io.IOException;
import java.util.*;

@AutoRegister
public class OwnerNoticeManager extends AbstractModule implements Listener {
    private class Notice {
        private final String playerName;
        private final String world;
        private final int x, y, z;
        private final String money;

        private Notice(String playerName, String world, int x, int y, int z, String money) {
            this.playerName = playerName;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.money = money;
        }

        private void notice(Player owner) {
            OwnerNoticeManager.this.notice(owner, playerName, world, x, y, z, money);
        }
    }
    private final File file;
    private final Map<UUID, List<Notice>> noticeMap = new HashMap<>();
    public OwnerNoticeManager(SweetLocks plugin) {
        super(plugin);
        this.file = plugin.resolve("./notice.yml");
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        reload();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        List<Notice> notices = noticeMap.remove(player.getUniqueId());
        if (notices != null) {
            save();
            plugin.getScheduler().runTaskLaterAsync(() -> {
                for (Notice notice : notices) {
                    notice.notice(player);
                }
            }, 20L);
        }
    }

    public void reload() {
        noticeMap.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("notice");
        if (section != null) for (String key : section.getKeys(false)) {
            UUID ownerUUID = UUID.fromString(key);
            List<Notice> notices = new ArrayList<>();
            List<ConfigurationSection> list = Util.getSectionList(section, key);
            for (ConfigurationSection entry : list) {
                String playerName = entry.getString("player-name");
                String world = entry.getString("world");
                int x = entry.getInt("x");
                int y = entry.getInt("y");
                int z = entry.getInt("z");
                String money = entry.getString("money");
                if (playerName != null && world != null && money != null) {
                    notices.add(new Notice(playerName, world, x, y, z, money));
                }
            }
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, List<Notice>> entry : noticeMap.entrySet()) {
            String uuid = entry.getKey().toString();
            List<Notice> notices = entry.getValue();
            if (notices.isEmpty()) continue;
            List<ConfigurationSection> sections = new ArrayList<>();
            for (Notice notice : notices) {
                ConfigurationSection section = new MemoryConfiguration();
                section.set("player-name", notice.playerName);
                section.set("world", notice.world);
                section.set("x", notice.x);
                section.set("y", notice.y);
                section.set("z", notice.z);
                section.set("money", notice.money);
                sections.add(section);
            }
            config.set("notice." + uuid, sections);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            warn(e);
        }
    }

    public void notice(OfflinePlayer owner, Player player, Block block, String money) {
        String world = block.getWorld().getName();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        Player p = owner.isOnline() ? owner.getPlayer() : null;
        if (p != null) {
            notice(p, player.getName(), world, x, y, z, money);
        } else {
            List<Notice> list = Util.getOrPut(noticeMap, owner.getUniqueId(), () -> new ArrayList<>());
            list.add(new Notice(player.getName(), world, x, y, z, money));
            save();
        }
    }

    public void notice(Player owner, String player, String world, int x, int y, int z, String money) {
        ListPair<String, Object> replacements = new ListPair<>();
        replacements.add("%player%", player);
        replacements.add("%world%", world);
        replacements.add("%x%", x);
        replacements.add("%y%", y);
        replacements.add("%z%", z);
        replacements.add("%money%", money);
        Messages.door__owner_notice.tm(owner, replacements);
    }

    public static OwnerNoticeManager inst() {
        return instanceOf(OwnerNoticeManager.class);
    }
}
