/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jhw.module.authorization_server.oauth2.config;

import com.nimbusds.jose.shaded.json.JSONArray;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 *
 * @author Jesus Hernandez Barrios (jhernandezb96@gmail.com)
 */
@Component
public class JwtConfig {

    //TIENE QUE SER LARGA
    //private static final String SECRET = SHA.hash512("secretsecretsecretsecretsecretsecretsecretsecretsecretsecretsecretsecret");
    static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);//llave random, como tiene que ser

    @Bean
    public JwtAccessTokenConverter converter() {
        JwtAccessTokenConverter conv = new JwtAccessTokenConverter();
        conv.setSigningKey(new String(SECRET_KEY.getEncoded()));
        return conv;
    }

    @Bean
    public TokenStore tokenStore(
            @Autowired JwtAccessTokenConverter converter) {
        TokenStore store = new JwtTokenStore(converter);
        return store;
    }

    @Bean
    public JwtDecoder decoder(@Autowired JwtAccessTokenConverter converter) {
        Map<String, String> keys = converter.getKey();
        String secret = keys.get("value");
        String alg = keys.get("alg");

        SecretKey key = new SecretKeySpec(secret.getBytes(), alg);

        return NimbusJwtDecoder.withSecretKey(key).build();//NimbusJwtDecoder.withPublicKey(RSAPublicKey)
    }

    @Bean
    public JwtAuthenticationConverter authConverter() {
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(jwt -> {
            JSONArray arr = (JSONArray) jwt.getClaims().get("authorities");
            return arr.stream()
                    .map(String::valueOf)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });
        return conv;
    }
}
