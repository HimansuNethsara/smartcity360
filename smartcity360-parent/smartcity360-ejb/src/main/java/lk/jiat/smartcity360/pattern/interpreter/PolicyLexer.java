package lk.jiat.smartcity360.pattern.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PolicyLexer {

    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "IF", TokenType.IF, "THEN", TokenType.THEN, "AND", TokenType.AND, "OR", TokenType.OR,
            "NOT", TokenType.NOT, "FINE", TokenType.FINE, "ALERT", TokenType.ALERT);

    private PolicyLexer() {
    }

    public static List<Token> tokenize(String src) {
        List<Token> out = new ArrayList<>();
        int i = 0;
        int n = src.length();
        while (i < n) {
            char c = src.charAt(i);
            int pos = i + 1;
            if (Character.isWhitespace(c)) {
                i++;
            } else if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < n && (Character.isLetterOrDigit(src.charAt(i)) || src.charAt(i) == '_')) {
                    i++;
                }
                String word = src.substring(start, i);
                TokenType kw = KEYWORDS.get(word.toUpperCase());
                out.add(kw != null ? new Token(kw, word.toUpperCase(), pos) : new Token(TokenType.IDENTIFIER, word.toLowerCase(), pos));
            } else if (Character.isDigit(c) || (c == '-' && i + 1 < n && Character.isDigit(src.charAt(i + 1)))) {
                int start = i;
                i++;
                boolean dot = false;
                while (i < n && (Character.isDigit(src.charAt(i)) || (!dot && src.charAt(i) == '.'
                        && i + 1 < n && Character.isDigit(src.charAt(i + 1))))) {
                    dot |= src.charAt(i) == '.';
                    i++;
                }
                out.add(new Token(TokenType.NUMBER, src.substring(start, i), pos));
            } else if (c == '"') {
                int end = src.indexOf('"', i + 1);
                if (end < 0) {
                    throw new PolicyParseException("This text is missing its closing quote", pos);
                }
                out.add(new Token(TokenType.STRING, src.substring(i + 1, end), pos));
                i = end + 1;
            } else if (c == '(') {
                out.add(new Token(TokenType.LPAREN, "(", pos));
                i++;
            } else if (c == ')') {
                out.add(new Token(TokenType.RPAREN, ")", pos));
                i++;
            } else if (c == '>' || c == '<' || c == '=' || c == '!') {
                String two = i + 1 < n ? src.substring(i, i + 2) : "";
                if (two.equals(">=") || two.equals("<=") || two.equals("!=")) {
                    out.add(new Token(TokenType.OPERATOR, two, pos));
                    i += 2;
                } else if (c == '!') {
                    throw new PolicyParseException("'!' must be followed by '=' (write != for \"not equal\")", pos);
                } else {
                    out.add(new Token(TokenType.OPERATOR, String.valueOf(c), pos));
                    i++;
                }
            } else {
                throw new PolicyParseException("Unexpected character '" + c + "'", pos);
            }
        }
        out.add(new Token(TokenType.END, "", n + 1));
        return out;
    }
}
