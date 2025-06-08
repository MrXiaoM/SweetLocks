package top.mrxiaom.sweet.locks.gui;

import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.gui.IGui;
import top.mrxiaom.pluginbase.utils.*;
import top.mrxiaom.sweet.locks.Messages;
import top.mrxiaom.sweet.locks.SweetLocks;
import top.mrxiaom.sweet.locks.data.LockData;
import top.mrxiaom.sweet.locks.func.AbstractGuiModule;
import top.mrxiaom.sweet.locks.func.SignLinesFormatter;
import top.mrxiaom.sweet.locks.gui.edit.FlagIcon;
import top.mrxiaom.sweet.locks.utils.Prompter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoRegister
public class GuiEditLock extends AbstractGuiModule {
    private final Map<String, String> presetFlagMap = new HashMap<>();
    private final Map<Character, FlagIcon> flagIcons = new HashMap<>();
    private LoadedIcon iconPrice;
    private String iconPricePromptTips;
    private String iconPricePromptCancel;
    private Double priceMin;
    private Double priceMax;
    public GuiEditLock(SweetLocks plugin) {
        super(plugin, plugin.resolve("./gui/edit-lock.yml"));
        presetFlagMap.put("进", "can-enter");
        presetFlagMap.put("出", "can-leave");
        presetFlagMap.put("空", "no-items");
        presetFlagMap.put("钱", "no-money");
        presetFlagMap.put("效", "no-potions");
    }

    @Override
    protected String warningPrefix() {
        return "[gui/edit-lock]";
    }

    @Override
    public void reloadConfig(MemoryConfiguration cfg) {
        if (!file.exists()) {
            plugin.saveResource("gui/edit-lock.yml", file);
        }
        super.reloadConfig(cfg);
    }

    @Override
    protected void reloadMenuConfig(YamlConfiguration config) {
        flagIcons.clear();
        String priceMinStr = config.getString("money.min", "0");
        String priceMaxStr = config.getString("money.max", "10000");
        priceMin = priceMinStr.equals("unlimited") ? null
                : Util.parseDouble(priceMinStr).orElse(0.0);
        priceMax = priceMinStr.equals("unlimited") ? null
                : Util.parseDouble(priceMaxStr).orElse(10000.0);
    }

    @Override
    protected void loadMainIcon(ConfigurationSection section, String id, LoadedIcon icon) {
        if (id.length() != 1) return;
        if (id.equals("价")) {
            iconPrice = icon;
            iconPricePromptTips = section.getString(id = ".prompt.tips", "&7[&b收费门&7]&f 请在聊天栏发送&e收费门进入价格&7 (发送&f cancel &7取消设置)");
            iconPricePromptCancel = section.getString(id = ".prompt.cancel", "cancel");
            return;
        }
        String flag = presetFlagMap.get(id);
        if (flag != null) {
            char iconId = id.charAt(0);
            String with = section.getString(id + ".with", "&a是");
            String none = section.getString(id + ".none", "&c否");
            flagIcons.put(iconId, new FlagIcon(iconId, icon, flag, with, none));
        }
    }

    @Override
    protected ItemStack applyMainIcon(IGui instance, Player player, char id, int index, int appearTimes) {
        Impl gui = (Impl) instance;
        SignLinesFormatter formatter = SignLinesFormatter.inst();
        if (id == '价') {
            String price = formatter.formatPrice(gui.getData());
            return iconPrice.generateIcon(player, null, oldLore -> {
                List<String> lore = new ArrayList<>();
                for (String line : oldLore) {
                    lore.add(line.replace("%price%", price));
                }
                return lore;
            });
        }
        FlagIcon flagIcon = flagIcons.get(id);
        if (flagIcon != null) {
            String flag = gui.getData().hasFlag(flagIcon.flag)
                    ? flagIcon.with
                    : flagIcon.none;
            return flagIcon.icon.generateIcon(player, null, oldLore -> {
                List<String> lore = new ArrayList<>();
                for (String line : oldLore) {
                    lore.add(line.replace("%flag%", flag));
                }
                return lore;
            });
        }
        return null;
    }

    public Impl create(Player player, LockData data) {
        return new Impl(player, data);
    }

    public class Impl extends Gui implements InventoryHolder {
        private final LockData data;
        /**
         * 主要图标锁，以免在运行定时器时再次点击图标
         */
        private boolean clicked = false;
        private Inventory inventory;
        protected Impl(Player player, LockData data) {
            super(player, guiTitle, guiInventory);
            this.data = data;
        }

        public LockData getData() {
            return data;
        }

        @NotNull
        @Override
        public Inventory getInventory() {
            return inventory;
        }

        @Override
        public void updateInventory(Inventory inv) {
            super.updateInventory(inv);
            clicked = false;
        }

        @Override
        public void updateInventory(InventoryView view) {
            super.updateInventory(view);
            clicked = false;
        }

        @Override
        protected Inventory create(InventoryHolder holder, int size, String title) {
            String parsedTitle = ColorHelper.parseColor(PAPI.setPlaceholders(player, title));
            return inventory = super.create(this, size, parsedTitle);
        }

        @Override
        public void onClick(
                InventoryAction action, ClickType click,
                InventoryType.SlotType slotType, int slot,
                ItemStack currentItem, ItemStack cursor,
                InventoryView view, InventoryClickEvent event
        ) {
            event.setCancelled(true);
            Character clickedId = getClickedId(slot);
            if (clickedId == null || clicked) return;
            if (clickedId.equals('价')) {
                // 设置收费门价格
                clicked = true;
                player.closeInventory();
                AdventureUtil.sendMessage(player, iconPricePromptTips);
                Prompter.onChat(plugin, player, message -> {
                    boolean save = false;
                    if (!message.equals(iconPricePromptCancel)) {
                        Double price = Util.parseDouble(message).orElse(null);
                        if (price != null) {
                            if (priceMin != null && price < priceMin) {
                                Messages.price__min_limited.tm(player, Pair.of("%money%", priceMin));
                            } else if (priceMax != null && price > priceMax) {
                                Messages.price__max_limited.tm(player, Pair.of("%money%", priceMax));
                            } else {
                                data.setPrice(price);
                                save = true;
                            }
                        } else {
                            Messages.price__not_number.tm(player);
                        }
                    }
                    if (save) {
                        plugin.getPlatform().runAtLocation(data.getLocation(), t -> {
                            SignLinesFormatter formatter = SignLinesFormatter.inst();
                            data.save(formatter.generateLockSignLines(data));
                            plugin.getScheduler().runTask(() -> open());
                        });
                    } else {
                        plugin.getScheduler().runTask(() -> open());
                    }
                });
                return;
            }
            FlagIcon flagIcon = flagIcons.get(clickedId);
            if (flagIcon != null) {
                clicked = true;
                // 反转标志
                String flag = flagIcon.flag;
                if (data.hasFlag(flag)) {
                    data.removeFlag(flag);
                } else {
                    data.addFlags(flag);
                }
                plugin.getPlatform().runAtLocation(data.getLocation(), t -> {
                    SignLinesFormatter formatter = SignLinesFormatter.inst();
                    data.save(formatter.generateLockSignLines(data));
                    plugin.getScheduler().runTask(() -> updateInventory(inventory));
                });
                return;
            }
            // 其它图标点击
            handleOtherClick(click, slot);
        }
    }

    public static GuiEditLock inst() {
        return instanceOf(GuiEditLock.class);
    }
}
