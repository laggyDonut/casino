package de.edvschuleplattling.irgendwieanders.model.usermanagement.administratormanagement;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
public class AdminNote implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Useraccount user; // Über wen ist die Notiz?

    @ManyToOne(fetch = FetchType.LAZY)
    private Useraccount author; // Welcher Admin hat sie geschrieben?

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public AdminNote(Useraccount user, Useraccount author, String content) {
        this.user = user;
        this.author = author;
        this.content = content;
    }
}