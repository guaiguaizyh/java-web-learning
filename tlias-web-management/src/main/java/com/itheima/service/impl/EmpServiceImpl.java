//package com.itheima.service.impl;
//
//import com.github.pagehelper.Page;
//import com.github.pagehelper.PageHelper;
//import com.itheima.mapper.EmpMapper;
//import com.itheima.pojo.Emp;
//import com.itheima.pojo.EmpQueryParam;
//import com.itheima.pojo.PageResult;
//import com.itheima.service.EmpService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//@Service
//public class EmpServiceImpl implements EmpService{
//
//    //需要调用接口，注入接口
//    @Autowired
//    private EmpMapper empMapper;
//
//    @Override
//    /*public PageResult<Emp> page(Integer page, Integer pageSize, String name
//                                , Integer gender, LocalDate begin, LocalDate end)*/
//    public PageResult<Emp> page(EmpQueryParam empQueryParam)
//    {
//        /*---------原始代码实现-------------*/
//        /*//1.调用mapper接口，查询总记录数
//        Long total = Long.valueOf(empMapper.count());
//        //2.调用mapper接口，查询分页数据
//        Integer start = (page - 1) * pageSize;//需要将起始索引计算出来
//        List<Emp> rows= empMapper.list(start, pageSize);//第一个参数是起始索引，第二个参数是每页记录数
//        //3.封装到PageResult中返回
//        return new PageResult<>(total,rows);
//        //封装多个参数需要在实体类中构建全参构造*/
//         /*---------使用插件-------------*/
//
////        //1.设置分页参数
////        PageHelper.startPage(page,pageSize);//插件使用的类
////        //2.执行查询
////        List<Emp> empList= empMapper.list(name, gender, begin, end);
////        //3.封装结果
////        Page<Emp> p = (Page<Emp>) empList;//解析查询结果 将empList强转换为Page类型
////        return new PageResult<Emp>(p.getTotal(),p.getResult());
//       // 1.设置分页参数
//        PageHelper.startPage(empQueryParam.getPage(), empQueryParam.getPageSize());//插件使用的类
//        //2.执行查询
//        List<Emp> empList= (List<Emp>) empMapper.data(empQueryParam);
//        //3.封装结果
//        Page<Emp> p = (Page<Emp>) empList;//解析查询结果 将empList强转换为Page类型
//        return new PageResult<Emp>(p.getTotal(),p.getResult());
//    }
//}


package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.EmpExprMapper;
import com.itheima.mapper.EmpMapper;
import com.itheima.pojo.*;
import com.itheima.service.EmpLogService;
import com.itheima.service.EmpService;
import com.itheima.utils.JwtUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 员工管理
 */
//@Service
//public class EmpServiceImpl implements EmpService {
//
//    @Autowired
//    private EmpMapper empMapper;
//
////    @Override
////    public PageResult page(Integer page, Integer pageSize) {
////        //1. 获取总记录数
////        Long total = empMapper.count();
////
////        //2. 获取结果列表
////        Integer start = (page - 1) * pageSize;
////        List<Emp> empList = empMapper.list(start, pageSize);
////
////        //3. 封装结果
////        return new PageResult(total, empList);
////    }
//@Override
//public PageResult page(Integer page, Integer pageSize) {
//    //1. 设置分页参数
//    PageHelper.startPage(page,pageSize);
//
//    //2. 执行查询
//    List<Emp> empList = empMapper.list();
//    Page<Emp> p = (Page<Emp>) empList;
//
//    //3. 封装结果
//    return new PageResult(p.getTotal(), p.getResult());
//}
//}


/**
 * 员工管理
 */
//@Service
//public class EmpServiceImpl implements EmpService {
//
//    @Autowired
//    private EmpMapper empMapper;
//
//    @Override
//    public PageResult page(EmpQueryParam empQueryParam) {
//        //1. 设置PageHelper分页参数
//        PageHelper.startPage(empQueryParam.getPage());
//        //2. 执行查询
//        List<Emp> empList = empMapper.list(empQueryParam);
//        //3. 封装分页结果
//        Page<Emp> p = (Page<Emp>) empList;
//        return new PageResult(p.getTotal(), p.getResult());
//    }
//}
@Service
public class EmpServiceImpl implements EmpService {
    @Autowired
    private EmpMapper empMapper;
    @Autowired
    private EmpExprMapper empExprMapper;
    @Autowired
    private EmpLogService empLogService;
    //@Autowired 出现了致命的自引用循环依赖 注入自身接口导致循环依赖
    private EmpService empService;
    //研究事务传播行为，再注入一个行为

    @Override
    public PageResult page(EmpQueryParam empQueryParam) {
        // 检查并设置默认值
        if (empQueryParam.getPage() == null || empQueryParam.getPageSize() == null) {
            throw new IllegalArgumentException("分页参数不能为空");
        }
        // 设置PageHelper分页参数
        PageHelper.startPage(empQueryParam.getPage(), empQueryParam.getPageSize());
        // 执行查询
        List<Emp> empList = empMapper.list(empQueryParam);
        // 封装分页结果
        Page<Emp> p = (Page<Emp>) empList;
        return new PageResult(p.getTotal(), p.getResult());
    }

    //添加员工
    @SneakyThrows
    @Transactional(rollbackFor = {Exception.class})//用于控制事务
    //@Transactional
    @Override

    public void save(Emp emp) {
        try {
            // 处理默认值
            emp.setCreateTime(LocalDateTime.now());
            emp.setUpdateTime(LocalDateTime.now());
            // 保存员工基本信息数据
            empMapper.insert(emp);
            //添加一个错误代码测试
            //int i=1/0;
            //测试rollbackFor，模拟异常发生
//           if (true) {
//               throw new Exception("保存员工信息时发生异常");
//           }
            //保存员工的工作经历信息-批量
            Integer empId = emp.getId();
            List<EmpExpr> exprList = emp.getEmpExprList();
            //判断是否工作经历是否为空
            if (!CollectionUtils.isEmpty(exprList)) {
                exprList.forEach(empExpr -> empExpr.setEmpId(empId));
                empExprMapper.insertBatch(exprList);
            }
        } finally {
            //记录操作日志，第一个id不需要赋值，因为id是自增的
            EmpLog empLog = new EmpLog(null, LocalDateTime.now(), emp.toString());
            empLogService.insertLog(empLog);
        }
    }

    //删除员工
    @Transactional
    //默认情况下，事务是只读的，即只读事务，所以这里需要设置事务为可读写事务
    //由于删除员工信息，既要删除员工基本信息，又要删除工作经历信息，
    // 操作多次数据库的删除，所以需要进行事务控制。
    @Override//用于标记重写
    public void deleteByIds(List<Integer> ids) {
        // 删除员工基本信息数据
        empMapper.deleteByIds(ids);
        // 删除员工的工作经历信息
        empExprMapper.deleteByEmpIds(ids);
    }

    //查询所有员工信息
    @Override
    public List<Emp> findAll() {
        return empMapper.findAll();
    }

    //查询回显
    @Override
    public Emp getInfo(Integer id) {
        return empMapper.getById(id);
    }

    //修改员工信息
    @Transactional
    @Override
    public void update(Emp emp) {
        //1.根据ID更新员工基本信息
        emp.setUpdateTime(LocalDateTime.now());
        empMapper.updateById(emp);
        //2.根据员工ID删除员工工作经历信息
        empExprMapper.deleteByEmpIds(Arrays.asList(emp.getId()));
        //3.新增员工工作经历信息
        Integer empId = emp.getId();//获取员工ID还有工作经历
        List<EmpExpr> exprList = emp.getEmpExprList();
        if (!CollectionUtils.isEmpty(exprList)) {//如果工作经验不为空，则进行批量插入
            exprList.forEach(empExpr -> empExpr.setEmpId(empId));
            //设置每个工作经历的empId为当前员工的ID
            empExprMapper.insertBatch(exprList);//批量插入工作经历信息
        }
    }

    //    @Override
//    public LoginInfo login(Emp emp) {
//        Emp empLogin = empMapper.selectByUsernameAndPassword(emp);
//        if(empLogin != null){
//            //1. 生成JWT令牌
//            Map<String,Object> dataMap = new HashMap<>();
//            dataMap.put("id", empLogin.getId());
//            dataMap.put("username", empLogin.getUsername());
//
//            String jwt = JwtUtils.generateToken(dataMap);
//            return new LoginInfo(empLogin.getId(), empLogin.getUsername(), empLogin.getName(), jwt);
//
//        }
//        return null;
//    }
//    @Override
//    public LoginInfo login(Emp emp) {
//        Emp e= empMapper.getUsernameAndPassword(emp);
//        if (e!= null) {
//            //1. 生成JWT令牌
//            return  new LoginInfo(e.getId(), e.getUsername(), e.getName(), "");
//        }
//        return null;
//    }
    @Override
    public LoginInfo login(Emp emp) {
        Emp empLogin = empMapper.getUsernameAndPassword(emp);
        if(empLogin != null){
            //1. 生成JWT令牌
            Map<String,Object> dataMap = new HashMap<>();
            dataMap.put("id", empLogin.getId());
            dataMap.put("username", empLogin.getUsername());

            String jwt = JwtUtils.generateJwt(dataMap);
            LoginInfo loginInfo = new LoginInfo(empLogin.getId(), empLogin.getUsername(), empLogin.getName(), jwt);
            return loginInfo;
        }
        return null;
    }
}
