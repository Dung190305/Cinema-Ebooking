package com.cinemaebooking.backend.loyalty.presentation;

import com.cinemaebooking.backend.loyalty.application.dto.earning_rule.CreateEarningRuleRequest;
import com.cinemaebooking.backend.loyalty.application.dto.earning_rule.EarningRuleResponse;
import com.cinemaebooking.backend.loyalty.application.dto.earning_rule.UpdateEarningRuleRequest;
import com.cinemaebooking.backend.loyalty.application.usecase.earning_rule.*;
import com.cinemaebooking.backend.loyalty.domain.valueobject.EarningRuleId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loyalty/earning-rules")
@RequiredArgsConstructor
public class EarningRuleController {
    private final CreateEarningRuleUseCase create;
    private final UpdateEarningRuleUseCase update;
    private final DeleteEarningRuleUseCase delete;
    private final GetEarningRuleDetailUseCase detail;
    private final GetEarningRuleListUseCase list;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EarningRuleResponse create(@RequestBody CreateEarningRuleRequest request) {
        return create.execute(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public EarningRuleResponse update(@PathVariable Long id, @RequestBody UpdateEarningRuleRequest request) {
        return update.execute(EarningRuleId.of(id), request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        delete.execute(EarningRuleId.of(id));
    }

    @GetMapping("/{id}")
    public EarningRuleResponse detail(@PathVariable Long id) {
        return detail.execute(EarningRuleId.of(id));
    }

    @GetMapping
    public List<EarningRuleResponse> list() {
        return list.execute();
    }
}