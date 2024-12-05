package it.pintux.life.common.api;

import it.pintux.life.common.form.FormMenuUtil;
import it.pintux.life.common.form.obj.FormMenu;
import it.pintux.life.common.utils.FormPlayer;

import javax.management.InstanceAlreadyExistsException;
import java.util.function.BiConsumer;

public class BedrockGuiAPI {

    private final FormMenuUtil menuUtil;

    public BedrockGuiAPI(FormMenuUtil menuUtil) {
        this.menuUtil = menuUtil;
    }

    public void addMenu(String key, FormMenu menu) throws InstanceAlreadyExistsException {
        if (menuUtil.getFormMenus().containsKey(key.toLowerCase())) {
            throw new InstanceAlreadyExistsException("Menu already exists with key: " + key);
        }
        menuUtil.getFormMenus().put(key.toLowerCase(), menu);
    }

    public void removeMenu(String key) {
        menuUtil.getFormMenus().remove(key.toLowerCase());
    }

    public FormMenu getMenu(String key) {
        return menuUtil.getFormMenus().get(key.toLowerCase());
    }

    public FormMenuBuilder createMenuBuilder() {
        return new FormMenuBuilder(menuUtil.getButtonCallbacks());
    }

    public void addMenuFromBuilder(String key, FormMenuBuilder builder) throws InstanceAlreadyExistsException {
        addMenu(key, builder.build());
    }

    public void registerButtonCallback(String action, BiConsumer<FormPlayer, String> callback) throws InstanceAlreadyExistsException {
        if (menuUtil.getFormMenus().containsKey(action.toLowerCase())) {
            throw new InstanceAlreadyExistsException("Action already exists with key: " + action);
        }
        menuUtil.getButtonCallbacks().put(action.toLowerCase(), callback);
    }

    public BiConsumer<FormPlayer, String> getCallbackForAction(String action) {
        return menuUtil.getButtonCallbacks().get(action.toLowerCase());
    }

    public void openMenu(FormPlayer player, FormMenu menu, String[] args) {
        menuUtil.openForm(player, menu, args);
    }

    public FormMenuUtil getMenuUtil() {
        return menuUtil;
    }
}