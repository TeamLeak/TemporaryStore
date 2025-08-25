package com.github.saintedlittle.listener;

import com.github.saintedlittle.MainActivity;
import com.github.saintedlittle.shop.ShopPurchaseService;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class ShopMenuListener implements Listener {
    private final NamespacedKey KEY_ACTION;
    private final ShopPurchaseService purchaseService;

    public ShopMenuListener(MainActivity plugin, ShopPurchaseService purchaseService) {
        this.KEY_ACTION = new NamespacedKey(plugin, "action");
        this.purchaseService = purchaseService;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory() == null) return;

        String title = e.getView().getTitle();
        if (!title.contains("Abyss")) return; // определи свой GUI надёжнее, если нужно

        e.setCancelled(true); // Ничего переносить нельзя — только действия по клику

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();

        // Кнопки (close / submenu)
        String action = meta.getPersistentDataContainer().get(KEY_ACTION, PersistentDataType.STRING);
        if (action != null) {
            switch (action) {
                case "close" -> p.closeInventory();
                case "submenu" -> p.sendMessage(ChatColor.LIGHT_PURPLE + "Открывается другое меню...");
            }
            return;
        }

        // Покупка товара
        purchaseService.tryPurchase(p, item);
    }
}
