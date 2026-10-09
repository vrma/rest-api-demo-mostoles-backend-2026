package com.example.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import com.example.dto.ProductDto;
import com.example.entities.Product;

/**
 * ProductMapper es la interfaz de mapeo de MapStruct que se encarga de convertir
 * el Record ProductDto (capa de presentacion, usado en las peticiones) a la
 * entidad JPA Product (capa de persistencia).
 * 
 * La presentacion se mapea solamente por su id:
 * 
 * - ProductDto.presentationId -> Product.presentation.id
 * 
 * Con componentModel = "spring", MapStruct genera una implementacion que queda
 * registrada como un bean de Spring y puede ser inyectada en el controlador.
 * 
 * Las respuestas no usan DTO: la entidad Product hereda de RepresentationModel,
 * por lo que es ella misma el modelo de representacion con sus enlaces HATEOAS
 * (ver ProductModelAssembler).
 */
@Mapper(componentModel = ComponentModel.SPRING)
public interface ProductMapper {

	@Mapping(target = "presentation.id", source = "presentationId")
	Product toEntity(ProductDto productDto);

}