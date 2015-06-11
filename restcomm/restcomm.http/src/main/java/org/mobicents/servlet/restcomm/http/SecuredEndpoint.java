package org.mobicents.servlet.restcomm.http;

import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.permission.WildcardPermissionResolver;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mobicents.servlet.restcomm.entities.Account;
import org.mobicents.servlet.restcomm.entities.shiro.ShiroResources;
import org.mobicents.servlet.restcomm.http.keycloak.KeycloakClient;

/**
 *
 * @author orestis.tsakiridis@telestax.com
 *
 */
public abstract class SecuredEndpoint extends AbstractEndpoint {
    protected KeycloakClient keycloakClient;
    protected static RestcommRoles restcommRoles;

    public static final String SECURITY_MODE = "cloud"; // TODO move this to the restcomm configuration

    public SecuredEndpoint() {
        super();
    }

    protected void init(final Configuration configuration) {
        super.init(configuration);
        ShiroResources shiroResources = ShiroResources.getInstance();
        restcommRoles = shiroResources.get(RestcommRoles.class);
        keycloakClient = new KeycloakClient(request);
    }

    // Throws an authorization exception in case the user does not have the permission OR does not own (or is a parent) the account
    protected void secure(final Account account, final String permission) throws AuthorizationException {
        secureKeycloak(account, permission, getKeycloakAccessToken());
    }

    protected void secureApi(String neededPermissionString) {
        if ( SECURITY_MODE.equals("standalone") ) {
            secureApiShiro(neededPermissionString);
        } else {
            secureApiKeycloak(neededPermissionString);
        }
    }

    private void secureApiKeycloak(String neededPermissionString) {
        final AccessToken accessToken = getKeycloakAccessToken();
        secureApi(neededPermissionString, accessToken);
    }

    private void secureApiShiro(String neededPermissionString) {
        // TODO implement with shiro. For now all requests allowed
        logger.info("secureApi: Using standalone security mode" );
    }

    // check if the user with the roles in accessToken can access has the following permissions (on the API)
    private void secureApi(String neededPermissionString, final AccessToken accessToken) {
        // normalize the permission string
        neededPermissionString = "domain:" + neededPermissionString;

        Set<String> roleNames;
        try {
            roleNames = accessToken.getRealmAccess().getRoles();
        } catch (NullPointerException e) {
            throw new UnauthorizedException("No access token present or no roles in it");
        }

        // no need to check permissions for users with the RestcommAdmin role
        if ( roleNames.contains("RestcommAdmin") ) {
            return;
        }

        WildcardPermissionResolver resolver = new WildcardPermissionResolver();
        Permission neededPermission = resolver.resolvePermission(neededPermissionString);
        // build the authorization token
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo(roleNames);

        // check the neededPermission against all roles of the user
        for (String roleName: roleNames) {
            SimpleRole simpleRole = restcommRoles.getRole(roleName);
            if ( simpleRole == null) {
                // logger.warn("Cannot map keycloak role '" + roleName + "' to local restcomm configuration. Ignored." );
            }
            else {
                Set<Permission> permissions = simpleRole.getPermissions();
                // check the permissions one by one
                for (Permission permission: permissions) {
                    if (permission.implies(neededPermission)) {
                        logger.debug("Granted access by permission " + permission.toString());
                        return;
                    }
                }
                logger.debug("Role " + roleName + " does not allow " + neededPermissionString);
            }
        }
        throw new AuthorizationException();
    }
    private void secureKeycloak(final Account account, final String neededPermissionString, final AccessToken accessToken) {
        secureApi(neededPermissionString, accessToken);
        // check if the logged user has access to the account that is operated upon
        secureByAccount(accessToken, account);
    }



    protected String getLoggedUsername() {
        if ( SECURITY_MODE.equals("standalone") ) {
            return getLoggedUsernameShiro();
        } else {
            return getLoggedUsernameKeycloak();
        }
    }

    private String getLoggedUsernameShiro() {
        // TODO put some propert implementation here
        logger.warn("getLoggedUsernameShiro: Returnig hardcoded value for ousername" );
        return "otsakir";
    }

    private String getLoggedUsernameKeycloak() {
        KeycloakSecurityContext session = (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
        if (session.getToken() != null) {
            return session.getToken().getPreferredUsername();
        }
        return null;
    }

    /* make sure the token bearer can access data that belong to this account. In its simplest form this means that the username in the token
     * is the same as the account username. When the organization concepts are implemented and hierarchical accounts are created a smarter
     * approach that will allow parant users access the resources of their children should be employed.
     */
    private void secureByAccount(final AccessToken accessToken, final Account account) {
        // load logged user's account
        Account loggedAccount = accountsDao.getAccount(accessToken.getPreferredUsername());
        if ( loggedAccount != null && loggedAccount.getSid() != null ) {
            // check if loggedAccount is the same as the checked account or is parent of it
            if ( loggedAccount.getSid().equals(account.getSid()) || loggedAccount.getSid().equals(account.getAccountSid()) ) {
                // no op
            } else {
                throw new UnauthorizedException("User cannot access resources for the specified account.");
            }
        }
    }


    protected void secureByAccount(final Account account) {
        if ( SECURITY_MODE.equals("standalone") ) {
            secureByAccountShiro(account);
        } else {
            secureByAccountKeycloak(account);
        }
    }

    private void secureByAccountKeycloak(final Account account) {
        final AccessToken accessToken = getKeycloakAccessToken();
        secureByAccount(accessToken, account);
    }

    private void secureByAccountShiro(final Account account) {
        // TODO add a propert implementation
        logger.warn("secureByAccountShiro: missing implementation" );
    }

    // does the accessToken contain the role?
    /*
    protected void secureByRole(final AccessToken accessToken, String role) {
        Set<String> roleNames;
        try {
            roleNames = accessToken.getRealmAccess().getRoles();
        } catch (NullPointerException e) {
            throw new UnauthorizedException("No access token present or no roles in it");
        }

        if (roleNames.contains(role))
            return;
        else
            throw new UnauthorizedException("Role "+role+" is missing from token");
    }*/

    private AccessToken getKeycloakAccessToken() {
        KeycloakSecurityContext session = (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
        AccessToken accessToken = session.getToken();
        return accessToken;
    }

}
