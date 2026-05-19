package com.cinemaebooking.backend.combo.presentation;

import com.cinemaebooking.backend.combo.application.dto.ComboResponse;
import com.cinemaebooking.backend.combo.application.dto.CreateComboRequest;
import com.cinemaebooking.backend.combo.application.dto.UpdateComboRequest;
import com.cinemaebooking.backend.combo.application.usecase.*;
import com.cinemaebooking.backend.combo.domain.valueobject.ComboId;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/combos")
@RequiredArgsConstructor
public class ComboController {

    private final CreateComboUseCase createComboUseCase;
    private final UpdateComboUseCase updateComboUseCase;
    private final DeleteComboUseCase deleteComboUseCase;
    private final GetComboDetailUseCase getComboDetailUseCase;
    private final GetComboListUseCase getComboListUseCase;
    private final ActivateComboUseCase activateComboUseCase;
    private final DeactivateComboUseCase deactivateComboUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComboResponse createCombo(@RequestBody CreateComboRequest request) {
        return createComboUseCase.execute(request);
    }

    @PutMapping("/{id}")
    public ComboResponse updateCombo(@PathVariable Long id,
                                     @RequestBody UpdateComboRequest request) {
        ComboId comboId = toComboId(id);
        return updateComboUseCase.execute(comboId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCombo(@PathVariable Long id) {
        ComboId comboId = toComboId(id);
        deleteComboUseCase.execute(comboId);
    }

    @GetMapping("/{id}")
    public ComboResponse getComboDetail(@PathVariable Long id) {
        ComboId comboId = toComboId(id);
        return getComboDetailUseCase.execute(comboId);
    }

    @GetMapping
    public Page<ComboResponse> getComboList(@PageableDefault(size = 10) Pageable pageable) {
        return getComboListUseCase.execute(pageable);
    }

    @PostMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.OK)
    public ComboResponse activateCombo(@PathVariable Long id) {
        ComboId comboId = toComboId(id);
        return activateComboUseCase.execute(comboId);
    }

    @PostMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    public ComboResponse deactivateCombo(@PathVariable Long id) {
        ComboId comboId = toComboId(id);
        return deactivateComboUseCase.execute(comboId);
    }

    private ComboId toComboId(Long id) {
        if (id == null) {
            throw CommonExceptions.invalidInput("Combo id must not be null");
        }
        return ComboId.of(id);
    }
}