package com.queueless.repository;
import com.queueless.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface EmailOtpRepository extends JpaRepository<EmailOtp, UUID> {
    Optional<EmailOtp> findByEmailAndOtp(String email, String otp);
    void deleteByEmail(String email);
}
