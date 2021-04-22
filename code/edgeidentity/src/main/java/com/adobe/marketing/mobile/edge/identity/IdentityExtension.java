/*
Copyright 2021 Adobe. All rights reserved.
This file is licensed to you under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. You may obtain a copy
of the License at http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
OF ANY KIND, either express or implied. See the License for the specific language
governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.edge.identity;

import static com.adobe.marketing.mobile.edge.identity.IdentityConstants.LOG_TAG;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import java.util.Map;

class IdentityExtension extends Extension {
private final IdentityState state = new IdentityState(new IdentityProperties());

/**
* Constructor.
*
* <p>Called during the Identity extension's registration. The following listeners are registered
* during this extension's registration.
*
* <ul>
*   <li>Listener {@link ListenerEventHubBoot} to listen for event with eventType {@link
*       IdentityConstants.EventType#HUB} and EventSource {@link
*       IdentityConstants.EventSource#BOOTED}
*   <li>Listener {@link ListenerEdgeIdentityRequestIdentity} to listen for event with eventType
*       {@link IdentityConstants.EventType#EDGE_IDENTITY} and EventSource {@link
*       IdentityConstants.EventSource#REQUEST_IDENTITY}
*   <li>Listener {@link ListenerGenericIdentityRequestContent} to listen for event with eventType
*       {@link IdentityConstants.EventType#GENERIC_IDENTITY} and EventSource {@link
*       IdentityConstants.EventSource#REQUEST_CONTENT}
*   <li>Listener {@link ListenerEdgeIdentityUpdateIdentity} to listen for event with eventType
*       {@link IdentityConstants.EventType#EDGE_IDENTITY} and EventSource {@link
*       IdentityConstants.EventSource#UPDATE_IDENTITY}
*   <li>Listener {@link ListenerEdgeIdentityRemoveIdentity} to listen for event with eventType
*       {@link IdentityConstants.EventType#EDGE_IDENTITY} and EventSource {@link
*       IdentityConstants.EventSource#REMOVE_IDENTITY}
*   <li>Listener {@link ListenerIdentityRequestReset} to listen for event with eventType {@link
*       IdentityConstants.EventType#EDGE_IDENTITY} and EventSource {@link
*       IdentityConstants.EventSource#REQUEST_CONTENT}
*   <li>Listener {@link ListenerIdentityRequestReset} to listen for event with eventType {@link
*       IdentityConstants.EventType#GENERIC_IDENTITY} and EventSource {@link
*       IdentityConstants.EventSource#REQUEST_RESET}
*   <li>Listener {@link ListenerHubSharedState} to listen for event with eventType {@link
*       IdentityConstants.EventType#HUB} and EventSource {@link
*       IdentityConstants.EventSource#SHARED_STATE}
* </ul>
*
* <p>Thread : Background thread created by MobileCore
*
* @param extensionApi {@link ExtensionApi} instance
*/
protected IdentityExtension(ExtensionApi extensionApi) {
	super(extensionApi);
	ExtensionErrorCallback<ExtensionError> listenerErrorCallback =
		new ExtensionErrorCallback<ExtensionError>() {
		@Override
		public void error(final ExtensionError extensionError) {
			MobileCore.log(
				LoggingMode.ERROR,
				LOG_TAG,
				String.format(
					"Failed to register listener, error: %s", extensionError.getErrorName()));
		}
		};

	extensionApi.registerEventListener(
		IdentityConstants.EventType.HUB,
		IdentityConstants.EventSource.BOOTED,
		ListenerEventHubBoot.class,
		listenerErrorCallback);
	extensionApi.registerEventListener(
		IdentityConstants.EventType.EDGE_IDENTITY,
		IdentityConstants.EventSource.REQUEST_IDENTITY,
		ListenerEdgeIdentityRequestIdentity.class,
		listenerErrorCallback);
	extensionApi.registerEventListener(
		IdentityConstants.EventType.GENERIC_IDENTITY,
		IdentityConstants.EventSource.REQUEST_CONTENT,
		ListenerGenericIdentityRequestContent.class,
		listenerErrorCallback);
	extensionApi.registerEventListener(
		IdentityConstants.EventType.EDGE_IDENTITY,
		IdentityConstants.EventSource.UPDATE_IDENTITY,
		ListenerEdgeIdentityUpdateIdentity.class,
		listenerErrorCallback);
	extensionApi.registerEventListener(
		IdentityConstants.EventType.EDGE_IDENTITY,
		IdentityConstants.EventSource.REMOVE_IDENTITY,
		ListenerEdgeIdentityRemoveIdentity.class,
		listenerErrorCallback);
	extensionApi.registerEventListener(
		IdentityConstants.EventType.HUB,
		IdentityConstants.EventSource.SHARED_STATE,
		ListenerHubSharedState.class,
		listenerErrorCallback);
	extensionApi.registerEventListener(
		IdentityConstants.EventType.GENERIC_IDENTITY,
		IdentityConstants.EventSource.REQUEST_RESET,
		ListenerIdentityRequestReset.class,
		listenerErrorCallback);
	state.bootUp();
}

/**
* Required override. Each extension must have a unique name within the application.
*
* @return unique name of this extension
*/
@Override
protected String getName() {
	return IdentityConstants.EXTENSION_NAME;
}

/**
* Optional override.
*
* @return the version of this extension
*/
@Override
protected String getVersion() {
	return IdentityConstants.EXTENSION_VERSION;
}

/**
* Call this method with the EventHub's Boot event to handle the boot operation of the {@code
* Identity} Extension.
*
* <p>On boot share the initial identities loaded from persistence to XDM shared state.
*
* @param event the boot {@link Event}
*/
void handleEventHubBoot(final Event event) {

	// share the initial XDMSharedState on bootUp
	final Map currentIdentities = state.getIdentityProperties().toXDMData(false);

	if (currentIdentities == null || currentIdentities.isEmpty()) {
	MobileCore.log(
		LoggingMode.WARNING,
		LOG_TAG,
		"IdentityExtension - Nothing loaded from persistence for initial Identity XDM shared state on boot");
	return;
	}

	shareIdentityXDMSharedState(event);
}

/**
* Handles update identity requests to add/update customer identifiers.
*
* @param event the edge update identity {@link Event}
*/
void handleUpdateIdentities(final Event event) {
	final Map<String, Object> eventData =
		event.getEventData(); // do not need to null check on eventData, as they are done
	// on listeners
	final IdentityMap map = IdentityMap.fromXDMMap(eventData);

	if (map == null) {
	MobileCore.log(
		LoggingMode.DEBUG,
		LOG_TAG,
		"IdentityExtension - Failed to update identifiers as no identifiers were found in the event data.");
	return;
	}

	state.updateCustomerIdentifiers(map);
	shareIdentityXDMSharedState(event);
}

/**
* Handles remove identity requests to remove customer identifiers.
*
* @param event the edge remove identity request {@link Event}
*/
void handleRemoveIdentity(final Event event) {
	final Map<String, Object> eventData =
		event.getEventData(); // do not need to null check on eventData, as they are done
	// on listeners
	final IdentityMap map = IdentityMap.fromXDMMap(eventData);

	if (map == null) {
	MobileCore.log(
		LoggingMode.DEBUG,
		LOG_TAG,
		"IdentityExtension - Failed to remove identifiers as no identifiers were found in the event data.");
	return;
	}

	state.removeCustomerIdentifiers(map);
	shareIdentityXDMSharedState(event);
}

void handleGenericIdentityRequest(final Event event) {
	// TODO
}

/**
* Handles events of type {@code com.adobe.eventType.hub} and source {@code
* com.adobe.eventSource.sharedState}. If the state change event is for the direct Identity
* extension, get the direct Identity shared state and attempt to update the legacy ECID with the
* direct Identity extension ECID.
*
* @param event an event of type {@code com.adobe.eventType.hub} and source {@code
*     com.adobe.eventSource.sharedState}
*/
void handleHubSharedState(final Event event) {
	if (event == null || event.getEventData() == null) {
	return;
	}

	try {
	final String stateOwner =
		(String) event.getEventData().get(IdentityConstants.EventDataKeys.STATE_OWNER);

	if (!IdentityConstants.SharedStateKeys.IDENTITY_DIRECT.equals(stateOwner)) {
		return;
	}
	} catch (ClassCastException e) {
	MobileCore.log(
		LoggingMode.DEBUG,
		LOG_TAG,
		"IdentityExtension - Could not process direct Identity shared state change event, failed to parse event state owner as String: "
			+ e.getLocalizedMessage());
	return;
	}

	final Map<String, Object> identityState =
		getSharedState(IdentityConstants.SharedStateKeys.IDENTITY_DIRECT, event);

	if (identityState == null) {
	MobileCore.log(
		LoggingMode.DEBUG,
		LOG_TAG,
		"IdentityExtension - Could not process direct Identity shared state change event, Identity shared state is null");
	return;
	}

	try {
	final String legacyEcidString =
		(String) identityState.get(IdentityConstants.EventDataKeys.VISITOR_ID_ECID);
	final ECID legacyEcid = legacyEcidString == null ? null : new ECID(legacyEcidString);

	if (state.updateLegacyExperienceCloudId(legacyEcid)) {
		shareIdentityXDMSharedState(event);
	}
	} catch (ClassCastException e) {
	MobileCore.log(
		LoggingMode.DEBUG,
		LOG_TAG,
		"IdentityExtension - Could not process direct Identity shared state change event, failed to parse stored ECID as String: "
			+ e.getLocalizedMessage());
	}
}

/**
* Handles events requesting for identifiers. Dispatches response event containing the
* identifiers. Called by listener registered with event hub.
*
* @param event the identity request {@link Event}
*/
void handleIdentityRequest(final Event event) {
	Map<String, Object> xdmData = state.getIdentityProperties().toXDMData(false);
	Event responseEvent =
		new Event.Builder(
				IdentityConstants.EventNames.IDENTITY_RESPONSE_CONTENT_ONE_TIME,
				IdentityConstants.EventType.EDGE_IDENTITY,
				IdentityConstants.EventSource.RESPONSE_IDENTITY)
			.setEventData(xdmData)
			.build();

	MobileCore.dispatchResponseEvent(
		responseEvent,
		event,
		new ExtensionErrorCallback<ExtensionError>() {
		@Override
		public void error(ExtensionError extensionError) {
			MobileCore.log(
				LoggingMode.DEBUG,
				LOG_TAG,
				"IdentityExtension - Failed to dispatch Edge Identity response event for event "
					+ event.getUniqueIdentifier()
					+ " with error "
					+ extensionError.getErrorName());
		}
		});
}

/**
* Handles Edge Identity request reset events.
*
* @param event the identity request reset {@link Event}
*/
void handleRequestReset(final Event event) {
	state.resetIdentifiers();
	shareIdentityXDMSharedState(event);

	// dispatch reset complete event
	final Event responseEvent =
		new Event.Builder(
				IdentityConstants.EventNames.RESET_IDENTITIES_COMPLETE,
				IdentityConstants.EventType.EDGE_IDENTITY,
				IdentityConstants.EventSource.RESET_COMPLETE)
			.build();

	MobileCore.dispatchEvent(
		responseEvent,
		new ExtensionErrorCallback<ExtensionError>() {
		@Override
		public void error(ExtensionError extensionError) {
			MobileCore.log(
				LoggingMode.DEBUG,
				LOG_TAG,
				"IdentityExtension - Failed to dispatch Edge Identity reset response event for event "
					+ event.getUniqueIdentifier()
					+ " with error "
					+ extensionError.getErrorName());
		}
		});
}

/**
* Retrieves the shared state for the given state owner
*
* @param stateOwner the state owner for the requested shared state
* @param event the {@link Event} for which is shared state is to be retrieved
*/
private Map<String, Object> getSharedState(final String stateOwner, final Event event) {
	final ExtensionApi extensionApi = getApi();

	if (extensionApi == null) {
	return null;
	}

	final ExtensionErrorCallback<ExtensionError> getSharedStateCallback =
		new ExtensionErrorCallback<ExtensionError>() {
		@Override
		public void error(final ExtensionError extensionError) {
			MobileCore.log(
				LoggingMode.DEBUG,
				LOG_TAG,
				String.format(
					"IdentityExtension - Failed getting direct Identity shared state. Error : %s.",
					extensionError.getErrorName()));
		}
		};

	return extensionApi.getSharedEventState(stateOwner, event, getSharedStateCallback);
}

/**
* Fetches the latest Identity properties and shares the XDMSharedState.
*
* @param event the {@link Event} that triggered the XDM shared state change
*/
private void shareIdentityXDMSharedState(final Event event) {
	final ExtensionApi extensionApi = super.getApi();

	if (extensionApi == null) {
	MobileCore.log(
		LoggingMode.DEBUG,
		LOG_TAG,
		"IdentityExtension - ExtensionApi is null, unable to share XDM shared state for reset identities");
	return;
	}

	// set the shared state
	final ExtensionErrorCallback<ExtensionError> errorCallback =
		new ExtensionErrorCallback<ExtensionError>() {
		@Override
		public void error(final ExtensionError extensionError) {
			MobileCore.log(
				LoggingMode.DEBUG,
				LOG_TAG,
				String.format(
					"IdentityExtension - Failed create XDM shared state. Error : %s.",
					extensionError.getErrorName()));
		}
		};

	extensionApi.setXDMSharedEventState(
		state.getIdentityProperties().toXDMData(false), event, errorCallback);
}
}
