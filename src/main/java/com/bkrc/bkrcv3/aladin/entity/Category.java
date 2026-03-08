package com.bkrc.bkrcv3.aladin.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name="CATEGORY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cid;
    @Column(name="sub_cid")
    private String subCid;
    @Column
    private String depth1;
}
