package com.study_project.filter;

import com.study_project.validation.exception.UnauthenticatedUserException;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.study_project.model.User;
import com.study_project.repository.UserRepository;
import com.study_project.service.TokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.study_project.configuration.security.SecurityConfiguration.AUTHENTICATION_TYPE;
import static com.study_project.configuration.security.SecurityConfiguration.AUTHORIZATION_HEADER_REQUEST;

import java.io.IOException;
import java.util.Optional;

@Component
public class AuthenticationByTokenFilter extends OncePerRequestFilter {

	private final TokenService tokenService;
	private final UserRepository userRepository;

	public AuthenticationByTokenFilter(TokenService tokenService, UserRepository userRepository) {
		this.tokenService = tokenService;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
		String token = recoverToken(request);
		boolean isValid = tokenService.isValidToken(token);
		if (isValid) {
			authenticateUser(token);
		}

		filterChain.doFilter(request, response);
	}

	private String recoverToken(HttpServletRequest request) {
		String token = request.getHeader(AUTHORIZATION_HEADER_REQUEST);

		if (token == null || !token.startsWith(AUTHENTICATION_TYPE + " ")) {
			return null;
		}

		return token.substring(7);
	}

	private void authenticateUser(String token) {
		Long userId = tokenService.getUserId(token);
		Optional<User> userInDatabase = userRepository.findById(userId);
		if (userInDatabase.isPresent()) {
			User user = userInDatabase.get();

			boolean tokensMatch = user.getTokenVersion() == tokenService.getTokenVersion(token);
			if (tokensMatch) {
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
	}

}
