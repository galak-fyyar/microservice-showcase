package me.sokolenko.microservice.util

import com.hazelcast.config.Config
import com.hazelcast.config.NetworkConfig
import com.hazelcast.config.SerializerConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.nio.serialization.StreamSerializer
import com.netflix.config.ConfigurationManager

/**
 * @author Anatoliy Sokolenko
 */
class HazelcastFactory {

    def HazelcastInstance instance

    HazelcastFactory(String name, Map<Class, StreamSerializer> serializers = [:]) {
        Config cfg = new Config()
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

    def getQueue(String name) {
        instance.getQueue(name)
    }

}
