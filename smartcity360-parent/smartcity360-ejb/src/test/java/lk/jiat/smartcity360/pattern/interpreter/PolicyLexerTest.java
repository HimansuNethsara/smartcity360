package lk.jiat.smartcity360.pattern.interpreter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolicyLexerTest {

    private static List<TokenType> types(String src) {
        return PolicyLexer.tokenize(src).stream().map(Token::type).toList();
    }

    @Test
    @DisplayName("A simple policy becomes keyword, identifier, operator, number tokens ending in END")
    void simplePolicy() {
        assertEquals(List.of(TokenType.IF, TokenType.IDENTIFIER, TokenType.OPERATOR, TokenType.NUMBER,
                TokenType.THEN, TokenType.FINE, TokenType.NUMBER, TokenType.END),
                types("IF noise > 70 THEN FINE 500"));
    }

    @Test
    @DisplayName("Keywords are case-insensitive; variable names are lower-cased")
    void caseInsensitive() {
        List<Token> t = PolicyLexer.tokenize("if NOISE > 1 then alert \"x\"");
        assertEquals(TokenType.IF, t.get(0).type());
        assertEquals("noise", t.get(1).text());
        assertEquals(TokenType.THEN, t.get(4).type());
        assertEquals(TokenType.ALERT, t.get(5).type());
    }

    @Test
    @DisplayName("All comparison operators are recognised, including the two-character ones")
    void operators() {
        List<Token> t = PolicyLexer.tokenize("> < >= <= = !=");
        assertEquals(List.of(">", "<", ">=", "<=", "=", "!="),
                t.stream().filter(x -> x.type() == TokenType.OPERATOR).map(Token::text).toList());
    }

    @Test
    @DisplayName("Numbers may be negative or decimal; quoted text keeps its content")
    void numbersAndStrings() {
        List<Token> t = PolicyLexer.tokenize("-5 3.25 \"Too loud\"");
        assertEquals("-5", t.get(0).text());
        assertEquals("3.25", t.get(1).text());
        assertEquals(TokenType.STRING, t.get(2).type());
        assertEquals("Too loud", t.get(2).text());
    }

    @Test
    @DisplayName("Brackets are tokens")
    void brackets() {
        assertEquals(List.of(TokenType.LPAREN, TokenType.RPAREN, TokenType.END), types("()"));
    }

    @Test
    @DisplayName("Token positions are 1-based")
    void positions() {
        List<Token> t = PolicyLexer.tokenize("IF noise");
        assertEquals(1, t.get(0).pos());
        assertEquals(4, t.get(1).pos());
    }

    @Test
    @DisplayName("Bad input is rejected with a position")
    void badInput() {
        PolicyParseException open = assertThrows(PolicyParseException.class, () -> PolicyLexer.tokenize("\"never closed"));
        assertTrue(open.getMessage().contains("closing quote"));
        assertEquals(1, open.getPosition());

        PolicyParseException at = assertThrows(PolicyParseException.class, () -> PolicyLexer.tokenize("IF @"));
        assertTrue(at.getMessage().contains("Unexpected character '@'"));
        assertEquals(4, at.getPosition());

        assertThrows(PolicyParseException.class, () -> PolicyLexer.tokenize("noise ! 5"));
    }
}
