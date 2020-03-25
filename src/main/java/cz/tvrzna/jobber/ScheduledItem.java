package cz.tvrzna.jobber;

import java.util.Date;

/**
 * The Class ScheduledItem holds information for running {@link AbstractJob}s in
 * <code>Jobber</code>.
 *
 * @since 0.1.0
 * @author michalt
 */
public class ScheduledItem
{
	private final AbstractJob job;
	private final String cronExpression;
	private Date lastRun;
	private Date nextRun;

	/**
	 * Instantiates a new scheduled item.
	 *
	 * @param job
	 *          the job
	 * @param cronExpression
	 *          the cron expression
	 */
	public ScheduledItem(AbstractJob job, String cronExpression)
	{
		this.job = job;
		this.cronExpression = cronExpression;
	}

	/**
	 * Gets the last run.
	 *
	 * @return the last run
	 */
	public Date getLastRun()
	{
		return lastRun;
	}

	/**
	 * Sets the last run.
	 *
	 * @param lastRun
	 *          the new last run
	 */
	public void setLastRun(Date lastRun)
	{
		this.lastRun = lastRun;
	}

	/**
	 * Gets the next run.
	 *
	 * @return the next run
	 */
	public Date getNextRun()
	{
		return nextRun;
	}

	/**
	 * Sets the next run.
	 *
	 * @param nextRun
	 *          the new next run
	 */
	public void setNextRun(Date nextRun)
	{
		this.nextRun = nextRun;
	}

	/**
	 * Gets the job.
	 *
	 * @return the job
	 */
	public AbstractJob getJob()
	{
		return job;
	}

	/**
	 * Gets the cron expression.
	 *
	 * @return the cron expression
	 */
	public String getCronExpression()
	{
		return cronExpression;
	}
}
