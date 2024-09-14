package it.pintux.life;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class BedrockGUI extends JavaPlugin implements Listener {

    private FormMenuUtil formMenuUtil;

    @Override
    public void onEnable() {
        getCommand("bedrockgui").setExecutor(new BedrockCommand(this));
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        reloadData();
    }

    @Override
    public void onDisable() {
    }

    public void reloadData() {
        saveConfig();
        reloadConfig();
        formMenuUtil = new FormMenuUtil(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreprocessCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!FloodgateUtil.isFloodgate(player)) {
            return;
        }
        formMenuUtil.getFormMenus().forEach((key, formMenu) -> {
            if (formMenu.formCommand() != null) {
                if (event.getMessage().equalsIgnoreCase(formMenu.formCommand())) {
                    event.setCancelled(true);
                    formMenuUtil.openForm(player, key);
                }
            }
        });
    }

    public FormMenuUtil getFormMenuUtil() {
        return formMenuUtil;
    }
}
