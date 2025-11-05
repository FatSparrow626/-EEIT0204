package com.project.core.schedule;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.core.service.InventoryAlertService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j //不用在寫logFactory
@Component //交給spring管理
@RequiredArgsConstructor
public class LowStockScheduler {
    private final InventoryAlertService alertService;

    //代表每天的9點跟下午3點會各掃描一次
    @Scheduled(cron = "0 0 9,15 * * ?")//秒 分 時 日 月 星期(?)代表不關心這欄位
    public void runTwiceDaily(){
        log.info("低庫存通知觸發");
        alertService.checkAndNotifyLowStock(); 
    }

    @Scheduled(cron = "0 */1 * * * ?")// *代表全部 */10代表每10分鐘掃描一次
    public void run10minDaily(){
        log.info("低庫存通知觸發");
        alertService.checkAndNotifyLowStock();
    }
}
