package com.foodorderingapp.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "categories",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"vendor_id", "name"})},
        indexes = {@Index(name = "idx_category_vendor", columnList = "vendor_id")}
)
@SQLRestriction("is_deleted = false") // Chuẩn Spring Boot 3 thay thế cho @Where
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Column(nullable = false)
    private String name;

    @JsonIgnore
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}