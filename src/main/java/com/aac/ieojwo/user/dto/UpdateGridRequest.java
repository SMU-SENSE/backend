package com.aac.ieojwo.user.dto;
import com.aac.ieojwo.user.domain.GridSize;
import jakarta.validation.constraints.NotNull;
public record UpdateGridRequest(@NotNull GridSize gridSize) {}
