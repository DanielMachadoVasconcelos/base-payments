package com.ead.payments.orders.place;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("issuer.client")
public class IssuerClientProperties {

    private String host;
    private String port;

}
