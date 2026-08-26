package io.jettra.flux.pages;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;
import io.jettra.server.JettraServer;
import io.jettra.server.core.JettraContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class FluxBaseHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        
        // --- Security Filter ---
        boolean isNoLogin = this.getClass().isAnnotationPresent(io.jettra.core.login.NoLoginRequired.class);
        
        if (!isNoLogin) {
            String username = getLoggedUser(exchange);
            if (username == null || username.isEmpty()) {
                io.jettra.flux.widgets.Modal modal = io.jettra.flux.widgets.Modal.of(
                    io.jettra.flux.widgets.Column.of(
                        io.jettra.flux.widgets.Label.of("Sesión Requerida").modifier(new io.jettra.flux.core.Modifier().cssClass("bold").padding(10)),
                        io.jettra.flux.widgets.Paragraph.of("Su sesión ha expirado o no ha iniciado sesión. Redirigiendo al login en 3 segundos..."),
                        io.jettra.flux.widgets.Paragraph.of("<script>setTimeout(function(){ window.location.href='" + io.jettra.server.JettraServer.resolvePath("/login") + "'; }, 3000);</script>")
                    )
                );
                modal.open(true);
                
                String themeName = getThemeCookie(exchange);
                if (themeName == null || themeName.isEmpty()) themeName = "Ast";
                io.jettra.flux.theme.ThemeData theme = getThemeByName(themeName);
                
                String html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                        + "<link rel=\"stylesheet\" href=\"/static/font-awesome/css/all.min.css\">\n"
                        + "<link rel=\"stylesheet\" href=\"/static/bootstrap-icons/font/bootstrap-icons.css\">\n"
                        + "<link rel=\"stylesheet\" href=\"/static/material-icons/material-symbols.css\">\n"
                        + theme.generateGlobalCss()
                        + "</head><body style=\"margin:0; padding:0; box-sizing: border-box;\">"
                        + io.jettra.flux.widgets.Scaffold.of().body(modal).render(theme)
                        + "</body></html>";
                        
                renderResponse(exchange, html, 401);
                return;
            }
            
            // Verificación de @PageWidgetAllow
            if (this.getClass().isAnnotationPresent(io.jettra.core.security.widget.PageWidgetAllow.class)) {
                io.jettra.core.security.widget.PageWidgetAllow pageAllow = this.getClass().getAnnotation(io.jettra.core.security.widget.PageWidgetAllow.class);
                String userRole = getLoggedRole(exchange);
                String userDept = getLoggedDepartment(exchange);
                boolean hasAccess = false;
                java.util.List<String> requiredRoles = new java.util.ArrayList<>();
                try {
                    Object[] pageRolesArr = (Object[]) pageAllow.role();
                    for (Object r : pageRolesArr) {
                        requiredRoles.add((r instanceof Enum) ? ((Enum<?>) r).name() : String.valueOf(r));
                    }
                } catch (java.lang.annotation.AnnotationTypeMismatchException e) {
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("AppRole\\.([A-Z0-9_]+)").matcher(e.getMessage());
                    while (m.find()) {
                        requiredRoles.add(m.group(1));
                    }
                }

                if (requiredRoles.isEmpty()) {
                    hasAccess = true;
                } else {
                    for (String rName : requiredRoles) {
                        if (rName.equalsIgnoreCase(userRole) || checkSynonym(rName, userRole)) {
                            hasAccess = true;
                            break;
                        }
                    }
                }
                if (hasAccess && !pageAllow.department().isEmpty()) {
                    if (!pageAllow.department().equalsIgnoreCase(userDept)) {
                        hasAccess = false;
                    }
                }
                if (!hasAccess) {
                    String allowedRolesStr = String.join(",", requiredRoles);
                    io.jettra.flux.widgets.Modal modal = io.jettra.flux.widgets.Modal.of(
                        io.jettra.flux.widgets.Column.of(
                            io.jettra.flux.widgets.Label.of("Acceso Denegado").modifier(new io.jettra.flux.core.Modifier().cssClass("bold").padding(10)),
                            io.jettra.flux.widgets.Paragraph.of("No tienes los permisos (" + allowedRolesStr + ") necesarios para ver esta página."),
                            io.jettra.flux.widgets.Paragraph.of("<button onclick=\"window.location.href='" + io.jettra.server.JettraServer.resolvePath("/login") + "'\" style=\"padding: 10px 20px; background: #3b82f6; color: white; border: none; border-radius: 5px; cursor: pointer; margin-top: 15px;\">Cerrar</button>")
                        )
                    );
                    modal.open(true);
                    String themeName = getThemeCookie(exchange);
                    if (themeName == null || themeName.isEmpty()) themeName = "Ast";
                    io.jettra.flux.theme.ThemeData theme = getThemeByName(themeName);
                    String html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                            + "<link rel=\"stylesheet\" href=\"/static/font-awesome/css/all.min.css\">\n"
                            + "<link rel=\"stylesheet\" href=\"/static/bootstrap-icons/font/bootstrap-icons.css\">\n"
                            + "<link rel=\"stylesheet\" href=\"/static/material-icons/material-symbols.css\">\n"
                            + theme.generateGlobalCss()
                            + "</head><body style=\"margin:0; padding:0; box-sizing: border-box;\">"
                            + io.jettra.flux.widgets.Scaffold.of().body(modal).render(theme)
                            + "</body></html>";
                    renderResponse(exchange, html, 403);
                    return;
                }
            }
        }
        // --- Fin Security Filter ---
        
        Map<String, String> params = new HashMap<>(parseQueryParams(exchange.getRequestURI().getQuery()));
        
        if ("true".equals(params.get("_jtSyncCheck"))) {
            handleSyncCheck(exchange, params);
            return;
        }
        
        if (params.containsKey("change_lang")) {
            io.jettra.flux.core.LanguageFlux.changeLanguage(exchange, params.get("change_lang"));
            String path = exchange.getRequestURI().getPath();
            String cPath = io.jettra.server.JettraServer.getContextPath();
            String relPath = path;
            if (cPath != null && path.startsWith(cPath)) {
                relPath = path.substring(cPath.length());
            }
            if (relPath.isEmpty()) relPath = "/";
            redirect(exchange, relPath);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            params.putAll(parseRequestBody(exchange));
            
            // Verificación de @ActionWidgetAllow (Invocación de Método)
            if (params.containsKey("_action_method")) {
                String methodName = params.get("_action_method");
                try {
                    java.lang.reflect.Method method = this.getClass().getDeclaredMethod(methodName, HttpExchange.class, Map.class);
                    if (method.isAnnotationPresent(io.jettra.core.security.widget.ActionWidgetAllow.class)) {
                        io.jettra.core.security.widget.ActionWidgetAllow actionAllow = method.getAnnotation(io.jettra.core.security.widget.ActionWidgetAllow.class);
                        String userRole = getLoggedRole(exchange);
                        String userDept = getLoggedDepartment(exchange);
                        boolean hasAccess = false;
                        java.util.List<String> actionRoles = new java.util.ArrayList<>();
                        try {
                            Object[] actionRolesArr = (Object[]) actionAllow.role();
                            for (Object r : actionRolesArr) {
                                actionRoles.add((r instanceof Enum) ? ((Enum<?>) r).name() : String.valueOf(r));
                            }
                        } catch (java.lang.annotation.AnnotationTypeMismatchException e) {
                            java.util.regex.Matcher m = java.util.regex.Pattern.compile("AppRole\\.([A-Z0-9_]+)").matcher(e.getMessage());
                            while (m.find()) {
                                actionRoles.add(m.group(1));
                            }
                        }

                        if (actionRoles.isEmpty()) {
                            hasAccess = true;
                        } else {
                            for (String rName : actionRoles) {
                                if (rName.equalsIgnoreCase(userRole) || checkSynonym(rName, userRole)) {
                                    hasAccess = true;
                                    break;
                                }
                            }
                        }
                        if (hasAccess && !actionAllow.department().isEmpty()) {
                            if (!actionAllow.department().equalsIgnoreCase(userDept)) {
                                hasAccess = false;
                            }
                        }
                        if (!hasAccess) {
                            String errorHtml = "<script>alert('Acceso Denegado al Método: " + methodName + "'); window.history.back();</script>";
                            renderResponse(exchange, errorHtml, 403);
                            return;
                        }
                    }
                    method.setAccessible(true);
                    method.invoke(this, exchange, params);
                    return; // Si el método se invoca con éxito, asume que manejó la redirección.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (onPost(exchange, params)) {
                return; // If onPost returns true, it handled the response (e.g. redirect)
            }
        } else {
            if (onGet(exchange, params)) {
                return; // If onGet returns true, it handled the response
            }
        }

        // Render UI
        String themeName = getThemeCookie(exchange);
        if (themeName == null || themeName.isEmpty()) {
            themeName = "Ast"; // Default theme
        }
        
        io.jettra.flux.theme.ThemeData theme = getThemeByName(themeName);
        Widget ui = buildUI(exchange, params, themeName);
        if (ui != null) {
            StringBuilder syncJs = new StringBuilder();
            injectSyncLogic(syncJs);
            injectSecurityHeartbeat(syncJs);
            
            String html = "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n" +
                          "<meta charset=\"UTF-8\">\n" +
                          "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                          "<title>" + getTitle() + "</title>\n" +
                          "<link rel=\"stylesheet\" href=\"/static/font-awesome/css/all.min.css\">\n" +
                          "<link rel=\"stylesheet\" href=\"/static/bootstrap-icons/font/bootstrap-icons.css\">\n" +
                          "<link rel=\"stylesheet\" href=\"/static/material-icons/material-symbols.css\">\n" +
                          theme.generateGlobalCss() + "\n" +
                          syncJs.toString() + "\n" +
                          "</head>\n<body style=\"margin: 0; padding: 0; box-sizing: border-box;\">\n" +
                          ui.render(theme) + "\n" +
                          "<div id='j-sync-popup-container'></div>\n" +
                          "</body>\n</html>";
            renderResponse(exchange, html, 200);
        }
    }

    protected abstract Widget buildUI(HttpExchange exchange, Map<String, String> params, String currentTheme);

    protected String getTitle() {
        return "JettraFlux App";
    }

    protected boolean onGet(HttpExchange exchange, Map<String, String> params) throws IOException {
        return false;
    }

    protected boolean onPost(HttpExchange exchange, Map<String, String> params) throws IOException {
        return false;
    }

    protected void redirect(HttpExchange exchange, String path) throws IOException {
        exchange.getResponseHeaders().set("Location", JettraServer.resolvePath(path));
        exchange.sendResponseHeaders(302, -1);
        exchange.getResponseBody().close();
    }

    protected void renderResponse(HttpExchange exchange, String html, int statusCode) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    protected void setSessionCookie(HttpExchange exchange, String username, String role, String department) {
        String cPath = JettraServer.getContextPath();
        if (cPath == null || cPath.isEmpty()) cPath = "/";
        exchange.getResponseHeaders().set("Set-Cookie", "username=" + username + "; Path=" + cPath);
        exchange.getResponseHeaders().add("Set-Cookie", "role=" + role + "; Path=" + cPath);
        exchange.getResponseHeaders().add("Set-Cookie", "department=" + department + "; Path=" + cPath);
        JettraContext.getCurrent().set(JettraContext.Scope.SESSION, "username", username);
        JettraContext.getCurrent().set(JettraContext.Scope.SESSION, "role", role);
        JettraContext.getCurrent().set(JettraContext.Scope.SESSION, "department", department);
    }

    protected void setSessionCookie(HttpExchange exchange, String username, String role) {
        setSessionCookie(exchange, username, role, "");
    }

    protected void setSessionCookie(HttpExchange exchange, String username) {
        setSessionCookie(exchange, username, "USER", "");
    }

    protected void clearSessionCookie(HttpExchange exchange) {
        String cPath = JettraServer.getContextPath();
        if (cPath == null || cPath.isEmpty()) cPath = "/";
        exchange.getResponseHeaders().set("Set-Cookie", "username=; Path=" + cPath + "; Max-Age=0");
        exchange.getResponseHeaders().add("Set-Cookie", "role=; Path=" + cPath + "; Max-Age=0");
        exchange.getResponseHeaders().add("Set-Cookie", "department=; Path=" + cPath + "; Max-Age=0");
        if (JettraContext.getCurrent() != null) {
            JettraContext.getCurrent().set(JettraContext.Scope.SESSION, "username", "");
            JettraContext.getCurrent().set(JettraContext.Scope.SESSION, "role", "");
            JettraContext.getCurrent().set(JettraContext.Scope.SESSION, "department", "");
            JettraContext.getCurrent().set(JettraContext.Scope.SESSION, "credentialFlux", "");
        }
    }

    protected String getLoggedUser(HttpExchange exchange) {
        if (JettraContext.getCurrent() != null) {
            Object userObj = JettraContext.getCurrent().get(JettraContext.Scope.SESSION, "username");
            if (userObj != null && !userObj.toString().trim().isEmpty()) {
                return userObj.toString();
            }
        }
        // Simple cookie parse
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies != null) {
            for (String c : cookies.split(";")) {
                c = c.trim();
                if (c.startsWith("username=")) {
                    return c.substring("username=".length());
                }
            }
        }
        return null;
    }

    protected String getLoggedRole(HttpExchange exchange) {
        if (JettraContext.getCurrent() != null) {
            Object roleObj = JettraContext.getCurrent().get(JettraContext.Scope.SESSION, "role");
            if (roleObj != null && !roleObj.toString().trim().isEmpty()) {
                return roleObj.toString();
            }
        }
        // Simple cookie parse for role
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies != null) {
            for (String c : cookies.split(";")) {
                c = c.trim();
                if (c.startsWith("role=")) {
                    return c.substring("role=".length());
                }
            }
        }
        return "USER";
    }

    protected String getLoggedDepartment(HttpExchange exchange) {
        if (JettraContext.getCurrent() != null) {
            Object deptObj = JettraContext.getCurrent().get(JettraContext.Scope.SESSION, "department");
            if (deptObj != null) {
                return deptObj.toString();
            }
        }
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies != null) {
            for (String c : cookies.split(";")) {
                c = c.trim();
                if (c.startsWith("department=")) {
                    return c.substring("department=".length());
                }
            }
        }
        return "";
    }

    protected String getThemeCookie(HttpExchange exchange) {
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies != null) {
            for (String c : cookies.split(";")) {
                c = c.trim();
                if (c.startsWith("jettra_theme=")) {
                    return c.substring("jettra_theme=".length());
                }
            }
        }
        return null;
    }

    protected io.jettra.flux.theme.ThemeData getThemeByName(String name) {
        // Trigger initialization
        try { Class.forName("io.jettra.flux.theme.Themes"); } catch (Exception ignored) {}
        
        io.jettra.flux.theme.ThemeData theme = io.jettra.flux.theme.ThemeRegistry.getTheme(name);
        if (theme != null) {
            return theme;
        }
        return io.jettra.flux.theme.Themes.AstTheme();
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            int eqIdx = pair.indexOf('=');
            try {
                if (eqIdx >= 0) {
                    map.put(URLDecoder.decode(pair.substring(0, eqIdx), StandardCharsets.UTF_8),
                            URLDecoder.decode(pair.substring(eqIdx + 1), StandardCharsets.UTF_8));
                } else if (!pair.isBlank()) {
                    map.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
                }
            } catch (Exception ignored) {}
        }
        return map;
    }

    private Map<String, String> parseRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
        }
        String formData = sb.toString();
        Map<String, String> map = new HashMap<>();
        if (formData.isEmpty()) return map;
        for (String pair : formData.split("&")) {
            int eqIdx = pair.indexOf('=');
            try {
                if (eqIdx >= 0) {
                    map.put(URLDecoder.decode(pair.substring(0, eqIdx), StandardCharsets.UTF_8),
                            URLDecoder.decode(pair.substring(eqIdx + 1), StandardCharsets.UTF_8));
                } else if (!pair.isBlank()) {
                    map.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
                }
            } catch (Exception ignored) {}
        }
        return map;
    }

    private void handleSyncCheck(HttpExchange exchange, Map<String, String> params) throws IOException {
        long startTime = io.jettra.flux.sync.JettraSyncManager.SERVER_START_TIME;
        io.jettra.flux.sync.JettraPageSincronized syncAnno = this.getClass().getAnnotation(io.jettra.flux.sync.JettraPageSincronized.class);
        
        if (syncAnno == null) {
            renderResponse(exchange, String.format("{\"serverStartTime\": %d}", startTime), 200);
            return;
        }
        String entity = syncAnno.entity().isEmpty() ? this.getClass().getSimpleName().replace("Page", "Model") : syncAnno.entity();
        io.jettra.flux.sync.JettraSyncManager.SyncInfo info = io.jettra.flux.sync.JettraSyncManager.getLastChange(entity);
        
        if (info == null) {
            renderResponse(exchange, String.format("{\"serverStartTime\": %d}", startTime), 200);
        } else {
            String json = String.format("{\"type\":\"%s\", \"user\":\"%s\", \"ts\":%d, \"serverStartTime\": %d}", 
                info.type, info.user, info.timestamp, startTime);
            renderResponse(exchange, json, 200);
        }
    }

    private void injectSecurityHeartbeat(StringBuilder builder) {
        long startTime = io.jettra.flux.sync.JettraSyncManager.SERVER_START_TIME;
        String loginPath = io.jettra.server.JettraServer.resolvePath("/login");
        
        builder.append("<script>\n")
               .append("  const J_SERVER_START_TIME = ").append(startTime).append(";\n")
               .append("  const J_LOGIN_PATH = '").append(loginPath).append("';\n")
               .append("  \n")
               .append("  function checkJettraHeartbeat() {\n")
               .append("    if (window.location.pathname === J_LOGIN_PATH) return;\n")
               .append("    \n")
               .append("    fetch(window.location.pathname + '?_jtSyncCheck=true')\n")
               .append("      .then(r => {\n")
               .append("        if (!r.ok) throw new Error('Server down');\n")
               .append("        return r.json();\n")
               .append("      })\n")
               .append("      .then(data => {\n")
               .append("        if (data.serverStartTime && data.serverStartTime !== J_SERVER_START_TIME) {\n")
               .append("           console.warn('Server restarted, redirecting to login...');\n")
               .append("           window.location.href = J_LOGIN_PATH;\n")
               .append("        }\n")
               .append("      })\n")
               .append("      .catch(err => {\n")
               .append("        console.error('JettraHeartbeat Error:', err);\n")
               .append("        window.location.href = J_LOGIN_PATH;\n")
               .append("      });\n")
               .append("  }\n")
               .append("  \n")
               .append("  setInterval(checkJettraHeartbeat, 10000);\n")
               .append("</script>\n");
    }

    private void injectSyncLogic(StringBuilder builder) {
        io.jettra.flux.sync.JettraPageSincronized syncAnno = this.getClass().getAnnotation(io.jettra.flux.sync.JettraPageSincronized.class);
        if (syncAnno == null) return;

        String entity = syncAnno.entity().isEmpty() ? this.getClass().getSimpleName().replace("Page", "Model") : syncAnno.entity();
        long loadTime = System.currentTimeMillis();

        builder.append("<script>\n")
               .append("  const J_SYNC_ENTITY = '").append(entity).append("';\n")
               .append("  const J_SYNC_LOAD_TIME = ").append(loadTime).append(";\n")
               .append("  const J_SYNC_TYPE = '").append(syncAnno.value()).append("';\n")
               .append("  \n")
               .append("  function checkJettraSync() {\n")
               .append("    fetch(window.location.pathname + '?_jtSyncCheck=true')\n")
               .append("      .then(r => r.json())\n")
               .append("      .then(data => {\n")
               .append("        if (data.ts && data.ts > J_SYNC_LOAD_TIME) {\n")
               .append("          if (J_SYNC_TYPE === 'ALL' || J_SYNC_TYPE === data.type) {\n")
               .append("            showJettraSyncPopup(data);\n")
               .append("          }\n")
               .append("        }\n")
               .append("      });\n")
               .append("  }\n")
               .append("  \n")
               .append("  function showJettraSyncPopup(data) {\n")
               .append("    const container = document.getElementById('j-sync-popup-container');\n")
               .append("    if (container.innerHTML !== '') return;\n")
               .append("    \n")
               .append("    let actionText = '';\n")
               .append("    switch(data.type) {\n")
               .append("        case 'CREATE': actionText = 'creó un nuevo registro'; break;\n")
               .append("        case 'UPDATE': actionText = 'actualizó los datos'; break;\n")
               .append("        case 'DELETE': actionText = 'eliminó un registro'; break;\n")
               .append("        case 'MOVE': actionText = 'movió un elemento'; break;\n")
               .append("        default: actionText = 'realizó cambios';\n")
               .append("    }\n")
               .append("    \n")
               .append("    const msg = `${data.user} ${actionText}. ¿Desea actualizar la vista?`;\n")
               .append("    const popup = document.createElement('div');\n")
               .append("    popup.className = 'j-sync-popup-3d';\n")
               .append("    popup.innerHTML = `\n")
               .append("      <div class='j-sync-content' style='background: rgba(30, 41, 59, 0.9); backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; padding: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.5); color: white; display: flex; gap: 15px; align-items: center; position: fixed; bottom: 20px; right: 20px; z-index: 9999;'>\n")
               .append("        <div class='j-sync-icon' style='font-size: 24px;'>📡</div>\n")
               .append("        <div class='j-sync-text'>\n")
               .append("          <strong style='display:block; margin-bottom:5px; font-size:16px;'>Sincronización</strong>\n")
               .append("          <p style='margin:0; font-size:14px; color:#cbd5e1;'>${msg}</p>\n")
               .append("        </div>\n")
               .append("        <div class='j-sync-actions' style='display: flex; gap: 10px; margin-left: 10px;'>\n")
               .append("          <button onclick='window.location.reload()' style='background: #3b82f6; color: white; border: none; padding: 8px 12px; border-radius: 6px; cursor: pointer; font-weight: 600;'>Sincronizar</button>\n")
               .append("          <button onclick='this.closest(\".j-sync-popup-3d\").remove()' style='background: rgba(255,255,255,0.1); color: white; border: none; padding: 8px 12px; border-radius: 6px; cursor: pointer;'>Cerrar</button>\n")
               .append("        </div>\n")
               .append("      </div>\n")
               .append("    `;\n")
               .append("    container.appendChild(popup);\n")
               .append("  }\n")
               .append("  \n")
               .append("  setInterval(checkJettraSync, 5000);\n")
               .append("</script>\n");
    }

    private boolean checkSynonym(String pluginRole, String userRole) {
        if (userRole == null || userRole.isEmpty()) return false;
        try {
            java.io.InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("plugin-config.json");
            if (is != null) {
                String content = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                String pRoleStr = "\"plugin-role\"\\s*:\\s*\"" + java.util.regex.Pattern.quote(pluginRole) + "\"";
                String pSecRoleStr = "\"plugin-security-role\"\\s*:\\s*\"" + java.util.regex.Pattern.quote(pluginRole) + "\"";
                String aRoleStr = "\"applicative-role\"\\s*:\\s*\"" + java.util.regex.Pattern.quote(userRole) + "\"";
                String aRoleStrLegacy = "\"application-role\"\\s*:\\s*\"" + java.util.regex.Pattern.quote(userRole) + "\"";
                String aSecRoleStr = "\"applicative-security-role\"\\s*:\\s*\"" + java.util.regex.Pattern.quote(userRole) + "\"";

                String[] blocks = content.split("\\{");
                for (String block : blocks) {
                    boolean matchPlugin = java.util.regex.Pattern.compile(pRoleStr).matcher(block).find() ||
                                          java.util.regex.Pattern.compile(pSecRoleStr).matcher(block).find();
                    boolean matchApp = java.util.regex.Pattern.compile(aRoleStr).matcher(block).find() ||
                                       java.util.regex.Pattern.compile(aRoleStrLegacy).matcher(block).find() ||
                                       java.util.regex.Pattern.compile(aSecRoleStr).matcher(block).find();
                    if (matchPlugin && matchApp) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            // ignore
        }
        return false;
    }
}
