package com.javatest.flowable.common.enums;

/**
 * @author azure
 * 返回状态码和对应的信息
 */
public enum ReturnCode {

    // 成功
    SUCCESS(0,null),

//    /* 参数异常 */
//    PARAM_INVALID(1000,"参数无效"),
//    PARAM_BLANK(1001,"参数为空"),
//    PARAM_TYPEERROR(1002,"参数类型错误"),
//    PARAM_LACK(1003,"参数缺失"),
//    PARAM_TRANSFER_ERROR(1004,"参数转换错误"),
//
//    /* 用户验证异常 */
//    USER_NOT_LOGIN(2000,"用户未登录"),
//    USER_LOGIN_ERROR(2001,"用户登录失败，请检查账号名和密码"),
//    USER_PASS_NOTREPEAT(2002,"两次密码不一致，请重新输入"),
//    USER_NOAUTH(2003,"用户没有访问权限"),
//    USER_NOT_EXIST(2004,"用户不存在"),
//
//    /* 接口异常 */
//    NULL_POINTER(3001,"空指针异常"),
//    FT_EXCEPTION(3002,"文件异常"),
//    Business_EXCEPTION(3100,"自定义异常");

    /**
     * 根据业务场景重新调整异常返回码
     */
    /* 10 表示通用异常，比如校验异常等*/
    PARAM_VALID_EXCEPTION(10001,"参数格式校验失败"),
    NULL_POINTER(10002,"空指针异常"),
    PARAM_NOT_COMPLETE(10003,"参数不完整"),

    /* 11 表示模型模块异常*/
    MODEL_COMMON_ERROR(11000,"流程常规错误"),
    MODEL_ID_NULL(11001,"模板id不能为空"),
    MODEL_DEPLOY_ERROR(11002,"流程部署错误"),
    MODEL_IMPORT_ERROR(11003,"流程文件导入错误"),



    /* 12 表示流程定义模块异常*/


    /* 13 表示流程实例模块异常*/
    INSTANCE_STOP_PROCESS_ERROR(13001,"流程已挂起，请联系管理员激活!"),
    INSTANCE_PROCESS_NOT_FOUND(13002,"不存在运行的流程实例"),
    INSTANCE_PROCESS_ALREADY_HANG_ON(13003,"此流程已经挂起"),
    INSTANCE_ID_NULL(13004,"流程id不能为空"),

    /* 14 表示任务模块异常*/

    /* 其余的自定义异常，这个只是为了方便把验证码添加在上面时不用考虑结束的分号，可以去掉不要，也可以加上含义*/
    MY_EXCEPTION(90000,"自定义异常");

    private final Integer code;
    private final String msg;

    ReturnCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

}
