package com.kld.gsm.coord.timertask;


import com.kld.gsm.ATG.dao.MonitorTimeInventoryDao;
import com.kld.gsm.ATG.dao.SysManageCanInfoDao;
import com.kld.gsm.ATG.domain.SysManageCanInfo;
import com.kld.gsm.ATGDevice.ATGManager;
import com.kld.gsm.ATGDevice.atg_stock_data_out_t;
import com.kld.gsm.ATGDevice.atg_timestock_data_in_t;
import com.kld.gsm.MacLog.util.EhCacheHelper;
import com.kld.gsm.coord.Context;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/*
Created BY niyang
Created Date 2016/1/10
@description: 实时罐存线程
*/
public class RTTimeStockThead extends Thread{
    Logger logger = Logger.getLogger(RTTimeStockThead.class);
    //static List canList = new ArrayList();

    @Autowired
    private MonitorTimeInventoryDao monitortimeInventoryDao;
    @Autowired
    SysManageCanInfoDao sysManageCanInfodao;

    @Override
    public void run() {
        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
        String pid = rt.getName();
        MDC.put("PID", pid);
        sysManageCanInfodao = Context.getInstance().getBean(SysManageCanInfoDao.class);
        //获取所有罐号
        List<SysManageCanInfo> oilCanInforList = sysManageCanInfodao.selectAll();
        logger.info("oilCanInforList.size:"+oilCanInforList.size());

     /*   for (SysManageCanInfo o : oilCanInforList) {
            logger.info("o.getOilcan():"+o.getOilcan());
            canList.add(o.getOilcan());
        }*/
        while(true){
            try
            {
                logger.info("begin");
                sleep(200);
                logger.info("200ms wakeup");
                boolean res=getstock();
                logger.info("获取实时罐存end："+res+","+new Date().toString());
                if (res){
                    //执行成功睡
                    try {
                        //间隔时间
                        sleep(TimeTaskPar.get("rtstockjg") * 1000);
                        logger.info("wake up");
                    } catch (InterruptedException e) {
                        logger.error("实时罐存间隔Inter:" + e);
                    }catch (Exception e){
                        logger.error("实时罐存间隔:" + e);
                    }
                }
            }catch (Exception e){
                logger.error("获取实时罐存异常："+new Date().toString()+e.getStackTrace());
            }
        }
    }

    public boolean getstock(){
        Integer result=0;
        Callable<Integer> call = new Callable<Integer>() {
            public Integer call() throws Exception {
                try {
                    logger.info("获取实时罐存"+new Date().toString());
                    boolean res=ATGManager.getStockByThread();
                    return res==true?1:0;
                }catch (Exception e){
                    return 0;
                }
            }
        };
        logger.info("new exec");
        final ExecutorService exec = Executors.newFixedThreadPool(1);
        logger.info("end new exec");
        Future<Integer> future = exec.submit(call);
        logger.info("submit call");
        try {
            // set  timeout to 10 seconds
            int iSleep=TimeTaskPar.get("rtstockcs");
            if(TimeTaskPar.get("rtstockcs")<1)
            {
                iSleep=7;
            }
            result = future.get(1000 * (iSleep-1), TimeUnit.MILLISECONDS);
            logger.info("获取实时罐存 value from call is :" + result);
        } catch (TimeoutException ex) {
            logger.error("===============获取实时罐存 task time out===============\n" + new Date().toString());
            future.cancel(true);//
            logger.info("超时取消线程：" + future.isCancelled());
        } catch (Exception e) {
            logger.error("===============获取实时罐存异常==============\n" + e.getMessage());
            future.cancel(true);
            logger.info("异常取消线程：" + future.isCancelled());
        }
        // close thread pool
        //exec.shutdown();
        exec.shutdownNow();
        logger.info("线程状态：" + future.isCancelled());
        if (result==1){
            return true;
        }else {
            return false;
        }

    }
}
