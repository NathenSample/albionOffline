package io.github.nathensample.statusbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
public class Application
{
	//TODO: Crashes if started during DT,
	/*
	Error starting ApplicationContext. To display the auto-configuration report re-run your application with 'debug' enabled.
2019-09-23 11:55:50.849 ERROR 22191 --- [pool-1-thread-1] o.s.s.s.TaskUtils$LoggingErrorHandler    : Unexpected error occurred in scheduled task.

java.io.IOException: Server returned HTTP response code: 500 for URL: http://serverstatus.albiononline.com
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
