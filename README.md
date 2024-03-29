# jobber
[![javadoc](https://javadoc.io/badge2/cz.tvrzna/jobber/0.2.1/javadoc.svg)](https://javadoc.io/doc/cz.tvrzna/jobber/0.2.2)

Extremely simple Job Scheduler.

## What is jobber good for?
In some cases Quartz could be an overkill, for simple scheduling of jobs it is unnecessary to have large library with many function, that are not useful for the specific use-case.

## Installation
```xml
<dependency>
    <groupId>cz.tvrzna</groupId>
    <artifactId>jobber</artifactId>
    <version>0.2.2</version>
</dependency>
```

## Example

__Main.java__

```java
package test.project;

import java.util.Arrays;

import cz.tvrzna.jobber.AbstractJob;
import cz.tvrzna.jobber.Jobber;

public class Main
{
	public static void main(String[] args) throws InterruptedException
	{
		Jobber.init(null, Arrays.asList(new AbstractJob[] { new SampleJob() }));
		Jobber.start();
		while (true)
		{
			Thread.sleep(99999l);
		}
	}
}
```

__SampleJob.java__

```java
package test.project;

import java.util.Date;

import cz.tvrzna.jobber.AbstractJob;
import cz.tvrzna.jobber.annotations.Scheduled;

@Scheduled("0/30 * * * * *")
public class SampleJob extends AbstractJob
{
	@Override
	protected void execute() throws Exception
	{
		System.out.println(new Date() + " - Imagine a task, that you want to run each 30 seconds.");
	}
}

```

__Result output__

```
Thu Mar 12 13:36:00 CET 2020 - Imagine a task, that you want to run each 30 seconds.
Thu Mar 12 13:36:30 CET 2020 - Imagine a task, that you want to run each 30 seconds.
Thu Mar 12 13:37:00 CET 2020 - Imagine a task, that you want to run each 30 seconds.
Thu Mar 12 13:37:30 CET 2020 - Imagine a task, that you want to run each 30 seconds.
Thu Mar 12 13:38:00 CET 2020 - Imagine a task, that you want to run each 30 seconds.
```
