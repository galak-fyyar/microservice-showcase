package me.sokolenko.microservice.domain

import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.hazelcast.nio.serialization.StreamSerializer
import me.sokolenko.microservice.domain.api.Challenge

/**
 * @author Anatoliy Sokolenko
 */
class ChallengeSerializer implements StreamSerializer<Challenge> {
    @Override
    void write(ObjectDataOutput output, Challenge challenge) throws IOException {
        output.writeLong(challenge.uuid.getMostSignificantBits())
        output.writeLong(challenge.uuid.getLeastSignificantBits())
        output.writeUTF(challenge.text)
        output.writeLong(challenge.authorUuid.getMostSignificantBits())
        output.writeLong(challenge.authorUuid.getLeastSignificantBits())
        for (UUID inviteeUuid : challenge.inviteeUuids) {
            output.writeBoolean(true)

            output.writeLong(inviteeUuid.getMostSignificantBits())
            output.writeLong(inviteeUuid.getLeastSignificantBits())
        }

        output.writeBoolean(false)
    }

    @Override
    Challenge read(ObjectDataInput input) throws IOException {
        def challenge = new Challenge()

        challenge.uuid = new UUID(input.readLong(), input.readLong())
        challenge.text = input.readUTF()
        challenge.authorUuid = new UUID(input.readLong(), input.readLong())
        while (input.readBoolean()) {
            challenge.inviteeUuids.add(new UUID(input.readLong(), input.readLong()))
        }
    }

    @Override
    int getTypeId() {
        return 1
    }

    @Override
    void destroy() {

    }
}
