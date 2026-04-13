package com.eScheduler.controllers;

import com.eScheduler.model.UserLogin;
import com.eScheduler.requests.LoginRequestDTO;
import com.eScheduler.responses.ResponseDTO;
import com.eScheduler.responses.customDTOClasses.AuthenticationDTO;
import com.eScheduler.services.GoogleAuthService;
import com.eScheduler.services.UserLoginService;
import com.eScheduler.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping(path = "/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserLoginService userLoginService;
    private final JwtUtil jwtUtil;
    private final GoogleAuthService googleAuthService;

    public AuthController(GoogleAuthService googleAuthService,AuthenticationManager authenticationManager, UserLoginService userLoginService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userLoginService = userLoginService;
        this.jwtUtil = jwtUtil;
        this.googleAuthService = googleAuthService;
    }

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequestDTO credentials) {
//        try {
//            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(credentials.getEmail(), credentials.getPassword()));
//            UserLogin userLogin = userLoginService.getUserByCredentials(credentials.getEmail());
//            String token = jwtUtil.generateToken(userLogin.getEmail());
//            return ResponseEntity.ok(new ResponseDTO<>("User found", true, token));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.ok(new ResponseDTO<>("Password or Email is incorrect!", false, null));
//        }
//    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            String email = googleAuthService.validateGoogleToken(request.getIdToken());
            System.out.println(email);

            if (email == null) {
                return ResponseEntity.badRequest().body(new ResponseDTO<>("Invalid Google token", false, null));
            }

            boolean isAdmin = userLoginService.getUserByCredentials(email);

            System.out.println("USER FOUND: " + email);
            System.out.println("IS ADMIN: " + isAdmin);


            String jwtToken = jwtUtil.generateToken(email,isAdmin);

            System.out.println("JWT: " + jwtToken);

            return ResponseEntity.ok(new AuthenticationDTO("Login successful", true, jwtToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new AuthenticationDTO("Authentication failed", false, null));
        }
    }
}
