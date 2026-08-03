package com.template.api.config.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.template.api.feature.user.AppUserRepository;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

	private final AppUserRepository appUserRepository;

	public DatabaseUserDetailsService(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return appUserRepository.findByUsernameIgnoreCase(username)
				.map(user -> User.withUsername(user.getUsername())
						.password(user.getPasswordHash())
						.disabled(!user.isEnabled())
						.roles(user.roleList().toArray(String[]::new))
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}
}
