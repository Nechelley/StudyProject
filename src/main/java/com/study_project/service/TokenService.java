package com.study_project.service;

import com.auth0.jwt.JWT;
import com.study_project.configuration.security.JwtProperties;
import com.study_project.configuration.security.SecurityConfiguration;
import com.study_project.validation.exception.UnauthenticatedUserException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.study_project.model.User;

import java.util.Date;

@Service
public class TokenService {

	private static final String TOKEN_VERSION = "tokenVersion";

	private final String ISSUER;
	private final String EXPIRATION;

	public TokenService(JwtProperties jwtProperties) {
		ISSUER = jwtProperties.issuer();
		EXPIRATION = jwtProperties.expiration();
	}

	public String createToken(Authentication authentication) throws UnauthenticatedUserException {
		User userLogged = (User) authentication.getPrincipal();
		Date today = new Date();
		Date expirationDate = new Date(today.getTime() + Long.parseLong(EXPIRATION));

		if (userLogged == null) {
			throw new UnauthenticatedUserException();
		}
		return JWT.create()
				.withIssuer(ISSUER)
				.withSubject(userLogged.getId().toString())
				.withClaim(TOKEN_VERSION, userLogged.getTokenVersion())
				.withIssuedAt(today)
				.withExpiresAt(expirationDate)
				.sign(SecurityConfiguration.getAlgorithm());
	}

	public boolean isValidToken(String token) {
		try {
			JWT.require(SecurityConfiguration.getAlgorithm())
					.withIssuer(ISSUER)
					.build()
					.verify(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Long getUserId(String token) {
		return Long.parseLong(
				JWT.require(SecurityConfiguration.getAlgorithm())
						.withIssuer(ISSUER)
						.build()
						.verify(token)
						.getSubject()
		);
	}

	public int getTokenVersion(String token) {
		return JWT.require(SecurityConfiguration.getAlgorithm())
				.withIssuer(ISSUER)
				.build()
				.verify(token)
				.getClaim(TOKEN_VERSION).asInt();
	}

}
