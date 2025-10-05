package top.mrxiaom.sweet.locks.nms;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTCompoundList;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTList;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SignNBT implements ISign {
    private boolean useNbtAsTextComponent;
    private boolean supportPersistentData;
    private boolean supportSignSide;
    private boolean supportBlockData;
    private static @NotNull GsonComponentSerializer serializer = BukkitComponentSerializer.gson();
    private static @NotNull LegacyComponentSerializer legacy = BukkitComponentSerializer.legacy();
    public SignNBT() {
        useNbtAsTextComponent = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4);
        supportPersistentData = isPresent("org.bukkit.persistence.PersistentDataContainer");
        supportSignSide = isPresent("org.bukkit.block.sign.SignSide");
        supportBlockData = isPresent("org.bukkit.block.data.BlockData");
    }

    public static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean setLines(Sign sign, List<Component> lines) {
        NBT.modify(sign, nbt -> {
            // 1.20.x 双面告示牌支持
            if (supportSignSide) {
                ReadWriteNBT frontText = nbt.getOrCreateCompound("front_text");
                if (useNbtAsTextComponent) {
                    try {
                        // 1.21.5+ 文本组件改用 NBT 而非 JSON 字符串
                        frontText.removeKey("messages");
                        ReadWriteNBTCompoundList messages = frontText.getCompoundList("messages");
                        for (int i = 0; i < lines.size(); i++) {
                            String json = serializer.serialize(lines.get(i));
                            ReadWriteNBT component = NBT.parseNBT(json);
                            messages.addCompound().mergeCompound(component);
                        }
                    } catch (Exception e) {
                        // 如果解析出错，则使用旧版颜色字符来应用木牌
                        frontText.removeKey("messages");
                        ReadWriteNBTList<String> messages = frontText.getStringList("messages");
                        for (int i = 0; i < lines.size(); i++) {
                            String legacyText = legacy.serialize(lines.get(i));
                            messages.set(i, legacyText);
                        }
                    }
                } else {
                    frontText.removeKey("messages");
                    ReadWriteNBTList<String> messages = frontText.getStringList("messages");
                    for (int i = 0; i < lines.size(); i++) {
                        messages.set(i, serializer.serialize(lines.get(i)));
                    }
                }
            } else {
                // 旧版的单面告示牌格式
                for (int i = 0; i < lines.size(); i++) {
                    String json = serializer.serialize(lines.get(i));
                    nbt.setString("Text" + (i + 1), json);
                }
            }
        });
        return true;
    }
}
