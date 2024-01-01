package top.zynorl.demo.gatewaydemo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.zynorl.demo.gatewaydemo.domin.DBUser;

@Mapper
public interface UserMapper {
    @Select("select * from user_info where username=#{username}")
    DBUser selectUserByUsername(String username);
}
