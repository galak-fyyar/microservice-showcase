package me.sokolenko.microservice.nav

import com.hazelcast.core.MultiMap
import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandKey
import me.sokolenko.microservice.usr.api.GetUserCommand
import me.sokolenko.microservice.usr.api.User
import org.slf4j.LoggerFactory

import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey

/**
 * @author Anatoliy Sokolenko
 */
class IndexUsersCommand extends HystrixCommand {

    private final static logger = LoggerFactory.getLogger(IndexUsersCommand.class)

    private final MultiMap<String, UUID> index

    private final Queue deferred

    private final Set<UUID> userUuids

    private final UUID challengeUuid

    protected IndexUsersCommand(MultiMap<String, UUID> index, Queue deferred, Set<UUID> userUuids, UUID challengeUuid) {
        super(withGroupKey(asKey('Navigation-Impl'))
                .andCommandKey(HystrixCommandKey.Factory.asKey('IndexUsers')))

        this.index = index
        this.deferred = deferred
        this.userUuids = userUuids
        this.challengeUuid = challengeUuid
    }

    @Override
    protected Object run() throws Exception {
        List<User> users = new GetUserCommand(userUuids).execute()

        for (User user : users) {
            index.put(user.email, challengeUuid)
        }

        logger.info("Challenge ${challengeUuid} successfully indexed with users ${userUuids}")
    }

    @Override
    protected Object getFallback() {
        deferred.add([(userUuids):challengeUuid])

        null
    }
}
