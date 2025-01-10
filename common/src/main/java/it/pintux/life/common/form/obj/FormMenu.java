package it.pintux.life.common.form.obj;

import java.util.List;
import java.util.StringJoiner;

public class FormMenu {
    private String formCommand;
    private String server;
    private String permission;
    private String formTitle;
    private String formContent;
    private FormMenuType formType;
    private List<FormButton> formButtons;
    private List<FormComponent> components;
    private List<FormAction> globalActions;

    public FormMenu(String formCommand, String server, String permission, String formTitle, String formContent, FormMenuType formType, List<FormButton> formButtons, List<FormComponent> components, List<FormAction> globalActions) {
        this.formCommand = formCommand;
        this.server = server;
        this.permission = permission;
        this.formTitle = formTitle;
        this.formContent = formContent;
        this.formType = formType;
        this.formButtons = formButtons;
        this.components = components;
        this.globalActions = globalActions;
    }

    public String getFormCommand() {
        return formCommand;
    }

    public void setFormCommand(String formCommand) {
        this.formCommand = formCommand;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
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

    public FormMenuType getFormType() {
        return formType;
    }

    public void setFormType(FormMenuType formType) {
        this.formType = formType;
    }

    public List<FormButton> getFormButtons() {
        return formButtons;
    }

    public void setFormButtons(List<FormButton> formButtons) {
        this.formButtons = formButtons;
    }

    public List<FormComponent> getComponents() {
        return components;
    }

    public void setComponents(List<FormComponent> components) {
        this.components = components;
    }

    public List<FormAction> getGlobalActions() {
        return globalActions;
    }

    public void setGlobalActions(List<FormAction> globalActions) {
        this.globalActions = globalActions;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FormMenu.class.getSimpleName() + "[", "]")
                .add("formCommand='" + formCommand + "'")
                .add("server='" + server + "'")
                .add("permission='" + permission + "'")
                .add("formTitle='" + formTitle + "'")
                .add("formContent='" + formContent + "'")
                .add("formType=" + formType)
                .add("formButtons=" + formButtons)
                .add("components=" + components)
                .add("globalActions=" + globalActions)
                .toString();
    }
}
