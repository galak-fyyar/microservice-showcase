package me.sokolenko.microservice.nav

import com.hazelcast.core.MultiMap

/**
 * Created by galak on 10/13/14.
 */
class DeferredIndexer extends Thread {

    private final MultiMap<String, UUID> index

    private final Queue deferred

    DeferredIndexer(MultiMap<String, UUID> index, Queue deferred) {
        this.index = index
        this.deferred = deferred
    }

    @Override
    void run() {
        while(!currentThread().isInterrupted()) {
            def el = deferred.poll()

            el?.each { Set<UUID> userUuids, UUID challengeUuid ->
                new IndexUsersCommand(index, deferred, userUuids, challengeUuid).execute()
            }

            sleep(10 * 1000L)
        }
    }
}
