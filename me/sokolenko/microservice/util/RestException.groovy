package me.sokolenko.microservice.util

import com.netflix.client.http.HttpResponse

/**
 * @author Anatoliy Sokolenko
 */
class RestException extends RuntimeException {

    private final HttpResponse response

    private final String content

    RestException(HttpResponse response) {
        this.response = response

        if (this.response.hasPayload()) {
            StringBuilder contentBuilder = new StringBuilder()

            this.response.inputStream.newReader().lines().collect({ line ->
                contentBuilder.append(line).append('\n')
            })

            this.content = contentBuilder.toString()
        }
    }

    HttpResponse getResponse() {
        return response
    }

    @Override
    public String toString() {
        return "RestException{" +
                "status=" + response.status +
                "headers=" + response.headers +
                "hasEntity=" + response.hasEntity() +
                "content=" + content +
                '}';
    }
}
