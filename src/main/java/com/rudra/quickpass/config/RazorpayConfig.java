package com.rudra.quickpass.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    private static final Logger log = LoggerFactory.getLogger(RazorpayConfig.class);

    @Value("${razorpay.key.id:rzp_test_placeholder}")
    private String keyId;

    @Value("${razorpay.key.secret:placeholder_secret}")
    private String keySecret;

    @Bean
    public RazorpayClient razorpayClient() {
        if (isConfigured()) {
            try {
                return new RazorpayClient(keyId, keySecret);
            } catch (RazorpayException e) {
                log.error("Failed to initialize RazorpayClient: {}", e.getMessage());
            }
        } else {
            log.info("Razorpay keys not yet provided in launch.json. Running in simulated test mode.");
        }
        return null;
    }

    public boolean isConfigured() {
        return keyId != null && !keyId.isBlank()
                && !keyId.contains("placeholder") && !keyId.contains("YOUR_KEY")
                && keySecret != null && !keySecret.isBlank()
                && !keySecret.contains("placeholder") && !keySecret.contains("YOUR_KEY");
    }

    public String getKeyId() {
        return keyId;
    }

    public String getKeySecret() {
        return keySecret;
    }
}

