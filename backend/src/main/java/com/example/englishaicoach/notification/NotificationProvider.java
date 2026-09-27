package com.example.englishaicoach.notification;

/** Ranh giới gửi push tới thiết bị, độc lập với SDK FCM. */
public interface NotificationProvider {

    /** Gửi payload đã được dịch vụ thông báo chuẩn bị tới một thiết bị. */
    void send(String pushToken, String title, String body);
}
