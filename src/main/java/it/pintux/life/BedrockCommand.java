package it.pintux.life;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BedrockCommand implements CommandExecutor, TabCompleter {

    private final BedrockGUI plugin;

    public BedrockCommand(BedrockGUI plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /bgui reload");
            sender.sendMessage(ChatColor.RED + "Usage: /bgui open <menu_name>");
            return true;
        }
        String arg = args[0];
        if (arg.equalsIgnoreCase("reload")) {
            if (!player.hasPermission("bedrockgui.admin")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return true;
            }
            plugin.reloadData();
            sender.sendMessage(ChatColor.GREEN + "Reloaded BedrockGUI!");
            return true;
        }
        if (arg.equalsIgnoreCase("open")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /bgui open <menu_name>");
                return true;
            }

            if (!FloodgateUtil.isFloodgate(player)) {
                sender.sendMessage(ChatColor.RED + "You are not a bedrock player!");
                return true;
            }
            String menuName = args[1];
            plugin.getFormMenuUtil().openForm(player, menuName);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        if (!sender.hasPermission("bedrockgui.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> commands = new ArrayList<>();
            commands.add("reload");
            commands.add("open");
            return commands;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("open")) {
            return Stream.of(plugin.getFormMenuUtil().getFormMenus().keySet())
                    .flatMap(Set::stream)
                    .map(String::toLowerCase)
                    .filter(c -> c.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
