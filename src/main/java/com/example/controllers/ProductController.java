package com.example.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entities.Product;
import com.example.services.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * La anotacion @RestController es para que todos los metodos que van a ser
 * creados dentro de este
 * controlador y reciben peticiones a través del protocolo HTTP, mediante los
 * verbos correspondientes
 * (GET, POST, PUT, DELETE, PATCH, etc.) devuelvan o reciban datos en formato de
 * JSON (JavaScript Object Notation)
 */

@RestController

/**
 * Una API REST esta orientada al recurso, es decir, que el controlador necesita
 * que se le especifique
 * que recurso va a responder, por ejemplo en esto seria /products, y en
 * dependencia del verbo del protocolo
 * HTTP se estaria haciendo una peticion (request) contreta. Por ejemplo: Si el
 * verbo es GET, significa
 * que estamos solicitando todos los productos al recurso /products. Si el
 * verbo es POST significa que queremos
 * recibir un producto en formato JSON, en el cuerpo de la peticion (request) y
 * persistirlo (guardarlo)
 * en las tablas correspondientes
 */
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 
     * IMPORTANTE!!!
     * 
     * Una API REST tiene que devolver informacion respecto a como ha sido
     * solucionada la peticion (request),
     * por ejemplo: el codigo 200 significa estado OK de la peticion, el codido 201
     * significaria CREATED,
     * el codigo 500 significaria que el servidor no ha podido cumplimentar la
     * peticion, el codigo 401 NO ENCONTRADO,
     * el codigo 403 prohibido, etc. Todos estos codigos se pueden encontrar en el
     * sitio de W3Schools
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
     * seran requeridos, es decir,
     * que no son obligatorios que se suministren. Y en caso de NO ser suministrados
     * (page y size),
     * los productos se van a devolver ordenados.
     * 
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> dameProductos(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {

        List<Product> products = null;
        Map<String, Object> responseAsMap = new HashMap<>();
        Sort sort = Sort.by("name");

        // Comprobar si en la peticion (request) me han suministrado los parametros page
        // y size
        if (page != null && size != null) {

            Pageable pageable = PageRequest.of(page, size, sort);

            // Implica devolver los productos paginados, es decir, una pagina de Product
            Page<Product> productPage = productService.findAll(pageable);
            products = productPage.getContent();
            responseAsMap.put("productos", products);

        } else {

            // Devolver los productos ordenados, por nombre (name), por ejemplo
            products = productService.findAll(sort);
            responseAsMap.put("productos", products);
        }

        return new ResponseEntity<>(responseAsMap, HttpStatus.OK);
    }

    /**
     * El metodo siguiente recupera un Producto por el id que se recibe como una
     * variable en la ruta,
     * mediante un end point (url o uri) que tiene el formato siguiente:
     * 
     * http://localhost:8080/productos/1
     * 
     * Donde el valor 1 al final del end point seria el id del producto
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findProductById(
            @PathVariable(name = "id", required = true) int product_id) {

        Map<String, Object> responseAsMap = new HashMap<>();
        ResponseEntity<Map<String, Object>> responseEntity = null;

        try {
            Product product = productService.findById(product_id);
            if (product != null) {
                String successMessage = "El producto con id " + product_id +
                        " ha sido encontrado";
                responseAsMap.put("mensaje todo OK: ", successMessage);
                responseAsMap.put("producto encontrado: ", product);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap,
                        HttpStatus.OK);
            } else {
                String failureMessage = "No ha sido encontrado ningun producto con id: "
                        + product_id;
                responseAsMap.put("Error: ", failureMessage);
                responseEntity = new ResponseEntity<>(responseAsMap, HttpStatus.NOT_FOUND);
            }
        } catch (DataAccessException e) {
            String errorMessage = "Error grave al buscar el producto con id "
                    + product_id + ", y la causa mas probable es: " +
                    e.getMostSpecificCause().getMessage();
            responseAsMap.put("Error grave: ", errorMessage);
            responseEntity = new ResponseEntity<>(responseAsMap,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }

    /**
     * Metodo que recibe por POST el Producto para ser persistido, guardado, y que
     * valida el JSON
     * recibido, para comprobar si esta bien formado o no
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveProduct(@Valid @RequestBody Product product,
            BindingResult result) {

        List<String> mensajesDeError = new ArrayList<>();
        Map<String, Object> responseAsMap = new HashMap<>();
        ResponseEntity<Map<String, Object>> responseEntity = null;

        // Primero, comprobar si hay errores en el producto recibido
        if (result.hasErrors()) {
            // Recuperamos los errores que tiene el producto recibido y se lo informamos al
            // que realizo la peticion (request) de persistir el producto
            List<ObjectError> objectErrors = result.getAllErrors();

            objectErrors.stream().forEach(objectError -> mensajesDeError.add(objectError.getDefaultMessage()));

            responseAsMap.put("El producto tiene los siguientes errores: ",
                    mensajesDeError);
            responseAsMap.put("Producto mal formado: ", product);

            responseEntity = new ResponseEntity<>(responseAsMap,
                    HttpStatus.BAD_REQUEST);

            return responseEntity;
        }

        // Persistimos el producto porque si hemos llegado a este punto es que esta bien
        // formado

        try {
            Product productoPersistido = productService.save(product);
            responseAsMap.put("mensaje: ", "Producto persistido exitosamente!!!");
            responseAsMap.put("producto Persistido: ", productoPersistido);
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.CREATED);
        } catch (DataAccessException e) {
            responseAsMap.put("Error Grave", "No ha podido ser guardado el producto y la causa mas probable es: " +
                    e.getMostSpecificCause().getMessage());
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }
}
