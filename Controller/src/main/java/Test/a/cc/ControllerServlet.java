package Test.a.cc;


import Test.a.BumenDy;
import Test.a.EmpDy;
import Test.a.d.Emp;
import Test.a.d.page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * 编写者: 唐国军
 * 创建日期: 2022/10/5 19:21
 */
@Controller
public class ControllerServlet {

  //我就是我
    @Autowired
    private EmpDy empDy;
    @Autowired
    private BumenDy bumenDy;


    //修改给性别和部门赋值
    @RequestMapping(value = "/emp/{id}", method = RequestMethod.GET)
    public ModelAndView input(@PathVariable("id") Integer id, Map<String, Object> map) throws Exception{

        Map<String, String> genders = new HashMap<String, String>();
        genders.put("1", "男");
        genders.put("0", "女");
        ModelAndView modelAndView=new ModelAndView("xz");
        modelAndView.addObject("genders",genders);
        modelAndView.addObject("bmsj", bumenDy.querybumen());
        modelAndView.addObject("emp",empDy.queryid(id));


        return modelAndView;
    }



    //修改
    @RequestMapping(value = "/emp", method = RequestMethod.PUT)
    public String update(@Valid Emp emp, BindingResult bindingResult, Map<String, Object> map, @RequestParam("file") MultipartFile file) {

        //获取错误数量
        if (bindingResult.getErrorCount() > 0) {
            System.out.println("出错了");

            for (FieldError error : bindingResult.getFieldErrors()) {
                System.out.println(error.getField() + ":" + error.getDefaultMessage());
            }

            Map<String, String> genders = new HashMap<String, String>();
            genders.put("1", "男");
            genders.put("0", "女");
            //genders这个正好是存在map里面是有key和value的
            //把性别放到map里面map就是request里面
            map.put("genders", genders);

            //bumenDao.getbumens()这个是查询出来的集合 Collection
            //调用查询方法把查询出的所有部门全放到map里面map就是request
            map.put("bmsj", bumenDy.querybumen());
            //不需要这个直接把上面的方法参数Emp emp用就行
            //map.put("emps",emp);
            return "xz";
        }

        Emp  emp1= (Emp) empDy.queryid(emp.getId());
        System.out.println(emp1.getTouxiang()+"   emp1.getTouxiang();");

        //如果修改的id的信息没有图片路径就会进入这里用时间戳做图片名//如果有图片路径和图片名就用原来对应id查询出来信息的路径和图片名
        String path =emp1.getTouxiang();
        if (path ==null ||  path.equals(""))
        {
            //选择图片的路径
            String  filepath=file.getOriginalFilename();
            String filepaths[]=filepath.split("\\.");

            //System.currentTimeMillis时间戳防止出现同一个名字的文件就会覆盖  但是同一时间就可能会出现就会覆盖
            long shijinachuo=0;
            //这里用了同步块 防止出现同一个名字的文件就会覆盖  但是同一时间就可能会出现就会覆盖
            synchronized ("a"){
                //System.currentTimeMillis时间戳防止出现同一个名字的文件就会覆盖  但是同一时间就可能会出现就会覆盖
                shijinachuo=System.currentTimeMillis();
            }
            System.out.println("修改用时间戳—————————");
            String aa=shijinachuo+"."+filepaths[filepaths.length-1];
            //如果修改的id的信息没有图片路径就会进入这里用时间戳做图片名
            path="C:\\Users\\aa\\Desktop\\head\\"+aa;//下载的路径

        }
        try {
            InputStream inputStream=file.getInputStream();// 获取到文件上传的流
            OutputStream outputStream = new FileOutputStream(path);
            byte[] bytes=new byte[1024];
            System.out.println("修改用原来的名字——————————");
            int length=0;
            while ((length=inputStream.read(bytes)) !=-1)
            {
                outputStream.write(bytes,0,length);
                //刷新
                outputStream.flush();
            }
            //关闭
            inputStream.close();
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        //新增把头像图片新增的路径给emp对象的字段
        emp.setTouxiang(path);

        //一起添加到map里面本来是数据库
        empDy.updateemp(emp);
        return "redirect:/query";

    }




    //查询
    @RequestMapping(value = "/query",method = RequestMethod.GET)
    //ModelAndView有数据有视图   //locale就是浏览器的语言类型
    public ModelAndView query(Locale locale, Integer pageNO, Integer pageSize) {
        page<Emp> page=new page<Emp>();
        if (pageNO==null || pageSize==0)
        {
            page.setPageNo(1);
        }
        else
        {
            page.setPageNo(pageNO);
        }
        if (pageSize==null || pageSize==0)
        {
            page.setPageSize(2);
        }
        else
        {
            page.setPageSize(pageSize);
        }

        ModelAndView modelAndView = new ModelAndView("List");
        empDy.getall(page);
        System.out.println(page.getPageNo());
        System.out.println(page.getSum());
        System.out.println(page.getPageSize());

        modelAndView.addObject("page",page);

        modelAndView.addObject("locale", locale.toString());

        return modelAndView;
    }




    //图片显示
    @RequestMapping("/aa")
    public void xiazaiship(HttpServletResponse response, String Path) throws IOException {

        if(Path !=null && !Path.equals("")) {
            //分割符  \\是转意符 \\分割条件
            String[] fileName = Path.split("\\\\");
            //addHeader和setHeader效果一样
            response.addHeader("Content-DIsposition", "attachment;filename="+fileName[fileName.length-1]);
            InputStream inputStream = new FileInputStream(Path);

            byte[] bytes = new byte[1024 * 1024 * 10];
            int length = 0;
            //length是文件的大小和长度
            while ((length = inputStream.read(bytes)) != -1) {
                //bytes整个数组 0是从哪里开始 length是一次读多少
                response.getOutputStream().write(bytes, 0, length);
            }
            inputStream.close();
        }
    }

    //新增设置单选框和下拉框的值
    @RequestMapping(value = "/emp", method = RequestMethod.GET)
    public String add(Map<String, Object> map) {
        Map<String, String> genders = new HashMap<String, String>();
        genders.put("1", "男");
        genders.put("0", "女");
        //genders这个正好是存在map里面是有key和value的
        //把性别放到map里面map就是request里面
        map.put("genders", genders);
        System.out.println("新增get");

        //bumenDao.getbumens()这个是查询出来的集合 Collection
        //调用查询方法把查询出的所有部门全放到map里面map就是request
        map.put("bmsj", bumenDy.querybumen());
        // 这里我改成了emp所以 jsp的form表单modelAttribute就要改成一样的 modelAttribute="emp"
        // 如果你给的是 map.put("command",new Emp());那么jsp就不用加这个modelAttribute
        //new Emp()这个实体类要有一个空的构造方法
        map.put("emp", new Emp());
        return "xz";
    }

    //新增
    @RequestMapping(value = "/emp", method = RequestMethod.POST)
    //BindingResult 有很多子类   Emp emp 要和input界面的modelAttribute="emp"一样也可在下面改参数名//map.put("emps",emp);
    public String save(@Valid Emp emp, BindingResult bindingResult, Map<String, Object> map, @RequestParam("file") MultipartFile file) {
        //int a=1/0;

        System.out.println("新增——————————");
        //获取错误数量
        if (bindingResult.getErrorCount() > 0) {
            System.out.println("出错了");

            for (FieldError error : bindingResult.getFieldErrors()) {
                System.out.println(error.getField() + ":" + error.getDefaultMessage());
            }

            Map<String, String> genders = new HashMap<String, String>();
            genders.put("1", "男");
            genders.put("0", "女");
            //genders这个正好是存在map里面是有key和value的
            //把性别放到map里面map就是request里面
            map.put("genders", genders);
            //bumenDao.getbumens()这个是查询出来的集合 Collection
            //调用查询方法把查询出的所有部门全放到map里面map就是request
            map.put("bmsj", bumenDy.querybumen());
            //不需要这个直接把上面的方法参数Emp emp用就行
            //map.put("emps",emp);
            System.out.println("设置值成功");
            return "xz";
        }

        //文件上传
        //file.getOriginalFilename()是得到上传时的文件名
        String Filename = file.getOriginalFilename();
        //分隔符  \\是转意符
        String[] fenge = Filename.split("\\.");
        //System.currentTimeMillis时间戳防止出现同一个名字的文件就会覆盖  但是同一时间就可能会出现就会覆盖
        long shijinachuo = 0;
        //这里用了同步块 防止出现同一个名字的文件就会覆盖  但是同一时间就可能会出现就会覆盖
        synchronized ("a") {
            //System.currentTimeMillis时间戳防止出现同一个名字的文件就会覆盖  但是同一时间就可能会出现就会覆盖
            shijinachuo = System.currentTimeMillis();
        }
        //这里用了同步块 防止出现同一个名字的文件就会覆盖  但是同一时间就可能会出现就会覆盖
        String houzhui = shijinachuo + "." + fenge[fenge.length - 1];
        //存储路径加随机生成都名字
        String path = "C:\\Users\\aa\\Desktop\\head\\" + houzhui;//下载的路径

        try {
            InputStream inputStream = file.getInputStream();// 获取到文件上传的流
            OutputStream outputStream = new FileOutputStream(path);
            byte[] bytes = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, length);
                //刷新
                outputStream.flush();
            }
            //关闭
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //新增把头像图片新增的路径给emp对象的字段
        emp.setTouxiang(path);

        //一起添加到map里面本来是数据库
        empDy.addusers(emp);

        return "redirect:/query";
    }


    //删除
    @RequestMapping(value = "/emp/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Integer id) {

        Emp  emp =(Emp)empDy.queryid(id);
        System.out.println(emp.getTouxiang()+ " "+"头像");

        String file= emp.getTouxiang();

        if (!file.equals("") && file !=null){

            File file1=new File(file);

            System.out.println(file1+" "+"file1");

            boolean b=file1.delete();
            if (b)
            {
                System.out.println("删除成功");
            }
            else{
                System.out.println("删除失败");
            }
        }

        empDy.deleteid(id);
        return "redirect:/query";
    }



    //每次都会运行把名字补充上
    @ModelAttribute
    public void getEmployee(@RequestParam(value = "id", required = false) Integer id, Map<String, Object> map) {

        if (id != null) {
            map.put("emp", empDy.queryid(id));
        }
    }

}
