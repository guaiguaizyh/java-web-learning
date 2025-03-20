package com.itheima.mapper;

import com.itheima.pojo.Dept;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DeptMapper {
    //查询部门数据
    @Select("SELECT id, name, create_time, update_time FROM dept ORDER BY update_time DESC")
    List<Dept> findAll();
    //删除部门---对应的学员也删除
    @Delete("DELETE FROM dept WHERE id = #{id}")
    //#{id}表示占位符，变成？，生成预编译的SQL语句
    void deleteById(Integer id);
    //添加部门
    @Insert("INSERT INTO dept(name, create_time, update_time) VALUES (#{name}, #{createTime}, #{updateTime})")
    void insert(Dept dept);//insert函数
    //mapper中需要传递多个参数，可以把多个参数封装到一个对象中，
    //#{...} 里面写的是对象的属性名，需要用驼峰用名【注意是属性名，不是表的字段名】
    //修改部门名称
    //查询回显
    @Select("SELECT id, name, create_time, update_time FROM dept WHERE id = #{id}")
    Dept findById(Integer id);

    @Update("UPDATE dept SET name = #{name}, update_time = #{updateTime} WHERE id = #{id}")
    void update(Dept dept);
}

