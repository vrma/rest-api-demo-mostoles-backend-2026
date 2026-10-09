package com.example.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import com.example.dto.ProductDto;
import com.example.entities.Product;

/**
 * ProductMapper es la interfaz de mapeo de MapStruct que se encarga de convertir
 * entre la entidad JPA Product (capa de persistencia) y el Record ProductDto
 * (capa de presentacion).
 * 
 * La presentacion se mapea solamente por su id:
 * 
 * - ProductDto.presentationId -> Product.presentation.id (al crear/actualizar)
 * 
 * - Product.presentation.id -> ProductDto.presentationId (al devolver)
 * 
 * Con componentModel = "spring", MapStruct genera una implementacion que queda
 * registrada como un bean de Spring y puede ser inyectada en el controlador.
 */
@Mapper(componentModel = ComponentModel.SPRING)
public interface ProductMapper {

	@Mapping(target = "presentation.id", source = "presentationId")
	Product toEntity(ProductDto productDto);

	@Mapping(target = "presentationId", source = "presentation.id")
	ProductDto toDto(Product product);

}