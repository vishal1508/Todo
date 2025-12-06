package com.vishal.todo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.mail.autoconfigure.MailProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {
    @Value("${spring.mail.host}")
    private String emailHost;
    @Value("${spring.mail.port}")
    private int emailPort;
    @Value("${spring.mail.username}")
    private String emaiId;
    @Value("${spring.mail.password}")
    private String emailPassword;
    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String isSmtpAuth;
    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String isStartTlsEnable;
    @Value("${spring.mail.properties.mail.transport.protocol}")
    private String transportProtocol;

    private MailProperties mailProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
        javaMailSenderImpl.setHost(emailHost);
        javaMailSenderImpl.setPort(emailPort);
        javaMailSenderImpl.setUsername(emaiId);
        javaMailSenderImpl.setPassword(emailPassword);
        Properties props = javaMailSenderImpl.getJavaMailProperties();
        props.put("mail.smtp.auth", isSmtpAuth);
        props.put("mail.smtp.starttls.enable", isStartTlsEnable);
        props.put("mail.transport.protocol", transportProtocol);

        return javaMailSenderImpl;
    }
}
