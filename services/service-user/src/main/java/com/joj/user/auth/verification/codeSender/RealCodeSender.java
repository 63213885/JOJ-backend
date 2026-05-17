package com.joj.user.auth.verification.codeSender;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;

import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;

import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.user.auth.model.IdentifierType;
import com.joj.user.auth.verification.config.AliyunSmsProperties;
import com.joj.user.auth.verification.model.VerificationScene;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/10 19:53
 */

@Slf4j
@Component
public class RealCodeSender implements CodeSender {

    private final AliyunSmsProperties properties;
    private final AsyncClient client;
//    @Resource
//    private VerificationCodeStore codeStore;
    private final JavaMailSender mailSender;

    public RealCodeSender(AliyunSmsProperties properties, JavaMailSender javaMailSender) throws Exception {
        this.properties = properties;
        this.mailSender = javaMailSender;

        // Configure Credentials authentication information
//        DefaultCredentialProvider provider = DefaultCredentialProvider.builder().build();
        StaticCredentialProvider provider = StaticCredentialProvider.create(
                Credential.builder()
                        .accessKeyId(properties.getAccessKeyId())
                        .accessKeySecret(properties.getAccessKeySecret())
                        .build()
        );

        client = AsyncClient.builder()
                .region("cn-hangzhou") // Region ID
                //.httpClient(httpClient) // Use the configured HttpClient, otherwise use the default HttpClient (Apache HttpClient)
                .credentialsProvider(provider)
                //.serviceConfiguration(Configuration.create()) // Service-level configuration
                // Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                // Endpoint 请参考 https://api.aliyun.com/product/Dypnsapi
                                .setEndpointOverride(properties.getEndpoint())
                        //.setConnectTimeout(Duration.ofSeconds(30))
                )
                .build();
    }

    private void sendPhoneCode(String scene, String phoneNumber, String code, int expireMinutes) {
        log.info("准备发送短信验证码：scene={} phoneNumber={} code={} expireMinutes={}", scene, phoneNumber, code, expireMinutes);
        try {
            // Parameter settings for API request
            SendSmsVerifyCodeRequest sendSmsVerifyCodeRequest = SendSmsVerifyCodeRequest.builder()
                    .phoneNumber(phoneNumber)
                    .signName(properties.getSignName())
                    .templateCode(properties.getTemplateCode())
                    .templateParam(String.format("{\"code\":\"%s\",\"min\":\"%s\"}", code, expireMinutes))
                    // Request-level configuration rewrite, can set Http request parameters, etc.
                    // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                    .build();

//            // Asynchronously get the return value of the API request
//            CompletableFuture<SendSmsVerifyCodeResponse> response = client.sendSmsVerifyCode(sendSmsVerifyCodeRequest);
//            // Synchronously get the return value of the API request
//            SendSmsVerifyCodeResponse resp = response.get();
            client.sendSmsVerifyCode(sendSmsVerifyCodeRequest)
                    .thenAccept(resp -> {
                        String acceptCode = resp.getBody().getCode();
                        String message = resp.getBody().getMessage();

                        if ("OK".equals(acceptCode)) {
                            // 成功才写 Redis
                            // todo: code写进redis应该搬进来
                            log.info("异步短信发送成功，没有同步写进Redis，可能有时差");
                        } else {
                            log.error("异步短信发送失败，acceptCode={} message={}", acceptCode, message);
                        }
                    }).exceptionally(e -> {
                        log.error("短信发送异常", e);
                        return null;
                    });

        } catch (Exception e) {
            log.error("发送短信验证码失败, phone={}", phoneNumber, e);
            throw new RuntimeException("发送短信验证码失败", e);
        }
    }

    @Value("${spring.mail.username}")
    private String adminEmail;

    private void sendEmailCode(String scene, String userEmail, String code, int expireMinutes) {
        log.info("准备发送邮箱验证码：scene={} userEmail={} code={} expireMinutes={}", scene, userEmail, code, expireMinutes);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(adminEmail);
            message.setTo(userEmail);
            if (scene.equals(VerificationScene.REGISTER.name())) {
                scene = "注册";
            } else if (scene.equals(VerificationScene.LOGIN.name())) {
                scene = "登录";
            } else if (scene.equals(VerificationScene.RESET_PASSWORD.name())) {
                scene = "重置密码";
            }
            message.setSubject(String.format("JOJ%s验证码", scene));
            message.setText(String.format("【JOJ】您正在进行%s操作，您的验证码是：%s，%s分钟内有效。", scene, code, expireMinutes));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("发送邮箱验证码失败, email={}", userEmail, e);
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "发送邮箱验证码失败");
        }

    }

    @Override
    public void sendCode(VerificationScene scene, IdentifierType identifierType, String identifier, String code, int expireMinutes) {
        if (identifierType == IdentifierType.PHONE) {
            sendPhoneCode(scene.name(), identifier, code, expireMinutes);
        } else if (identifierType == IdentifierType.EMAIL) {
            sendEmailCode(scene.name(), identifier, code, expireMinutes);
        } else {
            log.warn("未知的标识类型，无法发送验证码，标识值={}", identifier);
            return;
        }
    }
}
