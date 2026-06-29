package com.example.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entities.Presentation;

public interface PresentationDao extends JpaRepository<Presentation, Integer> {

}
