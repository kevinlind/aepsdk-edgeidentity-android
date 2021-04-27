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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.MobileCore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class ListenerEdgeIdentityRemoveIdentityTests {
	@Mock
	private IdentityExtension mockIdentityExtension;

	private ListenerEdgeIdentityRemoveIdentity listener;
	private ExecutorService testExecutor;

	@Before
	public void setup() {
		testExecutor = Executors.newSingleThreadExecutor();
		mockIdentityExtension = Mockito.mock(IdentityExtension.class);
		doReturn(testExecutor).when(mockIdentityExtension).getExecutor();
		MobileCore.start(null);
		listener =
			spy(
				new ListenerEdgeIdentityRemoveIdentity(
					null,
					IdentityConstants.EventType.EDGE_IDENTITY,
					IdentityConstants.EventSource.REMOVE_IDENTITY
				)
			);
	}

	@Test
	public void testHear() throws Exception {
		// setup
		Event event = new Event.Builder(
			"Remove Identity",
			IdentityConstants.EventType.EDGE_IDENTITY,
			IdentityConstants.EventSource.REMOVE_IDENTITY
		)
		.build();
		doReturn(mockIdentityExtension).when(listener).getIdentityExtension();

		// test
		listener.hear(event);

		// verify
		testExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
		verify(mockIdentityExtension, times(1)).processAddEvent(event);
	}

	@Test
	public void testHear_WhenParentExtensionNull() throws Exception {
		// setup
		Event event = new Event.Builder(
			"Remove Identity",
			IdentityConstants.EventType.EDGE_IDENTITY,
			IdentityConstants.EventSource.REMOVE_IDENTITY
		)
		.build();
		doReturn(null).when(listener).getIdentityExtension();

		// test
		listener.hear(event);

		// verify
		testExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
		verify(mockIdentityExtension, times(0)).processAddEvent(any(Event.class));
	}

	@Test
	public void testHear_WhenEventNull() throws Exception {
		// setup
		doReturn(null).when(listener).getIdentityExtension();
		doReturn(mockIdentityExtension).when(listener).getIdentityExtension();

		// test
		listener.hear(null);

		// verify
		testExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
		verify(mockIdentityExtension, times(0)).processAddEvent(any(Event.class));
	}
}
