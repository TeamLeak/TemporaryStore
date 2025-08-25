package com.github.saintedlittle.listener;

import com.github.saintedlittle.MainActivity;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class ShopMenuListener implements Listener {
    private final NamespacedKey KEY_ACTION;
    private final NamespacedKey KEY_PRICE;
    private final MainActivity plugin;

    private static final String CURRENCY_IA_ID = "abyss:eye_of_poo";

    public ShopMenuListener(MainActivity plugin) {
        this.plugin = plugin;
        this.KEY_ACTION = new NamespacedKey(plugin, "action");
        this.KEY_PRICE = new NamespacedKey(plugin, "shop_price");
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(KEY_ACTION, PersistentDataType.STRING)) return;

        String action = pdc.get(KEY_ACTION, PersistentDataType.STRING);
        if (action == null) return;

        // Отменяем клик для всех действий магазина
        e.setCancelled(true);

        switch (action) {
            case "close" -> p.closeInventory();
            case "submenu" -> openSubmenu(p);
            case "buy" -> buyItem(pdc, p, item);
            default -> { /* no-op */ }
        }
    }

    private void buyItem(PersistentDataContainer pdc, Player p, ItemStack item) {
        Double priceD = pdc.get(KEY_PRICE, PersistentDataType.DOUBLE);
        if (priceD == null) {
            p.sendMessage(ChatColor.RED + "Ошибка: цена не указана.");
            return;
        }

        int price = (int) Math.round(priceD);
        if (price <= 0) {
            p.sendMessage(ChatColor.RED + "Ошибка: цена указана некорректно.");
            return;
        }

        // Проверяем наличие валюты
        if (!hasCurrency(p, price)) {
            p.sendMessage(ChatColor.RED + "У вас недостаточно глаз Пу! Требуется: " + price);
            return;
        }

        // Создаем награду (копия без метаданных магазина)
        ItemStack reward = item.clone();
        ItemMeta rMeta = reward.getItemMeta();
        if (rMeta != null) {
            PersistentDataContainer rPdc = rMeta.getPersistentDataContainer();
            rPdc.remove(KEY_PRICE);
            rPdc.remove(KEY_ACTION);

            // Чистим строку с "Цена:"
            if (rMeta.hasLore()) {
                List<String> lore = new ArrayList<>(Objects.requireNonNull(rMeta.getLore()));
                lore.removeIf(line -> line != null && ChatColor.stripColor(line).toLowerCase().startsWith("цена:"));
                rMeta.setLore(lore);
            }

            reward.setItemMeta(rMeta);
        }

        // Забираем валюту
        if (!takeCurrency(p, price)) {
            p.sendMessage(ChatColor.RED + "Ошибка при списании валюты!");
            return;
        }

        // Выдаем награду
        var leftover = p.getInventory().addItem(reward);
        if (!leftover.isEmpty()) {
            leftover.values().forEach(st -> p.getWorld().dropItemNaturally(p.getLocation(), st));
        }

        // Сообщение об успешной покупке
        String name = (reward.getItemMeta() != null && reward.getItemMeta().hasDisplayName())
                ? reward.getItemMeta().getDisplayName()
                : ChatColor.YELLOW + "товар";
        p.sendMessage(ChatColor.GREEN + "Вы купили " + name
                + ChatColor.GRAY + " за " + ChatColor.GOLD + price + ChatColor.GRAY + " " + ChatColor.YELLOW + "глаз Пу");
    }

    /**
     * Проверяет, есть ли у игрока достаточно валюты
     */
    private boolean hasCurrency(Player player, int amount) {
        return countCurrency(player) >= amount;
    }

    /**
     * Подсчитывает количество валюты у игрока
     */
    private int countCurrency(Player player) {
        int total = 0;
        Inventory inv = player.getInventory();

        // Получаем эталонный ItemStack валюты
        ItemStack currencyStack = getCurrencyItemStack();
        if (currencyStack == null) {
            plugin.getLogger().warning("Не удалось получить ItemStack валюты: " + CURRENCY_IA_ID);
            return 0;
        }

        for (ItemStack stack : inv.getContents()) {
            if (stack != null && isCurrencyItem(stack, currencyStack)) {
                total += stack.getAmount();
            }
        }

        return total;
    }

    /**
     * Списывает указанное количество валюты у игрока
     */
    private boolean takeCurrency(Player player, int amount) {
        if (!hasCurrency(player, amount)) return false;

        Inventory inv = player.getInventory();
        ItemStack currencyStack = getCurrencyItemStack();
        if (currencyStack == null) return false;

        int remaining = amount;

        for (int i = 0; i < inv.getSize() && remaining > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack != null && isCurrencyItem(stack, currencyStack)) {
                int stackAmount = stack.getAmount();

                if (stackAmount <= remaining) {
                    // Забираем весь стак
                    remaining -= stackAmount;
                    inv.setItem(i, null);
                } else {
                    // Забираем часть стака
                    stack.setAmount(stackAmount - remaining);
                    remaining = 0;
                }
            }
        }

        return remaining == 0;
    }

    /**
     * Получает ItemStack валюты
     */
    private ItemStack getCurrencyItemStack() {
        CustomStack cs = CustomStack.getInstance(CURRENCY_IA_ID);
        if (cs != null) {
            return cs.getItemStack();
        }

        return null;
    }

    /**
     * Проверяет, является ли предмет валютой
     */
    private boolean isCurrencyItem(ItemStack item, ItemStack currencyTemplate) {
        if (item == null || currencyTemplate == null) return false;

        // Для ItemsAdder предметов
        CustomStack itemCS = CustomStack.byItemStack(item);
        CustomStack templateCS = CustomStack.byItemStack(currencyTemplate);

        if (itemCS != null && templateCS != null) {
            return itemCS.getNamespacedID().equals(templateCS.getNamespacedID());
        }

        // Для обычных предметов
        return item.getType() == currencyTemplate.getType() &&
                Objects.equals(item.getItemMeta(), currencyTemplate.getItemMeta());
    }

    private void openSubmenu(Player p) {
        // Демо-подменю
        Inventory sub = Bukkit.createInventory(p, 27, ChatColor.DARK_PURPLE + "Abyss | Submenu");
        p.openInventory(sub);
    }
}