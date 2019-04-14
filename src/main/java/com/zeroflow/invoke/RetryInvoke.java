package com.zeroflow.invoke;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zeroflow.base.BaseFlowHandler;
import com.zeroflow.base.BaseFlowLogHandler;
import com.zeroflow.bean.ErrorLog;
import com.zeroflow.utils.EnhanceLogger;
import com.zeroflow.utils.GenericsUtils;
import com.zeroflow.utils.LogEvent;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: richard.chen
 * @version: v1.0
 * @description:基础重试调用器
 * @date:2019/4/11
 */
@Slf4j
public class RetryInvoke{
    private EnhanceLogger elog = EnhanceLogger.of(log);
    //流程执行器
    private Class<? extends BaseFlowHandler> flowHandler;
    //流程日志管理器
    private BaseFlowLogHandler flowLogHandler;

    /**
     * @param flowHandlerClz    流程对应的管理器
     * @param flowLogHandlerClz 流程对应的日志管理器
     */
    public RetryInvoke(Class<? extends BaseFlowHandler> flowHandlerClz, Class<? extends BaseFlowLogHandler> flowLogHandlerClz) {
        this.flowHandler = flowHandlerClz;
        try {
            this.flowLogHandler = (BaseFlowLogHandler) flowLogHandlerClz.newInstance();
        } catch (Exception ex) {
            elog.error(LogEvent.of("RetryInvoke-RetryInvoke", "初始化FlowLogHandler异常", ex)
            );
        }
    }

    /**
     * 执行重试
     *
     * @throws Exception
     */
    public void invoke() throws Exception {
        List<ErrorLog> errorLogList = flowLogHandler.getErrorLogList();
        elog.info(LogEvent.of("BaseRetryInvoke-invoke-Info", "执行重试流程")
                .others("错误日志条数", errorLogList.size())
        );
        for (ErrorLog errorLog : errorLogList) {
            List<String> commandRecord = restoreCommandRecord(errorLog);
            //获取泛型对应的值
            BaseFlowHandler handle = flowHandler.newInstance();
            handle.setContext(restoreContext(errorLog)).setFlowLogHandler(flowLogHandler).setRetryParam(commandRecord,errorLog);
            handle.invoke();
        }
    }

    /**
     * 解释上下文为对应对象
     *
     * @param log
     * @return
     */
    protected <T> T  restoreContext(ErrorLog log) {
        Class<T> clazz = GenericsUtils.getSuperClassGenricType(flowHandler, 0);
        T context = JSON.parseObject(log.getContext(), clazz);
        return context;
    }

    /**
     * 解释命令列表为JAVA对象
     *
     * @param log
     * @return
     */
    protected List<String> restoreCommandRecord(ErrorLog log) {
        ArrayList<String> commandRecord = JSON.parseObject(log.getCommand_record(), new TypeReference<ArrayList<String>>() {
        });
        return commandRecord;
    }

}
