package org.dti.se.finalproject1backend1.outers.exceptions.products;

public class CategoryNotFoundException extends RuntimeException {
  public CategoryNotFoundException() {
    super("Category not found");
  }
}
