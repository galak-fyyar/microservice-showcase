package me.sokolenko.microservice.usr

@GrabConfig(systemClassLoader = true)
@GrabExclude(group = 'org.slf4j', module = 'slf4j-log4j12')
@GrabExclude(group = 'javax.servlet', module = 'servlet-api')

@Grab(group = 'io.undertow', module = 'undertow-core', version = '1.0.15.Final')
@Grab(group = 'io.undertow', module = 'undertow-servlet', version = '1.0.15.Final')
@Grab(group = 'com.sun.jersey', module = 'jersey-server', version = '1.11')
@Grab(group = 'com.sun.jersey', module = 'jersey-servlet', version = '1.11')
@Grab(group = 'com.sun.jersey', module = 'jersey-json', version = '1.11')
@Grab(group = 'com.netflix.ribbon', module='ribbon-core', version='0.3.13')
@Grab(group = 'com.netflix.ribbon', module='ribbon-httpasyncclient', version='0.3.13')
@Grab(group = 'com.netflix.ribbon', module='ribbon-eureka', version='0.3.13')
@Grab(group = 'com.hazelcast', module='hazelcast', version='3.3.1')
@Grab(group = 'javax.ws.rs', module = 'jsr311-api', version = '1.1.1')
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.2')
@Grab(group = 'com.netflix.archaius', module='archaius-core', version='0.6.0')
@Grab(group = 'com.netflix.archaius', module = 'archaius-zookeeper', version = '0.6.0')
@Grab(group = 'com.netflix.hystrix', module = 'hystrix-core', version = '1.3.18')
@Grab(group = 'com.netflix.hystrix', module = 'hystrix-metrics-event-stream', version = '1.3.18')
@Grab(group = 'com.netflix.hystrix', module='hystrix-request-servlet', version='1.3.18')
@Grab(group = 'com.netflix.eureka', module = 'eureka-client', version = '1.1.141')
@Grab(group='com.google.guava', module='guava', version='18.0')
@Grab(group='hsqldb', module='hsqldb', version='1.8.0.10')

import com.netflix.config.DynamicIntProperty
import com.netflix.config.DynamicPropertyFactory
import me.sokolenko.microservice.usr.api.User
import me.sokolenko.microservice.util.ConfigurationStarter
import me.sokolenko.microservice.util.DiscoveryStarter
import me.sokolenko.microservice.util.HazelcastStarter
import me.sokolenko.microservice.util.ServerStarter

import javax.ws.rs.Consumes
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Response
import java.util.concurrent.ConcurrentMap

import static javax.ws.rs.core.Response.Status.CONFLICT
import static javax.ws.rs.core.Response.Status.NOT_FOUND

/**
 * @author Anatoliy Sokolenko
 */
@Path('/user/v1')
@Consumes('application/json')
@Produces('application/json')
class UserV1Resource {

    final ConcurrentMap<UUID, User> storage

    final DynamicIntProperty defaultLimit = DynamicPropertyFactory.instance.getIntProperty('user.api.limit.default', 10)

    UserV1Resource(ConcurrentMap<UUID, User> storage) {
        this.storage = storage
    }

    @GET
    def list(@QueryParam('limit') @DefaultValue('-1') int limit,
             @QueryParam('offset') @DefaultValue('0') int offset) {

        if (limit == -1) {
            limit = defaultLimit.get()
        }

        def result = []
        def pos = 0

        def ix = storage.iterator()
        while (result.size() < limit && ix.hasNext()) {
            if (pos >= offset) {
                def user = ix.next().value
                result.add(user)
            }

            pos++
        }

        return result
    }

    @GET
    @Path('/{userUuidsString}')
    def get(@PathParam('userUuidsString') String userUuidsString) {
        def userUuids = userUuidsString.split(',')

        def result = []
        for (def userUuid : userUuids) {
            def user = storage[UUID.fromString(userUuid)]

            if (user) {
                result.add(user)
            }
        }

        if (!result.isEmpty()) {
            return result
        } else {
            return Response.status(NOT_FOUND).build()
        }
    }

    @PUT
    def add(User user) {
        if (!user.uuid) {
            user.uuid = UUID.randomUUID()
        }

        if (storage.putIfAbsent(user.uuid, user)) {
            Response.status(CONFLICT)
                    .entity("User with UUID ${user.uuid} already exists")
                    .build()
        } else {
            user.uuid
        }
    }

}

new ConfigurationStarter().start()

def hazelcast = new HazelcastStarter('user-server')
    .addSerializer(User.class, new UserSerializer())
    .addMap('users', UserOnDiskStore.class)
    .start()

new ServerStarter().start('user-server')
        .deployHystrix()
        .deployApi(new UserV1Resource(hazelcast.getMap('users')))

new DiscoveryStarter().start('user-server')
