package com.ceteva.mosaic;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProductProvider;

public class ProductProvider implements IProductProvider {

	public String getName() {
      return "com.ceteva.mosaic.ProductProvider";
	}

	public IProduct[] getProducts() {
	  IProduct[] products = new IProduct[1];
	  products[0] = new Product();
      return products;
	}
}
