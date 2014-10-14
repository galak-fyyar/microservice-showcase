package me.sokolenko.microservice.usr

import com.hazelcast.core.MapStoreAdapter
import me.sokolenko.microservice.usr.api.User

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

/**
 * Created by galak on 10/14/14.
 */
class UserOnDiskStore extends MapStoreAdapter<UUID, User> {

    private Connection cxn;

    UserOnDiskStore() {
        cxn = DriverManager.getConnection("jdbc:hsqldb:/tmp/microservice", "SA", "");
        cxn.createStatement().executeUpdate("create table if not exists users (uuid char(36), email varchar(1024))");
    }

    @Override
    void delete(UUID key) {
        cxn.createStatement().execute("delete from users where uuid=${key.toString()}")
    }

    @Override
    void store(UUID key, User value) {
        cxn.createStatement().execute("""
            merge into users using ((values(${key.toString()}, ${value.email})))
            as vals(uuid, email) on users.uuid = vals.uuid
            when MATCHED THEN UPDATE SET users.email = vals.email
            WHEN NOT MATCHED THEN INSERT VALUES vals.uuid, vals.email
        """)
    }

    @Override
    User load(UUID key) {
        ResultSet rs = cxn.createStatement().executeQuery("select email from users where uuid=${key}")

        if (rs.next()) {
            return new User(uuid: key, email: rs.getString('email'))
        } else {
            return null
        }
    }

    @Override
    Set<UUID> loadAllKeys() {
        ResultSet rs = cxn.createStatement().executeQuery("select uuid from users")

        Set<UUID> result = new HashSet<>()
        while (rs.next()) {
            result.add(UUID.fromString(rs.getString('uuid')))
        }

        return result
    }
}
