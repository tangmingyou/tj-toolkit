package com.tang.tj.toolkit.expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionCalcTest {

    @BeforeEach
    void setUp() {
        System.out.println("测试开始...");
    }
    
    @Test
    void calcExp01() {
        String expStr = "(a+b)-1.0 - (-50%) - b + (a+b)/b";
        
        // 解析复杂二元运算表达式
        // 为二叉树结构，叶子节点为单个常量或变量
        ExpressionCalc.Exp exp = ExpressionCalc.parseExp(expStr);
        
        // 表达式变量值
        Map<String, Double> vars = new HashMap<>();
        vars.put("a", 1d);
        vars.put("b", 2d);
        double result1 = ExpressionCalc.calcExp(exp, vars);
        assertEquals(2d, result1, 0d);

        // 替换表达式变量再次计算
        vars.put("a", 2d);
        vars.put("b", 10.5d);
        double result2 = ExpressionCalc.calcExp(exp, vars);
        assertEquals(2.69047d, result2, 0.00001d);
    }
    
}