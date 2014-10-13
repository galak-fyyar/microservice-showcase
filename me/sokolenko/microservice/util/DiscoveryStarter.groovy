package me.sokolenko.microservice.util

import com.netflix.appinfo.ApplicationInfoManager
import com.netflix.appinfo.InstanceInfo
import com.netflix.appinfo.MyDataCenterInstanceConfig
import com.netflix.discovery.DefaultEurekaClientConfig
import com.netflix.discovery.DiscoveryManager

/**
 * @author Anatoliy Sokolenko
 */
@GrabConfig(systemClassLoader = true)
@Grab(group = 'com.netflix.eureka', module = 'eureka-client', version = '1.1.141')
class DiscoveryStarter {

    def start(String namespace) {
        if (!namespace.endsWith('.')) {
            namespace += '.'
        }

        DiscoveryManager.getInstance().initComponent(
                new MyDataCenterInstanceConfig(namespace),
                new DefaultEurekaClientConfig(namespace))

        ApplicationInfoManager.getInstance().setInstanceStatus(InstanceInfo.InstanceStatus.UP)

    }

}
