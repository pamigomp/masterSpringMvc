package masterSpringMvc.config;

import java.io.IOException;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class SslConfig {

	@Bean
	public EmbeddedServletContainerFactory servletContainer() throws IOException {
		TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory = new TomcatEmbeddedServletContainerFactory();
		tomcatEmbeddedServletContainerFactory.addAdditionalTomcatConnectors(createSslConnector());
		return tomcatEmbeddedServletContainerFactory;
	}

	private Connector createSslConnector() throws IOException {
		Connector connector = new Connector(Http11NioProtocol.class.getName());
		Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
		connector.setPort(8443);
		connector.setSecure(true);
		connector.setScheme("https");
		protocol.setSSLEnabled(true);
		protocol.setKeyAlias("masterspringmvc");
		protocol.setKeystorePass("haslo123");
		protocol.setKeyPass("haslo123");
		protocol.setKeystoreFile(new ClassPathResource("tomcat.keystore").getFile().getAbsolutePath());
		protocol.setSSLProtocol("TLSv1.2");
		return connector;
	}
}
