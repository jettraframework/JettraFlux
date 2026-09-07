package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests validating the 5 new reactive UI components in JettraFlux:
 * JettraFluxModal, JettraFluxButton, JettraFluxDynamicForm, JettraFluxJsonEditor, JettraFluxNotification.
 */
public class JettraFluxAdaptiveComponentsTest {

    @Test
    @DisplayName("JettraFluxModal: verify fluent construction, lifecycle, and HTML structure")
    public void testJettraFluxModal() {
        JettraFluxModal modal = JettraFluxModal.of("testModal", "Test Modal Title")
                .subtitle("Modal Subtitle Explanation")
                .icon("fas fa-cogs")
                .badge("ACTIVE", "#10b981")
                .maxWidth("800px")
                .maxHeight("80vh")
                .open(true)
                .closeOnEsc(true)
                .closeOnClickOutside(true)
                .addBody(Span.of("Modal Body Content"))
                .addFooterAction(JettraFluxButton.of("Save", "fas fa-save").submit());

        assertNotNull(modal);
        assertEquals("testModal", modal.getId());

        String html = modal.render(Themes.FlatTheme());
        assertNotNull(html);
        assertTrue(html.contains("id=\"testModal\""));
        assertTrue(html.contains("Test Modal Title"));
        assertTrue(html.contains("Modal Subtitle Explanation"));
        assertTrue(html.contains("ACTIVE"));
        assertTrue(html.contains("Modal Body Content"));
        assertTrue(html.contains("window.JettraFluxModal"));
        assertTrue(html.contains("JettraFluxModal.close('testModal')"));
    }

    @Test
    @DisplayName("JettraFluxButton: verify variants, sizes, types, and click handlers")
    public void testJettraFluxButton() {
        JettraFluxButton primaryBtn = JettraFluxButton.of("Guardar", "fas fa-check")
                .id("btnSave")
                .variant(JettraFluxButton.Variant.PRIMARY)
                .size(JettraFluxButton.Size.MD)
                .submit()
                .badge("NEW")
                .onClickJs("console.log('Saved')");

        String primaryHtml = primaryBtn.render(Themes.FlatTheme());
        assertTrue(primaryHtml.contains("type=\"submit\""));
        assertTrue(primaryHtml.contains("Guardar"));
        assertTrue(primaryHtml.contains("fas fa-check"));
        assertTrue(primaryHtml.contains("NEW"));
        assertTrue(primaryHtml.contains("console.log('Saved')"));

        JettraFluxButton loadingBtn = JettraFluxButton.of("Procesando")
                .loading(true)
                .variant(JettraFluxButton.Variant.DANGER);

        String loadingHtml = loadingBtn.render(Themes.FlatTheme());
        assertTrue(loadingHtml.contains("fa-spinner fa-spin"));
    }

    @Test
    @DisplayName("JettraFluxDynamicForm: verify fields, polymorphic sections, and client script")
    public void testJettraFluxDynamicForm() {
        JettraFluxDynamicForm form = JettraFluxDynamicForm.of("engineForm", "/api/insert")
                .method("POST")
                .onSubmit("handleFormSubmit()")
                .onReset("handleFormReset()")
                .addField(TextField.of("common_name").value("Alpha"))
                .addSection("SECTION_A", Span.of("Content Section A"))
                .addSection("SECTION_B", Span.of("Content Section B"))
                .activeSection("SECTION_A")
                .addAction(JettraFluxButton.of("Submit").submit());

        assertNotNull(form);
        String html = form.render(Themes.FlatTheme());
        assertTrue(html.contains("id=\"engineForm\""));
        assertTrue(html.contains("action=\"/api/insert\""));
        assertTrue(html.contains("method=\"POST\""));
        assertTrue(html.contains("handleFormSubmit()"));
        assertTrue(html.contains("engineForm_section_SECTION_A"));
        assertTrue(html.contains("engineForm_section_SECTION_B"));
        assertTrue(html.contains("window.JettraFluxDynamicForm"));
        assertTrue(html.contains("switchSection: function"));
    }

    @Test
    @DisplayName("JettraFluxJsonEditor: verify validation status badge, formatting tools, and textarea")
    public void testJettraFluxJsonEditor() {
        String json = "{\n  \"status\": \"OK\",\n  \"count\": 10\n}";
        JettraFluxJsonEditor editor = JettraFluxJsonEditor.of("jsonEditor", "Config JSON", json)
                .name("config_payload")
                .height("180px")
                .showToolbar(true);

        assertNotNull(editor);
        String html = editor.render(Themes.FlatTheme());
        assertTrue(html.contains("id=\"jsonEditor_container\""));
        assertTrue(html.contains("Config JSON"));
        assertTrue(html.contains("VALID JSON"));
        assertTrue(html.contains("Prettify"));
        assertTrue(html.contains("Minify"));
        assertTrue(html.contains("window.JettraFluxJsonEditor"));
        assertTrue(html.contains("validate: function"));
        assertTrue(html.contains("format: function"));
    }

    @Test
    @DisplayName("JettraFluxNotification: verify alert types, dismiss button, and client controls")
    public void testJettraFluxNotification() {
        JettraFluxNotification notif = JettraFluxNotification.of("testNotif", "Operation completed successfully.", JettraFluxNotification.Type.SUCCESS)
                .title("Operación Exitosa")
                .dismissible(true)
                .visible(true);

        assertNotNull(notif);
        String html = notif.render(Themes.FlatTheme());
        assertTrue(html.contains("id=\"testNotif\""));
        assertTrue(html.contains("Operación Exitosa"));
        assertTrue(html.contains("Operation completed successfully."));
        assertTrue(html.contains("window.JettraFluxNotification"));
        assertTrue(html.contains("show: function"));
        assertTrue(html.contains("hide: function"));
    }
}
