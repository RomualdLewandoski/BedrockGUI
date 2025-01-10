package it.pintux.life.bungee;

import it.pintux.life.bungee.utils.BungeePlayer;
import it.pintux.life.common.FloodgateUtil;
import it.pintux.life.common.utils.FormPlayer;
import it.pintux.life.common.utils.MessageData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BedrockCommand extends Command implements TabExecutor {

    private final BedrockGUI plugin;

    public BedrockCommand(BedrockGUI plugin, String name) {
        super(name, "", "bguiproxy");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if (strings.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /bguiproxy reload");
            sender.sendMessage(ChatColor.RED + "Usage: /bguiproxy open <menu_name>");
            sender.sendMessage(ChatColor.RED + "Usage: /bguiproxy openfor <player> <menu_name> [arguments]");
            return;
        }

        String arg = strings[0];

        if (arg.equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("bedrockgui.admin")) {
                sender.sendMessage(plugin.getMessageData().getValue(MessageData.NO_PEX, null, null));
                return;
            }

            plugin.reloadData();
            sender.sendMessage(ChatColor.GREEN + "Reloaded BedrockGUI!");
            return;
        }

        if (arg.equalsIgnoreCase("open")) {
            if (sender instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) sender;
                if (strings.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /bguiproxy open <menu_name> [arguments]");
                    return;
                }

                if (!FloodgateUtil.isFloodgate(player.getUniqueId())) {
                    sender.sendMessage(plugin.getMessageData().getValue(MessageData.MENU_NOJAVA, null, null));
                    return;
                }

                String menuName = strings[1];
                String[] menuArgs = Arrays.copyOfRange(strings, 2, strings.length);
                if (!plugin.getFormMenuUtil().checkServerRequirement(player.hasPermission("bedrockgui.bypass"), player.getServer().getInfo().getName(), menuName)) {
                    sender.sendMessage(plugin.getMessageData().getValue(MessageData.MENU_NOPEX, null, null));
                    return;
                }
                FormPlayer formPlayer = new BungeePlayer(player);
                plugin.getFormMenuUtil().openForm(formPlayer, menuName, menuArgs);
            }
            return;
        }

        if (arg.equalsIgnoreCase("openfor")) {
            if (!sender.hasPermission("bedrockgui.admin")) {
                sender.sendMessage(plugin.getMessageData().getValue(MessageData.NO_PEX, null, null));
                return;
            }
            if (strings.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /bguiproxy openfor <player> <menu_name> [arguments]");
                return;
            }

            String playerName = strings[1];
            String menuName = strings[2];
            String[] menuArgs = Arrays.copyOfRange(strings, 3, strings.length);

            ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(playerName);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + playerName + "' is not online.");
                return;
            }

            if (!FloodgateUtil.isFloodgate(targetPlayer.getUniqueId())) {
                sender.sendMessage(plugin.getMessageData().getValue(MessageData.MENU_NOJAVA, null, null));
                return;
            }
            if (!plugin.getFormMenuUtil().checkServerRequirement(targetPlayer.hasPermission("bedrockgui.bypass"), targetPlayer.getServer().getInfo().getName(), menuName)) {
                sender.sendMessage(plugin.getMessageData().getValue(MessageData.MENU_NOPEX, null, null));
                return;
            }
            FormPlayer formPlayer = new BungeePlayer(targetPlayer);
            plugin.getFormMenuUtil().openForm(formPlayer, menuName, menuArgs);
            sender.sendMessage(ChatColor.GREEN + "Opened menu '" + menuName + "' for player '" + playerName + "'.");
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            return new ArrayList<>();
        }

        if (!commandSender.hasPermission("bedrockgui.admin")) {
            List<String> commands = new ArrayList<>();
            commands.add("open");
            return commands;
        }

        if (strings.length == 1) {
            List<String> commands = new ArrayList<>();
            commands.add("reload");
            commands.add("open");
            return commands;
        }
        if (strings.length == 2 && strings[0].equalsIgnoreCase("open")) {
            return Stream.of(plugin.getFormMenuUtil().getFormMenus().keySet())
                    .flatMap(Set::stream)
                    .map(String::toLowerCase)
                    .filter(c -> c.startsWith(strings[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
