package com.example.entities;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "presentations")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
public class Presentation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @NotNull(message = "La presentacion tiene que tener un nombre")
    @NotEmpty(message = "El nombre de la presentacion no puede estar vacio")
    @Size(min = 4, max = 25, message = "El nombre de la presentacion no puede tener menos de 4 caracteres ni mas de 25")
    private String name;

    @NotNull(message = "La presentacion tiene que tener una descripcion")
    @NotEmpty(message = "La descripcion de la presentacion no puede estar vacia")
    @Size(max = 30, message = "La descripcion de la presentacion no puede superar los 30 caracteres")
    private String description;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "presentation")
    @JsonIgnore
    private List<Product> products;
}
