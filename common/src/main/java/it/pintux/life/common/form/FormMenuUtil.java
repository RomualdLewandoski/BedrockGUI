package it.pintux.life.common.form;

import it.pintux.life.common.form.obj.FormButton;
import it.pintux.life.common.form.obj.FormMenu;
import it.pintux.life.common.utils.FormConfig;
import it.pintux.life.common.utils.FormPlayer;
import it.pintux.life.common.utils.MessageData;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.*;

public class FormMenuUtil {

    private final Map<String, FormMenu> formMenus;
    private final FormConfig config;
    private final MessageData messageData;

    public FormMenuUtil(FormConfig config, MessageData messageData) {
        this.config = config;
        formMenus = new HashMap<>();
        this.messageData = messageData;
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
            if (type.equalsIgnoreCase("SIMPLE") || type.equalsIgnoreCase("MODAL")) {
                for (String button : config.getKeys("menu." + key + ".buttons")) {
                    String text = config.getString("menu." + key + ".buttons." + button + ".text");
                    String image = config.getString("menu." + key + ".buttons." + button + ".image");
                    String onClick = config.getString("menu." + key + ".buttons." + button + ".onClick");
                    buttons.add(new FormButton(text, image, onClick));
                }
                if (type.equalsIgnoreCase("MODAL")) {
                    if (buttons.size() != 2) {
                        System.out.println("Modal's must only have 2 buttons! Please modify menu." + key);
                        continue;
                    }
                }
            }

            Map<String, Map<String, Object>> components = new HashMap<>();
            if (type.equalsIgnoreCase("CUSTOM")) {
                for (String componentKey : config.getKeys("menu." + key + ".components")) {
                    Map<String, Object> component = config.getValues("menu." + key + ".components." + componentKey);
                    components.put(componentKey, component);
                }
            }

            List<String> globalActions = config.getStringList("menu." + key + ".global_actions");

            FormMenu menu = new FormMenu(command, permission, title, description, type, buttons, components, globalActions);
            formMenus.put(key.toLowerCase(), menu);
            System.out.println("Loaded form menu: " + key + " type: " + type);
        }
    }

    public void openForm(FormPlayer player, String menuName, String[] args) {
        FormMenu menu = formMenus.get(menuName.toLowerCase());
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

        String type = menu.getFormType();

        Map<String, String> placeholders = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            placeholders.put(String.valueOf(i + 1), args[i]);
        }

        switch (type.toUpperCase()) {
            case "MODAL":
                openModalForm(player, menu, placeholders);
                break;
            case "SIMPLE":
                openSimpleForm(player, menu, placeholders);
                break;
            case "CUSTOM":
                openCustomForm(player, menu, placeholders);
                break;
        }
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

        formBuilder
                .button1(replacePlaceholders(b1.getText(), placeholders, player))
                .button2(replacePlaceholders(b2.getText(), placeholders, player))
                .validResultHandler((formResponse, modalResponse) -> {
                    if (modalResponse.clickedButtonId() == 0) {
                        if (b1.getOnClick() != null) {
                            handleOnClick(player, b1.getOnClick(), placeholders);
                        }
                    } else {
                        if (b2.getOnClick() != null) {
                            handleOnClick(player, b2.getOnClick(), placeholders);
                        }
                    }
                })
                .build();

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

        List<String> onClickActions = new ArrayList<>();
        for (FormButton button : buttons) {
            String buttonText = replacePlaceholders(button.getText(), placeholders, player);
            if (button.getImage() != null) {
                formBuilder.button(buttonText, FormImage.Type.URL, button.getImage());
            } else {
                formBuilder.button(buttonText);
            }
            if (button.getOnClick() != null) {
                onClickActions.add(button.getOnClick());
            }
        }

        formBuilder.validResultHandler((form, response) -> {
            int clickedButtonId = response.clickedButtonId();
            String action = onClickActions.get(clickedButtonId);

            handleOnClick(player, action, placeholders);
        });

        SimpleForm form = formBuilder.build();
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    private void openCustomForm(FormPlayer player, FormMenu formMenu, Map<String, String> placeholders) {
        String title = replacePlaceholders(formMenu.getFormTitle(), placeholders, player);
        CustomForm.Builder formBuilder = CustomForm.builder().title(title);

        Map<Integer, String> componentActions = new HashMap<>();
        Map<String, Object> componentResults = new HashMap<>();
        int[] componentIndex = {0};

        for (String componentKey : formMenu.getComponents().keySet()) {
            Map<String, Object> component = formMenu.getComponents().get(componentKey);
            String type = (String) component.get("type");

            switch (type.toLowerCase()) {
                case "input":
                    String inputText = replacePlaceholders((String) component.get("text"), placeholders, player);
                    String placeholder = (String) component.get("placeholder");
                    String defaultValue = (String) component.get("default");
                    formBuilder.input(inputText, placeholder, defaultValue);
                    componentActions.put(componentIndex[0], (String) component.get("action"));
                    componentResults.put(componentKey, "");
                    break;
                case "slider":
                    String sliderText = replacePlaceholders((String) component.get("text"), placeholders, player);
                    int min = (int) component.get("min");
                    int max = (int) component.get("max");
                    int step = (int) component.get("step");
                    int defaultSlider = (int) component.get("default");
                    formBuilder.slider(sliderText, min, max, step, defaultSlider);
                    componentActions.put(componentIndex[0], (String) component.get("action"));
                    componentResults.put(componentKey, 0);
                    break;
                case "dropdown":
                    String dropdownText = replacePlaceholders((String) component.get("text"), placeholders, player);
                    List<String> options = (List<String>) component.get("options");
                    int defaultDropdown = (int) component.get("default");
                    formBuilder.dropdown(dropdownText, options, defaultDropdown);
                    componentActions.put(componentIndex[0], (String) component.get("action"));
                    componentResults.put(componentKey, "");
                    break;
                case "toggle":
                    String toggleText = replacePlaceholders((String) component.get("text"), placeholders, player);
                    boolean defaultToggle = (boolean) component.get("default");
                    formBuilder.toggle(toggleText, defaultToggle);
                    componentActions.put(componentIndex[0], (String) component.get("action"));
                    componentResults.put(componentKey, false);
                    break;
            }

            componentIndex[0]++;
        }

        formBuilder.validResultHandler((formResponse, customFormResponse) -> {
            componentIndex[0] = 0;

            for (String componentKey : formMenu.getComponents().keySet()) {
                Map<String, Object> component = formMenu.getComponents().get(componentKey);
                String type = (String) component.get("type");
                String action = componentActions.get(componentIndex[0]);

                String result = "";
                switch (type.toLowerCase()) {
                    case "input":
                        result = customFormResponse.asInput(componentIndex[0]);
                        componentResults.put(componentKey, result);
                        break;
                    case "slider":
                        int sliderResult = (int) customFormResponse.asSlider(componentIndex[0]);
                        result = String.valueOf(sliderResult);
                        componentResults.put(componentKey, sliderResult);
                        break;
                    case "dropdown":
                        int dropdownResult = customFormResponse.asDropdown(componentIndex[0]);
                        List<String> options = (List<String>) component.get("options");
                        result = options.get(dropdownResult);
                        componentResults.put(componentKey, result);
                        break;
                    case "toggle":
                        boolean toggleResult = customFormResponse.asToggle(componentIndex[0]);
                        result = String.valueOf(toggleResult);
                        componentResults.put(componentKey, toggleResult);
                        break;
                }

                if (action != null) {
                    handleCustomAction(player, action, result);
                }

                componentIndex[0]++;
            }

            List<String> globalActions = formMenu.getGlobalActions();
            if (globalActions != null) {
                for (String globalAction : globalActions) {
                    String finalAction = globalAction;
                    for (Map.Entry<String, Object> entry : componentResults.entrySet()) {
                        finalAction = finalAction.replace("$" + entry.getKey(), entry.getValue().toString());
                    }

                    handleCustomAction(player, finalAction, null);
                }
            }
        });

        CustomForm form = formBuilder.build();
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    private void handleOnClick(FormPlayer player, String onClickAction, Map<String, String> placeholders) {
        onClickAction = replacePlaceholders(onClickAction.trim().replaceAll("\\s+", " "), placeholders, player);

        String[] parts = onClickAction.split(" ", 2);

        if (parts.length < 2) {
            player.sendMessage("Invalid onClick action: " + onClickAction);
            return;
        }

        String action = parts[0];
        String value = parts[1];

        if (action.equalsIgnoreCase("command")) {
            if (!value.startsWith("/")) {
                value = "/".concat(value);
            }
            player.executeAction(value);
        } else if (action.equalsIgnoreCase("open")) {
            String[] newArgs = value.split(" ");
            String menuName = newArgs[0];
            String[] actualArgs = Arrays.copyOfRange(newArgs, 1, newArgs.length);
            openForm(player, menuName, actualArgs);
        }
    }

    private boolean validateCommandArguments(String command, String[] args, FormPlayer player) {
        if (command == null || command.isEmpty()) {
            return true;
        }
        int requiredArgs = countPlaceholders(command);
        if (args.length < requiredArgs) {
            player.sendMessage(messageData.getValue(MessageData.MENU_ARGS, Map.of("args", requiredArgs), null));
            return false;
        }
        return true;
    }


    private void handleCustomAction(FormPlayer player, String action, String value) {
        if (value != null) {
            action = action.replace("$1", value);
        }

        String[] parts = action.split(" ", 2);
        String actionType = parts[0];
        String actionValue = parts.length > 1 ? parts[1] : "";

        switch (actionType.toLowerCase()) {
            case "command":
                if (!actionValue.startsWith("/")) {
                    actionValue = "/" + actionValue;
                }
                player.executeAction(actionValue);
                break;
            default:
                player.sendMessage("Unknown action type: " + actionType);
                break;
        }
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

        return text;
    }

    public Map<String, FormMenu> getFormMenus() {
        return formMenus;
    }
}