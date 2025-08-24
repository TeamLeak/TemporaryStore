package com.github.saintedlittle.command;

import com.github.saintedlittle.MainActivity;
import com.github.saintedlittle.command.annotations.CommandSpec;
import com.github.saintedlittle.command.annotations.PlayerOnly;
import com.github.saintedlittle.command.annotations.SubcommandSpec;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class AnnotatedCommand implements CommandExecutor, TabCompleter {

    protected final MainActivity plugin;
    private final CommandSpec spec;
    private final boolean playerOnlyAtClass;
    private final Map<String, Method> subHandlers = new HashMap<>();
    private final Map<String, SubcommandSpec> subMeta = new HashMap<>();
    private final Map<String, List<String>> subAliases = new HashMap<>();

    // для конвертации §-строк из Messages в Component
    private static final LegacyComponentSerializer SEC = LegacyComponentSerializer.legacySection();

    protected AnnotatedCommand(MainActivity plugin) {
        this.plugin = plugin;
        this.spec = this.getClass().getAnnotation(CommandSpec.class);
        if (spec == null) throw new IllegalStateException("Missing @CommandSpec on " + getClass().getName());
        this.playerOnlyAtClass = this.getClass().isAnnotationPresent(PlayerOnly.class);
        indexSubcommands();
    }

    public CommandSpec meta() { return spec; }

    private void indexSubcommands() {
        for (Method m : getClass().getDeclaredMethods()) {
            SubcommandSpec sub = m.getAnnotation(SubcommandSpec.class);
            if (sub == null) continue;
            m.setAccessible(true);
            String key = sub.name().toLowerCase(Locale.ROOT);
            subHandlers.put(key, m);
            subMeta.put(key, sub);
            for (String a : sub.aliases()) {
                subAliases
                        .computeIfAbsent(a.toLowerCase(Locale.ROOT), k -> new ArrayList<>())
                        .add(key);
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String @NotNull [] rawArgs) {

        List<String> args = Arrays.asList(rawArgs);
        CommandContext ctx = new CommandContext(plugin, sender, label, args);

        // class-level player-only
        if (playerOnlyAtClass && !(sender instanceof Player)) {
            send(sender, plugin.messages().get("player-only"));
            return true;
        }
        // root permission
        if (!spec.permission().isEmpty() && !sender.hasPermission(spec.permission())) {
            send(sender, plugin.messages().get("no-permission"));
            return true;
        }

        if (!args.isEmpty()) {
            String first = args.getFirst().toLowerCase(Locale.ROOT);
            // alias → main
            if (subAliases.containsKey(first)) {
                first = subAliases.get(first).getFirst();
            }
            Method handler = subHandlers.get(first);
            if (handler != null) {
                // sub-level player-only
                boolean subPlayerOnly = handler.isAnnotationPresent(PlayerOnly.class);
                if (subPlayerOnly && !(sender instanceof Player)) {
                    send(sender, plugin.messages().get("player-only"));
                    return true;
                }
                // sub permission (fallback на root)
                SubcommandSpec sm = subMeta.get(first);
                String perm = sm.permission().isEmpty() ? spec.permission() : sm.permission();
                if (!perm.isEmpty() && !sender.hasPermission(perm)) {
                    send(sender, plugin.messages().get("no-permission"));
                    return true;
                }
                // invoke with tail
                List<String> tail = args.subList(1, args.size());
                return invoke(handler, new CommandContext(plugin, sender, label, tail));
            }
        }

        // fallback → root
        return rootExecute(ctx);
    }

    private boolean invoke(Method m, CommandContext ctx) {
        try {
            Object res = m.getParameterCount() == 1
                    ? m.invoke(this, ctx)
                    : m.invoke(this);
            return !(res instanceof Boolean b) || b;
        } catch (Throwable t) { // ловим Throwable по твоей просьбе
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Command error in " + getClass().getSimpleName() + "." + m.getName(),
                    t
            );
            ctx.sender().sendMessage(Component.text("Command error. See console.", NamedTextColor.RED));
            return true;
        }
    }

    /** Поведение по умолчанию у корня (если саб не указан) */
    protected boolean rootExecute(CommandContext ctx) {
        String usage = spec.usage().isEmpty() ? "/" + spec.name() : spec.usage();
        ctx.sender().sendMessage(Component.text("Usage: " + usage, NamedTextColor.YELLOW));
        return true;
    }

    // TAB
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      String @NotNull [] rawArgs) {
        List<String> args = Arrays.asList(rawArgs);
        if (args.size() == 1) {
            String prefix = args.getFirst().toLowerCase(Locale.ROOT);
            Set<String> names = new HashSet<>(subHandlers.keySet());
            names.addAll(subAliases.keySet());
            return names.stream()
                    .filter(s -> s.startsWith(prefix))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /* ===== Helpers ===== */

    /** Отправляет строку с §-кодами как Component */
    private void send(CommandSender sender, String legacySectionText) {
        sender.sendMessage(SEC.deserialize(legacySectionText));
    }
}
