package it.pintux.life.common.form.obj;

import it.pintux.life.common.utils.FormPlayer;

import java.util.StringJoiner;
import java.util.function.BiConsumer;

public class FormAction {

    private String action;
    private final BiConsumer<FormPlayer, String> callback;

    public FormAction(String action, BiConsumer<FormPlayer, String> callback) {
        this.action = action;
        this.callback = callback;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public BiConsumer<FormPlayer, String> getCallback() {
        return callback;
    }

    public void executeCallback(FormPlayer player, String value) {
        if (callback != null) {
            callback.accept(player, value);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FormAction.class.getSimpleName() + "[", "]")
                .add("action='" + action + "'")
                .add("callback=" + callback)
                .toString();
    }
}
