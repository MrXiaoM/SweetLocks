package top.mrxiaom.sweet.locks.func;
        
import top.mrxiaom.sweet.locks.SweetLocks;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<SweetLocks> {
    public AbstractPluginHolder(SweetLocks plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(SweetLocks plugin, boolean register) {
        super(plugin, register);
    }
}
