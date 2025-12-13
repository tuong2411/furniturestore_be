package com.example.demo.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Service
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(AuthenticationManager authenticationManager,
                     UserRepository userRepository,
                     PasswordEncoder passwordEncoder) {
    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {

	    Authentication authentication = authenticationManager.authenticate(
	        new UsernamePasswordAuthenticationToken(
	            request.getEmailOrUsername(),
	            request.getPassword()
	        )
	    );

	    // ✅ SET authentication vào SecurityContext
	    SecurityContextHolder.getContext().setAuthentication(authentication);

	    // ✅ TẠO SESSION + LƯU SECURITY CONTEXT
	    HttpSession session = httpRequest.getSession(true);
	    session.setAttribute(
	        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
	        SecurityContextHolder.getContext()
	    );

	    CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
	    User user = principal.getUser();

	    return new LoginResponse(
	        user.getUserId(),
	        user.getFullName(),
	        user.getEmail(),
	        user.getRoleName()
	    );
	}

  public void register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalStateException("Email đã được sử dụng");
    }

    String hash = passwordEncoder.encode(request.getPassword());
    userRepository.insertCustomer(request.getFullName(), request.getEmail(), hash);
  }
}
