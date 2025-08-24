package com.github.saintedlittle.command;

import com.github.saintedlittle.MainActivity;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public record CommandContext(MainActivity plugin, CommandSender sender, String label, List<String> args) {

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public Player asPlayer() {
        return (Player) sender;
    }
}