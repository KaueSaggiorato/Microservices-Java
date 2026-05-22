package br.edu.atitus.productservice.clients;

import org.springframework.stereotype.Component;

@Component
public class CurrencyClientFallback implements CurrencyClient {

    @Override
    public CurrencyResponse getCurrency(String source, String target) {
        return new CurrencyResponse("ERROR", "ERROR", 0.0, "Fallback");
    }
}