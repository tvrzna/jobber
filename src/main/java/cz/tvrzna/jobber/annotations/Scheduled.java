package cz.tvrzna.jobber.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This interfaces defines, that annotated class is able to be scheduled. The
 * value defines simplified CRON expression.
 *
 * @since 0.1.0
 * @author michalt
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Scheduled
{

	/**
	 * The simplified CRON expression.
	 *
	 * @return the CRON expression
	 */
	String value();
}
