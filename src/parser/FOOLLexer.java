// Generated from /Users/civo/IdeaProjects/fool/src/parser/FOOL.g4 by ANTLR 4.7
package parser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FOOLLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		SEMIC=1, COLON=2, COMMA=3, EQ=4, LEQ=5, GEQ=6, ASM=7, PLUS=8, MINUS=9, 
		TIMES=10, DIV=11, TRUE=12, FALSE=13, LPAR=14, RPAR=15, CLPAR=16, CRPAR=17, 
		IF=18, THEN=19, ELSE=20, LET=21, IN=22, VAR=23, FUN=24, INT=25, BOOL=26, 
		CLASS=27, IMPLEMENTS=28, THIS=29, NEW=30, DOT=31, INTEGER=32, ID=33, WS=34, 
		LINECOMENTS=35, BLOCKCOMENTS=36, ERR=37;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"SEMIC", "COLON", "COMMA", "EQ", "LEQ", "GEQ", "ASM", "PLUS", "MINUS", 
		"TIMES", "DIV", "TRUE", "FALSE", "LPAR", "RPAR", "CLPAR", "CRPAR", "IF", 
		"THEN", "ELSE", "LET", "IN", "VAR", "FUN", "INT", "BOOL", "CLASS", "IMPLEMENTS", 
		"THIS", "NEW", "DOT", "DIGIT", "INTEGER", "CHAR", "ID", "WS", "LINECOMENTS", 
		"BLOCKCOMENTS", "ERR"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "';'", "':'", "','", "'=='", null, null, "'='", "'+'", "'-'", "'*'", 
		"'/'", "'true'", "'false'", "'('", "')'", "'{'", "'}'", "'if'", "'then'", 
		"'else'", "'let'", "'in'", "'var'", "'fun'", "'int'", "'bool'", "'class'", 
		"'implements'", "'this'", "'new'", "'.'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "SEMIC", "COLON", "COMMA", "EQ", "LEQ", "GEQ", "ASM", "PLUS", "MINUS", 
		"TIMES", "DIV", "TRUE", "FALSE", "LPAR", "RPAR", "CLPAR", "CRPAR", "IF", 
		"THEN", "ELSE", "LET", "IN", "VAR", "FUN", "INT", "BOOL", "CLASS", "IMPLEMENTS", 
		"THIS", "NEW", "DOT", "INTEGER", "ID", "WS", "LINECOMENTS", "BLOCKCOMENTS", 
		"ERR"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
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

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	   //there is a much better way to do this, check the ANTLR guide
	   //I will leave it like this for now just becasue it is quick
	   //but it doesn't work well
	   public int lexicalErrors=0;


	public FOOLLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "FOOL.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 38:
			ERR_action((RuleContext)_localctx, actionIndex);
			break;
		}
	}
	private void ERR_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0:
			 System.out.println("Invalid char: "+ getText()); lexicalErrors++; 
			break;
		}
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\'\u00fc\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\3\2\3\2\3\3\3\3\3\4"+
		"\3\4\3\5\3\5\3\5\3\6\3\6\3\6\3\6\5\6_\n\6\3\7\3\7\3\7\3\7\5\7e\n\7\3\b"+
		"\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3"+
		"\16\3\16\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3"+
		"\23\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3"+
		"\26\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\32\3\32\3"+
		"\32\3\32\3\33\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\34\3\34\3\35\3"+
		"\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\36\3\36\3\36\3\36\3"+
		"\36\3\37\3\37\3\37\3\37\3 \3 \3!\3!\3\"\6\"\u00c8\n\"\r\"\16\"\u00c9\3"+
		"#\3#\3$\3$\3$\7$\u00d1\n$\f$\16$\u00d4\13$\3%\3%\3%\3%\3&\3&\3&\3&\7&"+
		"\u00de\n&\f&\16&\u00e1\13&\3&\3&\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\7"+
		"\'\u00ee\n\'\f\'\16\'\u00f1\13\'\3\'\3\'\3\'\3\'\3\'\3(\3(\3(\3(\3(\2"+
		"\2)\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35"+
		"\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36"+
		";\37= ?!A\2C\"E\2G#I$K%M&O\'\3\2\b\4\2C\\c|\5\2\13\f\17\17\"\"\4\2\f\f"+
		"\17\17\4\2,,\61\61\3\2,,\3\2\61\61\2\u0103\2\3\3\2\2\2\2\5\3\2\2\2\2\7"+
		"\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2"+
		"\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2"+
		"\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2"+
		"\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2"+
		"\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2"+
		"\2C\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\3Q"+
		"\3\2\2\2\5S\3\2\2\2\7U\3\2\2\2\tW\3\2\2\2\13^\3\2\2\2\rd\3\2\2\2\17f\3"+
		"\2\2\2\21h\3\2\2\2\23j\3\2\2\2\25l\3\2\2\2\27n\3\2\2\2\31p\3\2\2\2\33"+
		"u\3\2\2\2\35{\3\2\2\2\37}\3\2\2\2!\177\3\2\2\2#\u0081\3\2\2\2%\u0083\3"+
		"\2\2\2\'\u0086\3\2\2\2)\u008b\3\2\2\2+\u0090\3\2\2\2-\u0094\3\2\2\2/\u0097"+
		"\3\2\2\2\61\u009b\3\2\2\2\63\u009f\3\2\2\2\65\u00a3\3\2\2\2\67\u00a8\3"+
		"\2\2\29\u00ae\3\2\2\2;\u00b9\3\2\2\2=\u00be\3\2\2\2?\u00c2\3\2\2\2A\u00c4"+
		"\3\2\2\2C\u00c7\3\2\2\2E\u00cb\3\2\2\2G\u00cd\3\2\2\2I\u00d5\3\2\2\2K"+
		"\u00d9\3\2\2\2M\u00e4\3\2\2\2O\u00f7\3\2\2\2QR\7=\2\2R\4\3\2\2\2ST\7<"+
		"\2\2T\6\3\2\2\2UV\7.\2\2V\b\3\2\2\2WX\7?\2\2XY\7?\2\2Y\n\3\2\2\2Z[\7>"+
		"\2\2[_\7?\2\2\\]\7?\2\2]_\7>\2\2^Z\3\2\2\2^\\\3\2\2\2_\f\3\2\2\2`a\7@"+
		"\2\2ae\7?\2\2bc\7?\2\2ce\7@\2\2d`\3\2\2\2db\3\2\2\2e\16\3\2\2\2fg\7?\2"+
		"\2g\20\3\2\2\2hi\7-\2\2i\22\3\2\2\2jk\7/\2\2k\24\3\2\2\2lm\7,\2\2m\26"+
		"\3\2\2\2no\7\61\2\2o\30\3\2\2\2pq\7v\2\2qr\7t\2\2rs\7w\2\2st\7g\2\2t\32"+
		"\3\2\2\2uv\7h\2\2vw\7c\2\2wx\7n\2\2xy\7u\2\2yz\7g\2\2z\34\3\2\2\2{|\7"+
		"*\2\2|\36\3\2\2\2}~\7+\2\2~ \3\2\2\2\177\u0080\7}\2\2\u0080\"\3\2\2\2"+
		"\u0081\u0082\7\177\2\2\u0082$\3\2\2\2\u0083\u0084\7k\2\2\u0084\u0085\7"+
		"h\2\2\u0085&\3\2\2\2\u0086\u0087\7v\2\2\u0087\u0088\7j\2\2\u0088\u0089"+
		"\7g\2\2\u0089\u008a\7p\2\2\u008a(\3\2\2\2\u008b\u008c\7g\2\2\u008c\u008d"+
		"\7n\2\2\u008d\u008e\7u\2\2\u008e\u008f\7g\2\2\u008f*\3\2\2\2\u0090\u0091"+
		"\7n\2\2\u0091\u0092\7g\2\2\u0092\u0093\7v\2\2\u0093,\3\2\2\2\u0094\u0095"+
		"\7k\2\2\u0095\u0096\7p\2\2\u0096.\3\2\2\2\u0097\u0098\7x\2\2\u0098\u0099"+
		"\7c\2\2\u0099\u009a\7t\2\2\u009a\60\3\2\2\2\u009b\u009c\7h\2\2\u009c\u009d"+
		"\7w\2\2\u009d\u009e\7p\2\2\u009e\62\3\2\2\2\u009f\u00a0\7k\2\2\u00a0\u00a1"+
		"\7p\2\2\u00a1\u00a2\7v\2\2\u00a2\64\3\2\2\2\u00a3\u00a4\7d\2\2\u00a4\u00a5"+
		"\7q\2\2\u00a5\u00a6\7q\2\2\u00a6\u00a7\7n\2\2\u00a7\66\3\2\2\2\u00a8\u00a9"+
		"\7e\2\2\u00a9\u00aa\7n\2\2\u00aa\u00ab\7c\2\2\u00ab\u00ac\7u\2\2\u00ac"+
		"\u00ad\7u\2\2\u00ad8\3\2\2\2\u00ae\u00af\7k\2\2\u00af\u00b0\7o\2\2\u00b0"+
		"\u00b1\7r\2\2\u00b1\u00b2\7n\2\2\u00b2\u00b3\7g\2\2\u00b3\u00b4\7o\2\2"+
		"\u00b4\u00b5\7g\2\2\u00b5\u00b6\7p\2\2\u00b6\u00b7\7v\2\2\u00b7\u00b8"+
		"\7u\2\2\u00b8:\3\2\2\2\u00b9\u00ba\7v\2\2\u00ba\u00bb\7j\2\2\u00bb\u00bc"+
		"\7k\2\2\u00bc\u00bd\7u\2\2\u00bd<\3\2\2\2\u00be\u00bf\7p\2\2\u00bf\u00c0"+
		"\7g\2\2\u00c0\u00c1\7y\2\2\u00c1>\3\2\2\2\u00c2\u00c3\7\60\2\2\u00c3@"+
		"\3\2\2\2\u00c4\u00c5\4\62;\2\u00c5B\3\2\2\2\u00c6\u00c8\5A!\2\u00c7\u00c6"+
		"\3\2\2\2\u00c8\u00c9\3\2\2\2\u00c9\u00c7\3\2\2\2\u00c9\u00ca\3\2\2\2\u00ca"+
		"D\3\2\2\2\u00cb\u00cc\t\2\2\2\u00ccF\3\2\2\2\u00cd\u00d2\5E#\2\u00ce\u00d1"+
		"\5E#\2\u00cf\u00d1\5A!\2\u00d0\u00ce\3\2\2\2\u00d0\u00cf\3\2\2\2\u00d1"+
		"\u00d4\3\2\2\2\u00d2\u00d0\3\2\2\2\u00d2\u00d3\3\2\2\2\u00d3H\3\2\2\2"+
		"\u00d4\u00d2\3\2\2\2\u00d5\u00d6\t\3\2\2\u00d6\u00d7\3\2\2\2\u00d7\u00d8"+
		"\b%\2\2\u00d8J\3\2\2\2\u00d9\u00da\7\61\2\2\u00da\u00db\7\61\2\2\u00db"+
		"\u00df\3\2\2\2\u00dc\u00de\n\4\2\2\u00dd\u00dc\3\2\2\2\u00de\u00e1\3\2"+
		"\2\2\u00df\u00dd\3\2\2\2\u00df\u00e0\3\2\2\2\u00e0\u00e2\3\2\2\2\u00e1"+
		"\u00df\3\2\2\2\u00e2\u00e3\b&\2\2\u00e3L\3\2\2\2\u00e4\u00e5\7\61\2\2"+
		"\u00e5\u00e6\7,\2\2\u00e6\u00ef\3\2\2\2\u00e7\u00ee\n\5\2\2\u00e8\u00e9"+
		"\7\61\2\2\u00e9\u00ee\n\6\2\2\u00ea\u00eb\7,\2\2\u00eb\u00ee\n\7\2\2\u00ec"+
		"\u00ee\5M\'\2\u00ed\u00e7\3\2\2\2\u00ed\u00e8\3\2\2\2\u00ed\u00ea\3\2"+
		"\2\2\u00ed\u00ec\3\2\2\2\u00ee\u00f1\3\2\2\2\u00ef\u00ed\3\2\2\2\u00ef"+
		"\u00f0\3\2\2\2\u00f0\u00f2\3\2\2\2\u00f1\u00ef\3\2\2\2\u00f2\u00f3\7,"+
		"\2\2\u00f3\u00f4\7\61\2\2\u00f4\u00f5\3\2\2\2\u00f5\u00f6\b\'\2\2\u00f6"+
		"N\3\2\2\2\u00f7\u00f8\13\2\2\2\u00f8\u00f9\b(\3\2\u00f9\u00fa\3\2\2\2"+
		"\u00fa\u00fb\b(\4\2\u00fbP\3\2\2\2\13\2^d\u00c9\u00d0\u00d2\u00df\u00ed"+
		"\u00ef\5\b\2\2\3(\2\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}