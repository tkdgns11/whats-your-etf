package com.whatsyouretf.userservice.domain.portfolio.repository.mock;

import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioIssueRepository;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioIssues;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class MockPortfolioIssueRepository implements PortfolioIssueRepository {
    @Override
    public List<PortfolioIssues> getIssuesByPortfolioId(Long portfolioId) {
        return List.of(
            new PortfolioIssues(LocalDate.of(2025, 3, 12), "kodex 200 떡상", "kodex 200 15% 떡상"),
            new PortfolioIssues(LocalDate.of(2024, 2, 12), "kodex 200 떡상", "kodex 200 55% 떡상"),
            new PortfolioIssues(LocalDate.of(2023, 3, 5), "kodex 200 떡상", "kodex 200 35% 떡상")
        );
    }
}
