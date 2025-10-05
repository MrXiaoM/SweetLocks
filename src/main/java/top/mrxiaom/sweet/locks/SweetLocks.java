package top.mrxiaom.sweet.locks;
        
import com.tcoded.folialib.impl.PlatformScheduler;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.block.BlockEvent;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.economy.EnumEconomy;
import top.mrxiaom.pluginbase.economy.IEconomy;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.resolver.DefaultLibraryResolver;
import top.mrxiaom.pluginbase.utils.ClassLoaderWrapper;
import top.mrxiaom.pluginbase.utils.ConfigUpdater;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class SweetLocks extends BukkitPlugin {
    public static SweetLocks getInstance() {
        return (SweetLocks) BukkitPlugin.getInstance();
    }

    private final List<String> disableWorlds = new ArrayList<>();
    private final PlatformScheduler platform;
    private ConfigUpdater configUpdater;
    public SweetLocks() throws Exception {
        super(options()
                .bungee(false)
                .adventure(true)
                .database(false)
                .reconnectDatabaseWhenReloadConfig(false)
                .economy(EnumEconomy.VAULT, true)
                .scanIgnore("top.mrxiaom.sweet.locks.libs")
        );
        FoliaLibScheduler scheduler = new FoliaLibScheduler(this);
        this.scheduler = scheduler;
        this.platform = scheduler.getFoliaLib().getScheduler();

        info("正在检查依赖库状态");
        File librariesDir = ClassLoaderWrapper.isSupportLibraryLoader
                ? new File("libraries")
                : new File(this.getDataFolder(), "libraries");
        DefaultLibraryResolver resolver = new DefaultLibraryResolver(getLogger(), librariesDir);

        resolver.addLibrary(BuildConstants.LIBRARIES);

        List<URL> libraries = resolver.doResolve();
        info("正在添加 " + libraries.size() + " 个依赖库到类加载器");
        for (URL library : libraries) {
            this.classLoader.addURL(library);
        }
    }

    @Override
    protected @NotNull ClassLoaderWrapper initClassLoader(URLClassLoader classLoader) {
        return ClassLoaderWrapper.isSupportLibraryLoader
                ? new ClassLoaderWrapper(ClassLoaderWrapper.findLibraryLoader(classLoader))
                : new ClassLoaderWrapper(classLoader);
    }

    @NotNull
    public IEconomy getEconomy() {
        return options.economy();
    }

    @NotNull
    public PlatformScheduler getPlatform() {
        return this.platform;
    }

    public boolean isInDisabledWorld(BlockEvent event) {
        return isInDisabledWorld(event.getBlock());
    }

    public boolean isInDisabledWorld(Block block) {
        return isDisabledWorld(block.getWorld());
    }

    public boolean isDisabledWorld(World world) {
        return disableWorlds.contains(world.getName());
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
    protected void beforeEnable() {
        LanguageManager.inst()
                .setLangFile("messages.yml")
                .register(Messages.class, Messages::holder);
        configUpdater = ConfigUpdater.create(this, "config.yml")
                .fullMatch("disable-worlds")
                .fullMatch("door-blocks")
                .prefixMatch("view-container.")
                .prefixMatch("create.")
                .fullMatch("groups")
                .fullMatch("lock-sign")
                .prefixMatch("money.")
                .prefixMatch("reach.")
                .prefixMatch("prevent-solid-target.")
                .prefixMatch("flags.");
    }

    @Override
    protected void beforeReloadConfig(FileConfiguration config) {
        if (ConfigUpdater.supportComments) {
            // 仅在支持设置配置文件注释的版本更新配置
            configUpdater.apply(config, new File(getDataFolder(), "config.yml"));
        }

        disableWorlds.clear();
        disableWorlds.addAll(config.getStringList("disable-worlds"));
    }

    @Override
    protected void afterEnable() {
        getLogger().info("SweetLocks 加载完毕");
    }
}
