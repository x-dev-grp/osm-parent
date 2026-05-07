package com.xdev.onsignalNotifcations.impl;

import com.xdev.onsignalNotifcations.dto.NotificationRequest;

public interface OneSignalServiceImpl {
    String sendNotification(NotificationRequest notificationRequest) ;
}
