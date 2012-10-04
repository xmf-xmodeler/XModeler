package XOS;

public class TChannel extends XInputStream {

    public static final int EOF           = 1;

    public static final int STRING        = 5;

    public static final int NAME          = 6;

    public static final int INT           = 7;

    public static final int SPECIAL       = 8;

    public static final int SYMBOL        = 13;

    private int             type;

    private boolean         error         = false;

    private String          errorMessage;

    // The chars are recorded as they are read.

    private StringBuffer    chars         = new StringBuffer();

    private int             lineCount     = 1;

    // Keep track of the position on the current line.

    private int             charCount     = 1;

    private int             backup        = 0;

    // CharCount from previous line. Needed for buffering.

    private int             cbuffer       = -1;

    private int             prevCharCount = 0;

    // Chars contributing to the current token are saved in tokenChars.
    // The token chars include whitespace.

    private StringBuffer    token         = new StringBuffer();

    private StringBuffer    rawChars      = new StringBuffer();

    private int             value         = 0;

    private XChannel        in;

    public TChannel(XChannel in) {
        this.in = in;
    }

    public void advanceCharCount(int c) {
        // Record the current position in terms of lines and chars.
        if (isNewLine(c)) {
            lineCount++;
            prevCharCount = charCount;
            charCount = 1;
        } else
            charCount++;
    }

    public int available() {
        if (cbuffer == -1)
            return in.available();
        else
            return in.available() + 1;
    }

    public void cbuffer(int c) {
        if (cbufferEmpty()) {
            cbuffer = c;
            if (isNewLine(c)) {
                lineCount--;
                charCount = prevCharCount;
            } else
                charCount--;
        } else
            throw new Error("XTStream.cbuffer: limit exceeded " + (char) c);
    }

    public boolean cbufferEmpty() {
        return cbuffer == -1;
    }

    public int charCount() {
        return charCount - backup;
    }

    public int charToInt(int c) {
        return c - '0';
    }

    public void close() {
        in.close();
    }

    public boolean eof() {
        return cbuffer == -1 && in.eof();
    }

    public boolean error() {
        return error;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public boolean isNameChar(int c) {
        return ('A' <= c && 'Z' >= c) || ('a' <= c && 'z' >= c) || isNumericChar(c) || c == '_';
    }

    public boolean isNewLine(int c) {
        return c == '\n';
    }

    public boolean isNumericChar(int c) {
        return '0' <= c && '9' >= c;
    }

    public int lineCount() {
        return lineCount;
    }

    public int peek() {

        // A TChannel may only produce tokens. Tokens safely cannot start with
        // white space. Peek is used to determine whether there is any input
        // waiting to be read. A TChannel can safely filter out leading whitespace
        // before returning to the caller.

        int c;
        while (isWhiteSpace(c = in.peek()))
            in.read();
        return c;
    }

    public int read() {
        return in.read();
    }

    public boolean skipComment() {
        // At this stage we have read a '/' character. Peek at the next
        // character, if it is '/' or '*' then skip the comment otherwise
        // back up. Return true when a comment has been skipped.
        if (eof())
            return false;
        int c = readChar();
        switch (c) {
        case '/':
            skipLine();
            return true;
        case '*':
            skipCommentBlock();
            return true;
        default:
            cbuffer(c);
            return false;
        }
    }

    public void skipCommentBlock() {
        while (!eof() && !isEndOfCommentBlock()) {
            readChar();
        }
    }

    public void skipLine() {
        int c = 0;
        while (!eof() && ((c = readChar()) != '\n'))
            ;
    }

    public void skipWhiteSpace() {
        boolean whiteSpace = true;
        int c = readChar();
        while (!eof() && whiteSpace)
            if (isWhiteSpace(c)) {
                rawChars.append((char) c);
                c = readChar();
            } else
                whiteSpace = false;
        cbuffer(c);
    }

    public boolean isEndOfCommentBlock() {
        int c;
        if (!eof())
            if ((c = readChar()) == '*')
                if (!eof())
                    if ((c = readChar()) == '/') {
                        return true;
                    } else {
                        cbuffer(c);
                        return false;
                    }
                else
                    return false;
            else {
                cbuffer(c);
                return false;
            }
        else
            return false;
    }

    public boolean isStartNameChar(int c) {
        return ('A' <= c && 'Z' >= c) || ('a' <= c && 'z' >= c);
    }

    public boolean isSpecialTerminator(int c) {
        return isWhiteSpace(c) || isNumericChar(c) || isNameChar(c) || (c == '\"') || (c == '(') || (c == ')') || (c == '{') || (c == '}')
                || (c == ';') || (c == ',') || (c == '.');
    }

    public boolean isWhiteSpace(int c) {
        switch (c) {
        case ' ':
        case '\n':
        case '\r':
        case '\t':
            return true;
        default:
            return false;
        }
    }

    public void name(int c) {
        token.append((char) c);
        rawChars.append((char) c);
        while (!eof() && isNameChar(c = readChar())) {
            token.append((char) c);
            rawChars.append((char) c);
        }
        if (!isNameChar(c) && !isWhiteSpace(c))
            cbuffer(c);
        if (isWhiteSpace(c))
            rawChars.append((char) c);
        type = NAME;
    }

    public void nextToken() {
        skipWhiteSpace();
        int c;
        if (eof()) {
            type = EOF;
            return;
        }
        switch (c = readChar()) {
        case '(':
            special("(");
            break;
        case ')':
            special(")");
            break;
        case '{':
            special("{");
            break;
        case '}':
            special("}");
            break;
        case '\"':
            string();
            break;
        case ';':
            special(";");
            break;
        case '.':
            special(".");
            break;
        case ',':
            special(",");
            break;
        case '\'':
            symbol();
            break;
        case '/':
            if (skipComment())
                nextToken();
            else
                special('/');
            break;
        case '>':
            rightChevron();
            break;
        case ' ':
        case '\n':
        case '\r':
        case '\t':
            nextToken();
            break;
        default:
            if (isStartNameChar(c))
                name(c);
            else if (isNumericChar(c))
                number(c);
            else
                special(c);
        }
        
        // Don't leave trailing white space in the input stream since this
        // will fool the tokenizer into thinking that there is a following
        // token (ready returns true when a single char is available).
        
        boolean trailingWhiteSpace = true;
        while (available() > 0 && trailingWhiteSpace) {
            c = readChar();
            if (!isWhiteSpace(c)) {
                cbuffer(c);
                trailingWhiteSpace = false;
            } else rawChars.append((char)c);
        }
    }

    public void number(int c) {
        backup = 1;
        value = charToInt(c);
        rawChars.append((char) c);
        while (!eof() && isNumericChar(c = readChar())) {
            value = (value * 10) + charToInt(c);
            rawChars.append((char) c);
            backup++;
        }
        if (!isNumericChar(c) && !isWhiteSpace(c)) {
            cbuffer(c);
            backup++;
        }
        if (isWhiteSpace(c))
            rawChars.append((char) c);
        type = INT;
    }

    public int posValue() {
        if (cbufferEmpty())
            return chars.length();
        else
            return chars.length() - 1;
    }

    public int protectedStringChar() {
        if (eof()) {
            error = true;
            errorMessage = "EOF encountered after \\ in string.";
            return 0;
        } else {
            int c = readChar();
            switch (c) {
            case 'n':
                return '\n';
            case 't':
                return '\t';
            case 'r':
                return '\r';
            default:
                return c;
            }
        }
    }

    public String rawChars() {
        return rawChars.toString();
    }

    public int readChar() {
        if (cbufferEmpty()) {
            int c = in.read();
            chars.append((char) c);
            advanceCharCount(c);
            return c;
        } else {
            int i = cbuffer;
            cbuffer = -1;
            advanceCharCount(i);
            return i;
        }
    }

    public boolean ready() {
        return cbuffer != -1 || in.ready();
    }

    public void reset() {
        
        // This is called when we want to reset before reading the next token.
        // It does not lose any buffered character and does not lose any consumed
        // input characters in 'chars'.
        
        error = false;
        backup = 0;
        token.setLength(0);
        rawChars.setLength(0);
    }
    
    public void resetToInitialState() {
        
        // Called when we want to lose any saved information in the channel.
        
        reset();
        chars.setLength(0);
        cbuffer = -1;
        in.resetToInitialState();
    }

    public void rightChevron() {
        if (eof())
            special(">");
        else {
            int c = readChar();
            if (c == '=')
                special(">=");
            else {
                if (!isWhiteSpace(c))
                    cbuffer(c);
                special(">");
            }
        }
    }

    public void string() {
        int c = -1;
        while (!eof() && ((c = readChar()) != '\"')) {
            if (c == '\\')
                c = protectedStringChar();
            token.append((char) c);
            rawChars.append((char) c);
        }
        if (c == '\"')
            type = STRING;
        else {
            error = true;
            errorMessage = "BasicTokenChannel.string: EOF in string token (" + token + ")";
        }
    }

    public void semi(String string) {
        token.append(';');
        rawChars.append(';');
        type = SPECIAL;
    }

    public void special(int c) {
        token.append((char) c);
        rawChars.append((char) c);
        while (!eof() && !isSpecialTerminator(c = readChar())) {
            token.append((char) c);
            rawChars.append((char) c);
        }
        if (!eof() && !isWhiteSpace(c))
            cbuffer(c);
        if (isWhiteSpace(c))
            rawChars.append((char) c);
        type = SPECIAL;
    }

    public void special(String string) {
        token.append(string);
        rawChars.append(string);
        type = SPECIAL;
    }

    public void symbol() {
        int c = -1;
        while (!eof() && ((c = readChar()) != '\'')) {
            token.append((char) c);
            rawChars.append((char) c);
        }
        if (c == '\'')
            type = SYMBOL;
        else {
            error = true;
            errorMessage = "BasicTokenChannel.string: EOF in symbol token (" + token + ")";
        }
    }

    public String textTo(int index) {
        if (index > chars.length())
            throw new Error("BasicTokenChannel.textTo: index out of bounds " + index);
        return chars.substring(index);
    }

    public String token() {
        return token.toString();
    }

    public String tokenChars() {
        if (cbufferEmpty())
            return token.toString();
        else
            switch (token.toString().charAt(token.length() - 1)) {
            case ' ':
            case '\n':
            case '\r':
                return token.toString().substring(0, token.length());
            default:
                return token.toString().substring(0, token.length() - 1);
            }
    }

    public int type() {
        return type;
    }

    public int value() {
        return value;
    }

}