package scribbler.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriTemplate
import scribbler.LOG
import java.io.IOException
import java.net.URI
import java.util.*
import kotlin.concurrent.thread

@Profile("local")
@Configuration
@ConditionalOnWebApplication
open class LocalConfiguration
{
	@Bean
	@ConfigurationProperties("browse")
	open fun browserProperties(): BrowserProperties
	{
		return BrowserProperties()
	}

	@Bean
	open fun browserStartupListener(): EmbeddedServletContainerInitializedListener
	{
		return EmbeddedServletContainerInitializedListener()
	}

	class BrowserProperties
	{
		var enabled: Boolean = false
		var uri: UriTemplate = UriTemplate("{scheme}://{address}:{port}{context-path}{servlet-path}")
	}

	class EmbeddedServletContainerInitializedListener : ApplicationListener<EmbeddedServletContainerInitializedEvent>
	{
		@Autowired
		private val server: ServerProperties? = null

		@Autowired
		private val browser: BrowserProperties? = null

		private val max = 5000L;

		override fun onApplicationEvent(event: EmbeddedServletContainerInitializedEvent)
		{
			thread(name = "startupListener") {
				if (server!!.ssl.trustStore != null) {
					System.setProperty("javax.net.ssl.trustStore", server.ssl.trustStore)
				}

				val uri = buildUri(event.embeddedServletContainer.port)
				val request = RequestEntity.get(uri).build()
				var response: ResponseEntity<String>? = null
				val start = System.currentTimeMillis()
				var exception: Throwable? = null
				var client = RestTemplate()
				client.errorHandler = object : DefaultResponseErrorHandler() {
					override fun handleError(response: ClientHttpResponse) {}
				}
				do {
					try {
						response = client.exchange(request, String::class.java)
					} catch (ex: ResourceAccessException) {
						exception = ex
					}
				} while (response?.statusCode == null && !timedOut(start, exception))

				if (response?.statusCode != null) {
					LOG.info(String.format(
							"%n----------------------------------------" +
							"%n" +
							"%n The application is ready:" +
							"%n %s" +
							"%n" +
							"%n----------------------------------------", uri
					))

					if (browser!!.enabled) {
						openBrowser(uri)
					}
				}
			}
		}

		private fun buildUri(port: Int): URI
		{
			val variables = HashMap<String, Any>()
			variables.put("scheme", if (server!!.ssl.isEnabled) "https" else "http")
			variables.put("address", if (server.address == null) "localhost" else server.address)
			variables.put("port", port)
			variables.put("context-path", if (server.contextPath == null) "" else server.contextPath)
			variables.put("servlet-path", if (server.servletPath == null) "/" else server.servletPath)
			return browser!!.uri.expand(variables)
		}

		private fun openBrowser(uri: URI)
		{
			val headless = java.lang.Boolean.getBoolean("java.awt.headless")
			try {
				if (headless) {
					System.setProperty("java.awt.headless", "false")
					System.setProperty("apple.awt.UIElement", "true")
				}
				java.awt.Desktop.getDesktop().browse(uri)
			} catch (ex: IOException) {
				LOG.debug("Failed to open desktop browser.", ex)
				LOG.error("Failed to open desktop browser: {}", ex.message);
			} finally {
				if (headless) {
					System.setProperty("java.awt.headless", headless.toString())
				}
			}
		}

		private fun timedOut(start: Long, exception: Throwable?): Boolean
		{
			if (System.currentTimeMillis() - start > max) {
				if (exception == null) {
					LOG.error("Listener timed out while waiting for server to start.")
				} else {
					LOG.error("Listener timed out while waiting for server to start: {}", exception.message)
				}
				return true;
			}
			Thread.sleep(250)
			return false;
		}
	}
}
