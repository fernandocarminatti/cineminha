package com.pssa.cineminha.dto;

import java.util.List;

public record SessionStatus(boolean authenticated, String username, List<String> roles) {
}