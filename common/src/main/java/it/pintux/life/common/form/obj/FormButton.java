package it.pintux.life.common.form.obj;

import java.util.StringJoiner;

public class FormButton {
    private String text;
    private String image;
    private String onClick;

    public FormButton(String text, String image, String onClick) {
        this.text = text;
        this.image = image;
        this.onClick = onClick;
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

    public String getOnClick() {
        return onClick;
    }

    public void setOnClick(String onClick) {
        this.onClick = onClick;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FormButton.class.getSimpleName() + "[", "]")
                .add("text='" + text + "'")
                .add("image='" + image + "'")
                .add("onClick='" + onClick + "'")
                .toString();
    }
}
