package com.itheima.mapper;

import com.itheima.pojo.Clazz;
import com.itheima.pojo.ClazzQueryParam;
import com.itheima.pojo.Emp;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ClazzMapper {
    //分页查询
    public List<Clazz> clist(ClazzQueryParam clazzQueryParam);

    //实现批量员工信息
    //删除部门
    @Delete("DELETE FROM clazz WHERE id = #{id}")
    //#{id}表示占位符，变成？，生成预编译的SQL语句
    void deleteById(Integer id);
    //新增班级
    void insert(Clazz clazz);
    //修改班级信息
    //查询回显
    Clazz getById(Integer id);
    //修改员工信息
    void updateById(Clazz clazz);
    //查询所有班级信息
    @Select("SELECT * FROM clazz")
    List<Clazz> findALL();


}
