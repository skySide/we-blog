# WeBlog项目

## 项目准备

在将项目环境搭建好之后，我们需要明确实体需要有哪些（下面已经画出来了):

各个实体之间的关系为:

- 一对一: 博客blog和分类category()
- 一对多: 评论comment和回复comment   
- 多对多: 博客blog和标签tag         

需要创建数据库:

![image-20230205215022042](C:\Users\MACHENIKE\AppData\Roaming\Typora\typora-user-images\image-20230205215022042.png)

对应的建表语句为:

```sql
create table if not exists tb_blog (
id int primary key auto_increment,
title varchar(256) not null,
blog_description mediumtext not null,
views bigint default 0,
likes bigint default 0,
content mediumtext,
blog_status int default 0, -- blog是否已经发布 
enable_comment int default 1, -- 是否开启了评论
user_id int  not null,
category_id,
create_time timestamp default current_timestamp,
update_time timestamp default current_timestamp
);

create table if not exists tb_comment (
id int primary key auto_increment,
user_id int not null,
blog_id int not null,
likes int default 0,
comment_body mediumtext,
comment_status int default 0,
comment_type int,
create_time timestamp default current_timestamp,
update_time timestamp default current_timestamp
);


create table if not exists tb_blog_tag(
id int primary key auto_increment,
blog_id int not null,
tag_id int not null
);

create table if not exists tb_tag(
id int primary key auto_increment,
name varchar(64)
);

create table if not exists tb_category(
id int primary key auto_increment,
name varchar(64),
blog_id int,
);

create table if not exists tb_user (
id int primary key auto_increment,
icon varchar(256) default '/admin/dist/img/user/user_0.png',
nickname varchar(256) not null,
password varchar(256) not null,
salt varchar(12) default '123456' -- 盐值，用于密码的加密和解密
);


create table if not exists tb_comment_reply (
id int primary key auto_increment,
comment_id int not null,
reply_id int not null
);
```

## 后台

### 用户登录/注册

![image-20230205223646646](C:\Users\MACHENIKE\AppData\Roaming\Typora\typora-user-images\image-20230205223646646.png)

值得注意的是，在前台将数据发送给后台的时候，password已经经过了1次MD5加密，但是写入到数据库中的密码还需要再经过1次MD5加密，所以我们再查找用户的时候，还需要对password进行1次MD5加密才可以进行查找用户的操作。

对应的代码为:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>personal blog | Log in</title>
    <!-- Tell the browser to be responsive to screen width -->
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="shortcut icon" th:href="@{/admin/dist/img/favicon.png}"/>
    <!-- Font Awesome -->
    <link rel="stylesheet" th:href="@{/admin/dist/css/font-awesome.min.css}">
    <!-- Ionicons -->
    <link rel="stylesheet" th:href="@{/admin/dist/css/ionicons.min.css}">
    <!-- Theme style -->
    <link rel="stylesheet" th:href="@{/admin/dist/css/adminlte.min.css}">
    <!-- jQuery -->
    <script th:src="@{/admin/plugins/jquery/jquery.min.js}"></script>
    <!-- Bootstrap 4 -->
    <script th:src="@{/admin/plugins/bootstrap/js/bootstrap.bundle.min.js}"></script>
    <script th:src="@{/admin/dist/js/plugins/particles.js}"></script>
    <script th:src="@{/admin/dist/js/plugins/login-bg-particles.js}"></script>
    <!-- jquery-validator -->
    <script type="text/javascript" th:src="@{/admin/dist/jquery-validation/jquery.validate.min.js}"></script>
    <script type="text/javascript" th:src="@{/admin/dist/jquery-validation/localization/messages_zh.min.js}"></script>
    <!-- md5.js -->
    <script type="text/javascript" th:src="@{/admin/dist/js/md5.min.js}"></script>
    <!-- common.js -->
    <script type="text/javascript" th:src="@{/admin/dist/js/common.js}"></script>
    <!-- layer -->
    <script type="text/javascript" th:src="@{/admin/dist/layer/layer.js}"></script>
    <style>
        canvas {
            display: block;
            vertical-align: bottom;
        }
        #particles {
            background-color: #F7FAFC;
            position: absolute;
            top: 0;
            width: 100%;
            height: 100%;
            z-index: -1;
        }
    </style>
</head>
<body class="hold-transition login-page">
<div id="particles">
</div>
<div class="login-box">
    <div class="login-logo" style="color: #007bff;">
        <h1>personal blog</h1>
    </div>
    <!-- /.login-logo -->
    <div class="card">
        <div class="card-body login-card-body">
            <form id="loginForm" name="loginForm" method="post">
                <div class="form-group has-feedback">
                    <input type="text" id="username" name="username" class="form-control" placeholder="请输入用户名"
                           required="true">
                </div>
                <div class="form-group has-feedback">
                    <input type="password" id="password" name="password" class="form-control" placeholder="请输入密码"
                           required="true" minlength="6" maxlength="12">
                </div>
                <div class="row">
                    <div class="col-6">
                        <input type="text" class="form-control" id="verifyCode" name="verifyCode" placeholder="请输入验证码" required="true">
                    </div>
                    <div class="col-6">
                        <img alt="单击图片刷新！" class="pointer" th:src="@{/common/kaptcha}"
                             onclick="this.src='/common/kaptcha?d='+new Date()*1">
                    </div>
                </div>
                <div class="form-group has-feedback"></div>
                <div class="row">
                    <div class="col-12">
                        <button class="btn btn-primary btn-block btn-flat" onclick="login()">登录/注册</button>
                    </div>
                </div>
            </form>

        </div>
        <!-- /.login-card-body -->
    </div>
</div>
<!-- /.login-box -->
<script>
    function login() {
        $("#loginForm").validate({
            submitHandler: function (form) {
                doLogin();
            }
        });
    }
    function doLogin() {
        //加载中
        g_showLoading();
        var inputPass = $("#password").val();
        var salt = g_passsword_salt;
        //将盐值中的特定字符和表单中的password进行拼接，然后进行md5加密
        var str = "" + salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        var password = md5(str);
        $.ajax({
            //点击提交按钮之后，发送请求，来到了/user/login中
            url: "/user/login",
            type: "POST",
            data: {
                //传递的参数
                username: $("#username").val(),
                password: password,
                verifyCode: $("#verifyCode").val()
            },
            //这时候返回的数据data就是上面的doLogin的返回值Result
            //所以对应的code,msg就是RespBean中的属性值
            //而success的意思是能够成功执行了方法doLogin,并且有返回值
            //所以在success方法的内部中需要判断它的状态码
            success: function (data) {
                if (data.code == 200) {
                    layer.close();
                    sessionStorage.setItem("token", data.obj);
                    //登录完毕之后，发送admin/请求，来到后台首页
                    location.href = "/admin/";
                } else {
                    layer.msg(data.msg);
                }
            },
            error: function () {
                //执行doLogin方法抛出异常的时候
                layer.msg("后台服务出现异常");
            }
        });
    }
</script>
</body>
</html>

```

CommonController生成验证码的方法:

```java
@Controller
public class CommonController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/common/kaptcha")
    public void defaultKaptcha(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        httpServletResponse.setHeader("Cache-Control", "no-store");
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);
        httpServletResponse.setContentType("image/png");

        ShearCaptcha shearCaptcha= CaptchaUtil.createShearCaptcha(150, 30, 4, 2);
        String verifyCode = shearCaptcha.getCode();
        /*
        验证码存入session中,不保存到redis中，因为key不可以保证唯一性
        此时在并发的情况下，就会导致验证码覆盖
        */
        httpServletRequest.getSession().setAttribute(SystemConstants.LOGIN_VERIFYCODE, verifyCode);

        // 输出图片流
        shearCaptcha.write(httpServletResponse.getOutputStream());
    }
}
```

LoginController执行登录操作的代码为:

```java
@PostMapping("/login")
@ResponseBody
public Result doLogin(LoginVo loginVo, HttpSession session){
    String username = loginVo.getUsername();
    String password = loginVo.getPassword();
    String inputVerify = loginVo.getVerifyCode();
    String realVerify = (String)session.getAttribute(SystemConstants.LOGIN_VERIFYCODE);
    if(!inputVerify.equals(realVerify)){
        return Result.fail(ResultBean.LOGIN_VERIFY_ERROR, null);
    }
    User user = userService.getUserByUsernameAndPassword(username, password);
    if(user == null){
        //用户不存在，执行注册操作
        user = new User();
        String dbPass = MD5Utils.formPassToDbPass(password);
        user.setNickname(username);
        user.setPassword(dbPass);
        userService.save(user);
    }
    UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
    //将用户保存到session，之后就可以从session中取出用户
    session.setAttribute("user", userDTO);
    return Result.success();
}
```

AdminController的代码为:

```java
@GetMapping({"/", "/index"})
    public ModelAndView index(HttpSession session, ModelAndView modelAndView){
        //获取当前登录用户总的blog数目
        List<Blog> blogs = blogService.list();
        modelAndView.addObject("blogCount", blogs.size());
        //获取当前登录用户总的评论数
        int commentCount = 0;
        for(Blog blog : blogs){
            //获取这篇blog的评论数(没有回复的评论 + 回复)
            commentCount += commentService.count(new LambdaQueryWrapper<Comment>().eq(Comment::getBlogId, blog.getId()));
        }
        modelAndView.addObject("commentCount", commentCount);
        //获取文章分类总数
        modelAndView.addObject("categoryCount", categoryService.count());
        //获取标签的总数
        modelAndView.addObject("tagCount", tagService.count());
        modelAndView.setViewName("/admin/index");
        return modelAndView;
    }
```

登录成功之后，就会来到后台界面,在后台界面中可以看到如下信息:

![image-20230205223906826](C:\Users\MACHENIKE\AppData\Roaming\Typora\typora-user-images\image-20230205223906826.png)

### 博客管理

博客管理的界面如下所示，主要包括的是新增博客，编辑博客，删除博客等管理。

![image-20230205230228342](C:\Users\MACHENIKE\AppData\Roaming\Typora\typora-user-images\image-20230205230228342.png)

注意的是，当我们点击了博客管理，或者总文章数的时候，就会来到/admin/blog.html的界面，这时候对于数据的操作主要是在blog.js中，可以通过查看blog.js，从而知道相应操作的url。

这里需要着重提一下的是，在blog.js中的下面代码:

```js
$("#jqGrid").jqGrid({
        url: '/admin/blog/list',
        datatype: "json",
        colModel: [
            {label: 'id', name: 'id', index: 'id', width: 10, key: true, hidden: true},
            {label: '标题', name: 'title', index: 'title', width: 80},
            {label: '浏览', name: 'views', index: 'views', width: 30},
            {label: '点赞', name: 'likes', index: 'likes', width: 30},
            {label: '分类', name: 'categoryName', index: 'categoryName', width: 30},
            {label: '状态', name: 'blogStatus', index: 'blogStatus', width: 100, formatter: blogStatusFormatter},
            {label: '创作时间', name: 'createTime', index: 'createTime', width: 80, formatter: dateFormatter},
            {label: '编辑时间', name: 'updateTime', index: 'updateTime', width: 80, formatter: dateFormatter}
        ],
        height: 700,
        rowNum: 10,
        rowList: [10, 20, 50],
        styleUI: 'Bootstrap',
        loadtext: '信息读取中...',
        rownumbers: false,
        rownumWidth: 20,
        autowidth: true,
        multiselect: true,
        pager: "#jqGridPager",
        jsonReader: {
            root: "obj.list",
            page: "obj.currentPage",
            total: "obj.pages",
            records: "obj.total"
        },
        prmNames: {
            page: "currentPage",
            rows: "pageSize",
            order: "order",
        },
```

是用来构造表格的，但是首先需要发送请求`/admin/blog/list`,同时会携带参数`prmNames,参数的名字就是currentPage,pageSize,order`，其实携带的参数远不止这些，可以点击F12看清发送的请求就可以知道的，而最后返回来的结果是一个Result实体数据，在这个实体中，除了有状态码code,状态信息msg,还包括了相应数据obj。这时候我们返回来的相应数据PageResult<Blog\>,具体到时候可以查看这个PageResult类的成员。之后通过jsonRead来读取obj中的数据。

而**当进行新增博客，编辑博客，删除博客的时候，执行完毕之后，就会重新执行上面的代码进行渲染。而对于搜索功能中，则虽然是直接利用上面的代码，但是它多发送了一个参数`keyword`,这样就可以根据标题或者分类来找到keyword对应的blog了**。 

下面的操作中也是根据类似的道理操作的。

### 评论管理

![image-20230205231437655](C:\Users\MACHENIKE\AppData\Roaming\Typora\typora-user-images\image-20230205231437655.png)

### 标签管理

![image-20230205231419449](C:\Users\MACHENIKE\AppData\Roaming\Typora\typora-user-images\image-20230205231419449.png)

### 分类管理

![image-20230205231357398](C:\Users\MACHENIKE\AppData\Roaming\Typora\typora-user-images\image-20230205231357398.png)



其余的操作暂时还没有实现，后续可能会继续进行完善，例如友情链接，系统配置，但是已经实现了退出，只需要让这个session失效即可。

## 前台

### 查看blog

![image-20230120155420436](C:\Users\MACHENIKE\AppData\Roaming\Typora\typora-user-images\image-20230120155420436.png)

对应的BlogController中的代码为:

```java
@GetMapping({"/page","/"})
public ModelAndView page(@RequestParam(value="currentPage", defaultValue = "1", required = false)Integer currentPage,
                         @RequestParam(value="pageSize", defaultValue = "8", required = false)Integer size,
                         ModelAndView modelAndView){
    IPage<Blog> page = new Page<Blog>(currentPage, size);
    //获取所有已经发布了的blog,并且根据发布的时间降序排序
    blogService.page(page,new LambdaQueryWrapper<Blog>()
                     .eq(Blog::getBlogStatus, 1)
                     .orderByDesc(Blog::getCreateTime));
    List<Blog> blogs = page.getRecords();
    blogs.forEach(blog ->{
        //对于每一篇blog，需要设置作者的名字
        findUserByBlog(blog);
        findCategoryByBlog(blog);
    });
    modelAndView.addObject("blogs", blogs);
    modelAndView.addObject("currentPage", currentPage);
    modelAndView.addObject("pages", page.getPages());//页数
    //获取所有的标签
    modelAndView.addObject("tags", tagService.list());
    //获取最新发布的blog
    modelAndView.addObject("newBlogs",blogService.getNewBlogs());
    //获取点击最多的blog
    modelAndView.addObject("hotBlogs", blogService.getHotBlogs());
    modelAndView.setViewName("/blog/amaze/index.html");
    return modelAndView;
}
```

当在搜索框中输入`/blog/page`,就会来到blog/amaze/index.html，效果如下所示:

![image-20230120202937667](C:\Users\MACHENIKE\AppData\Roaming\Typora\typora-user-images\image-20230120202937667.png)

### 根据category查看blog

点击某一个分类，例如`admin in 日常随笔`中的日常随笔，就会发送请求`/blog/category?categoryId=xxx`,然后获取数据，在回到`/blag/amaze/list.html`界面.对应的流程为:

![image-20230120203844740](C:\Users\MACHENIKE\AppData\Roaming\Typora\typora-user-images\image-20230120203844740.png)

对应的代码为:

```java
@GetMapping("/category")
public ModelAndView getBlogByCategoryId(@RequestParam(value = "categoryId", defaultValue = "1", required = false)Integer categoryId,
                                        @RequestParam(value="currentPage", defaultValue = "1", required = false)Integer currentPage,
                                        @RequestParam(value="pageSize", defaultValue = "4", required= false)Integer pageSize,
                                        ModelAndView modelAndView){
    IPage<Blog> page = new Page<>(currentPage, pageSize);
    blogService.page(page, new LambdaQueryWrapper<Blog>().eq(Blog::getCategoryId, categoryId));
    List<Blog> blogs = page.getRecords();
    blogs.forEach(blog -> {
        findUserByBlog(blog);
        findCategoryByBlog(blog);
    });
    modelAndView.addObject("blogs", blogs);
    modelAndView.addObject("currentPage", currentPage);
    modelAndView.addObject("pages", page.getPages());
    //获取所有的标签
    modelAndView.addObject("tags", tagService.list());
    //获取所有的最新发布的blog
    modelAndView.addObject("newBlogs", blogService.getNewBlogs());
    //获取浏览最多的blog
    modelAndView.addObject("hotBlogs", blogService.getHotBlogs());
    modelAndView.setViewName("/blog/amaze/list.html");
    //获取请求的url,因为在分页列表中需要通过这个url来进行查询的
    String url = "/blog/category?categoryId=" + categoryId;
    modelAndView.addObject("url", url);
    return modelAndView;
}
```

### 根据标签查看blog

点击右侧栏中某一个热门标签，就会发送请求`/blog/tag?tagId=xxx`,然后就会执行相应的代码，将数据返回到`/blog/amaze/list.html`进行渲染，对应的流程为:

![image-20230120204500701](C:\Users\MACHENIKE\AppData\Roaming\Typora\typora-user-images\image-20230120204500701.png)

对应的代码为:

```java
@GetMapping("/tag")
public ModelAndView getBlogByTagId(@RequestParam(value = "tagId", defaultValue = "1", required = false)Integer tagId,
                                   @RequestParam(value="currentPage", defaultValue = "1", required = false)Integer currentPage,
                                   @RequestParam(value="pageSize", defaultValue = "4", required= false)Integer pageSize,
                                   ModelAndView modelAndView){
    List<Blog> blogs = blogService.getBlogsByTagId(tagId, (currentPage - 1) * pageSize, pageSize);
    blogs.forEach(blog -> {
        findCategoryByBlog(blog);
        findUserByBlog(blog);
    });
    modelAndView.addObject("blogs", blogs);
    modelAndView.addObject("currentPage", currentPage);
    //获取这个标签一共有多少页记录
    modelAndView.addObject("pages", tagService.getPages(tagId, pageSize));
    //获取所有的标签
    modelAndView.addObject("tags", tagService.list());
    //获取所有的最新发布的blog
    modelAndView.addObject("newBlogs", blogService.getNewBlogs());
    //获取浏览最多的blog
    modelAndView.addObject("hotBlogs", blogService.getHotBlogs());
    modelAndView.setViewName("/blog/amaze/list.html");
    //获取请求的url
    String url = "/blog/tag?tagId=" + tagId;
    modelAndView.addObject("url", url);
    return modelAndView;

}
```

### 查看某篇blog详情

![image-20230123214648628](C:\Users\MACHENIKE\AppData\Roaming\Typora\typora-user-images\image-20230123214648628.png)

```java
@GetMapping("/detail")
public ModelAndView detail(@RequestParam(name="id", defaultValue = "1", required = false)Integer id,
                           @RequestParam(name = "currentPage", defaultValue = "1", required = false)Integer currentPage,
                           @RequestParam(name = "pageSize", defaultValue = "4", required = false)Integer pageSize,
                           ModelAndView modelAndView){
    blogService.update().setSql("views = views + 1").eq("id", id).update();
    Blog blog = blogService.getOne(new LambdaQueryWrapper<Blog>().eq(Blog::getId, id));
    findUserByBlog(blog);
    findCategoryByBlog(blog);
    //1、获取这个blog的所有标签
    List<Tag> tags = tagService.getTagsByBlogId(id);
    blog.setTags(tags);
    modelAndView.addObject("blog", blog);
    //2、获取这个blog的所有评论以及总的评论数
    //2.1 先获取所有已经审核了的一级评论
    IPage<Comment> page = new Page<>(currentPage, pageSize);
    commentService.page(page, new LambdaQueryWrapper<Comment>().eq(Comment::getBlogId, id)
                        .eq(Comment::getCommentType, CommentConstants.FIRST_COMMENT_TYPE)
                        .eq(Comment::getCommentStatus, CommentConstants.CHECK_DONE)
                        .orderByDesc(Comment::getCreateTime));
    List<Comment> comments = page.getRecords();
    //2.2 获取已经审核了的二级评论(也即一级评论的回复)
    comments.forEach(comment -> {
        //2.2.1 获取发布一级评论的用户
        findUserByComment(comment);
        //2.2.2 获取当前这一条评论的回复
        List<Comment> replies = commentService.getRepliesByCommentId(comment.getId());
        log.info("replies = " + replies);
        //设置每一条reply的用户
        replies.forEach(reply -> {
            findUserByComment(reply);
        });
        comment.setReplies(replies);
    });
    System.out.println(comments);
    //获取评论页数
    modelAndView.addObject("commentTotal", page.getTotal());
    modelAndView.addObject("pages", page.getPages());
    modelAndView.addObject("currentPage", currentPage);
    modelAndView.addObject("comments", comments);
    //获取所有的标签
    modelAndView.addObject("tags", tagService.list());
    //获取所有的最新发布blog
    modelAndView.addObject("newBlogs", blogService.getNewBlogs());
    //获取点击最多的blog
    modelAndView.addObject("hotBlogs", blogService.getHotBlogs());
    //设置url
    String url = "/blog/detail?id=" + id;
    modelAndView.addObject("url", url);
    modelAndView.setViewName("/blog/amaze/detail.html");
    return modelAndView;
}
```



### 发布评论

```java

@PostMapping("/comment")
@Transactional
@ResponseBody
public Result comment(@RequestParam("id")Integer blogId,
                      @RequestParam("verifyCode")String verifyCode,
                      @RequestParam("commentator")String commentator,
                      @RequestParam("commentBody")String commentBody,
                      HttpSession session){
    //获取验证码,判断验证码是否一致
    String realVerifyCode = (String)session.getAttribute(SystemConstants.LOGIN_VERIFYCODE);
    if(!verifyCode.equals(realVerifyCode)){
        return Result.fail(ResultBean.LOGIN_VERIFY_ERROR, null);
    }
    //获取当前评论的用户id
    User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getNickname, commentator));
    if(user == null){
        return Result.fail(ResultBean.USER_NOT_EXIST, null);
    }
    //创建新的评论
    Comment comment = new Comment();
    comment.setBlogId(blogId);
    comment.setCommentBody(commentBody);
    comment.setCreateTime(new Date());
    comment.setUserId(user.getId());
    comment.setCommentType(CommentConstants.FIRST_COMMENT_TYPE);
    commentService.save(comment);
    return Result.success();

}
```

注意的是，这里**发布评论的时候要求用户是已经存在数据库表tb_user中的，如果需求是任意的游客都可以发布评论的话，那么需要重新设置tb_comment表，将发表评论的用户id直接修改为用户的名字即可**。

### 根据关键词keyword进行模糊查找

在本次的项目中，根据keyword进行模糊查找，只要title或者contend中包含了keyword，那么就将该blog返回即可。**后续如果需要进行改进的话，这里可以利用Elasticsearch来进行搜索**。







