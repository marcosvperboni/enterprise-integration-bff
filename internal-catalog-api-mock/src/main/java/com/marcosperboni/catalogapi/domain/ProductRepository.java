package com.marcosperboni.catalogapi.domain;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<Product> findAll();

    Optional<Product> findById(String id);

    Product save(Product product);

    boolean deleteById(String id);
}
