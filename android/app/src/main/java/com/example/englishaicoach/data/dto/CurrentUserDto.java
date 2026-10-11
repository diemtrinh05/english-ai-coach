package com.example.englishaicoach.data.dto;

public final class CurrentUserDto {
    public String id;
    public String email;
    public String fullName;
    public String role;
    public boolean valid() {
        return id != null && !id.isEmpty() && email != null && fullName != null
                && ("USER".equals(role) || "ADMIN".equals(role));
    }
}
