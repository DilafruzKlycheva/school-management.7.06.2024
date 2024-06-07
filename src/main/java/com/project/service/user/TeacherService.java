package com.project.service.user;

import com.project.entity.concretes.business.LessonProgram;
import com.project.entity.concretes.user.User;
import com.project.entity.enums.RoleType;
import com.project.exception.ConflictException;
import com.project.payload.mappers.UserMapper;
import com.project.payload.messages.ErrorMessages;
import com.project.payload.messages.SuccessMessages;
import com.project.payload.request.user.TeacherRequest;
import com.project.payload.response.business.ResponseMessage;
import com.project.payload.response.user.StudentResponse;
import com.project.payload.response.user.TeacherResponse;
import com.project.payload.response.user.UserResponse;
import com.project.repository.user.UserRepository;
import com.project.service.helper.MethodHelper;
import com.project.service.validator.UniquePropertyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final UserRepository userRepository;
    private final UniquePropertyValidator uniquePropertyValidator;
    private final UserMapper userMapper;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;
    private final MethodHelper methodHelper;

    public ResponseMessage<TeacherResponse> saveTeacher(TeacherRequest teacherRequest) {

        // TODO : LessonProgram eklenecek

        //!!! unique kontrolu
        uniquePropertyValidator.checkDuplicate(teacherRequest.getUsername(),
                teacherRequest.getSsn(), teacherRequest.getPhoneNumber(),teacherRequest.getEmail());

        // DTO --> POJO
        User teacher = userMapper.mapTeacherRequestToUser(teacherRequest);

        teacher.setUserRole(userRoleService.getUserRole(RoleType.TEACHER));
        //TODO: LessonProgram eklenecek.
        teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));
        if(teacherRequest.getIsAdvisorTeacher()){
            teacher.setIsAdvisor(Boolean.TRUE);
        } else teacher.setIsAdvisor(Boolean.FALSE);

        User savedTeacher = userRepository.save(teacher);

        return ResponseMessage.<TeacherResponse>builder()
                .message(SuccessMessages.TEACHER_SAVE)
                .httpStatus(HttpStatus.CREATED)
                .object(userMapper.mapUserToTeacherResponse(savedTeacher))
                .build();
    }

    public List<StudentResponse> getAllStudentByAdvisorUsername(String userName) {

        User teacher = methodHelper.isUserExistByUsername(userName);

        methodHelper.checkAdvisor(teacher);

        return userRepository.findByAdvisorTeacherId(teacher.getId())
                .stream()
                .map(userMapper::mapUserToStudentResponse)
                .collect(Collectors.toList());

    }

    public List<UserResponse> getAllAdvisorTeacher() {

        return userRepository.findAllByAdvisor(Boolean.TRUE)
                .stream()
                .map(userMapper::mapUserToUserResponse)
                .collect(Collectors.toList());
    }

    public ResponseMessage<TeacherResponse> updateTeacherForManagers(TeacherRequest teacherRequest, Long userId) {

        User user = methodHelper.isUserExist(userId);
        // !!! Parametrede gelen id bir teacher a ait degilse exception firlatiliyor
        methodHelper.checkRole(user,RoleType.TEACHER);

        //!!! TODO: LessonProgramlar getiriliyor

        // !!! unique kontrolu
        uniquePropertyValidator.checkUniqueProperties(user, teacherRequest);
        // !!! DTO --> POJO
        User updatedTeacher = userMapper.mapTeacherRequestToUpdatedUser(teacherRequest, userId);
        // !!! props. that does n't exist in mappers
        updatedTeacher.setPassword(passwordEncoder.encode(teacherRequest.getPassword()));
        // !!! TODO: LessonProgram sonrasi eklenecek
        updatedTeacher.setUserRole(userRoleService.getUserRole(RoleType.TEACHER));

        User savedTeacher = userRepository.save(updatedTeacher);

        return ResponseMessage.<TeacherResponse>builder()
                .object(userMapper.mapUserToTeacherResponse(savedTeacher))
                .message(SuccessMessages.TEACHER_UPDATE)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public ResponseMessage<UserResponse> saveAdvisorTeacher(Long teacherId) {

        // !!! Save de yazdigimiz ya varsa kontrolu
        User teacher = methodHelper.isUserExist(teacherId);
        // !!! id ile gelen uer Teacher mi kontrolu
        methodHelper.checkRole(teacher,RoleType.TEACHER);

        // !!! id ile gelen teacher zaten advisor mi kontrolu ?
        if(Boolean.TRUE.equals(teacher.getIsAdvisor())) { // condition : teacher.getIsAdvisor()
            throw new ConflictException(
                    String.format(ErrorMessages.ALREADY_EXIST_ADVISOR_MESSAGE, teacherId));
        }

        teacher.setIsAdvisor(Boolean.TRUE);
        userRepository.save(teacher);

        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.ADVISOR_TEACHER_SAVE)
                .object(userMapper.mapUserToUserResponse(teacher))
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public ResponseMessage<UserResponse> deleteAdvisorTeacherById(Long teacherId) {
        User teacher = methodHelper.isUserExist(teacherId);
        // !!! id ile gelen user Teacher mi kontrolu
        methodHelper.checkRole(teacher,RoleType.TEACHER);

        // !!! id ile gelen teacheradvisor mi kontrolu ?
        methodHelper.checkAdvisor(teacher);

        teacher.setIsAdvisor(Boolean.FALSE);
        userRepository.save(teacher);

        // !!! silinen advisor Teacherlarin Student lari varsa bu iliskinin de koparilmasi gerekiyor
        List<User> allStudents = userRepository.findByAdvisorTeacherId(teacherId);
        if(!allStudents.isEmpty()) {
            allStudents.forEach(students -> students.setAdvisorTeacherId(null));
        }

        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.ADVISOR_TEACHER_DELETE)
                .object(userMapper.mapUserToUserResponse(teacher))
                .httpStatus(HttpStatus.OK)
                .build();
    }
}
