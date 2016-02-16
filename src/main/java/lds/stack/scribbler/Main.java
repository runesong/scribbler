/**
 * Copyright (C) 2016 Robert Thornton. All rights reserved.
 * This notice may not be removed.
 */
package lds.stack.scribbler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

@SpringBootApplication
public class Main
{
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args)
	{
		SpringApplication.run(Main.class, args);
	}

	@Bean
	@ConditionalOnProperty(name = "browse.enabled")
	public EmbeddedServletContainerInitializedListener browserStartupListener()
	{
		return new EmbeddedServletContainerInitializedListener();
	}

	private static class EmbeddedServletContainerInitializedListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent>
	{
		@Override
		public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event)
		{
			try {
				URI uri = URI.create("http://localhost:" + event.getEmbeddedServletContainer().getPort());
				RestTemplate template = new RestTemplate();
				template.setErrorHandler(new DefaultResponseErrorHandler() {
					@Override
					public void handleError(ClientHttpResponse response) throws IOException {
						response.getStatusCode(); // as long as we have an HTTP status code, we're good
					}
				});
				template.exchange(RequestEntity.get(uri).build(), String.class);
				boolean headless = Boolean.getBoolean("java.awt.headless");
				try {
					if (headless) {
						System.setProperty("java.awt.headless", "false");
						System.setProperty("apple.awt.UIElement", "true");
					}
					java.awt.Desktop.getDesktop().browse(uri);
				} finally {
					if (headless) {
						System.setProperty("java.awt.headless", String.valueOf(headless));
					}
				}
			} catch (Throwable e) {
				LOG.warn("Automatic browser launch aborted: " + e.toString());
			}
		}
	}
}
