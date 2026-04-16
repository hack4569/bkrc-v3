package com.bkrc.bkrcv3.history.entity;

import com.bkrc.bkrcv3.common.shared.BaseEntity;
import com.bkrc.bkrcv3.history.application.HistoryResponse;
import com.bkrc.bkrcv3.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name="histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class History extends BaseEntity {
    @Id
    @Column(name = "history_id")
    private Long id;

    private String loginId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    private int itemId;

    public void setMember(Member member) {
        this.member = member;
    }

    public HistoryResponse of() {
        HistoryResponse historyResponse = new HistoryResponse();
        historyResponse.setItemId(getItemId());
        historyResponse.setLoginId(getLoginId());
        historyResponse.setCreatedAt(getCreated());
        return historyResponse;
    }
}