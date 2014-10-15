package me.sokolenko.microservice.domain.api

import com.google.common.reflect.TypeToken
import com.netflix.client.http.HttpRequest
import com.netflix.hystrix.HystrixCommandKey
import me.sokolenko.microservice.util.RestCommand

import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey

/**
 * @author Anatoliy Sokolenko
 */
class GetChallengeCommand extends RestCommand<Challenge> {

    final UUID challengeUuid

    public GetChallengeCommand(UUID challengeUuid) {
        super(withGroupKey(asKey('Challenge'))
                .andCommandKey(HystrixCommandKey.Factory.asKey('GetChallenge')), 'challenge-client',
                TypeToken.of(Challenge.class)
        )

        this.challengeUuid = challengeUuid
    }

    @Override
    HttpRequest.Builder buildRequest() {
        HttpRequest.newBuilder()
                .uri("/api/challenge/v1/" + challengeUuid)
    }
}
