package com.pssa.cineminha.controller;

import com.pssa.cineminha.dto.SessionStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @GetMapping("/session")
    public ResponseEntity<SessionStatus> checkSession(Principal principal){
        if(principal == null){
            return ResponseEntity.ok(new SessionStatus(false,null, null));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return ResponseEntity.ok(new SessionStatus(
                true,
                principal.getName(),
                roles));
    }
}