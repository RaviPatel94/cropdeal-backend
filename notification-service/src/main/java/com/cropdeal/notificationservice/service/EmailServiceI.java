package com.cropdeal.notificationservice.service;

public interface EmailServiceI {
	void sendEmail(String to, String subject, String body);
}
