package com.project.service.business;

import com.project.entity.concretes.business.EducationTerm;
import com.project.exception.BadRequestException;
import com.project.exception.ResourceNotFoundException;
import com.project.payload.mappers.EducationTermMapper;
import com.project.payload.messages.ErrorMessages;
import com.project.payload.messages.SuccessMessages;
import com.project.payload.request.business.EducationTermRequest;
import com.project.payload.response.business.EducationTermResponse;
import com.project.payload.response.business.ResponseMessage;
import com.project.repository.business.EducationTermRepository;
import com.project.service.helper.PageableHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationTermService {

    private final EducationTermRepository educationTermRepository;
    private final EducationTermMapper educationTermMapper;
    private final PageableHelper pageableHelper;


    // Not: save() *******************************************************************************
    public ResponseMessage<EducationTermResponse> saveEducationTerm(EducationTermRequest educationTermRequest){
        validateEducationTermDates(educationTermRequest);
        EducationTerm savedEducationTerm =
                educationTermRepository.save(educationTermMapper.mapEducationTermRequestToEducationTerm(educationTermRequest));
        return ResponseMessage.<EducationTermResponse>builder()
                .message(SuccessMessages.EDUCATION_TERM_SAVE)
                .object(educationTermMapper.mapEducationTermToEducationTermResponse(savedEducationTerm))
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    // !!! yrd Metod - 1 *******************************************************************
    private void validateEducationTermDatesForRequest(EducationTermRequest educationTermRequest){
        // !!! bu metodda amacimiz requestten gelen registrationDate,StartDate ve endDate arasindaki
        // tarih sirasina gore dogru mu setlenmis onu kontrol etmek

        // registration > start
        if(educationTermRequest.getLastRegistrationDate().isAfter(educationTermRequest.getStartDate())){
            throw new ResourceNotFoundException(
                    ErrorMessages.EDUCATION_START_DATE_IS_EARLIER_THAN_LAST_REGISTRATION_DATE);
        }
        // end > start
        if(educationTermRequest.getEndDate().isBefore(educationTermRequest.getStartDate())){
            throw new ResourceNotFoundException(
                    ErrorMessages.EDUCATION_END_DATE_IS_EARLIER_THAN_START_DATE);
        }
    }

    // !!! yrd Metod - 2 ********************************************************************
    private void validateEducationTermDates(EducationTermRequest educationTermRequest){

        validateEducationTermDatesForRequest(educationTermRequest); // Yrd Method - 1

        // !!! Bir yil icinde bir tane Guz donemi veya Yaz Donemi olmali kontrolu
        if(educationTermRepository.existsByTermAndYear( // JPQL
                educationTermRequest.getTerm(),educationTermRequest.getStartDate().getYear())){
            throw new ResourceNotFoundException(
                    ErrorMessages.EDUCATION_TERM_IS_ALREADY_EXIST_BY_TERM_AND_YEAR_MESSAGE);
        }
        // !!! yil icine eklencek educationTerm, mevcuttakilerin tarihleri ile cakismamali ****************************
        if(educationTermRepository.findByYear(educationTermRequest.getStartDate().getYear())
                .stream()
                .anyMatch(educationTerm ->
                        (			educationTerm.getStartDate().equals(educationTermRequest.getStartDate()) //!!! 1. kontrol : baslama tarihleri ayni ise --> et1(10 kasim 2023) / YeniEt(10 kasim 2023)
                                || (educationTerm.getStartDate().isBefore(educationTermRequest.getStartDate())//!!! 2. kontrol : baslama tarihi mevcuttun baslama ve bitis tarihi ortasinda ise -->
                                   && educationTerm.getEndDate().isAfter(educationTermRequest.getStartDate())) // Ornek : et1 ( baslama 10 kasim 20203 - bitme 20 kasim 20203)  - YeniEt ( baslama 15 kasim 2023 bitme 25 kasim 20203)
                                || (educationTerm.getStartDate().isBefore(educationTermRequest.getEndDate()) //!!! 3. kontrol bitis tarihi mevcuttun baslama ve bitis tarihi ortasinda ise
                                   && educationTerm.getEndDate().isAfter(educationTermRequest.getEndDate()))// Ornek : et1 ( baslama 10 kasim 20203 - bitme 20 kasim 20203)  - YeniEt ( baslama 09 kasim 2023 bitme 15 kasim 20203)
                                || (educationTerm.getStartDate().isAfter(educationTermRequest.getStartDate()) //!!!4.kontrol : yeni eklenecek eskiyi tamamen kapsiyorsa
                                   && educationTerm.getEndDate().isBefore(educationTermRequest.getEndDate()))//et1 ( baslama 10 kasim 20203 - bitme 20 kasim 20203)  - YeniEt ( baslama 09 kasim 2023 bitme 25 kasim 20203)
                        ))
        ) {
            throw new BadRequestException(ErrorMessages.EDUCATION_TERM_CONFLICT_MESSAGE);
        }
    }

    public EducationTermResponse getEducationTermById(Long id) {
        EducationTerm term = isEducationTermExist(id);
        return educationTermMapper.mapEducationTermToEducationTermResponse(term);
    }

    private EducationTerm isEducationTermExist(Long id){
        return educationTermRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException(String.format(ErrorMessages.EDUCATION_TERM_NOT_FOUND_MESSAGE,id)));
    }

    public List<EducationTermResponse> getAllEducationTerms() {

        return educationTermRepository.findAll()
                .stream()
                .map(educationTermMapper::mapEducationTermToEducationTermResponse)
                .collect(Collectors.toList());
    }

    public Page<EducationTermResponse> getAllEducationTermsByPage(int page, int size, String sort, String type) {

        Pageable pageable = pageableHelper.getPageableWithProperties(page, size, sort, type);
        return educationTermRepository.findAll(pageable).map(educationTermMapper::mapEducationTermToEducationTermResponse);
    }

    // Not: deleteById() *********************************************************************
    public ResponseMessage deleteEducationTermById(Long id){
        isEducationTermExist(id);
        educationTermRepository.deleteById(id);
        //!!! SORU : EducationTerm silinince LessonProgramlar ne olacak, buraya onuda sileecek
         // kodlar eklememiz gerekecek mi?? Hayir, EducationTerm entityde Cascade kullanildigi icin
         // gerek yok..
        return ResponseMessage.builder()
                .message(SuccessMessages.EDUCATION_TERM_DELETE)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    // Not: updateById() *********************************************************************
    public ResponseMessage<EducationTermResponse>updateEducationTerm(Long id,EducationTermRequest educationTermRequest){
        // !!! ıd var mı ???
        isEducationTermExist(id);
        // !!! gırılen tarıhler dogru mu ???
        validateEducationTermDates(educationTermRequest);

        EducationTerm educationTermUpdated =
                educationTermRepository.save(
                        educationTermMapper.mapEducationTermRequestToUpdatedEducationTerm(id,educationTermRequest));

        return ResponseMessage.<EducationTermResponse>builder()
                .message(SuccessMessages.EDUCATION_TERM_UPDATE)
                .httpStatus(HttpStatus.OK)
                .object(educationTermMapper.mapEducationTermToEducationTermResponse(educationTermUpdated))
                .build();
    }

    public EducationTerm findEducationTermById(Long educationTermId){
        return isEducationTermExist(educationTermId);
    }
}


















