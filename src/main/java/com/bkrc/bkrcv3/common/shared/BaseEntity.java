package com.bkrc.bkrcv3.common.shared;

import com.bkrc.bkrcv3.common.utils.LocalDateUtils;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {
    //private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    @CreatedDate
    private LocalDateTime created;

    @LastModifiedDate
    private LocalDateTime updated;

    @CreatedBy
    private Long createdBy;

    @LastModifiedBy
    private Long updatedBy;

    public String getCreatedDate() {
        return LocalDateUtils.getDate(created);
    }

    public String getCreatedDateTime() {
        return LocalDateUtils.getDateTime(created);
    }

    public String getUpdatedDate() {
        return LocalDateUtils.getDate(updated);
    }

    public String getUpdatedDateTime() {
        return LocalDateUtils.getDateTime(updated);
    }

}
