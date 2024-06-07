package com.project.controller.user;

import com.project.payload.request.user.StudentRequest;
import com.project.payload.request.user.StudentRequestWithoutPassword;
import com.project.payload.response.business.ResponseMessage;
import com.project.payload.response.user.StudentResponse;
import com.project.service.user.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/save") // http://localhost:8080/student/save + JSON + POST
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ResponseMessage<StudentResponse>> saveStudent(
            @RequestBody @Valid StudentRequest studentRequest){
        return ResponseEntity.ok(studentService.saveStudent(studentRequest));
    }

    // Not: updateStudentForStudents() **********************************************************
    // !!! ogrencinin kendisini update etme islemi
    @PatchMapping("/update")   // http://localhost:8080/user/updateStudent
    @PreAuthorize("hasAnyAuthority('STUDENT')")
    public ResponseEntity<String> updateStudent(@RequestBody @Valid
                                                    StudentRequestWithoutPassword studentRequestWithoutPassword,
                                                    HttpServletRequest request){
        return studentService.updateStudent(studentRequestWithoutPassword, request);
    }

    // Not: updateStudent() **********************************************************
    @PutMapping("/update/{userId}")   // http://localhost:8080/user/update/2
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public ResponseMessage<StudentResponse>updateStudentForManagers(
            @PathVariable Long userId,
            @RequestBody @Valid StudentRequest studentRequest){
        return studentService.updateStudentForManagers(userId,studentRequest);
    }
    // Not: ChangeActıveStatusOfStudent() ***********************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    @GetMapping("/changeStatus") // /students/changeStatus?id=1&status=true
    public ResponseMessage changeStatusOfStudent (@RequestParam Long id, @RequestParam boolean status){
        return studentService.changeStatusOfStudent(id,status);
    }
    // TODO : LESSON PROGRAM

}
