package it.pintux.life.paper;

import it.pintux.life.common.FloodgateUtil;
import it.pintux.life.common.utils.FormPlayer;
import it.pintux.life.common.utils.MessageData;
import it.pintux.life.paper.utils.PaperPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
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
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /bgui reload");
            sender.sendMessage(ChatColor.RED + "Usage: /bgui open <menu_name>");
            sender.sendMessage(ChatColor.RED + "Usage: /bgui openfor <player> <menu_name> [arguments]");
            return true;
        }

        String arg = args[0];

        if (arg.equalsIgnoreCase("reload")) {
            if (sender instanceof Player && !sender.hasPermission("bedrockgui.admin")) {
                sender.sendMessage(plugin.getMessageData().getValue(MessageData.NO_PEX, null, null));
                return true;
            }

            plugin.reloadData();
            sender.sendMessage(ChatColor.GREEN + "Reloaded BedrockGUI!");
            return true;
        }

        if (arg.equalsIgnoreCase("open")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /bgui open <menu_name> [arguments]");
                    return true;
                }

                if (!FloodgateUtil.isFloodgate(player.getUniqueId())) {
                    sender.sendMessage(plugin.getMessageData().getValue(MessageData.MENU_NOJAVA, null, null));
                    return true;
                }

                String menuName = args[1];
                String[] menuArgs = Arrays.copyOfRange(args, 2, args.length);
                FormPlayer formPlayer = new PaperPlayer(player);
                plugin.getFormMenuUtil().openForm(formPlayer, menuName, menuArgs);
            }
            return true;
        }

        if (arg.equalsIgnoreCase("openfor")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /bgui openfor <player> <menu_name> [arguments]");
                return true;
            }

            String playerName = args[1];
            String menuName = args[2];
            String[] menuArgs = Arrays.copyOfRange(args, 3, args.length);

            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + playerName + "' is not online.");
                return true;
            }

            if (!FloodgateUtil.isFloodgate(targetPlayer.getUniqueId())) {
                sender.sendMessage(plugin.getMessageData().getValue(MessageData.MENU_NOJAVA, null, null));
                return true;
            }

            FormPlayer formPlayer = new PaperPlayer(targetPlayer);
            plugin.getFormMenuUtil().openForm(formPlayer, menuName, menuArgs);
            sender.sendMessage(ChatColor.GREEN + "Opened menu '" + menuName + "' for player '" + playerName + "'.");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        if (!sender.hasPermission("bedrockgui.admin")) {
            List<String> commands = new ArrayList<>();
            commands.add("open");
            return commands;
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
