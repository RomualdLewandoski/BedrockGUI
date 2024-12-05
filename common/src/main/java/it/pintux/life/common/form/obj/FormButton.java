package it.pintux.life.common.form.obj;

import it.pintux.life.common.utils.FormPlayer;

import java.util.StringJoiner;
import java.util.function.BiConsumer;

public class FormButton {
    private String text;
    private String image;
    private final BiConsumer<FormPlayer, String> callback;

    public FormButton(String text, String image, BiConsumer<FormPlayer, String> callback) {
        this.text = text;
        this.image = image;
        this.callback = callback;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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
        return new StringJoiner(", ", FormButton.class.getSimpleName() + "[", "]")
                .add("text='" + text + "'")
                .add("image='" + image + "'")
                .add("callback=" + callback)
                .toString();
    }
}
