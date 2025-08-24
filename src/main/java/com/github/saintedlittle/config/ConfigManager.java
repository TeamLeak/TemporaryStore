package com.github.saintedlittle.config;

import com.github.saintedlittle.MainActivity;
import com.github.saintedlittle.model.ShopItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public final class ConfigManager {

    private final MainActivity plugin;

    private String menuTitle;
    private String menuDescription;
    private List<ShopItem> items = Collections.emptyList();

    // кэш готовых ItemStack (по ключу type:id)
    private final Map<String, ItemStack> cache = new HashMap<>();

    public ConfigManager(MainActivity plugin) {
        this.plugin = plugin;
        reload(); // начальная загрузка
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        this.menuTitle = cfg.getString("shop.menu.title", "&aShop");
        this.menuDescription = cfg.getString("shop.menu.description", "&7Buy awesome items");

        this.items = cfg.getMapList("shop.items").stream()
                .map(e -> {
                    String type = String.valueOf(e.get("type"));
                    String id = String.valueOf(e.get("id"));
                    double price = toDouble(e.get("price"));
                    return new ShopItem(type, id, price);
                })
                .collect(Collectors.toList());

        // очищаем кэш, пересоберём по запросу
        cache.clear();
        plugin.getLogger().info("Loaded " + items.size() + " shop item(s).");
    }

    private static double toDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception ignored) { }
        return 0.0;
    }

    public String menuTitle() { return menuTitle; }
    public String menuDescription() { return menuDescription; }
    public List<ShopItem> items() { return items; }

    /**
     * Собирает ItemStack через MMOItems с кэшем, чтобы не строить повторно.
     */
    public ItemStack resolveItem(ShopItem spec) {
        String key = spec.type() + ":" + spec.id();
        ItemStack cached = cache.get(key);
        if (cached != null) return cached.clone();

        MMOItem mm = MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get(spec.type()), spec.id());
        if (mm == null) {
            plugin.getLogger().warning("MMOItem not found: " + key);
            return null;
        }
        ItemStack built = mm.newBuilder().build();

        if (built != null) {
            cache.put(key, built.clone());
        }
        return built;
    }
}