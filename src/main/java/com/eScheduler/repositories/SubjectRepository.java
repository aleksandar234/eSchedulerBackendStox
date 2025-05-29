package com.eScheduler.repositories;

import com.eScheduler.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    @Query("SELECT s FROM Subject s WHERE s.name = ?1")
    Optional<Subject> findByName(String name);

    Optional<Subject> findByNameAndStudyProgramAndSemester(String name, String studyProgram, Integer semester);
}
