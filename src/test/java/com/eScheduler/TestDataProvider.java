package com.eScheduler;

import com.eScheduler.model.Subject;
import com.eScheduler.model.Teacher;
import com.eScheduler.model.UserLogin;

public class TestDataProvider {
    public static Teacher createTeacher1() {
        UserLogin userLogin = new UserLogin();
        userLogin.setId(1L);
        userLogin.setEmail("mMarkovic@example.com");
        userLogin.setAdmin(false);
        userLogin.setPassword("password1");

        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("Marko");
        teacher.setLastName("Markovic");
        teacher.setTitle("nastavnik");
        teacher.setUserLogin(userLogin);
//        userLogin.setTeacher(teacher);

        return teacher;
    }

    public static Teacher createTeacher2() {
        UserLogin userLogin = new UserLogin();
        userLogin.setId(2L);
        userLogin.setEmail("aAnic@example.com");
        userLogin.setAdmin(false);
        userLogin.setPassword("password2");

        Teacher teacher = new Teacher();
        teacher.setId(2L);
        teacher.setFirstName("Ana");
        teacher.setLastName("Anic");
        teacher.setTitle("saradnik");
        teacher.setUserLogin(userLogin);
//        userLogin.setTeacher(teacher);

        return teacher;
    }

    public static Subject createSubject1() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("Programiranje");
        subject.setStudyProgram("RN");
        subject.setSemester(3);
        subject.setLectureHours(30);
        subject.setExerciseHours(15);
        subject.setPracticumHours(10);
        subject.setMandatory("obavezni");
        subject.setLectureSessions(15);
        subject.setExerciseSessions(15);

        return subject;
    }

    public static Subject createSubject2() {
        Subject subject = new Subject();
        subject.setId(2L);
        subject.setName("Matematika");
        subject.setStudyProgram("IS");
        subject.setSemester(1);
        subject.setLectureHours(40);
        subject.setExerciseHours(20);
        subject.setPracticumHours(0);
        subject.setMandatory("izborni");
        subject.setLectureSessions(20);
        subject.setExerciseSessions(10);

        return subject;
    }
}
