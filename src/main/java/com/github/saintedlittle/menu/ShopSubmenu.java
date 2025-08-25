package com.github.saintedlittle.menu;

import com.github.saintedlittle.MainActivity;
import com.github.saintedlittle.config.ConfigManager;
import com.github.saintedlittle.model.ShopItem;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class ShopSubmenu {

    private static final String TITLE = "";
    private static final int SIZE = 36;
    private static final int OFFSET_X = 70;
    private static final int OFFSET_Y = -48;
    private static final String TEXTURE_ID = "abyss:shopmenu_second";

    private final MainActivity plugin;
    private final ConfigManager configManager;
    private final NamespacedKey KEY_ACTION;
    private final NamespacedKey KEY_PRICE;

    public ShopSubmenu(MainActivity activity) {
        this.plugin = activity;
        this.configManager = activity.configManager();
        this.KEY_ACTION = new NamespacedKey(activity, "action");
        this.KEY_PRICE  = new NamespacedKey(activity, "shop_price");
    }

    public void open(Player player) {
        TexturedInventoryWrapper tiw = new TexturedInventoryWrapper(
                null,
                SIZE,
                TITLE,
                OFFSET_X,
                OFFSET_Y,
                new FontImageWrapper(TEXTURE_ID)
        );

        Inventory inv = tiw.getInternal();

        // 1) Кнопки закрытия в 30..32
        setRange(inv, 30, 32, makeTransparentButton("close"));

        // 2) Товары во второй странице: только в слотах 10..16
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        List<ShopItem> items = configManager.secondPageItems();

        int i = 0;
        for (int slot : slots) {
            if (i >= items.size()) break;

            ShopItem spec = items.get(i++);
            ItemStack stack = spec.toItemStack(KEY_PRICE); // ShopItem сам добавит цену в PDC и lore
            if (stack == null) {
                plugin.getLogger().warning("MMOItem not found for second page: " + spec);
                continue;
            }

            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, "buy");
                stack.setItemMeta(meta);
            }

            inv.setItem(slot, stack);
        }

        tiw.showInventory(player);
    }

    /** Прозрачная «кнопка» с тегом действия (через ItemsAdder blank:menu_blank, как в ShopCommand). */
    private ItemStack makeTransparentButton(String action) {
        CustomStack cs = CustomStack.getInstance("blank:menu_blank");
        ItemStack it;
        if (cs != null) {
            it = cs.getItemStack().clone();
        } else {
            // fallback, если IA-айтем отсутствует
            it = new ItemStack(Material.BARRIER);
            ItemMeta m = it.getItemMeta();
            if (m != null) {
                m.setDisplayName("§cMissing IA item: blank:menu_blank");
                it.setItemMeta(m);
            }
            return it;
        }

        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, action);
            it.setItemMeta(meta);
        }
        return it;
    }

    private static void setRange(Inventory inv, int from, int to, ItemStack item) {
        if (item == null) return;
        for (int i = from; i <= to && i < inv.getSize(); i++) {
            inv.setItem(i, item.clone());
        }
    }
}
