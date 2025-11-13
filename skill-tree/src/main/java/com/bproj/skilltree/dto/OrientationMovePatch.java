package com.bproj.skilltree.dto;

import com.bproj.skilltree.model.Orientation;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class OrientationMovePatch {
    @NotNull
    @Pattern(regexp= "SKILL|ACHIEVEMENT")
    private OrientationMoveType type;

    @NotNull
    private String id;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double x;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double y;

    public OrientationMovePatch(OrientationMoveType type, String id, Double x, Double y) {
        this.type = type;
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public OrientationMoveType getType() {
        return type;
    }

    public void setType(OrientationMoveType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }
}
