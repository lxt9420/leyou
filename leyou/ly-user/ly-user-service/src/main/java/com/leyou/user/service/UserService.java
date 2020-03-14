package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * author:lu
 * create time: 2020/2/28.
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;
    private static final String KEY_PREFIX="user:verify:phone";
    public Boolean checkData(String data, Integer type) {
        User u=new User();
        switch (type){
            case 1:
                u.setUsername(data);
                break;
            case 2:
                u.setPhone(data);
                break;
            default:
               throw new LyException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        return userMapper.selectCount(u) == 0;
    }

    public void sendCode(String phone) {
        //生成key
        String key=KEY_PREFIX + phone;
        //生成验证码
        String code= NumberUtils.generateCode(6);
        Map<String,String> msg=new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        //发送短信
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",msg);
        //保存验证码
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);
    }

    public void register(@Valid User user, String code) {
        //获取redis验证码
        String redisCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        //判断验证码是否正确
        if(!StringUtils.equals(code,redisCode)){
           throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        //生成盐值
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //生成md5
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));

        user.setCreated(new Date());
        //存入数据库
        userMapper.insert(user);
    }

    public User queryUserByUsernameAndPassword(String username, String password) {
        User record = new User();
        record.setUsername(username);
        //查询用户
        User user = userMapper.selectOne(record);
        //验证
        if(user==null){
            throw new LyException(ExceptionEnum.USER_NO_FOND);
        }
        //验证密码
        if(StringUtils.equals(record.getPassword(),CodecUtils.md5Hex(password,record.getSalt()))){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_PASSWORD);
        }
        //用户名密码正确
        return user;
    }
}
