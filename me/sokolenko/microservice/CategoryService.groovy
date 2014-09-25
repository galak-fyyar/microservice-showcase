package me.sokolenko.microservice

@Grab(group = 'javax.ws.rs', module = 'jsr311-api', version = '1.1.1')
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.2')
@Grab(group='com.netflix.eureka', module='eureka-client', version='1.1.141')

import com.netflix.appinfo.ApplicationInfoManager
import com.netflix.appinfo.InstanceInfo
import com.netflix.appinfo.MyDataCenterInstanceConfig
import com.netflix.discovery.DefaultEurekaClientConfig
import com.netflix.discovery.DiscoveryManager

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces

/**
 * @author Anatoliy Sokolenko
 */

@Path("/category/v1")
@Produces("application/json")
class CategoryV1Resource {

    @GET
    @Path('/{categoryId}')
    def get(@PathParam('categoryId') int categoryId) {
        return new CategoryInfo(id: categoryId, name: 'Hello, World!')
    }

}

class CategoryInfo {
    def int id;

    def String name;
}

this.class.classLoader.rootLoader.addURL(new URL('file:///./'))

System.setProperty('eureka.client.props', 'eureka-client')
DiscoveryManager.getInstance().initComponent(
        new MyDataCenterInstanceConfig('category.'),
        new DefaultEurekaClientConfig('category.'))
ApplicationInfoManager.getInstance().setInstanceStatus(InstanceInfo.InstanceStatus.UP)

new UndertowJaxrsServer().start(13232)
        .deployHybris()
        .deployApi(CategoryV1Resource.class)
