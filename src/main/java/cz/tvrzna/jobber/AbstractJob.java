package cz.tvrzna.jobber;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class defines abstract sculpture for any job, that could be run in
 * <code>Scheduler</code>. Job is started via {@link AbstractJob#run()} method,
 * that checks, if jobs is not already running and after that it invokes
 * <code>execute</code> abstract method.
 *
 * @since 0.1.0
 * @author michalt
 */
public abstract class AbstractJob implements Runnable
{
	private AtomicBoolean running = new AtomicBoolean(false);

	/**
	 * Starts the <code>AbstractJob</code>, unless it is already running.
	 */
	@Override
	public void run()
	{
		if (running.compareAndSet(false, true))
		{
			try
			{
				execute();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			running.set(false);
		}
	}

	/**
	 * Executes the main method of implementation of <code>AbstractJob</code>.
	 *
	 * @throws Exception
	 *           the exception
	 */
	protected abstract void execute() throws Exception;

	/**
	 * Checks if is running.
	 *
	 * @return true, if is running
	 */
	public boolean isRunning()
	{
		return running.get();
	}

}
