package me.sokolenko.microservice.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

/**
 * @author Anatoliy Sokolenko
 */
class ExceptionLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionLoggingFilter.class)

    @Override
    void init(FilterConfig filterConfig) throws ServletException {
        logger.debug('ExceptionLoggingFilter initialized')
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            chain.doFilter(request, response)
        } catch (Throwable e) {
            logger.error('Uncaught error occurred', e)
        }

    }

    @Override
    void destroy() {

    }
}
