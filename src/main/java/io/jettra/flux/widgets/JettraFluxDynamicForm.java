package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JettraFluxDynamicForm - Fluent dynamic form builder for JettraFlux.
 * Supports polymorphic sub-sections (for dynamically swapping forms based on engine types),
 * event handlers (onSubmit, onReset), field groups, and reactive async submission.
 */
public class JettraFluxDynamicForm extends Widget {

    private String formId;
    private String actionUrl = "";
    private String method = "POST";
    private String jsOnSubmit = null;
    private String jsOnReset = null;

    private final List<Widget> commonFields = new ArrayList<>();
    private final Map<String, Widget> polymorphicSections = new LinkedHashMap<>();
    private String initialActiveSection = null;
    private final List<Widget> actionButtons = new ArrayList<>();

    private JettraFluxDynamicForm(String formId) {
        this.formId = formId;
        this.id = formId;
    }

    public static JettraFluxDynamicForm of(String formId) {
        return new JettraFluxDynamicForm(formId);
    }

    public static JettraFluxDynamicForm of(String formId, String actionUrl) {
        JettraFluxDynamicForm f = new JettraFluxDynamicForm(formId);
        f.actionUrl = actionUrl;
        return f;
    }

    public JettraFluxDynamicForm action(String actionUrl) {
        this.actionUrl = actionUrl;
        return this;
    }

    public JettraFluxDynamicForm method(String method) {
        this.method = method;
        return this;
    }

    public JettraFluxDynamicForm onSubmit(String jsOnSubmit) {
        this.jsOnSubmit = jsOnSubmit;
        return this;
    }

    public JettraFluxDynamicForm onReset(String jsOnReset) {
        this.jsOnReset = jsOnReset;
        return this;
    }

    public JettraFluxDynamicForm addField(Widget field) {
        if (field != null) commonFields.add(field);
        return this;
    }

    public JettraFluxDynamicForm addFields(Widget... fields) {
        if (fields != null) {
            for (Widget f : fields) {
                if (f != null) commonFields.add(f);
            }
        }
        return this;
    }

    public JettraFluxDynamicForm addSection(String sectionKey, Widget sectionWidget) {
        if (sectionKey != null && sectionWidget != null) {
            polymorphicSections.put(sectionKey.toUpperCase(), sectionWidget);
            if (initialActiveSection == null) {
                initialActiveSection = sectionKey.toUpperCase();
            }
        }
        return this;
    }

    public JettraFluxDynamicForm activeSection(String sectionKey) {
        if (sectionKey != null) {
            this.initialActiveSection = sectionKey.toUpperCase();
        }
        return this;
    }

    public JettraFluxDynamicForm addAction(Widget button) {
        if (button != null) actionButtons.add(button);
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();

        sb.append("<form id=\"").append(formId).append("\" ");
        if (actionUrl != null && !actionUrl.isBlank()) sb.append("action=\"").append(actionUrl).append("\" ");
        sb.append("method=\"").append(method).append("\" ");

        if (jsOnSubmit != null && !jsOnSubmit.isBlank()) {
            sb.append("onsubmit=\"").append(jsOnSubmit).append("; return false;\" ");
        }
        if (jsOnReset != null && !jsOnReset.isBlank()) {
            sb.append("onreset=\"").append(jsOnReset).append(";\" ");
        }

        sb.append("class=\"jettra-flux-dynamic-form ").append(modifier != null ? modifier.getClasses() : "").append("\" ");
        sb.append("style=\"display:flex; flex-direction:column; gap:14px; ")
          .append(modifier != null ? modifier.getStyles() : "").append("\">\n");

        // Common Fields
        for (Widget f : commonFields) {
            sb.append(f.render(theme)).append("\n");
        }

        // Polymorphic Sections container
        if (!polymorphicSections.isEmpty()) {
            sb.append("  <div id=\"").append(formId).append("_polymorphic_container\" class=\"jettra-flux-polymorphic-sections\">\n");
            for (Map.Entry<String, Widget> entry : polymorphicSections.entrySet()) {
                String sectionKey = entry.getKey();
                Widget widget = entry.getValue();
                boolean isActive = sectionKey.equalsIgnoreCase(initialActiveSection);
                String display = isActive ? "block" : "none";

                sb.append("    <div id=\"").append(formId).append("_section_").append(sectionKey)
                  .append("\" class=\"jettra-flux-form-section\" data-section=\"").append(sectionKey).append("\" ")
                  .append("style=\"display:").append(display).append(";\">\n");
                sb.append(widget.render(theme)).append("\n");
                sb.append("    </div>\n");
            }
            sb.append("  </div>\n");
        }

        // Actions / Buttons footer
        if (!actionButtons.isEmpty()) {
            sb.append("  <div class=\"jettra-flux-form-actions\" style=\"display:flex; justify-content:flex-end; align-items:center; gap:8px; margin-top:8px;\">\n");
            for (Widget btn : actionButtons) {
                sb.append(btn.render(theme)).append("\n");
            }
            sb.append("  </div>\n");
        }

        sb.append("</form>\n");

        // Dynamic form switcher client script
        sb.append("<script>\n")
          .append("if (!window.JettraFluxDynamicForm) {\n")
          .append("  window.JettraFluxDynamicForm = {\n")
          .append("    switchSection: function(formId, sectionKey) {\n")
          .append("      var key = (sectionKey || '').toUpperCase();\n")
          .append("      var form = document.getElementById(formId);\n")
          .append("      if (!form) return;\n")
          .append("      var sections = form.querySelectorAll('.jettra-flux-form-section');\n")
          .append("      sections.forEach(function(sec) {\n")
          .append("        if (sec.getAttribute('data-section') === key) {\n")
          .append("          sec.style.display = 'block';\n")
          .append("        } else {\n")
          .append("          sec.style.display = 'none';\n")
          .append("        }\n")
          .append("      });\n")
          .append("    },\n")
          .append("    reset: function(formId) {\n")
          .append("      var form = document.getElementById(formId);\n")
          .append("      if (form) form.reset();\n")
          .append("    }\n")
          .append("  };\n")
          .append("}\n")
          .append("</script>\n");

        return sb.toString();
    }
}
