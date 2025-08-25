package com.github.saintedlittle.command.impl;

import com.github.saintedlittle.MainActivity;
import com.github.saintedlittle.command.AnnotatedCommand;
import com.github.saintedlittle.command.CommandContext;
import com.github.saintedlittle.command.annotations.CommandSpec;
import com.github.saintedlittle.model.ShopItem;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

@CommandSpec(
        name = "abyssshop",
        description = "Open the Abyss shop menu",
        usage = "/abyssshop",
        permission = "shop.use",
        aliases = {"abyss"}
)
public final class ShopCommand extends AnnotatedCommand {

    private static final LegacyComponentSerializer AMP = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer SEC = LegacyComponentSerializer.legacySection();

    private final NamespacedKey KEY_ACTION;
    private final NamespacedKey KEY_PRICE;

    public ShopCommand(MainActivity plugin) {
        super(plugin);
        this.KEY_ACTION = new NamespacedKey(plugin, "action");
        this.KEY_PRICE = new NamespacedKey(plugin, "shop_price");
    }

    @Override
    protected boolean rootExecute(CommandContext ctx) {
        if (!(ctx.sender() instanceof Player player)) {
            String msg = plugin.messages().get("player-only");
            ctx.sender().sendMessage(SEC.deserialize(msg));
            return true;
        }

        var cfg = plugin.configManager();
        String rawTitle = cfg.menuTitle();
        Component title = AMP.deserialize(rawTitle);

        TexturedInventoryWrapper tiw = new TexturedInventoryWrapper(
                null,
                54,
                "Abyss",
                50,
                -48,
                new FontImageWrapper("abyss:shopmenu_main")
        );

        Inventory inv = tiw.getInternal();

        // 45-47: закрытие
        setRange(inv, 45, 47, makeTransparentButton("close"));

        // 51-53: открыть подменю
        setRange(inv, 51, 53, makeTransparentButton("submenu"));

        loadShopGrid(inv, cfg.items());

        tiw.showInventory(player);
        return true;
    }

    /** Прозрачная «кнопка» (GLASS_PANE) с PDC-меткой действия */
    private ItemStack makeTransparentButton(String action) {
        CustomStack cs = CustomStack.getInstance("blank:menu_blank");
        ItemStack it;
        if (cs != null) {
            it = cs.getItemStack().clone();
        } else {
            // fallback, если предмет не найден в IA
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
            // никаких displayName — берём как есть из IA
            // навешиваем только action-тег
            meta.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, action);
            it.setItemMeta(meta);
        }

        return it;
    }

    private static void setRange(Inventory inv, int from, int to, ItemStack item) {
        for (int i = from; i <= to; i++) {
            if (item != null) {
                inv.setItem(i, item.clone());
            }
        }
    }

    private void loadShopGrid(Inventory inv, List<ShopItem> shopItems) {
        int[] slots = {
                10,11,12,13,14,15,16,
                19,20,21,22,23,24,25,
                28,29,30,31,32,33,34
        };

        int i = 0;
        for (int slot : slots) {
            if (i >= shopItems.size()) break;
            ShopItem si = shopItems.get(i++);
            ItemStack st = si.toItemStack(KEY_PRICE);

            // Проверяем на null перед использованием
            if (st == null) {
                plugin.getLogger().warning("Не удалось создать ItemStack для " + si);
                continue;
            }

            ItemMeta meta = st.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, "buy");
                st.setItemMeta(meta);
                inv.setItem(slot, st);
            }
        }
    }
}