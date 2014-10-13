package me.sokolenko.microservice.util

import com.netflix.client.http.AsyncHttpClientBuilder

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by galak on 9/29/14.
 */
class ClientHolder {

    static holder = new ConcurrentHashMap()

    static get(String name) {
        if (holder.containsKey(name)) {
            return holder.get(name)
        } else {
            synchronized (ClientHolder.class) {
                def client = AsyncHttpClientBuilder.withApacheAsyncClient(name)
                        .withLoadBalancer()
                        .build()

                holder[name] = client

                return client
            }
        }
    }

}
