package com.eScheduler.repositories;


import com.eScheduler.model.SchoolYear;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface SchoolYearRepository extends JpaRepository<SchoolYear, Long> {

    @Override
    Optional<SchoolYear> findById(Long aLong);


    Optional<SchoolYear> findByActiveTrue();


    List<SchoolYear> findAll();

    Optional<SchoolYear> getSchoolYearById(Long year);

    @Modifying
    @Transactional
    @Query("UPDATE SchoolYear s SET s.active = false WHERE s.active = true")
    void deactivatePreviousYear();
}
