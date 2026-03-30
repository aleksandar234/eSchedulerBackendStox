package com.eScheduler.repositories;

import com.eScheduler.model.Subject;
import com.eScheduler.responses.customDTOClasses.SubjectDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    @Query("SELECT s FROM Subject s WHERE s.name = ?1")
    Optional<Subject> findByName(String name);

    Optional<Subject> findByNameAndStudyProgramAndSemester(String name, String studyProgram, Integer semester);

    @Query("SELECT s FROM Subject s WHERE s.name = :name AND s.schoolYear.id = :schoolYearId")
    Optional<Subject> findByNameAndYear(@Param("name") String name, @Param("schoolYearId") Long schoolYearId);

    @Query("SELECT s FROM Subject s WHERE s.schoolYear.id = :schoolYearId")
    List<Subject> findBySchoolYearId(@Param("schoolYearId") Long schoolYearId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Subject s " +
            "WHERE s.name = :name " +
            "AND s.studyProgram = :studyProgram " +
            "AND s.schoolYear.id = :schoolYearId")
    boolean existsByNameAndStudyProgramAndSchoolYearId(
            @Param("name") String name,
            @Param("studyProgram") String studyProgram,
            @Param("schoolYearId") Long schoolYearId);

    Optional<Subject> findByNameAndStudyProgramAndSchoolYearId(String name, String studyProgram, Long schoolYearId);

    @Query("SELECT s FROM Subject s " +
            "WHERE s.name = :name " +
            "AND s.studyProgram = :studyProgram " +
            "AND s.semester = :semester " +
            "AND s.schoolYear.id = :schoolYearId")
    Optional<Subject> findByNameAndStudyProgramAndSemesterAndSchoolYearId(
            String name,
            String studyProgram,
            Integer semester,
            Long schoolYearId
    );

    @Modifying
    @Transactional
    @Query("delete from Subject s where s.schoolYear.id = :yearId")
    void deleteBySchoolYearId(@Param("yearId") Long yearId);

    @Query("SELECT s FROM Subject s WHERE s.name = ?1 AND s.studyProgram = ?2 AND s.semester = ?3")
    Subject findBySubjectNameStudyProgramSemester(String name, String studyProgram, Integer semester);


}
