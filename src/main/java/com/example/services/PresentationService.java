package com.example.services;

import java.util.List;

import com.example.entities.Presentation;

public interface PresentationService {

    List<Presentation> findAll();
    void save(Presentation presentation);
    Presentation findById(int id);
}
