package org.dti.se.finalproject1backend1.outers.utilities;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;

import java.util.List;

public class PermissionUtil {

    private PermissionUtil() {
        // Utility class, prevent instantiation
    }

    /**
     * Extract permission strings from account.
     * Caches the result in a local variable to avoid repeated stream operations.
     *
     * @param account The account to extract permissions from
     * @return List of permission strings
     */
    public static List<String> getPermissions(Account account) {
        return account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .toList();
    }

    /**
     * Check if account has a specific permission.
     *
     * @param account    The account to check
     * @param permission The permission to check for
     * @return true if account has the permission
     */
    public static boolean hasPermission(Account account, String permission) {
        return account
                .getAccountPermissions()
                .stream()
                .anyMatch(p -> permission.equals(p.getPermission()));
    }

    /**
     * Check if account has any of the specified permissions.
     *
     * @param account     The account to check
     * @param permissions The permissions to check for
     * @return true if account has any of the permissions
     */
    public static boolean hasAnyPermission(Account account, String... permissions) {
        List<String> accountPermissions = getPermissions(account);
        for (String permission : permissions) {
            if (accountPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if account is a super admin.
     *
     * @param account The account to check
     * @return true if account is a super admin
     */
    public static boolean isSuperAdmin(Account account) {
        return hasPermission(account, "SUPER_ADMIN");
    }

    /**
     * Check if account is a warehouse admin.
     *
     * @param account The account to check
     * @return true if account is a warehouse admin
     */
    public static boolean isWarehouseAdmin(Account account) {
        return hasPermission(account, "WAREHOUSE_ADMIN");
    }

    /**
     * Check if account is a customer.
     *
     * @param account The account to check
     * @return true if account is a customer
     */
    public static boolean isCustomer(Account account) {
        return hasPermission(account, "CUSTOMER");
    }
}
