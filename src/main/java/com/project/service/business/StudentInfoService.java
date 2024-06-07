package com.project.service.business;

import com.project.entity.concretes.business.EducationTerm;
import com.project.entity.concretes.business.Lesson;
import com.project.entity.concretes.business.StudentInfo;
import com.project.entity.concretes.user.User;
import com.project.entity.enums.Note;
import com.project.entity.enums.RoleType;
import com.project.exception.ConflictException;
import com.project.exception.ResourceNotFoundException;
import com.project.payload.mappers.StudentInfoMapper;
import com.project.payload.messages.ErrorMessages;
import com.project.payload.messages.SuccessMessages;
import com.project.payload.request.business.StudentInfoRequest;
import com.project.payload.request.business.UpdateStudentInfoRequest;
import com.project.payload.response.business.ResponseMessage;
import com.project.payload.response.business.StudentInfoResponse;
import com.project.repository.business.StudentInfoRepository;
import com.project.service.helper.MethodHelper;
import com.project.service.helper.PageableHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class StudentInfoService {

    private final StudentInfoRepository studentInfoRepository;
    private final MethodHelper methodHelper;
    private final LessonService lessonService;
    private final EducationTermService educationTermService;
    private final StudentInfoMapper studentInfoMapper;
    private final PageableHelper pageableHelper;


    @Value("${midterm.exam.percentage}")
    private Double midtermExamPercentage;

    @Value("${final.exam.percentage}")
    private Double finalExamPercentage;

    public ResponseMessage<StudentInfoResponse> saveStudentInfo(HttpServletRequest httpServletRequest,
                                                                StudentInfoRequest studentInfoRequest) {

        String teacherUsername = (String) httpServletRequest.getAttribute("username");
        // !!! requestte gelen studentId ile studenti getirme
        User student = methodHelper.isUserExist(studentInfoRequest.getStudentId());
        // requestten gelen studentId gercekten bir Studenta mi ait
        methodHelper.checkRole(student, RoleType.STUDENT);

        // !!! username ile teacher getirme
        User teacher = methodHelper.isUserExistByUsername(teacherUsername);
        // !!! requestten gelen lessonId ile lesson getiriyoruz
        Lesson lesson = lessonService.isLessonExistById(studentInfoRequest.getLessonId());
        // !!! requestten gelen educationTermId ile educationTerm getiriyoruz
        EducationTerm educationTerm =
                educationTermService.findEducationTermById(studentInfoRequest.getEducationTermId());

        // letterGrade hesaplamasi :
        Note note = checkLetterGrade(calculateAverageExam(studentInfoRequest.getMidtermExam(),
                studentInfoRequest.getFinalExam()));
        // DTO --> POJO
        StudentInfo studentInfo =
                studentInfoMapper.mapStudentInfoRequestToStudentInfo(
                studentInfoRequest,
                note,
                calculateAverageExam(studentInfoRequest.getMidtermExam(),
                        studentInfoRequest.getFinalExam()));

        studentInfo.setStudent(student);
        studentInfo.setTeacher(teacher);
        studentInfo.setEducationTerm(educationTerm);
        studentInfo.setLesson(lesson);

        StudentInfo savedStudentInfo = studentInfoRepository.save(studentInfo);

        return ResponseMessage.<StudentInfoResponse>builder()
                .message(SuccessMessages.STUDENT_INFO_SAVE)
                .object(studentInfoMapper.mapStudentInfoToStudentInfoResponse(savedStudentInfo))
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    private Double calculateAverageExam(Double midtermExam, Double finalExam){
        return ((midtermExam * midtermExamPercentage ) + (finalExam * finalExamPercentage));
    }

    private Note checkLetterGrade(Double average){
        if(average<50.0){
            return Note.FF;
        } else if (average<60) {
            return Note.DD;
        } else if (average<65) {
            return Note.CC;
        } else if (average<70) {
            return Note.CB;
        } else if (average<75) {
            return Note.BB;
        } else if (average<80) {
            return Note.BA;
        } else   {
            return Note.AA;
        }
    }

    public Page<StudentInfoResponse> getAllForTeacher(HttpServletRequest httpServletRequest, int page, int size) {
        Pageable pageable = pageableHelper.getPageableWithProperties(page, size);
        String username = (String) httpServletRequest.getAttribute("username");

        return studentInfoRepository.findByTeacherId_UsernameEquals(username, pageable)
                .map(studentInfoMapper::mapStudentInfoToStudentInfoResponse);
    }

    public Page<StudentInfoResponse> getAllForStudent(HttpServletRequest httpServletRequest, int page, int size) {
        Pageable pageable = pageableHelper.getPageableWithProperties(page, size);
        String username = (String) httpServletRequest.getAttribute("username");

        return studentInfoRepository.findByStudentId_UsernameEquals(username, pageable)
                .map(studentInfoMapper::mapStudentInfoToStudentInfoResponse);
    }

    public ResponseMessage deleteStudentInfo(Long studentInfoId){
        StudentInfo studentInfo = isStudentInfoExistById(studentInfoId);
        // !!! Silmeden once Student ve Teacherdan baglarini koparmamiz gerekiyor mu ??
        //	cevap : hayir, mappedBY ile studentInfo tarafini isaret ettigimiz icin,
        //	header sadece studentInfo tablosunda olusacagi icin, sadece bu tarafda
        //	silmek yeterli
        studentInfoRepository.deleteById(studentInfo.getId());

        return ResponseMessage.builder()
                .message(SuccessMessages.STUDENT_INFO_DELETE)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public StudentInfo isStudentInfoExistById(Long id){
        boolean isExist = studentInfoRepository.existsByIdEquals(id); //Derived
        if(!isExist){
            throw new ResourceNotFoundException(String.format(ErrorMessages.STUDENT_INFO_NOT_FOUND,id));
        } else {
            return studentInfoRepository.findById(id).get();
        }
    }

    public Page<StudentInfoResponse> getAllStudentInfoByPage(int page, int size, String sort, String type){
        Pageable pageable = pageableHelper.getPageableWithProperties(page, size, sort, type);
        return studentInfoRepository.findAll(pageable)
                .map(studentInfoMapper::mapStudentInfoToStudentInfoResponse);
    }

    public ResponseMessage<StudentInfoResponse>update(UpdateStudentInfoRequest studentInfoRequest,
                                                      Long studentInfoId){
        Lesson lesson = lessonService.isLessonExistById(studentInfoRequest.getLessonId());
        StudentInfo studentInfo = isStudentInfoExistById(studentInfoId); // delete de yazilan method
        EducationTerm educationTerm =
                educationTermService.findEducationTermById(studentInfoRequest.getEducationTermId());

        Double noteAverage =
                calculateAverageExam(studentInfoRequest.getMidtermExam(), studentInfoRequest.getFinalExam());
        Note note = checkLetterGrade(noteAverage);
        StudentInfo studentInfoForUpdate =
                studentInfoMapper.mapStudentInfoUpdateToStudentInfo(studentInfoRequest,
                        studentInfoId,
                        lesson,
                        educationTerm,
                        note,
                        noteAverage);
        studentInfoForUpdate.setStudent(studentInfo.getStudent());
        studentInfoForUpdate.setTeacher(studentInfo.getTeacher());
        StudentInfo updatedStudentInfo = studentInfoRepository.save(studentInfoForUpdate);
        return ResponseMessage.<StudentInfoResponse>builder()
                .message(SuccessMessages.STUDENT_INFO_UPDATE)
                .httpStatus(HttpStatus.OK)
                .object(studentInfoMapper.mapStudentInfoToStudentInfoResponse(updatedStudentInfo))
                .build();
    }

    private void checkSameLesson(Long studentId,String lessonName){
        boolean isLessonDuplicationExist =
                studentInfoRepository.getAllByStudentId_Id(studentId) // Derived Query
                        .stream()
                        .anyMatch(e->e.getLesson().getLessonName().equalsIgnoreCase(lessonName));
        if(isLessonDuplicationExist){
            throw new ConflictException(String.format(ErrorMessages.LESSON_ALREADY_EXIST_WITH_LESSON_NAME,lessonName));
        }
    }
}
