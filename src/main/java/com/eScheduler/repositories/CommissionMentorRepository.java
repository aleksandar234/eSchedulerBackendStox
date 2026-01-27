package com.eScheduler.repositories;


import com.eScheduler.model.CommissionMentor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommissionMentorRepository extends JpaRepository<CommissionMentor, Long> {

    List<CommissionMentor> findByNastavnikIdOrderByDatumUnosaDesc(Long nastavnikId);

    List<CommissionMentor> findBySkolskaGodinaId(Long skolskaGodinaId);

}
