package com.jvolima.dscatalog.tests;

import java.time.Instant;

import com.jvolima.dscatalog.dto.ProductDTO;
import com.jvolima.dscatalog.entities.Category;
import com.jvolima.dscatalog.entities.Product;

public class Factory {
	public static Product createProduct() {
		Product product = new Product(1L, "Headset", "Beautiful Headset", 400.0, "https://razer.com/headset.png", Instant.parse("2023-08-15T08:15:20Z"));
		product.getCategories().add(new Category(2L, "Electronics"));
		
		return product;
	}
	
	public static ProductDTO createProductDTO() {
		Product product = createProduct();
		
		return new ProductDTO(product, product.getCategories());
	}
}
