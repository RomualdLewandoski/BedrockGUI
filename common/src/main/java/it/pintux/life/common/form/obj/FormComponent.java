package it.pintux.life.common.form.obj;

import java.util.Map;
import java.util.StringJoiner;

public class FormComponent {
    private String key;
    private String type;
    private Map<String, Object> properties;

    public FormComponent(String key, String type, Map<String, Object> properties) {
        this.key = key;
        this.type = type;
        this.properties = properties;
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

    @Override
    public String toString() {
        return new StringJoiner(", ", FormComponent.class.getSimpleName() + "[", "]")
                .add("key='" + key + "'")
                .add("type='" + type + "'")
                .add("properties=" + properties)
                .toString();
    }
}
