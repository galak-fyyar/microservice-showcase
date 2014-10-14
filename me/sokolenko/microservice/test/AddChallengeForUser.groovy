package me.sokolenko.microservice.test

import com.google.common.util.concurrent.RateLimiter
import me.sokolenko.microservice.domain.api.Challenge
import me.sokolenko.microservice.domain.api.CreateChallengeCommand
import me.sokolenko.microservice.util.ConfigurationStarter
import me.sokolenko.microservice.util.DiscoveryStarter

import static java.util.UUID.fromString

/**
 * @author Anatoliy Sokolenko
 */

new ConfigurationStarter().start()
new DiscoveryStarter().start('web-test')

def threshold = RateLimiter.create(50)

while(true) {
    threshold.acquire()

    def challenge = new Challenge(text: 'У вас есть 24 часа чтобы повторить обливашки',
            authorUuid: fromString('ad36cd47-5d35-4333-9f46-dfd74643c5c3'),
            inviteeUuids: [fromString('08298232-7f23-4dc2-86af-a040aecf54c9')])

    challenge.uuid = new CreateChallengeCommand(challenge).execute()
}



