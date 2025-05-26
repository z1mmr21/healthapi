package in.z1mmr.healthapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.support.MultipartFilter;

@SpringBootApplication
public class HealthapiApplication {
	public static void main(String[] args) {
		SpringApplication.run(HealthapiApplication.class, args);
	}

}
