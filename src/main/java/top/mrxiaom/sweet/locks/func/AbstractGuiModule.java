package top.mrxiaom.sweet.locks.func;

import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.locks.SweetLocks;

import java.io.File;

public abstract class AbstractGuiModule extends top.mrxiaom.pluginbase.func.AbstractGuiModule<SweetLocks> {
    public AbstractGuiModule(SweetLocks plugin, File file) {
        super(plugin, file);
    }
    public AbstractGuiModule(SweetLocks plugin, File file, @Nullable String mainIconsKey, @Nullable String otherIconsKey) {
        super(plugin, file, mainIconsKey, otherIconsKey);
    }
}
