/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.xdi.oxauth.model.common;

import org.xdi.oxauth.model.configuration.AppConfiguration;
import org.xdi.oxauth.model.registration.Client;

/**
 * The client credentials (or other forms of client authentication) can be used
 * as an authorization grant when the authorization scope is limited to the
 * protected resources under the control of the client, or to protected
 * resources previously arranged with the authorization server. Client
 * credentials are used as an authorization grant typically when the client is
 * acting on its own behalf (the client is also the resource owner), or is
 * requesting access to protected resources based on an authorization previously
 * arranged with the authorization server.
 *
 * @author Javier Rojas Blum Date: 09.29.2011
 */
public class ClientCredentialsGrant extends AuthorizationGrant {

    /**
     * Construct a client credentials grant.
     *
     * @param user   The resource owner.
     * @param client An application making protected resource requests on behalf of
     *               the resource owner and with its authorization.
     */
    public ClientCredentialsGrant(User user, Client client, AppConfiguration appConfiguration) {
        super(user, AuthorizationGrantType.CLIENT_CREDENTIALS, client, null, appConfiguration);
    }

    /**
     * The authorization server MUST NOT issue a refresh token.
     */
    @Override
    public RefreshToken createRefreshToken() {
        throw new UnsupportedOperationException(
                "The authorization server MUST NOT issue a refresh token.");
    }
}