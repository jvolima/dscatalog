package com.jvolima.dscatalog.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.jvolima.dscatalog.dto.ProductDTO;
import com.jvolima.dscatalog.entities.Category;
import com.jvolima.dscatalog.entities.Product;
import com.jvolima.dscatalog.repositories.CategoryRepository;
import com.jvolima.dscatalog.repositories.ProductRepository;
import com.jvolima.dscatalog.services.exceptions.DatabaseException;
import com.jvolima.dscatalog.services.exceptions.ResourceNotFoundException;
import com.jvolima.dscatalog.tests.Factory;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {
	@InjectMocks
	private ProductService service;
	
	@Mock
	private ProductRepository productRepository;
	
	@Mock
	private CategoryRepository categoryRepository;
	
	private long existingId;
	private long nonExistingId;
	private long dependentId;
	private long categoryId;
	private PageImpl<Product> page;
	private Product product;
	
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		categoryId = 4L;
		product = Factory.createProduct();
		page = new PageImpl<>(List.of(product));
		
		Mockito.when(productRepository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);
		
		Mockito.when(productRepository.findById(existingId)).thenReturn(Optional.of(product));
		Mockito.when(productRepository.findById(nonExistingId)).thenReturn(Optional.empty());
		
		Mockito.when(productRepository.getOne(existingId)).thenReturn(product);
		Mockito.when(productRepository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);
		
		Set<Category> categories = product.getCategories(); 
		Mockito.when(categoryRepository.getOne(categoryId)).thenReturn(categories.iterator().next());
		
		Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(product);
		
		Mockito.doNothing().when(productRepository).deleteById(existingId);	
		Mockito.doThrow(EmptyResultDataAccessException.class).when(productRepository).deleteById(nonExistingId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(productRepository).deleteById(dependentId);
	}
	
	@Test
	public void findAllPagedShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);
		
		Page<ProductDTO> result = service.findAllPaged(pageable);
		
		Assertions.assertNotNull(result);
		Mockito.verify(productRepository).findAll(pageable);
	}
	
	@Test
	public void findByIdShouldReturnProductDTOWhenIdExists() {
		ProductDTO dto = service.findById(existingId);
		
		Assertions.assertNotNull(dto);
		Mockito.verify(productRepository).findById(existingId);
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingId);
		});		
		Mockito.verify(productRepository).findById(nonExistingId);
	}
	
	@Test
	public void updateShouldReturnProductDTOWhenIdExists() {
		ProductDTO dto = service.update(existingId, Factory.createProductDTO());
		
		Assertions.assertNotNull(dto);
		Mockito.verify(productRepository).getOne(existingId);
		Mockito.verify(productRepository).save(product);
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExistingId, Factory.createProductDTO());
		});
		Mockito.verify(productRepository).getOne(nonExistingId);
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
		Mockito.verify(productRepository).deleteById(existingId);
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
		Mockito.verify(productRepository).deleteById(nonExistingId);
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
		Mockito.verify(productRepository).deleteById(dependentId);
	}
}
