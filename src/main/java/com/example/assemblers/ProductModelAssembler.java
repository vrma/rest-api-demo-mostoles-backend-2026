package com.example.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
 * 
 * Enlaces generados:
 * 
 * - Sobre cada producto (toModel): self y all-products siempre; update (PUT) y
 *   delete (DELETE) solamente si el usuario autenticado tiene el rol ADMIN; y
 *   product-image si el producto tiene imagen.
 * 
 * - Sobre la coleccion (toCollectionModel): self siempre y create (POST)
 *   solamente si el usuario autenticado es ADMIN.
 * 
 * - Sobre la coleccion paginada (toPagedModel): los mismos enlaces que la
 *   coleccion mas first, prev y next, y los metadatos de paginacion (PagedModel +
 *   PageMetadata).
 * 
 * Los enlaces de mutacion se anuncian condicionalmente segun los roles del
 * usuario autenticado (SecurityContext), de modo que la hipermedia refleja las
 * operaciones que realmente puede realizar el cliente.
 */
@Component
public class ProductModelAssembler extends RepresentationModelAssemblerSupport<Product, Product> {

	private static final String ALL_PRODUCTS = "all-products";
	private static final String PRODUCT_IMAGE = "product-image";
	private static final String CREATE = "create";
	private static final String UPDATE = "update";
	private static final String DELETE = "delete";
	private static final String ROLE_ADMIN = "ROLE_ADMIN";

	public ProductModelAssembler() {
		super(ProductController.class, Product.class);
	}

	/**
	 * Convierte una entidad Product en un RepresentationModel anadiendole sus
	 * enlaces hipermedia: self y all-products siempre; update y delete solo si el
	 * usuario autenticado es ADMIN; y product-image si tiene imagen.
	 */
	@Override
	public Product toModel(Product product) {

		product.add(
				linkTo(methodOn(ProductController.class).findProductById(product.getId())).withSelfRel(),
				linkTo(methodOn(ProductController.class).dameProductos(null, null)).withRel(ALL_PRODUCTS));

		// Los enlaces de mutacion solo se anuncian a los administradores
		if (esAdmin()) {
			product.add(updateLink(product), deleteLink(product));
		}

		if (product.getProductImage() != null) {
			product.add(linkTo(methodOn(ProductController.class).downloadFile(product.getProductImage()))
					.withRel(PRODUCT_IMAGE));
		}

		return product;
	}

	/**
	 * Convierte una coleccion de entidades Product en un CollectionModel no
	 * paginado, anadiendole el enlace self de la coleccion y el enlace create (POST)
	 * para dar de alta nuevos productos.
	 */
	@Override
	public CollectionModel<Product> toCollectionModel(Iterable<? extends Product> entities) {

		CollectionModel<Product> collectionModel = super.toCollectionModel(entities);

		collectionModel.add(linkTo(methodOn(ProductController.class).dameProductos(null, null)).withSelfRel());

		// El enlace create (POST) solo se anuncia a los administradores
		if (esAdmin()) {
			collectionModel.add(createLink());
		}

		return collectionModel;
	}

	/**
	 * Convierte una pagina de entidades Product en un PagedModel, que ademas de los
	 * productos con sus enlaces incorpora los metadatos de paginacion (PageMetadata:
	 * size, number, totalElements, totalPages) y los enlaces dinamicos de navegacion
	 * self, create, first, prev y next.
	 */
	public PagedModel<Product> toPagedModel(Page<Product> page) {

		List<Product> content = page.getContent().stream().map(this::toModel).toList();

		PagedModel<Product> pagedModel = PagedModel.of(
				content,
				new PageMetadata(page.getSize(), page.getNumber(), page.getTotalElements()),
				linkTo(methodOn(ProductController.class).dameProductos(page.getNumber(), page.getSize()))
						.withSelfRel());

		// El enlace create (POST) solo se anuncia a los administradores
		if (esAdmin()) {
			pagedModel.add(createLink());
		}

		if (page.getTotalPages() > 0) {

			pagedModel.add(linkTo(
					methodOn(ProductController.class).dameProductos(0, page.getSize())).withRel("first"));

			pagedModel.add(linkTo(
					methodOn(ProductController.class).dameProductos(page.getTotalPages() - 1, page.getSize()))
					.withRel("last"));
		}

		if (page.hasPrevious()) {
			pagedModel.add(linkTo(
					methodOn(ProductController.class).dameProductos(page.getNumber() - 1, page.getSize()))
					.withRel("prev"));
		}

		if (page.hasNext()) {
			pagedModel.add(linkTo(
					methodOn(ProductController.class).dameProductos(page.getNumber() + 1, page.getSize()))
					.withRel("next"));
		}

		return pagedModel;
	}

	/**
	 * Comprueba si el usuario autenticado en el contexto de seguridad tiene el rol
	 * ADMIN. Se utiliza para anunciar los enlaces de mutacion (create/update/delete)
	 * solamente cuando el cliente puede realmente ejecutar esas operaciones.
	 */
	private boolean esAdmin() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}

		return authentication.getAuthorities().stream()
				.anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
	}

	/**
	 * Enlace update (PUT /products/{id}) de un producto.
	 */
	private Link updateLink(Product product) {
		try {
			return linkTo(methodOn(ProductController.class).updateProduct(null, null, null, product.getId()))
					.withRel(UPDATE);
		} catch (IOException e) {
			throw new IllegalStateException(
					"No se pudo construir el enlace 'update' del producto con id " + product.getId(), e);
		}
	}

	/**
	 * Enlace delete (DELETE /products/{id}) de un producto.
	 */
	private Link deleteLink(Product product) {
		return linkTo(methodOn(ProductController.class).deleteProducto(product.getId())).withRel(DELETE);
	}

	/**
	 * Enlace create (POST /products) de la coleccion de productos.
	 */
	private Link createLink() {
		try {
			return linkTo(methodOn(ProductController.class).saveProduct(null, null, null)).withRel(CREATE);
		} catch (IOException e) {
			throw new IllegalStateException("No se pudo construir el enlace 'create' de productos", e);
		}
	}

}
