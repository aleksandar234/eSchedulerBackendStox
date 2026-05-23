package com.eScheduler.repositories;

import com.eScheduler.model.MasterClasses;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterClassesRepository extends JpaRepository<MasterClasses, Long> {

    // GET po nastavniku
    List<MasterClasses> findByNastavnikIdOrderByDatumUnosaDesc(Long nastavnikId);

    List<MasterClasses> findBySkolskaGodinaId(Long skolskaGodinaId);

    List<MasterClasses> findByNastavnikIdAndStepenStudijaOrderByDatumUnosaDesc(
            Long nastavnikId,
            String stepenStudija
    );

}
