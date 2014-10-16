package me.sokolenko.microservice.util

import com.google.common.reflect.TypeToken
import com.netflix.client.http.AsyncHttpClient
import com.netflix.client.http.HttpRequest
import com.netflix.client.http.HttpResponse
import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommand.Setter

/**
 * @author Anatoliy Sokolenko
 */
abstract class RestCommand<T> extends HystrixCommand<T> {

    final String clientNamespace

    final TypeToken<T> type

    protected RestCommand(Setter setter, String clientNamespace, TypeToken<T> type = null) {
        super(setter)

        this.clientNamespace = clientNamespace
        this.type = type
    }

    abstract HttpRequest.Builder buildRequest()

    @Override
    protected run() {
        def requestBuilder = buildRequest()
            .header('content-type', 'application/json')
            .header('accept', 'application/json')

        requestBuilder.retriable = true

        def future = restClient.execute(requestBuilder.build())
        HttpResponse resp = future.get()

        if (!resp.success) {
            throw new RestException(resp)
        } else if (type) {
            return resp.getEntity(type)
        }
    }

    protected AsyncHttpClient getRestClient() {
        ClientHolder.getClient(clientNamespace)
    }
}
