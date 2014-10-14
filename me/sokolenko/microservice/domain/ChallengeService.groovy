package me.sokolenko.microservice.domain

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

import me.sokolenko.microservice.domain.api.Challenge
import me.sokolenko.microservice.nav.api.UpdateIndexCommand
import me.sokolenko.microservice.util.ConfigurationStarter
import me.sokolenko.microservice.util.DiscoveryStarter
import me.sokolenko.microservice.util.HazelcastFactory
import me.sokolenko.microservice.util.ServerStarter

import javax.ws.rs.*
import javax.ws.rs.core.Response
import java.util.concurrent.ConcurrentMap

import static javax.ws.rs.core.Response.Status.CONFLICT
import static javax.ws.rs.core.Response.Status.NOT_FOUND

/**
 * @author Anatoliy Sokolenko
 */

@Path('/challenge/v1')
@Consumes('application/json')
@Produces('application/json')
class ChallengeV1Resource {

    final ConcurrentMap<UUID, Challenge> storage

    ChallengeV1Resource(ConcurrentMap<UUID, Challenge> storage) {
        this.storage = storage
    }

    @GET
    @Path('/{challengeUuid}')
    def get(@PathParam('challengeUuid') UUID challengeUuid) {
        def challenge = storage[challengeUuid]

        if (challenge) {
            return challenge
        } else {
            Response.status(NOT_FOUND).build()
        }
    }

    @PUT
    def create(Challenge challenge) {
        if (!challenge.uuid) {
            challenge.uuid = UUID.randomUUID()
        }

        if (storage.putIfAbsent(challenge.uuid, challenge)) {
            Response.status(CONFLICT)
                    .entity("Challenge with UUID ${challenge.uuid} already exists")
                    .build()
        } else {
            new UpdateIndexCommand(challenge).execute()

            challenge.uuid
        }
    }

}

new ConfigurationStarter().start()

def hazelcast = new HazelcastFactory('challenge', [(Challenge.class): new ChallengeSerializer()])

new ServerStarter().start('challenge')
        .deployHystrix()
        .deployApi(new ChallengeV1Resource(hazelcast.getMap('challenges')))

new DiscoveryStarter().start('challenge')
