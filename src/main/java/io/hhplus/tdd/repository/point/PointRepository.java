package io.hhplus.tdd.repository.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.domain.point.PointHistory;
import io.hhplus.tdd.domain.point.TransactionType;
import io.hhplus.tdd.domain.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointRepository {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    /**
     * 특정 유저의 포인트 조회
     * @param id
     * @return
     */
    public UserPoint selectPointById(long id) {
        return userPointTable.selectById(id);
    }

    /**
     * 특정 유저의 포인트 히스토리 조회
     * @return
     */
    public List<PointHistory> selectAllPointHistoryById(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    /**
     * 특정 유저의 포인트 충전
     * @param id
     * @param amount
     * @return
     */
    public UserPoint insertOrUpdate(long id, long amount) {
        return userPointTable.insertOrUpdate(id, amount);
    }

    /**
     * 특정 유저의 포인트 히스토리 기록
     * @param userId
     * @param amount
     * @param type
     * @param updateMillis
     */
    public void insertPointHistory(long userId, long amount, TransactionType type, long updateMillis) {
        pointHistoryTable.insert(userId, amount, type, updateMillis);
    }
}
