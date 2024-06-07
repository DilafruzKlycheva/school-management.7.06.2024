package com.project.controller.business;

import com.project.payload.request.business.StudentInfoRequest;
import com.project.payload.request.business.UpdateStudentInfoRequest;
import com.project.payload.response.business.ResponseMessage;
import com.project.payload.response.business.StudentInfoResponse;
import com.project.service.business.StudentInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/studentInfo")
@RequiredArgsConstructor
public class StudentInfoController {

    private final StudentInfoService studentInfoService;

    @PreAuthorize("hasAnyAuthority('TEACHER')")
    @PostMapping("/save") // http://localhost:8080/studentInfo/save   + POST  + JSON
    public ResponseMessage<StudentInfoResponse> saveStudentInfo(HttpServletRequest httpServletRequest,
                                                                @RequestBody @Valid StudentInfoRequest studentInfoRequest){

        return studentInfoService.saveStudentInfo(httpServletRequest, studentInfoRequest);
    }

    // Not : ( ODEV )  Delete() ************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @DeleteMapping("/delete/{studentInfoId}")// http://localhost:8080/studentInfo/delete/1
    public ResponseMessage delete (@PathVariable Long studentInfoId){
        return studentInfoService.deleteStudentInfo(studentInfoId);
    }

    // Not: ( ODEV ) getAllWithPage ********************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    @GetMapping("/getAllStudentInfoByPage") // http://localhost:8080/studentInfo/getAllStudentInfoByPage?page=0&size=10&sort=id&type=desc
    public Page<StudentInfoResponse> getAllStudentInfoByPage(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "sort") String sort,
            @RequestParam(value = "type") String type
    ) {
        return  studentInfoService.getAllStudentInfoByPage(page,size,sort,type);
    }

    // Not: ( ODEV ) Update() *************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @PutMapping("/update/{studentInfoId}") // http://localhost:8080/studentInfo/update/1
    // student id bilgisine ihtiyac olmadigi icin, icinde studentId olmayan yeni bir DTO yazdik
    public ResponseMessage<StudentInfoResponse>update(@RequestBody @Valid UpdateStudentInfoRequest studentInfoRequest,
                                                      @PathVariable Long studentInfoId){
        return studentInfoService.update(studentInfoRequest,studentInfoId);
    }

    // !!! -> Bir ogretmen kendi ogrencilerinin bilgilerini almak isterse :
    @PreAuthorize("hasAnyAuthority('TEACHER')")
    @GetMapping("/getAllForTeacher")   // http://localhost:8080/studentInfo/getAllForTeacher
    public ResponseEntity<Page<StudentInfoResponse>> getAllForTeacher(
            HttpServletRequest httpServletRequest,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size
    ){
        return new ResponseEntity<>(studentInfoService.getAllForTeacher(httpServletRequest,page,size), HttpStatus.OK);
    }

    // !!! --> bir ogrenci kendi bilgilerini almak isterse
    @PreAuthorize("hasAnyAuthority('STUDENT')")
    @GetMapping("/getAllForStudent")  // http://localhost:8080/studentInfo/getAllForStudent
    public ResponseEntity<Page<StudentInfoResponse>> getAllForStudent(
            HttpServletRequest httpServletRequest,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size

    ){
        return new ResponseEntity<>(studentInfoService.getAllForStudent(httpServletRequest,page,size), HttpStatus.OK);
    }




}
