package scribbler.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;
import scribbler.Main;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "browse", name = "enabled", havingValue = "true")
public class LocalConfiguration
{
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	@Bean
	@ConfigurationProperties("browse")
	public BrowserProperties browserProperties()
	{
		return new BrowserProperties();
	}

	@Bean
	public EmbeddedServletContainerInitializedListener browserStartupListener()
	{
		return new EmbeddedServletContainerInitializedListener();
	}

	private static class BrowserProperties
	{
		private boolean enabled;
		private UriTemplate uri;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public UriTemplate getUri() {
			return uri;
		}

		public void setUri(UriTemplate uri) {
			this.uri = uri;
		}
	}

	private static class EmbeddedServletContainerInitializedListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent>
	{
		@Autowired
		private ServerProperties server;

		@Autowired
		private BrowserProperties browser;

		@Override
		public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event)
		{
			try {
				if (server.getSsl().getTrustStore() != null) {
					System.setProperty("javax.net.ssl.trustStore", server.getSsl().getTrustStore());
				}

				Map<String, Object> variables = new HashMap<>();
				variables.put("scheme", server.getSsl().isEnabled() ? "https" : "http");
				variables.put("address", server.getAddress() == null ? "localhost" : server.getAddress());
				variables.put("port", event.getEmbeddedServletContainer().getPort());
				variables.put("context-path", server.getContextPath() == null ? "" : server.getContextPath());
				variables.put("servlet-path", server.getServletPath() == null ? "/" : server.getServletPath());
				URI uri = browser.getUri().expand(variables);

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
