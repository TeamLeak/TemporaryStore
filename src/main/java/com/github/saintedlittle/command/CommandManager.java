package com.github.saintedlittle.command;

import com.github.saintedlittle.MainActivity;
import com.github.saintedlittle.command.annotations.CommandSpec;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public final class CommandManager {
    private final MainActivity plugin;

    public CommandManager(MainActivity plugin) {
        this.plugin = plugin;
    }

    /**
     * Регистрирует AnnotatedCommand через Bukkit CommandMap.
     * Можно вообще не указывать команду в plugin.yml.
     */
    public void register(AnnotatedCommand cmd) {
        CommandSpec meta = cmd.meta();
        try {
            PluginCommand pc = createPluginCommand(meta.name(), plugin);
            if (!meta.description().isEmpty()) pc.setDescription(meta.description());
            if (!meta.usage().isEmpty()) pc.setUsage(meta.usage());
            if (meta.aliases().length > 0) pc.setAliases(java.util.Arrays.asList(meta.aliases()));
            if (!meta.permission().isEmpty()) pc.setPermission(meta.permission());
            pc.setExecutor(cmd);
            pc.setTabCompleter(cmd);

            CommandMap map = getCommandMap();
            map.register(plugin.getName(), pc);

            plugin.getLogger().info("Registered command /" + meta.name());
        } catch (Throwable t) {
            plugin.getLogger().log(
                    java.util.logging.Level.SEVERE,
                    "Failed to register command /" + meta.name(),
                    t
            );
        }
    }

    private static PluginCommand createPluginCommand(String name, Plugin plugin) throws Exception {
        Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        c.setAccessible(true);
        return c.newInstance(name, plugin);
    }

    private static CommandMap getCommandMap() throws Exception {
        Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        f.setAccessible(true);
        return (CommandMap) f.get(Bukkit.getServer());
    }
}