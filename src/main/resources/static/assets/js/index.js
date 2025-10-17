//add by owen 修复 path 无法引用http://页面的问题 begin
String.prototype.startWith = function(str) {
	if (str == null || str == "" || this.length == 0
			|| str.length > this.length)
		return false;
	if (this.substr(0, str.length) == str)
		return true;
	else
		return false;
	return true;
}


function initIFrame() {
    var $parent = $(".admin-iframe").parent();
    if ($parent.hasClass('layui-body')) {
        $parent.addClass('admin-iframe-body');
        return;
    }
    if ($parent.hasClass('layui-tab-item')) {
        $parent.css({'padding': '0', 'overflow-y': 'hidden'});
    } else {
        $parent.css({'width': '100%', 'height': '100%'});
    }
}
//add by owen 修复 path 无法引用http://页面的问题 end

layui.define(['myConfig', 'admin', 'layer', 'element'], function (exports) {
    var $ = layui.$;
    var config = layui.myConfig;
    var admin = layui.admin;
    var layer = layui.layer;
    
    var index = {
        // 从服务器获取登录用户的信息
        getUser: function (success) {
            layer.load(2);
            admin.req('api-user/users/current', {}, function (data) {
                layer.closeAll('loading');
                if (data && data.statusCodeValue === 0) {
                    let user = data.data;
                    config.putUser(user);
                    admin.putTempData("permissions",user.permissions);
                    success(user);
                } else {
                    layer.msg('获取用户失败', {icon: 2});
                    //add by owen 登录失败重定向到登录页
                    config.removeToken();
                    location.replace('index.html');
                }
            }, 'GET');
        },
        //获取菜单
        getMenus: function () {
            admin.req('api-user/menus/current', {}, function (data) {
                admin.putTempData("menus",data.data);
            }, 'GET');
        },
        // 页面元素绑定事件监听
        bindEvent: function () {
			// debugger;
            // 退出登录
            $('#btnLogout').click(function () {
                layer.confirm('确定退出登录？', function () {
					debugger;
                    //通过认证中心 tuic
                    admin.req('api-auth/oauth/remove/token?token='+config.getToken().access_token , {}, function (data) {
                            config.removeToken();
                            location.replace('index.html');
                    }, 'POST');
                });
            });
            // 修改密码
            $('#setPsw').click(function () {
               // admin.popupRight('pages/password.html');
                admin.popupCenter({
                    title: '修改密码',
                    path: 'pages/password.html'
                });
            });
            // 个人信息
            $('#setInfo').click(function () {
                //Q.go('myInfo');
                admin.popupCenter({
                    title: '个人信息',
                    path: 'pages/myInfo.html'
                });
            });
            $("#questionText").bind('keydown', function (e) {
                let keyCode = e.keyCode || e.which || e.code || e.charCode;
                if (keyCode == 13) {
                    $('#sendButton').click();
                }
            });
        }
    };

    // 自动登录及token拦截
    if (!config.getToken() || config.getToken() == '') { //token为空
        if (! (location.href.substring(location.href.lastIndexOf("/") + 1) == 'index.html')) {
            admin.putTempData(config.CAS_LOGIN_PARAMS, ''); //清空
            let href = location.href;
            if (href.indexOf("?") != -1) {
                let params = href.substring(href.indexOf('?') + 1);
                if (params.indexOf('txt1') != -1) {
                    admin.putTempData(config.CAS_LOGIN_PARAMS, params); //暂存参数
                }
            }
            top.location.replace('index.html');
            return;
        }
    } else {
        let href = location.href;
        if (/\\index.html\?txt/.test(href)) {
            config.putToken('');
            let params = href.substring(href.indexOf('?') + 1);
            admin.putTempData(config.CAS_LOGIN_PARAMS, params); //暂存参数
            top.location.replace('index.html');
            return;
        }
    }

    exports('index', index);
});
