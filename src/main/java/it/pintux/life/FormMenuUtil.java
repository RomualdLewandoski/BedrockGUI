package it.pintux.life;

import it.pintux.life.form.FormButton;
import it.pintux.life.form.FormMenu;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormMenuUtil {

    private final Map<String, FormMenu> formMenus;
    private final BedrockGUI plugin;

    public FormMenuUtil(BedrockGUI plugin) {
        this.plugin = plugin;
        formMenus = new HashMap<>();
        loadFormMenus();
    }

    private void loadFormMenus() {
        FileConfiguration config = plugin.getConfig();
        for (String key : config.getConfigurationSection("menu").getKeys(false)) {
            String command = config.getString("menu." + key + ".command");
            String type = config.getString("menu." + key + ".type", "SIMPLE");
            String title = config.getString("menu." + key + ".title", "Unknown");
            String description = config.getString("menu." + key + ".description");
            List<FormButton> buttons = new ArrayList<>();
            for (String button : config.getConfigurationSection("menu." + key + ".buttons").getKeys(false)) {
                String text = config.getString("menu." + key + "." + button + ".text");
                String image = config.getString("menu." + key + "." + button + ".image");
                String onClick = config.getString("menu." + key + "." + button + ".onClick");
                buttons.add(new FormButton(text, image, onClick));
            }
            if (type.equalsIgnoreCase("MODAL")) {
                if (buttons.size() != 2) {
                    plugin.getLogger().severe("Modal's must only have 2 buttons! Please modify menu." + key);
                    continue;
                }
            }
            FormMenu menu = new FormMenu(command, title, description, type, buttons);
            formMenus.put(key.toLowerCase(), menu);
            plugin.getLogger().info("Loaded form menu: " + key + " type: " + type);
        }
    }

    public void openForm(Player player, String menuName) {
        FormMenu menu = formMenus.get(menuName.toLowerCase());

        String type = menu.formType();

        switch (type.toUpperCase()) {
            case "MODAL":
                openModalForm(player, menu);
                break;
            case "SIMPLE":
                openSimpleForm(player, menu);
                break;
            default:
                player.sendMessage("Unknown form type: " + type);
        }
    }

    private void openModalForm(Player player, FormMenu formMenu) {
        String title = PlaceholderAPI.setPlaceholders(player, formMenu.formTitle());
        String content = formMenu.formContent() != null ? PlaceholderAPI.setPlaceholders(player, formMenu.formContent()) : null;

        List<FormButton> buttons = formMenu.formButtons();
        FormButton b1 = buttons.get(0);
        FormButton b2 = buttons.get(1);

        ModalForm.Builder formBuilder = ModalForm.builder()
                .title(title);

        if (content != null) {
            formBuilder.content(content);
        }

        formBuilder
                .button1(PlaceholderAPI.setPlaceholders(player, b1.text()))
                .button2(PlaceholderAPI.setPlaceholders(player, b2.text()))
                .validResultHandler((formResponse, modalResponse) -> {
                    if (modalResponse.clickedButtonId() == 0) {
                        if (b1.onClick() != null) {
                            handleOnClick(player, b1.onClick());
                        }
                    } else {
                        if (b2.onClick() != null) {
                            handleOnClick(player, b2.onClick());
                        }
                    }
                })
                .build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), formBuilder);
    }

    private void openSimpleForm(Player player, FormMenu formMenu) {
        String title = PlaceholderAPI.setPlaceholders(player, formMenu.formTitle());
        String content = formMenu.formContent() != null ? PlaceholderAPI.setPlaceholders(player, formMenu.formContent()) : null;
        List<FormButton> buttons = formMenu.formButtons();

        SimpleForm.Builder formBuilder = SimpleForm.builder()
                .title(title);

        if (content != null) {
            formBuilder.content(content);
        }

        List<String> onClickActions = new ArrayList<>();

        buttons.forEach(formButton -> {
            String buttonText = PlaceholderAPI.setPlaceholders(player, formButton.text());
            if (formButton.image() != null) {
                formBuilder.button(buttonText, FormImage.Type.URL, formButton.image());
            } else {
                formBuilder.button(buttonText);
            }
            if (formButton.onClick() != null) {
                onClickActions.add(formButton.onClick());
            }
        });

        formBuilder.validResultHandler((form, response) -> {
            int clickedButtonId = response.clickedButtonId();
            String action = onClickActions.get(clickedButtonId);

            handleOnClick(player, action);
        });


        SimpleForm form = formBuilder.build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    private void handleOnClick(Player player, String onClickAction) {
        onClickAction = onClickAction.trim().replaceAll("\\s+", " ");

        String[] parts = onClickAction.split(" ", 2);

        if (parts.length != 2) {
            player.sendMessage("Invalid onClick action: " + onClickAction);
            return;
        }

        String action = parts[0];
        String value = parts[1];

        if (action.equalsIgnoreCase("command")) {
            player.performCommand(value);
        } else if (action.equalsIgnoreCase("open")) {
            openForm(player, value);
        }
    }

    public Map<String, FormMenu> getFormMenus() {
        return formMenus;
    }
}