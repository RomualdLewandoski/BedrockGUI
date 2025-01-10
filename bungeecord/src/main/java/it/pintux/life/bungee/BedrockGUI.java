package it.pintux.life.bungee;

import it.pintux.life.bungee.utils.BungeeConfig;
import it.pintux.life.bungee.utils.BungeeMessageConfig;
import it.pintux.life.bungee.utils.BungeePlayer;
import it.pintux.life.common.FloodgateUtil;
import it.pintux.life.common.api.BedrockGuiAPI;
import it.pintux.life.common.form.FormMenuUtil;
import it.pintux.life.common.utils.FormPlayer;
import it.pintux.life.common.utils.MessageConfig;
import it.pintux.life.common.utils.MessageData;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

public class BedrockGUI extends Plugin implements Listener {

    private FormMenuUtil formMenuUtil;
    private MessageData messageData;
    private static BedrockGUI instance;
    private BedrockGuiAPI api;

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerCommand(this, new BedrockCommand(this, "bedrockguiproxy"));
        getProxy().getPluginManager().registerListener(this, this);
        try {
            makeConfig("config.yml");
            makeConfig("messages.yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        reloadData();
        new Metrics(this, 23364);
        instance = this;
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public void reloadData() {
        Configuration mainConfig;
        Configuration messageConfig;
        try {
            mainConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            messageConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "messages.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MessageConfig configHandler = new BungeeMessageConfig(messageConfig);
        messageData = new MessageData(configHandler);
        formMenuUtil = new FormMenuUtil(new BungeeConfig(mainConfig), messageData);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerPreprocessCommand(ChatEvent event) {
        if ((event.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        if (!(event.isCommand()) || !(event.isProxyCommand())) {
            return;
        }
        FormPlayer player = new BungeePlayer((ProxiedPlayer) event.getSender());

        if (!FloodgateUtil.isFloodgate(player.getUniqueId())) {
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
                        player.sendMessage(messageData.getValue(MessageData.MENU_ARGS, Map.of("args", requiredArgs), null));
                    }
                }
            }
        });
    }

    public void makeConfig(String config) throws IOException {
        if (!getDataFolder().exists()) {
            getLogger().info("Created config folder: " + getDataFolder().mkdir());
        }

        File configFile = new File(getDataFolder(), config);

        if (!configFile.exists()) {
            FileOutputStream outputStream = new FileOutputStream(configFile);
            InputStream in = getResourceAsStream(config);
            in.transferTo(outputStream);
        }
    }

    public static BedrockGUI getInstance() {
        return instance;
    }

    public FormMenuUtil getFormMenuUtil() {
        return formMenuUtil;
    }

    public MessageData getMessageData() {
        return messageData;
    }

    public BedrockGuiAPI getApi() {
        if (api == null) {
            api = new BedrockGuiAPI(formMenuUtil);
        }
        return api;
    }
}