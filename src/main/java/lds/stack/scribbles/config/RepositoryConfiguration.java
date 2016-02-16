/*
 * Copyright (C) 2016 Robert Thornton. All rights reserved.
 * This notice may not be removed.
 */
package lds.stack.scribbles.config;

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
