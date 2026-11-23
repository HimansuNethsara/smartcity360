package lk.jiat.smartcity360.pattern.interpreter;

import java.util.List;
import java.util.Set;

public final class PolicyParser {

    public static final int MAX_LENGTH = 500;
    private static final int MAX_DEPTH = 10;
    private static final long MAX_FINE = 1_000_000;
    private static final int MAX_ALERT_LENGTH = 120;

    private final PolicyVocabulary vocabulary;
    private final List<Token> tokens;
    private int index;
    private int depth;

    private PolicyParser(String src, PolicyVocabulary vocabulary) {
        this.vocabulary = vocabulary;
        this.tokens = PolicyLexer.tokenize(src);
    }

    public static PolicyProgram parse(String src, PolicyVocabulary vocabulary) {
        if (src == null || src.isBlank()) {
            throw new PolicyParseException("The policy is empty", 0);
        }
        if (src.length() > MAX_LENGTH) {
            throw new PolicyParseException("The policy is longer than " + MAX_LENGTH + " characters", 0);
        }
        return new PolicyParser(src, vocabulary).program();
    }

    private PolicyProgram program() {
        expect(TokenType.IF, "A policy must start with IF");
        Expression condition = condition();
        expect(TokenType.THEN, "Expected THEN after the condition");
        PolicyAction action = action();
        if (peek().type() != TokenType.END) {
            throw error("Nothing may follow the action");
        }
        return new PolicyProgram(condition, action);
    }

    private Expression condition() {
        Expression left = and();
        while (peek().type() == TokenType.OR) {
            next();
            left = new OrExpression(left, and());
        }
        return left;
    }

    private Expression and() {
        Expression left = not();
        while (peek().type() == TokenType.AND) {
            next();
            left = new AndExpression(left, not());
        }
        return left;
    }

    private Expression not() {
        if (peek().type() == TokenType.NOT) {
            next();
            enter();
            Expression inner = not();
            depth--;
            return new NotExpression(inner);
        }
        return primary();
    }

    private Expression primary() {
        if (peek().type() == TokenType.LPAREN) {
            next();
            enter();
            Expression inner = condition();
            depth--;
            expect(TokenType.RPAREN, "Expected ')' to close the bracket");
            return inner;
        }
        return comparison();
    }

    private Expression comparison() {
        Token var = peek();
        if (var.type() != TokenType.IDENTIFIER) {
            throw error("Expected a variable name such as " + sample());
        }
        next();
        PolicyVocabulary.VariableType type = vocabulary.typeOf(var.text());
        if (type == null) {
            throw new PolicyParseException("Unknown variable '" + var.text() + "'. Known: "
                    + String.join(", ", vocabulary.names()), var.pos());
        }

        Token op = peek();
        if (op.type() != TokenType.OPERATOR) {
            throw error("Expected a comparison (>, <, >=, <=, = or !=) after '" + var.text() + "'");
        }
        next();
        ComparisonExpression.Operator operator = ComparisonExpression.Operator.of(op.text());
        if (operator == null) {
            throw new PolicyParseException("Unknown operator '" + op.text() + "'", op.pos());
        }

        Token value = peek();
        ValueExpression literal;
        if (type == PolicyVocabulary.VariableType.NUMBER) {
            if (value.type() != TokenType.NUMBER) {
                throw error("'" + var.text() + "' is a number, so compare it with a number");
            }
            literal = new LiteralValue(Double.parseDouble(value.text()));
        } else {
            if (value.type() != TokenType.STRING) {
                throw error("'" + var.text() + "' is text, so compare it with a quoted text");
            }
            if (operator != ComparisonExpression.Operator.EQ && operator != ComparisonExpression.Operator.NE) {
                throw new PolicyParseException("Text can only be compared with = or !=", op.pos());
            }
            Set<String> allowed = vocabulary.allowedTexts(var.text());
            if (allowed != null && !allowed.contains(value.text().toUpperCase())) {
                throw new PolicyParseException("'" + value.text() + "' is not valid for '" + var.text()
                        + "'. Use one of: " + String.join(", ", allowed), value.pos());
            }
            literal = new LiteralValue(value.text().toUpperCase());
        }
        next();
        return new ComparisonExpression(new VariableValue(var.text()), operator, literal);
    }

    private PolicyAction action() {
        Token t = peek();
        if (t.type() == TokenType.FINE) {
            next();
            Token amount = peek();
            if (amount.type() != TokenType.NUMBER || amount.text().contains(".") || amount.text().startsWith("-")) {
                throw error("FINE needs a whole number, e.g. FINE 5000");
            }
            long value;
            try {
                value = Long.parseLong(amount.text());
            } catch (NumberFormatException e) {
                throw new PolicyParseException("That fine amount is too large", amount.pos());
            }
            if (value < 1 || value > MAX_FINE) {
                throw new PolicyParseException("A fine must be between 1 and " + MAX_FINE, amount.pos());
            }
            next();
            return new FineAction(value);
        }
        if (t.type() == TokenType.ALERT) {
            next();
            Token message = peek();
            if (message.type() != TokenType.STRING) {
                throw error("ALERT needs a quoted message, e.g. ALERT \"Too loud\"");
            }
            if (message.text().isBlank() || message.text().length() > MAX_ALERT_LENGTH) {
                throw new PolicyParseException("An alert message must be 1 to " + MAX_ALERT_LENGTH + " characters", message.pos());
            }
            next();
            return new AlertAction(message.text());
        }
        throw error("Expected FINE or ALERT after THEN");
    }

    // ---------------------------------------------------------------- helpers

    private void enter() {
        if (++depth > MAX_DEPTH) {
            throw error("The policy is nested too deeply (limit " + MAX_DEPTH + ")");
        }
    }

    private String sample() {
        return vocabulary.names().iterator().next();
    }

    private Token peek() {
        return tokens.get(index);
    }

    private Token next() {
        return tokens.get(index++);
    }

    private void expect(TokenType type, String message) {
        if (peek().type() != type) {
            throw error(message);
        }
        next();
    }

    private PolicyParseException error(String message) {
        Token t = peek();
        String found = t.type() == TokenType.END ? "the end of the policy" : "'" + t.text() + "'";
        return new PolicyParseException(message + " (found " + found + ")", t.pos());
    }
}
