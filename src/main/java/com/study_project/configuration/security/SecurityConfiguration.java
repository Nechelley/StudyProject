package com.study_project.configuration.security;

import com.auth0.jwt.algorithms.Algorithm;
import com.study_project.enums.ProfileEnum;
import com.study_project.repository.UserRepository;
import com.study_project.service.TokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.study_project.filter.AuthenticationByTokenFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {
	public static final String AUTHENTICATION_TYPE = "Bearer";
	public static final String AUTHORIZATION_HEADER_REQUEST = "Authorization";

	private static String SECRET;

	private final TokenService tokenService;

	private final UserRepository userRepository;

	public SecurityConfiguration(TokenService tokenService, UserRepository userRepository, JwtProperties jwtProperties) {
		this.tokenService = tokenService;
		this.userRepository = userRepository;
		SECRET = jwtProperties.secret();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) {
		return
				http.csrf(AbstractHttpConfigurer::disable)
						.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
						.authorizeHttpRequests(req -> {
							req.requestMatchers(HttpMethod.POST, "/authentication").permitAll();
							req.requestMatchers(HttpMethod.POST, "/user").permitAll();
							req.requestMatchers(HttpMethod.POST, "/user/admin").hasAuthority(ProfileEnum.ADMIN.getName());
							req.requestMatchers(HttpMethod.GET, "/user").hasAuthority(ProfileEnum.ADMIN.getName());
							req.requestMatchers(HttpMethod.GET, "/user/{id}").authenticated();
							req.anyRequest().authenticated();
						})
						.addFilterBefore(new AuthenticationByTokenFilter(tokenService, userRepository), UsernamePasswordAuthenticationFilter.class)
						.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return getEncrypter();
	}

	public static Algorithm getAlgorithm() {
		return Algorithm.HMAC256(SECRET);
	}

	public static PasswordEncoder getEncrypter() {
		return new BCryptPasswordEncoder();
	}

}
