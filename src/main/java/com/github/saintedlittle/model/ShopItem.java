package com.github.saintedlittle.model;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record ShopItem(String type, String id, double price) {

    public ItemStack toItemStack(NamespacedKey keyPrice) {
        MMOItem mmo = MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get(type), id);
        if (mmo == null) return null;
        ItemStack item = mmo.newBuilder().build();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // PDC — цена
            meta.getPersistentDataContainer().set(keyPrice, PersistentDataType.DOUBLE, price);

            // lore
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(ChatColor.GOLD + "Цена: " + ChatColor.YELLOW + price + " глаз Пу");
            meta.setLore(lore);

            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull String toString() {
        return "ShopItem{" + type + ":" + id + ", price=" + price + "}";
    }
}
