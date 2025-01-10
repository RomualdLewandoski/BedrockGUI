package it.pintux.life.common.form.obj;

import it.pintux.life.common.utils.FormPlayer;

import java.util.Map;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

public class FormComponent {
    private String key;
    private String type;
    private Map<String, Object> properties;
    private final BiConsumer<FormPlayer, Object> callback;

    public FormComponent(String key, String type, Map<String, Object> properties, BiConsumer<FormPlayer, Object> callback) {
        this.key = key;
        this.type = type;
        this.properties = properties;
        this.callback = callback;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public BiConsumer<FormPlayer, Object> getCallback() {
        return callback;
    }

    public void executeCallback(FormPlayer player, Object value) {
        if (callback != null) {
            System.out.println("Executing callback for player " + player + " with value " + value);
            callback.accept(player, value);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FormComponent.class.getSimpleName() + "[", "]")
                .add("key='" + key + "'")
                .add("type='" + type + "'")
                .add("properties=" + properties)
                .toString();
    }
}
