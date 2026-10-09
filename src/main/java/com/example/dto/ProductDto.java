package com.example.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * ProductDto es el Record de presentacion que se intercambia con el cliente
 * (frontend) a traves de los endpoints, manteniendo así separada la capa de
 * presentacion de la capa de persistencia (la entidad JPA Product).
 * 
 * La presentacion se relaciona mediante su id (presentationId) y no exponiendo
 * el objeto Presentation, lo que acopla menos las dos capas.
 */
public record ProductDto(

        @NotNull(message = "El producto tiene que tener un nombre")
        @NotEmpty(message = "El nombre del producto no puede estar vacio")
        @Size(min = 4, max = 25, message = "El nombre del producto no puede tener menos de 4 caracteres ni mas de 25")
        String name,

        @NotNull(message = "La description del producto es requerida")
        @NotBlank(message = "La description del producto no puede tener espacios vacios solamente")
        @Size(max = 45, message = "La description no puede superar los 45 caracteres")
        String description,

        @Min(value = 0, message = "El stock del producto no puede ser negativo")
        int stock,

        @Min(value = 0, message = "El precio no puede estar en valores negativos")
        BigDecimal price,

        @NotNull(message = "La presentacion del producto es requerida")
        Integer presentationId,

        String productImage) {

}