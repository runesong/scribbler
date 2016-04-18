package scribbler.config;

import org.asciidoctor.Asciidoctor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguration
{
	@Bean
	public RepositoryProperties repositoryProperties()
	{
		return new RepositoryProperties();
	}

	@Bean(destroyMethod = "shutdown")
	public Asciidoctor asciidoctor()
	{
		return Asciidoctor.Factory.create();
	}
}
