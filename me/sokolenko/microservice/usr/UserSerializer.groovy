package me.sokolenko.microservice.usr

import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.hazelcast.nio.serialization.StreamSerializer
import me.sokolenko.microservice.usr.api.User

/**
 * @author Anatoliy Sokolenko
 */
class UserSerializer implements StreamSerializer<User> {

    @Override
    void write(ObjectDataOutput output, User user) throws IOException {
        output.writeLong(user.uuid.getMostSignificantBits())
        output.writeLong(user.uuid.getLeastSignificantBits())
        output.writeUTF(user.email)
    }

    @Override
    User read(ObjectDataInput input) throws IOException {
        User user = new User()

        user.uuid = new UUID(input.readLong(), input.readLong())
        user.email = input.readUTF()

        user
    }

    @Override
    int getTypeId() {
        return 100
    }

    @Override
    void destroy() {

    }

}
