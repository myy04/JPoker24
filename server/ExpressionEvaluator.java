import java.util.*;
import java.util.regex.*;

public class ExpressionEvaluator {

    public static double evaluateExpression(String expression) {
        List<String> tokens = tokenize(expression);
        handleUnaryMinus(tokens);
        List<String> postfix = infixToPostfix(tokens);
        return evaluatePostfix(postfix);
    }

    private static List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        String cleaned = expression.replaceAll("\\s+", "");
        String regex = "\\d+(\\.\\d+)?|\\.\\d+|[-+*/()]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(cleaned);
        while (matcher.find()) {
            String token = matcher.group();
            tokens.add(token);
        }
        return tokens;
    }

    private static void handleUnaryMinus(List<String> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.equals("-")) {
                boolean isUnary = false;
                if (i == 0) {
                    isUnary = true;
                } else {
                    String prevToken = tokens.get(i - 1);
                    if (prevToken.equals("(") || prevToken.matches("[-+*/]")) {
                        isUnary = true;
                    }
                }
                if (isUnary) {
                    tokens.add(i, "0");
                    i++;
                }
            }
        }
    }

    private static List<String> infixToPostfix(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();
        Map<String, Integer> precedence = new HashMap<>();
        precedence.put("+", 1);
        precedence.put("-", 1);
        precedence.put("*", 2);
        precedence.put("/", 2);

        for (String token : tokens) {
            if (isNumber(token)) {
                output.add(token);
            } else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    output.add(stack.pop());
                }
                stack.pop();
            } else {
                while (!stack.isEmpty() && !stack.peek().equals("(") &&
                        precedence.getOrDefault(token, 0) <= precedence.getOrDefault(stack.peek(), 0)) {
                    output.add(stack.pop());
                }
                stack.push(token);
            }
        }

        while (!stack.isEmpty()) {
            output.add(stack.pop());
        }

        return output;
    }

    private static boolean isNumber(String token) {
        return token.matches("\\d+(\\.\\d+)?|\\.\\d+");
    }

    private static double evaluatePostfix(List<String> postfix) {
        Deque<Double> stack = new ArrayDeque<>();
        for (String token : postfix) {
            if (isNumber(token)) {
                stack.push(Double.parseDouble(token));
            } else {
                double b = stack.pop();
                double a = stack.pop();
                switch (token) {
                    case "+":
                        stack.push(a + b);
                        break;
                    case "-":
                        stack.push(a - b);
                        break;
                    case "*":
                        stack.push(a * b);
                        break;
                    case "/":
                        if (b == 0) {
                            throw new ArithmeticException("Division by zero");
                        }
                        stack.push(a / b);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid operator: " + token);
                }
            }
        }
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression");
        }
        return stack.pop();
    }

}