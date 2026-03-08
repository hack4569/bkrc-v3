package com.bkrc.bkrcv3.history.application;

import com.bkrc.bkrcv3.history.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    Optional<List<History>> findAllByLoginId(String loginId);
    Integer deleteByLoginId(String loginId);
}
