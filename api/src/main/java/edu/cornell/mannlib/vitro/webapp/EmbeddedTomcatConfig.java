package edu.cornell.mannlib.vitro.webapp;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedTomcatConfig {
    private static final Logger logger= LoggerFactory.getLogger(EmbeddedTomcatConfig.class);


    @Bean
    public ServletWebServerFactory serverFactory(){
        TomcatServletWebServerFactory tomcatFactory = new TomcatServletWebServerFactory(){

            @Override
            protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
                tomcat.enableNaming();
                return super.getTomcatWebServer(tomcat);
            }

            @Override
            protected void postProcessContext(Context context){

                ContextEnvironment environment = new ContextEnvironment();

                environment.setType(java.lang.String.class.getName());
                environment.setName("vitro/home");
                environment.setValue("/home/veljkomaksimovic/Lyrasis/Vitro/home");
                environment.setOverride(true);

                context.getNamingResources().addEnvironment(environment);
            }
        };
        return tomcatFactory;
    }
}
