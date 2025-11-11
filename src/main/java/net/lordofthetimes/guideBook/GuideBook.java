package net.lordofthetimes.guideBook;

import org.bukkit.plugin.java.JavaPlugin;

public final class GuideBook extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.getCommand("howto").setExecutor(new HowToCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
