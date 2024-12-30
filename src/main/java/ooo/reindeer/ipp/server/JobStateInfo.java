package ooo.reindeer.ipp.server;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.model.JobState;
import com.hp.jipp.model.Types;

public class JobStateInfo {
    // 任务状态属性
    Attribute<?> jobState;
    // 任务状态原因属性
    Attribute<?> jobStateReason;

    /**
     * 构造函数，用于初始化任务状态信息。
     *
     * @param jobState        任务状态，类型为 {@link com.hp.jipp.model.JobState}/>
     * @param jobStateReason  任务状态描述，新丁使用 {@link com.hp.jipp.model.JobStateReason} 中的值，默认使用 none/>
     */
    public JobStateInfo(JobState jobState, String jobStateReason) {
        // 初始化任务状态属性
        this.jobState = Types.jobState.of(jobState);
        // 初始化任务状态原因属性
        this.jobStateReason = Types.jobStateReasons.of(jobStateReason);
    }

    /**
     * 获取任务状态属性。
     *
     * @return 任务状态属性
     */
    public Attribute<?> getJobState() {
        return jobState;
    }

    /**
     * 获取任务状态原因属性。
     *
     * @return 任务状态原因属性
     */
    public Attribute<?> getJobStateReason() {
        return jobStateReason;
    }
}

