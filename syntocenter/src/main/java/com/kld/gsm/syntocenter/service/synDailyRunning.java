package com.kld.gsm.syntocenter.service;

/*
Created BY niyang
Created Date 2015/11/19
*/
public interface synDailyRunning {
    //交易加油流水表
    int synTradeAccountLost();
    //交易库存表
    int  TradeInventoryLost();
    //成品油日结存报表
    int OilDailyRecordLost();
    //班结库存表
    int ShiftStockLost();

}
