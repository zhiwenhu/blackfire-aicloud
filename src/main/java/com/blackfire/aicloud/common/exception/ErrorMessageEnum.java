package com.blackfire.aicloud.common.exception;

/**
 * 业务异常信息枚举
 *
 * @Author zhangjx
 * @Date:2020-09-16 16:17:22
 */
public enum ErrorMessageEnum {
    UCENTER_SERVICE_PROVIDER_FAILED(200000, "用户中心服务提供者调用失败！"),
    UCENTER_MQ_EXECUTE_ERROR(200001, "MQ服务调用失败！"),
    UCENTER_TOKEN_HAS_EXPIRED(200002, "token已过期，请重新登录！"),
    UCENTER_TOKEN_NOT_NULL(200003, "token不存在，无权访问！"),
    UCENTER_ACCT_OR_PWD_ERROR(200004, "登录账号或密码不正确！"),
    UCENTER_ACCT_NOT_NULL(200005, "登录账号不能为空！"),
    UCENTER_USER_NOT_EXIST(200006, "用户不存在！"),
    UCENTER_USER_IS_EXIST(200007, "用户已经存在！"),
    UCENTER_MOBILE_HAD_EXIST(200008, "手机号码已存在！"),
    UCENTER_PASSWORD_NOT_NULL(200009, "密码不能为空！"),
    UCENTER_MOBILE_NOT_VALID(200010, "手机号不合法！"),
    UCENTER_UFIELDCONFIG_NOT_EXIST(200011, "用户不存在！"),
    UCENTER_TABLENAME_NOT_NULL(200012, "表名不能为空！"),
    UCENTER_FIELDCOMMENT_NOT_NULL(200013, "字段注释不能为空！"),
    UCENTER_FIELDNAME_NOT_NULL(200014, "字段名称不能为空！"),
    UCENTER_FIELDTYPE_NOT_NULL(200015, "字段类型不能为空！"),
    UCENTER_INPUTWAY_NOT_NULL(200016, "录入方式不能为空！"),

    UCENTER_TENANT_ROLE_NOT_EXIST(200017, "角色不存在！"),
    UCENTER_TENANT_ROLE_HAD_EXIST(200018, "角色已存在！"),
    UCENTER_TENANT_ROLE_NAME_HAD_EXIST(200019, "角色名称已存在！"),
    UCENTER_TENANT_ROLE_SN_HAD_EXIST(200020, "角色编号已存在！"),

    UCENTER_USERNAME_NOT_NULL(200021, "人员名称不能为空！"),
    UCENTER_UPERSONNEL_NOT_EXIST(200022, "人员不存在！"),
    UCENTER_USYSCONFIG_NOT_EXIST(200023, "系统配置不存在！"),
    UCENTER_DEPTSYSNAME_NOT_NULL(200024, "部门名称不能为空！"),
    UCENTER_LOGINACCOUNT_NOT_NULL(200025, "登录账号不能为空！"),

    UCENTER_PERMISSION_NOT_EXIST(200026, "资源信息不存在！"),
    UCENTER_PERMISSION_HAD_EXIST(200027, "资源信息已存在！"),
    UCENTER_PERMISSION_URL_HAD_EXIST(200028, "资源URL已存在！"),
    UCENTER_PERMISSION_CODE_HAD_EXIST(200029, "资源标识已存在！"),
    UCENTER_PERMISSION_NAME_HAD_EXIST(200030, "资源名称已存在！"),
    UCENTER_PERMISSION_EXIST_SUP_PER(200031, "存在子权限资源"),
    UCENTER_PERMISSION_NOT_HAVE(200054, "用户未配置权限或没有该权限！"),

    UCENTER_LOGIN_SYSCONFIG_NOT_EXIST(200032, "请先配置登录方式，再登录！"),
    UCENTER_LOGIN_INPUT_USERNAME_ERROR(200033, "请输入正确的用户名进行登录！"),
    UCENTER_LOGIN_INPUT_MOBILE_ERROR(200034, "请输入正确的手机号进行登录！"),
    UCENTER_LOGIN_INPUT_EMAIL_ERROR(200035, "请输入正确的邮箱进行登录！"),
    UCENTER_LOGIN_INPUT_USERNAME_OR_MOBILE_ERROR(200036, "请输入正确的用户名或手机号进行登录！"),
    UCENTER_LOGIN_INPUT_USERNAME_OR_EMAIL_ERROR(200037, "请输入正确的用户名或邮箱进行登录！"),
    UCENTER_LOGIN_INPUT_MOBILE_OR_EMAIL_ERROR(200038, "请输入正确的手机号或邮箱进行登录！"),
    UCENTER_LOGIN_INPUT_USERNAME_OR_MOBILE_OR_EMAIL_ERROR(200039, "请输入正确的用户名或手机号或邮箱进行登录！"),
    UCENTER_LOGIN_VERIFY_CODE_WRONG(200040, "验证码不正确！"),
    UCENTER_LOGIN_VERIFY_CODE_EXPIRE(200041, "验证码已失效！"),
    UCENTER_LOGIN_ACCOUNT_LOCKED(200047, "账号已锁定！"),
    UCENTER_LOGIN_ACCOUNT_NOTENABLE(200049, "账号已禁用！"),
    UCENTER_LOGIN_PWD_EXPIRE(200048, "密码已过期，请修改密码！"),
    UCENTER_OLDPWD_NOT_SAME_ORIGIN_PWD(200042, "密码修改失败，旧密码与原密码不相同！"),
    UCENTER_USYSCONFIG_EXIST(200043, "系统配置已存在！"),
    UCENTER_EMAIL_HAD_EXIST(200044, "邮箱已存在！"),

    UCENTER_TENANT_IS_EXIST(200045, "租户已经存在！"),
    UCENTER_TENANT_NOT_EXIST(200046, "租户不存在！"),
    UCENTER_TENANT_TAG_NOT_EXIST(200051, "标签不存在！"),
    UCENTER_TENANT_TAG_NOT_YOURS(200052, "该标签不是你的标签！"),
    UCENTER_FINDPWDVERIFYCODE_VERIFY_CODE_WRONG(200053, "验证码不正确！"),
    UCENTER_WXLOGIN_FAILED(200054, "微信认证登录失败！"),
    UCENTER_WX_URL_NOT_NULL(200055, "url不能为空！"),
    UCENTER_WX_GETTICKET_FAILED(200056, "获取微信签名失败！"),
    UCENTER_PARAM_NOT_GOOD(200057, "参数不对！"),
    ;

    private static final String EMPTY = "";

    private int errCode; //错误代码

    private String errMsg; //错误信息

    /**
     * * Get message by code
     * * @param code
     *
     * @return name
     */
    public static String getText(int code) {
        for (ErrorMessageEnum em : ErrorMessageEnum.values()) {
            if (em.errCode == code) {
                return em.errMsg;
            }
        }
        return EMPTY;
    }

    ErrorMessageEnum(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
