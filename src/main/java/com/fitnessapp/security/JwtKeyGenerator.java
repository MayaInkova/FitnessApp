package com.fitnessapp.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        // Генерира нов ключ за алгоритъма HS512.
        // Keys.secretKeyFor(SignatureAlgorithm.HS512) създава ключ, който е гарантирано
        // достатъчно сигурен за HS512 алгоритъма (т.е. >= 512 бита).
        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();

        // Base64 кодира байтовете на ключа за използване в application.properties
        String base64Key = Encoders.BASE64.encode(keyBytes);

        System.out.println("Генериран JWT HS512 Таен Ключ (Base64 Кодиран):");
        System.out.println(base64Key);

    }
}


