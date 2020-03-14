package com.leyou.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * author:lu
 * create time: 2020/2/26.
 */
@Data
@ConfigurationProperties(prefix = "ly.sms")
public class SmsProperties {
    String accessKeyId;
    String AccessKeySecret;
    String signName;
    String verifyCodeTemplate;
}
