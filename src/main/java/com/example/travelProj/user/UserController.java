package com.example.travelProj.user;

import com.example.travelProj.Image;
import com.example.travelProj.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final ImageService imageService;

    @GetMapping("/signup")
    public String signup(@ModelAttribute UserCreateForm userCreateForm) {
        return "/user/signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "notEqual",
                    "Passwords do not match.");
            return "signup_form";
        }
        try {
            userService.create(userCreateForm.getUsername(),
                    userCreateForm.getEmail(), userCreateForm.getPassword1(), userCreateForm.getNickname());
        }catch(DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "User already exists.");
            return "signup_form";
        }catch(Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }
        return "redirect:/";
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

        // 프로필 이미지 URL을 출력
        String profileImageUrl = (user.getProfileImage() != null) ? user.getProfileImage().getUrl() : null;

        System.out.println("Profile Image URL: " + profileImageUrl);

        // 모델에 사용자 정보 및 프로필 이미지 URL 추가
        model.addAttribute("user", user);
        model.addAttribute("profileImageUrl", profileImageUrl);

        return "user/mypage"; // 사용자 마이페이지로 이동
    }

    @GetMapping("/mypage/edit")
    public String editUserInfo(@AuthenticationPrincipal SiteUser siteUser, Model model) {
        model.addAttribute("user", siteUser);
        return "user/mypage";
    }

    @PostMapping("/mypage/edit")
    public String updateUserInfo(@ModelAttribute SiteUser updatedUser,
                                 @RequestParam(value = "file", required = false) MultipartFile file,
                                 @AuthenticationPrincipal SiteUser user) throws IOException {
        // 닉네임 업데이트
        user.setNickname(updatedUser.getNickname());

        // 프로필 이미지 업로드
        if (file != null && !file.isEmpty()) {
            // UserService의 updateProfileImage 메서드 호출
            userService.updateProfileImage(user.getId(), file);
        }

        // 사용자 정보 저장
        userService.updateUser(user); // 현재 사용자 정보 업데이트
        return "redirect:/mypage";
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

    @PostMapping("/uploadProfileImage")
    public ResponseEntity<?> uploadProfileImage(
            @AuthenticationPrincipal SiteUser user,
            @RequestParam("profileImage") MultipartFile file) {

        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Login is required."));
            }

            // UserService의 updatedProfileImage를 호출
            userService.updateProfileImage(user.getId(), file);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "The profile image has been successfully uploaded."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred during file upload."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An unexpected error occurred."));
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

    @PostMapping("/mypage/delete")
    public String deleteUser(@AuthenticationPrincipal SiteUser siteUser) {
        userService.deleteUser(siteUser.getId());
        return "redirect:/";
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
