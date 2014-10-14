package me.sokolenko.microservice.test

import me.sokolenko.microservice.domain.api.Challenge
import me.sokolenko.microservice.domain.api.CreateChallengeCommand
import me.sokolenko.microservice.usr.api.CreateUserCommand
import me.sokolenko.microservice.usr.api.User
import me.sokolenko.microservice.util.ConfigurationStarter
import me.sokolenko.microservice.util.DiscoveryStarter

import static java.util.UUID.fromString

/**
 * @author Anatoliy Sokolenko
 */
new ConfigurationStarter().start()
new DiscoveryStarter().start('web-test')

def texts = [
        'У вас есть 24 часа чтобы повторить обливашки',
        'Ice Bucket',
]

def users = [
        new User(uuid: fromString('b17be141-53e9-49fa-a636-d68f56ee93f0'), email: 'JosephNFreese@rhyta.com'),
        new User(uuid: fromString('06148745-1f44-4c28-8902-2915202a1f93'), email: 'CliffordJRobertson@rhyta.com'),
        new User(uuid: fromString('2eca4441-bd71-4331-8688-a2819f963411'), email: 'PatDDavis@jourrapide.com'),
        new User(uuid: fromString('b80a5392-71eb-44e9-9f3f-b53b054ec8f3'), email: 'JohnBLeveille@rhyta.com'),
        new User(uuid: fromString('924022f7-e8a2-4057-86da-cd4c1407b53c'), email: 'EarlSDempsey@rhyta.com'),
        new User(uuid: fromString('456511cd-1981-4103-957f-8fbd0bb72354'), email: 'CurtisMFranklin@teleworm.us'),
        new User(uuid: fromString('154b0489-ce24-45f6-a51d-657c8a1cf07d'), email: 'CharlesIMazza@armyspy.com'),
        new User(uuid: fromString('54ca14ce-12a4-496d-a0a3-b58147d4f97b'), email: 'BarbaraMColon@jourrapide.com'),
        new User(uuid: fromString('956111c6-01e7-4007-b311-1d4d903fe88e'), email: 'PatriciaEBoyles@rhyta.com'),
        new User(uuid: fromString('dc4a384e-4cef-4f38-9d86-b40b90e54124'), email: 'LuisAEdmunds@dayrep.com'),
        new User(uuid: fromString('ec5dfd8a-aab6-40de-9d61-76e9401ece64'), email: 'JimmyCLong@dayrep.com'),
        new User(uuid: fromString('e4b9c0c3-1d79-4743-8046-fe3994692c64'), email: 'BarryMBlount@rhyta.com'),
        new User(uuid: fromString('b760fcb6-581c-4851-972f-9ce6bceb7ace'), email: 'DarleneJLemon@armyspy.com'),
        new User(uuid: fromString('742db0b2-ce84-47d7-8337-7a9e28349b34'), email: 'DanaBSamuel@jourrapide.com'),
]

//for (User user : users) {
//    new CreateUserCommand(user).execute()
//    println (user)
//}


def rnd = new Random()
def textId = rnd.ints(0, texts.size()).iterator()
def userId = rnd.ints(0, users.size()).iterator()
def inviteesNum = rnd.ints(1, 5).iterator()

for (int i = 0; i < 100; i++) {
    def inviteeCount = inviteesNum.nextInt()
    def inviteeUuids = new HashSet(inviteeCount)
    for (int j = 0; j < inviteeCount; j++) {
        inviteeUuids << users[userId.nextInt()].uuid
    }

    def challenge = new Challenge(text: texts[textId.nextInt()],
            authorUuid: users[userId.nextInt()].uuid,
            inviteeUuids: inviteeUuids
    )

    challenge.uuid = new CreateChallengeCommand(challenge).execute()
    println(challenge)
}
