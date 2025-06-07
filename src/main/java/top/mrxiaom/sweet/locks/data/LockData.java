package top.mrxiaom.sweet.locks.data;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import top.mrxiaom.sweet.locks.SignEditor;

import java.util.ArrayList;
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
    @SerializedName("flags")
    private List<String> flags;

    public LockData() {}
    public LockData(Sign sign, OfflinePlayer owner, double price) {
        this.sign = sign;
        this.owner = owner;
        this.ownerUUID = owner.getUniqueId().toString();
        this.price = price;
        this.flags = new ArrayList<>();
    }

    private void onLoad() {
        UUID uuid = UUID.fromString(this.ownerUUID);
        this.owner = Bukkit.getOfflinePlayer(uuid);
    }

    public String saveToJson() {
        return gson.toJson(this, LockData.class);
    }

    public void save(List<String> signLines) {
        SignEditor.set(sign, this, signLines);
    }

    public void remove(List<String> signLines) {
        SignEditor.set(sign, null, signLines);
    }

    public void remove() {
        remove(Lists.newArrayList("", "", "", ""));
    }

    public static LockData load(Sign sign, String json) {
        LockData data = gson.fromJson(json, LockData.class);
        data.sign = sign;
        data.onLoad();
        return data;
    }
}
