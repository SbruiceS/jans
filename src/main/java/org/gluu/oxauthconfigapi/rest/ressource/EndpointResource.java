package org.gluu.oxauthconfigapi.rest.ressource;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.couchbase.client.core.message.ResponseStatus;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import org.gluu.oxauth.model.configuration.AppConfiguration;
import org.gluu.oxtrust.service.JsonConfigurationService;
import org.gluu.oxauthconfigapi.filters.ProtectedApi;
import org.gluu.oxauthconfigapi.rest.model.ApiError;
import org.gluu.oxauthconfigapi.rest.model.Endpoint;
import org.gluu.oxauthconfigapi.util.ApiConstants;

@Path(ApiConstants.BASE_API_URL + ApiConstants.CONFIG + ApiConstants.PROPERTIES + ApiConstants.ENDPOINTS)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EndpointResource extends BaseResource {
	
	@Inject
	Logger log;
	
	@Inject
	JsonConfigurationService jsonConfigurationService;
	
	@GET
	@Operation(summary = "Gets oxAuth available endpoints.")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Endpoint.class, required = true, description = "Success"))),
			@APIResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class, required = false)) , description = "Unauthorized"),
			@APIResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Server error") })
	@ProtectedApi(scopes = { READ_ACCESS })
	public Response getAvailableEndpoints() {
		log.debug("EndpointResource::getAvailableEndpoints() - Retrieve oxAuth available endpoints.");
		try {
			
			AppConfiguration appConfiguration = this.jsonConfigurationService.getOxauthAppConfiguration();
			Endpoint endpoint = new Endpoint();
			endpoint.setBaseEndpoint(appConfiguration.getBaseEndpoint());
			endpoint.setAuthorizationEndpoint(appConfiguration.getAuthorizationEndpoint());
			endpoint.setTokenEndpoint(appConfiguration.getTokenEndpoint());
			endpoint.setTokenRevocationEndpoint(appConfiguration.getTokenRevocationEndpoint());
			endpoint.setUserInfoEndpoint(appConfiguration.getUserInfoEndpoint());
			endpoint.setClientInfoEndpoint(appConfiguration.getClientInfoEndpoint());
			endpoint.setEndSessionEndpoint(appConfiguration.getEndSessionEndpoint());
			endpoint.setRegistrationEndpoint(appConfiguration.getRegistrationEndpoint());
			endpoint.setOpenIdDiscoveryEndpoint(appConfiguration.getOpenIdDiscoveryEndpoint());
			endpoint.setOpenIdConfigurationEndpoint(appConfiguration.getOpenIdConfigurationEndpoint());
			endpoint.setIdGenerationEndpoint(appConfiguration.getIdGenerationEndpoint());
			endpoint.setIntrospectionEndpoint(appConfiguration.getIntrospectionEndpoint());
			endpoint.setUmaConfigurationEndpoint(appConfiguration.getUmaConfigurationEndpoint());
			endpoint.setOxElevenGenerateKeyEndpoint(appConfiguration.getOxElevenGenerateKeyEndpoint());
			endpoint.setBackchannelAuthenticationEndpoint(appConfiguration.getBackchannelAuthenticationEndpoint());
			endpoint.setBackchannelDeviceRegistrationEndpoint(appConfiguration.getBackchannelDeviceRegistrationEndpoint());
			
			return Response.ok(endpoint).build();
			
		}catch(Exception ex) {
			log.error("Failed to retrieve oxAuth available endpoints.", ex);
			return getInternalServerError(ex);		
		}
	}

	
	@PUT
	@Operation(summary = "Update oxAuth available endpoints.")
	@APIResponses( value = {
			@APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class, required = true, description = "Success"))),
			@APIResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ApiError.class, required = false)) , description = "Unauthorized"),
			@APIResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = ApiError.class)), description = "Server error") })
	@ProtectedApi(scopes = { WRITE_ACCESS })
	public Response updateAvailableEndpoints(@Valid Endpoint endpoint) {
		log.debug("EndpointResource::updateAvailableEndpoints() - Update oxAuth available endpoints.");
		try {
			
			AppConfiguration appConfiguration = this.jsonConfigurationService.getOxauthAppConfiguration();

			appConfiguration.setBaseEndpoint(endpoint.getBaseEndpoint());
			appConfiguration.setAuthorizationEndpoint(endpoint.getAuthorizationEndpoint());
			appConfiguration.setTokenEndpoint(endpoint.getTokenEndpoint());
			appConfiguration.setTokenRevocationEndpoint(endpoint.getTokenRevocationEndpoint());
			appConfiguration.setUserInfoEndpoint(endpoint.getUserInfoEndpoint());
			appConfiguration.setClientInfoEndpoint(endpoint.getClientInfoEndpoint());
			appConfiguration.setEndSessionEndpoint(endpoint.getEndSessionEndpoint());
			appConfiguration.setRegistrationEndpoint(endpoint.getRegistrationEndpoint());
			appConfiguration.setOpenIdDiscoveryEndpoint(endpoint.getOpenIdDiscoveryEndpoint());
			appConfiguration.setOpenIdConfigurationEndpoint(endpoint.getOpenIdConfigurationEndpoint());
			appConfiguration.setIdGenerationEndpoint(endpoint.getIdGenerationEndpoint());
			appConfiguration.setIntrospectionEndpoint(endpoint.getIntrospectionEndpoint());
			appConfiguration.setUmaConfigurationEndpoint(endpoint.getUmaConfigurationEndpoint());
			appConfiguration.setOxElevenGenerateKeyEndpoint(endpoint.getOxElevenGenerateKeyEndpoint());
			appConfiguration.setBackchannelAuthenticationEndpoint(endpoint.getBackchannelAuthenticationEndpoint());
			appConfiguration.setBackchannelDeviceRegistrationEndpoint(endpoint.getBackchannelDeviceRegistrationEndpoint());
			
			this.jsonConfigurationService.saveOxAuthAppConfiguration(appConfiguration);
			
			return Response.ok(ResponseStatus.SUCCESS).build();
			
		}catch(Exception ex) {
			log.error("Failed to update oxAuth available endpoints.", ex);
			return getInternalServerError(ex);		
		}
	}
}
