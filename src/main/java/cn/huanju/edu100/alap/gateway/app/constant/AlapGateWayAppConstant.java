package cn.huanju.edu100.alap.gateway.app.constant;

import org.springframework.core.Ordered;

/**
 * @Description: 服务用常量
 * @Author: xushengbin@hqwx.com
 * @Date: 2021-08-20
 */
public class AlapGateWayAppConstant {

    private static final int FilterOrder_Pre = Ordered.HIGHEST_PRECEDENCE;
    private static final int FilterOrder_Base = 0;
    private static final int FilterOrder_Max = Ordered.LOWEST_PRECEDENCE;

    public interface FilterOrder {
        int AuthToken = FilterOrder_Base;
    }
}
