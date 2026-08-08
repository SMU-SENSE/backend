package com.aac.ieojwo.user.domain;

import com.aac.ieojwo.common.domain.BaseTimeEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "aac_users")
public class AacUser extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 50) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private UserMode mode;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private GridSize gridSize;
    @Column(nullable = false) private boolean active;
    private LocalDate birthDate;
    @Column(length = 30) private String emergencyContact;
    @Column(length = 1000) private String notes;
    @Column(length = 500) private String profileImageUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private VoiceType voiceType;
    @Column(nullable = false, precision = 3, scale = 2) private BigDecimal speechRate;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private AacUserSetupStep setupStep;

    protected AacUser() {}

    private AacUser(String name, UserMode mode, GridSize gridSize) {
        this.name = name;
        this.mode = mode;
        this.gridSize = gridSize;
        this.active = true;
        this.voiceType = VoiceType.CHILD_MALE;
        this.speechRate = new BigDecimal("1.00");
        this.setupStep = AacUserSetupStep.PROFILE_COMPLETED;
    }

    public static AacUser create(String name, UserMode mode, GridSize gridSize) {
        return new AacUser(name, mode, gridSize);
    }

    public static AacUser createProfile(String name, LocalDate birthDate, String emergencyContact,
                                        String notes, String profileImageUrl, UserMode mode, GridSize gridSize) {
        AacUser user = new AacUser(name, mode == null ? UserMode.SIMPLE : mode,
                gridSize == null ? GridSize.GRID_3X3 : gridSize);
        user.birthDate = birthDate;
        user.emergencyContact = emergencyContact.trim();
        user.notes = blankToNull(notes);
        user.profileImageUrl = blankToNull(profileImageUrl);
        return user;
    }

    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    public void updateSettings(UserMode mode, GridSize gridSize) { this.mode = mode; this.gridSize = gridSize; }
    public void updateGrid(GridSize gridSize) { this.gridSize = gridSize; advanceTo(AacUserSetupStep.GRID_COMPLETED); }
    public void updateVoiceSettings(VoiceType voiceType, BigDecimal speechRate) { this.voiceType = voiceType; this.speechRate = speechRate; advanceTo(AacUserSetupStep.VOICE_COMPLETED); }
    public void confirmSetup() { this.setupStep = AacUserSetupStep.CONFIRMED; }
    private void advanceTo(AacUserSetupStep next) { if (setupStep.ordinal() < next.ordinal()) setupStep = next; }

    public Long getId() { return id; }
    public String getName() { return name; }
    public UserMode getMode() { return mode; }
    public GridSize getGridSize() { return gridSize; }
    public boolean isActive() { return active; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getEmergencyContact() { return emergencyContact; }
    public String getNotes() { return notes; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public VoiceType getVoiceType() { return voiceType; }
    public BigDecimal getSpeechRate() { return speechRate; }
    public AacUserSetupStep getSetupStep() { return setupStep; }
}
