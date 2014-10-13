package me.sokolenko.microservice.usr.api

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
class CreateUserCommand extends RestCommand<UUID> {

    final User user

    CreateUserCommand(User user) {
        super(withGroupKey(asKey('User'))
                .andCommandKey(HystrixCommandKey.Factory.asKey('CreateUser')), 'user-client', TypeToken.of(UUID.class))

        this.user = user
    }

    @Override
    HttpRequest.Builder buildRequest() {
        HttpRequest.newBuilder()
                .uri("/api/user/v1/")
                .entity(user)
                .verb(PUT)
    }
}
