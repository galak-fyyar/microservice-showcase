package me.sokolenko.microservice.usr.api

import com.google.common.reflect.TypeToken
import com.netflix.client.http.HttpRequest
import com.netflix.hystrix.HystrixCommandKey
import me.sokolenko.microservice.util.RestCommand

import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey

/**
 * @author Anatoliy Sokolenko
 */
class GetUserCommand extends RestCommand<List<User>> {

    final Set<UUID> userUuids

    GetUserCommand(UUID userUuid) {
        this(Collections.singleton(userUuid))
    }

    GetUserCommand(Set<UUID> userUuids) {
        super(withGroupKey(asKey('User'))
                .andCommandKey(HystrixCommandKey.Factory.asKey('GetUser')), 'user-client', new UserList())

        this.userUuids = userUuids
    }

    @Override
    HttpRequest.Builder buildRequest() {
        StringBuilder pathParam = new StringBuilder()
        for (Iterator<UUID> userUuidsIt = userUuids.iterator(); userUuidsIt.hasNext(); ) {
            UUID userUuid = userUuidsIt.next()

            pathParam.append(userUuid)

            if (userUuidsIt.hasNext()) {
                pathParam.append(',')
            }
        }

        HttpRequest.newBuilder()
                .uri("/api/user/v1/${pathParam}")
    }

    public static class UserList extends TypeToken<List<User>> {

    }
}
