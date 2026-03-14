package com.heang.koriaibackend.domain.scenarios.controller;

import com.heang.koriaibackend.common.api.ApiResponse;
import com.heang.koriaibackend.domain.scenarios.dto.ScenarioResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private static final List<ScenarioResponse> SCENARIOS = List.of(
            new ScenarioResponse("1", "Ordering at a Restaurant", "Daily Life", "Beginner",
                    "Practice ordering food and drinks in Korean", "Order a full meal politely",
                    "안녕하세요! 식당에 오신 것을 환영합니다. 무엇을 주문하시겠어요? (Hello! Welcome to the restaurant. What would you like to order?)"),
            new ScenarioResponse("2", "At the Convenience Store", "Daily Life", "Beginner",
                    "Buy items and ask about prices", "Complete a purchase using Korean numbers and polite speech",
                    "어서오세요! 찾으시는 게 있으신가요? (Welcome! Is there something you're looking for?)"),
            new ScenarioResponse("3", "Taking the Subway", "Transport", "Beginner",
                    "Ask for directions and buy a subway ticket", "Navigate the Seoul subway in Korean",
                    "실례합니다. 명동역은 어느 방향인가요? (Excuse me. Which direction is Myeongdong station?)"),
            new ScenarioResponse("4", "Job Interview", "Professional", "Intermediate",
                    "Practice formal Korean in a job interview context", "Introduce yourself and answer interview questions formally",
                    "안녕하세요. 오늘 면접을 보러 온 지원자입니다. 잘 부탁드립니다. (Hello. I am the applicant here for today's interview. I look forward to working with you.)"),
            new ScenarioResponse("5", "Visiting a Doctor", "Health", "Intermediate",
                    "Describe symptoms and understand medical advice", "Communicate health issues clearly in Korean",
                    "어디가 불편하세요? 증상이 언제부터 시작됐나요? (Where does it hurt? When did the symptoms start?)"),
            new ScenarioResponse("6", "Making Friends at University", "Social", "Intermediate",
                    "Small talk and getting to know someone", "Have a natural conversation with a new Korean friend",
                    "안녕하세요! 저도 이 수업 듣는데, 혹시 어느 학과예요? (Hi! I'm also in this class, what's your major?)"),
            new ScenarioResponse("7", "Business Negotiation", "Professional", "Advanced",
                    "Negotiate terms and discuss business proposals in formal Korean", "Complete a business deal using honorific speech",
                    "오늘 미팅에 참석해 주셔서 감사드립니다. 제안서를 검토하셨는지요? (Thank you for attending today's meeting. Have you reviewed our proposal?)"),
            new ScenarioResponse("8", "Discussing Current Events", "Culture", "Advanced",
                    "Talk about news, politics, and social issues in Korean", "Express complex opinions fluently",
                    "최근 뉴스에서 흥미로운 기사를 보셨나요? 어떻게 생각하세요? (Have you seen any interesting news recently? What do you think?)")
    );

    @GetMapping
    public ApiResponse<List<ScenarioResponse>> getList() {
        return ApiResponse.success(SCENARIOS);
    }

    @GetMapping("/{id}")
    public ApiResponse<ScenarioResponse> getById(@PathVariable String id) {
        return SCENARIOS.stream()
                .filter(s -> s.id().equals(id))
                .findFirst()
                .map(ApiResponse::success)
                .orElse(ApiResponse.success(null));
    }
}