package me.sokolenko.microservice.test

import me.sokolenko.microservice.domain.api.Challenge
import me.sokolenko.microservice.domain.api.CreateChallengeCommand
import me.sokolenko.microservice.usr.api.CreateUserCommand
import me.sokolenko.microservice.usr.api.User
import me.sokolenko.microservice.util.ConfigurationStarter
import me.sokolenko.microservice.util.DiscoveryStarter

/**
 * @author Anatoliy Sokolenko
 */
new ConfigurationStarter().start()
new DiscoveryStarter().start('web-test')

def texts = [
        'У вас есть 24 часа чтобы повторить обливашки',
        'Ice Bucket',
]

def emails = [
        'JosephNFreese@rhyta.com', 'CliffordJRobertson@rhyta.com', 'PatDDavis@jourrapide.com',
        'JohnBLeveille@rhyta.com', 'EarlSDempsey@rhyta.com', 'CurtisMFranklin@teleworm.us',
        'CharlesIMazza@armyspy.com', 'BarbaraMColon@jourrapide.com', 'PatriciaEBoyles@rhyta.com',
        'LuisAEdmunds@dayrep.com', 'JimmyCLong@dayrep.com', 'BarryMBlount@rhyta.com',
        'DarleneJLemon@armyspy.com', 'DanaBSamuel@jourrapide.com'
]

def users = []
for (String email : emails) {
    def user = new User(email: email)

    user.uuid = new CreateUserCommand(user).execute()
    users << user

    println(user)
}


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
