package com.example.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.example.controllers.ProductController;
import com.example.entities.Product;

/**
 * ProductModelAssembler es el componente de Spring que centraliza la construccion
 * de los enlaces HATEOAS (hipermedia) de los productos, eliminando el codigo
 * repetitivo de construir los enlaces en cada endpoint del controlador.
 * 
 * Hereda de RepresentationModelAssemblerSupport, que a su vez implementa
 * RepresentationModelAssembler. El primer tipo generico es la entidad de origen
 * (Product) y el segundo el modelo de representacion de destino; como la propia
 * entidad Product hereda de RepresentationModel, el modelo de destino tambien es
 * Product.
 */
@Component
public class ProductModelAssembler extends RepresentationModelAssemblerSupport<Product, Product> {

	public ProductModelAssembler() {
		super(ProductController.class, Product.class);
	}

	/**
	 * Convierte una entidad Product en un RepresentationModel anadiendole sus
	 * enlaces hipermedia:
	 * 
	 * - self: apunta al propio recurso del producto (GET /products/{id})
	 * 
	 * - all-products: apunta a la coleccion completa de productos (GET /products)
	 * 
	 * - product-image: si el producto tiene imagen, apunta a su descarga
	 *   (GET /products/fileDownLoad/{fileCode})
	 */
	@Override
	public Product toModel(Product product) {

		Link selfLink = linkTo(methodOn(ProductController.class).findProductById(product.getId())).withSelfRel();
		Link allProductsLink = linkTo(methodOn(ProductController.class).dameProductos(null, null))
				.withRel("all-products");

		product.add(selfLink, allProductsLink);

		if (product.getProductImage() != null) {
			Link imageLink = linkTo(methodOn(ProductController.class).downloadFile(product.getProductImage()))
					.withRel("product-image");
			product.add(imageLink);
		}

		return product;
	}

	/**
	 * Convierte una coleccion de entidades Product en un CollectionModel,
	 * anadiendole el enlace self de la coleccion completa.
	 */
	@Override
	public CollectionModel<Product> toCollectionModel(Iterable<? extends Product> entities) {

		CollectionModel<Product> collectionModel = super.toCollectionModel(entities);

		collectionModel.add(linkTo(methodOn(ProductController.class).dameProductos(null, null)).withSelfRel());

		return collectionModel;
	}

	/**
	 * Sobrecarga del metodo anterior para el caso paginado: ademas del enlace self
	 * (con los parametros page y size), anade los enlaces de paginacion prev y next
	 * cuando proceden.
	 */
	public CollectionModel<Product> toCollectionModel(Iterable<? extends Product> entities, int page, int size,
			int totalPages) {

		CollectionModel<Product> collectionModel = super.toCollectionModel(entities);

		collectionModel.add(linkTo(methodOn(ProductController.class).dameProductos(page, size)).withSelfRel());

		if (page > 0) {
			collectionModel.add(
					linkTo(methodOn(ProductController.class).dameProductos(page - 1, size)).withRel("prev"));
		}

		if (page < totalPages - 1) {
			collectionModel.add(
					linkTo(methodOn(ProductController.class).dameProductos(page + 1, size)).withRel("next"));
		}

		return collectionModel;
	}

}