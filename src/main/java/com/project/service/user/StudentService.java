package com.project.service.user;

import com.project.entity.concretes.business.LessonProgram;
import com.project.entity.concretes.user.User;
import com.project.entity.enums.RoleType;
import com.project.payload.mappers.UserMapper;
import com.project.payload.messages.SuccessMessages;
import com.project.payload.request.user.StudentRequest;
import com.project.payload.request.user.StudentRequestWithoutPassword;
import com.project.payload.response.business.ResponseMessage;
import com.project.payload.response.user.StudentResponse;
import com.project.repository.user.UserRepository;
import com.project.service.helper.MethodHelper;
import com.project.service.validator.UniquePropertyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final UserRepository userRepository;
    private final MethodHelper methodHelper;
    private final UniquePropertyValidator uniquePropertyValidator;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleService userRoleService;

    public ResponseMessage<StudentResponse> saveStudent(StudentRequest studentRequest) {

        User advisoryTeacher = methodHelper.isUserExist(studentRequest.getAdvisorTeacherId());
        methodHelper.checkAdvisor(advisoryTeacher);
        uniquePropertyValidator.checkDuplicate(studentRequest.getUsername(),
                studentRequest.getSsn(), studentRequest.getPhoneNumber(), studentRequest.getEmail());

        User student = userMapper.mapStudentRequestToUser(studentRequest);
        student.setAdvisorTeacherId(advisoryTeacher.getId());
        student.setPassword(passwordEncoder.encode(studentRequest.getPassword()));
        student.setUserRole(userRoleService.getUserRole(RoleType.STUDENT));
        student.setActive(true);
        student.setIsAdvisor(Boolean.FALSE);

        student.setStudentNumber(getLastNumber());

        return ResponseMessage.<StudentResponse>builder()
                .object(userMapper.mapUserToStudentResponse(userRepository.save(student)))
                .message(SuccessMessages.STUDENT_SAVE)
                .build();
    }

    private int getLastNumber(){
        //DB de hıc ogrencı yoksa  ogrencı numarası olarak 1000 gonderıyoruz
        if(!userRepository.findStudent(RoleType.STUDENT)){
            return 1000;
        }

        return userRepository.getMaxStudentNumber() + 1 ;
    }

    // Not: updateStudent() **********************************************************
    public ResponseMessage<StudentResponse> updateStudentForManagers(Long userId,
                                                                     StudentRequest studentRequest) {
        User user = methodHelper.isUserExist(userId);
        // !!! Parametrede gelen id bir student'a ait degilse exception firlatiliyor
        methodHelper.checkRole(user,RoleType.STUDENT);
        // !!! unique kontrolu
        uniquePropertyValidator.checkUniqueProperties(user, studentRequest);

        user.setName(studentRequest.getName());
        user.setSurname(studentRequest.getSurname());
        user.setBirthDay(studentRequest.getBirthDay());
        user.setBirthPlace(studentRequest.getBirthPlace());
        user.setSsn(studentRequest.getSsn());
        user.setEmail(studentRequest.getEmail());
        user.setPhoneNumber(studentRequest.getPhoneNumber());
        user.setGender(studentRequest.getGender());
        user.setMotherName(studentRequest.getMotherName());
        user.setFatherName(studentRequest.getFatherName());
        user.setPassword(passwordEncoder.encode(studentRequest.getPassword()));
        user.setAdvisorTeacherId(studentRequest.getAdvisorTeacherId());

        return ResponseMessage.<StudentResponse>builder()
                .object(userMapper.mapUserToStudentResponse(userRepository.save(user)))
                .message(SuccessMessages.STUDENT_UPDATE)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    // Not: ChangeActıveStatusOfStudent() ***********************************************
    public ResponseMessage changeStatusOfStudent(Long studentId, boolean status) {

        User student = methodHelper.isUserExist(studentId);

        methodHelper.checkRole(student,RoleType.STUDENT);

        student.setActive(status);
        userRepository.save(student);
        return ResponseMessage.builder()
                .message("Student is " + (status ? "active" : "passive")) // ternary operator
                .httpStatus(HttpStatus.OK)
                .build();
    }
    // Not: updateStudentForStudents() **********************************************************
    public ResponseEntity<String> updateStudent(StudentRequestWithoutPassword studentRequest,
                                                HttpServletRequest request) {

        String userName = (String) request.getAttribute("username");
        User student = userRepository.findByUsername(userName);

        // !!! unique kontrolu
        uniquePropertyValidator.checkUniqueProperties(student, studentRequest);

        student.setMotherName(studentRequest.getMotherName());
        student.setFatherName(studentRequest.getFatherName());
        student.setBirthDay(studentRequest.getBirthDay());
        student.setEmail(studentRequest.getEmail());
        student.setPhoneNumber(studentRequest.getPhoneNumber());
        student.setBirthPlace(studentRequest.getBirthPlace());
        student.setGender(studentRequest.getGender());
        student.setName(studentRequest.getName());
        student.setSurname(studentRequest.getSurname());
        student.setSsn(studentRequest.getSsn());

        userRepository.save(student);

        String message = SuccessMessages.USER_UPDATE;

        return ResponseEntity.ok(message);
    }
}
