// Generated from C:/Users/Alberto/workspace/fool/src/parser\FOOL.g4 by ANTLR 4.7
package grammar;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FOOLLexer extends Lexer {
    public static final int
            SEMIC = 1, COLON = 2, COMMA = 3, EQ = 4, LEQ = 5, GEQ = 6, AND = 7, OR = 8, NOT = 9, ASM = 10,
            PLUS = 11, MINUS = 12, TIMES = 13, DIV = 14, TRUE = 15, FALSE = 16, LPAR = 17, RPAR = 18,
            CLPAR = 19, CRPAR = 20, IF = 21, THEN = 22, ELSE = 23, LET = 24, IN = 25, VAR = 26, FUN = 27,
            INT = 28, BOOL = 29, CLASS = 30, IMPLEMENTS = 31, THIS = 32, NEW = 33, DOT = 34, INTEGER = 35,
            ID = 36, WS = 37, LINECOMENTS = 38, BLOCKCOMENTS = 39, ERR = 40;
    public static final String[] ruleNames = {
            "SEMIC", "COLON", "COMMA", "EQ", "LEQ", "GEQ", "AND", "OR", "NOT", "ASM",
            "PLUS", "MINUS", "TIMES", "DIV", "TRUE", "FALSE", "LPAR", "RPAR", "CLPAR",
            "CRPAR", "IF", "THEN", "ELSE", "LET", "IN", "VAR", "FUN", "INT", "BOOL",
            "CLASS", "IMPLEMENTS", "THIS", "NEW", "DOT", "DIGIT", "INTEGER", "CHAR",
            "ID", "WS", "LINECOMENTS", "BLOCKCOMENTS", "ERR"
    };
    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2*\u010a\b\1\4\2\t" +
                    "\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13" +
                    "\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22" +
                    "\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31" +
                    "\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!" +
                    "\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\3" +
                    "\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\6\3\6\5\6e\n\6\3\7\3\7\3" +
                    "\7\3\7\5\7k\n\7\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r" +
                    "\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21" +
                    "\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\26\3\27" +
                    "\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\32" +
                    "\3\32\3\32\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\35\3\35\3\35\3\35" +
                    "\3\36\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3\37\3 \3 \3 \3 \3" +
                    " \3 \3 \3 \3 \3 \3 \3!\3!\3!\3!\3!\3\"\3\"\3\"\3\"\3#\3#\3$\3$\3%\6%\u00d6" +
                    "\n%\r%\16%\u00d7\3&\3&\3\'\3\'\3\'\7\'\u00df\n\'\f\'\16\'\u00e2\13\'\3" +
                    "(\3(\3(\3(\3)\3)\3)\3)\7)\u00ec\n)\f)\16)\u00ef\13)\3)\3)\3*\3*\3*\3*" +
                    "\3*\3*\3*\3*\3*\7*\u00fc\n*\f*\16*\u00ff\13*\3*\3*\3*\3*\3*\3+\3+\3+\3" +
                    "+\3+\2\2,\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33" +
                    "\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67" +
                    "\359\36;\37= ?!A\"C#E$G\2I%K\2M&O\'Q(S)U*\3\2\b\4\2C\\c|\5\2\13\f\17\17" +
                    "\"\"\4\2\f\f\17\17\4\2,,\61\61\3\2,,\3\2\61\61\2\u0111\2\3\3\2\2\2\2\5" +
                    "\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2" +
                    "\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33" +
                    "\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2" +
                    "\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2" +
                    "\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2" +
                    "\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2I\3\2\2\2\2M\3\2\2\2\2O" +
                    "\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\3W\3\2\2\2\5Y\3\2\2\2\7[\3\2" +
                    "\2\2\t]\3\2\2\2\13d\3\2\2\2\rj\3\2\2\2\17l\3\2\2\2\21o\3\2\2\2\23r\3\2" +
                    "\2\2\25t\3\2\2\2\27v\3\2\2\2\31x\3\2\2\2\33z\3\2\2\2\35|\3\2\2\2\37~\3" +
                    "\2\2\2!\u0083\3\2\2\2#\u0089\3\2\2\2%\u008b\3\2\2\2\'\u008d\3\2\2\2)\u008f" +
                    "\3\2\2\2+\u0091\3\2\2\2-\u0094\3\2\2\2/\u0099\3\2\2\2\61\u009e\3\2\2\2" +
                    "\63\u00a2\3\2\2\2\65\u00a5\3\2\2\2\67\u00a9\3\2\2\29\u00ad\3\2\2\2;\u00b1" +
                    "\3\2\2\2=\u00b6\3\2\2\2?\u00bc\3\2\2\2A\u00c7\3\2\2\2C\u00cc\3\2\2\2E" +
                    "\u00d0\3\2\2\2G\u00d2\3\2\2\2I\u00d5\3\2\2\2K\u00d9\3\2\2\2M\u00db\3\2" +
                    "\2\2O\u00e3\3\2\2\2Q\u00e7\3\2\2\2S\u00f2\3\2\2\2U\u0105\3\2\2\2WX\7=" +
                    "\2\2X\4\3\2\2\2YZ\7<\2\2Z\6\3\2\2\2[\\\7.\2\2\\\b\3\2\2\2]^\7?\2\2^_\7" +
                    "?\2\2_\n\3\2\2\2`a\7>\2\2ae\7?\2\2bc\7?\2\2ce\7>\2\2d`\3\2\2\2db\3\2\2" +
                    "\2e\f\3\2\2\2fg\7@\2\2gk\7?\2\2hi\7?\2\2ik\7@\2\2jf\3\2\2\2jh\3\2\2\2" +
                    "k\16\3\2\2\2lm\7(\2\2mn\7(\2\2n\20\3\2\2\2op\7~\2\2pq\7~\2\2q\22\3\2\2" +
                    "\2rs\7#\2\2s\24\3\2\2\2tu\7?\2\2u\26\3\2\2\2vw\7-\2\2w\30\3\2\2\2xy\7" +
                    "/\2\2y\32\3\2\2\2z{\7,\2\2{\34\3\2\2\2|}\7\61\2\2}\36\3\2\2\2~\177\7v" +
                    "\2\2\177\u0080\7t\2\2\u0080\u0081\7w\2\2\u0081\u0082\7g\2\2\u0082 \3\2" +
                    "\2\2\u0083\u0084\7h\2\2\u0084\u0085\7c\2\2\u0085\u0086\7n\2\2\u0086\u0087" +
                    "\7u\2\2\u0087\u0088\7g\2\2\u0088\"\3\2\2\2\u0089\u008a\7*\2\2\u008a$\3" +
                    "\2\2\2\u008b\u008c\7+\2\2\u008c&\3\2\2\2\u008d\u008e\7}\2\2\u008e(\3\2" +
                    "\2\2\u008f\u0090\7\177\2\2\u0090*\3\2\2\2\u0091\u0092\7k\2\2\u0092\u0093" +
                    "\7h\2\2\u0093,\3\2\2\2\u0094\u0095\7v\2\2\u0095\u0096\7j\2\2\u0096\u0097" +
                    "\7g\2\2\u0097\u0098\7p\2\2\u0098.\3\2\2\2\u0099\u009a\7g\2\2\u009a\u009b" +
                    "\7n\2\2\u009b\u009c\7u\2\2\u009c\u009d\7g\2\2\u009d\60\3\2\2\2\u009e\u009f" +
                    "\7n\2\2\u009f\u00a0\7g\2\2\u00a0\u00a1\7v\2\2\u00a1\62\3\2\2\2\u00a2\u00a3" +
                    "\7k\2\2\u00a3\u00a4\7p\2\2\u00a4\64\3\2\2\2\u00a5\u00a6\7x\2\2\u00a6\u00a7" +
                    "\7c\2\2\u00a7\u00a8\7t\2\2\u00a8\66\3\2\2\2\u00a9\u00aa\7h\2\2\u00aa\u00ab" +
                    "\7w\2\2\u00ab\u00ac\7p\2\2\u00ac8\3\2\2\2\u00ad\u00ae\7k\2\2\u00ae\u00af" +
                    "\7p\2\2\u00af\u00b0\7v\2\2\u00b0:\3\2\2\2\u00b1\u00b2\7d\2\2\u00b2\u00b3" +
                    "\7q\2\2\u00b3\u00b4\7q\2\2\u00b4\u00b5\7n\2\2\u00b5<\3\2\2\2\u00b6\u00b7" +
                    "\7e\2\2\u00b7\u00b8\7n\2\2\u00b8\u00b9\7c\2\2\u00b9\u00ba\7u\2\2\u00ba" +
                    "\u00bb\7u\2\2\u00bb>\3\2\2\2\u00bc\u00bd\7k\2\2\u00bd\u00be\7o\2\2\u00be" +
                    "\u00bf\7r\2\2\u00bf\u00c0\7n\2\2\u00c0\u00c1\7g\2\2\u00c1\u00c2\7o\2\2" +
                    "\u00c2\u00c3\7g\2\2\u00c3\u00c4\7p\2\2\u00c4\u00c5\7v\2\2\u00c5\u00c6" +
                    "\7u\2\2\u00c6@\3\2\2\2\u00c7\u00c8\7v\2\2\u00c8\u00c9\7j\2\2\u00c9\u00ca" +
                    "\7k\2\2\u00ca\u00cb\7u\2\2\u00cbB\3\2\2\2\u00cc\u00cd\7p\2\2\u00cd\u00ce" +
                    "\7g\2\2\u00ce\u00cf\7y\2\2\u00cfD\3\2\2\2\u00d0\u00d1\7\60\2\2\u00d1F" +
                    "\3\2\2\2\u00d2\u00d3\4\62;\2\u00d3H\3\2\2\2\u00d4\u00d6\5G$\2\u00d5\u00d4" +
                    "\3\2\2\2\u00d6\u00d7\3\2\2\2\u00d7\u00d5\3\2\2\2\u00d7\u00d8\3\2\2\2\u00d8" +
                    "J\3\2\2\2\u00d9\u00da\t\2\2\2\u00daL\3\2\2\2\u00db\u00e0\5K&\2\u00dc\u00df" +
                    "\5K&\2\u00dd\u00df\5G$\2\u00de\u00dc\3\2\2\2\u00de\u00dd\3\2\2\2\u00df" +
                    "\u00e2\3\2\2\2\u00e0\u00de\3\2\2\2\u00e0\u00e1\3\2\2\2\u00e1N\3\2\2\2" +
                    "\u00e2\u00e0\3\2\2\2\u00e3\u00e4\t\3\2\2\u00e4\u00e5\3\2\2\2\u00e5\u00e6" +
                    "\b(\2\2\u00e6P\3\2\2\2\u00e7\u00e8\7\61\2\2\u00e8\u00e9\7\61\2\2\u00e9" +
                    "\u00ed\3\2\2\2\u00ea\u00ec\n\4\2\2\u00eb\u00ea\3\2\2\2\u00ec\u00ef\3\2" +
                    "\2\2\u00ed\u00eb\3\2\2\2\u00ed\u00ee\3\2\2\2\u00ee\u00f0\3\2\2\2\u00ef" +
                    "\u00ed\3\2\2\2\u00f0\u00f1\b)\2\2\u00f1R\3\2\2\2\u00f2\u00f3\7\61\2\2" +
                    "\u00f3\u00f4\7,\2\2\u00f4\u00fd\3\2\2\2\u00f5\u00fc\n\5\2\2\u00f6\u00f7" +
                    "\7\61\2\2\u00f7\u00fc\n\6\2\2\u00f8\u00f9\7,\2\2\u00f9\u00fc\n\7\2\2\u00fa" +
                    "\u00fc\5S*\2\u00fb\u00f5\3\2\2\2\u00fb\u00f6\3\2\2\2\u00fb\u00f8\3\2\2" +
                    "\2\u00fb\u00fa\3\2\2\2\u00fc\u00ff\3\2\2\2\u00fd\u00fb\3\2\2\2\u00fd\u00fe" +
                    "\3\2\2\2\u00fe\u0100\3\2\2\2\u00ff\u00fd\3\2\2\2\u0100\u0101\7,\2\2\u0101" +
                    "\u0102\7\61\2\2\u0102\u0103\3\2\2\2\u0103\u0104\b*\2\2\u0104T\3\2\2\2" +
                    "\u0105\u0106\13\2\2\2\u0106\u0107\b+\3\2\u0107\u0108\3\2\2\2\u0108\u0109" +
                    "\b+\4\2\u0109V\3\2\2\2\13\2dj\u00d7\u00de\u00e0\u00ed\u00fb\u00fd\5\b" +
                    "\2\2\3+\2\2\3\2";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    private static final String[] _LITERAL_NAMES = {
            null, "';'", "':'", "','", "'=='", null, null, "'&&'", "'||'", "'!'",
            "'='", "'+'", "'-'", "'*'", "'/'", "'true'", "'false'", "'('", "')'",
            "'{'", "'}'", "'if'", "'then'", "'else'", "'let'", "'in'", "'var'", "'fun'",
            "'int'", "'bool'", "'class'", "'implements'", "'this'", "'new'", "'.'"
    };
    private static final String[] _SYMBOLIC_NAMES = {
            null, "SEMIC", "COLON", "COMMA", "EQ", "LEQ", "GEQ", "AND", "OR", "NOT",
            "ASM", "PLUS", "MINUS", "TIMES", "DIV", "TRUE", "FALSE", "LPAR", "RPAR",
            "CLPAR", "CRPAR", "IF", "THEN", "ELSE", "LET", "IN", "VAR", "FUN", "INT",
            "BOOL", "CLASS", "IMPLEMENTS", "THIS", "NEW", "DOT", "INTEGER", "ID",
            "WS", "LINECOMENTS", "BLOCKCOMENTS", "ERR"
    };
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);
    public static String[] channelNames = {
            "DEFAULT_TOKEN_CHANNEL", "HIDDEN"
    };
    public static String[] modeNames = {
            "DEFAULT_MODE"
    };

    static {
        RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION);
    }

    static {
        tokenNames = new String[_SYMBOLIC_NAMES.length];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = VOCABULARY.getLiteralName(i);
            if (tokenNames[i] == null) {
                tokenNames[i] = VOCABULARY.getSymbolicName(i);
            }

            if (tokenNames[i] == null) {
                tokenNames[i] = "<INVALID>";
            }
        }
    }

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }

    //there is a much better way to do this, check the ANTLR guide
    //I will leave it like this for now just becasue it is quick
    //but it doesn't work well
    public int lexicalErrors = 0;

    public FOOLLexer(CharStream input) {
        super(input);
        _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override

    public Vocabulary getVocabulary() {
        return VOCABULARY;
    }

    @Override
    public String getGrammarFileName() {
        return "FOOL.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public String[] getChannelNames() {
        return channelNames;
    }

    @Override
    public String[] getModeNames() {
        return modeNames;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    @Override
    public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
        switch (ruleIndex) {
            case 41:
                ERR_action((RuleContext) _localctx, actionIndex);
                break;
        }
    }

    private void ERR_action(RuleContext _localctx, int actionIndex) {
        switch (actionIndex) {
            case 0:
                System.out.println("Invalid char: " + getText());
                lexicalErrors++;
                break;
        }
    }
}