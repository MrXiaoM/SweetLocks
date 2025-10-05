package top.mrxiaom.sweet.locks;

import com.google.common.collect.Lists;
import com.google.gson.*;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTCompoundList;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTList;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.SignSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.locks.data.LockData;
import top.mrxiaom.sweet.locks.nms.BlockBukkit;
import top.mrxiaom.sweet.locks.nms.IBlock;
import top.mrxiaom.sweet.locks.nms.ISign;
import top.mrxiaom.sweet.locks.nms.SignNBT;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static top.mrxiaom.pluginbase.utils.AdventureUtil.miniMessage;

public class SignEditor {
    private static Gson gson = new GsonBuilder().create();
    private static boolean supportPersistentData;
    private static boolean supportBlockData;
    private static ISign signApi;
    private static @NotNull GsonComponentSerializer serializer = BukkitComponentSerializer.gson();
    private static IBlock blockNMS;

    protected static void init() {
        supportPersistentData = Util.isPresent("org.bukkit.persistence.PersistentDataContainer");
        supportBlockData = Util.isPresent("org.bukkit.block.data.BlockData");
        try {
            ISign paper = (ISign) Class.forName("top.mrxiaom.sweet.locks.nms.SignPaper").getConstructor().newInstance();
            signApi = paper;
        } catch (Throwable ignored) {
            signApi = new SignNBT();
        }
        if (!supportPersistentData) {
            String packageName = MinecraftVersion.getVersion().getPackageName();
            try {
                Class<?> type = Class.forName("top.mrxiaom.sweet.locks.nms.Block_" + packageName);
                blockNMS = (IBlock) type.getConstructor().newInstance();
            } catch (Throwable ignored) {
                throw new IllegalStateException("当前服务器版本 " + packageName + " 不受支持!");
            }
        } else {
            blockNMS = new BlockBukkit();
        }
    }

    public static void update(Block block) {
        blockNMS.update(block);
    }

    /**
     * 获取在墙上的告示牌的面朝方向<br>
     * 当玩家点击方块的方向，等于告示牌面朝方向时，视为玩家进入
     * @return 当告示牌不在墙上时，返回 UP
     */
    @SuppressWarnings("deprecation")
    public static BlockFace getWallSignFacing(Sign sign) {
        if (supportBlockData) {
            org.bukkit.block.data.BlockData data = sign.getBlockData();
            if (data instanceof org.bukkit.block.data.type.WallSign) {
                return ((org.bukkit.block.data.type.WallSign) data).getFacing();
            }
        } else {
            // 1.12 及以下兼容
            org.bukkit.material.MaterialData data = sign.getData();
            if (data instanceof org.bukkit.material.Sign) {
                org.bukkit.material.Sign s = (org.bukkit.material.Sign) data;
                if (s.isWallSign()) {
                    return s.getFacing();
                }
            }
        }
        return BlockFace.UP;
    }

    public static @Nullable LockData get(@NotNull Sign sign) {
        String json = getRaw(sign);
        if (json != null) {
            return LockData.load(sign, json);
        }
        return null;
    }

    public static void set(@NotNull Sign sign, @Nullable LockData data, List<String> signLines) {
        if (data == null) {
            setRaw(sign, null, signLines);
            return;
        }
        String json = data.saveToJson();
        if (setRaw(sign, json, signLines)) {
            update(sign.getBlock());
        }
    }

    private static @Nullable String getRaw(@NotNull Sign sign) {
        if (supportPersistentData) {
            return NBT.getPersistentData(sign, nbt -> {
                if (nbt.hasTag("SweetLocks")) {
                    return nbt.getString("SweetLocks");
                }
                return null;
            });
        } else {
            return NBT.get(sign, nbt -> {
                // 支持 1.14 以下，通过告示牌第一行的 insertion 储存数据
                if (nbt.hasTag("Text1")) {
                    Component component = serializer.deserialize(nbt.getString("Text1"));
                    String insertion = component.insertion();
                    if (insertion != null && insertion.startsWith("SweetLocks:")) {
                        return insertion.substring(11);
                    }
                }
                return null;
            });
        }
    }

    @SuppressWarnings({"deprecation"})
    private static boolean setRaw(@NotNull Sign sign, @Nullable String content, List<String> signLines) {
        if (supportPersistentData) {
            NBT.modifyPersistentData(sign, nbt -> {
                if (content == null) {
                    nbt.removeKey("SweetLocks");
                } else {
                    nbt.setString("SweetLocks", content);
                }
            });
        }
        // 设置涂蜡 (1.20.4+)
        boolean waxed = content != null;
        try {
            sign.setWaxed(waxed);
        } catch (Throwable t) {
            sign.setEditable(!waxed);
        }
        // 设置木牌内容
        List<Component> lines = new ArrayList<>();
        for (int i = 0; i < signLines.size() && i < 4; i++) {
            lines.add(serializeToComponent(i, content, signLines));
        }
        return signApi.setLines(sign, lines);
    }

    private static Component serializeToComponent(int i, @Nullable String content, List<String> signLines) {
        String str = signLines.get(i);
        Component component = str.isEmpty() ? Component.text("") : miniMessage(str);
        if (!supportPersistentData && i == 0 && content != null) {
            // 支持 1.14 以下，通过告示牌第一行的 insertion 储存数据
            return component.insertion("SweetLocks:" + content);
        }
        return component;
    }
}
