package me.sokolenko.microservice.domain

@groovy.lang.GrabConfig(systemClassLoader = true)
@Grab(group='com.hazelcast', module='hazelcast', version='3.3.1')
@groovy.lang.GrabExclude(group = 'org.slf4j', module = 'slf4j-log4j12')
@Grab(group = 'javax.ws.rs', module = 'jsr311-api', version = '1.1.1')
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.2')
@Grab(group='com.netflix.archaius', module='archaius-core', version='0.6.0')

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
