package it.pintux.life.common.api;

import it.pintux.life.common.form.obj.FormButton;
import it.pintux.life.common.form.obj.FormComponent;
import it.pintux.life.common.form.obj.FormMenu;
import it.pintux.life.common.form.obj.FormMenuType;
import it.pintux.life.common.utils.FormPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class FormMenuBuilder {

    private String command;
    private String permission;
    private String title = "Unknown";
    private String description;
    private FormMenuType type = FormMenuType.SIMPLE;
    private final List<FormButton> buttons = new ArrayList<>();
    private final List<FormComponent> components = new ArrayList<>();
    private final List<String> globalActions = new ArrayList<>();
    private final Map<String, BiConsumer<FormPlayer, String>> actionCallbacks;

    protected FormMenuBuilder(Map<String, BiConsumer<FormPlayer, String>> actionCallbacks) {
        this.actionCallbacks = actionCallbacks;
    }

    public FormMenuBuilder setCommand(String command) {
        this.command = command;
        return this;
    }

    public FormMenuBuilder setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public FormMenuBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public FormMenuBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public FormMenuBuilder setType(FormMenuType type) {
        this.type = type;
        return this;
    }

    public FormMenuBuilder addButton(String text, String image, String callbackKey) {
        BiConsumer<FormPlayer, String> callback = actionCallbacks.get(callbackKey.toLowerCase());
        if (callback != null) {
            buttons.add(new FormButton(text, image, callback));
        } else {
            throw new IllegalArgumentException("Callback not found for key: " + callbackKey);
        }
        return this;
    }

    public FormMenuBuilder addButton(String text, String image, BiConsumer<FormPlayer, String> callback) {
        buttons.add(new FormButton(text, image, callback));
        return this;
    }

    public FormMenuBuilder addComponent(String key, String type, Map<String, Object> properties) {
        components.add(new FormComponent(key, type, properties));
        return this;
    }

    public FormMenuBuilder addGlobalAction(String action) {
        globalActions.add(action);
        return this;
    }

    public FormMenu build() {
        if (type == FormMenuType.MODAL && buttons.size() != 2) {
            throw new IllegalStateException("MODAL menus must have exactly 2 buttons.");
        }

        if (type == FormMenuType.CUSTOM && components.isEmpty()) {
            throw new IllegalStateException("CUSTOM menus must have at least one component.");
        }

        return new FormMenu(command, permission, title, description, type, buttons, components, globalActions);
    }
}