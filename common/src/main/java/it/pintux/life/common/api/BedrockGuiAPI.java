package it.pintux.life.common.api;

import it.pintux.life.common.form.FormMenuUtil;
import it.pintux.life.common.form.obj.FormMenu;
import it.pintux.life.common.utils.FormPlayer;

import javax.management.InstanceAlreadyExistsException;
import java.util.function.BiConsumer;

/**
 * The primary API for creating, managing, and opening GUI menus (Simple, Modal, Custom).
 * <p>
 * Internally uses a {@link FormMenuUtil} to load, store, and open {@link FormMenu} objects.
 */
public class BedrockGuiAPI {

    private final FormMenuUtil menuUtil;

    /**
     * Creates a new BedrockGuiAPI instance, wrapping an existing {@link FormMenuUtil}.
     *
     * @param menuUtil The utility that handles form creation and opening logic.
     */
    public BedrockGuiAPI(FormMenuUtil menuUtil) {
        this.menuUtil = menuUtil;
    }

    /**
     * Adds a new {@link FormMenu} to the internal storage under the given key.
     *
     * @param key  The unique key to reference this menu later (case-insensitive).
     * @param menu The FormMenu instance to store.
     * @throws InstanceAlreadyExistsException If a menu with the same key already exists.
     */
    public void addMenu(String key, FormMenu menu) throws InstanceAlreadyExistsException {
        if (menuUtil.getFormMenus().containsKey(key.toLowerCase())) {
            throw new InstanceAlreadyExistsException("Menu already exists with key: " + key);
        }
        menuUtil.getFormMenus().put(key.toLowerCase(), menu);
    }

    /**
     * Removes a stored {@link FormMenu} from the internal storage.
     *
     * @param key The unique key for the menu to remove (case-insensitive).
     */
    public void removeMenu(String key) {
        menuUtil.getFormMenus().remove(key.toLowerCase());
    }

    /**
     * Retrieves a {@link FormMenu} by its key.
     *
     * @param key The unique key (case-insensitive).
     * @return The FormMenu instance if found, otherwise null.
     */
    public FormMenu getMenu(String key) {
        return menuUtil.getFormMenus().get(key.toLowerCase());
    }

    /**
     * Creates a new {@link FormMenuBuilder} which can be used to build
     * a {@link FormMenu} programmatically (Simple, Modal, Custom).
     *
     * @return a fresh FormMenuBuilder instance.
     */
    public FormMenuBuilder createMenuBuilder() {
        return new FormMenuBuilder(menuUtil.getButtonCallbacks());
    }

    /**
     * Builds a menu from the provided builder and then registers it under the specified key.
     *
     * @param key     The unique key to store the resulting menu.
     * @param builder The builder containing all menu configuration.
     * @throws InstanceAlreadyExistsException If a menu with the same key already exists.
     */
    public void addMenuFromBuilder(String key, FormMenuBuilder builder) throws InstanceAlreadyExistsException {
        addMenu(key, builder.build());
    }

    /**
     * Registers a custom action callback under the given action name.
     * <p>
     * Example usage:
     * <pre>
     *     api.registerButtonCallback("myaction", (player, actionValue) -> {
     *         // handle action
     *     });
     * </pre>
     *
     * @param action   The action name (case-insensitive).
     * @param callback The callback code to be executed when this action is triggered.
     * @throws InstanceAlreadyExistsException If an existing menu uses the same key (conflict).
     */
    public void registerButtonCallback(String action, BiConsumer<FormPlayer, String> callback)
            throws InstanceAlreadyExistsException {
        if (menuUtil.getFormMenus().containsKey(action.toLowerCase())) {
            throw new InstanceAlreadyExistsException("Action already exists with key: " + action);
        }
        menuUtil.getButtonCallbacks().put(action.toLowerCase(), callback);
    }

    /**
     * Retrieves a previously registered callback for a given action.
     *
     * @param action The action name (case-insensitive).
     * @return The callback if found, otherwise null.
     */
    public BiConsumer<FormPlayer, String> getCallbackForAction(String action) {
        return menuUtil.getButtonCallbacks().get(action.toLowerCase());
    }

    /**
     * Opens a specific {@link FormMenu} for the given player, optionally providing arguments
     * that can be used to replace placeholders ($1, $2, etc.) in the menu title, description, or actions.
     *
     * @param player The player who will see the menu.
     * @param menu   The FormMenu instance to open.
     * @param args   Optional arguments for placeholder replacements.
     */
    public void openMenu(FormPlayer player, FormMenu menu, String[] args) {
        menuUtil.openForm(player, menu, args);
    }

    /**
     * @return The underlying {@link FormMenuUtil} instance, used to manipulate menus directly if needed.
     */
    public FormMenuUtil getMenuUtil() {
        return menuUtil;
    }
}