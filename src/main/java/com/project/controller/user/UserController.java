package com.project.controller.user;

import com.project.payload.request.user.UserRequest;
import com.project.payload.request.user.UserRequestWithoutPassword;
import com.project.payload.response.abstracts.BaseUserResponse;
import com.project.payload.response.business.ResponseMessage;
import com.project.payload.response.user.UserResponse;
import com.project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/save/{userRole}") // http://localhost:8080/user/save/Admin  + JSON + POST
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ResponseMessage<UserResponse>> saveUser(@RequestBody @Valid UserRequest userRequest,
                                                                  @PathVariable String userRole){
        return ResponseEntity.ok(userService.saveUser(userRequest, userRole));
    }

    // Not: getAllAdminOrDeanOrViceDeanByPage() ******************************************
    @GetMapping("/getAllUserByPage/{userRole}") // http://localhost:8080/user/getAllUserByPage/Admin
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getUserByPage(
            @PathVariable String userRole,
            @RequestParam(value = "page",defaultValue = "0") int page,
            @RequestParam(value = "size",defaultValue = "10") int size,
            @RequestParam(value = "sort",defaultValue = "name") String sort,
            @RequestParam(value = "type",defaultValue = "desc") String type
    ){
        Page<UserResponse> adminsOrDeans = userService.getUsersByPage(page,size,sort,type,userRole);
        return new ResponseEntity<>(adminsOrDeans, HttpStatus.OK) ;
    }

    // Not :  getUserById() *********************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @GetMapping("getUserById/{userId}")  // http://localhost:8080/user/getUserById/1
    public ResponseMessage<BaseUserResponse> getUserById(@PathVariable Long userId){
        return userService.getUserById(userId) ;
    }

    // Not : deleteUser() **********************************************************
    // !!! Admin ise hepsini silebilsin
    // !!! Mudur ve Mudur Yrd ise altindaki rol yetkisi olani silebilsin
    @DeleteMapping("/delete/{id}") //http://localhost:8080/user/delete/3
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id ,
                                                 HttpServletRequest httpServletRequest){
        return ResponseEntity.ok(userService.deleteUserById(id , httpServletRequest)) ;
    }

    // Not: updateAdminOrDeanOrViceDean() ********************************************
    // !!! Admin --> Dean veya  ViceDEan i guncellerken kullanilacak method
    // !!! Student ve teacher icin ekstra fieldlar gerekecegi icin, baska endpoint gerekiyor
    @PutMapping("/update/{userId}") // http://localhost:8080/user/update/1
    @PreAuthorize("hasAuthority('ADMIN')")
    //!!! donen deger BaseUserResponse --> polymorphism
    public ResponseMessage<BaseUserResponse> updateAdminDeanViceDeanForAdmin(
            @RequestBody @Valid UserRequest userRequest,
            @PathVariable Long userId){
        return userService.updateUser(userRequest,userId) ;
    }

    // Not: updateUserForUser() **********************************************************
    // !!! Kullanicinin kendisini update etmesini saglayan method
    // !!! AuthenticationController da updatePassword oldugu icin buradaki DTO da password olmamali
    @PatchMapping("/updateUser")   // http://localhost:8080/user/updateUser
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER','TEACHER')")
    public ResponseEntity<String>updateUser(@RequestBody @Valid
                                                UserRequestWithoutPassword userRequestWithoutPassword,
                                            HttpServletRequest request){
        return userService.updateUserForUsers(userRequestWithoutPassword, request) ;
    }


    // Not : getByName() ***************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    @GetMapping("/getUserByName")   // http://localhost:8080/user/getUserByName?name=user1
    public List<UserResponse> getUserByName(@RequestParam (name = "name") String userName){
        return userService.getUserByName(userName) ;
    }

}