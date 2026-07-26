package com.dtc.busmanagement.service.impl;

import com.dtc.busmanagement.dto.ChangePasswordRequest;
import com.dtc.busmanagement.dto.LoginRequest;
import com.dtc.busmanagement.dto.LoginResponse;
import com.dtc.busmanagement.entity.AuditLog;
import com.dtc.busmanagement.entity.User;
import com.dtc.busmanagement.exception.BadRequestException;
import com.dtc.busmanagement.exception.ResourceNotFoundException;
import com.dtc.busmanagement.repository.AuditLogRepository;
import com.dtc.busmanagement.repository.UserRepository;
import com.dtc.busmanagement.security.JwtUtils;
import com.dtc.busmanagement.security.UserDetailsImpl;
import com.dtc.busmanagement.service.AuthService;
import com.dtc.busmanagement.dto.RegisterRequest;
import com.dtc.busmanagement.entity.Role;
import com.dtc.busmanagement.enums.RoleName;
import com.dtc.busmanagement.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String roleName = userDetails.getAuthorities().iterator().next().getAuthority();

        // Save Audit Log
        AuditLog log = new AuditLog(loginRequest.getUsername(), "LOGIN_SUCCESS", "Logged in with role: " + roleName, "127.0.0.1");
        auditLogRepository.save(log);

        return new LoginResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roleName);
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new BadRequestException("Username '" + request.getUsername().trim() + "' is already taken.");
        }

        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new BadRequestException("Email '" + request.getEmail().trim() + "' is already registered.");
        }

        RoleName roleNameEnum = RoleName.ROLE_SCHEDULER;
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            String r = request.getRole().trim().toUpperCase();
            if (!r.startsWith("ROLE_")) {
                r = "ROLE_" + r;
            }
            try {
                roleNameEnum = RoleName.valueOf(r);
            } catch (IllegalArgumentException e) {
                roleNameEnum = RoleName.ROLE_SCHEDULER;
            }
        }

        final RoleName targetRoleName = roleNameEnum;
        Role userRole = roleRepository.findByName(targetRoleName)
                .orElseGet(() -> roleRepository.save(new Role(targetRoleName)));

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : "");
        user.setRole(userRole);
        user.setStatus("ACTIVE");

        userRepository.save(user);

        // Save Audit Log
        AuditLog auditLog = new AuditLog(user.getUsername(), "USER_REGISTER", "Registered user with role: " + userRole.getName().name(), "127.0.0.1");
        auditLogRepository.save(auditLog);

        // Auto-authenticate registered user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return new LoginResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), userRole.getName().name());
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Save Audit Log
        AuditLog log = new AuditLog(username, "PASSWORD_CHANGE", "Changed account password successfully", "127.0.0.1");
        auditLogRepository.save(log);
    }
}
