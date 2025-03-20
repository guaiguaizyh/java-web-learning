package com.itheima.mapper;

import com.itheima.pojo.*;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StuMapper {
    //分页查询
    public List<Stu> stulist(StuQueryParam stuQueryParam);
    //删除
    void deleteByIds(@Param("ids") List<Integer> ids);
    //新增学员信息
    void insert(Stu stu);
    //根据id查询学员信息
    Stu getById (Integer id);
    //修改学员信息
    void update(Stu stu);
    //统计学员的学历信息
    @MapKey("name")
    List<Map<String,Object>> countStudentDegreeData();
    //统计班级的人数信息根据学员的表
    @MapKey("pos")
    List<Map<String,Object>> countClazzCountData();
}
