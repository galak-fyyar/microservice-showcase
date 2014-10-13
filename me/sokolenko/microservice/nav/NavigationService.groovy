package me.sokolenko.microservice.nav

@GrabConfig(systemClassLoader = true)
@Grab(group='com.hazelcast', module='hazelcast', version='3.3.1')

import com.netflix.config.DynamicIntProperty
import com.netflix.config.DynamicPropertyFactory
import me.sokolenko.microservice.domain.api.Challenge
import me.sokolenko.microservice.usr.api.GetUserCommand
import me.sokolenko.microservice.usr.api.User
import me.sokolenko.microservice.util.ConfigurationStarter
import me.sokolenko.microservice.util.DiscoveryStarter
import me.sokolenko.microservice.util.HazelcastFactory
import me.sokolenko.microservice.util.ServerStarter

import javax.ws.rs.*

import com.hazelcast.core.MultiMap

import javax.ws.rs.core.Response

/**
 * @author Anatoliy Sokolenko
 */
@Path('/navigation/v1')
@Consumes('application/json')
@Produces('application/json')
class NavigationV1Resource {

    final MultiMap<String, UUID> index

    final DynamicIntProperty defaultLimit = DynamicPropertyFactory.instance.getIntProperty('navigation.api.limit.default', 10)

    NavigationV1Resource(MultiMap<String, UUID> index) {
        this.index = index
    }

    @GET
    def get(@QueryParam('q') String query,
            @QueryParam('limit') @DefaultValue('-1') int limit,
            @QueryParam('offset') @DefaultValue('0') int offset) {

        if (limit == -1) {
            limit = defaultLimit.get()
        }

        def result = []
        def pos = 0

        def terms
        if (query) {
            terms = query.split(/\s/)
        } else {
            terms = index.keySet()
        }

        for (def term : terms) {
            if (pos >= offset) {
                def uuids = index.get(term)

                if (uuids.size() > limit) {
                    uuids = new ArrayList<UUID>(uuids).subList(0, limit)
                }

                if (uuids) {
                    result.addAll(uuids)
                }
            }

            if (result.size() == limit) {
                break
            }

            pos++
        }

        result
    }

    @PUT
    def index(Challenge challenge) {
        challenge.text.split(/\s/).each { String term ->
            index.put(term, challenge.uuid)
        }

        def userUuids = new HashSet()
        userUuids.add(challenge.authorUuid)
        userUuids.addAll(challenge.inviteeUuids)

        List<User> users = new GetUserCommand(userUuids).execute()
        for (User user : users) {
            index.put(user.email, challenge.uuid)
        }

        Response.ok().build()
    }

}

def hazelcast = new HazelcastFactory('navigation')

new ConfigurationStarter().start()
new ServerStarter().start('navigation')
        .deployHybris()
        .deployApi(new NavigationV1Resource(hazelcast.getMultiMap('navigation-index')))

new DiscoveryStarter().start('navigation')
