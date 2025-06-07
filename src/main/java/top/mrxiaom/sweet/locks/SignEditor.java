package top.mrxiaom.sweet.locks;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTList;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.List;

import static top.mrxiaom.pluginbase.utils.AdventureUtil.miniMessage;

public class SignEditor {
    private static boolean supportPersistentData;
    private static boolean supportSignSide;
    private static @NotNull GsonComponentSerializer serializer = BukkitComponentSerializer.gson();

    protected static void init() {
        supportPersistentData = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_14_R1);
        supportSignSide = Util.isPresent("org.bukkit.block.sign.SignSide");
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
                    Component component = serializer.deserializeOrNull(nbt.getString("Text1"));
                    String insertion = component == null ? "" : component.style().insertion();
                    if (insertion.startsWith("SweetLocks:")) {
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
            // 1.20.x 双面告示牌支持
            if (supportSignSide) {
                ReadWriteNBT frontText = nbt.getOrCreateCompound("front_text");
                ReadWriteNBTList<String> messages = frontText.getStringList("messages");
                for (int i = 0; i < signLines.size(); i++) {
                    messages.set(i, serializeToJSON(i, content, signLines));
                }
            } else {
                for (int i = 0; i < signLines.size(); i++) {
                    nbt.setString("Text" + (i + 1), serializeToJSON(i, content, signLines));
                }
            }
        });
    }

    private static String serializeToJSON(int i, @Nullable String content, List<String> signLines) {
        Component component = miniMessage(signLines.get(i));
        if (i == 0 && !supportPersistentData && content != null) {
            // 支持 1.14 以下，通过告示牌第一行的 insertion 储存数据
            return serializer.serialize(component.style(it -> it.insertion("SweetLocks:" + content)));
        }
        return serializer.serialize(component);
    }
}
