package com.github.saintedlittle.i18n;

import com.github.saintedlittle.MainActivity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class Messages {

    private final MainActivity plugin;
    private YamlConfiguration bundle;

    // простейший кэш строк
    private final Map<String, String> cache = new HashMap<>();

    public Messages(MainActivity plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        // читаем локаль из config.yml
        String locale = plugin.getConfig().getString("locale", "en");

        File langFile = new File(plugin.getDataFolder(), "lang/messages_" + locale + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Locale file not found for '" + locale + "', fallback to 'en'.");
            langFile = new File(plugin.getDataFolder(), "lang/messages_en.yml");
        }

        this.bundle = YamlConfiguration.loadConfiguration(langFile);
        this.cache.clear();
        plugin.getLogger().info("Messages loaded: locale=" + locale);
    }

    public String get(String key) {
        if (cache.containsKey(key)) return cache.get(key);
        String raw = bundle.getString(key, key);

        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
        String colored = LegacyComponentSerializer.legacySection().serialize(component);

        cache.put(key, colored);
        return colored;
    }

    public String get(String key, Map<String, String> placeholders) {
        String s = get(key);
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                s = s.replace("%" + e.getKey() + "%", e.getValue());
            }
        }
        return s;
    }
}