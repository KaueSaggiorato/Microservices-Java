package br.edu.atitus.productservice.controllers;

import br.edu.atitus.productservice.clients.CurrencyClient;
import br.edu.atitus.productservice.clients.CurrencyResponse;
import br.edu.atitus.productservice.dtos.ProductDTO;
import br.edu.atitus.productservice.entities.ProductEntity;
import br.edu.atitus.productservice.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
@RequestMapping("products")
public class ProductController {

    private final ProductRepository productRepository;
    private final CurrencyClient currencyClient;
    private final CacheManager cacheManager;

    @Value("${server.port}")
    private String serverPort;

    public ProductController(ProductRepository productRepository, CurrencyClient currencyClient, CacheManager cacheManager) {
        this.productRepository = productRepository;
        this.currencyClient = currencyClient;
        this.cacheManager = cacheManager;
    }

    @GetMapping("/{productId}")
    @CircuitBreaker(name = "CurrencyClientgetCurrencyStringString")
    public ProductDTO findProduct(
            @PathVariable Long productId,
            @RequestParam String targetCurrency
    ) throws Exception {
        Double convertedPrice = null;

        String environment = "Product-service running on port: " + serverPort;
        String requestCurrency = targetCurrency;
        ProductEntity entity = productRepository.findById(productId)
                .orElseThrow(() -> new Exception("Product not found"));

        String serviceInfo = "Product-service running on Port: " + serverPort;

        if (targetCurrency.equals(entity.getCurrency())) {
            convertedPrice = entity.getPrice();
        } else {
            String nameCache = "ConvertedValue";
            String keyCache = entity.getCurrency() + "-" + targetCurrency;
            Double convertedValue = cacheManager.getCache(nameCache).get(keyCache, Double.class);
            if (convertedValue == null){
                CurrencyResponse currency = currencyClient.getCurrency(entity.getCurrency(), targetCurrency);
                if (currency != null) {
                    convertedPrice = currency.conversionRate() * entity.getPrice();
                    environment = environment + " - " + currency.environment();
                    cacheManager.getCache(nameCache).put(keyCache, currency.conversionRate());
                } else {
                    convertedPrice = -1.0;
                    environment = environment + " - Currency Fallback";
                }
            } else {
                convertedPrice = convertedValue * entity.getPrice();
                environment = environment + " - Currency in cache";
            }
        }
        return new ProductDTO(
                entity.getId(),
                entity.getDescription(),
                entity.getBrand(),
                entity.getModel(),
                entity.getPrice(),
                entity.getCurrency(),
                entity.getStock(),
                serviceInfo,
                convertedPrice,
                targetCurrency
        );
    }
}