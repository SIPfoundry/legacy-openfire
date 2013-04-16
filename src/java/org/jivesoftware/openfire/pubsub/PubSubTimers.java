package org.jivesoftware.openfire.pubsub;

import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.util.JiveGlobals;

public class PubSubTimers {

	private static PubSubTimers instance;

	private static Timer flushTimer;

	private static Timer purgeTimer = new Timer(
			"Pubsub purge stale items timer");
	private static long purgeTimerDelay = JiveGlobals.getIntProperty(
			"xmpp.pubsub.purge.timer", 300) * 1000;

	private static final int MAX_ITEMS_FLUSH = JiveGlobals.getIntProperty(
			"xmpp.pubsub.flush.max", 1000);

	public PubSubTimers(final BasePubSubProvider provider) {
		if (MAX_ITEMS_FLUSH > 0) {
			flushTimer = new Timer("Pubsub item flush timer");

			long flushTimerDelay = JiveGlobals.getIntProperty(
					"xmpp.pubsub.flush.timer", 120) * 1000;

			// Enforce a min of 20s
			if (flushTimerDelay < 20000) {
				flushTimerDelay = 20000;
			}

			flushTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					provider.flushPendingItems(false); // this member only
				}
			}, flushTimerDelay, flushTimerDelay);
		}

		// Enforce a min of 20s
		if (purgeTimerDelay < 60000) {
			purgeTimerDelay = 60000;
		}

		purgeTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				provider.purgeItems();
			}
		}, purgeTimerDelay, purgeTimerDelay);

	}

	public static PubSubTimers getInstance(BasePubSubProvider provider) {
		if(instance == null) {
			synchronized (new byte[0]) {
				if(instance == null) {
					instance = new PubSubTimers(provider);
				}
			}
		}

		return instance;
	}
}
