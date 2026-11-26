package json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonParser {

    private static final int MAX_DEPTH = 64;
    private static final String BOM = "\uFEFF";

    private final String text;
    private int pos = 0;

    private JsonParser(String text) {
        this.text = text;
    }

    public static Object parse(String json) {
        if (json == null) {
            throw new JsonParseException("JSON tidak boleh null");
        }
        String clean = json.startsWith(BOM) ? json.substring(1) : json;
        JsonParser parser = new JsonParser(clean);
        parser.skipWhitespace();
        Object value = parser.readValue(0);
        parser.skipWhitespace();
        if (parser.pos < parser.text.length()) {
            throw parser.error("Ada karakter tersisa setelah JSON selesai");
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object value = parse(json);
        if (!(value instanceof Map)) {
            throw new JsonParseException("JSON harus berupa object { ... }");
        }
        return (Map<String, Object>) value;
    }

    private Object readValue(int depth) {
        if (depth > MAX_DEPTH) {
            throw error("JSON terlalu dalam (maks " + MAX_DEPTH + " tingkat)");
        }
        char c = peek();
        switch (c) {
            case '{':
                return readObject(depth);
            case '[':
                return readArray(depth);
            case '"':
                return readString();
            case 't':
                readLiteral("true");
                return Boolean.TRUE;
            case 'f':
                readLiteral("false");
                return Boolean.FALSE;
            case 'n':
                readLiteral("null");
                return null;
            default:
                if (c == '-' || isDigit(c)) {
                    return readNumber();
                }
                throw error("Karakter tidak dikenal '" + c + "'");
        }
    }

    private Map<String, Object> readObject(int depth) {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++;
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return map;
        }
        while (true) {
            skipWhitespace();
            if (peek() != '"') {
                throw error("Nama field harus berupa string");
            }
            String key = readString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            map.put(key, readValue(depth + 1));
            skipWhitespace();
            char c = peek();
            if (c == '}') {
                pos++;
                return map;
            }
            if (c != ',') {
                throw error("Diharapkan ',' atau '}'");
            }
            pos++;
        }
    }

    private List<Object> readArray(int depth) {
        List<Object> list = new ArrayList<>();
        pos++;
        skipWhitespace();
        if (peek() == ']') {
            pos++;
            return list;
        }
        while (true) {
            skipWhitespace();
            list.add(readValue(depth + 1));
            skipWhitespace();
            char c = peek();
            if (c == ']') {
                pos++;
                return list;
            }
            if (c != ',') {
                throw error("Diharapkan ',' atau ']'");
            }
            pos++;
        }
    }

    private String readString() {
        pos++;
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= text.length()) {
                throw error("String tidak ditutup");
            }
            char c = text.charAt(pos++);
            if (c == '"') {
                return sb.toString();
            }
            if (c < 0x20) {
                throw error("Karakter kontrol tidak boleh ada di dalam string");
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (pos >= text.length()) {
                throw error("String tidak ditutup");
            }
            char e = text.charAt(pos++);
            switch (e) {
                case '"':  sb.append('"');  break;
                case '\\': sb.append('\\'); break;
                case '/':  sb.append('/');  break;
                case 'b':  sb.append('\b'); break;
                case 'f':  sb.append('\f'); break;
                case 'n':  sb.append('\n'); break;
                case 'r':  sb.append('\r'); break;
                case 't':  sb.append('\t'); break;
                case 'u':  sb.append(readUnicode()); break;
                default:
                    throw error("Escape tidak valid: \\" + e);
            }
        }
    }

    private char readUnicode() {
        if (pos + 4 > text.length()) {
            throw error("Escape \\u tidak lengkap");
        }
        String hex = text.substring(pos, pos + 4);
        if (!hex.matches("[0-9a-fA-F]{4}")) {
            throw error("Escape \\u tidak valid: \\u" + hex);
        }
        pos += 4;
        return (char) Integer.parseInt(hex, 16);
    }

    private BigDecimal readNumber() {
        int start = pos;
        if (peek() == '-') {
            pos++;
        }
        if (pos < text.length() && text.charAt(pos) == '0') {
            pos++;
        } else {
            requireDigits();
        }
        if (pos < text.length() && text.charAt(pos) == '.') {
            pos++;
            requireDigits();
        }
        if (pos < text.length() && (text.charAt(pos) == 'e' || text.charAt(pos) == 'E')) {
            pos++;
            if (pos < text.length() && (text.charAt(pos) == '+' || text.charAt(pos) == '-')) {
                pos++;
            }
            requireDigits();
        }
        return new BigDecimal(text.substring(start, pos));
    }

    private void requireDigits() {
        if (pos >= text.length() || !isDigit(text.charAt(pos))) {
            throw error("Angka tidak valid");
        }
        while (pos < text.length() && isDigit(text.charAt(pos))) {
            pos++;
        }
    }

    private void readLiteral(String word) {
        if (!text.startsWith(word, pos)) {
            throw error("Diharapkan '" + word + "'");
        }
        pos += word.length();
    }

    private char peek() {
        if (pos >= text.length()) {
            throw error("JSON berakhir tiba-tiba");
        }
        return text.charAt(pos);
    }

    private void expect(char expected) {
        if (peek() != expected) {
            throw error("Diharapkan '" + expected + "'");
        }
        pos++;
    }

    private void skipWhitespace() {
        while (pos < text.length()) {
            char c = text.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                pos++;
            } else {
                break;
            }
        }
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private JsonParseException error(String message) {
        return new JsonParseException(message + " (posisi " + pos + ")");
    }
}
