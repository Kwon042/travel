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
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
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
    public String showMypage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/user/login";
        }
        SiteUser user = (SiteUser) authentication.getPrincipal();

        // 프로필 이미지 URL을 출력
        String profileImageUrl = null;
        if (user.getProfileImage() != null) {
            profileImageUrl = user.getProfileImage().getUrl(); // Image 객체에서 URL을 가져옴
        }
        System.out.println("Profile Image URL: " + profileImageUrl);

        model.addAttribute("user", user);
        model.addAttribute("profileImageUrl", profileImageUrl);

        return "user/mypage";
    }

    @GetMapping("/mypage/edit")
    public String editUserInfo(@AuthenticationPrincipal SiteUser siteUser, Model model) {
        model.addAttribute("user", siteUser);
        return "user/mypage";
    }

    @PostMapping("/mypage/edit")
    public String updateUserInfo(@ModelAttribute SiteUser updatedUser,
                                 @RequestParam("file") MultipartFile file,
                                 @AuthenticationPrincipal SiteUser currentUser) throws IOException {
        // 닉네임 업데이트
        currentUser.setNickname(updatedUser.getNickname());

        // 프로필 이미지 업로드
        if (file != null && !file.isEmpty()) {
            // ImageService의 uploadProfileImage 메서드 호출
            String imageUrl = imageService.uploadProfileImage(currentUser.getId(), file);

            // profileImage 객체의 URL을 설정
            if (currentUser.getProfileImage() == null) {
                currentUser.setProfileImage(new Image());
            }
            currentUser.getProfileImage().setUrl(imageUrl); // 이미지 URL을 프로필에 설정
        }

        userService.updateUser(currentUser);
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

            // ImageService에서 프로필 이미지 업로드 처리
            String imageUrl = imageService.uploadProfileImage(user.getId(), file);

            // 프로필 이미지 URL을 SiteUser 객체에 설정
            if (user.getProfileImage() == null) {
                user.setProfileImage(new Image());
            }
            user.getProfileImage().setUrl(imageUrl);

            // 사용자 정보 업데이트
            userService.updateUser(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "The profile image has been successfully uploaded.",
                    "newProfileImageUrl", imageUrl // 새로운 프로필 이미지 URL 추가
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
