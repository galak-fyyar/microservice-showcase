package me.sokolenko.microservice

@Grab(group='ch.qos.logback', module='logback-classic', version='1.1.2')
@Grab(group='com.google.guava', module='guava', version='18.0')
@Grab(group='com.netflix.eureka', module='eureka-client', version='1.1.141')
@Grab(group='com.netflix.ribbon', module='ribbon', version='2.0-RC9')
@Grab(group='com.netflix.ribbon', module='ribbon-core', version='2.0-RC9')
@Grab(group='com.netflix.ribbon', module='ribbon-httpclient', version='2.0-RC9')
@Grab(group='com.netflix.ribbon', module='ribbon-loadbalancer', version='2.0-RC9')
@Grab(group='com.netflix.ribbon', module='ribbon-eureka', version='2.0-RC9')
@Grab(group='org.codehaus.jackson', module='jackson-mapper-asl', version='1.9.13')

import com.google.common.util.concurrent.RateLimiter
import com.netflix.appinfo.ApplicationInfoManager
import com.netflix.appinfo.InstanceInfo
import com.netflix.appinfo.MyDataCenterInstanceConfig
import com.netflix.config.ConfigurationManager
import com.netflix.discovery.DefaultEurekaClientConfig
import com.netflix.discovery.DiscoveryManager
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext
import com.netflix.ribbon.ClientOptions
import com.netflix.ribbon.UnsuccessfulResponseException
import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.HttpResponseStatus
import io.reactivex.netty.protocol.http.client.HttpClientResponse
import org.codehaus.jackson.map.ObjectMapper

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces

import static com.netflix.client.config.DefaultClientConfigImpl.getClientConfigWithDefaultValues
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey
import static com.netflix.hystrix.HystrixObservableCommand.Setter.withGroupKey
import static com.netflix.ribbon.Ribbon.createHttpResourceGroup

class CategoryInfo {
    def int id;

    def String name;
}

@Path("/hello")
class Resource {
    @GET
    @Produces("text/plain")
    def String get() {
        return "hello world";
    }
}

this.class.classLoader.rootLoader.addURL(new URL('file:///./'))

ConfigurationManager.loadPropertiesFromResources('eureka-client.properties')

System.setProperty('eureka.client.props', 'eureka-client')
DiscoveryManager.getInstance().initComponent(
        new MyDataCenterInstanceConfig('navigation.'),
        new DefaultEurekaClientConfig('navigation.'))
ApplicationInfoManager.getInstance().setInstanceStatus(InstanceInfo.InstanceStatus.UP)

new UndertowJaxrsServer().start(13131)
.deployHybris()
.deployApi(Resource.class)

def threshold = RateLimiter.create(10)          //TODO replace with Hystrix

def executor = Executors.newFixedThreadPool(1)


def categoryResourceGroup = createHttpResourceGroup('category-client',
        ClientOptions.from(getClientConfigWithDefaultValues('category-client')))
def getCategoryTpl = categoryResourceGroup.newTemplateBuilder('getCategory', ByteBuf.class)     //TODO use another class
        .withMethod('GET')
        .withUriTemplate('/api/category/v1/{categoryId}')
        .withFallbackProvider({ -> null})
//        .withCacheProvider('/api/category/v1/{categoryId}', new GuavaCacheProviderFactory().createCacheProvider())
        .withHystrixProperties(withGroupKey(asKey('Category'))
            .andCommandKey(HystrixCommandKey.Factory.asKey('GetCategoryChildren')))
        .withResponseValidator({HttpClientResponse resp ->
            if (!HttpResponseStatus.OK.equals(resp.status)) {
                throw new UnsuccessfulResponseException('Should be OK')
            }
        })
        .build()

def mapper = new ObjectMapper()

while (true) {
    threshold.acquire()

    executor.submit { ->
        def ctx = HystrixRequestContext.initializeContext()

        def getCategoryReq = getCategoryTpl.requestBuilder()
            .withRequestProperty('categoryId', 510)
            .build()

//        println getCategoryReq.execute()

        try {
            getCategoryReq.observe().doOnNext({ ByteBuf resp ->
                println mapper.readValue(resp.array(), CategoryInfo.class)
            }).doOnError({ Throwable t ->
                println t.toString()
            })
        } catch (Throwable t) {
            println t.toString()
        }

        ctx.shutdown()
    }
}
