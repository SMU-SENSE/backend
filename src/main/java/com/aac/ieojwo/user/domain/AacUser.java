package com.aac.ieojwo.user.domain;

import com.aac.ieojwo.common.domain.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "aac_users")
public class AacUser extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GridSize gridSize;

    @Column(nullable = false)
    private boolean active;

    protected AacUser() {
    }

    private AacUser(String name, UserMode mode, GridSize gridSize) {
        this.name = name;
        this.mode = mode;
        this.gridSize = gridSize;
        this.active = true;
    }

    public static AacUser create(String name, UserMode mode, GridSize gridSize) {
        return new AacUser(name, mode, gridSize);
    }

    public void updateSettings(UserMode mode, GridSize gridSize) {
        this.mode = mode;
        this.gridSize = gridSize;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UserMode getMode() {
        return mode;
    }

    public GridSize getGridSize() {
        return gridSize;
    }

    public boolean isActive() {
        return active;
    }
}
