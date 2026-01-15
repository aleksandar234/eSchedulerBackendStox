package com.eScheduler.services;


import com.eScheduler.exceptions.custom.NotFoundException;
import com.eScheduler.model.SchoolYear;
import com.eScheduler.repositories.SchoolYearRepository;
import com.eScheduler.responses.customDTOClasses.SchoolYearDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SchoolYearService {

    private final SchoolYearRepository schoolYearRepository;

    public SchoolYearService(SchoolYearRepository schoolYearRepository) {
        this.schoolYearRepository = schoolYearRepository;
    }

    public SchoolYear getActiveSchoolYears() {
        Optional<SchoolYear> activeSchoolYear = schoolYearRepository.findByActiveTrue();
        return activeSchoolYear.orElse(null);
    }

    public List<SchoolYear> findAllSchoolYears() {
        return schoolYearRepository.findAll();
    }

    public SchoolYearDTO getActiveSchoolYear() {
        // Dohvati entitet iz baze
        Optional<SchoolYear> activeSchoolYear = schoolYearRepository.findByActiveTrue();

        // Ako postoji aktivna godina, mapiraj je u DTO
        if (activeSchoolYear.isPresent()) {
            SchoolYear sy = activeSchoolYear.get();
            SchoolYearDTO dto = new SchoolYearDTO();
            dto.setId_skolska_godina(sy.getId());
            dto.setOznaka(sy.getLabel());
            dto.setDatum_pocetka(sy.getStartDate());
            dto.setDatum_zavrsetka(sy.getEndDate());
            dto.setAktivna(sy.isActive());
            return dto;
        } else {
            // Ako nema aktivne godine, vrati null
            return null;
        }
    }


    public SchoolYear getSchoolYearById(Long id) {
        return schoolYearRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Školska godina nije pronađena"));
    }

}
