package cz.tvrzna.jobber;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cz.tvrzna.jobber.annotations.Scheduled;
import cz.tvrzna.jobber.jobs.TestJob;

public class JobberTest
{
	private boolean jobRunned = false;

	@SuppressWarnings("unchecked")
	@Test
	public void testJobber() throws InterruptedException
	{
		jobRunned = false;

		List<Class<AbstractJob>> jobClasses = new ArrayList<>();
		jobClasses.add((Class<AbstractJob>) ((Class<?>) TestJob.class));

		List<AbstractJob> jobInstances = new ArrayList<>();
		jobInstances.add(new TestJob2());
		AbstractJob test3 = new TestJob3();
		jobInstances.add(test3);

		Jobber.start();
		Jobber.stop();
		Jobber.init(null, null);
		Jobber.init(new ArrayList<>(), new ArrayList<>());
		Jobber.init(jobClasses, jobInstances);
		Jobber.init(jobClasses, jobInstances);
		Jobber.getScheduledItems();

		Jobber.start();
		Jobber.start();
		Jobber.fireJob("TestJob3");
		Thread.sleep(50l);
		new Thread(test3).start();
		Thread.sleep(1200l);
		Jobber.stop();
		Jobber.stop();

		Assertions.assertTrue(jobRunned);
	}

	@Test
	public void testJobWithException() throws InterruptedException {
		List<AbstractJob> jobInstances = new ArrayList<>();
		jobInstances.add(new TestJob4());

		Jobber.init(null, jobInstances);
		Jobber.start();
		Jobber.fireJob("TestJob4");

		Jobber.getScheduledItems().get(0).getLastRun();
		Thread.sleep(100l);
		Jobber.stop();
	}

	@Scheduled("* * * * * *")
	private class TestJob2 extends AbstractJob
	{
		@Override
		protected void execute() throws Exception
		{
			System.out.println("job runs");
			jobRunned = true;
		}
	}

	@Scheduled("0/10 * * * * *")
	private class TestJob3 extends AbstractJob
	{
		@Override
		protected void execute() throws Exception
		{
			Thread.sleep(500l);
		}
	}

	@Scheduled("0 * * * * * *")
	private class TestJob4 extends AbstractJob
	{
		@Override
		protected void execute() throws Exception
		{
			throw new Exception("Job throws an exception");
		}
	}
}
