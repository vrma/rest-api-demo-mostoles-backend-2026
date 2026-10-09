package com.example.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.assemblers.ProductModelAssembler;
import com.example.dto.ProductDto;
import com.example.entities.Product;
import com.example.mappers.ProductMapper;
import com.example.services.ProductService;
import com.example.utilities.FileDownloadUtil;
import com.example.utilities.FileUploadUtil;
import com.example.utilities.FileUtil;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * La anotacion @RestController es para que todos los metodos que van a ser
 * creados dentro de este controlador y reciben peticiones a través del
 * protocolo HTTP, mediante los verbos correspondientes (GET, POST, PUT, DELETE,
 * PATCH, etc.) devuelvan o reciban datos en formato de JSON (JavaScript Object
 * Notation)
 */

@RestController

/**
 * Una API REST esta orientada al recurso, es decir, que el controlador necesita
 * que se le especifique que recurso va a responder, por ejemplo en esto seria
 * /products, y en dependencia del verbo del protocolo HTTP se estaria haciendo
 * una peticion (request) contreta. Por ejemplo: Si el verbo es GET, significa
 * que estamos solicitando todos los productos al recurso /products. Si el verbo
 * es POST significa que queremos recibir un producto en formato JSON, en el
 * cuerpo de la peticion (request) y persistirlo (guardarlo) en las tablas
 * correspondientes
 */
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;
	private final FileUploadUtil fileUploadUtil;
	private final FileDownloadUtil fileDownloadUtil;
	private final FileUtil fileUtil;
	private final ProductMapper productMapper;
	private final ProductModelAssembler productModelAssembler;

	/**
	 * 
	 * IMPORTANTE!!!
	 * 
	 * Una API REST tiene que devolver informacion respecto a como ha sido
	 * solucionada la peticion (request), por ejemplo: el codigo 200 significa
	 * estado OK de la peticion, el codido 201 significaria CREATED, el codigo 500
	 * significaria que el servidor no ha podido cumplimentar la peticion, el codigo
	 * 401 NO ENCONTRADO, el codigo 403 prohibido, etc. Todos estos codigos se
	 * pueden encontrar en el sitio de W3Schools
	 * 
	 * https://www.w3schools.com/tags/ref_httpmessages.asp
	 * 
	 */

	/**
	 * El metodo siguiente va a responder a una peticion (request) del tipo:
	 * 
	 * http://localhost:8080/products?page=0&size=3
	 * 
	 * Donde los parametros page y size seran utilizados para la paginacion, y no
	 * seran requeridos, es decir, que no son obligatorios que se suministren. Y en
	 * caso de NO ser suministrados (page y size), los productos se van a devolver
	 * ordenados.
	 * 
	 */
	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<CollectionModel<Product>> dameProductos(
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "size", required = false) Integer size) {

		List<Product> products;
		Sort sort = Sort.by("name");
		CollectionModel<Product> collectionModel;

		// Comprobar si en la peticion (request) me han suministrado los parametros page
		// y size
		if (page != null && size != null) {

			Pageable pageable = PageRequest.of(page, size, sort);

			// Implica devolver los productos paginados, es decir, una pagina de Product
			Page<Product> productPage = productService.findAll(pageable);
			products = productPage.getContent();

			// El ensamblador construye los enlaces de cada producto y los de paginacion
			// (self, prev y next)
			collectionModel = productModelAssembler.toCollectionModel(products, page, size,
					productPage.getTotalPages());

		} else {

			// Devolver los productos ordenados, por nombre (name), por ejemplo
			products = productService.findAll(sort);

			// El ensamblador construye los enlaces de cada producto y el self de la
			// coleccion
			collectionModel = productModelAssembler.toCollectionModel(products);
		}

		return new ResponseEntity<>(collectionModel, HttpStatus.OK);
	}

	/**
	 * El metodo siguiente recupera un Producto por el id que se recibe como una
	 * variable en la ruta, mediante un end point (url o uri) que tiene el formato
	 * siguiente:
	 * 
	 * http://localhost:8080/productos/1
	 * 
	 * Donde el valor 1 al final del end point seria el id del producto
	 */
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<RepresentationModel<?>> findProductById(
			@PathVariable(name = "id", required = true) int product_id) {

		Map<String, Object> responseAsMap = new HashMap<>();
		ResponseEntity<RepresentationModel<?>> responseEntity = null;

		try {
			Product product = productService.findById(product_id);
			if (product != null) {
				String successMessage = "El producto con id " + product_id + " ha sido encontrado";
				responseAsMap.put("mensaje todo OK: ", successMessage);

				/**
				 * Capa de presentacion: el ensamblador (ProductModelAssembler) se encarga de
				 * construir todos los enlaces HATEOAS del producto (self, all-products y, si
				 * procede, product-image). El controlador ya no repite la construccion de
				 * enlaces.
				 */
				responseEntity = new ResponseEntity<>(productModelAssembler.toModel(product), HttpStatus.OK);
			} else {
				String failureMessage = "No ha sido encontrado ningun producto con id: " + product_id;
				responseAsMap.put("Error: ", failureMessage);
				responseEntity = new ResponseEntity<>(EntityModel.of(responseAsMap), HttpStatus.NOT_FOUND);
			}
		} catch (DataAccessException e) {
			String errorMessage = "Error grave al buscar el producto con id " + product_id
					+ ", y la causa mas probable es: " + e.getMostSpecificCause().getMessage();
			responseAsMap.put("Error grave: ", errorMessage);
			responseEntity = new ResponseEntity<>(EntityModel.of(responseAsMap), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return responseEntity;
	}

	/**
	 * Metodo que recibe por POST el Producto para ser persistido, guardado, y que
	 * valida el JSON recibido, para comprobar si esta bien formado o no
	 * 
	 * Primero: Hay que cambiar lo que recibe el metodo saveProduct, porque ya el
	 * producto no viene ocupando todo el cuerpo de la peticion (request), sino una
	 * parte, y la otra parte la ocupa la imagen del producto
	 * 
	 * Y, muy importante, que no se nos olvide anotar este metodo y todos los que
	 * insertan, crean, eliminan registros en las tablas con la
	 * anotacion @Transactional, y tambien hay que especificar el tipo de archivo
	 * que va a consumir este metodo
	 * 
	 * @throws IOException
	 * 
	 */
	@PostMapping(consumes = "multipart/form-data")
	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<RepresentationModel<?>> saveProduct(@Valid @RequestPart(name = "product") ProductDto productDto,
			BindingResult result,
			@RequestPart(name = "file", required = false) MultipartFile imagenDelProducto) throws IOException {

		List<String> mensajesDeError = new ArrayList<>();
		Map<String, Object> responseAsMap = new HashMap<>();
		ResponseEntity<RepresentationModel<?>> responseEntity = null;

		// Primero, comprobar si hay errores en el producto recibido
		if (result.hasErrors()) {
			// Recuperamos los errores que tiene el producto recibido y se lo informamos al
			// que realizo la peticion (request) de persistir el producto
			List<ObjectError> objectErrors = result.getAllErrors();

			objectErrors.stream().forEach(objectError -> mensajesDeError.add(objectError.getDefaultMessage()));

			responseAsMap.put("El producto tiene los siguientes errores: ", mensajesDeError);
			responseAsMap.put("Producto mal formado: ", productDto);

			responseEntity = new ResponseEntity<>(EntityModel.of(responseAsMap), HttpStatus.BAD_REQUEST);

			return responseEntity;
		}

		/**
		 * Capa de persistencia: convertir el ProductDto recibido (capa de presentacion)
		 * en la entidad Product que entiende JPA/Hibernate, mediante MapStruct
		 */
		Product product = productMapper.toEntity(productDto);

		// Persistimos el producto porque si hemos llegado a este punto es que esta bien
		// formado
		// Pero antes vamos a comprobar si hemos recibido imagen del producto, para
		// guardarla
		// en el sistema de archivo (file system)

		if (imagenDelProducto != null && !imagenDelProducto.isEmpty()) {

			/**
			 * Para guardar la imagen del producto, en primer lugar le agregaremos como
			 * prefijo un codigo alfanumerico (de letras y numeros), generado aleatoriamente
			 * a partir de un metodo que se encuentre en la biblioteca Apache Commonds Text,
			 * que hay que descargar la dependencia desde el repositorio central de maven y
			 * agregarla al pom.xml
			 */

			/**
			 * Vamos a crear un Componente en un paquete que podria ser
			 * com.example.utilities, y este componente va a tener un metodo para guardar la
			 * imagen recibida en una carpeta del file system y devolver un codigo
			 * alfanumerico, generado aleatoriamente, que llevara como prefijo el nombre del
			 * fichero de imagen recibido.
			 * 
			 * Se hara uso intensivo de NIO.2 y se comprobara si la carpeta existe o no,
			 * para crearla
			 */

			String fileCode = fileUploadUtil.saveFile(imagenDelProducto.getOriginalFilename(), imagenDelProducto);

			product.setProductImage(fileCode + '-' + imagenDelProducto.getOriginalFilename());
		}

		try {
			Product productoPersistido = productService.save(product);

			/**
			 * Capa de presentacion: el ensamblador construye todos los enlaces HATEOAS del
			 * producto (self, all-products y, si tiene imagen, product-image)
			 */
			responseEntity = new ResponseEntity<>(productModelAssembler.toModel(productoPersistido),
					HttpStatus.CREATED);
		} catch (DataAccessException e) {
			responseAsMap.put("Error Grave", "No ha podido ser guardado el producto y la causa mas probable es: "
					+ e.getMostSpecificCause().getMessage());
			responseEntity = new ResponseEntity<>(EntityModel.of(responseAsMap), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return responseEntity;
	}

	/**
	 * Metodo que recupera la imagen de un producto, dado el codigo que tiene como
	 * prefijo el nombre de la imagen
	 */
	@GetMapping("/fileDownLoad/{fileCode}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> downloadFile(@PathVariable String fileCode) {

		Resource resource = null;

		try {
			resource = fileDownloadUtil.getFileAsResource(fileCode);
		} catch (IOException ioe) {
			return ResponseEntity.internalServerError().build();
		}

		if (resource == null)
			return new ResponseEntity<>("Imagen del producto no encontrada ", HttpStatus.NOT_FOUND);
		/**
		 * Si estamos en este punto quiere decir que el fichero (imagen del producto) ha
		 * sido encontrado y podemos enviarlo como respuesta a la peticion, como un
		 * fichero adjunto en el cuerpo de la respuesta
		 */

		String contentType = "application/octet-stream";
		String headerValue = "attachment; fileName=\"" + resource.getFilename() + "\"";

		/**
		 * HATEOAS: en este endpoint no tiene sentido envolver el binario de la imagen
		 * en un EntityModel (debe viajar como fichero adjunto en el cuerpo de la
		 * respuesta). No obstante, si se puede informar al cliente, mediante el header
		 * HTTP Link, del enlace hacia la coleccion de productos
		 */
		Link allProductsLink = linkTo(methodOn(ProductController.class).dameProductos(null, null))
				.withRel("all-products");

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
				.header(HttpHeaders.LINK, allProductsLink.toString()).body(resource);
	}

	/**
	 * Metodo que actualiza (update) un producto dado el id del mismo.
	 * 
	 * Es basicamente igual al metodo que persiste el producto. Respondera a una
	 * peticion del tipo siguiente, por ejemplo:
	 * 
	 * http://localhost:8080/productos/3
	 * 
	 * Y no habra ambiguedad con el metodo de buscar un producto por el id, porque
	 * el verbo utilizado del protocolo HTTP sera diferente, PUT en este caso.
	 * 
	 */

//	@PutMapping("/{id}")
//	@Transactional
//	public ResponseEntity<Map<String, Object>> updateProduct(@Valid @RequestBody Product product, BindingResult results,
//			@PathVariable Integer id) {
//
//		ResponseEntity<Map<String, Object>> responseEntity = null;
//		Map<String, Object> responseAsMap = new HashMap<>();
//
//		// Comprobar si el producto recibido en el cuerpo de la peticion tiene errores
//		if (results.hasErrors()) {
//
//			// Recuperar todos los errores que tiene el producto
//			List<ObjectError> objectErrors = results.getAllErrors();
//
//			// Hay que recorrer la lista de ObjectError para recuperar los mensajes de error
//			// por defecto que le voy a mostrar al cliente que ha hecho la peticion,
//			// es decir, que ha enviado el producto mal formado
//
//			// Los mensajes de error tienen que ser almacenados en una lista donde cada
//			// elemento de la lista
//			// sea un String
//
//			List<String> mensajesError = new ArrayList<>();
//
//			objectErrors.stream().forEach(objectError -> mensajesError.add(objectError.getDefaultMessage()));
//
//			responseAsMap.put("errores", mensajesError);
//			responseAsMap.put("product", product);
//
//			responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.BAD_REQUEST);
//
//			return responseEntity;
//
//		}
//
//		/**
//		 * Si no hay errores vamos a actualizar el producto recibido y devolver
//		 * informacion al respecto como se requiere para una API REST
//		 */
//
//		try {
//			product.setId(id);
//			Product productoModoficado = productService.save(product);
//			String mensaje = "El producto ha sido modificado exitosamente";
//			responseAsMap.put("mensaje", mensaje);
//			responseAsMap.put("product", productoModoficado);
//			responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, 	
//					HttpStatus.OK);
//		} catch (DataAccessException e) {
//			String errorMessage = "El producto no se pudo modificar y la causa mas probable es: "
//					+ e.getMostSpecificCause();
//			responseAsMap.put("error", errorMessage);
//			responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, 
//					HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//
//		return responseEntity;
//	}

	@PutMapping(value = "/{id}", consumes = "multipart/form-data")
	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<RepresentationModel<?>> updateProduct(@Valid @RequestPart(name = "product") ProductDto productDto,
			BindingResult result,
			@RequestPart(name = "file", required = false) MultipartFile imagenDelProducto,
			@PathVariable(name = "id", required = true) int product_id) throws IOException {

		List<String> mensajesDeError = new ArrayList<>();
		Map<String, Object> responseAsMap = new HashMap<>();
		ResponseEntity<RepresentationModel<?>> responseEntity = null;

		// comprobar errores de validación
		if (result.hasErrors()) {

			List<ObjectError> objectErrors = result.getAllErrors();

			objectErrors.stream().forEach(objectError -> {
				mensajesDeError.add(objectError.getDefaultMessage());
			});

			responseAsMap.put("respuesta de error: ", mensajesDeError);
			responseAsMap.put("producto mal formado: ", productDto);
			responseEntity = new ResponseEntity<>(EntityModel.of(responseAsMap), HttpStatus.BAD_REQUEST);

			return responseEntity;
		}
		/**
		 * Persisto (guardo) el producto porque está bien formado compruebo si hay
		 * imagen para guardarla y en tal caso debo eliminar la imagen del producto
		 */
		Product productoParaActualizar = productService.findById(product_id);

		if (productoParaActualizar == null) {

			responseAsMap.put("mensaje de error: ", "producto con id: " + product_id + " no encontrado.");
			return new ResponseEntity<>(EntityModel.of(responseAsMap), HttpStatus.NOT_FOUND);
		}

		/**
		 * Capa de persistencia: convertir el ProductDto recibido (capa de presentacion)
		 * en la entidad Product que entiende JPA/Hibernate, mediante MapStruct
		 */
		Product product = productMapper.toEntity(productDto);

		if (imagenDelProducto != null && !imagenDelProducto.isEmpty()) {

			/**
			 * comprobar si productoParaActualizar tiene imagen y si es así eliminarla
			 */
			if (productoParaActualizar.getProductImage() != null) {
				// Eliminar la imagen asociada
				fileUtil.eliminarArchivo(productoParaActualizar.getProductImage());
			}
			/**
			 * agregar prefijo: código alfanumérico aleatorio con método Apache Commons text
			 * (Lang3) (dependencia Maven -> pom.xml)
			 * 
			 * Beans vs Components: Ahora crearemos un componente en el paquete utilities.
			 * Dentro habrá un método para guardar la imagen en una carpeta y devuelve un
			 * código aleatorio que llevará como prefijo el nombre del fichero original
			 * 
			 * NIO.2 (entrada salida no bloqueante) si no existe la carpeta la creará.
			 */
			String fileCode = fileUploadUtil.saveFile(imagenDelProducto.getOriginalFilename(),
					imagenDelProducto);

			product.setProductImage(fileCode + '-' + imagenDelProducto.getOriginalFilename());
		}

		try {
			product.setId(product_id);
			Product productoAGuardar = productService.save(product);

			/**
			 * Capa de presentacion: el ensamblador construye todos los enlaces HATEOAS del
			 * producto (self, all-products y, si tiene imagen, product-image)
			 */
			responseEntity = new ResponseEntity<>(productModelAssembler.toModel(productoAGuardar), HttpStatus.OK);

		} catch (DataAccessException e) {

			String errorMessage = "Error grave al actualizado el producto y la causa más probable es "
					+ e.getMostSpecificCause().getMessage();
			// e.getStackTrace();
			responseAsMap.put("Error grave: ", errorMessage);

			responseEntity = new ResponseEntity<>(EntityModel.of(responseAsMap), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return responseEntity;

	}
	
    /**==============================================================================================
     * Metodo para eliminar un producto dado el id
     */
    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<?>> deleteProducto(@PathVariable Integer id) {

        ResponseEntity<EntityModel<?>> responseEntity = null;
        var responseAsMap = new HashMap<String, Object>();

        try {

            // recuperar el producto y comprobar si hay foto y eliminar el archivo
            Product productToDelete = productService.findById(id);

            if (productToDelete == null) {

            responseAsMap.put("mensaje de error: ", "producto con id: " + id + " no encontrado.");
            return new ResponseEntity<>(EntityModel.of(responseAsMap), HttpStatus.NOT_FOUND);
        }

            if (productToDelete.getProductImage() != null) {
                fileUtil.eliminarArchivo(productToDelete.getProductImage());
            }

            productService.delete(productService.findById(id));
            String successMessage = "El producto con id " + id + ", ha sido eliminado";
            responseAsMap.put("mensaje", successMessage);

            /**
             * HATEOAS: envolver la respuesta en un EntityModel con un enlace hacia la
             * coleccion de productos
             */
            Link allProductsLink = linkTo(methodOn(ProductController.class).dameProductos(null, null))
                    .withRel("all-products");
            responseEntity = new ResponseEntity<>(EntityModel.of(responseAsMap, allProductsLink), HttpStatus.OK);
        } catch (DataAccessException e) {
            String errorMessage = "No ha podido ser eliminado el producto cuyo id es: " + id
                    + ", siendo la causa mas probable: " + e.getMostSpecificCause().getMessage();
            responseAsMap.put("mensaje", errorMessage);
            responseEntity = new ResponseEntity<>(EntityModel.of(responseAsMap),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }

}
