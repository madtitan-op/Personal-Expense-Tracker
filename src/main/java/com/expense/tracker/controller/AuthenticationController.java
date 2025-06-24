package com.expense.tracker.controller;

import com.expense.tracker.dto.AuthenticationRequestDTO;
import com.expense.tracker.model.User;
import com.expense.tracker.service.JwtService;
import com.expense.tracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication Controller", description = "REST endpoints for User authentication")
@RequiredArgsConstructor
@RestController
@RequestMapping("api/auth")
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationManager authMgr;
    private final JwtService jwtService;

    @Operation(summary = "Register new User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping("register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            userService.register(user);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityExistsException e) {
            return new ResponseEntity<>("User already exists!", HttpStatus.CONFLICT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something unexpected happened!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get authentication token by logging in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credentials verified and token generated"),
            @ApiResponse(responseCode = "401", description = "Invalid Credentials")
    })
    @PostMapping("login")
    public ResponseEntity<String> login(@RequestBody AuthenticationRequestDTO authReq) {
        try {
            Authentication authentication = authMgr
                    .authenticate(new UsernamePasswordAuthenticationToken(authReq.username(), authReq.password()));
            if (authentication.isAuthenticated()) {
                String token = jwtService.generateToken(authReq.username());
                return new ResponseEntity<>(token, HttpStatus.OK);
            }

            return new ResponseEntity<>("Invalid credentials!", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something unexpected happened!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
