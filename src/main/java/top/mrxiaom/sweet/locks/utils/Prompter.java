package top.mrxiaom.sweet.locks.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class Prompter {
    private Prompter() {}

    public static void onChat(JavaPlugin plugin, Player player, Consumer<String> consumer) {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            private final String name = player.getName();
            @EventHandler(priority = EventPriority.LOWEST)
            public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
                if (e.getPlayer().getName().equals(name)) {
                    e.setCancelled(true);
                    consumer.accept(e.getMessage());
                    HandlerList.unregisterAll(this);
                }
            }
            @EventHandler
            public void onQuit(PlayerQuitEvent e) {
                if (e.getPlayer().getName().equals(name)) {
                    HandlerList.unregisterAll(this);
                }
            }
        }, plugin);
    }
}
