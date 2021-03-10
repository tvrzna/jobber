package cz.tvrzna.jobber;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
	public void testJobWithException() throws InterruptedException
	{
		List<AbstractJob> jobInstances = new ArrayList<>();
		jobInstances.add(new TestJob4());

		Jobber.init(null, jobInstances);
		Jobber.start();
		Jobber.fireJob("TestJob4");

		Jobber.getScheduledItems().get(0).getLastRun();
		Thread.sleep(100l);
		Jobber.stop();
	}

	@Test
	public void testParseCronExpression()
	{
		Calendar checkCal = Calendar.getInstance();

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2020);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 27);
		cal.set(Calendar.HOUR_OF_DAY, 20);
		cal.set(Calendar.MINUTE, 42);
		cal.set(Calendar.SECOND, 36);

		Date date;

		date = Jobber.parseCronExpression("1,2,3 * * * * *", cal.getTime());
		checkCal.setTime(date);
		Assertions.assertEquals(1, checkCal.get(Calendar.SECOND));
		Assertions.assertEquals(43, checkCal.get(Calendar.MINUTE));

		date = Jobber.parseCronExpression("* 30,50,45,40,20,55 * * * *", cal.getTime());
		checkCal.setTime(date);
		Assertions.assertEquals(45, checkCal.get(Calendar.MINUTE));

		date = Jobber.parseCronExpression("* 90,180 * * * *", cal.getTime());
		checkCal.setTime(date);
		Assertions.assertEquals(59, checkCal.get(Calendar.MINUTE));
		Assertions.assertEquals(20, checkCal.get(Calendar.HOUR_OF_DAY));
	}

	@Test
	public void testEmptyCronExpression()
	{
		Calendar checkCal = Calendar.getInstance();

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2020);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 27);
		cal.set(Calendar.HOUR_OF_DAY, 20);
		cal.set(Calendar.MINUTE, 42);
		cal.set(Calendar.SECOND, 36);

		Date date = Jobber.parseCronExpression("", cal.getTime());
		checkCal.setTime(date);
		Assertions.assertEquals(36, checkCal.get(Calendar.SECOND));
		Assertions.assertEquals(42, checkCal.get(Calendar.MINUTE));
		Assertions.assertEquals(20, checkCal.get(Calendar.HOUR_OF_DAY));
		Assertions.assertEquals(27, checkCal.get(Calendar.DAY_OF_MONTH));
		Assertions.assertEquals(3, checkCal.get(Calendar.MONTH));
		Assertions.assertEquals(2020, checkCal.get(Calendar.YEAR));
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
