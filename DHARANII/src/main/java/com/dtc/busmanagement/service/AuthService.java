package com.dtc.busmanagement.service;

import com.dtc.busmanagement.dto.ChangePasswordRequest;
import com.dtc.busmanagement.dto.LoginRequest;
import com.dtc.busmanagement.dto.LoginResponse;
import com.dtc.busmanagement.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    LoginResponse register(RegisterRequest registerRequest);
    void changePassword(String username, ChangePasswordRequest request);
}
