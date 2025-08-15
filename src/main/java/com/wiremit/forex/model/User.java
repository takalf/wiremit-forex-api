package com.wiremit.forex.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User extends BaseEntity implements UserDetails {

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Builder.Default
    private boolean enabled = true;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;

    @Builder.Default
    @Column(name = "password_expiry_days", nullable = false)
    private Integer passwordExpiryDays = 90; // Default 90 days

    @Builder.Default
    @Column(name = "force_password_change", nullable = false)
    private Boolean forcePasswordChange = false;

    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Builder.Default
    @Column(name = "max_login_attempts", nullable = false)
    private Integer maxLoginAttempts = 5; // Default 5 attempts

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_failed_login_at")
    private LocalDateTime lastFailedLoginAt;

    @PrePersist
    protected void onCreate() {
        if (passwordChangedAt == null) {
            passwordChangedAt = LocalDateTime.now();
        }
        if (passwordExpiresAt == null && passwordExpiryDays != null) {
            passwordExpiresAt = passwordChangedAt.plusDays(passwordExpiryDays);
        }
    }

    public boolean isPasswordExpired() {
        return passwordExpiresAt != null && LocalDateTime.now().isAfter(passwordExpiresAt);
    }

    public boolean isAccountTemporarilyLocked() {
        return accountLockedUntil != null && LocalDateTime.now().isBefore(accountLockedUntil);
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
        this.lastLoginAt = LocalDateTime.now();
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        this.lastFailedLoginAt = LocalDateTime.now();

        if (this.failedLoginAttempts >= this.maxLoginAttempts) {
            // Lock account for 30 minutes after max attempts
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();
        this.passwordExpiresAt = this.passwordChangedAt.plusDays(this.passwordExpiryDays);
        this.forcePasswordChange = false;
    }

    public long getDaysUntilPasswordExpiry() {
        if (passwordExpiresAt == null) return Long.MAX_VALUE;
        return java.time.Duration.between(LocalDateTime.now(), passwordExpiresAt).toDays();
    }

    public int getRemainingLoginAttempts() {
        return Math.max(0, maxLoginAttempts - failedLoginAttempts);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isAccountTemporarilyLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !isPasswordExpired() && !forcePasswordChange;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}