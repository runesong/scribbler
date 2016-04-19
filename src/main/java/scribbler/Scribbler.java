package scribbler;

import com.fasterxml.jackson.module.kotlin.KotlinModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Scribbler
{
	public static void main(String[] args)
	{
		SpringApplication.run(Scribbler.class, args);
	}

	@Bean
	KotlinModule kotlinModule()
	{
		return new KotlinModule();
	}
}
