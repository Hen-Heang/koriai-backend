package com.heang.koriaibackend.domain.push.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.heang.koriaibackend.domain.push.dto.RegisterDeviceRequest;
import com.heang.koriaibackend.domain.push.mapper.UserDeviceMapper;
import com.heang.koriaibackend.domain.push.model.UserDevice;
import com.heang.koriaibackend.domain.push.service.PushMessage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Firebase Cloud Messaging (FCM) delivery for mobile devices. Initializes a named
 * FirebaseApp from a service-account JSON and self-disables when credentials are
 * absent, so the app runs fine without FCM configured.
 *
 * Set the whole service-account JSON in firebase.credentials (env FIREBASE_CREDENTIALS).
 */
@Slf4j
@Service
public class FcmService {

    private static final String APP_NAME = "koriai-push";

    private final UserDeviceMapper deviceMapper;
    private final String credentialsJson;

    private FirebaseMessaging messaging;

    public FcmService(UserDeviceMapper deviceMapper,
                      @Value("${firebase.credentials:}") String credentialsJson) {
        this.deviceMapper = deviceMapper;
        this.credentialsJson = credentialsJson;
    }

    @PostConstruct
    void init() {
        if (credentialsJson == null || credentialsJson.isBlank()) {
            log.info("FCM disabled (no firebase.credentials configured)");
            return;
        }
        try {
            GoogleCredentials creds = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)));
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(creds).build();
            FirebaseApp app = FirebaseApp.getApps().stream()
                    .filter(a -> APP_NAME.equals(a.getName()))
                    .findFirst()
                    .orElseGet(() -> FirebaseApp.initializeApp(options, APP_NAME));
            this.messaging = FirebaseMessaging.getInstance(app);
            log.info("FCM enabled");
        } catch (Exception e) {
            log.error("Failed to initialize FCM; channel disabled: {}", e.getMessage());
            this.messaging = null;
        }
    }

    public boolean isConfigured() {
        return messaging != null;
    }

    public void registerDevice(Long userId, RegisterDeviceRequest req) {
        UserDevice device = UserDevice.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .fcmToken(req.token())
                .platform(req.platform())
                .build();
        deviceMapper.upsert(device);
    }

    public void unregisterDevice(String token) {
        if (token != null && !token.isBlank()) {
            deviceMapper.deleteByToken(token);
        }
    }

    /**
     * Send to all of a user's registered devices. Tokens FCM reports as
     * unregistered/invalid are pruned so they aren't retried.
     */
    public void sendToUser(Long userId, PushMessage message) {
        if (!isConfigured()) {
            return;
        }
        List<UserDevice> devices = deviceMapper.findByUserId(userId);
        for (UserDevice device : devices) {
            try {
                Message fcmMessage = Message.builder()
                        .setToken(device.getFcmToken())
                        .setNotification(Notification.builder()
                                .setTitle(message.title())
                                .setBody(message.body())
                                .build())
                        .putData("url", message.url() != null ? message.url() : "")
                        .build();
                messaging.send(fcmMessage);
            } catch (FirebaseMessagingException e) {
                MessagingErrorCode code = e.getMessagingErrorCode();
                if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                    deviceMapper.deleteByToken(device.getFcmToken());
                } else {
                    log.warn("FCM send failed for user {}: {}", userId, e.getMessage());
                }
            } catch (Exception e) {
                log.warn("FCM send error for user {}: {}", userId, e.getMessage());
            }
        }
    }
}
