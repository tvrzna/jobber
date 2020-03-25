package cz.tvrzna.jobber;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cz.tvrzna.jobber.annotations.Scheduled;

/**
 * The class is simple Job Scheduler, that runs periodically any class, that
 * extends {@link AbstractJob} and have defined {@link Scheduled} annotation
 * with simplified CRON expression. <br>
 * Whole <code>Jobber</code> is <code>static</code> singleton, that requires
 * to be initialized by {@link Jobber#init(List, List)}. If jobs are correctly
 * loaded, next step could be starting via method {@link Jobber#start()}. This
 * starts scheduling and executing of all correct jobs in independent
 * <code>Thread</code>.<br>
 * Each started job runs in own <code>Thread</code>, that means any job could
 * not be interrupted at this moment.
 *
 * @since 0.1.0
 * @author michalt
 */
public class Jobber
{
	private static boolean loaded = false;
	private static List<ScheduledItem> schedulerContext;
	private static Thread jobberThread;

	/**
	 * Instantiates a new Jobber.
	 */
	private Jobber()
	{
	}

	/**
	 * Gets the scheduled items.
	 *
	 * @return the scheduled items
	 */
	public static List<ScheduledItem> getScheduledItems()
	{
		return Collections.unmodifiableList(schedulerContext);
	}

	/**
	 * Inits the <code>Jobber</code>, adds initialized jobs and initializes
	 * classes. These jobs are added into <code>schedulerContext</code> as
	 * parameter in newly initialized {@link ScheduledItem}, that handles all
	 * information for next run of job.
	 *
	 * @param jobClasses
	 *          the job classes
	 * @param jobInstances
	 *          the job instances
	 */
	public static void init(List<Class<AbstractJob>> jobClasses, List<AbstractJob> jobInstances)
	{
		if (!loaded)
		{
			schedulerContext = new ArrayList<>();
			try
			{
				Date currentDate = new Date();
				for (AbstractJob job : prepareInstances(jobClasses, jobInstances))
				{
					ScheduledItem item = new ScheduledItem(job, job.getClass().getAnnotation(Scheduled.class).value());
					item.setNextRun(parseCronExpression(item.getCronExpression(), currentDate));
					schedulerContext.add(item);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			if (schedulerContext != null && !schedulerContext.isEmpty())
			{
				loaded = true;
			}
		}
	}

	/**
	 * Stops the <code>Jobber</code>. All running jobs will be finished, but
	 * any other will be started after execution of this method.
	 */
	public static void stop()
	{
		if (jobberThread != null && jobberThread.isAlive())
		{
			jobberThread.interrupt();
			loaded = false;
		}
	}

	/**
	 * Starts the <code>Jobber</code> as independent <code>Thread</code>, that
	 * iterates through the <code>schedulerContext</code> and seeks for
	 * {@link ScheduledItem}, that could have started referenced
	 * {@link AbstractJob}. Before job is started, new start timestamp is
	 * evaluated to prevent multiple starting of single Job.<br>
	 * Jobs are handled sequentially, if there is 2 jobs scheduled on same time,
	 * second needs to wait, before first is started. Each job is in independent
	 * <code>Thread</code>, so jobs could run in parallel.
	 */
	public static void start()
	{
		if (jobberThread != null && jobberThread.isAlive())
		{
			return;
		}
		AtomicBoolean locked = new AtomicBoolean(false);
		jobberThread = new Thread("jobber")
		{
			@Override
			public void run()
			{
				while (true)
				{
					try
					{
						if (locked.compareAndSet(false, true))
						{
							Date currentDate = new Date();
							for (ScheduledItem item : getScheduledItems())
							{
								if (!item.getJob().isRunning())
								{
									if (currentDate.after(item.getNextRun()))
									{
										item.setLastRun(currentDate);
										item.setNextRun(parseCronExpression(item.getCronExpression(), currentDate));
										new Thread(item.getJob(), item.getJob().getClass().getName()).start();
									}
								}
							}
							locked.set(false);
						}
						Thread.sleep(10l);
					}
					catch (Exception e)
					{
						locked.set(false);
						e.printStackTrace();
					}
				}
			}
		};
		jobberThread.start();
	}

	/**
	 * Prepare instances of {@link AbstractJob}s, either they are defined as class
	 * or instance. These instances are returned as
	 * <code>List&lt;AbstractJob&gt;</code>.
	 *
	 * @param jobClasses
	 *          the job classes
	 * @param jobInstances
	 *          the job instances
	 * @return the list
	 * @throws InstantiationException
	 *           the instantiation exception
	 * @throws IllegalAccessException
	 *           the illegal access exception
	 * @throws IllegalArgumentException
	 *           the illegal argument exception
	 * @throws InvocationTargetException
	 *           the invocation target exception
	 * @throws NoSuchMethodException
	 *           the no such method exception
	 * @throws SecurityException
	 *           the security exception
	 */
	private static List<AbstractJob> prepareInstances(List<Class<AbstractJob>> jobClasses, List<AbstractJob> jobInstances)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		List<AbstractJob> result = new ArrayList<>();

		if (jobInstances != null && !jobInstances.isEmpty())
		{
			for (AbstractJob job : jobInstances)
			{
				if (job.getClass().isAnnotationPresent(Scheduled.class))
				{
					result.add(job);
				}
			}
		}

		if (jobClasses != null && !jobClasses.isEmpty())
		{
			for (Class<AbstractJob> clazz : jobClasses)
			{
				if (clazz.isAnnotationPresent(Scheduled.class) && !isJobInitialized(result, clazz))
				{
					Constructor<?> constr = clazz.getDeclaredConstructor();
					constr.setAccessible(true);
					result.add((AbstractJob) constr.newInstance());
				}
			}
		}

		return result;
	}

	/**
	 * Checks if is job initialized.
	 *
	 * @param jobInstances
	 *          the job instances
	 * @param clazz
	 *          the clazz
	 * @return true, if is job initialized
	 */
	private static boolean isJobInitialized(List<AbstractJob> jobInstances, Class<AbstractJob> clazz)
	{
		return jobInstances.stream().anyMatch(job -> clazz.equals(job.getClass()));
	}

	/**
	 * Starts job by its <code>jobName</code> independently on its scheduled
	 * timestamp of next run.
	 *
	 * @param jobName
	 *          the job name
	 */
	public static void fireJob(String jobName)
	{
		ScheduledItem scheduledItem = schedulerContext.stream().filter(item -> item.getJob().getClass().getSimpleName().equals(jobName)).findFirst().orElse(null);
		if (scheduledItem != null)
		{
			scheduledItem.setNextRun(new Date());
		}
	}

	/**
	 * Parses the simplified CRON expression into <code>Date</code> of next
	 * possible run.
	 *
	 * @param cronExpression
	 *          the cron expression
	 * @param currentDate
	 *          the current date
	 * @return the date
	 */
	private static Date parseCronExpression(String cronExpression, Date currentDate)
	{
		String[] parts = cronExpression.split(" ");

		Calendar currentCalendar = Calendar.getInstance();
		currentCalendar.setTime(currentDate);
		currentCalendar.set(Calendar.MILLISECOND, 0);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		newCronValue(cal, Calendar.SECOND, parts[0], currentCalendar);
		newCronValue(cal, Calendar.MINUTE, parts[1], currentCalendar);
		newCronValue(cal, Calendar.HOUR_OF_DAY, parts[2], currentCalendar);
		newCronValue(cal, Calendar.DAY_OF_YEAR, parts[3], currentCalendar);
		newCronValue(cal, Calendar.MONTH, parts[4], currentCalendar);
		newCronValue(cal, Calendar.YEAR, parts[5], currentCalendar);

		return cal.getTime();
	}

	/**
	 * New cron value.
	 *
	 * @param cal
	 *          the cal
	 * @param field
	 *          the field
	 * @param strValue
	 *          the str value
	 * @param currentCalendar
	 *          the current calendar
	 */
	private static void newCronValue(Calendar cal, int field, String strValue, Calendar currentCalendar)
	{
		int maxValue = cal.getActualMaximum(field);
		int currentValue = cal.get(field);
		int value = 0;

		if (strValue.contains("*"))
		{
			value = (currentValue) <= maxValue ? currentValue : 0;

			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(cal.getTime());
			cal2.set(field, value);

			if (cal2.before(currentCalendar) || cal2.compareTo(currentCalendar) == 0)
			{
				value = (currentValue + 1) <= maxValue ? currentValue + 1 : 0;
			}
		}
		else if (strValue.contains("/"))
		{
			String[] parts = strValue.split("/");
			value = Integer.parseInt(parts[0]);
			int div = Integer.parseInt(parts[1]);

			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(cal.getTime());
			cal2.set(field, value);

			while (cal2.before(currentCalendar) || cal2.compareTo(currentCalendar) == 0)
			{
				value += div;
				cal2.set(field, value);
				if (value > maxValue)
				{
					value = Integer.parseInt(parts[0]);
					break;
				}
			}
		}
		else
		{
			value = Integer.parseInt(strValue);
		}

		cal.set(field, value);
	}
}
