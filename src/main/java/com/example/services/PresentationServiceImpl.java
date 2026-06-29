package com.example.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.dao.PresentationDao;
import com.example.entities.Presentation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PresentationServiceImpl implements PresentationService {

    private final PresentationDao presentationDao;

    @Override
    public List<Presentation> findAll() {
        return presentationDao.findAll();
    }

    @Override
    public void save(Presentation presentation) {
        presentationDao.save(presentation);
    }

    @Override
    public Presentation findById(int id) {
        return presentationDao.findById(id)
            .orElseThrow(() -> new RuntimeException("No ha sido encontrada la presentacion para el id suministrado"));
    }

}
