package me.sokolenko.microservice.util

import com.netflix.config.ConcurrentCompositeConfiguration
import com.netflix.config.ConfigurationManager
import com.netflix.config.DynamicWatchedConfiguration
import com.netflix.config.source.ZooKeeperConfigurationSource
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.BoundedExponentialBackoffRetry

/**
 * @author Anatoliy Sokolenko
 */
class ConfigurationStarter {

    def start(String configFilename = 'settings') {
        this.class.classLoader.rootLoader.addURL(new URL('file:///./'))
        ConfigurationManager.loadCascadedPropertiesFromResources(configFilename)

        def curator = CuratorFrameworkFactory.builder()
                .connectString(ConfigurationManager.getConfigInstance().getString('zk.connectString'))
                .retryPolicy(new BoundedExponentialBackoffRetry(10, 1000, 3))
                .build()
        curator.start()

        def source = new ZooKeeperConfigurationSource(curator, '/me/sokolenko/microservice')
        source.start()

        def composite = new ConcurrentCompositeConfiguration()
        composite.addConfiguration(new DynamicWatchedConfiguration(source))

        ConfigurationManager.install(composite)
    }

}
