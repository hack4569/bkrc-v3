package com.bkrc.bkrcv3.like.application;

import com.bkrc.bkrcv3.like.entity.Like;
import com.bkrc.bkrcv3.like.entity.LikeCount;
import com.bkrc.bkrcv3.member.application.UserService;
import com.bkrc.bkrcv3.member.entity.Member;
import com.bkrc.bkrcv3.outbox.OutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class LikeServiceTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private LikeCountRepository likeCountRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private UserService userService;

    @Test
    void 동시사용자_10명_같은책_좋아요_테스트() throws InterruptedException {
        // given
        int threadCount = 10;           // 동시 사용자 수
        int itemId = 368038025;              // 테스트용 책 ID
        likeCountRepository.save(LikeCount.create(itemId, 0));
        // 서로 다른 loginId 생성
        List<String> loginIds = userService.getAllMembers().stream().map(Member::getLoginId).toList();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);  // 동시 출발 신호탄
        CountDownLatch doneLatch = new CountDownLatch(threadCount); // 완료 대기

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            final String loginId = loginIds.get(i);
            executor.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 동시에 출발하도록 대기
                    likeService.like(Like.create(itemId, loginId));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("[실패] " + e.getMessage()); // 이 줄 추가
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 동시 출발!
        doneLatch.await();      // 모든 스레드 완료 대기
        executor.shutdown();

        // then
        // 검증할 때 이렇게 사용
        LikeCount likeCount = likeCountRepository.findAll().stream().filter(i -> i.getItemId() == itemId).findFirst().get();
        List<Like> likes = likeRepository.findAll();

        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("성공: " + successCount.get() + "건");
        System.out.println("실패: " + failCount.get() + "건");
        System.out.println("DB LikeCount: " + likeCount.getLikeCount());
        System.out.println("DB Like 레코드 수: " + likes.size());

        // 10명 전부 성공해야 한다 (서로 다른 사용자이므로 중복 없음)
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failCount.get()).isEqualTo(0);

        // LikeCount가 race condition 없이 정확히 10이어야 한다
        assertThat(likeCount.getLikeCount()).isEqualTo(threadCount);

        // Like 레코드도 정확히 10개
        assertThat(likes.size()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("같은 사용자가 같은 책에 동시에 여러 번 좋아요 → 1번만 성공하고 나머지는 예외")
    void 동일유저_동시_중복좋아요_1건만_성공() throws InterruptedException {
        // given
        int threadCount = 5;
        int itemId = 366592752;
        String loginId = "lsh123";

        likeCountRepository.save(LikeCount.create(itemId, 0));

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    likeService.like(Like.create(itemId, loginId));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("[중복 좋아요 예외 발생] " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // then
        LikeCount likeCount = likeCountRepository.findAll().stream().filter(i -> i.getItemId() == itemId).findFirst().get();

        System.out.println("=== 중복 좋아요 테스트 결과 ===");
        System.out.println("성공: " + successCount.get() + "건");
        System.out.println("실패(중복): " + failCount.get() + "건");
        System.out.println("DB LikeCount: " + likeCount.getLikeCount());

        // 5번 시도 중 1번만 성공해야 한다
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);

        // LikeCount도 1이어야 한다
        assertThat(likeCount.getLikeCount()).isEqualTo(1);
    }
}