package com.ead.payments.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authorization.method.AuthorizationAdvisorProxyFactory;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(requests -> {
                    requests.requestMatchers("/").permitAll();
                    requests.requestMatchers("/actuator/**").permitAll();
                    requests.anyRequest().authenticated();
                })
                .csrf(CsrfConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .logout(LogoutConfigurer::permitAll)
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    @Profile({"local", "integration-test"})
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("customer")
                .password("{noop}password")
                .roles("CUSTOMER")
                .build();
        UserDetails merchant = User.withUsername("merchant")
                .password("{noop}password")
                .roles("MERCHANT")
                .build();
        UserDetails admin = User.withUsername("engineer")
                .password("{noop}password")
                .roles("ADMIN")
                .build();
        UserDetails grafana = User.withUsername("grafana")
                .password("{noop}password")
                .roles("ADMIN")
                .build();
                
        return new InMemoryUserDetailsManager(user, merchant, grafana, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    static Customizer<AuthorizationAdvisorProxyFactory> skipValueTypes() {
        return (factory) -> factory.setTargetVisitor(AuthorizationAdvisorProxyFactory.TargetVisitor.defaultsSkipValueTypes());
    }
}
