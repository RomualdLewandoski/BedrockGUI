package it.pintux.life.form;

import java.util.List;

public record FormMenu(String formCommand, String formTitle, String formContent, String formType, List<FormButton> formButtons) {
}
