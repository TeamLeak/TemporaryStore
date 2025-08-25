package com.github.saintedlittle;

import com.github.saintedlittle.command.AnnotatedCommand;
import com.github.saintedlittle.command.CommandManager;
import com.github.saintedlittle.command.annotations.CommandSpec;
import com.github.saintedlittle.config.ConfigManager;
import com.github.saintedlittle.i18n.Messages;
import com.github.saintedlittle.listener.ShopMenuListener;
import com.github.saintedlittle.shop.ShopPurchaseService;
import io.github.classgraph.ClassGraph;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class MainActivity extends JavaPlugin {

    private ConfigManager configManager;
    private Messages messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("lang/messages_en.yml", false);
        saveResource("lang/messages_ru.yml", false);

        this.messages = new Messages(this);
        this.configManager = new ConfigManager(this);
        CommandManager commandManager = new CommandManager(this);

        try (var scan = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()              // <-- required for getClassesWithAnnotation(...)
                .acceptPackages("com.github.saintedlittle.command.impl")
                .scan()) {

            for (var ci : scan.getClassesWithAnnotation(CommandSpec.class.getName())) {
                Class<?> clazz = ci.loadClass();
                if (!AnnotatedCommand.class.isAssignableFrom(clazz)) continue;

                var ctor = clazz.getConstructor(MainActivity.class);
                AnnotatedCommand cmd = (AnnotatedCommand) ctor.newInstance(this);
                commandManager.register(cmd);
            }
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Failed to auto-register commands", t);
        }

        ShopPurchaseService purchaseService = new ShopPurchaseService(this);
        getServer().getPluginManager().registerEvents(new ShopMenuListener(this, purchaseService), this);

        getLogger().info("ShopPlugin enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ShopPlugin disabled.");
    }

    public ConfigManager configManager() { return configManager; }
    public Messages messages() { return messages; }

    public boolean hotReload() {
        try {
            messages.reload();
            configManager.reload();
            getLogger().info("Hot reload completed successfully.");
            return true;
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Hot reload failed", t);
            return false;
        }
    }
}
