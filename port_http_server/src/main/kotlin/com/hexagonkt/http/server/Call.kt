package com.hexagonkt.http.server

import com.hexagonkt.helpers.CodedException
import com.hexagonkt.helpers.RequiredKeysMap
import com.hexagonkt.serialization.SerializationFormat
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.serialize

/**
 * HTTP request context. It holds client supplied data and methods to change the response.
 */
class Call(val request: Request, val response: Response, val session: Session) {

    /** Call attributes (for the current request). Same as HttpServletRequest.setAttribute(). */
    val attributes: MutableMap<String, Any> by lazy { LinkedHashMap<String, Any>() }

    val responseType: String get() =
        response.contentType ?:
        request.headers["Accept"]?.firstOrNull()?.let { if (it == "*/*") null else it } ?:
        requestType

    val requestType: String get() =
        request.contentType ?: defaultFormat.contentType

    val requestFormat: SerializationFormat get() =
        SerializationManager.formatOf(requestType)

    val responseFormat: SerializationFormat get() =
        SerializationManager.formatOf(responseType)

    // Request shortcuts
    val pathParameters: RequiredKeysMap<String, String> by lazy { request.pathParameters }
    val parameters: Map<String, List<String>> by lazy { request.parameters }

    fun ok(content: Any = "", contentType: String? = null) = send(200, content, contentType)

    fun ok(content: Any = "", serializationFormat: SerializationFormat) =
        send(200, content, serializationFormat)

    fun send(code: Int, content: Any = "", contentType: String? = null) {
        response.status = code
        response.body = content

        if (contentType != null)
            response.contentType = contentType
    }

    fun send(code: Int, content: Any = "", serializationFormat: SerializationFormat) =
        send(code, content.serialize(serializationFormat), serializationFormat.contentType)

    fun halt(content: Any): Nothing = halt(500, content)

    fun halt(code: Int = 500, content: Any = ""): Nothing {
        throw CodedException(code, content.toString())
    }

    fun redirect (url: String) = response.redirect(url)
}
