package com.javatest.flowable.contoller;

import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: Javen
 * @date:  2021/2/26 14:38
 * @description: 测试一个简单的流程
 */
@RestController
@RequestMapping("flow")
public class TestFlowableController {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;


    @GetMapping("testFlow1")
    public String testFlow1() throws Exception{
//        // 创建流程引擎
//        ProcessEngine processEngine = ProcessEngineConfiguration
//                .createStandaloneProcessEngineConfiguration()
//                .buildProcessEngine();

        // 获取流程服务
//        RepositoryService repositoryService = processEngine.getRepositoryService();
//        RuntimeService runtimeService = processEngine.getRuntimeService();
//        TaskService taskService = processEngine.getTaskService();

        // 部署流程定义（读取本地的bpmn文件）
        repositoryService.createDeployment()
                .addClasspathResource("process/FinancialReportProcess.bpmn20.xml")
                .deploy();

        // 开启一个流程实例（通过bpmn文件的processid来创建流程实例），并获取流程实例id
        String procId = runtimeService.startProcessInstanceByKey("financialReport").getId();

        // 获取第一个流程任务
        // 通过条件查询任务，根据流程定义（bpmn文件）中，第一个任务是由accountancy组执行的，所以可以根据组来获取任务
        // （当然复杂的任务可以用其他的方法精准检索）
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("accountancy").list();
        for (Task task : tasks) {
            System.out.println("接下来的流程任务可由accountancy组的成员处理: " + task.getName());

            // 指派到指定的用户处理
            // 这时候，这个任务就会从在该用户的个人任务列表中，并从其余的组内成员的任务列表中消失
            taskService.claim(task.getId(), "fozzie");
        }

        // 这时候模拟用户fozzie，对任务进行操作
        tasks = taskService.createTaskQuery().taskAssignee("fozzie").list();
        for (Task task : tasks) {
            System.out.println("fozzie的任务: " + task.getName());

            // 完成该任务
            taskService.complete(task.getId());
        }

        System.out.println("fozzie剩下的任务数: " + taskService.createTaskQuery().taskAssignee("fozzie").count());

        // 此时流程将走到第二个流程任务
        tasks = taskService.createTaskQuery().taskCandidateGroup("management").list();
        for (Task task : tasks) {
            System.out.println("接下来的流程任务可由management组的成员处理: " + task.getName());
            // 同样指定用户处理
            taskService.claim(task.getId(), "kermit");
        }

        // 完成第二个流程任务
        for (Task task : tasks) {
            taskService.complete(task.getId());
        }

        // 当所有流程任务都完成时，整个流程即结束

        // 完成后，可以回看整个流程
//        HistoryService historyService = processEngine.getHistoryService();
        HistoricProcessInstance historicProcessInstance =
                historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult();
        System.out.println("流程实例完成时间: " + historicProcessInstance.getEndTime());

        return "ok";
    }
}
