package com.template.api.feature.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.template.api.config.security.SecurityProperties;

/**
 * Optional demo users for local template DX. Disabled when app.security.seed-demo-users=false.
 */
@Component
@Order(10)
@EnableConfigurationProperties(SecurityProperties.class)
public class DemoUserSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecurityProperties securityProperties;

	public DemoUserSeeder(
			AppUserRepository appUserRepository,
			PasswordEncoder passwordEncoder,
			SecurityProperties securityProperties) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.securityProperties = securityProperties;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (!securityProperties.seedDemoUsers()) {
			log.info("Demo user seeding disabled (app.security.seed-demo-users=false)");
			return;
		}
		seed("admin", "admin123", "ADMIN");
		seed("user", "user123", "USER");
	}

	private void seed(String username, String rawPassword, String roles) {
		if (appUserRepository.existsByUsernameIgnoreCase(username)) {
			return;
		}
		appUserRepository.save(new AppUser(username, passwordEncoder.encode(rawPassword), roles));
		log.info("Seeded demo user '{}'", username);
	}
}
