package top.mrxiaom.sweet.locks.func;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Bukkit;
import org.bukkit.block.*;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.locks.Messages;
import top.mrxiaom.sweet.locks.SignEditor;
import top.mrxiaom.sweet.locks.SweetLocks;
import top.mrxiaom.sweet.locks.func.entry.ClickAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static top.mrxiaom.pluginbase.utils.ColorHelper.parseColor;

@AutoRegister
public class ViewContainerManager extends AbstractModule implements Listener {
    public static String FLAG_PREVIEW = "SWEETLOCKS_PREVIEW";
    public static class Holder implements InventoryHolder {
        private final Player viewer;
        private final Inventory inventory;
        public Holder(Player viewer, Inventory inv) {
            this.viewer = viewer;
            this.inventory = Bukkit.createInventory(this, inv.getType(), parseColor(Messages.viewer_title.str()));
            ItemStack[] contents = inv.getContents();
            ItemStack[] copy = new ItemStack[contents.length];
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (item != null) {
                    try {
                        NBT.modify(item, nbt -> {
                            nbt.setBoolean(FLAG_PREVIEW, true);
                        });
                    } catch (Throwable ignored) {
                    }
                    copy[i] = item.clone();
                }
            }
            this.inventory.setContents(copy);
        }

        @NotNull
        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
    private final boolean supportSignSide = Util.isPresent("org.bukkit.block.sign.SignSide");
    private @Nullable String viewSign;
    private final Set<InventoryType> viewTypes = new HashSet<>();
    private final Set<ClickAction> viewActions = new HashSet<>();
    public ViewContainerManager(SweetLocks plugin) {
        super(plugin);
        registerEvents();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        viewSign = config.getString("view-container.sign", null);
        viewTypes.clear();
        viewActions.clear();
        for (String s : config.getStringList("view-container.types")) {
            InventoryType type = Util.valueOr(InventoryType.class, s, null);
            if (type != null) {
                viewTypes.add(type);
            }
        }
        for (String s : config.getStringList("view-container.actions")) {
            ClickAction action = Util.valueOr(ClickAction.class, s, null);
            if (action != null) {
                viewActions.add(action);
            }
        }
    }

    public boolean checkView(String[] lines) {
        for (String line : lines) {
            if (line != null && line.contains(viewSign)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMatchAction(Action action, Player player) {
        for (ClickAction click : viewActions) {
            if (click.isMatch(action, player)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"deprecation"})
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (viewSign == null || e.useInteractedBlock().equals(Event.Result.DENY)) return;
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (plugin.isInDisabledWorld(block)) return;
        InventoryType type = player.getOpenInventory().getTopInventory().getType();
        if (!type.equals(InventoryType.CREATIVE) && !type.equals(InventoryType.CRAFTING)) return;
        if (!isMatchAction(e.getAction(), player)) return;

        BlockState state = block.getState();
        if (state instanceof Sign) {
            Sign sign = (Sign) state;
            boolean view = false;
            if (supportSignSide) {
                SignSide side1 = sign.getSide(Side.FRONT);
                SignSide side2 = sign.getSide(Side.FRONT);
                view = checkView(side1.getLines()) || checkView(side2.getLines());
            } else {
                view = checkView(sign.getLines());
            }
            if (!view) return;
            BlockFace facing = SignEditor.getWallSignFacing(sign);
            if (facing.equals(BlockFace.UP)) return;
            Block containerBlock = block.getRelative(facing.getOppositeFace());
            BlockState containerState = containerBlock.getState();
            if (containerState instanceof Container) {
                Container container = (Container) containerState;
                Inventory inv = container.getInventory();
                if (viewTypes.contains(inv.getType())) {
                    Holder holder = new Holder(player, inv);
                    player.openInventory(holder.getInventory());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.isCancelled()) return;
        InventoryHolder holder = e.getView().getTopInventory().getHolder();
        if (holder instanceof Holder) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.isCancelled()) return;
        InventoryHolder holder = e.getView().getTopInventory().getHolder();
        if (holder instanceof Holder) {
            e.setCancelled(true);
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                Inventory inv = player.getOpenInventory().getTopInventory();
                if (inv.getHolder() instanceof Holder) {
                    player.closeInventory();
                }
            } catch (Throwable ignored) {
                // folia 调用 getHolder() 时，如果玩家正打开原版界面，那么就会因为没有使用正确的调度器而报错
            }
        }
    }
}
