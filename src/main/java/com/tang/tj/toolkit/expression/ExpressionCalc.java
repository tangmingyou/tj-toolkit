package com.tang.tj.toolkit.expression;

import com.tang.tj.toolkit.util.Collects;
import com.tang.tj.toolkit.util.Nums;
import com.tang.tj.toolkit.util.Objs;
import com.tang.tj.toolkit.util.Strings;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 指标表达式计算工具类
 * 注: 表达式变量不可包含乘除加减小括号符号 () * + - /, 如包含则使用 {@link ExpressionCalc#parseExp(String, Map)} 解析表达式
 */
public class ExpressionCalc {

    private static final int MAX_DEEP_LEVEL = 1000; 
    
    // 特殊运算符号
     private static final Set<Character> TOKENS = new HashSet<>(Arrays.asList('(', ')', '*', '/', '+', '-'));
//    public static final Set<Character> TOKENS = ImmutableSet.of('(', ')', '*', '/', '+', '-');

    // 判断字符串是否包含加减乘除括号
    private static final Pattern EXP_PATTERN = Pattern.compile("^.*?[()*/+\\-].*$");

    /** 百分比值字符串 */
    private static final Pattern PERCENT_PATTERN = Pattern.compile("^(-?\\d+(\\.\\d+)?)%$");

    /**
     * 传入变量计算解析后的表达式
     * @param exp 解析后的表达式
     * @param variableMap 变量值
     * @return 表达式计算结果
     */
    public static double calcExp(Exp exp, Map<String, Double> variableMap) {
        return calcExp(0, exp, variableMap, 
                varName -> {throw new IllegalArgumentException("表达式变量["+varName+"]不存在");}, 
                (left,right) -> 0d
        );
    }

    public static double calcExp(Exp exp, Map<String, Double> variableMap, Function<String, Double> nonVarValueProvider) {
        return calcExp(0, exp, variableMap, nonVarValueProvider, (left,right) -> 0d);
    }

    public static double calcExp(Exp exp, Map<String, Double> variableMap,
                                 Function<String, Double> nonVarValueProvider,
                                 BiFunction<Double, Double, Double> dividendZeroCalc) {
        return calcExp(0, exp, variableMap, nonVarValueProvider, dividendZeroCalc);
    }

    /**
     * 计算解析表达式
     * @param nonVarValueProvider 表达式变量不在variableMap时的值provider
     * @param dividendZeroCalc 被除数等于0时算法
     */
    private static double calcExp(int callTimes, Exp exp, Map<String, Double> variableMap,
                                  Function<String, Double> nonVarValueProvider,
                                  BiFunction<Double, Double, Double> dividendZeroCalc) {
        if (callTimes > MAX_DEEP_LEVEL) {
            throw new IllegalStateException(String.format("表达式递归运算次数超过%d次", MAX_DEEP_LEVEL));
        }
        if (exp.type == 1) {
            return exp.value;
        }
        if (exp.type == 3) {
            if (!variableMap.containsKey(exp.varName)) {
                return Objs.ifNull(nonVarValueProvider.apply(exp.varName), 0d);
            }
            // 取变量, 默认0
            return Objs.ifNull(variableMap.get(exp.varName), 0d);
        }
        if (exp.type != 2) {
            throw new IllegalArgumentException("未知的表达式类型:" + exp.type);
        }
        // 左边值
        double left = calcExp(callTimes + 1, exp.values[0], variableMap, nonVarValueProvider, dividendZeroCalc);
        // 右边值
        double right = calcExp(callTimes + 1, exp.values[1], variableMap, nonVarValueProvider, dividendZeroCalc);
        switch (exp.opt) {
            case '+': return left + right;
            case '-': return left - right;
            case '*': return left * right;
            case '/':
                if (right == 0) {
                    return dividendZeroCalc.apply(left, right);
                }
                return left / right;
            default:
                throw new IllegalArgumentException("未知的运算符:"+ exp.opt);
        }
    }

    /**
     * ！！！忽略表达式变量大小写，变量复杂时可解析度高
     * 处理表达式中变量可能会有特殊计算符号, 先使用变量将表达式字符串变量替换，再将中文括号替换成英文括号进行解析
     * @param exp 字符串表达式
     * @param variableMap 表达式所有变量
     * @return 表达式解析对象
     */
    public static Exp parseExp(String exp, Map<String, Double> variableMap) {
        if (Collects.isEmpty(variableMap)) {
            return parseExp(exp);
        }
        Map<String, Double> replaceVariables = new HashMap<>(Collects.mapCapacity(variableMap.size()));
        Map<String, String> varNameMap = new HashMap<>();
        // 这里将指标名排序，长的放到前面，避免长的指标名包含短的指标名情况
        List<String> varNames = new ArrayList<>(variableMap.keySet());
        varNames.sort((a, b) -> b.length() - a.length());

        int varNum = 1;
        exp = exp.toUpperCase(Locale.ENGLISH);
        for (String variable : varNames) {
            if (Strings.isEmpty(variable)) {
                continue;
            }
            if (Nums.isParsable(variable)) {
                continue;
            }
            String varIdx = "v" + (varNum++);
            exp = exp.replace(variable.toUpperCase(Locale.ENGLISH), varIdx);
            replaceVariables.put(varIdx, variableMap.get(variable));
            varNameMap.put(variable, varIdx);
        }

        Exp expression = parseExp(exp);
        expression.varNameMapping = varNameMap;
        variableMap.putAll(replaceVariables);
        return expression;
    }

    /**
     * 不忽略表达式变量大小写，变量不能包含特殊符号(加减乘除、小括号)，不支持中文括号
     */
	public static Exp parseExp(String exp) {
        exp = exp.replace('（', '(').replace('）', ')');
		return parseExp(0, exp);
	}

    private static Exp parseExp(int callTimes, String exp) {
        if (callTimes > MAX_DEEP_LEVEL) {
            throw new IllegalStateException(String.format("表达式递归解析次数超过%d次", MAX_DEEP_LEVEL));
        }
        exp = exp.trim();
        if (Strings.isEmpty(exp)) {
            throw new IllegalStateException("表达式有误请检查");
        }
        // 表达式为常量值 12.31
        if (Nums.isParsable(exp)) {
            Exp expression = new Exp();
            expression.type = 1;
            expression.value = Nums.createDouble(exp);
            expression.segment = exp;
            return expression;
        }

        // 表达式为百分比 -20.22%
        Matcher matcher = PERCENT_PATTERN.matcher(exp);
        if (matcher.matches()) {
            Exp expression = new Exp();
            expression.type = 1;
            expression.value = Double.parseDouble(matcher.group(1)) / 100;
            expression.segment = exp;
            return expression;
        }

        // 表达式不包含运算符，表达式为变量
        if (!EXP_PATTERN.matcher(exp).matches()) {
            Exp expression = new Exp();
            expression.type = 3;
            expression.varName = exp;
            expression.segment = exp;
            return expression;
        }

        // ...表达式为运算操作
        List<Exp> segments = new ArrayList<>();

        // begin操作符左边的常量或变量名
        for (int i = 0, len = exp.length(), begin = i; i < len; i++) {
            char c = exp.charAt(i);
            if (!TOKENS.contains(c)) {
                // 最后一个字符，且exp不为常量或变量，作为表达式片段截取
                if (i == len - 1) {
                    if (begin > 0 && begin <= i) {
                        segments.add(parseExp(callTimes + 1, exp.substring(begin, len)));
                    }
                }
                continue;
            }

            // 左括号:有表达式片段
            if ('(' == c) {
                // 找右括号 )
                int closeIdx = findCloseIdx(exp, i + 1, '(', ')');
                if (closeIdx < 0) {
                    // throw new IllegalStateException(String.format("( at index %d not close", i));
                    throw new IllegalStateException(String.format("无右括号对应第%d个字符的左括号", i));
                }
                segments.add(parseExp(callTimes + 1, exp.substring(i + 1, closeIdx)));
                i = closeIdx;
                begin = i + 1;
                continue;
            }
            if (')' == c) {
                // throw new IllegalStateException(String.format("unknown symbol ) at index %d", i));
                throw new IllegalStateException(String.format("无左括号对应第%d个字符的右括号", i));
            }

            // 加减乘除操作符
            // 操作符左边值
            if (begin < i) {
                String valueLeft = exp.substring(begin, i);
                if (Strings.isNotEmpty(valueLeft.trim())) {
                    segments.add(parseExp(callTimes + 1, valueLeft));
                }
            }
            // 操作符
            Exp expS = new Exp();
            expS.type = 4;
            expS.opt = c;
            segments.add(expS);

            begin = i + 1;
        }

        return combineExpSegments(exp, segments);
    }

    private static Exp combineExpSegments(String exp, List<Exp> segments) {
        int size = segments.size();
        if ((size % 2) != 1) {
            throw new IllegalStateException("表达式丢失运算符");
        }
        // 是常量或变量
        if (size == 1) {
            return segments.get(0);
        }
        // 最简单的二元运算
        if (size == 3) {
            Exp expression = new Exp();
            expression.segment = exp;
            expression.type = 2;
            expression.opt = segments.get(1).opt;
            expression.values = new Exp[]{segments.get(0), segments.get(2)};
            return expression;
        }

        List<Exp> plusSegments = new ArrayList<>(segments.size());

        Exp temp = new Exp();
        temp.type = 2;
        temp.values = new Exp[]{segments.get(0), null};
        // 从左往右解析
        for (int i = 1; i < size; i+=2) {
            Exp opt = segments.get(i);
            Exp expRight = segments.get(i + 1);
            if (opt.type != 4) {
                throw new IllegalStateException(String.format("%s 无运算符", exp));
            }
            // 乘除优先
            if (opt.opt == '*' || opt.opt == '/') {
                temp.opt = opt.opt;
                temp.values[1] = expRight;
                // 不是最后一个 segment, new 一个新的 exp 容器
                if (i + 2 < size) {
                    Exp nextTemp = new Exp();
                    nextTemp.type = 2;
                    nextTemp.values = new Exp[]{temp, null};
                    temp = nextTemp;
                }
            } else {
                // 加减
                plusSegments.add(temp.values[0]);
                plusSegments.add(opt);

                temp = new Exp();
                temp.type = 2;
                temp.values = new Exp[]{expRight, null};
            }
        }

        plusSegments.add(temp.values[1] == null ? temp.values[0] : temp);

        // 开始计算只有加减的公式
        if (plusSegments.size() == 1) {
            plusSegments.get(0).segment = exp;
            return plusSegments.get(0);
        }
        if (plusSegments.size() == 3) {
            Exp expression = new Exp();
            expression.segment = exp;
            expression.type = 2;
            expression.opt = plusSegments.get(1).opt;
            expression.values = new Exp[]{plusSegments.get(0), plusSegments.get(2)};
            return expression;
        }

        // 加减从左往右运算
        temp = new Exp();
        temp.type = 2;
        temp.values = new Exp[]{plusSegments.get(0), null};
        for (int i = 1, len = plusSegments.size(); i < len; i+=2) {
            Exp opt = plusSegments.get(i);
            Exp expRight = plusSegments.get(i + 1);
            if (opt.type != 4) {
                throw new IllegalStateException(String.format("%s 无运算符", exp));
            }

            temp.opt = opt.opt;
            temp.values[1] = expRight;
            // 不是最后一个 segment, new 一个新的 exp 容器
            if (i + 2 < len) {
                Exp nextTemp = new Exp();
                nextTemp.type = 2;
                nextTemp.values = new Exp[]{temp, null};
                temp = nextTemp;
            }
        }

        temp.segment = exp;
        return temp;
    }

    private static int findCloseIdx(String exp, int begin, char open, char close) {
        int segment = 1; // 中间有括号包括号情况
        for (int i = begin, len = exp.length(); i < len; i++) {
            char c = exp.charAt(i);
            if (c == close) {
                segment--;
                if (segment == 0) {
                    return i;
                }
            }
            if (c == open) {
                segment ++;
            }
        }
        return -1;
    }


    /**
     * 计算表达式
     */
    public static class Exp {

        /** 1.常量值  2.二元操作符表达式  3.变量值 4.临时存储操作符(加减乘除) */
        int type;

        /** type=1常量值 */
        double value;

        /** type=3 变量名 */
        String varName;

        // type=2 操作符 加减乘除
        char opt;

        // 操作值表达式对象 (常量, 指标值变量, 表达式)
        Exp[] values;

        // 字符串表达式片段
        String segment;

        /** 根exp节点,存储变量映射关系 */
        public Map<String, String> varNameMapping;

        @Override
        public String toString() {
            return "Exp{" +
                    "type=" + type +
                    ", value=" + value +
                    ", varName='" + varName + '\'' +
                    ", opt=" + opt +
                    ", values=" + Arrays.toString(values) +
                    ", segment='" + segment + '\'' +
                    '}';
        }

        // ========== 可不要 getter setter 这里方便打印 json
        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public String getVarName() {
            return varName;
        }

        public void setVarName(String varName) {
            this.varName = varName;
        }

        public char getOpt() {
            return opt;
        }

        public void setOpt(char opt) {
            this.opt = opt;
        }

        public Exp[] getValues() {
            return values;
        }

        public void setValues(Exp[] values) {
            this.values = values;
        }

        public String getSegment() {
            return segment;
        }

        public void setSegment(String segment) {
            this.segment = segment;
        }
    }

}
