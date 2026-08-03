package com.template.api.feature.user;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.template.api.common.error.ConflictException;
import com.template.api.common.error.ResourceNotFoundException;
import com.template.api.common.paging.PageQuery;
import com.template.api.common.paging.PageResponse;
import com.template.api.common.paging.Pageables;
import com.template.api.common.paging.PagedReads;
import com.template.api.common.paging.Specs;
import com.template.api.common.select.FieldSelection;
import com.template.api.config.security.SecurityProperties;

@Service
@Transactional
public class UserService {

	private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "USER");

	static final Set<String> SORT_FIELDS = Set.of("username", "enabled", "createdAt", "updatedAt");
	static final Set<String> SELECT_ROOTS = Set.of("id", "username", "roles", "enabled", "createdAt");
	static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "username");

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecurityProperties securityProperties;
	private final PagedReads pagedReads;

	public UserService(
			AppUserRepository appUserRepository,
			PasswordEncoder passwordEncoder,
			SecurityProperties securityProperties,
			PagedReads pagedReads) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.securityProperties = securityProperties;
		this.pagedReads = pagedReads;
	}

	@Transactional(readOnly = true)
	public UserResponse getByUsername(String username) {
		return UserResponse.from(requireUser(username));
	}

	@Transactional(readOnly = true)
	public Object getByUsername(String username, String select) {
		UserResponse full = UserResponse.from(requireUser(username));
		return FieldSelection.applyOne(full, select, SELECT_ROOTS);
	}

	@Transactional(readOnly = true)
	public Object listAll(PageQuery query) {
		Specification<AppUser> spec = query.normalizedSearch()
				.map(UserSpecs::matchesSearch)
				.orElseGet(Specs::unrestricted);
		if (query.includes().wantsCountOnly()) {
			return pagedReads.countOnly(AppUser.class, spec);
		}
		Pageable pageable = Pageables.toPageable(query, SORT_FIELDS, DEFAULT_SORT);
		PageResponse<UserResponse> page =
				pagedReads.find(AppUser.class, spec, pageable, query, UserResponse::from);
		return FieldSelection.apply(page, query, SELECT_ROOTS);
	}

	public UserResponse register(RegisterRequest request) {
		if (!securityProperties.allowRegistration()) {
			throw new org.springframework.security.access.AccessDeniedException("Registration is disabled");
		}
		return createInternal(request.username(), request.password(), List.of("USER"));
	}

	public UserResponse createByAdmin(CreateUserRequest request) {
		List<String> roles = normalizeRoles(request.roles());
		return createInternal(request.username(), request.password(), roles);
	}

	public void changePassword(String username, ChangePasswordRequest request) {
		AppUser user = requireUser(username);
		if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
			throw new BadCredentialsException("Current password is incorrect");
		}
		user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
		appUserRepository.save(user);
	}

	private UserResponse createInternal(String username, String rawPassword, List<String> roles) {
		String normalized = username.trim();
		if (appUserRepository.existsByUsernameIgnoreCase(normalized)) {
			throw new ConflictException("Username already exists: " + normalized);
		}
		String roleCsv = String.join(",", roles);
		AppUser saved = appUserRepository.save(
				new AppUser(normalized, passwordEncoder.encode(rawPassword), roleCsv));
		return UserResponse.from(saved);
	}

	private AppUser requireUser(String username) {
		return appUserRepository.findByUsernameIgnoreCase(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
	}

	private static List<String> normalizeRoles(List<String> roles) {
		List<String> normalized = roles.stream()
				.map(r -> r.trim().toUpperCase(Locale.ROOT))
				.filter(r -> !r.isEmpty())
				.distinct()
				.collect(Collectors.toList());
		if (normalized.isEmpty()) {
			throw new IllegalArgumentException("At least one role is required");
		}
		for (String role : normalized) {
			if (!ALLOWED_ROLES.contains(role)) {
				throw new IllegalArgumentException("Unsupported role: " + role);
			}
		}
		return normalized;
	}
}
