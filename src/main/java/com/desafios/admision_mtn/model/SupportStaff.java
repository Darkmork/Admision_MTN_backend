package com.desafios.admision_mtn.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "support_staff")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SupportStaff {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private SupportStaffType staffType;

    private String department;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "support_staff_responsibilities", joinColumns = @JoinColumn(name = "staff_id"))
    @Column(name = "responsibility")
    private List<String> responsibilities;

    @Column(nullable = false)
    private boolean canAccessReports = false;

    @Column(nullable = false)
    private boolean canManageSchedules = false;
}