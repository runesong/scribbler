package scribbler.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.asciidoctor.Asciidoctor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
open class ParserConfiguration
{
//	@Bean
//	open fun kotlinModule(): KotlinModule = KotlinModule()

//	@Bean
//	open fun objectMapperBuilder(): Jackson2ObjectMapperBuilder
//			= Jackson2ObjectMapperBuilder().modulesToInstall(KotlinModule())

	@Profile("asciidoctor")
	@Bean(destroyMethod = "shutdown")
    open fun asciidoctor(): Asciidoctor = Asciidoctor.Factory.create()
}
