package me.sokolenko.microservice.nav.api

import com.netflix.client.http.HttpRequest
import com.netflix.hystrix.HystrixCommandKey
import me.sokolenko.microservice.domain.api.Challenge
import me.sokolenko.microservice.util.RestCommand

import static com.netflix.client.http.HttpRequest.Verb.PUT
import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey

/**
 * @author Anatoliy Sokolenko
 */
class UpdateIndexCommand extends RestCommand {

    final Challenge challenge

    public UpdateIndexCommand(Challenge challenge) {
        super(withGroupKey(asKey('Navigation'))
                .andCommandKey(HystrixCommandKey.Factory.asKey('UpdateIndex')), 'navigation-client')

        this.challenge = challenge
    }

    @Override
    HttpRequest.Builder buildRequest() {
        HttpRequest.newBuilder()
                .uri('/api/navigation/v1')
                .entity(challenge)
                .verb(PUT)
    }
}
