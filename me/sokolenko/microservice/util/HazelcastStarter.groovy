package me.sokolenko.microservice.util

import com.hazelcast.config.Config
import com.hazelcast.config.MapConfig
import com.hazelcast.config.MapStoreConfig
import com.hazelcast.config.NetworkConfig
import com.hazelcast.config.SerializerConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.MapStore
import com.hazelcast.nio.serialization.StreamSerializer
import com.netflix.config.ConfigurationManager

/**
 * @author Anatoliy Sokolenko
 */
class HazelcastStarter {

    def HazelcastInstance instance
    def Config cfg

    HazelcastStarter(String name) {
        cfg = new Config()
        cfg.groupConfig.name = name
//        cfg.setProperty('hazelcast.initial.min.cluster.size', '2')

        def hazelcastIf = ConfigurationManager.configInstance.getString('hazelcast.interface')
        //need this, because multicast does not work through VM
        def hazelcastIps = ConfigurationManager.configInstance.getString('hazelcast.ips')

        String ip
        if (hazelcastIf) {
            ip = NetworkInterface.getNetworkInterfaces().findResult { NetworkInterface it ->
                it.inetAddresses.findResult { InetAddress addr ->
                    if (addr.hostAddress.startsWith(hazelcastIf)) {
                        return addr.hostAddress
                    }
                }
            }
        } else {
            ip = InetAddress.getLocalHost().hostAddress
        }

        def netCfg = new NetworkConfig()
        netCfg.setPublicAddress(ip)

        netCfg.join.awsConfig.enabled = false
        netCfg.join.tcpIpConfig.enabled = true
        netCfg.join.tcpIpConfig.addMember(hazelcastIps)
        netCfg.join.multicastConfig.enabled = false
//        netCfg.join.multicastConfig.addTrustedInterface(ip)

        cfg.setNetworkConfig(netCfg)
    }

    def addSerializer(Class entityClazz, StreamSerializer serializer) {
        cfg.serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(serializer)
                .setTypeClass(entityClazz)
        )

        return this
    }

    def addMap(String map, Class<? extends MapStore> storeClazz) {
        def config = new MapConfig()
        config.mapStoreConfig = new MapStoreConfig()
        config.mapStoreConfig.className = storeClazz.name

        cfg.mapConfigs.put(map, config)

        return this
    }

    def start() {
        this.instance = Hazelcast.newHazelcastInstance(cfg)

        return instance
    }

}
