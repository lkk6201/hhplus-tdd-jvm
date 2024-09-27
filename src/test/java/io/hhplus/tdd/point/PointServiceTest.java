package io.hhplus.tdd.point;

import io.hhplus.tdd.config.PointConfig;
import io.hhplus.tdd.domain.point.PointHistory;
import io.hhplus.tdd.domain.point.TransactionType;
import io.hhplus.tdd.domain.point.UserPoint;
import io.hhplus.tdd.exception.PointException;
import io.hhplus.tdd.repository.point.PointRepository;
import io.hhplus.tdd.service.point.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PointConfig pointConfig;

    private UserPoint userPoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 초기 포인트를 1000으로 설정한 유저 세팅
        userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());
    }

    @Test
    @DisplayName("포인트 조회 성공")
    void getUserPointSuccess() {
        long userId = 1L;

        when(pointRepository.selectPointById(userId)).thenReturn(userPoint);

        UserPoint resultUserPoint = pointService.getUserPoint(userId);

        assertEquals(userPoint, resultUserPoint);
        verify(pointRepository).selectPointById(userId);
    }

    @Test
    @DisplayName("포인트 히스토리 조회 성공")
    void getUserPointHistorySuccess() {
        long userId = 1L;

        PointHistory mockHistory1 = new PointHistory(1L, userId, 100, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistory mockHistory2 = new PointHistory(2L, userId, 100, TransactionType.USE, System.currentTimeMillis());

        List<PointHistory> mockHistoryList = Arrays.asList(mockHistory1, mockHistory2);

        when(pointRepository.selectAllPointHistoryById(userId)).thenReturn(mockHistoryList);

        List<PointHistory> resultList = pointService.getUserPointHistory(userId);

        assertEquals(2, resultList.size());
        assertEquals(mockHistory1, resultList.get(0));
        assertEquals(mockHistory2, resultList.get(1));
    }

    @Test
    @DisplayName("포인트 충전 성공")
    void chargeUserPoint() {
        long userId = 1L;
        long currentPoint = 1000L;
        long amountToCharge = 50L;
        long maxBalance = 100000L;

        when(pointRepository.selectPointById(userId)).thenReturn(userPoint);
        when(pointConfig.getMaxBalance()).thenReturn(maxBalance);
        when(pointRepository.insertOrUpdate(userId, currentPoint + amountToCharge))
                .thenReturn(new UserPoint(userId, currentPoint + amountToCharge, System.currentTimeMillis()));

        UserPoint updatedUserPoint = pointService.chargeUserPoint(userId, amountToCharge);

        assertEquals(currentPoint + amountToCharge, updatedUserPoint.point());
        verify(pointRepository, times(1)).insertPointHistory(eq(userId), eq(amountToCharge), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    @DisplayName("포인트 충전 시 최대 잔고 초과")
    void chargeUserPointExceedsMaxBalance() {
        long userId = 1L;
        long amountToCharge = 999999L;
        long maxBalance = 100000;

        when(pointRepository.selectPointById(userId)).thenReturn(userPoint);
        when(pointConfig.getMaxBalance()).thenReturn(maxBalance);

        PointException exception = assertThrows(PointException.class, () -> pointService.chargeUserPoint(userId, amountToCharge));
        assertEquals("충전 후 포인트 잔고는 " + maxBalance + "포인트를 초과할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("포인트 충전 시 충전 대상 포인트가 0 또는 음수인지 확인")
    void chargeUserPointNegativeAmount() {
        long userId = 1L;
        long invalidAmount = -50;

        PointException exception = assertThrows(PointException.class, () -> pointService.chargeUserPoint(userId, invalidAmount));
        assertEquals("충전할 포인트는 0보다 큰 값을 가져야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("포인트 사용 성공")
    void useUserPointSuccess() {
        long userId = 1L;
        long currentPoint = 1000L;
        long amountToUse = 50L;

        when(pointRepository.selectPointById(userId)).thenReturn(userPoint);
        when(pointRepository.insertOrUpdate(userId, currentPoint - amountToUse))
                .thenReturn(new UserPoint(userId, currentPoint - amountToUse, System.currentTimeMillis()));

        UserPoint updatedUserPoint = pointService.useUserPoint(userId, amountToUse);

        assertEquals(currentPoint - amountToUse, updatedUserPoint.point());
        verify(pointRepository, times(1)).insertPointHistory(eq(userId), eq(amountToUse), eq(TransactionType.USE), anyLong());
    }

    @Test
    @DisplayName("포인트 사용 시 사용 대상 포인트가 0 또는 음수인지 확인")
    void useUserPointNegativeAmount() {
        long userId = 1L;
        long invalidAmount = -50;

        PointException exception = assertThrows(PointException.class, () -> pointService.useUserPoint(userId, invalidAmount));
        assertEquals("사용할 포인트는 0보다 큰 값을 가져야 합니다.", exception.getMessage());
    }
}
