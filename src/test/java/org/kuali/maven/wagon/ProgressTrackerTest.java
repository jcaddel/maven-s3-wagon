package org.kuali.maven.wagon;

import org.junit.Test;

public class ProgressTrackerTest {

    @Test
    public void testPercentCompleteTracker() {
        ProgressTracker tracker = new PercentCompleteTracker();
        tracker.setTotal(38);
        for (int i = 0; i < 38; i++) {
            tracker.increment();
        }
    }

}
