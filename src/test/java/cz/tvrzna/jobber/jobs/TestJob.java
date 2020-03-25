package cz.tvrzna.jobber.jobs;

import cz.tvrzna.jobber.AbstractJob;
import cz.tvrzna.jobber.annotations.Scheduled;

@Scheduled("0 0 0 0 0 2299")
public class TestJob extends AbstractJob
{

	@Override
	protected void execute() throws Exception
	{
	}
}
