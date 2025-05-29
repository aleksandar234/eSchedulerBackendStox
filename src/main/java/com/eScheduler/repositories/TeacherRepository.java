package com.eScheduler.repositories;

import com.eScheduler.model.Teacher;
import com.eScheduler.model.UserLogin;
import com.eScheduler.responses.customDTOClasses.TeacherDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    @Query("SELECT t FROM Teacher t WHERE t.firstName = ?1")
    Optional<Teacher> findByName(String name);

    @Query("SELECT u FROM UserLogin u WHERE u.email = ?1")
    Optional<UserLogin> findByEmail(String email);

    @Query("SELECT t FROM Teacher t WHERE t.title = ?1")
    List<Teacher> findByType(String type);

    Optional<Teacher> findByUserLoginEmail(String email);

}
