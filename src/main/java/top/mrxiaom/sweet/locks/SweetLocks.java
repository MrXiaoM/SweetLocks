package top.mrxiaom.sweet.locks;
        
import com.tcoded.folialib.impl.PlatformScheduler;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.economy.EnumEconomy;
import top.mrxiaom.pluginbase.economy.IEconomy;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;

public class SweetLocks extends BukkitPlugin {
    public static SweetLocks getInstance() {
        return (SweetLocks) BukkitPlugin.getInstance();
    }

    private final PlatformScheduler platform;
    public SweetLocks() {
        super(options()
                .bungee(false)
                .adventure(true)
                .database(false)
                .reconnectDatabaseWhenReloadConfig(false)
                .economy(EnumEconomy.VAULT)
                .scanIgnore("top.mrxiaom.sweet.locks.libs")
        );
        FoliaLibScheduler scheduler = new FoliaLibScheduler(this);
        this.scheduler = scheduler;
        this.platform = scheduler.getFoliaLib().getScheduler();
    }
    @NotNull
    public IEconomy getEconomy() {
        return options.economy();
    }

    @NotNull
    public PlatformScheduler getPlatform() {
        return this.platform;
    }

    @Override
    protected void beforeLoad() {
        MinecraftVersion.replaceLogger(getLogger());
        MinecraftVersion.disableUpdateCheck();
        MinecraftVersion.disableBStats();
        MinecraftVersion.getVersion();
        SignEditor.init();
    }

    @Override
    protected void afterEnable() {
        getLogger().info("SweetLocks 加载完毕");
    }
}
