package com.nguyen.movieticket.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BookingReferenceGenerator {

    public String generateReference() {
        return "MT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
