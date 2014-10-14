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
            authorUuid: fromString('456511cd-1981-4103-957f-8fbd0bb72354'),
            inviteeUuids: [fromString('b760fcb6-581c-4851-972f-9ce6bceb7ace')])

    challenge.uuid = new CreateChallengeCommand(challenge).execute()
}



