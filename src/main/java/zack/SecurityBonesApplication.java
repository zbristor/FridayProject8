package zack;

import it.ozimov.springboot.mail.configuration.EnableEmailTools;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEmailTools
public class SecurityBonesApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityBonesApplication.class, args);
	}
}
