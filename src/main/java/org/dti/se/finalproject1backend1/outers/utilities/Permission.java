package org.dti.se.finalproject1backend1.outers.utilities;

/**
 * Enum representing available permission types in the system.
 * Use this instead of hardcoded strings to prevent typos and improve maintainability.
 */
public enum Permission {
    SUPER_ADMIN("SUPER_ADMIN"),
    WAREHOUSE_ADMIN("WAREHOUSE_ADMIN"),
    CUSTOMER("CUSTOMER");

    private final String value;

    Permission(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
