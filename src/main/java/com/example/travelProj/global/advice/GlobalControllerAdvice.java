package com.example.travelProj.global.advice;

import com.example.travelProj.domain.user.SiteUser;
import com.example.travelProj.domain.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@RequiredArgsConstructor
@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserService userService;

    @ModelAttribute
    public void addUserToModel(HttpSession session, Model model) {
        SiteUser currentUser = (SiteUser) session.getAttribute("user");

        if (currentUser != null) {
            // 세션에 있는 사용자 정보를 최신으로 갱신
            SiteUser updatedUser = userService.findUserById(currentUser.getId());
            session.setAttribute("user", updatedUser);  // 세션에 최신 사용자 정보 저장

            model.addAttribute("currentUser", updatedUser);  // 모델에 최신 사용자 정보 추가
        }
    }
}
