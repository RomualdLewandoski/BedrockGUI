package it.pintux.life.common.form;

import it.pintux.life.common.form.obj.FormButton;
import it.pintux.life.common.form.obj.FormComponent;
import it.pintux.life.common.form.obj.FormMenu;
import it.pintux.life.common.form.obj.FormMenuType;
import it.pintux.life.common.utils.FormConfig;
import it.pintux.life.common.utils.FormPlayer;
import it.pintux.life.common.utils.MessageData;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class FormMenuUtil {

    private final Map<String, FormMenu> formMenus;
    private final FormConfig config;
    private final MessageData messageData;
    private final Logger logger = Logger.getLogger(FormMenuUtil.class.getName());
    private final Map<String, BiConsumer<FormPlayer, String>> buttonCallbacks;

    public FormMenuUtil(FormConfig config, MessageData messageData) {
        this.config = config;
        formMenus = new HashMap<>();
        this.messageData = messageData;
        buttonCallbacks = new HashMap<>();
        registerDefaultCallbacks();
        loadFormMenus();
    }

    private void loadFormMenus() {
        for (String key : config.getKeys("menu")) {
            String command = config.getString("menu." + key + ".command");
            String permission = config.getString("menu." + key + ".permission");
            String type = config.getString("menu." + key + ".type", "SIMPLE");
            String title = config.getString("menu." + key + ".title", "Unknown");
            String description = config.getString("menu." + key + ".description");
            List<FormButton> buttons = new ArrayList<>();
            FormMenuType menuType;

            try {
                menuType = FormMenuType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid menu type: " + type + ". Skipping menu: " + key);
                continue;
            }

            if (menuType == FormMenuType.SIMPLE || menuType == FormMenuType.MODAL) {
                for (String buttonKey : config.getKeys("menu." + key + ".buttons")) {
                    String text = config.getString("menu." + key + ".buttons." + buttonKey + ".text");
                    String image = config.getString("menu." + key + ".buttons." + buttonKey + ".image");
                    String action = config.getString("menu." + key + ".buttons." + buttonKey + ".onClick");

                    if (action == null) {
                        logger.warning("No action defined for button: " + buttonKey + " in menu: " + key + ".");
                        buttons.add(new FormButton(text, image, null));
                        continue;
                    }

                    String[] actionParts = action.split(" ", 2);
                    String actionType = actionParts[0].toLowerCase();
                    String actionValue = actionParts.length > 1 ? actionParts[1] : "";

                    BiConsumer<FormPlayer, String> callback = getCallbackForAction(actionType);
                    if (callback != null) {
                        buttons.add(new FormButton(text, image, (player, ignored) -> callback.accept(player, actionValue)));
                    } else {
                        logger.warning("Unknown action type: " + actionType + " for button: " + buttonKey);
                    }
                }

                if (menuType == FormMenuType.MODAL && buttons.size() != 2) {
                    logger.info("MODAL menus must have exactly 2 buttons. Skipping menu: " + key);
                    continue;
                }
            }

            List<FormComponent> components = new ArrayList<>();
            if (menuType == FormMenuType.CUSTOM) {
                for (String componentKey : config.getKeys("menu." + key + ".components")) {
                    String componentType = config.getString("menu." + key + ".components." + componentKey + ".type");
                    Map<String, Object> properties = config.getValues("menu." + key + ".components." + componentKey);

                    components.add(new FormComponent(componentKey, componentType, properties));
                }
            }

            List<String> globalActions = config.getStringList("menu." + key + ".global_actions");

            FormMenu menu = new FormMenu(command, permission, title, description, menuType, buttons, components, globalActions);
            formMenus.put(key.toLowerCase(), menu);
            logger.info("Loaded form menu: " + key + " type: " + type);
        }
    }

    public void openForm(FormPlayer player, FormMenu menu, String[] args) {
        if (menu == null) {
            player.sendMessage(messageData.getValue(MessageData.MENU_NOT_FOUND, null, null));
            return;
        }

        if (menu.getPermission() != null && !player.hasPermission(menu.getPermission())) {
            player.sendMessage(messageData.getValue(MessageData.MENU_NOPEX, null, null));
            return;
        }

        if (menu.getFormCommand() != null && !validateCommandArguments(menu.getFormCommand(), args, player)) {
            return;
        }

        FormMenuType type = menu.getFormType();

        Map<String, String> placeholders = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            placeholders.put(String.valueOf(i + 1), args[i]);
        }

        switch (type) {
            case MODAL:
                openModalForm(player, menu, placeholders);
                break;
            case SIMPLE:
                openSimpleForm(player, menu, placeholders);
                break;
            case CUSTOM:
                openCustomForm(player, menu, placeholders);
                break;
        }
    }

    public void openForm(FormPlayer player, String menuName, String[] args) {
        FormMenu menu = formMenus.get(menuName.toLowerCase());
        openForm(player, menu, args);
    }

    private void openModalForm(FormPlayer player, FormMenu formMenu, Map<String, String> placeholders) {
        String title = replacePlaceholders(formMenu.getFormTitle(), placeholders, player);

        List<FormButton> buttons = formMenu.getFormButtons();
        FormButton b1 = buttons.get(0);
        FormButton b2 = buttons.get(1);

        ModalForm.Builder formBuilder = ModalForm.builder()
                .title(title);

        String content = formMenu.getFormContent();
        if (content != null) {
            formBuilder.content(replacePlaceholders(content, placeholders, player));
        }

        List<BiConsumer<FormPlayer, Map<String, Object>>> globalCallbacks = handleGlobalCallbacks(formMenu, placeholders);

        formBuilder
                .button1(replacePlaceholders(b1.getText(), placeholders, player))
                .button2(replacePlaceholders(b2.getText(), placeholders, player))
                .validResultHandler((formResponse, modalResponse) -> {
                    if (modalResponse.clickedButtonId() == 0) {
                        b1.executeCallback(player, null);
                    } else {
                        b2.executeCallback(player, null);
                    }

                    for (BiConsumer<FormPlayer, Map<String, Object>> globalCallback : globalCallbacks) {
                        globalCallback.accept(player, Map.of());
                    }
                });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), formBuilder.build());
    }


    private void openSimpleForm(FormPlayer player, FormMenu formMenu, Map<String, String> placeholders) {
        String title = replacePlaceholders(formMenu.getFormTitle(), placeholders, player);

        List<FormButton> buttons = formMenu.getFormButtons();
        SimpleForm.Builder formBuilder = SimpleForm.builder().title(title);

        String content = formMenu.getFormContent();
        if (content != null) {
            formBuilder.content(replacePlaceholders(content, placeholders, player));
        }

        List<BiConsumer<FormPlayer, Map<String, Object>>> globalCallbacks = handleGlobalCallbacks(formMenu, placeholders);

        for (FormButton button : buttons) {
            String buttonText = replacePlaceholders(button.getText(), placeholders, player);
            if (button.getImage() != null) {
                formBuilder.button(buttonText, FormImage.Type.URL, button.getImage());
            } else {
                formBuilder.button(buttonText);
            }
        }

        formBuilder.validResultHandler((form, response) -> {
            int clickedButtonId = response.clickedButtonId();
            FormButton clickedButton = buttons.get(clickedButtonId);
            clickedButton.executeCallback(player, null);

            for (BiConsumer<FormPlayer, Map<String, Object>> globalCallback : globalCallbacks) {
                globalCallback.accept(player, Map.of());
            }
        });

        SimpleForm form = formBuilder.build();
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    private List<BiConsumer<FormPlayer, Map<String, Object>>> handleGlobalCallbacks(FormMenu formMenu, Map<String, String> placeholders) {
        List<BiConsumer<FormPlayer, Map<String, Object>>> globalCallbacks = new ArrayList<>();
        for (String globalAction : formMenu.getGlobalActions()) {
            globalCallbacks.add((formPlayer, results) -> {
                String resolvedAction = replacePlaceholders(globalAction, placeholders, formPlayer);
                rezolveAction(formPlayer, resolvedAction);
            });
        }
        return globalCallbacks;
    }


    private void openCustomForm(FormPlayer player, FormMenu formMenu, Map<String, String> placeholders) {
        String title = replacePlaceholders(formMenu.getFormTitle(), placeholders, player);
        CustomForm.Builder formBuilder = CustomForm.builder().title(title);

        Map<Integer, BiConsumer<FormPlayer, String>> componentCallbacks = new HashMap<>();
        Map<String, Object> componentResults = new HashMap<>();
        List<BiConsumer<FormPlayer, Map<String, Object>>> globalCallbacks = new ArrayList<>();
        int[] componentIndex = {0};

        for (FormComponent component : formMenu.getComponents()) {
            String type = component.getType().toLowerCase();
            Map<String, Object> properties = component.getProperties();
            String componentKey = component.getKey();

            switch (type) {
                case "input":
                    String inputText = replacePlaceholders((String) properties.get("text"), placeholders, player);
                    String placeholder = (String) properties.getOrDefault("placeholder", "");
                    String defaultValue = (String) properties.getOrDefault("default", "");
                    formBuilder.input(inputText, placeholder, defaultValue);
                    componentCallbacks.put(componentIndex[0], getCallbackForAction((String) properties.get("action")));
                    componentResults.put(componentKey, "");
                    break;
                case "slider":
                    String sliderText = replacePlaceholders((String) properties.get("text"), placeholders, player);
                    int min = (int) properties.get("min");
                    int max = (int) properties.get("max");
                    int step = (int) properties.get("step");
                    int defaultSlider = (int) properties.get("default");
                    formBuilder.slider(sliderText, min, max, step, defaultSlider);
                    componentCallbacks.put(componentIndex[0], getCallbackForAction((String) properties.get("action")));
                    componentResults.put(componentKey, 0);
                    break;
                case "dropdown":
                    String dropdownText = replacePlaceholders((String) properties.get("text"), placeholders, player);
                    List<String> options = (List<String>) properties.get("options");
                    int defaultDropdown = (int) properties.get("default");
                    formBuilder.dropdown(dropdownText, options, defaultDropdown);
                    componentCallbacks.put(componentIndex[0], getCallbackForAction((String) properties.get("action")));
                    componentResults.put(componentKey, "");
                    break;
                case "toggle":
                    String toggleText = replacePlaceholders((String) properties.get("text"), placeholders, player);
                    boolean defaultToggle = (boolean) properties.get("default");
                    formBuilder.toggle(toggleText, defaultToggle);
                    componentCallbacks.put(componentIndex[0], getCallbackForAction((String) properties.get("action")));
                    componentResults.put(componentKey, false);
                    break;
            }

            componentIndex[0]++;
        }

        for (String globalAction : formMenu.getGlobalActions()) {
            globalCallbacks.add((formPlayer, results) -> {
                String resolvedAction = replaceGlobalPlaceholders(globalAction, results);
                rezolveAction(formPlayer, resolvedAction);
            });
        }

        formBuilder.validResultHandler((formResponse, customFormResponse) -> {
            componentIndex[0] = 0;

            for (FormComponent component : formMenu.getComponents()) {
                String type = component.getType().toLowerCase();
                String componentKey = component.getKey();
                BiConsumer<FormPlayer, String> callback = componentCallbacks.get(componentIndex[0]);

                Object result = null;
                switch (type) {
                    case "input":
                        result = customFormResponse.asInput(componentIndex[0]);
                        componentResults.put(componentKey, result);
                        break;
                    case "slider":
                        result = customFormResponse.asSlider(componentIndex[0]);
                        componentResults.put(componentKey, result);
                        break;
                    case "dropdown":
                        int dropdownIndex = customFormResponse.asDropdown(componentIndex[0]);
                        List<String> options = (List<String>) component.getProperties().get("options");
                        result = options.get(dropdownIndex);
                        componentResults.put(componentKey, result);
                        break;
                    case "toggle":
                        result = customFormResponse.asToggle(componentIndex[0]);
                        componentResults.put(componentKey, result);
                        break;
                }

                if (callback != null) {
                    callback.accept(player, result != null ? result.toString() : null);
                }

                componentIndex[0]++;
            }

            for (BiConsumer<FormPlayer, Map<String, Object>> globalCallback : globalCallbacks) {
                globalCallback.accept(player, componentResults);
            }
        });

        CustomForm form = formBuilder.build();
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    private void rezolveAction(FormPlayer formPlayer, String resolvedAction) {
        String[] actionParts = resolvedAction.split(" ", 2);
        String actionType = actionParts[0].toLowerCase();
        String actionValue = actionParts.length > 1 ? actionParts[1] : "";

        BiConsumer<FormPlayer, String> callback = getCallbackForAction(actionType);
        if (callback != null) {
            callback.accept(formPlayer, actionValue);
        }
    }

    private String replaceGlobalPlaceholders(String action, Map<String, Object> results) {
        for (Map.Entry<String, Object> entry : results.entrySet()) {
            action = action.replace("$" + entry.getKey(), entry.getValue().toString());
        }
        return action;
    }

    private boolean validateCommandArguments(String command, String[] args, FormPlayer player) {
        if (command == null || command.isEmpty() || args == null || args.length == 0) {
            return true;
        }
        int requiredArgs = countPlaceholders(command);
        if (args.length < requiredArgs) {
            player.sendMessage(messageData.getValue(MessageData.MENU_ARGS, Map.of("args", requiredArgs), null));
            return false;
        }
        return true;
    }

    public void registerButtonCallback(String action, BiConsumer<FormPlayer, String> callback) {
        buttonCallbacks.put(action.toLowerCase(), callback);
    }

    private BiConsumer<FormPlayer, String> getCallbackForAction(String action) {
        return buttonCallbacks.get(action.toLowerCase());
    }

    public Map<String, BiConsumer<FormPlayer, String>> getButtonCallbacks() {
        return buttonCallbacks;
    }

    public void registerDefaultCallbacks() {
        registerButtonCallback("command", (player, actionValue) -> {
            String resolvedValue = replacePlaceholders(actionValue, Map.of(), player);
            if (!resolvedValue.startsWith("/")) {
                resolvedValue = "/" + resolvedValue;
            }
            player.executeAction(resolvedValue);
        });

        registerButtonCallback("say", (player, actionValue) -> {
            String resolvedValue = replacePlaceholders(actionValue, Map.of(), player);
            player.sendMessage(resolvedValue);
        });

        registerButtonCallback("open", (player, actionValue) -> {
            String resolvedValue = replacePlaceholders(actionValue, Map.of(), player);
            String[] parts = resolvedValue.split(" ");
            if (parts.length == 0) {
                player.sendMessage(messageData.getValue(MessageData.MENU_INVALID_OPEN_ACTION, null, null));
                return;
            }

            String menuName = parts[0];
            String[] args = parts.length > 1 ? resolvedValue.substring(menuName.length()).trim().split(" ") : new String[0];

            FormMenu menu = getFormMenus().get(menuName.toLowerCase());
            if (menu == null) {
                player.sendMessage(messageData.getValue(MessageData.MENU_NOT_FOUND, null, null));
                return;
            }

            openForm(player, menuName, args);
        });
    }


    private int countPlaceholders(String command) {
        int count = 0;
        while (command.contains("$" + (count + 1))) {
            count++;
        }
        return count;
    }

    private String replacePlaceholders(String text, Map<String, String> placeholders, FormPlayer player) {
        if (text == null) return null;

        if (text.contains("%")) {
            text = messageData.replaceVariables(text, null, player);
        }

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace("$" + entry.getKey(), entry.getValue());
        }

        return messageData.applyColor(text);
    }

    public Map<String, FormMenu> getFormMenus() {
        return formMenus;
    }
}