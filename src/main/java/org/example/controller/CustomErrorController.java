package org.example.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/error.html")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        if (status != null) {
            model.addAttribute("status", status);
        }

        if (message != null) {
            model.addAttribute("message", message);
        } else {
            model.addAttribute("message", "認証に失敗しました。ユーザー名とパスワードを確認してください。");
        }

        model.addAttribute("error", "Authentication Failed");
        model.addAttribute("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));

        // デバッグ情報をコンソールに出力
        System.out.println("エラー発生: " + status + " - " + message);

        return "error";
    }
}