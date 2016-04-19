package scribbler

import org.slf4j.LoggerFactory

const val TEXT_MARKDOWN : String = "text/markdown"
const val TEXT_ASCIIDOCTOR : String = "text/asciidoctor"

const val CONTENT_API_PATH = "/api/content";

val LOG = LoggerFactory.getLogger(Scribbler::class.java)
