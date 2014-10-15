package me.sokolenko.microservice.util

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet
import com.netflix.hystrix.contrib.requestservlet.HystrixRequestContextServletFilter
import com.sun.jersey.api.core.DefaultResourceConfig
import com.sun.jersey.api.core.ResourceConfig
import com.sun.jersey.api.json.JSONConfiguration
import io.undertow.Undertow
import io.undertow.server.handlers.PathHandler
import io.undertow.servlet.api.DeploymentInfo
import io.undertow.servlet.api.DeploymentManager
import io.undertow.servlet.api.ServletContainer
import io.undertow.servlet.util.ImmediateInstanceHandle
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.DispatcherType
import javax.servlet.ServletException

import static com.netflix.config.ConfigurationManager.getConfigInstance
import static io.undertow.servlet.Servlets.deployment
import static io.undertow.servlet.Servlets.servlet
import static io.undertow.servlet.Servlets.filter

/**
 * @author Anatoliy Sokolenko
 */
public class ServerStarter {

    private static final Logger logger = LoggerFactory.getLogger(ServerStarter.class)

    final PathHandler root = new PathHandler();
    final ServletContainer container = ServletContainer.Factory.newInstance();
    protected Undertow server;

    public ServerStarter deployApi(Object... resources) {
        def config = new DefaultResourceConfig()
        config.singletons.addAll(resources)

        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true)
        //workaround class loading bug, possibly related to Java 8
        config.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, true)

        deploy(deployment()
                .setContextPath('/api')
                .setDeploymentName('rest')
                .setClassLoader(this.class.classLoader)
                .addFilters(filter('loggingFilter', ExceptionLoggingFilter.class))
                .addFilterUrlMapping('loggingFilter', '/*', DispatcherType.REQUEST)
                .addFilters(filter('hystrixContext', HystrixRequestContextServletFilter.class))
                .addFilterUrlMapping('hystrixContext', '/*', DispatcherType.REQUEST)
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

    public ServerStarter deployHystrix() {
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
    private ServerStarter deploy(DeploymentInfo builder) {
        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();
        try {
            root.addPrefixPath(builder.getContextPath(), manager.start());
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public ServerStarter start(String namespace) {
        if (!namespace.endsWith('.')) {
            namespace += '.'
        }

        def retry = 0
        for (def port = getConfigInstance().getInt(namespace + 'start.port'); true; port++) {
            try {
                logger.info("Trying port $port")

                def starter = start(port)
                logger.info("Using port $port")
                getConfigInstance().setProperty(namespace + 'port', port)

                return starter
            } catch (RuntimeException e) {
                if (e.cause instanceof BindException) {
                    logger.info("Port $port is already in use")
                } else {
                    throw e
                }
            }

            retry++
            if (retry > 9) {
                throw new IllegalStateException('Could not bind not any port')
            }
        }
    }

    public ServerStarter start(int port, String host = '0.0.0.0') {
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
