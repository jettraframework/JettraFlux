package io.jettra.flux.security;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Immutable Java 25 record representing an authenticated security principal.
 * Holds identity, assigned roles, and department context.
 */
public record SecurityPrincipal(
    String username,
    Set<String> roles,
    String department
) {
    public SecurityPrincipal {
        Objects.requireNonNull(username, "username must not be null");
        roles = (roles == null) ? Set.of() : Set.copyOf(roles);
        department = (department == null) ? "" : department.trim();
    }

    /**
     * Factory method creating a SecurityPrincipal from comma-separated roles or single role.
     */
    public static SecurityPrincipal of(String username, String role, String department) {
        Set<String> roleSet = (role == null || role.isBlank())
            ? Set.of()
            : Arrays.stream(role.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toUnmodifiableSet());
        return new SecurityPrincipal(username, roleSet, department);
    }

    public boolean hasRole(String targetRole) {
        if (targetRole == null || targetRole.isBlank()) {
            return false;
        }
        return roles.stream().anyMatch(r -> r.equalsIgnoreCase(targetRole.trim()));
    }

    public boolean hasAnyRole(Set<String> targetRoles) {
        if (targetRoles == null || targetRoles.isEmpty()) {
            return true;
        }
        return targetRoles.stream().anyMatch(this::hasRole);
    }
}
