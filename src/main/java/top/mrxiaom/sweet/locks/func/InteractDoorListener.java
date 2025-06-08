package top.mrxiaom.sweet.locks.func;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.economy.IEconomy;
import top.mrxiaom.pluginbase.economy.NoEconomy;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.ListPair;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.locks.Messages;
import top.mrxiaom.sweet.locks.SignEditor;
import top.mrxiaom.sweet.locks.SweetLocks;
import top.mrxiaom.sweet.locks.data.LockData;
import top.mrxiaom.sweet.locks.func.entry.FlagDisplay;
import top.mrxiaom.sweet.locks.gui.GuiEditLock;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class InteractDoorListener extends AbstractModule implements Listener {
    private final boolean supportBlockData = Util.isPresent("org.bukkit.block.data.BlockData");
    private final List<String> doors = new ArrayList<>();
    private final List<String> notSolidMaterials = new ArrayList<>();
    private double taxPercent;
    private boolean preventSolidTarget;
    private double solidMinHeight;
    public InteractDoorListener(SweetLocks plugin) {
        super(plugin);
        registerEvents();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        doors.clear();
        notSolidMaterials.clear();
        for (String s : config.getStringList("door-blocks")) {
            doors.add(s.toUpperCase());
        }
        for (String s : config.getStringList("prevent-solid-target.not-solid-materials")) {
            notSolidMaterials.add(s.toUpperCase());
        }
        String taxString = config.getString("money.tax", "0%");
        if (taxString.endsWith("%")) {
            double v = Util.parseDouble(taxString.replace("%", "")).orElse(0.0);
            taxPercent = v / 100.0;
        } else {
            taxPercent = Util.parseDouble(taxString).orElse(0.0);
        }
        preventSolidTarget = config.getBoolean("prevent-solid-target.enable", true);
        solidMinHeight = config.getDouble("prevent-solid-target.min-height", 0.6);
    }

    public boolean isSolid(Block block) {
        if (block.getPistonMoveReaction().equals(PistonMoveReaction.BREAK)) {
            return false;
        }
        if (notSolidMaterials.contains(block.getType().name().toUpperCase())) {
            return false;
        }
        try {
            if (block.getBoundingBox().getHeight() < solidMinHeight) {
                return false;
            }
        } catch (Throwable ignored) {}
        return true;
    }

    public boolean isDoorBlock(Block block) {
        return doors.contains(block.getType().name().toUpperCase());
    }

    private boolean isOffHand(PlayerInteractEvent e) {
        try {
            return e.getHand().equals(EquipmentSlot.OFF_HAND);
        } catch (Throwable t) {
            return false;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.useInteractedBlock().equals(Event.Result.DENY)) return;
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        BlockFace clickFace = e.getBlockFace();
        if (block == null || !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (plugin.isInDisabledWorld(block)) return;

        // 右键点击铁门
        if (isDoorBlock(block)) {
            Block baseBlock = isBottomHalfDoor(block)
                    ? block.getRelative(0, 2, 0)
                    : block.getRelative(0, 1, 0);
            LockData data = findSign(baseBlock, clickFace);
            if (data == null) return;
            e.setCancelled(true);
            if (isOffHand(e)) return;
            BlockFace signFace = SignEditor.getWallSignFacing(data.getSign());
            SignLinesFormatter formatter = SignLinesFormatter.inst();
            ListPair<String, Object> replacements = new ListPair<>();
            replacements.add("%player%", formatter.formatOwner(data));
            replacements.add("%price%", formatter.formatPrice(data));
            // 是否正在进入收费门
            boolean isEntering = signFace.equals(clickFace);
            int reach = isEntering ? data.getReachEnter() : data.getReachLeave();
            Block targetBlock = baseBlock.getRelative(clickFace.getOppositeFace(), Math.max(1, 1 + reach))
                        .getRelative(BlockFace.DOWN, 2);
            // 判定是否有实心方块堵门
            if (preventSolidTarget) {
                if (isSolid(targetBlock))  return;
                if (isSolid(targetBlock.getRelative(BlockFace.UP))) return;
            }
            if (!player.isSneaking()) {
                (isEntering ? Messages.door__entering : Messages.door__leaving).tm(player, replacements);
                return;
            }
            // 判定处理 flags
            if (isEntering) { // 进入时
                if(!data.hasFlag("can-enter")) { // 进
                    Messages.door__can_not_enter.tm(player);
                    return;
                }
                if (data.hasFlag("no-items")) { // 空
                    boolean hasItem = false;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && !item.getType().equals(Material.AIR)) {
                            hasItem = true;
                            break;
                        }
                    }
                    if (hasItem) {
                        Messages.door__has_items.tm(player);
                        return;
                    }
                }
                if (data.hasFlag("no-money")) { // 钱
                    IEconomy economy = plugin.getEconomy();
                    if (!(economy instanceof NoEconomy) && economy.get(player) > data.getPrice()) {
                        Messages.door__has_money.tm(player);
                        return;
                    }
                }
                if (data.hasFlag("no-potions")) { // 效
                    if (!player.getActivePotionEffects().isEmpty()) {
                        Messages.door__has_potions.tm(player);
                        return;
                    }
                }
            } else { // 离开时
                if (!data.hasFlag("can-leave")) { // 出
                    Messages.door__can_not_leave.tm(player);
                    return;
                }
            }
            // 非创建者进门收费
            double price = data.getPrice(); // 价格 - 扣除玩家的金币
            double tax; // 税收
            if (player.hasPermission("sweet.locks.bypass.tax")) {
                tax = 0.0;
            } else {
                tax = price * taxPercent;
            }
            double money = Math.max(0, price - tax); // 最终金钱 - 给创建者的金币
            if (isEntering && price > 0 && !data.isOwner(player)) {
                IEconomy economy = plugin.getEconomy();
                if (economy.has(player, price)) {
                    economy.takeMoney(player, price);
                    economy.giveMoney(data.getOwner(), money);
                } else {
                    Messages.door__money_not_enough.tm(player);
                    return;
                }
            }
            // 进出收费门
            plugin.getPlatform().runAtEntity(player, t -> {
                // 传送目标
                Location target = toCenterLocation(targetBlock);
                target.setDirection(player.getLocation().getDirection());
                plugin.getPlatform().teleportAsync(player, target);
                if (isEntering) {
                    if (data.isOwner(player)) {
                        Messages.door__have_entered_owner.tm(player, replacements);
                    } else {
                        Messages.door__have_entered.tm(player, replacements);
                    }
                    if (!data.isOwner(player)) plugin.getScheduler().runTaskAsync(() -> {
                        String moneyStr = formatter.formatMoney(money);
                        String taxStr = tax > 0 ? formatter.formatMoney(tax) : null;
                        OwnerNoticeManager.inst().notice(data.getOwner(), player, data.getSign().getBlock(), moneyStr, taxStr);
                    });
                } else {
                    Messages.door__have_left.tm(player, replacements);
                }
            });
            return;
        }

        // 右键点击收费门牌子
        BlockState state = block.getState();
        if (state instanceof Sign) {
            LockData data = SignEditor.get((Sign) state);
            if (data == null) return;
            e.setCancelled(true);
            if (isOffHand(e)) return;
            if (player.isSneaking() && (data.isOwner(player) || player.isOp())) {
                if (GuiManager.inst().getOpeningGui(player) == null) {
                    plugin.getScheduler().runTask(() -> {
                        GuiEditLock.inst().create(player, data).open();
                    });
                }
            } else {
                SignLinesFormatter formatter = SignLinesFormatter.inst();
                Messages messages = data.isOwner(player)
                        ? Messages.door__information_owner
                        : Messages.door__information;
                ListPair<String, Object> replacements = new ListPair<>();
                replacements.add("%player%", formatter.formatOwner(data));
                replacements.add("%price%", formatter.formatPrice(data));
                List<String> list = messages.list(replacements);
                for (String s : list) {
                    if (s.equals("flags")) {
                        for (String flag : data.getFlags()) {
                            FlagDisplay display = formatter.getFlag(flag);
                            String info = display != null ? display.info : flag;
                            Messages.door__flag.tm(player, Pair.of("%flag%", info));
                        }
                        continue;
                    }
                    AdventureUtil.sendMessage(player, s);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private boolean isBottomHalfDoor(Block block) {
        if (supportBlockData) {
            org.bukkit.block.data.BlockData data = block.getState().getBlockData();
            if (data instanceof org.bukkit.block.data.Bisected) {
                return ((org.bukkit.block.data.Bisected) data).getHalf().equals(org.bukkit.block.data.Bisected.Half.BOTTOM);
            }
        } else {
            org.bukkit.material.MaterialData data = block.getState().getData();
            if (data instanceof org.bukkit.material.Door) {
                return !((org.bukkit.material.Door) data).isTopHalf();
            }
        }
        return false;
    }

    private @Nullable LockData findSign(Block baseBlock, BlockFace clickFace) {
        BlockState b1 = baseBlock.getRelative(clickFace).getState();
        if (b1 instanceof Sign) {
            LockData data = SignEditor.get((Sign) b1);
            if (data != null) {
                return data;
            }
        }
        BlockState b2 = baseBlock.getRelative(clickFace.getOppositeFace()).getState();
        if (b2 instanceof Sign) {
            LockData data = SignEditor.get((Sign) b2);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    private Location toCenterLocation(Block block) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        return new Location(block.getWorld(),
                x > 0 ? (x - 0.5) : (x + 0.5),
                y,
                z > 0 ? (z - 0.5) : (z + 0.5)
        );
    }

    public static InteractDoorListener inst() {
        return instanceOf(InteractDoorListener.class);
    }
}
