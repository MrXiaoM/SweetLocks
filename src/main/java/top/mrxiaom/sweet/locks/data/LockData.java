package top.mrxiaom.sweet.locks.data;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.locks.SignEditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LockData {
    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
    private Sign sign;
    private OfflinePlayer owner;
    @Expose
    @SerializedName("owner-uuid")
    private String ownerUUID;
    @Expose
    @SerializedName("price")
    private double price;
    @Expose
    @SerializedName("reach-enter")
    private int reachEnter;
    @Expose
    @SerializedName("reach-leave")
    private int reachLeave;
    @Expose
    @SerializedName("flags")
    private List<String> flags;

    public LockData() {}
    public LockData(
            @NotNull Sign sign,
            @NotNull OfflinePlayer owner,
            double price,
            int reachEnter,
            int reachLeave
    ) {
        this.sign = sign;
        this.owner = owner;
        this.ownerUUID = owner.getUniqueId().toString();
        this.price = price;
        this.reachEnter = reachEnter;
        this.reachLeave = reachLeave;
        this.flags = new ArrayList<>();
    }

    private void onLoad() {
        UUID uuid = UUID.fromString(this.ownerUUID);
        this.owner = Bukkit.getOfflinePlayer(uuid);
    }

    @NotNull
    public Sign getSign() {
        return sign;
    }

    public Location getLocation() {
        return sign.getLocation();
    }

    @NotNull
    public OfflinePlayer getOwner() {
        return owner;
    }

    public void setOwner(@NotNull OfflinePlayer owner) {
        this.owner = owner;
        this.ownerUUID = owner.getUniqueId().toString();
    }

    public boolean isOwner(@NotNull OfflinePlayer player) {
        return player.getUniqueId().equals(owner.getUniqueId());
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getReachEnter() {
        return reachEnter;
    }

    public void setReachEnter(int reachEnter) {
        this.reachEnter = reachEnter;
    }

    public int getReachLeave() {
        return reachLeave;
    }

    public void setReachLeave(int reachLeave) {
        this.reachLeave = reachLeave;
    }

    @NotNull
    public List<String> getFlags() {
        return flags;
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public void setFlags(@NotNull List<String> flags) {
        this.flags = flags;
    }

    public void addFlags(@NotNull String... flags) {
        this.flags.addAll(Arrays.asList(flags));
    }

    public void removeFlag(@NotNull String flag) {
        this.flags.remove(flag);
    }

    @NotNull
    public String saveToJson() {
        return gson.toJson(this, LockData.class);
    }

    public void save(@NotNull List<String> signLines) {
        SignEditor.set(sign, this, signLines);
        sign = (Sign) sign.getBlock().getState();
        sign.update();
    }

    @Nullable
    public static LockData load(@NotNull Sign sign, @NotNull String json) {
        try {
            LockData data = gson.fromJson(json, LockData.class);
            data.sign = sign;
            data.onLoad();
            return data;
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
