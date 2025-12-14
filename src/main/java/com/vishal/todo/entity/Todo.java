package com.vishal.todo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "todos",
        indexes = {
                @Index(name = "idx_todos_user_id", columnList = "user_id"),
                @Index(name = "idx_todos_created_at", columnList = "created_at")
        }
)
@Data
@NoArgsConstructor
public class Todo {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    // 🔗 Owner of the todo
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_todo_user")
    )
    private User user;

    // ---------- Auditing ----------
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---------- Soft Delete ----------
    @Column(nullable = false)
    private boolean deleted = false;
}
