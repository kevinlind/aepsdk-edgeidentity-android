/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.edge.identity;

import java.util.List;
import java.util.Map;

class Utils {

	private Utils() {}

	/**
	 * Checks if the {@code Map<String, Object>} provided is null or empty.
	 *
	 * @param map the {@code Map<String, Object>} to verify
	 * @return true if the {@code Map<String, Object>} provided is null or empty; false otherwise
	 */
	static boolean isNullOrEmpty(final Map<String, Object> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * Checks if the {@code List} provided is null or empty.
	 *
	 * @param list the {@code List} to verify
	 * @return true if the {@code List} provided is null or empty; false otherwise
	 */
	static boolean isNullOrEmpty(final List<?> list) {
		return list == null || list.isEmpty();
	}
}
