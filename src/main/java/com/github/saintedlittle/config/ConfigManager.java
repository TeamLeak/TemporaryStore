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

    // Первая страница (как было)
    private List<ShopItem> items = Collections.emptyList();
    // Вторая страница
    private List<ShopItem> secondPageItems = Collections.emptyList();

    // Кэши готовых ItemStack (по ключу "type:id")
    private final Map<String, ItemStack> cache = new HashMap<>();             // для первой страницы
    private final Map<String, ItemStack> cacheSecondPage = new HashMap<>();   // для второй страницы

    public ConfigManager(MainActivity plugin) {
        this.plugin = plugin;
        reload(); // начальная загрузка
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        this.menuTitle = cfg.getString("shop.menu.title", "&aShop");
        this.menuDescription = cfg.getString("shop.menu.description", "&7Buy awesome items");

        // --- Первая страница ---
        this.items = cfg.getMapList("shop.items").stream()
                .map(e -> {
                    String type = String.valueOf(e.get("type"));
                    String id = String.valueOf(e.get("id"));
                    double price = toDouble(e.get("price"));
                    return new ShopItem(type, id, price);
                })
                .collect(Collectors.toList());

        // --- Вторая страница ---
        this.secondPageItems = cfg.getMapList("second_page.items").stream()
                .map(e -> {
                    String type = String.valueOf(e.get("type"));
                    String id = String.valueOf(e.get("id"));
                    double price = toDouble(e.get("price"));
                    return new ShopItem(type, id, price);
                })
                .collect(Collectors.toList());

        // очищаем кэши — пересоберём по запросу
        cache.clear();
        cacheSecondPage.clear();

        plugin.getLogger().info("Loaded " + items.size() + " shop item(s) on page 1.");
        plugin.getLogger().info("Loaded " + secondPageItems.size() + " shop item(s) on page 2.");
    }

    private static double toDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception ignored) { }
        return 0.0;
    }

    public String menuTitle() { return menuTitle; }
    public String menuDescription() { return menuDescription; }

    // Первая страница
    public List<ShopItem> items() { return items; }
    // Вторая страница
    public List<ShopItem> secondPageItems() { return secondPageItems; }

    /**
     * Собирает ItemStack через MMOItems с кэшем (первая страница).
     */
    public ItemStack resolveItem(ShopItem spec) {
        return resolveFromCache(spec, cache, "page1");
    }

    /**
     * Собирает ItemStack через MMOItems с кэшем (вторая страница).
     */
    public ItemStack resolveSecondPageItem(ShopItem spec) {
        return resolveFromCache(spec, cacheSecondPage, "page2");
    }

    // --- Внутренний общий резолвер для избежания дублирования кода ---
    private ItemStack resolveFromCache(ShopItem spec, Map<String, ItemStack> localCache, String pageTagForLogs) {
        String key = spec.type() + ":" + spec.id();
        ItemStack cached = localCache.get(key);
        if (cached != null) return cached.clone();

        MMOItem mm = MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get(spec.type()), spec.id());
        if (mm == null) {
            plugin.getLogger().warning("MMOItem not found (" + pageTagForLogs + "): " + key);
            return null;
        }
        ItemStack built = mm.newBuilder().build();

        if (built != null) {
            localCache.put(key, built.clone());
        }
        return built;
    }
}
