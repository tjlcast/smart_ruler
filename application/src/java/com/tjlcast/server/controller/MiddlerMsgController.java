package com.tjlcast.server.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tjlcast.server.data.Filter;
import com.tjlcast.server.data.Rule;
import com.tjlcast.server.data.Rule2Filter;
import com.tjlcast.server.data_source.DataSourceProcessor;
import com.tjlcast.server.data_source.FromMsgMiddlerDeviceMsg;
import com.tjlcast.server.services.FilterService;
import com.tjlcast.server.services.Rule2FilterService;
import com.tjlcast.server.services.RuleService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

/**
 * Created by tangjialiang on 2018/4/22.
 */

@RestController
@RequestMapping("/api/test")
@Slf4j
public class MiddlerMsgController extends BaseContoller {

    @Autowired
    Rule2FilterService rule2FilterService;

    @Autowired
    FilterService filterService;

    @Autowired
    RuleService ruleService;

    @Autowired
    DataSourceProcessor dataSourceProcessor ;

    @ApiOperation(value = "测试：模拟从kafka中拉取数据")
    @RequestMapping(value = "/deviceMsg", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String postDeviceMsg(@RequestBody String jsonStr) {
        log.info("MiddlerMsgController receive a msg : " + jsonStr) ;

        // str 2 obj
        JsonObject jsonObj = (JsonObject)new JsonParser().parse(jsonStr);
        FromMsgMiddlerDeviceMsg fromMsgMiddlerDeviceMsg = new FromMsgMiddlerDeviceMsg(jsonObj);

        Random random = new Random();
        Rule rule = new Rule((int)(Math.random()*100000),fromMsgMiddlerDeviceMsg.getTenantId(),"Rule"+random.nextInt());
        ruleService.addRule(rule);

        Filter filter = new Filter((int)(Math.random()*100000),"function filter(key,value){if(key=='x' && value>0){ return true;} else{return false;}}");
        filterService.addFilter(filter);

        Rule2Filter rule2Filter= new Rule2Filter(rule.getId(),filter.getId());
        rule2FilterService.addARelation(rule2Filter);

        dataSourceProcessor.process(fromMsgMiddlerDeviceMsg);

        return "OK" ;
    }

    @ApiOperation(value = "测试：模拟从kafka中拉取数据")
    @RequestMapping(value = "/receive", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String receive(@RequestBody String jsonstr){

        System.out.println(jsonstr);
        return "success";
    }
}
