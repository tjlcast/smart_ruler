package com.tjlcast.server.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tjlcast.server.data.Filter;
import com.tjlcast.server.data.Rule;
import com.tjlcast.server.data.Rule2Filter;
import com.tjlcast.server.data.Transform;
import com.tjlcast.server.data_source.DataSourceProcessor;
import com.tjlcast.server.data_source.RuleCreation;
import com.tjlcast.server.services.FilterService;
import com.tjlcast.server.services.Rule2FilterService;
import com.tjlcast.server.services.RuleService;
import com.tjlcast.server.services.TransformService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by tangjialiang on 2018/4/13.
 */

@RestController
@RequestMapping("/api/rule")
@Slf4j
public class RuleController extends BaseContoller {
    @Autowired
    RuleService ruleService ;

    @Autowired
    FilterService filterService;

    @Autowired
    TransformService transformService;

    @Autowired
    Rule2FilterService rule2FilterService;

    @Autowired
    DataSourceProcessor dataSourceProcessor ;

    //Post新增规则
    @ApiOperation(value = "todo ***")
    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String addRule(@RequestBody String jsonStr) {
        JsonObject jsonObj = (JsonObject)new JsonParser().parse(jsonStr);
        RuleCreation ruleCreation = new RuleCreation(jsonObj);

        transformService.addTransform(ruleCreation.getTransform());
        Integer transformId =ruleCreation.getTransform().getTransformId();
        ruleCreation.getRule().setTransformId(transformId);

        ruleService.addRule(ruleCreation.getRule());
        Integer ruleId = ruleCreation.getRule().getRuleId();


        for(Filter filter:ruleCreation.getFilters())
        {
            filterService.addFilter(filter);
            Integer filterId =filter.getFilterId();

            Rule2Filter rule2Filter=new Rule2Filter(ruleId,filterId);
            rule2FilterService.addARelation(rule2Filter);

        }

        ifRuleDeleteOrChange(ruleCreation.getRule());
        return "OK" ;
    }

    //激活规则
    @ApiOperation(value = "todo ***")
    @RequestMapping(value = "/{ruleId}/activate", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String activateRule(@PathVariable("ruleId") String ruleId) {
        Rule rule = ruleService.findRuleById(Integer.valueOf(ruleId));

        ruleService.setRuleActive(Integer.valueOf(ruleId));

        ifRuleDeleteOrChange(rule);
        return "Activate" ;
    }

    //暂停规则
    @ApiOperation(value = "todo ***")
    @RequestMapping(value = "/{ruleId}/suspend", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String suspendRule(@PathVariable("ruleId") String ruleId) {
        Rule rule = ruleService.findRuleById(Integer.valueOf(ruleId));

        ruleService.setRuleSuspend(Integer.valueOf(ruleId));

        ifRuleDeleteOrChange(rule);
        return "Suspend" ;
    }

    //Delete 删除规则
    @ApiOperation(value = "todo ***")
    @RequestMapping(value = "/remove/{ruleId}", method = RequestMethod.DELETE, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String removeRule(@PathVariable("ruleId") String ruleId) {
        Rule rule = ruleService.findRuleById(Integer.valueOf(ruleId));
        List<Filter> filters=filterService.findFilterByRuleId(Integer.valueOf(ruleId));
        Transform transform=transformService.getByRuleId(Integer.valueOf(ruleId));

        rule2FilterService.removeRelation(Integer.valueOf(ruleId));

        for(Filter filter:filters){
            filterService.removeAFilter(filter.getFilterId());
        }

        ruleService.removeARule(Integer.valueOf(ruleId));

        transformService.deleteById(transform.getTransformId());

        ifRuleDeleteOrChange(rule);

        return "OK" ;
    }

    //GET 获取全部规则
    @ApiOperation(value = "todo ***")
    @RequestMapping(value = "/rules", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public List<RuleCreation> getRules() {
        List<RuleCreation> ruleCreations = new LinkedList<>();
        List<Rule> allRule = ruleService.getAllRule();
        for(Rule rule:allRule)
        {
            List<Filter> filters = filterService.findFilterByRuleId(rule.getRuleId());
            Transform transform = transformService.getByRuleId(rule.getRuleId());
            ruleCreations.add(new RuleCreation(rule,filters,transform));
        }
        return ruleCreations ;
    }

    //按规则ID获取规则
    @ApiOperation(value = "todo ***")
    @RequestMapping(value = "/rule/{ruleId}", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public RuleCreation getARule(@PathVariable("ruleId") String ruleId)
    {

        Rule rule = ruleService.findRuleById(Integer.valueOf(ruleId));

        List<Filter> filters = filterService.findFilterByRuleId(rule.getRuleId());
        Transform transform = transformService.getByRuleId(rule.getRuleId());
        RuleCreation ruleCreation=new RuleCreation(rule, filters, transform);
        return ruleCreation;
    }

    //按规则获取规则
    @ApiOperation(value = "todo ***")
    @RequestMapping(value = "/ruleByTenant/{tenantId}", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public List<RuleCreation> getRuleByTenantId(@PathVariable("tenantId") String tenantId)
    {
        List<RuleCreation> ruleCreations = new LinkedList<>();
        List<Rule> rules = ruleService.findRuleByTenantId(Integer.valueOf(tenantId));

        for(Rule rule:rules)
        {
            List<Filter> filters = filterService.findFilterByRuleId(rule.getRuleId());
            Transform transform = transformService.getByRuleId(rule.getRuleId());
            ruleCreations.add(new RuleCreation(rule,filters,transform));
        }
        return ruleCreations ;
    }

    //小心使用！！！！！！！！！！！！！
    @ApiOperation(value = "todo ***")
    @RequestMapping(value = "/removeAll/{pass}", method = RequestMethod.DELETE, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String removeALLRule(@PathVariable("pass") String pass){
        if(pass.equals("K815"))
        {
            rule2FilterService.removeAllRelation();
            filterService.removeAll();
            transformService.deleteAll();
            ruleService.removeAllRule();
            return "DeleteSuccess";
        }

        return "DeleteFault";
    }

    /**
     * 规则相关：用于更新Akka
     * @param rule
     */
    public void ifRuleDeleteOrChange(Rule rule){
        dataSourceProcessor.process(rule);
    }

}
