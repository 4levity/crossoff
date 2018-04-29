package org.pricelessfestival.crossoff.server;

import lombok.extern.log4j.Log4j2;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Created by ivan on 4/26/18.
 */
@Log4j2
class WebServer {

    public static int PORT = 8080;
    private Server server = null;

    void start() throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addFilter(WebServer.LogFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        server = new Server(PORT);
        server.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
        // use Jackson's JAX-RS JSON support (instead of Jersey's), and our own provider to inject ObjectMapper
        jerseyServlet.setInitParameter("jersey.config.server.provider.packages",
                "com.fasterxml.jackson.jaxrs.json," + GlobalObjectMapper.class.getPackage().getName());
        // register one resource class
        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", RootResource.class.getCanonicalName());

        server.start();
        log.info("Ready to handle requests!");
    }

    public static class LogFilter implements Filter {

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            chain.doFilter(request, response);
            if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                HttpServletRequest httpServletRequest = (HttpServletRequest) request;
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                log.info("{} {} {} : HTTP {}",
                        httpServletRequest.getRemoteAddr(),
                        httpServletRequest.getMethod(),
                        httpServletRequest.getPathInfo(),
                        httpServletResponse.getStatus());
            }
        }

        @Override
        public void destroy() {
        }
    }
}
