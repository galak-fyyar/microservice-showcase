package me.sokolenko.microservice

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.netflix.ribbon.CacheProvider
import com.netflix.ribbon.CacheProviderFactory

/**
 * TODO make configurable through Archaicus
 * TODO who to put content to this cache?
 *
 *
 * Created by galak on 9/23/14.
 */
class GuavaCacheProviderFactory<T> implements CacheProviderFactory<T> {
    @Override
    CacheProvider<T> createCacheProvider() {
        return new GuavaCacheProvider()
    }

    private final class GuavaCacheProvider<T> implements CacheProvider<T> {

        private final Cache cache = CacheBuilder.newBuilder().build()

        @Override
        rx.Observable<T> get(String keyTemplate, Map<String, Object> requestProperties) {
            return rx.Observable.create({
                null
            } as rx.Observable.OnSubscribe)
        }
    }
}
