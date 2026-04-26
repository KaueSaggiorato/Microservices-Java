package br.edu.atitus.productservice.controllers;

import br.edu.atitus.productservice.dtos.ProductDTO;
import br.edu.atitus.productservice.entities.ProductEntity;
import br.edu.atitus.productservice.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("products")
public class ProductController {

    private final ProductRepository productRepository;

    @Value("${server.port}")
    private String serverPort;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/{productId}")
    public ProductDTO findProduct(
            @PathVariable Long productId,
            @RequestParam String targetCurrency
    ) throws Exception {

        ProductEntity entity = productRepository.findById(productId)
                .orElseThrow(() -> new Exception("Product not found"));

        String serviceInfo = "Product-service running on Port: " + serverPort;

        return new ProductDTO(
                entity.getId(),
                entity.getDescription(),
                entity.getBrand(),
                entity.getModel(),
                entity.getPrice(),
                entity.getCurrency(),
                entity.getStock(),
                serviceInfo,
                null,
                targetCurrency
        );
    }
}