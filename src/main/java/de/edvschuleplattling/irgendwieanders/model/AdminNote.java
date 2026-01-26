package de.edvschuleplattling.irgendwieanders.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AdminNote implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Useraccount user; // Über wen ist die Notiz?

    @ManyToOne(fetch = FetchType.LAZY)
    private Useraccount author; // Welcher Admin hat sie geschrieben?

    @Column(nullable = false)
    private String content;

    private boolean isResolved; // Z.B. "Erledigt"

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }
}