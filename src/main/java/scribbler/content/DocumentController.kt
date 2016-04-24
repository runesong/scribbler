package scribbler.content

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestMethod.PUT
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import scribbler.CONTENT_API_PATH
import java.nio.charset.Charset
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping(CONTENT_API_PATH)
class DocumentController {

    @Autowired
    private val service: DocumentService? = null

    @Autowired
    private val repository: DocumentRepository? = null

    @Autowired
    private val objectMapper: ObjectMapper? = null

    @ResponseStatus(OK)
    @RequestMapping(method = arrayOf(GET), path = arrayOf("/**"), produces = arrayOf(("application/json")))
    fun getDocument(request: HttpServletRequest, response: HttpServletResponse) {
        repository!!.read(getPath(request), response.outputStream);
    }

    @ResponseStatus(NO_CONTENT)
    @RequestMapping(method = arrayOf(PUT), path = arrayOf("/**"), consumes = arrayOf("application/json"))
    fun putDocument(request: HttpServletRequest) {
        val body = request.inputStream.readBytes(request.contentLength).toString(Charset.forName("UTF-8"))
        val document: Document = objectMapper!!.readValue(body, Document::class.java)
        document.title = document.title.replace("""\W+""", "")
        service!!.save(getPath(request), document)
    }

    private fun getPath(request: HttpServletRequest): String {
        var path = request.servletPath.substring(CONTENT_API_PATH.length)
        path += if (!path.endsWith(".json")) ".json" else ""
        return if (path.startsWith("/")) path.substring(1) else path
    }
}
