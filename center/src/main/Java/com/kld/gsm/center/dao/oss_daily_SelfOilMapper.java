package com.kld.gsm.center.dao;

import com.kld.gsm.center.common.MysqlRepository;
import com.kld.gsm.center.domain.oss_daily_SelfOil;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author mjxu
 * @version 1.0
 * @CreationTime: 2016/7/20 10:09
 * @Description:
 */
@MysqlRepository
public interface oss_daily_SelfOilMapper {

    List<oss_daily_SelfOil>  selectId();
}