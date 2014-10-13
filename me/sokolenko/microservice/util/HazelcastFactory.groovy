package me.sokolenko.microservice.util

import com.hazelcast.config.Config
import com.hazelcast.config.NetworkConfig
import com.hazelcast.config.SerializerConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.nio.serialization.StreamSerializer

/**
 * @author Anatoliy Sokolenko
 */
@Grab(group='com.hazelcast', module='hazelcast', version='3.3.1')
class HazelcastFactory {

    def HazelcastInstance instance

    HazelcastFactory(String name, Map<Class, StreamSerializer> serializers = [:]) {
        Config cfg = new Config()
        cfg.groupConfig.name = name
//        cfg.setProperty('hazelcast.initial.min.cluster.size', '2')

        def ip = InetAddress.getLocalHost().getHostAddress()

        def netCfg = new NetworkConfig()
        netCfg.setPublicAddress(ip)

        netCfg.join.awsConfig.enabled = false
        netCfg.join.tcpIpConfig.enabled = false
        netCfg.join.multicastConfig.enabled = true
        netCfg.join.multicastConfig.addTrustedInterface(ip)

        cfg.setNetworkConfig(netCfg)

        serializers.each { clazz, serializer ->
            cfg.serializationConfig.addSerializerConfig(new SerializerConfig()
                    .setImplementation(serializer)
                    .setTypeClass(clazz)
            )
        }

        this.instance = Hazelcast.newHazelcastInstance(cfg)
    }

    def getMap(String name) {
        instance.getMap(name)
    }

    def getMultiMap(String name) {
        instance.getMultiMap(name)
    }

}
