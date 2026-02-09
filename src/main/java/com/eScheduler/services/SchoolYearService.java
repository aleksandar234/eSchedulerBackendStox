package com.eScheduler.services;


import com.eScheduler.exceptions.custom.NotFoundException;
import com.eScheduler.model.SchoolYear;
import com.eScheduler.repositories.DistributionRepository;
import com.eScheduler.repositories.SchoolYearRepository;
import com.eScheduler.repositories.SubjectRepository;
import com.eScheduler.repositories.TeacherRepository;
import com.eScheduler.responses.customDTOClasses.SchoolYearDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SchoolYearService {

    private final SchoolYearRepository schoolYearRepository;
    private final SubjectRepository subjectRepository;
    private final DistributionRepository distributionRepository;
    private final TeacherRepository teacherRepository;

    private static final int MAX_YEARS = 5;

    public SchoolYearService(SchoolYearRepository schoolYearRepository, SubjectRepository subjectRepository, DistributionRepository distributionRepository, TeacherRepository teacherRepository) {
        this.schoolYearRepository = schoolYearRepository;
        this.subjectRepository = subjectRepository;
        this.distributionRepository = distributionRepository;
        this.teacherRepository = teacherRepository;
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

//    @Transactional
//    public void createSchoolYear() {
//
//        long total = schoolYearRepository.count();
//        if (total > MAX_YEARS) {
//            removeOldestYear();
//        }
//
//    }

    @Transactional
    public void removeSelectedYear(Long id) {
        // Pronađi školsku godinu sa najmanjim ID-jem (najstarija)
        Optional<SchoolYear> optOldest = schoolYearRepository.findById(id);

        if (optOldest.isEmpty()) return; // ako nema godina, ništa se ne radi

        SchoolYear oldest = optOldest.get();
        Long oldestId = oldest.getId();

        // Briši sve zavisne entitete
        distributionRepository.deleteBySchoolYearId(oldestId);
        subjectRepository.deleteBySchoolYearId(oldestId);
        teacherRepository.deleteBySchoolYearId(oldestId);

        // Na kraju briši samu školsku godinu
        schoolYearRepository.deleteById(oldestId);
    }

    @Transactional
    public SchoolYear activateYearAndDeactivateOthers(Long yearId) {
        // 1. Deaktiviraj sve ostale godine
        schoolYearRepository.deactivateAll();

        // 2. Aktiviraj selektovanu godinu
        SchoolYear year = schoolYearRepository.findById(yearId)
                .orElseThrow(() -> new RuntimeException("Školska godina nije pronađena"));

        year.setActive(true);
        return schoolYearRepository.save(year);
    }


}
