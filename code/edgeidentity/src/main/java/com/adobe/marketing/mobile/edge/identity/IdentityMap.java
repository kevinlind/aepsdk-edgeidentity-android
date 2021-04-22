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

import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines a map containing a set of end user identities, keyed on either namespace integration code
 * or the namespace ID of the identity. Within each namespace, the identity is unique. The values of
 * the map are an array, meaning that more than one identity of each namespace may be carried.
 *
 * @see <a
 *     href="https://github.com/adobe/xdm/blob/master/docs/reference/context/identitymap.schema.md">IdentityMap
 *     Schema</a>
 */
@SuppressWarnings("unused")
public class IdentityMap {
    private final Map<String, List<IdentityItem>> identityItems = new HashMap<>();

    /**
     * Gets the {@link IdentityItem}s for the namespace returns an empty list if no {@link
     * IdentityItem}s were found for the namespace
     *
     * @param namespace namespace for the id
     * @return IdentityItem for the namespace,
     */
    public List<IdentityItem> getIdentityItemsForNamespace(final String namespace) {
        final List<IdentityItem> copyItems = new ArrayList<>();

        if (Utils.isNullOrEmpty(namespace)) {
            return copyItems;
        }

        final List<IdentityItem> items = identityItems.get(namespace);

        if (items == null) {
            return copyItems;
        }

        for (IdentityItem item : items) {
            copyItems.add(new IdentityItem(item));
        }

        return copyItems;
    }

    /**
     * Returns a list of all the namespaces contained in this {@code IdentityMap}.
     *
     * @return a list of all the namespaces for this {@link IdentityMap}, or an empty string if this
     *     {@code IdentityMap} is empty
     */
    public List<String> getNamespaces() {
        return new ArrayList<>(identityItems.keySet());
    }

    /**
     * Add an identity item which is used to clearly distinguish entities that are interacting with
     * digital experiences.
     *
     * @param item {@link IdentityItem} to be added to the namespace
     * @param namespace the namespace integration code or namespace ID of the identity
     * @see <a
     *     href="https://github.com/adobe/xdm/blob/master/docs/reference/context/identityitem.schema.md">IdentityItem
     *     Schema</a>
     */
    public void addItem(final IdentityItem item, final String namespace) {
        addItem(item, namespace, false);
    }

    /**
     * Remove a single {@link IdentityItem} from this map.
     *
     * @param item {@link IdentityItem} to be added to the namespace
     * @param namespace The {@code IdentityItem} to remove from the given namespace
     */
    public void removeItem(final IdentityItem item, final String namespace) {
        if (item == null) {
            MobileCore.log(
                    LoggingMode.DEBUG,
                    LOG_TAG,
                    "IdentityMap remove item ignored as must contain a non-null IdentityItem.");
            return;
        }

        if (Utils.isNullOrEmpty(namespace)) {
            MobileCore.log(
                    LoggingMode.DEBUG,
                    LOG_TAG,
                    "IdentityMap remove item ignored as must contain a non-null/non-empty namespace.");
            return;
        }

        removeItemFromMap(item, namespace);
    }

    /**
     * Determines if this {@link IdentityMap} has no identities.
     *
     * @return {@code true} if this {@code IdentityMap} contains no identifiers
     */
    public boolean isEmpty() {
        return identityItems.isEmpty();
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append("{\"").append(IdentityConstants.XDMKeys.IDENTITY_MAP).append("\": {");

        for (Map.Entry<String, List<IdentityItem>> me : identityItems.entrySet()) {
            b.append("\"").append(me.getKey()).append("\": [");

            for (IdentityItem item : me.getValue()) {
                b.append(item).append(",");
            }

            if (!me.getValue().isEmpty()) {
                b.deleteCharAt(b.length() - 1);
            }

            b.append("],");
        }

        if (!identityItems.isEmpty()) {
            b.deleteCharAt(b.length() - 1);
        }

        b.append("}}");

        return b.toString();
    }

    // ========================================================================================
    // protected methods
    // ========================================================================================

    /**
     * Add an identity item which is used to clearly distinguish entities that are interacting with
     * digital experiences.
     *
     * @param item {@link IdentityItem} to be added to the namespace
     * @param namespace the namespace integration code or namespace ID of the identity
     * @param isFirstItem on {@code true} keeps the provided {@code IdentityItem} as the first
     *     element of the identity list for this namespace
     * @see <a
     *     href="https://github.com/adobe/xdm/blob/master/docs/reference/context/identityitem.schema.md">IdentityItem
     *     Schema</a>
     */
    void addItem(final IdentityItem item, final String namespace, final boolean isFirstItem) {
        if (item == null) {
            MobileCore.log(
                    LoggingMode.DEBUG,
                    LOG_TAG,
                    "IdentityMap - add item ignored as must contain a non-null IdentityItem.");
            return;
        }

        if (Utils.isNullOrEmpty(namespace)) {
            MobileCore.log(
                    LoggingMode.DEBUG,
                    LOG_TAG,
                    "IdentityMap - add item ignored as must contain a non-null/non-empty namespace.");
            return;
        }

        addItemToMap(item, namespace, isFirstItem);
    }

    /**
     * Merge the given map on to this {@link IdentityMap}. Any {@link IdentityItem} in map which
     * shares the same namespace and id as an item in this {@code IdentityMap} will replace that
     * {@code IdentityItem}.
     *
     * @param map {@link IdentityMap} to be merged into this object
     */
    void merge(final IdentityMap map) {
        if (map == null) {
            return;
        }

        for (final String namespace : map.identityItems.keySet()) {
            for (IdentityItem identityItem : map.identityItems.get(namespace)) {
                addItem(identityItem, namespace);
            }
        }
    }

    /**
     * Remove identities present in passed in map from this {@link IdentityMap}. Identities are
     * removed which match the same namespace and id.
     *
     * @param map Identities to remove from this {@code IdentityMap}
     */
    void remove(final IdentityMap map) {
        if (map == null) {
            return;
        }

        for (final String namespace : map.identityItems.keySet()) {
            for (IdentityItem identityItem : map.identityItems.get(namespace)) {
                removeItem(identityItem, namespace);
            }
        }
    }

    /**
     * Removes all the {@link IdentityItem} on this {@link IdentityMap} linked to the specified
     * namespace (case insensitive)
     *
     * @return a {@code boolean} representing a successful removal of all {@code IdentityItem} in a
     *     provided namespace
     */
    boolean clearItemsForNamespace(final String namespace) {
        if (namespace == null) {
            return false;
        }

        boolean isRemoved = false;
        final List<String> filteredNamespaces = new ArrayList<>();

        for (final String eachNamespace : identityItems.keySet()) {
            if (namespace.equalsIgnoreCase(eachNamespace)) {
                isRemoved = true;
                filteredNamespaces.add(eachNamespace);
            }
        }

        for (final String eachNamespace : filteredNamespaces) {
            identityItems.remove(eachNamespace);
        }

        return isRemoved;
    }

    /**
     * Use this method to cast the {@link IdentityMap} as {@link Map<String,Object>} to be passed as
     * EventData for an SDK Event. This method returns an empty map if the {@code IdentityMap}
     * contains no data
     *
     * @return {@code Map} representation of xdm formatted IdentityMap
     */
    Map<String, Object> asXDMMap() {
        return asXDMMap(true);
    }

    /**
     * Use this method to cast the {@link IdentityMap} as {@link Map<String,Object>} to be passed as
     * EventData for an SDK Event.
     *
     * @param allowEmpty If false and if this {@code IdentityMap} contains no data, then returns a
     *     map with empty xdmFormatted Identity Map. If true and if this {@code IdentityMap}
     *     contains no data, then returns an empty map
     * @return {@code Map} representation of xdm formatted IdentityMap
     */
    Map<String, Object> asXDMMap(final boolean allowEmpty) {
        final Map<String, Object> xdmMap = new HashMap<>();
        final Map<String, List<Map<String, Object>>> identityMap = new HashMap<>();

        for (String namespace : identityItems.keySet()) {
            final List<Map<String, Object>> namespaceIds = new ArrayList<>();

            for (IdentityItem identityItem : identityItems.get(namespace)) {
                namespaceIds.add(identityItem.toObjectMap());
            }

            identityMap.put(namespace, namespaceIds);
        }

        if (!identityMap.isEmpty() || !allowEmpty) {
            xdmMap.put(IdentityConstants.XDMKeys.IDENTITY_MAP, identityMap);
        }

        return xdmMap;
    }

    /**
     * Creates an {@link IdentityMap} from the given xdm formatted {@link Map} Returns null if the
     * provided map is null/empty. Return null if the provided map is not in Identity Map's XDM
     * format.
     *
     * @return {@link Map<String,Object>} XDM format representation of IdentityMap
     */
    static IdentityMap fromXDMMap(final Map<String, Object> map) {
        if (Utils.isNullOrEmpty(map)) {
            return null;
        }

        final Map<String, Object> identityMapDict =
                (HashMap<String, Object>) map.get(IdentityConstants.XDMKeys.IDENTITY_MAP);

        if (identityMapDict == null) {
            return null;
        }

        final IdentityMap identityMap = new IdentityMap();

        for (final String namespace : identityMapDict.keySet()) {
            try {
                final ArrayList<HashMap<String, Object>> idArr =
                        (ArrayList<HashMap<String, Object>>) identityMapDict.get(namespace);

                for (Object idMap : idArr) {
                    final IdentityItem item = IdentityItem.fromData((Map<String, Object>) idMap);

                    if (item != null) {
                        identityMap.addItemToMap(item, namespace, false);
                    }
                }
            } catch (ClassCastException e) {
                MobileCore.log(
                        LoggingMode.DEBUG, LOG_TAG, "Failed to create IdentityMap from data.");
            }
        }

        return identityMap;
    }

    // ========================================================================================
    // private methods
    // ========================================================================================

    private void addItemToMap(
            final IdentityItem newItem, final String namespace, final boolean isFirstItem) {
        // check if namespace exists
        final List<IdentityItem> itemList;

        if (identityItems.containsKey(namespace)) {
            itemList = identityItems.get(namespace);
        } else {
            itemList = new ArrayList<>();
        }

        // Check if the item already exist in the current ItemList
        int index = itemList.indexOf(newItem);

        if (index >= 0) {
            itemList.set(index, newItem);
        } else if (isFirstItem) {
            itemList.add(0, newItem);
        } else {
            itemList.add(newItem);
        }

        identityItems.put(namespace, itemList);
    }

    private void removeItemFromMap(final IdentityItem item, final String namespace) {
        // check if namespace exists
        if (!identityItems.containsKey(namespace)) {
            return;
        }

        final List<IdentityItem> itemList = identityItems.get(namespace);
        itemList.remove(item);

        if (itemList.isEmpty()) {
            identityItems.remove(namespace);
        }
    }
}
