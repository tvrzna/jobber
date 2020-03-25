package cz.tvrzna.jobber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;

import cz.tvrzna.jobber.jobs.TestJob;

public class ScheduledItemTest
{
	@Test
	public void testGetLastRun()
	{
		ScheduledItem item = new ScheduledItem(new TestJob(), "0 * * * * *");

		final Date now = new Date();

		item.setLastRun(now);
		assertEquals(now, item.getLastRun());
	}

	@Test
	public void testGetNextRun()
	{
		ScheduledItem item = new ScheduledItem(new TestJob(), "0 * * * * *");

		final Date now = new Date();

		item.setNextRun(now);
		assertEquals(now, item.getNextRun());
	}

	@Test
	public void testGetJob()
	{
		AbstractJob job = new TestJob();
		ScheduledItem item = new ScheduledItem(job, "0 * * * * *");

		assertEquals(job, item.getJob());
	}

	@Test
	public void testGetCronExpression()
	{
		final String cronExpression = "0 * * * * *";

		ScheduledItem item = new ScheduledItem(new TestJob(), cronExpression);
		assertEquals(cronExpression, item.getCronExpression());
	}
}
