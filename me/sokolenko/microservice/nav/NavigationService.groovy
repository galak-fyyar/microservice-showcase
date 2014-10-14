package me.sokolenko.microservice.nav

@groovy.lang.GrabConfig(systemClassLoader = true)
@Grab(group='com.hazelcast', module='hazelcast', version='3.3.1')
@groovy.lang.GrabExclude(group = 'org.slf4j', module = 'slf4j-log4j12')
@Grab(group = 'javax.ws.rs', module = 'jsr311-api', version = '1.1.1')
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.2')
@Grab(group='com.netflix.archaius', module='archaius-core', version='0.6.0')

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

new ConfigurationStarter().start()

def hazelcast = new HazelcastFactory('navigation')

new ServerStarter().start('navigation')
        .deployHystrix()
        .deployApi(new NavigationV1Resource(index, deferred))

new DiscoveryStarter().start('navigation')
