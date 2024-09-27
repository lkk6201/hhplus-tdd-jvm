package io.hhplus.tdd.service.point;

import io.hhplus.tdd.config.PointConfig;
import io.hhplus.tdd.domain.point.PointHistory;
import io.hhplus.tdd.domain.point.TransactionType;
import io.hhplus.tdd.domain.point.UserPoint;
import io.hhplus.tdd.exception.PointException;
import io.hhplus.tdd.repository.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointConfig pointConfig;

    private final ConcurrentHashMap<Long, Lock> userLocks = new ConcurrentHashMap<>();

    /**
     * 특정 유저의 포인트 조회
     * @param id
     * @return
     */
    public UserPoint getUserPoint(long id) {
        return pointRepository.selectPointById(id);
    }

    /**
     * 특정 유저의 포인트 히스토리 조회
     * @param id
     * @return
     */
    public List<PointHistory> getUserPointHistory(long id) {
        return pointRepository.selectAllPointHistoryById(id);
    }

    /**
     * 특정 유저의 포인트 충전
     * @param id
     * @param amount
     * @return
     */
    public UserPoint chargeUserPoint(long id, long amount) {
        if (amount <= 0) {
            throw new PointException("충전할 포인트는 0보다 큰 값을 가져야 합니다.");
        }

        Lock lock = userLocks.computeIfAbsent(id, k -> new ReentrantLock());

        try {
            lock.lock();
            // 대상 UserPoint 조회
            UserPoint targetUserPoint = pointRepository.selectPointById(id);

            // 최대 잔고 유효성 검사
            if (amount + targetUserPoint.point() > pointConfig.getMaxBalance()) {
                throw new PointException("충전 후 포인트 잔고는 " + pointConfig.getMaxBalance() + "포인트를 초과할 수 없습니다.");
            }

            // 포인트 충전
            UserPoint updatedUserPoint = pointRepository.insertOrUpdate(id, targetUserPoint.point() + amount);
            // 포인트 충전 히스토리 기록
            pointRepository.insertPointHistory(id, amount, TransactionType.CHARGE, updatedUserPoint.updateMillis());

            return updatedUserPoint;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 특정 유저의 포인트 사용
     * @param id
     * @param amount
     * @return
     */
    public UserPoint useUserPoint(long id, long amount) {
        if (amount <= 0) {
            throw new PointException("사용할 포인트는 0보다 큰 값을 가져야 합니다.");
        }

        Lock lock = userLocks.computeIfAbsent(id, k -> new ReentrantLock());

        try {
            lock.lock();
            // 대상 UserPoint 조회
            UserPoint userPoint = pointRepository.selectPointById(id);
            long currentPoint = userPoint.point();

            // 잔여 포인트가 부족한 경우 사용하지 못하도록 처리
            if (currentPoint < amount) {
                throw new PointException("잔여 포인트가 부족합니다.", currentPoint);
            }

            // 포인트 사용
            UserPoint updatedUserPoint = pointRepository.insertOrUpdate(id, currentPoint - amount);
            // 포인트 사용 히스토리 기록
            pointRepository.insertPointHistory(id, amount, TransactionType.USE, updatedUserPoint.updateMillis());

            return updatedUserPoint;
        } finally {
            lock.unlock();
        }
    }
}
