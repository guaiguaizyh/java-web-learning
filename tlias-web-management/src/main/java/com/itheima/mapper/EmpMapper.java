//package com.itheima.mapper;
//
//import com.itheima.pojo.Emp;
//import com.itheima.pojo.EmpQueryParam;
//import org.apache.ibatis.annotations.Mapper;
//
//
//import java.util.List;
//
//
//@Mapper
//public interface EmpMapper {
//    /*--------原始分页查询实现-------------*/
//    //查询所有的员工及其对应的部门名称
//    //@Select("select e.*, d.name as deptName from emp e left join dept d on e.dept_id = d.id")
//    //给部门名起别名为:deptName
//    //因为在接口文档中，要求部门名称给前端返回的数据中，就必须为deptName.
//    //在这里我们将查询返回的每一条记录都封装到Emp对象中，就必须保证查询返回的字段名与属性名是一致的。
//   //此时，我们就需要在Emp中定义一个属性 deptName 用来封装部门名称
//    //public List<Emp> list();
//    //分页查询
//    //查询总记录数
//    /*@Select("select count(*) from emp e left join dept d on e.dept_id=d.id")
//    public Integer count();
//    //需要根据修改时间倒序排序 进行排序分页
//    //@Select("select e.*, d.name as deptName from emp e left join dept d on e.dept_id = d.id "
//            + "order by e.update_time desc limit #{start},#{pageSize}")
//    public List<Emp>list(Integer start,Integer pageSize);*/
//
///*------------------使用插件-----------------*/
//    //只需要查询操作，不需要分页操作
//   //@Select("select e.*,d.name deptName from emp as e left join dept as d on e.dept_id=d.id order by e.update_time desc")
//    /*-----条件查询-----因为SQL语句较为复杂，所以建议将Sql语句配置在XML映射文件中*/
//    //public List<Emp> list(String name, Integer gender, LocalDate begin, LocalDate end);
//
//    List<Emp> data(EmpQueryParam empQueryParam);
//    //调用List方法的时候需要将条件查询对应的参数传递下去
//



package com.itheima.mapper;

import com.itheima.pojo.Emp;
import com.itheima.pojo.EmpQueryParam;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


//public interface EmpMapper {
//
//    /**
//     * 查询总记录数
//     */
//    @Select("select count(*) from emp e left join dept d on e.dept_id = d.id ")
//    public Long count();
//
//    /**
//     * 查询所有的员工及其对应的部门名称
//     */
////    @Select("select e.*, d.name deptName from emp as e left join dept as d on e.dept_id = d.id limit #{start}, #{pageSize}")
////    public List<Emp> list(Integer start , Integer pageSize);
//    /**
//     * 查询所有的员工及其对应的部门名称
//     */
//    @Select("select e.*, d.name deptName from emp as e left join dept as d on e.dept_id = d.id")
//    public List<Emp> list();
//
//}
@Mapper
public interface EmpMapper {

    /**
     * 查询所有的员工及其对应的部门名称
     */
    public List<Emp> list(EmpQueryParam empQueryParam);

   //新增员工信息
    @Options(useGeneratedKeys = true, keyProperty = "id")
    //通过对应员工的主键来获取到对应员工信息
    @Insert("insert into emp(username, name, gender, phone, job, salary, image, entry_date, dept_id, create_time, update_time) " +
            "values (#{username},#{name},#{gender},#{phone},#{job},#{salary},#{image},#{entryDate},#{deptId},#{createTime},#{updateTime})")
    void insert(Emp emp);

    //实现批量删除员工信息
    void deleteByIds(List<Integer> ids);
    //查询回显
    Emp getById(Integer id);
    //修改员工信息
    void updateById(Emp emp);
    //统计各个职位的员工人数,因为操作的是员工表，所以直接写在EmpMapper中
    @MapKey("pos")
    List<Map<String,Object>> countEmpJobData();
    //统计各个性别的员工人数
    @MapKey("name")
    List<Map<String,Object>> countEmpGenderData();
 //根据用户名和密码查询员工信息
 @Select("select * from emp where username = #{username} and password = #{password}")
 Emp getUsernameAndPassword(Emp emp);
 //查询所有员工信息
 @Select("select * from emp")
 List<Emp> findAll();
}