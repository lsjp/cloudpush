package com.huangliang.cloudpushwebsocket.task;

import com.huangliang.api.constants.RedisPrefix;
import com.huangliang.cloudpushwebsocket.config.ComConfig;
import com.huangliang.cloudpushwebsocket.constants.Constants;
import com.huangliang.cloudpushwebsocket.service.ChannelService;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 扫描不在线的用户
 */
@Component
@EnableScheduling   // 2.开启定时任务
@Slf4j
public class ScanClientsNotOnline {

    @Autowired
    private ChannelService channelService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ComConfig config;

    private static Long now = null;

    @Scheduled(fixedRate=60000)
    private void execute() {
        Long startTime = System.currentTimeMillis();
        log.info("过期客户端扫描任务开启...");
        now = System.currentTimeMillis();
        Map<String,Channel> channels = channelService.getAll();
        if(CollectionUtils.isEmpty(channels)){
            log.info("没有客户端连接.");
            return ;
        }
        Channel channel = null;
        //存储需要删除的客户端
        List<String> delKeys = new ArrayList<>();
        for(String key : channels.keySet()){
            channel = channelService.get(key);
            //判断在线
            if(!channel.isOpen()&&outOfTime(channel)){
                //如果在离线就删除
                delKeys.add(key);
            }else{
                //如果在线就延长有效时间
                redisTemplate.expire(RedisPrefix.PREFIX_CLIENT+key,config.getExpireTime(),TimeUnit.SECONDS);
            }
        }
        //遍历key移除过期客户端连接
        for(String key : delKeys){
            channelService.remove(key);
        }
        redisTemplate.expire(RedisPrefix.PREFIX_SERVERCLIENTS+config.getInstanceId(),120,TimeUnit.SECONDS);
        log.info("过期客户端扫描任务执行完毕,踢出[{}]个客户端,耗时[{}]ms",delKeys.size(),System.currentTimeMillis()-startTime);
    }

    private boolean outOfTime(Channel channel) {
        String activeTime = channel.attr(Constants.attrActiveTime).get();
        if(System.currentTimeMillis()-Long.parseLong(activeTime)>config.getIntervalTime()){
            return true;
        }else{
            return false;
        }
    }
}
