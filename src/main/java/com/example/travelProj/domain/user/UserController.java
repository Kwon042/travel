package com.example.travelProj.domain.user;

import com.example.travelProj.domain.image.imageuser.ImageUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final ImageUserService imageUserService;

    @GetMapping("/signup")
    public String signup(@ModelAttribute UserCreateForm userCreateForm) {
        return "/user/signup_form";
    }

    @PostMapping("/signup")
    public String signUp(@ModelAttribute("userCreateForm") UserCreateForm userCreateForm,
                         BindingResult bindingResult) {
        // 중복 체크 및 비밀번호 확인
        checkForDuplicateFields(userCreateForm, bindingResult);
        checkPasswordMatch(userCreateForm, bindingResult);

        // 오류가 있으면 폼을 다시 반환
        if (bindingResult.hasErrors()) {
            return "user/signup_form";
        }

        // 사용자 이름에 "admin"이 포함되어 있으면 ADMIN 권한 부여
        boolean isAdmin = userCreateForm.getUsername().toLowerCase().contains("admin");

        // 회원가입 처리
        String result = handleUserCreation(userCreateForm, bindingResult, isAdmin);

        if (result != null) {
            return result; // 오류가 발생하면 폼을 다시 반환
        }

        // 회원가입 성공 시 로그인 페이지로 리디렉션
        return "redirect:/user/login";
    }

    private void checkForDuplicateFields(UserCreateForm userCreateForm, BindingResult bindingResult) {
        // username 중복 체크
        if (userService.existsByUsername(userCreateForm.getUsername())) {
            bindingResult.rejectValue("username", "username.exists", "Username is already taken.");
        }
        // email 중복 체크
        if (userService.existsByEmail(userCreateForm.getEmail())) {
            bindingResult.rejectValue("email", "email.exists", "Email is already taken.");
        }
        // nickname 중복 체크
        if (userService.existsByNickname(userCreateForm.getNickname())) {
            bindingResult.rejectValue("nickname", "nickname.exists", "Nickname is already taken.");
        }
    }

    private void checkPasswordMatch(UserCreateForm userCreateForm, BindingResult bindingResult) {
        // 비밀번호 일치 확인
        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "notEqual", "Passwords do not match.");
        }
    }

    private String handleUserCreation(UserCreateForm userCreateForm, BindingResult bindingResult, boolean isAdmin) {
        try {
            // 사용자 이름에 "admin"이 포함되면 createAdmin 사용, 그렇지 않으면 create 사용
            if (isAdmin) {
                userService.createAdmin(userCreateForm.getUsername(),
                        userCreateForm.getEmail(),
                        userCreateForm.getPassword1(),
                        userCreateForm.getNickname(),
                        "ADMIN");
            } else {
                userService.create(userCreateForm.getUsername(),
                        userCreateForm.getEmail(),
                        userCreateForm.getPassword1(),
                        userCreateForm.getNickname(),
                        "USER");
            }
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "User already exists.");
            return "user/signup_form"; // 오류가 있으면 다시 폼을 반환
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "user/signup_form"; // 오류가 있으면 다시 폼을 반환
        }
        return null; // 오류가 없으면 null 반환
    }

    @GetMapping("/login")
    public String login() {
        return "/user/login_form";
    }

    @GetMapping("/mypage")
    public String showMypage(Model model, @AuthenticationPrincipal SiteUser user) {
        // 현재 인증된 사용자가 없거나 인증되지 않은 경우 로그인 페이지로 리다이렉트
        if (user == null) {
            return "redirect:/user/login";
        }
        SiteUser currentUser = userService.findUserById(user.getId());
        // 프로필 이미지 URL을 출력
        String profileImageUrl = user.getProfileImageUrl();

        System.out.println("Profile Image URL: " + profileImageUrl);

        // 모델에 사용자 정보 및 프로필 이미지 URL 추가
        model.addAttribute("user", currentUser);
        model.addAttribute("profileImageUrl", profileImageUrl);

        return "user/mypage";
    }

    @GetMapping("/mypage/edit")
    public String updateUserInfo(@AuthenticationPrincipal SiteUser siteUser, Model model) {
        model.addAttribute("user", siteUser);
        return "user/mypage";
    }

    @PostMapping("/mypage/edit")
    public String updateUserInfo(@ModelAttribute SiteUser updatedUser,
                                 @RequestParam(value = "file", required = false) MultipartFile file,
                                 @AuthenticationPrincipal SiteUser user,
                                 HttpSession session)throws IOException {
        // 닉네임 업데이트
        user.setNickname(updatedUser.getNickname());

        // 프로필 이미지 업로드
        if (file != null && !file.isEmpty()) {
            String imageUrl = imageUserService.uploadProfileImage(user.getId(), file);
            System.out.println("Uploaded image URL: " + imageUrl);

            String cacheBustedImageUrl = imageUrl + "?v=" + System.currentTimeMillis();  // 캐시 무효화
            user.setProfileImageUrl(cacheBustedImageUrl);
        }

        // 사용자 정보 저장
        userService.updateUser(user);
        session.setAttribute("user", user);
        return "redirect:/user/mypage";
    }

    // 사용자 닉네임 및 이메일 업데이트 메서드
    @PostMapping("/mypage/update")
    public ResponseEntity<?> updateUserInfo(@RequestBody UserUpdateRequest request,
                                            @AuthenticationPrincipal SiteUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Login is required."));
        }

        try {
            switch (request.getField()) {
                case "nickname":
                    userService.updateNickname(user.getId(), request.getValue());
                    break;
                case "email":
                    userService.updateEmail(user.getId(), request.getValue());
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Invalid request."));
            }
            return ResponseEntity.ok(Map.of("success", true, "message", "Successfully updated."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred during file upload."));
        }
    }

    @GetMapping("/mypage/change_password")
    public String showChangePasswordPage() {
        // 모달로 보내기
        return "redirect:/user/mypage";
    }

    @PostMapping("/mypage/change_password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @AuthenticationPrincipal SiteUser siteUser) {
        if(passwordEncoder.matches(currentPassword, siteUser.getPassword())) {
            siteUser.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(siteUser);
            return "redirect:/user/mypage";
        }else {
            return "redirect:/user/mypage/change_password?error";
        }
    }

    @GetMapping("/getCurrentUserId")
    public ResponseEntity<Map<String, Object>> getCurrentUserId(@AuthenticationPrincipal SiteUser user) {
        if (user == null) {
            return ResponseEntity.ok(Map.of("success", false));
        }
        return ResponseEntity.ok(Map.of("success", true, "userId", user.getId()));
    }

    @PostMapping("/deleteAccount")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal SiteUser user, HttpServletRequest request, HttpServletResponse response) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Login is required."));
        }

        try {
            userService.deleteUser(user.getId());

            // 로그아웃 처리
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication()); // Authentication 추가

            return ResponseEntity.ok(Map.of("success", true, "message", "The account has been deleted."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred while deleting the account."));
        }
    }


}
