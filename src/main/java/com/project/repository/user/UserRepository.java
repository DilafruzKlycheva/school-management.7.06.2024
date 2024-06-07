package com.project.repository.user;

import com.project.entity.concretes.user.User;
import com.project.entity.enums.RoleType;
import com.project.payload.response.user.StudentResponse;
import com.project.payload.response.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Arrays;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsernameEquals(String username);

    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsBySsn(String ssn);

    boolean existsByPhoneNumber(String phone);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.userRole.roleName = :roleName") // JPQL versiyonu
    Page<User> findByUserByRole(String roleName, Pageable pageable);

    List<User> getUserByNameContaining(String name);

    @Query(value = "SELECT COUNT(u) FROM User u WHERE u.userRole.roleType = ?1")
    long countAdmin(RoleType roleType);

    List<User> findByAdvisorTeacherId(Long id);

    @Query("SELECT u FROM User u WHERE u.isAdvisor=?1")
    List<User> findAllByAdvisor(Boolean aTrue);

    @Query(value = "SELECT ( COUNT (u)>0 ) FROM User u WHERE u.userRole.roleType = ?1")
    boolean findStudent(RoleType roleType);

    @Query(value = "SELECT MAX (u.studentNumber) FROM User u")
    int getMaxStudentNumber();

    @Query("SELECT u from User u where u.id IN :studentIds")
    List<User> findByIdsEquals(Long[] studentIds);
}
