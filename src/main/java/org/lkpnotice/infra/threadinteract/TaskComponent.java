package org.lkpnotice.infra.threadinteract;

import java.util.List;

/**
 * Created by jpliu on 2019/11/1.
 */
public abstract class TaskComponent {

    public abstract void process(List<TaskData> taskDataList);
}
