package top.mrxiaom.sweet.locks.func.entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permissible;
import top.mrxiaom.pluginbase.utils.Util;

public class Group {
    private final String name;
    private final String permission;
    private final int priority;
    private double createPrice;
    private Double priceMin, priceMax;
    private double taxPercent;
    private int reachEnterMin, reachEnterMax;
    private int reachLeaveMin, reachLeaveMax;

    public Group(String name, int priority) {
        this.name = name;
        this.permission = "sweet.locks.group." + name;
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }

    public void load(ConfigurationSection section, Group defaultGroup) {
        this.createPrice = section.getDouble("create-price", defaultGroup.createPrice);

        String priceMinStr = section.getString("money.min", null);
        String priceMaxStr = section.getString("money.max", null);
        if (priceMinStr == null) {
            priceMin = defaultGroup.priceMin;
        } else {
            priceMin = priceMinStr.equals("unlimited") ? null
                    : Util.parseDouble(priceMinStr).orElse(defaultGroup.priceMin);
        }
        if (priceMaxStr == null) {
            priceMax = defaultGroup.priceMax;
        } else {
            priceMax = priceMinStr.equals("unlimited") ? null
                    : Util.parseDouble(priceMaxStr).orElse(defaultGroup.priceMax);
        }

        String taxString = section.getString("money.tax", null);
        if (taxString == null) {
            taxPercent = defaultGroup.taxPercent;
        } else if (taxString.endsWith("%")) {
            double v = Util.parseDouble(taxString.replace("%", "")).orElse(0.0);
            taxPercent = v / 100.0;
        } else {
            taxPercent = Util.parseDouble(taxString).orElse(0.0);
        }

        reachEnterMin = section.getInt("reach-enter.min", defaultGroup.reachEnterMin);
        reachEnterMax = section.getInt("reach-enter.max", defaultGroup.reachEnterMax);
        reachLeaveMin = section.getInt("reach-leave.min", defaultGroup.reachLeaveMin);
        reachLeaveMax = section.getInt("reach-leave.max", defaultGroup.reachLeaveMax);
    }

    public void loadDefault(ConfigurationSection config) {
        this.createPrice = config.getDouble("create.create-price", 0.0);

        String priceMinStr = config.getString("money.min", "0");
        String priceMaxStr = config.getString("money.max", "10000");
        priceMin = priceMinStr.equals("unlimited") ? null
                : Util.parseDouble(priceMinStr).orElse(0.0);
        priceMax = priceMinStr.equals("unlimited") ? null
                : Util.parseDouble(priceMaxStr).orElse(10000.0);

        String taxString = config.getString("money.tax", "0%");
        if (taxString.endsWith("%")) {
            double v = Util.parseDouble(taxString.replace("%", "")).orElse(0.0);
            taxPercent = v / 100.0;
        } else {
            taxPercent = Util.parseDouble(taxString).orElse(0.0);
        }

        reachEnterMin = config.getInt("reach.enter.min", 0);
        reachEnterMax = config.getInt("reach.enter.max", 0);
        reachLeaveMin = config.getInt("reach.leave.min", 0);
        reachLeaveMax = config.getInt("reach.leave.max", 0);
    }

    public boolean isInGroup(Permissible p) {
        return p.hasPermission(permission);
    }

    public String getName() {
        return name;
    }

    public double getCreatePrice() {
        return createPrice;
    }

    public Double getPriceMin() {
        return priceMin;
    }

    public Double getPriceMax() {
        return priceMax;
    }

    public double getTaxPercent() {
        return taxPercent;
    }

    public int getReachEnterMin() {
        return reachEnterMin;
    }

    public int getReachEnterMax() {
        return reachEnterMax;
    }

    public int getReachLeaveMin() {
        return reachLeaveMin;
    }

    public int getReachLeaveMax() {
        return reachLeaveMax;
    }
}
