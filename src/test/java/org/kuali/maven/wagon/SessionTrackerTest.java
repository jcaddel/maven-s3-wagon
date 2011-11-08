package org.kuali.maven.wagon;

import junit.framework.Assert;

import org.junit.Test;

public class SessionTrackerTest {

	@Test
	public void simple() {
		SessionTracker tracker = new SessionTracker();
		long millis = 1;
		tracker.setOpened(millis);
		Assert.assertEquals(millis, tracker.getOpened());
	}

}
