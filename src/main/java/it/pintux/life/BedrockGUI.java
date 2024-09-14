package it.pintux.life;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
        formMenuUtil = new FormMenuUtil(this);
    }

    @EventHandler
    public void onCmd(ServerCommandEvent event) {
        if (!(event.getSender() instanceof Player)) return;
        Player player = (Player) event.getSender();
        String command = event.getCommand();
        formMenuUtil.getFormMenus().forEach((key, formMenu) -> {
            if (formMenu.getFormCommand() != null) {
                if (command.equalsIgnoreCase(formMenu.getFormCommand())) {
                    event.setCancelled(true);
                    formMenuUtil.openForm(player, key);
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerPreprocessCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!FloodgateUtil.isFloodgate(player)) {
            return;
        }
        formMenuUtil.getFormMenus().forEach((key, formMenu) -> {
            if (formMenu.getFormCommand() != null) {
                if (event.getMessage().equalsIgnoreCase(formMenu.getFormCommand())) {
                    event.setCancelled(true);
                    formMenuUtil.openForm(player, key);
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
