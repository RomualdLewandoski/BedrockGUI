package it.pintux.life.common.api;

import it.pintux.life.common.form.obj.*;
import it.pintux.life.common.utils.FormPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A builder class for creating {@link FormMenu} objects programmatically.
 * <p>
 * Supports three menu types:
 * <ul>
 *     <li>{@link FormMenuType#SIMPLE} - A simple menu with multiple clickable buttons.</li>
 *     <li>{@link FormMenuType#MODAL} - A yes/no (2-button) form.</li>
 *     <li>{@link FormMenuType#CUSTOM} - A form with multiple components (input, slider, etc.).</li>
 * </ul>
 */
public class FormMenuBuilder {

    private String command;
    private String server;
    private String permission;
    private String title;
    private String description;
    private FormMenuType type;
    private final List<FormButton> buttons;
    private final List<FormComponent> components;
    private final List<FormAction> globalActions;
    private final Map<String, BiConsumer<FormPlayer, String>> actionCallbacks;

    /**
     * Protected constructor for creating a FormMenuBuilder.
     * Typically obtained via {@code BedrockGuiAPI.createMenuBuilder()}.
     *
     * @param actionCallbacks the map of known callbacks keyed by an action name (case-insensitive).
     */
    protected FormMenuBuilder(Map<String, BiConsumer<FormPlayer, String>> actionCallbacks) {
        this.actionCallbacks = actionCallbacks;
        this.buttons = new ArrayList<>();
        this.components = new ArrayList<>();
        this.globalActions = new ArrayList<>();
        this.type = FormMenuType.SIMPLE;
        this.title = "Unknown";
    }

    /**
     * Sets a command string, typically used for argument validation or referencing in some form menus.
     *
     * @param command The command string.
     * @return This builder instance for chaining.
     */
    public FormMenuBuilder setCommand(String command) {
        this.command = command;
        return this;
    }

    /**
     * Sets a server string (for advanced usage where menus may belong to different servers).
     *
     * @param server The server identifier.
     * @return This builder instance for chaining.
     */
    public FormMenuBuilder setServer(String server) {
        this.server = server;
        return this;
    }

    /**
     * Sets a permission required to open this menu.
     *
     * @param permission The permission node.
     * @return This builder instance for chaining.
     */
    public FormMenuBuilder setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * Sets the menu title. This text may support placeholders (e.g., $1, %player_name%).
     *
     * @param title The menu title.
     * @return This builder instance for chaining.
     */
    public FormMenuBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the menu description. This text may support placeholders (e.g., $1, %player_name%).
     *
     * @param description The menu description.
     * @return This builder instance for chaining.
     */
    public FormMenuBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the type of the form menu (SIMPLE, MODAL, or CUSTOM).
     *
     * @param type The {@link FormMenuType} to set.
     * @return This builder instance for chaining.
     */
    public FormMenuBuilder setType(FormMenuType type) {
        this.type = type;
        return this;
    }

    /**
     * Adds a new button to this menu (for SIMPLE or MODAL types) referencing a known callback key.
     * <p>
     * The callback key is the first "word" in the action line when used in config,
     * e.g., "command", "say", "open", or any custom callback previously registered.
     *
     * @param text        The button label.
     * @param image       The button image URL (can be null if no image).
     * @param callbackKey The action name (case-insensitive).
     * @return This builder instance for chaining.
     * @throws IllegalArgumentException if no callback is found for the given {@code callbackKey}.
     */
    public FormMenuBuilder addButton(String text, String image, String callbackKey) {
        BiConsumer<FormPlayer, String> callback = actionCallbacks.get(callbackKey.toLowerCase());
        if (callback != null) {
            buttons.add(new FormButton(text, image, callback));
        } else {
            throw new IllegalArgumentException("Callback not found for key: " + callbackKey);
        }
        return this;
    }

    /**
     * Adds a new button to this menu (for SIMPLE or MODAL types) with a custom inline callback.
     *
     * @param text     The button label.
     * @param image    The button image URL (can be null if no image).
     * @param callback The callback to execute when the button is clicked.
     * @return This builder instance for chaining.
     */
    public FormMenuBuilder addButton(String text, String image, BiConsumer<FormPlayer, String> callback) {
        buttons.add(new FormButton(text, image, callback));
        return this;
    }

    /**
     * Adds a new component to a CUSTOM form.
     * <p>
     * For example, an <em>input</em> component might specify:
     * <pre>
     *     key = "name_input"
     *     type = "input"
     *     properties = {
     *         "text": "Enter your name",
     *         "placeholder": "Name here",
     *         "default": "Steve"
     *     }
     * </pre>
     *
     * @param key        The unique component key (used for referencing in global actions).
     * @param type       The type of this component (e.g., "input", "slider", "toggle", "dropdown").
     * @param properties The map of component-specific properties (like "text", "default", etc.).
     * @param callback   An optional inline callback, if you need direct code execution for this component.
     * @return This builder instance for chaining.
     */
    public FormMenuBuilder addComponent(String key, String type, Map<String, Object> properties, BiConsumer<FormPlayer, Object> callback) {
        components.add(new FormComponent(key, type, properties, callback));
        return this;
    }

    /**
     * Adds a global action that is executed after the user submits a CUSTOM form.
     * <p>
     * These actions can reference each component's value with <code>$componentKey</code>.
     *
     * @param action The {@link FormAction} to be added.
     * @return This builder instance for chaining.
     */
    public FormMenuBuilder addGlobalAction(FormAction action) {
        globalActions.add(action);
        return this;
    }

    /**
     * Builds a {@link FormMenu} object based on the current configuration.
     * <p>
     * Validates the button count for MODAL (must be exactly 2) and ensures CUSTOM forms have components.
     *
     * @return A new {@link FormMenu} instance representing the configured form.
     * @throws IllegalStateException If the form type doesn't meet its requirements (e.g., not 2 buttons for MODAL, or no components for CUSTOM).
     */
    public FormMenu build() {
        if (type == FormMenuType.MODAL && buttons.size() != 2) {
            throw new IllegalStateException("MODAL menus must have exactly 2 buttons.");
        }

        if (type == FormMenuType.CUSTOM && components.isEmpty()) {
            throw new IllegalStateException("CUSTOM menus must have at least one component.");
        }

        return new FormMenu(command, server, permission, title, description, type, buttons, components, globalActions);
    }
}