package com.moises.turism.controller;

import com.moises.turism.dto.catalogo.CatalogoResponse;
import com.moises.turism.repository.CategoriaExperienciaRepository;
import com.moises.turism.repository.InteresRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/catalogos")
public class CatalogoController {

    private final CategoriaExperienciaRepository categoriaExperienciaRepository;
    private final InteresRepository interesRepository;

    @GetMapping("/categorias")
    public ResponseEntity<List<CatalogoResponse>> listarCategorias() {
        return ResponseEntity.ok(categoriaExperienciaRepository.findAllByOrderByNombreCategoriaAsc()
                .stream()
                .map(categoria -> new CatalogoResponse(categoria.getIdCategoria(), categoria.getNombreCategoria()))
                .toList());
    }

    @GetMapping("/intereses")
    public ResponseEntity<List<CatalogoResponse>> listarIntereses() {
        return ResponseEntity.ok(interesRepository.findAllByOrderByNombreInteresAsc()
                .stream()
                .map(interes -> new CatalogoResponse(interes.getIdInteres(), interes.getNombreInteres()))
                .toList());
    }
}
