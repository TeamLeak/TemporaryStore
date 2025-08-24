package com.github.saintedlittle.command.impl;

import com.github.saintedlittle.MainActivity;
import com.github.saintedlittle.command.AnnotatedCommand;
import com.github.saintedlittle.command.CommandContext;
import com.github.saintedlittle.command.annotations.CommandSpec;
import com.github.saintedlittle.command.annotations.SubcommandSpec;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@CommandSpec(
        name = "shop",
        description = "Main shop command",
        usage = "/shop <open|reload|about>",
        permission = "shop.use",
        aliases = {"market"}
)
public final class ShopCommand extends AnnotatedCommand {

    private static final LegacyComponentSerializer SEC = LegacyComponentSerializer.legacySection();

    public ShopCommand(MainActivity plugin) {
        super(plugin);
    }

    @Override
    protected boolean rootExecute(CommandContext ctx) {
        ctx.sender().sendMessage(Component.text("Usage: /shop <open|reload|about>", NamedTextColor.YELLOW));
        return true;
    }

    @SubcommandSpec(name = "about", description = "Show shop info")
    public boolean about(CommandContext ctx) {
        var cfg = plugin.configManager();

        String title = plugin.messages().get("menu-title",
                java.util.Map.of("title", cfg.menuTitle()));
        String desc  = plugin.messages().get("menu-description",
                java.util.Map.of("desc", cfg.menuDescription()));

        ctx.sender().sendMessage(SEC.deserialize(title));
        ctx.sender().sendMessage(SEC.deserialize(desc));
        ctx.sender().sendMessage(Component.text("Items loaded: " + cfg.items().size(), NamedTextColor.GRAY));
        return true;
    }

    @SubcommandSpec(name = "reload", permission = "shop.reload", description = "Reload config & messages")
    public boolean reload(CommandContext ctx) {
        boolean ok = plugin.hotReload();
        String msg = plugin.messages().get(ok ? "reloaded-ok" : "reloaded-fail");
        ctx.sender().sendMessage(SEC.deserialize(msg));
        return true;
    }

    @SubcommandSpec(name = "open", description = "Open shop GUI")
    public boolean open(CommandContext ctx) {
        ctx.sender().sendMessage(Component.text("TODO: open shop GUI here.", NamedTextColor.GREEN));
        return true;
    }
}
