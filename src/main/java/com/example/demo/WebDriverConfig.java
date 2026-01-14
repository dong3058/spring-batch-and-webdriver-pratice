package com.example.demo;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;

@Configuration
public class WebDriverConfig {
    @Bean
    public WebDriver WebDriverCreater(){
        ChromeOptions options = new ChromeOptions();

        options.setBinary("C:/Program Files/Google/Chrome/Application/chrome.exe");
        // 1. 봇 감지 시스템의 핵심인 'AutomationControlled' 속성을 끕니다.
        options.addArguments("--disable-blink-features=AutomationControlled");
// 2. 브라우저가 자동화 도구에 의해 제어되고 있다는 상단 바를 제거합니다.
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

// 3. User-Agent를 실제 브라우저와 동일하게 설정합니다.
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        // 헤드리스 모드 설정: 브라우저 UI 없이 실행
        options.addArguments("--headless");
        // 리소스 절약을 위한 GPU 가속 비활성화
        options.addArguments("--disable-gpu");
        // Linux 환경에서 Chrome을 사용할 때 발생할 수 있는 문제 우회
        options.addArguments("--no-sandbox");
        // /dev/shm이 접근할 수 없을 때의 문제를 피하기 위한 설정
        options.addArguments("--disable-dev-shm-usage");
        // 이미지 로딩 비활성화 (옵션)
        options.addArguments("--disable-images");
        // 확장 프로그램 비활성화
        options.addArguments("--disable-extensions");
        // SSL 인증서 무시 설정
        options.addArguments("--ignore-ssl-errors=yes");
        options.addArguments("--ignore-certificate-errors");
        // 브라우저 창 크기 설정
        options.addArguments("--window-size=1200,800");
        // 국가 언어 설정
        options.addArguments("--lang=ko"); // 한국어로 설정

        // 성능 최적화 관련 속성 추가
        options.addArguments("--disable-setuid-sandbox");
        options.addArguments("--disable-default-apps");
        options.addArguments("--no-first-run");
        options.addArguments("--no-service-autorun");

        options.addArguments("--remote-allow-origins=*");
        // 시크릿 모드 설정
        options.addArguments("--incognito");

        // Chrome의 적용 가능한 최적화
        options.setExperimentalOption("prefs", Map.of(
                "profile.default_content_setting_values.notifications", 2, // 알림 비활성화
                "profile.default_content_setting_values.images", 2, // 이미지 로딩 비활성화
                "profile.history.disabled", true
        ));

        return new ChromeDriver(options);
    }
}
