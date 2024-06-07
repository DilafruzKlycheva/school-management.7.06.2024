package com.project;

import com.project.entity.concretes.user.UserRole;
import com.project.entity.enums.Gender;
import com.project.entity.enums.RoleType;
import com.project.payload.request.user.UserRequest;
import com.project.repository.user.UserRoleRepository;
import com.project.service.user.UserRoleService;
import com.project.service.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;

@SpringBootApplication
public class SchoolManagmentApplication implements CommandLineRunner {

	private final UserRoleService userRoleService;
	private final UserRoleRepository userRoleRepository;
	private final UserService userService;

    public SchoolManagmentApplication(UserRoleService userRoleService, UserRoleRepository userRoleRepository, UserService userService) {
        this.userRoleService = userRoleService;
        this.userRoleRepository = userRoleRepository;
        this.userService = userService;
    }

    public static void main(String[] args) {
		SpringApplication.run(SchoolManagmentApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {

		// Role tablomu dolduracagim ama once bos mu diye kontrol edecegim
		if(userRoleService.getAllUserRole().isEmpty()){

			UserRole admin = new UserRole();
			admin.setRoleType(RoleType.ADMIN);
			admin.setRoleName("Admin");
			userRoleRepository.save(admin);

			UserRole dean = new UserRole();
			dean.setRoleType(RoleType.MANAGER);
			dean.setRoleName("Dean");
			userRoleRepository.save(dean);

			UserRole viceDean = new UserRole();
			viceDean.setRoleType(RoleType.ASSISTANT_MANAGER);
			viceDean.setRoleName("ViceDean");
			userRoleRepository.save(viceDean);

			UserRole student = new UserRole();
			student.setRoleType(RoleType.STUDENT);
			student.setRoleName("Student");
			userRoleRepository.save(student);

			UserRole teacher = new UserRole();
			teacher.setRoleType(RoleType.TEACHER);
			teacher.setRoleName("Teacher");
			userRoleRepository.save(teacher);
		}
		// Built_in Admin olusturuluyor eger sistemde Admin yoksa
		if(userService.countAllAdmins()==0){

			UserRequest adminRequest = new UserRequest();
			adminRequest.setUsername("Admin");
			adminRequest.setEmail("admin@admin.com");
			adminRequest.setSsn("111-11-1111");
			adminRequest.setPassword("123456");
			adminRequest.setName("Ahmet");
			adminRequest.setSurname("Mehmet");
			adminRequest.setPhoneNumber("111-111-1111");
			adminRequest.setGender(Gender.MALE);
			adminRequest.setBirthDay(LocalDate.of(1980,2,2));
			adminRequest.setBirthPlace("Ankara");
			userService.saveUser(adminRequest,"Admin");
		}

	}
}
