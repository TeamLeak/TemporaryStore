package com.github.saintedlittle.shop;

import com.github.saintedlittle.MainActivity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class ShopPurchaseService {
    private final NamespacedKey KEY_PRICE;

    public ShopPurchaseService(MainActivity plugin) {
        this.KEY_PRICE = new NamespacedKey(plugin, "shop_price");
    }

    /**
     * Пытается купить предмет, если в PDC есть цена.
     * @return true — клик обработан (цена была), false — это не товар магазина.
     */
    public boolean tryPurchase(Player p, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) return false;

        ItemMeta meta = clicked.getItemMeta();
        Double price = meta.getPersistentDataContainer().get(KEY_PRICE, PersistentDataType.DOUBLE);
        if (price == null) return false; // не товар магазина

        // TODO: Доработай тут логику.

        // Выдаём копию без ценника в PDC (чтобы игрок не нёс «продаваемость» дальше)
        ItemStack reward = clicked.clone();
        ItemMeta rMeta = reward.getItemMeta();
        if (rMeta != null) {
            rMeta.getPersistentDataContainer().remove(KEY_PRICE);
            reward.setItemMeta(rMeta);
        }

        p.getInventory().addItem(reward);

        Component nameComp = (reward.getItemMeta() != null && reward.getItemMeta().hasDisplayName())
                ? reward.displayName()
                : Component.text("товар", NamedTextColor.YELLOW);

        p.sendMessage(
                Component.text("Вы купили ", NamedTextColor.GREEN)
                        .append(nameComp)
                        .append(Component.text(" за ", NamedTextColor.GRAY))
                        .append(Component.text(price + " глаз Пу", NamedTextColor.GOLD))
        );

        return true;
    }
}
