package me.sokolenko.microservice.domain.api

import com.google.common.reflect.TypeToken
import com.netflix.client.http.HttpRequest
import com.netflix.hystrix.HystrixCommandKey
import me.sokolenko.microservice.util.RestCommand

import static com.netflix.client.http.HttpRequest.Verb.PUT
import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey

/**
 * @author Anatoliy Sokolenko
 */
class CreateChallengeCommand extends RestCommand<UUID> {

    final Challenge challenge

    public CreateChallengeCommand(Challenge challenge) {
        super(withGroupKey(asKey('Challenge'))
                .andCommandKey(HystrixCommandKey.Factory.asKey('CreateChallenge')), 'challenge-client',
                TypeToken.of(UUID.class)
        )

        this.challenge = challenge
    }

    @Override
    HttpRequest.Builder buildRequest() {
        HttpRequest.newBuilder()
                .uri("/api/challenge/v1/")
                .entity(challenge)
                .verb(PUT)
    }
}
