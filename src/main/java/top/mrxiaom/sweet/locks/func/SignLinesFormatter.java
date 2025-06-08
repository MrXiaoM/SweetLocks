package top.mrxiaom.sweet.locks.func;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ListPair;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.locks.SweetLocks;
import top.mrxiaom.sweet.locks.data.LockData;
import top.mrxiaom.sweet.locks.func.entry.FlagDisplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoRegister
public class SignLinesFormatter extends AbstractModule {
    private List<String> lockSign;
    private String moneyFormat;
    private String moneyReplaceEmpty;
    private List<String> flagsOrder;
    private Map<String, FlagDisplay> flagsDisplay = new HashMap<>();
    public SignLinesFormatter(SweetLocks plugin) {
        super(plugin);
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        lockSign = config.getStringList("lock-sign");
        moneyFormat = config.getString("money.format", "%.1f");
        moneyReplaceEmpty = config.getString("money.replace-empty", null);
        flagsOrder = config.getStringList("flags.order");
        flagsDisplay.clear();
        ConfigurationSection section = config.getConfigurationSection("flags.display");
        if (section != null) for (String flag : section.getKeys(false)) {
            String with = section.getString(flag + ".with", flag);
            String none = section.getString(flag + ".none", "");
            String info = section.getString(flag + ".info", with);
            flagsDisplay.put(flag, new FlagDisplay(flag, with, none, info));
        }
    }

    @NotNull
    public String formatOwner(@NotNull LockData data) {
        OfflinePlayer owner = data.getOwner();
        String name = owner.getName();
        return name == null
                ? owner.getUniqueId().toString()
                : name;
    }

    @NotNull
    public String formatPrice(@NotNull LockData data) {
        return formatMoney(data.getPrice());
    }

    @NotNull
    public String formatMoney(double money) {
        String str = String.format(moneyFormat, money);
        return moneyReplaceEmpty != null
                ? str.replace(moneyReplaceEmpty, "")
                : str;
    }

    @NotNull
    public String formatFlags(@NotNull LockData data) {
        StringBuilder flags = new StringBuilder();
        for (String flag : flagsOrder) {
            FlagDisplay display = flagsDisplay.get(flag);
            if (display != null) {
                if (data.hasFlag(flag)) {
                    flags.append(display.with);
                } else {
                    flags.append(display.none);
                }
            }
        }
        return flags.toString();
    }

    @Nullable
    public FlagDisplay getFlag(String flag) {
        return flagsDisplay.get(flag);
    }

    @NotNull
    public List<String> generateLockSignLines(@NotNull LockData data) {
        List<String> lines = new ArrayList<>();
        ListPair<String, Object> replacements = new ListPair<>();
        replacements.add("%player%", formatOwner(data));
        replacements.add("%price%", formatPrice(data));
        replacements.add("%flags%", formatFlags(data));
        return Pair.replace(lockSign, replacements);
    }

    public static SignLinesFormatter inst() {
        return instanceOf(SignLinesFormatter.class);
    }
}
