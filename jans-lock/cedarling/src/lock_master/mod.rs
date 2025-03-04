use std::{
	borrow::Cow,
	collections::{BTreeMap, HashSet},
	sync::OnceLock,
};
use wasm_bindgen::prelude::*;
use web_sys::*;

use crate::{
	crypto,
	http::{self, ResponseEx},
	startup::types::PolicyStoreConfig,
};

mod sse;
pub mod types;

#[wasm_bindgen]
extern "C" {
	#[wasm_bindgen(js_name = btoa)]
	pub fn js_btoa(input: &str) -> String;
}

// Stores a status list referencing each JWT's `jti` claim
pub static mut STATUS_LISTS: OnceLock<BTreeMap<String, (u8, HashSet<String>)>> = OnceLock::new();

pub async fn init<'a, T: serde::de::DeserializeOwned>(policy_store_config: &PolicyStoreConfig, application_name: &str, decompress: bool) -> T {
	let PolicyStoreConfig::LockMaster {
		url,
		policy_store_id,
		enable_dynamic_configuration,
		ssa_jwt,
	} = policy_store_config
	else {
		unreachable!("We arrive here from the PolicyStoreConfig::LockMaster")
	};

	// Get LockMasterConfig
	let url = format!("{}/.well-known/lock-master-configuration", url);
	let res = http::get(&url, &[]).await.expect_throw("Unable to fetch LockMasterConfig from URL");
	let lock_master_config: types::LockMasterConfig = res.into_json().await.unwrap_throw();


	// init sse updates
	if *enable_dynamic_configuration {
		sse::init(&lock_master_config.lock_sse_uri);
	}

	// Get OAuthConfig
	let res = http::get(&lock_master_config.oauth_as_well_known, &[]).await.expect_throw("Unable to fetch LockMasterConfig from URL");
	let openid_config = res.into_json::<types::OAuthConfig>().await.unwrap_throw();
	let iss = crypto::decode::get_issuer(ssa_jwt).expect_throw("SSA_JWT lacks `iss` field");

	let client: types::OAuthDynamicClient = {
		// OpenID dynamic client registration
		let client_req = types::OAuthDynamicClientRequest {
			client_name: application_name,
			application_type: "web",
			grant_types: &["client_credentials"],
			redirect_uris: &[&iss],
			token_endpoint_auth_method: "client_secret_basic",
			software_statement: ssa_jwt,
			contacts: &["newton@gluu.org"],
		};

		// send
		let url = openid_config.registration_endpoint.expect_throw("No registration endpoint found, issuer doesn't support DCR");
		let res = http::post(&url, http::PostBody::Json(client_req), &[]).await.expect_throw("Unable to register client");
		res.into_json().await.expect_throw("Unable to parse client registration response")
	};

	let grant: types::OAuthGrantResponse = {
		// https://docs.jans.io/v1.1.2/admin/auth-server/endpoints/token/
		let token = if let Some(client_secret) = client.client_secret {
			format!("{}:{}", client.client_id, client_secret)
		} else {
			console::warn_1(&JsValue::from_str("Client Secret is not provided"));
			client.client_id
		};

		let grant = types::OAuthGrantRequest {
			scope: &["https://jans.io/oauth/scopes/cedarling", "https://jans.io/oauth/scopes/lock_sse"],
			grant_type: "client_credentials authorization_code",
		};

		// send
		let auth = format!("Basic {}", js_btoa(&token));
		let headers = [("Authorization", auth.as_str())];
		let res = http::post(&openid_config.token_endpoint, http::PostBody::Form(&grant), &headers)
			.await
			.expect_throw("Unable to get Access Token");

		res.into_json().await.expect_throw("Unable to parse Access Token Response")
	};

	let buffer = {
		let url = format!("{}?policy_store_format=json&policy_store_id={}", lock_master_config.config_uri, policy_store_id);
		let auth = format!("Bearer {}", grant.access_token);

		let res = http::get(&url, &[("Authorization", &auth)]).await.expect_throw("Unable to fetch policies from remote location");
		let buffer = res.into_bytes().await.expect_throw("Unable to convert response to bytes");

		match decompress {
			true => Cow::Owned(miniz_oxide::inflate::decompress_to_vec_zlib(&buffer).unwrap_throw()),
			false => Cow::Owned(buffer),
		}
	};

	serde_json::from_slice(&buffer).unwrap_throw()
}
