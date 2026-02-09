package com.eScheduler.repositories;

import com.eScheduler.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RepositoryRestResource(exported = false)
public interface DistributionRepository extends JpaRepository<Distribution, Long> {

    @Query("SELECT d FROM Distribution d WHERE d.subject = ?1 AND d.classType = ?2")
    List<Distribution> findBySubject(Subject subject, String classType);

    @Query("SELECT s FROM Subject s WHERE s.name = ?1 ")
    Subject findBySubjectName(String name);

    @Query("SELECT u FROM UserLogin u WHERE u.email = ?1 ")
    UserLogin findByUserEmail(String email);


    @Query("SELECT t FROM Teacher t WHERE t.userLogin.email = ?1 ")
    Teacher findByTeacherEmail(String email);

    @Query("SELECT d FROM Distribution d WHERE d.teacher.userLogin.email = ?1")
    List<Distribution> getDistributionByTeacherEmail(String email);

    @Query("SELECT s FROM Subject s WHERE s.name = ?1 AND s.studyProgram = ?2 AND s.semester = ?3")
    Subject findBySubjectNameStudyProgramSemester(String name, String studyProgram, String semester);

    @Query("SELECT d FROM Distribution d WHERE d.schoolYear.id = :schoolYearId")
    List<Distribution> findBySchoolYearId(@Param("schoolYearId") Long schoolYearId);

    boolean existsByTeacherIdAndSubjectIdAndSchoolYearAndClassType(Long teacherId, Long subjectId, SchoolYear schoolYear, String classType);


    @Modifying
    @Transactional
    @Query("delete from Distribution d where d.schoolYear.id = :yearId")
    void deleteBySchoolYearId(@Param("yearId") Long yearId);


}

