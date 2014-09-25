package me.sokolenko.microservice

import com.netflix.niws.client.http.RestClient
import com.sun.jersey.api.client.Client

/**
 * Created by galak on 9/21/14.
 */
class ApiClient extends RestClient {

    @Override
    protected Client apacheHttpClientSpecificInitialization() {
        this.getCon


        return super.apacheHttpClientSpecificInitialization()
    }
}
