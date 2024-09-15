package it.pintux.life;

import it.pintux.life.utils.FloodgateUtil;
import it.pintux.life.utils.FormMenuUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Map;

public final class BedrockGUI extends JavaPlugin implements Listener {

    private FormMenuUtil formMenuUtil;
    private boolean isPlaceholderAPI;

    @Override
    public void onEnable() {
        getCommand("bedrockgui").setExecutor(new BedrockCommand(this));
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        reloadData();
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            isPlaceholderAPI = true;
        }
    }

    @Override
    public void onDisable() {
    }

    public void reloadData() {
        reloadConfig();
        this.saveResource("messages.yml", false);
        new MessageData(this, "messages.yml");
        formMenuUtil = new FormMenuUtil(this);
    }

    @EventHandler
    public void onCmd(ServerCommandEvent event) {
        if (!(event.getSender() instanceof Player)) return;
        Player player = (Player) event.getSender();
        String command = event.getCommand();

        formMenuUtil.getFormMenus().forEach((key, formMenu) -> {
            if (formMenu.getFormCommand() != null && command.startsWith(formMenu.getFormCommand())) {
                event.setCancelled(true);

                String[] parts = command.split(" ");
                String[] args = Arrays.copyOfRange(parts, 1, parts.length);

                formMenuUtil.openForm(player, key, args);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerPreprocessCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!FloodgateUtil.isFloodgate(player)) {
            return;
        }
        String message = event.getMessage();

        String commandWithoutSlash = message.substring(1).toLowerCase();

        String[] parts = commandWithoutSlash.split(" ");
        String commandName = parts[0];
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        formMenuUtil.getFormMenus().forEach((key, formMenu) -> {
            String formCommand = formMenu.getFormCommand();

            if (formCommand != null) {
                String[] formCommandParts = formCommand.split(" ");
                String baseCommand = formCommandParts[0];

                if (commandName.equalsIgnoreCase(baseCommand)) {
                    int requiredArgs = formCommandParts.length - 1;
                    if (args.length >= requiredArgs) {
                        event.setCancelled(true);

                        formMenuUtil.openForm(player, key, args);
                    } else {
                        player.sendMessage(MessageData.getValue(MessageData.MENU_ARGS, Map.of("args", requiredArgs), null));
                    }
                }
            }
        });
    }


    public FormMenuUtil getFormMenuUtil() {
        return formMenuUtil;
    }

    public boolean isPlaceholderAPI() {
        return isPlaceholderAPI;
    }
}
