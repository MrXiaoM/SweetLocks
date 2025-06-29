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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.locks.data.LockData;
import top.mrxiaom.sweet.locks.nms.BlockBukkit;
import top.mrxiaom.sweet.locks.nms.IBlock;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static top.mrxiaom.pluginbase.utils.AdventureUtil.miniMessage;

public class SignEditor {
    private static Gson gson = new GsonBuilder().create();
    private static boolean useNbtAsTextComponent;
    private static boolean supportPersistentData;
    private static boolean supportSignSide;
    private static boolean supportBlockData;
    private static @NotNull GsonComponentSerializer serializer = BukkitComponentSerializer.gson();
    private static @NotNull LegacyComponentSerializer legacy = BukkitComponentSerializer.legacy();
    private static IBlock blockNMS;

    protected static void init() {
        useNbtAsTextComponent = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4);
        supportPersistentData = Util.isPresent("org.bukkit.persistence.PersistentDataContainer");
        supportSignSide = Util.isPresent("org.bukkit.block.sign.SignSide");
        supportBlockData = Util.isPresent("org.bukkit.block.data.BlockData");
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
        setRaw(sign, json, signLines);
        update(sign.getBlock());
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

    private static void setRaw(@NotNull Sign sign, @Nullable String content, List<String> signLines) {
        if (supportPersistentData) {
            NBT.modifyPersistentData(sign, nbt -> {
                if (content == null) {
                    nbt.removeKey("SweetLocks");
                } else {
                    nbt.setString("SweetLocks", content);
                }
            });
        }
        NBT.modify(sign, nbt -> {
            nbt.setBoolean("is_waxed", content != null);
            // 1.20.x 双面告示牌支持
            if (supportSignSide) {
                ReadWriteNBT frontText = nbt.getOrCreateCompound("front_text");
                if (useNbtAsTextComponent) {
                    try {
                        // 1.21.5+ 文本组件改用 NBT 而非 JSON 字符串
                        frontText.removeKey("messages");
                        ReadWriteNBTCompoundList messages = frontText.getCompoundList("messages");
                        for (int i = 0; i < signLines.size(); i++) {
                            String json = serializeToJSON(i, content, signLines);
                            ReadWriteNBT component = NBT.parseNBT(json);
                            messages.addCompound().mergeCompound(component);
                        }
                    } catch (Exception e) {
                        // 如果解析出错，则使用旧版颜色字符来应用木牌
                        frontText.removeKey("messages");
                        ReadWriteNBTList<String> messages = frontText.getStringList("messages");
                        for (int i = 0; i < signLines.size(); i++) {
                            String str = signLines.get(i);
                            String legacyText = str.isEmpty() ? "" : legacy.serialize(miniMessage(str));
                            messages.set(i, legacyText);
                        }
                    }
                } else {
                    frontText.removeKey("messages");
                    ReadWriteNBTList<String> messages = frontText.getStringList("messages");
                    for (int i = 0; i < signLines.size(); i++) {
                        messages.set(i, serializeToJSON(i, content, signLines));
                    }
                }
            } else {
                // 旧版的单面告示牌格式
                for (int i = 0; i < signLines.size(); i++) {
                    nbt.setString("Text" + (i + 1), serializeToJSON(i, content, signLines));
                }
            }
        });
    }

    private static String serializeToJSON(int i, @Nullable String content, List<String> signLines) {
        String str = signLines.get(i);
        Component component = str.isEmpty() ? Component.text("") : miniMessage(str);
        if (!supportPersistentData && i == 0 && content != null) {
            // 支持 1.14 以下，通过告示牌第一行的 insertion 储存数据
            return serializer.serialize(component.insertion("SweetLocks:" + content));
        }
        return serializer.serialize(component);
    }
}
