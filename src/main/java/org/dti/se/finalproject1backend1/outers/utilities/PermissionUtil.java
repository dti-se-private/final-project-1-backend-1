package org.dti.se.finalproject1backend1.outers.utilities;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.entities.AccountPermission;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PermissionUtil {

    private PermissionUtil() {
        // Utility class, prevent instantiation
    }

    /**
     * Extract permission strings from account as a List.
     * Note: This creates a new list on each call. For repeated checks,
     * consider using hasPermission() or hasAnyPermission() instead.
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
     * Extract permission strings from account as a Set for efficient lookups.
     *
     * @param account The account to extract permissions from
     * @return Set of permission strings
     */
    public static Set<String> getPermissionsAsSet(Account account) {
        return account
                .getAccountPermissions()
                .stream()
                .map(AccountPermission::getPermission)
                .collect(Collectors.toSet());
    }

    /**
     * Check if account has a specific permission.
     * Optimized to use stream's anyMatch for early termination.
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
     * Check if account has a specific permission using the Permission enum.
     *
     * @param account    The account to check
     * @param permission The permission to check for
     * @return true if account has the permission
     */
    public static boolean hasPermission(Account account, Permission permission) {
        return hasPermission(account, permission.getValue());
    }

    /**
     * Check if account has any of the specified permissions.
     * Optimized to use Set lookup with O(n) complexity where n is account permissions.
     *
     * @param account     The account to check
     * @param permissions The permissions to check for
     * @return true if account has any of the permissions
     */
    public static boolean hasAnyPermission(Account account, String... permissions) {
        // Convert to Set for O(1) lookup instead of O(m) per permission
        Set<String> permissionSet = Set.of(permissions);
        return account
                .getAccountPermissions()
                .stream()
                .anyMatch(p -> permissionSet.contains(p.getPermission()));
    }

    /**
     * Check if account is a super admin.
     *
     * @param account The account to check
     * @return true if account is a super admin
     */
    public static boolean isSuperAdmin(Account account) {
        return hasPermission(account, Permission.SUPER_ADMIN);
    }

    /**
     * Check if account is a warehouse admin.
     *
     * @param account The account to check
     * @return true if account is a warehouse admin
     */
    public static boolean isWarehouseAdmin(Account account) {
        return hasPermission(account, Permission.WAREHOUSE_ADMIN);
    }

    /**
     * Check if account is a customer.
     *
     * @param account The account to check
     * @return true if account is a customer
     */
    public static boolean isCustomer(Account account) {
        return hasPermission(account, Permission.CUSTOMER);
    }
}
