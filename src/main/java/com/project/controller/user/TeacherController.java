package com.project.controller.user;

import com.project.payload.request.user.TeacherRequest;
import com.project.payload.response.business.ResponseMessage;
import com.project.payload.response.user.StudentResponse;
import com.project.payload.response.user.TeacherResponse;
import com.project.payload.response.user.UserResponse;
import com.project.service.user.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping("/save") // http://localhost:8080/teacher/save + JSON + POST
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ResponseMessage<TeacherResponse>> saveTeacher(@RequestBody @Valid TeacherRequest teacherRequest){
        return ResponseEntity.ok(teacherService.saveTeacher(teacherRequest));
    }

    // Not: ODEVVV updateTeacherById() ***************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    @PutMapping("/update/{userId}")  // http://localhost:8080/user/update/1
    public ResponseMessage<TeacherResponse>updateTeacherForManagers(@RequestBody @Valid TeacherRequest teacherRequest,
                                                                    @PathVariable Long userId){
        return teacherService.updateTeacherForManagers(teacherRequest,userId);
    }

    // Not: ODEVV SaveAdvisorTeacherByTeacherId() ****************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    @PatchMapping("/saveAdvisorTeacher/{teacherId}") // http://localhost:8080/teacher/saveAdvisorTeacher/1
    public ResponseMessage<UserResponse> saveAdvisorTeacher (@PathVariable Long teacherId){
        return teacherService.saveAdvisorTeacher(teacherId);
    }

    // Not : ODEVV  deleteAdvisorTeacherById() *******************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    @DeleteMapping("/deleteAdvisorTeacherById/{id}")// http://localhost:8080/teacher/deleteAdvisorTeacherById/1
    public ResponseMessage<UserResponse> deleteAdvisorTeacherById(@PathVariable Long id){
        return teacherService.deleteAdvisorTeacherById(id);
    }


    // Not: GetAllStudentByAdvisorUserName() **********************************************
    // !!! Bir rehber ogretmenin kendi ogrencilerin tamamini getiren metod
    @GetMapping("/getAllStudentByAdvisorUsername") // http://localhost:8080/teacher/getAllStudentByAdvisorUsername
    @PreAuthorize("hasAnyAuthority('TEACHER')")
    public List<StudentResponse> getAllStudentByAdvisorUsername(HttpServletRequest request){
        String userName = request.getHeader("username");
        return teacherService.getAllStudentByAdvisorUsername(userName);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    @GetMapping("/getAllAdvisorTeacher") // http://localhost:8080/teacher/getAllAdvisorTeacher
    public List<UserResponse> getAllAdvisorTeacher(){
        return teacherService.getAllAdvisorTeacher();
    }

    // TODO: LESSOn PROGRAM ekleme


}
