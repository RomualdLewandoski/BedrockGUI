package it.pintux.life.common.form;

import it.pintux.life.common.form.obj.*;
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
    private final Logger logger = Logger.getLogger(FormMenuUtil.class.getSimpleName());
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
            String server = config.getString("menu." + key + ".server");
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
                    String action = (String) properties.get("action");

                    BiConsumer<FormPlayer, Object> callback = null;
                    if (action != null) {
                        String[] actionParts = action.split(" ", 2);
                        String actionType = actionParts[0];
                        String actionValue = actionParts.length > 1 ? actionParts[1] : "";
                        callback = (player, value) -> {
                            String resolvedAction = replacePlaceholders(actionValue, Map.of(), player);
                            BiConsumer<FormPlayer, String> actionCallback = getCallbackForAction(actionType);
                            if (actionCallback != null) {
                                actionCallback.accept(player, resolvedAction);
                            }
                        };
                    }

                    components.add(new FormComponent(componentKey, componentType, properties, callback));
                }
            }

            List<FormAction> globalActions = new ArrayList<>();
            for (String action : config.getStringList("menu." + key + ".global_actions")) {
                String[] actionParts = action.split(" ", 2);
                String actionName = actionParts[0];
                String actionValue = actionParts.length > 1 ? actionParts[1] : "";

                BiConsumer<FormPlayer, String> callback = getCallbackForAction(actionName);
                if (callback != null) {
                    globalActions.add(new FormAction(actionValue, callback));
                } else {
                    logger.warning("Unknown global action: " + actionName + " in menu: " + key);
                }
            }

            FormMenu menu = new FormMenu(command, server, permission, title, description, menuType, buttons, components, globalActions);
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
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                placeholders.put(String.valueOf(i + 1), args[i]);
            }
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

    public boolean checkServerRequirement(boolean hasPex, String server, String menuName) {
        FormMenu menu = formMenus.get(menuName.toLowerCase());
        if (menu == null) {
            return true;
        }
        String savedServer = menu.getServer();
        if (savedServer == null) {
            return true;
        }
        return savedServer.equalsIgnoreCase(server) || hasPex;
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
        for (FormAction globalAction : formMenu.getGlobalActions()) {
            globalCallbacks.add((formPlayer, results) -> {
                String resolvedAction = replacePlaceholders(globalAction.getAction(), placeholders, formPlayer);
                globalAction.executeCallback(formPlayer, resolvedAction);
            });
        }
        return globalCallbacks;
    }

    private void openCustomForm(FormPlayer player, FormMenu formMenu, Map<String, String> placeholders) {
        String title = replacePlaceholders(formMenu.getFormTitle(), placeholders, player);

        CustomForm.Builder formBuilder = CustomForm.builder().title(title);
        Map<Integer, String> perComponentActionMap = new HashMap<>();

        Map<String, Object> componentResults = new HashMap<>();

        int componentIndex = 0;
        for (FormComponent component : formMenu.getComponents()) {
            String type = component.getType().toLowerCase();
            Map<String, Object> props = component.getProperties();

            String text = replacePlaceholders((String) props.get("text"), placeholders, player);

            switch (type) {
                case "input": {
                    String placeholder = (String) props.getOrDefault("placeholder", "");
                    String defaultVal = (String) props.getOrDefault("default", "");
                    formBuilder.input(text, placeholder, defaultVal);
                    break;
                }
                case "slider": {
                    int min = (int) props.get("min");
                    int max = (int) props.get("max");
                    int step = (int) props.get("step");
                    int defaultSlider = (int) props.get("default");
                    formBuilder.slider(text, min, max, step, defaultSlider);
                    break;
                }
                case "dropdown": {
                    List<String> options = (List<String>) props.get("options");
                    int defaultDropdown = (int) props.get("default");
                    formBuilder.dropdown(text, options, defaultDropdown);
                    break;
                }
                case "toggle": {
                    boolean defaultToggle = (boolean) props.get("default");
                    formBuilder.toggle(text, defaultToggle);
                    break;
                }
            }

            String action = (String) props.get("action");
            if (action != null) {
                perComponentActionMap.put(componentIndex, action);
            }

            componentIndex++;
        }

        List<BiConsumer<FormPlayer, Map<String, Object>>> globalCallbacks = new ArrayList<>();
        for (FormAction globalAction : formMenu.getGlobalActions()) {
            globalCallbacks.add((formPlayer, results) -> {
                String resolvedAction = replaceGlobalPlaceholders(globalAction.getAction(), results);
                resolveAction(formPlayer, resolvedAction);
            });
        }

        formBuilder.validResultHandler((formResponse, customFormResponse) -> {
            for (int i = 0; i < formMenu.getComponents().size(); i++) {
                FormComponent c = formMenu.getComponents().get(i);
                String type = c.getType().toLowerCase();
                String componentKey = c.getKey();
                Object result = null;

                switch (type) {
                    case "input":
                        result = customFormResponse.asInput(i);
                        break;
                    case "slider":
                        result = (int) customFormResponse.asSlider(i);
                        break;
                    case "dropdown": {
                        int ddIndex = customFormResponse.asDropdown(i);
                        List<String> options = (List<String>) c.getProperties().get("options");
                        result = options.get(ddIndex);
                        break;
                    }
                    case "toggle":
                        result = customFormResponse.asToggle(i);
                        break;
                }

                componentResults.put(componentKey, result);

                if (perComponentActionMap.containsKey(i)) {
                    String actionString = perComponentActionMap.get(i);

                    Map<String, String> localPlaceholders = new HashMap<>();
                    localPlaceholders.put("1", String.valueOf(result));

                    String replacedActionString = replacePlaceholders(actionString, localPlaceholders, player);

                    String[] parts = replacedActionString.split(" ", 2);
                    String actionName = parts[0].toLowerCase();
                    String actionValue = parts.length > 1 ? parts[1] : "";

                    BiConsumer<FormPlayer, String> callback = getCallbackForAction(actionName);
                    if (callback != null) {
                        callback.accept(player, actionValue);
                    }
                }
            }

            for (BiConsumer<FormPlayer, Map<String, Object>> globalCallback : globalCallbacks) {
                globalCallback.accept(player, componentResults);
            }
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), formBuilder.build());
    }


    private void resolveAction(FormPlayer formPlayer, String resolvedAction) {
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