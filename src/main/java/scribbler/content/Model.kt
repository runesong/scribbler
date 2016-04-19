package scribbler.content

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

data class Document(
	var title: String,
	val format: String,
	val body: String
)

open class DocumentException : RuntimeException {
	constructor(message: String, ex: Exception?): super(message, ex) {}
	constructor(message: String): super(message) {}
	constructor(ex: Exception): super(ex) {}
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class DocumentNotFoundException : DocumentException {
	constructor(message: String, ex: Exception?): super(message, ex) {}
	constructor(message: String): super(message) {}
	constructor(ex: Exception): super(ex) {}
}

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
class DocumentProcessingException : DocumentException {
	constructor(message: String, ex: Exception?): super(message, ex) {}
	constructor(message: String): super(message) {}
	constructor(ex: Exception): super(ex) {}
}
