package it.pintux.life.form;

import java.util.List;
import java.util.StringJoiner;

public class FormMenu {
    private String formCommand;
    private String permission;
    private String formTitle;
    private String formContent;
    private String formType;
    private List<FormButton> formButtons;

    public FormMenu(String formCommand,String permission, String formTitle, String formContent, String formType, List<FormButton> formButtons) {
        this.formCommand = formCommand;
        this.permission = permission;
        this.formTitle = formTitle;
        this.formContent = formContent;
        this.formType = formType;
        this.formButtons = formButtons;
    }

    public String getFormCommand() {
        return formCommand;
    }

    public void setFormCommand(String formCommand) {
        this.formCommand = formCommand;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getFormTitle() {
        return formTitle;
    }

    public void setFormTitle(String formTitle) {
        this.formTitle = formTitle;
    }

    public String getFormContent() {
        return formContent;
    }

    public void setFormContent(String formContent) {
        this.formContent = formContent;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public List<FormButton> getFormButtons() {
        return formButtons;
    }

    public void setFormButtons(List<FormButton> formButtons) {
        this.formButtons = formButtons;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FormMenu.class.getSimpleName() + "[", "]")
                .add("formCommand='" + formCommand + "'")
                .add("formTitle='" + formTitle + "'")
                .add("formContent='" + formContent + "'")
                .add("formType='" + formType + "'")
                .add("formButtons=" + formButtons)
                .toString();
    }
}
