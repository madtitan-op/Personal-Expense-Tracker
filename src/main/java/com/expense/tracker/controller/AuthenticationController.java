package com.expense.tracker.controller;

import com.expense.tracker.dto.AuthenticationRequestDTO;
import com.expense.tracker.model.User;
import com.expense.tracker.service.JwtService;
import com.expense.tracker.service.UserService;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("api/auth")
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationManager authMgr;
    private final JwtService jwtService;

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

    @PostMapping("login")
    public ResponseEntity<String> login(@RequestBody AuthenticationRequestDTO authReq) {
        try {
            Authentication authentication = authMgr
                    .authenticate(new UsernamePasswordAuthenticationToken(authReq.username(), authReq.password()));
            if (authentication.isAuthenticated()) {
                String token = jwtService.generateToken(authReq.username());
                return new ResponseEntity<>(token, HttpStatus.OK);
            }

            return new ResponseEntity<>("Login Unsuccessful!", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something unexpected happened!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
