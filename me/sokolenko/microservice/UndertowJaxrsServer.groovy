package me.sokolenko.microservice

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet
import com.sun.jersey.api.core.DefaultResourceConfig
import com.sun.jersey.api.core.ResourceConfig
import com.sun.jersey.api.json.JSONConfiguration
import io.undertow.Undertow
import io.undertow.server.handlers.PathHandler
import io.undertow.servlet.api.DeploymentInfo
import io.undertow.servlet.api.DeploymentManager
import io.undertow.servlet.api.ServletContainer
import io.undertow.servlet.util.ImmediateInstanceHandle

import javax.servlet.ServletException

import static io.undertow.servlet.Servlets.deployment
import static io.undertow.servlet.Servlets.servlet

/**
 * Created by galak on 9/21/14.
 */
@Grab(group = 'com.sun.jersey', module = 'jersey-server', version = '1.11')
@Grab(group = 'com.sun.jersey', module = 'jersey-servlet', version = '1.11')
@Grab(group = 'com.sun.jersey', module = 'jersey-json', version = '1.11')
@Grab(group = 'io.undertow', module = 'undertow-core', version = '1.0.15.Final')
@Grab(group = 'io.undertow', module = 'undertow-servlet', version = '1.0.15.Final')
@Grab(group = 'com.netflix.hystrix', module = 'hystrix-core', version = '1.4.0-RC5')
@Grab(group = 'com.netflix.hystrix', module = 'hystrix-metrics-event-stream', version = '1.4.0-RC5')
public class UndertowJaxrsServer {
    final PathHandler root = new PathHandler();
    final ServletContainer container = ServletContainer.Factory.newInstance();
    protected Undertow server;

    public UndertowJaxrsServer deployApi(Class<?>... resourceClasses) {
        def config = new DefaultResourceConfig(resourceClasses)
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true)
        //workaround class loading bug, possibly related to Java 8
        config.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, true)

        deploy(deployment()
                .setContextPath('/api')
                .setDeploymentName('rest')
                .setClassLoader(this.class.classLoader)
                .addServlet(
                servlet('rest', com.sun.jersey.spi.container.servlet.ServletContainer.class, { ->

                    new ImmediateInstanceHandle(new com.sun.jersey.spi.container.servlet.ServletContainer(
                            config
                    ))
                })
                        .addMapping('/*'))
        )

        this
    }

    public UndertowJaxrsServer deployHybris() {
        deploy(deployment()
                .setContextPath("/hystrixDashboard")
                .setDeploymentName("hystrixDashboard")
                .setClassLoader(this.class.classLoader)
                .addServlet(servlet("hystrixDashboard", HystrixMetricsStreamServlet.class)
                .addMapping("/hystrix.stream")))
    }

    /**
     * Adds an arbitrary web deployment to underlying Undertow server.  This is for your own deployments
     *
     * @param builder
     * @return
     */
    private UndertowJaxrsServer deploy(DeploymentInfo builder) {
        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();
        try {
            root.addPrefixPath(builder.getContextPath(), manager.start());
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public UndertowJaxrsServer start(int port, String host = '0.0.0.0') {
        Undertow.builder()
                .setHandler(root)
                .addHttpListener(port, host)
                .build()
                .start();

        return this;
    }

    public void stop() {
        server.stop();
    }

}
