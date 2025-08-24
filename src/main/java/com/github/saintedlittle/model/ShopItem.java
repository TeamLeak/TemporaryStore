package com.github.saintedlittle.model;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ShopItem(String type, String id, double price) {

    public ItemStack toItemStack() {
        MMOItem mmo = MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get(type), id);
        if (mmo == null) return null;
        return mmo.newBuilder().build();
    }

    @Override
    public @NotNull String toString() {
        return "ShopItem{" + type + ":" + id + ", price=" + price + "}";
    }
}
