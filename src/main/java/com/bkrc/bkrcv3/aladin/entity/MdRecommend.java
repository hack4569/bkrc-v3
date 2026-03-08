package com.bkrc.bkrcv3.aladin.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MdRecommend {
    private String title;
    private String comment;
    private String mdName;
}


