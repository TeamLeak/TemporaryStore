package com.github.saintedlittle.command.impl;

import com.github.saintedlittle.MainActivity;
import com.github.saintedlittle.command.AnnotatedCommand;
import com.github.saintedlittle.command.CommandContext;
import com.github.saintedlittle.command.annotations.CommandSpec;

@CommandSpec(
        name = "shopreload",
        description = "Reload shop config & messages",
        usage = "/shopreload",
        permission = "shop.reload"
)
public final class ShopReloadCommand extends AnnotatedCommand {

    public ShopReloadCommand(MainActivity plugin) {
        super(plugin);
    }

    @Override
    protected boolean rootExecute(CommandContext ctx) {
        boolean ok = plugin.hotReload();
        ctx.sender().sendMessage(plugin.messages().get(ok ? "reloaded-ok" : "reloaded-fail"));
        return true;
    }
}