package com.project.repository.business;

import com.project.entity.concretes.business.LessonProgram;
import com.project.payload.response.business.LessonProgramResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public interface LessonProgramRepository extends JpaRepository<LessonProgram, Long> {
    List<LessonProgram> findByUsers_IdNull();

    @Query("SELECT l FROM LessonProgram l INNER JOIN l.users users WHERE users.username = ?1")
    Set<LessonProgram> getLessonProgramByUsersUsername(String userName);

    List<LessonProgram> findByUsers_IdNotNull();
}

