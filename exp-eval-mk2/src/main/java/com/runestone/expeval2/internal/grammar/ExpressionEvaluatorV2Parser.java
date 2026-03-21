// Generated from exp-eval-mk2/src/main/antlr4/com/runestone/expeval2/internal/grammar/ExpressionEvaluatorV2.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class ExpressionEvaluatorV2Parser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		IF=1, THEN=2, ELSE=3, ELSEIF=4, ENDIF=5, AND=6, OR=7, XOR=8, XNOR=9, NAND=10, 
		NOR=11, TRUE=12, FALSE=13, MULT=14, DIV=15, PLUS=16, MINUS=17, PERCENT=18, 
		MODULO=19, MODULUS=20, EXCLAMATION=21, EXPONENTIATION=22, ROOT=23, SQRT=24, 
		GT=25, GE=26, LT=27, LE=28, EQ=29, NEQ=30, NOT=31, NOW_DATE=32, NOW_TIME=33, 
		NOW_DATETIME=34, LPAREN=35, RPAREN=36, LBRACKET=37, RBRACKET=38, COMMA=39, 
		SEMI=40, PERIOD=41, IDENTIFIER=42, STRING=43, NUMBER=44, POSITIVE=45, 
		DATE=46, TIME=47, TIME_OFFSET=48, DATETIME=49, BOOLEAN_TYPE=50, NUMBER_TYPE=51, 
		STRING_TYPE=52, DATE_TYPE=53, TIME_TYPE=54, DATETIME_TYPE=55, VECTOR_TYPE=56, 
		LINE_COMMENT=57, BLOCK_COMMENT=58, WS=59, ERROR_CHAR=60;
	public static final int
		RULE_mathStart = 0, RULE_assignmentStart = 1, RULE_logicalStart = 2, RULE_assignmentExpression = 3, 
		RULE_logicalExpression = 4, RULE_logicalOrExpression = 5, RULE_logicalAndExpression = 6, 
		RULE_logicalComparisonExpression = 7, RULE_logicalBitwiseExpression = 8, 
		RULE_logicalNotExpression = 9, RULE_logicalPrimary = 10, RULE_mathExpression = 11, 
		RULE_sumExpression = 12, RULE_multiplicationExpression = 13, RULE_unaryExpression = 14, 
		RULE_rootExpression = 15, RULE_exponentiationExpression = 16, RULE_postfixExpression = 17, 
		RULE_primaryMathExpression = 18, RULE_function = 19, RULE_referenceTarget = 20, 
		RULE_comparisonOperator = 21, RULE_allEntityTypes = 22, RULE_assignmentValue = 23, 
		RULE_genericEntity = 24, RULE_castExpression = 25, RULE_typeHint = 26, 
		RULE_logicalEntity = 27, RULE_numericEntity = 28, RULE_stringEntity = 29, 
		RULE_dateEntity = 30, RULE_timeEntity = 31, RULE_dateTimeEntity = 32, 
		RULE_vectorEntity = 33, RULE_vectorOfVariables = 34;
	private static String[] makeRuleNames() {
		return new String[] {
			"mathStart", "assignmentStart", "logicalStart", "assignmentExpression", 
			"logicalExpression", "logicalOrExpression", "logicalAndExpression", "logicalComparisonExpression", 
			"logicalBitwiseExpression", "logicalNotExpression", "logicalPrimary", 
			"mathExpression", "sumExpression", "multiplicationExpression", "unaryExpression", 
			"rootExpression", "exponentiationExpression", "postfixExpression", "primaryMathExpression", 
			"function", "referenceTarget", "comparisonOperator", "allEntityTypes", 
			"assignmentValue", "genericEntity", "castExpression", "typeHint", "logicalEntity", 
			"numericEntity", "stringEntity", "dateEntity", "timeEntity", "dateTimeEntity", 
			"vectorEntity", "vectorOfVariables"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'if'", "'then'", "'else'", "'elsif'", "'endif'", "'and'", "'or'", 
			"'xor'", "'xnor'", "'nand'", "'nor'", "'true'", "'false'", "'*'", "'/'", 
			"'+'", "'-'", "'%'", "'mod'", "'|'", "'!'", "'^'", null, "'sqrt'", "'>'", 
			"'>='", "'<'", "'<='", "'='", null, null, "'currDate'", "'currTime'", 
			"'currDateTime'", "'('", "')'", "'['", "']'", "','", "';'", "'.'", null, 
			null, null, null, null, null, null, null, "'<bool>'", "'<number>'", "'<text>'", 
			"'<date>'", "'<time>'", "'<datetime>'", "'<vector>'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "IF", "THEN", "ELSE", "ELSEIF", "ENDIF", "AND", "OR", "XOR", "XNOR", 
			"NAND", "NOR", "TRUE", "FALSE", "MULT", "DIV", "PLUS", "MINUS", "PERCENT", 
			"MODULO", "MODULUS", "EXCLAMATION", "EXPONENTIATION", "ROOT", "SQRT", 
			"GT", "GE", "LT", "LE", "EQ", "NEQ", "NOT", "NOW_DATE", "NOW_TIME", "NOW_DATETIME", 
			"LPAREN", "RPAREN", "LBRACKET", "RBRACKET", "COMMA", "SEMI", "PERIOD", 
			"IDENTIFIER", "STRING", "NUMBER", "POSITIVE", "DATE", "TIME", "TIME_OFFSET", 
			"DATETIME", "BOOLEAN_TYPE", "NUMBER_TYPE", "STRING_TYPE", "DATE_TYPE", 
			"TIME_TYPE", "DATETIME_TYPE", "VECTOR_TYPE", "LINE_COMMENT", "BLOCK_COMMENT", 
			"WS", "ERROR_CHAR"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
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

	@Override
	public String getGrammarFileName() { return "ExpressionEvaluatorV2.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ExpressionEvaluatorV2Parser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MathStartContext extends ParserRuleContext {
		public MathStartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mathStart; }
	 
		public MathStartContext() { }
		public void copyFrom(MathStartContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathInputContext extends MathStartContext {
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ExpressionEvaluatorV2Parser.EOF, 0); }
		public List<AssignmentExpressionContext> assignmentExpression() {
			return getRuleContexts(AssignmentExpressionContext.class);
		}
		public AssignmentExpressionContext assignmentExpression(int i) {
			return getRuleContext(AssignmentExpressionContext.class,i);
		}
		public MathInputContext(MathStartContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterMathInput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitMathInput(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitMathInput(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MathStartContext mathStart() throws RecognitionException {
		MathStartContext _localctx = new MathStartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_mathStart);
		try {
			int _alt;
			_localctx = new MathInputContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(73);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(70);
					assignmentExpression();
					}
					} 
				}
				setState(75);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(76);
			mathExpression();
			setState(77);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentStartContext extends ParserRuleContext {
		public AssignmentStartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentStart; }
	 
		public AssignmentStartContext() { }
		public void copyFrom(AssignmentStartContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentInputContext extends AssignmentStartContext {
		public TerminalNode EOF() { return getToken(ExpressionEvaluatorV2Parser.EOF, 0); }
		public List<AssignmentExpressionContext> assignmentExpression() {
			return getRuleContexts(AssignmentExpressionContext.class);
		}
		public AssignmentExpressionContext assignmentExpression(int i) {
			return getRuleContext(AssignmentExpressionContext.class,i);
		}
		public AssignmentInputContext(AssignmentStartContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterAssignmentInput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitAssignmentInput(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitAssignmentInput(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentStartContext assignmentStart() throws RecognitionException {
		AssignmentStartContext _localctx = new AssignmentStartContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_assignmentStart);
		int _la;
		try {
			_localctx = new AssignmentInputContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(80); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(79);
				assignmentExpression();
				}
				}
				setState(82); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==LBRACKET || _la==IDENTIFIER );
			setState(84);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalStartContext extends ParserRuleContext {
		public LogicalStartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalStart; }
	 
		public LogicalStartContext() { }
		public void copyFrom(LogicalStartContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalInputContext extends LogicalStartContext {
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ExpressionEvaluatorV2Parser.EOF, 0); }
		public List<AssignmentExpressionContext> assignmentExpression() {
			return getRuleContexts(AssignmentExpressionContext.class);
		}
		public AssignmentExpressionContext assignmentExpression(int i) {
			return getRuleContext(AssignmentExpressionContext.class,i);
		}
		public LogicalInputContext(LogicalStartContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalInput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalInput(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalInput(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalStartContext logicalStart() throws RecognitionException {
		LogicalStartContext _localctx = new LogicalStartContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_logicalStart);
		try {
			int _alt;
			_localctx = new LogicalInputContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(86);
					assignmentExpression();
					}
					} 
				}
				setState(91);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			setState(92);
			logicalExpression();
			setState(93);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentExpressionContext extends ParserRuleContext {
		public AssignmentExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentExpression; }
	 
		public AssignmentExpressionContext() { }
		public void copyFrom(AssignmentExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DestructuringAssignmentOperationContext extends AssignmentExpressionContext {
		public VectorOfVariablesContext vectorOfVariables() {
			return getRuleContext(VectorOfVariablesContext.class,0);
		}
		public TerminalNode EQ() { return getToken(ExpressionEvaluatorV2Parser.EQ, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(ExpressionEvaluatorV2Parser.SEMI, 0); }
		public DestructuringAssignmentOperationContext(AssignmentExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDestructuringAssignmentOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDestructuringAssignmentOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDestructuringAssignmentOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentOperationContext extends AssignmentExpressionContext {
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorV2Parser.IDENTIFIER, 0); }
		public TerminalNode EQ() { return getToken(ExpressionEvaluatorV2Parser.EQ, 0); }
		public AssignmentValueContext assignmentValue() {
			return getRuleContext(AssignmentValueContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(ExpressionEvaluatorV2Parser.SEMI, 0); }
		public AssignmentOperationContext(AssignmentExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterAssignmentOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitAssignmentOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitAssignmentOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentExpressionContext assignmentExpression() throws RecognitionException {
		AssignmentExpressionContext _localctx = new AssignmentExpressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_assignmentExpression);
		try {
			setState(105);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				_localctx = new AssignmentOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(95);
				match(IDENTIFIER);
				setState(96);
				match(EQ);
				setState(97);
				assignmentValue();
				setState(98);
				match(SEMI);
				}
				break;
			case LBRACKET:
				_localctx = new DestructuringAssignmentOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(100);
				vectorOfVariables();
				setState(101);
				match(EQ);
				setState(102);
				vectorEntity();
				setState(103);
				match(SEMI);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalExpressionContext extends ParserRuleContext {
		public LogicalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalExpression; }
	 
		public LogicalExpressionContext() { }
		public void copyFrom(LogicalExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalOrOperationContext extends LogicalExpressionContext {
		public LogicalOrExpressionContext logicalOrExpression() {
			return getRuleContext(LogicalOrExpressionContext.class,0);
		}
		public LogicalOrOperationContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalOrOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalOrOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalOrOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalExpressionContext logicalExpression() throws RecognitionException {
		LogicalExpressionContext _localctx = new LogicalExpressionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_logicalExpression);
		try {
			_localctx = new LogicalOrOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(107);
			logicalOrExpression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalOrExpressionContext extends ParserRuleContext {
		public LogicalOrExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalOrExpression; }
	 
		public LogicalOrExpressionContext() { }
		public void copyFrom(LogicalOrExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalOrChainOperationContext extends LogicalOrExpressionContext {
		public List<LogicalAndExpressionContext> logicalAndExpression() {
			return getRuleContexts(LogicalAndExpressionContext.class);
		}
		public LogicalAndExpressionContext logicalAndExpression(int i) {
			return getRuleContext(LogicalAndExpressionContext.class,i);
		}
		public List<TerminalNode> OR() { return getTokens(ExpressionEvaluatorV2Parser.OR); }
		public TerminalNode OR(int i) {
			return getToken(ExpressionEvaluatorV2Parser.OR, i);
		}
		public LogicalOrChainOperationContext(LogicalOrExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalOrChainOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalOrChainOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalOrChainOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalOrExpressionContext logicalOrExpression() throws RecognitionException {
		LogicalOrExpressionContext _localctx = new LogicalOrExpressionContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_logicalOrExpression);
		int _la;
		try {
			_localctx = new LogicalOrChainOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(109);
			logicalAndExpression();
			setState(114);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(110);
				match(OR);
				setState(111);
				logicalAndExpression();
				}
				}
				setState(116);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalAndExpressionContext extends ParserRuleContext {
		public LogicalAndExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalAndExpression; }
	 
		public LogicalAndExpressionContext() { }
		public void copyFrom(LogicalAndExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalAndChainOperationContext extends LogicalAndExpressionContext {
		public List<LogicalComparisonExpressionContext> logicalComparisonExpression() {
			return getRuleContexts(LogicalComparisonExpressionContext.class);
		}
		public LogicalComparisonExpressionContext logicalComparisonExpression(int i) {
			return getRuleContext(LogicalComparisonExpressionContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(ExpressionEvaluatorV2Parser.AND); }
		public TerminalNode AND(int i) {
			return getToken(ExpressionEvaluatorV2Parser.AND, i);
		}
		public LogicalAndChainOperationContext(LogicalAndExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalAndChainOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalAndChainOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalAndChainOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalAndExpressionContext logicalAndExpression() throws RecognitionException {
		LogicalAndExpressionContext _localctx = new LogicalAndExpressionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_logicalAndExpression);
		int _la;
		try {
			_localctx = new LogicalAndChainOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			logicalComparisonExpression();
			setState(122);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(118);
				match(AND);
				setState(119);
				logicalComparisonExpression();
				}
				}
				setState(124);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalComparisonExpressionContext extends ParserRuleContext {
		public LogicalComparisonExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalComparisonExpression; }
	 
		public LogicalComparisonExpressionContext() { }
		public void copyFrom(LogicalComparisonExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeComparisonOperationContext extends LogicalComparisonExpressionContext {
		public List<TimeEntityContext> timeEntity() {
			return getRuleContexts(TimeEntityContext.class);
		}
		public TimeEntityContext timeEntity(int i) {
			return getRuleContext(TimeEntityContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public TimeComparisonOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterTimeComparisonOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitTimeComparisonOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitTimeComparisonOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeComparisonOperationContext extends LogicalComparisonExpressionContext {
		public List<DateTimeEntityContext> dateTimeEntity() {
			return getRuleContexts(DateTimeEntityContext.class);
		}
		public DateTimeEntityContext dateTimeEntity(int i) {
			return getRuleContext(DateTimeEntityContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public DateTimeComparisonOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateTimeComparisonOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateTimeComparisonOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateTimeComparisonOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateComparisonOperationContext extends LogicalComparisonExpressionContext {
		public List<DateEntityContext> dateEntity() {
			return getRuleContexts(DateEntityContext.class);
		}
		public DateEntityContext dateEntity(int i) {
			return getRuleContext(DateEntityContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public DateComparisonOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateComparisonOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateComparisonOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateComparisonOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalComparisonOperationContext extends LogicalComparisonExpressionContext {
		public List<LogicalBitwiseExpressionContext> logicalBitwiseExpression() {
			return getRuleContexts(LogicalBitwiseExpressionContext.class);
		}
		public LogicalBitwiseExpressionContext logicalBitwiseExpression(int i) {
			return getRuleContext(LogicalBitwiseExpressionContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public LogicalComparisonOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalComparisonOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalComparisonOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalComparisonOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathComparisonOperationContext extends LogicalComparisonExpressionContext {
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public MathComparisonOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterMathComparisonOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitMathComparisonOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitMathComparisonOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringComparisonOperationContext extends LogicalComparisonExpressionContext {
		public List<StringEntityContext> stringEntity() {
			return getRuleContexts(StringEntityContext.class);
		}
		public StringEntityContext stringEntity(int i) {
			return getRuleContext(StringEntityContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public StringComparisonOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterStringComparisonOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitStringComparisonOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitStringComparisonOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalComparisonExpressionContext logicalComparisonExpression() throws RecognitionException {
		LogicalComparisonExpressionContext _localctx = new LogicalComparisonExpressionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_logicalComparisonExpression);
		int _la;
		try {
			setState(151);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				_localctx = new LogicalComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(125);
				logicalBitwiseExpression();
				setState(129);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2113929216L) != 0)) {
					{
					setState(126);
					comparisonOperator();
					setState(127);
					logicalBitwiseExpression();
					}
				}

				}
				break;
			case 2:
				_localctx = new MathComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(131);
				mathExpression();
				setState(132);
				comparisonOperator();
				setState(133);
				mathExpression();
				}
				break;
			case 3:
				_localctx = new StringComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(135);
				stringEntity();
				setState(136);
				comparisonOperator();
				setState(137);
				stringEntity();
				}
				break;
			case 4:
				_localctx = new DateComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(139);
				dateEntity();
				setState(140);
				comparisonOperator();
				setState(141);
				dateEntity();
				}
				break;
			case 5:
				_localctx = new TimeComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(143);
				timeEntity();
				setState(144);
				comparisonOperator();
				setState(145);
				timeEntity();
				}
				break;
			case 6:
				_localctx = new DateTimeComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(147);
				dateTimeEntity();
				setState(148);
				comparisonOperator();
				setState(149);
				dateTimeEntity();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalBitwiseExpressionContext extends ParserRuleContext {
		public LogicalBitwiseExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalBitwiseExpression; }
	 
		public LogicalBitwiseExpressionContext() { }
		public void copyFrom(LogicalBitwiseExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalBitwiseOperationContext extends LogicalBitwiseExpressionContext {
		public List<LogicalNotExpressionContext> logicalNotExpression() {
			return getRuleContexts(LogicalNotExpressionContext.class);
		}
		public LogicalNotExpressionContext logicalNotExpression(int i) {
			return getRuleContext(LogicalNotExpressionContext.class,i);
		}
		public List<TerminalNode> NAND() { return getTokens(ExpressionEvaluatorV2Parser.NAND); }
		public TerminalNode NAND(int i) {
			return getToken(ExpressionEvaluatorV2Parser.NAND, i);
		}
		public List<TerminalNode> NOR() { return getTokens(ExpressionEvaluatorV2Parser.NOR); }
		public TerminalNode NOR(int i) {
			return getToken(ExpressionEvaluatorV2Parser.NOR, i);
		}
		public List<TerminalNode> XOR() { return getTokens(ExpressionEvaluatorV2Parser.XOR); }
		public TerminalNode XOR(int i) {
			return getToken(ExpressionEvaluatorV2Parser.XOR, i);
		}
		public List<TerminalNode> XNOR() { return getTokens(ExpressionEvaluatorV2Parser.XNOR); }
		public TerminalNode XNOR(int i) {
			return getToken(ExpressionEvaluatorV2Parser.XNOR, i);
		}
		public LogicalBitwiseOperationContext(LogicalBitwiseExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalBitwiseOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalBitwiseOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalBitwiseOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalBitwiseExpressionContext logicalBitwiseExpression() throws RecognitionException {
		LogicalBitwiseExpressionContext _localctx = new LogicalBitwiseExpressionContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_logicalBitwiseExpression);
		int _la;
		try {
			_localctx = new LogicalBitwiseOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(153);
			logicalNotExpression();
			setState(158);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3840L) != 0)) {
				{
				{
				setState(154);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 3840L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(155);
				logicalNotExpression();
				}
				}
				setState(160);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalNotExpressionContext extends ParserRuleContext {
		public LogicalNotExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalNotExpression; }
	 
		public LogicalNotExpressionContext() { }
		public void copyFrom(LogicalNotExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalPrimaryOperationContext extends LogicalNotExpressionContext {
		public LogicalPrimaryContext logicalPrimary() {
			return getRuleContext(LogicalPrimaryContext.class,0);
		}
		public LogicalPrimaryOperationContext(LogicalNotExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalPrimaryOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalPrimaryOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalPrimaryOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalNotOperationContext extends LogicalNotExpressionContext {
		public LogicalNotExpressionContext logicalNotExpression() {
			return getRuleContext(LogicalNotExpressionContext.class,0);
		}
		public TerminalNode NOT() { return getToken(ExpressionEvaluatorV2Parser.NOT, 0); }
		public TerminalNode EXCLAMATION() { return getToken(ExpressionEvaluatorV2Parser.EXCLAMATION, 0); }
		public LogicalNotOperationContext(LogicalNotExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalNotOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalNotOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalNotOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalNotExpressionContext logicalNotExpression() throws RecognitionException {
		LogicalNotExpressionContext _localctx = new LogicalNotExpressionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_logicalNotExpression);
		int _la;
		try {
			setState(164);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXCLAMATION:
			case NOT:
				_localctx = new LogicalNotOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(161);
				_la = _input.LA(1);
				if ( !(_la==EXCLAMATION || _la==NOT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(162);
				logicalNotExpression();
				}
				break;
			case IF:
			case TRUE:
			case FALSE:
			case LPAREN:
			case IDENTIFIER:
			case BOOLEAN_TYPE:
				_localctx = new LogicalPrimaryOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(163);
				logicalPrimary();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalPrimaryContext extends ParserRuleContext {
		public LogicalPrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalPrimary; }
	 
		public LogicalPrimaryContext() { }
		public void copyFrom(LogicalPrimaryContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalExpressionParenthesisOperationContext extends LogicalPrimaryContext {
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorV2Parser.LPAREN, 0); }
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorV2Parser.RPAREN, 0); }
		public LogicalExpressionParenthesisOperationContext(LogicalPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalExpressionParenthesisOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalExpressionParenthesisOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalExpressionParenthesisOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalEntityOperationContext extends LogicalPrimaryContext {
		public LogicalEntityContext logicalEntity() {
			return getRuleContext(LogicalEntityContext.class,0);
		}
		public LogicalEntityOperationContext(LogicalPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalEntityOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalEntityOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalEntityOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalPrimaryContext logicalPrimary() throws RecognitionException {
		LogicalPrimaryContext _localctx = new LogicalPrimaryContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_logicalPrimary);
		try {
			setState(171);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				_localctx = new LogicalExpressionParenthesisOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(166);
				match(LPAREN);
				setState(167);
				logicalExpression();
				setState(168);
				match(RPAREN);
				}
				break;
			case IF:
			case TRUE:
			case FALSE:
			case IDENTIFIER:
			case BOOLEAN_TYPE:
				_localctx = new LogicalEntityOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(170);
				logicalEntity();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MathExpressionContext extends ParserRuleContext {
		public MathExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mathExpression; }
	 
		public MathExpressionContext() { }
		public void copyFrom(MathExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SumOperationContext extends MathExpressionContext {
		public SumExpressionContext sumExpression() {
			return getRuleContext(SumExpressionContext.class,0);
		}
		public SumOperationContext(MathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterSumOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitSumOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitSumOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MathExpressionContext mathExpression() throws RecognitionException {
		MathExpressionContext _localctx = new MathExpressionContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_mathExpression);
		try {
			_localctx = new SumOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(173);
			sumExpression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SumExpressionContext extends ParserRuleContext {
		public SumExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sumExpression; }
	 
		public SumExpressionContext() { }
		public void copyFrom(SumExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AdditiveOperationContext extends SumExpressionContext {
		public List<MultiplicationExpressionContext> multiplicationExpression() {
			return getRuleContexts(MultiplicationExpressionContext.class);
		}
		public MultiplicationExpressionContext multiplicationExpression(int i) {
			return getRuleContext(MultiplicationExpressionContext.class,i);
		}
		public List<TerminalNode> PLUS() { return getTokens(ExpressionEvaluatorV2Parser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(ExpressionEvaluatorV2Parser.PLUS, i);
		}
		public List<TerminalNode> MINUS() { return getTokens(ExpressionEvaluatorV2Parser.MINUS); }
		public TerminalNode MINUS(int i) {
			return getToken(ExpressionEvaluatorV2Parser.MINUS, i);
		}
		public AdditiveOperationContext(SumExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterAdditiveOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitAdditiveOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitAdditiveOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SumExpressionContext sumExpression() throws RecognitionException {
		SumExpressionContext _localctx = new SumExpressionContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_sumExpression);
		int _la;
		try {
			_localctx = new AdditiveOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(175);
			multiplicationExpression();
			setState(180);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS || _la==MINUS) {
				{
				{
				setState(176);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(177);
				multiplicationExpression();
				}
				}
				setState(182);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MultiplicationExpressionContext extends ParserRuleContext {
		public MultiplicationExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multiplicationExpression; }
	 
		public MultiplicationExpressionContext() { }
		public void copyFrom(MultiplicationExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MultiplicativeOperationContext extends MultiplicationExpressionContext {
		public List<UnaryExpressionContext> unaryExpression() {
			return getRuleContexts(UnaryExpressionContext.class);
		}
		public UnaryExpressionContext unaryExpression(int i) {
			return getRuleContext(UnaryExpressionContext.class,i);
		}
		public List<TerminalNode> MULT() { return getTokens(ExpressionEvaluatorV2Parser.MULT); }
		public TerminalNode MULT(int i) {
			return getToken(ExpressionEvaluatorV2Parser.MULT, i);
		}
		public List<TerminalNode> DIV() { return getTokens(ExpressionEvaluatorV2Parser.DIV); }
		public TerminalNode DIV(int i) {
			return getToken(ExpressionEvaluatorV2Parser.DIV, i);
		}
		public List<TerminalNode> MODULO() { return getTokens(ExpressionEvaluatorV2Parser.MODULO); }
		public TerminalNode MODULO(int i) {
			return getToken(ExpressionEvaluatorV2Parser.MODULO, i);
		}
		public MultiplicativeOperationContext(MultiplicationExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterMultiplicativeOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitMultiplicativeOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitMultiplicativeOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiplicationExpressionContext multiplicationExpression() throws RecognitionException {
		MultiplicationExpressionContext _localctx = new MultiplicationExpressionContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_multiplicationExpression);
		int _la;
		try {
			_localctx = new MultiplicativeOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(183);
			unaryExpression();
			setState(188);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 573440L) != 0)) {
				{
				{
				setState(184);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 573440L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(185);
				unaryExpression();
				}
				}
				setState(190);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnaryExpressionContext extends ParserRuleContext {
		public UnaryExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unaryExpression; }
	 
		public UnaryExpressionContext() { }
		public void copyFrom(UnaryExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RootExpressionOperationContext extends UnaryExpressionContext {
		public RootExpressionContext rootExpression() {
			return getRuleContext(RootExpressionContext.class,0);
		}
		public RootExpressionOperationContext(UnaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterRootExpressionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitRootExpressionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitRootExpressionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UnaryMinusOperationContext extends UnaryExpressionContext {
		public TerminalNode MINUS() { return getToken(ExpressionEvaluatorV2Parser.MINUS, 0); }
		public UnaryExpressionContext unaryExpression() {
			return getRuleContext(UnaryExpressionContext.class,0);
		}
		public UnaryMinusOperationContext(UnaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterUnaryMinusOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitUnaryMinusOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitUnaryMinusOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryExpressionContext unaryExpression() throws RecognitionException {
		UnaryExpressionContext _localctx = new UnaryExpressionContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_unaryExpression);
		try {
			setState(194);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MINUS:
				_localctx = new UnaryMinusOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(191);
				match(MINUS);
				setState(192);
				unaryExpression();
				}
				break;
			case IF:
			case MODULUS:
			case SQRT:
			case LPAREN:
			case IDENTIFIER:
			case NUMBER:
			case NUMBER_TYPE:
				_localctx = new RootExpressionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(193);
				rootExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RootExpressionContext extends ParserRuleContext {
		public RootExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rootExpression; }
	 
		public RootExpressionContext() { }
		public void copyFrom(RootExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RootChainOperationContext extends RootExpressionContext {
		public List<ExponentiationExpressionContext> exponentiationExpression() {
			return getRuleContexts(ExponentiationExpressionContext.class);
		}
		public ExponentiationExpressionContext exponentiationExpression(int i) {
			return getRuleContext(ExponentiationExpressionContext.class,i);
		}
		public List<TerminalNode> ROOT() { return getTokens(ExpressionEvaluatorV2Parser.ROOT); }
		public TerminalNode ROOT(int i) {
			return getToken(ExpressionEvaluatorV2Parser.ROOT, i);
		}
		public RootChainOperationContext(RootExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterRootChainOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitRootChainOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitRootChainOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RootExpressionContext rootExpression() throws RecognitionException {
		RootExpressionContext _localctx = new RootExpressionContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_rootExpression);
		try {
			int _alt;
			_localctx = new RootChainOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			exponentiationExpression();
			setState(201);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(197);
					match(ROOT);
					setState(198);
					exponentiationExpression();
					}
					} 
				}
				setState(203);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExponentiationExpressionContext extends ParserRuleContext {
		public ExponentiationExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exponentiationExpression; }
	 
		public ExponentiationExpressionContext() { }
		public void copyFrom(ExponentiationExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExponentiationOperationContext extends ExponentiationExpressionContext {
		public PostfixExpressionContext postfixExpression() {
			return getRuleContext(PostfixExpressionContext.class,0);
		}
		public TerminalNode EXPONENTIATION() { return getToken(ExpressionEvaluatorV2Parser.EXPONENTIATION, 0); }
		public UnaryExpressionContext unaryExpression() {
			return getRuleContext(UnaryExpressionContext.class,0);
		}
		public ExponentiationOperationContext(ExponentiationExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterExponentiationOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitExponentiationOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitExponentiationOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExponentiationExpressionContext exponentiationExpression() throws RecognitionException {
		ExponentiationExpressionContext _localctx = new ExponentiationExpressionContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_exponentiationExpression);
		int _la;
		try {
			_localctx = new ExponentiationOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(204);
			postfixExpression();
			setState(207);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXPONENTIATION) {
				{
				setState(205);
				match(EXPONENTIATION);
				setState(206);
				unaryExpression();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PostfixExpressionContext extends ParserRuleContext {
		public PostfixExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_postfixExpression; }
	 
		public PostfixExpressionContext() { }
		public void copyFrom(PostfixExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PostfixOperationContext extends PostfixExpressionContext {
		public PrimaryMathExpressionContext primaryMathExpression() {
			return getRuleContext(PrimaryMathExpressionContext.class,0);
		}
		public List<TerminalNode> PERCENT() { return getTokens(ExpressionEvaluatorV2Parser.PERCENT); }
		public TerminalNode PERCENT(int i) {
			return getToken(ExpressionEvaluatorV2Parser.PERCENT, i);
		}
		public List<TerminalNode> EXCLAMATION() { return getTokens(ExpressionEvaluatorV2Parser.EXCLAMATION); }
		public TerminalNode EXCLAMATION(int i) {
			return getToken(ExpressionEvaluatorV2Parser.EXCLAMATION, i);
		}
		public PostfixOperationContext(PostfixExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterPostfixOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitPostfixOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitPostfixOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PostfixExpressionContext postfixExpression() throws RecognitionException {
		PostfixExpressionContext _localctx = new PostfixExpressionContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_postfixExpression);
		int _la;
		try {
			_localctx = new PostfixOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(209);
			primaryMathExpression();
			setState(213);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PERCENT || _la==EXCLAMATION) {
				{
				{
				setState(210);
				_la = _input.LA(1);
				if ( !(_la==PERCENT || _la==EXCLAMATION) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(215);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryMathExpressionContext extends ParserRuleContext {
		public PrimaryMathExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primaryMathExpression; }
	 
		public PrimaryMathExpressionContext() { }
		public void copyFrom(PrimaryMathExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SquareRootOperationContext extends PrimaryMathExpressionContext {
		public TerminalNode SQRT() { return getToken(ExpressionEvaluatorV2Parser.SQRT, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorV2Parser.LPAREN, 0); }
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorV2Parser.RPAREN, 0); }
		public SquareRootOperationContext(PrimaryMathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterSquareRootOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitSquareRootOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitSquareRootOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathExpressionParenthesisOperationContext extends PrimaryMathExpressionContext {
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorV2Parser.LPAREN, 0); }
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorV2Parser.RPAREN, 0); }
		public MathExpressionParenthesisOperationContext(PrimaryMathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterMathExpressionParenthesisOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitMathExpressionParenthesisOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitMathExpressionParenthesisOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ModulusOperationContext extends PrimaryMathExpressionContext {
		public List<TerminalNode> MODULUS() { return getTokens(ExpressionEvaluatorV2Parser.MODULUS); }
		public TerminalNode MODULUS(int i) {
			return getToken(ExpressionEvaluatorV2Parser.MODULUS, i);
		}
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public ModulusOperationContext(PrimaryMathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterModulusOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitModulusOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitModulusOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumericEntityOperationContext extends PrimaryMathExpressionContext {
		public NumericEntityContext numericEntity() {
			return getRuleContext(NumericEntityContext.class,0);
		}
		public NumericEntityOperationContext(PrimaryMathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterNumericEntityOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitNumericEntityOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitNumericEntityOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryMathExpressionContext primaryMathExpression() throws RecognitionException {
		PrimaryMathExpressionContext _localctx = new PrimaryMathExpressionContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_primaryMathExpression);
		try {
			setState(230);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				_localctx = new MathExpressionParenthesisOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(216);
				match(LPAREN);
				setState(217);
				mathExpression();
				setState(218);
				match(RPAREN);
				}
				break;
			case SQRT:
				_localctx = new SquareRootOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(220);
				match(SQRT);
				setState(221);
				match(LPAREN);
				setState(222);
				mathExpression();
				setState(223);
				match(RPAREN);
				}
				break;
			case MODULUS:
				_localctx = new ModulusOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(225);
				match(MODULUS);
				setState(226);
				mathExpression();
				setState(227);
				match(MODULUS);
				}
				break;
			case IF:
			case IDENTIFIER:
			case NUMBER:
			case NUMBER_TYPE:
				_localctx = new NumericEntityOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(229);
				numericEntity();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionContext extends ParserRuleContext {
		public FunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function; }
	 
		public FunctionContext() { }
		public void copyFrom(FunctionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FunctionCallOperationContext extends FunctionContext {
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorV2Parser.IDENTIFIER, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorV2Parser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorV2Parser.RPAREN, 0); }
		public List<AllEntityTypesContext> allEntityTypes() {
			return getRuleContexts(AllEntityTypesContext.class);
		}
		public AllEntityTypesContext allEntityTypes(int i) {
			return getRuleContext(AllEntityTypesContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorV2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorV2Parser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorV2Parser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorV2Parser.SEMI, i);
		}
		public FunctionCallOperationContext(FunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterFunctionCallOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitFunctionCallOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitFunctionCallOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionContext function() throws RecognitionException {
		FunctionContext _localctx = new FunctionContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_function);
		int _la;
		try {
			_localctx = new FunctionCallOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(232);
			match(IDENTIFIER);
			setState(233);
			match(LPAREN);
			setState(242);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 143794334711558146L) != 0)) {
				{
				setState(234);
				allEntityTypes();
				setState(239);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA || _la==SEMI) {
					{
					{
					setState(235);
					_la = _input.LA(1);
					if ( !(_la==COMMA || _la==SEMI) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(236);
					allEntityTypes();
					}
					}
					setState(241);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(244);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReferenceTargetContext extends ParserRuleContext {
		public ReferenceTargetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_referenceTarget; }
	 
		public ReferenceTargetContext() { }
		public void copyFrom(ReferenceTargetContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FunctionReferenceTargetContext extends ReferenceTargetContext {
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public FunctionReferenceTargetContext(ReferenceTargetContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterFunctionReferenceTarget(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitFunctionReferenceTarget(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitFunctionReferenceTarget(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierReferenceTargetContext extends ReferenceTargetContext {
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorV2Parser.IDENTIFIER, 0); }
		public IdentifierReferenceTargetContext(ReferenceTargetContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterIdentifierReferenceTarget(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitIdentifierReferenceTarget(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitIdentifierReferenceTarget(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReferenceTargetContext referenceTarget() throws RecognitionException {
		ReferenceTargetContext _localctx = new ReferenceTargetContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_referenceTarget);
		try {
			setState(248);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				_localctx = new FunctionReferenceTargetContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(246);
				function();
				}
				break;
			case 2:
				_localctx = new IdentifierReferenceTargetContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(247);
				match(IDENTIFIER);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonOperatorContext extends ParserRuleContext {
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
	 
		public ComparisonOperatorContext() { }
		public void copyFrom(ComparisonOperatorContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EqualOperatorContext extends ComparisonOperatorContext {
		public TerminalNode EQ() { return getToken(ExpressionEvaluatorV2Parser.EQ, 0); }
		public EqualOperatorContext(ComparisonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterEqualOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitEqualOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitEqualOperator(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NotEqualOperatorContext extends ComparisonOperatorContext {
		public TerminalNode NEQ() { return getToken(ExpressionEvaluatorV2Parser.NEQ, 0); }
		public NotEqualOperatorContext(ComparisonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterNotEqualOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitNotEqualOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitNotEqualOperator(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LessThanOperatorContext extends ComparisonOperatorContext {
		public TerminalNode LT() { return getToken(ExpressionEvaluatorV2Parser.LT, 0); }
		public LessThanOperatorContext(ComparisonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLessThanOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLessThanOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLessThanOperator(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GreaterThanOperatorContext extends ComparisonOperatorContext {
		public TerminalNode GT() { return getToken(ExpressionEvaluatorV2Parser.GT, 0); }
		public GreaterThanOperatorContext(ComparisonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterGreaterThanOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitGreaterThanOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitGreaterThanOperator(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LessThanOrEqualOperatorContext extends ComparisonOperatorContext {
		public TerminalNode LE() { return getToken(ExpressionEvaluatorV2Parser.LE, 0); }
		public LessThanOrEqualOperatorContext(ComparisonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLessThanOrEqualOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLessThanOrEqualOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLessThanOrEqualOperator(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GreaterThanOrEqualOperatorContext extends ComparisonOperatorContext {
		public TerminalNode GE() { return getToken(ExpressionEvaluatorV2Parser.GE, 0); }
		public GreaterThanOrEqualOperatorContext(ComparisonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterGreaterThanOrEqualOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitGreaterThanOrEqualOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitGreaterThanOrEqualOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_comparisonOperator);
		try {
			setState(256);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case GT:
				_localctx = new GreaterThanOperatorContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(250);
				match(GT);
				}
				break;
			case GE:
				_localctx = new GreaterThanOrEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(251);
				match(GE);
				}
				break;
			case LT:
				_localctx = new LessThanOperatorContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(252);
				match(LT);
				}
				break;
			case LE:
				_localctx = new LessThanOrEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(253);
				match(LE);
				}
				break;
			case EQ:
				_localctx = new EqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(254);
				match(EQ);
				}
				break;
			case NEQ:
				_localctx = new NotEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(255);
				match(NEQ);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AllEntityTypesContext extends ParserRuleContext {
		public AllEntityTypesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_allEntityTypes; }
	 
		public AllEntityTypesContext() { }
		public void copyFrom(AllEntityTypesContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeEntityTypeContext extends AllEntityTypesContext {
		public TimeEntityContext timeEntity() {
			return getRuleContext(TimeEntityContext.class,0);
		}
		public TimeEntityTypeContext(AllEntityTypesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterTimeEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitTimeEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitTimeEntityType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringEntityTypeContext extends AllEntityTypesContext {
		public StringEntityContext stringEntity() {
			return getRuleContext(StringEntityContext.class,0);
		}
		public StringEntityTypeContext(AllEntityTypesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterStringEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitStringEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitStringEntityType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateEntityTypeContext extends AllEntityTypesContext {
		public DateEntityContext dateEntity() {
			return getRuleContext(DateEntityContext.class,0);
		}
		public DateEntityTypeContext(AllEntityTypesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateEntityType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeEntityTypeContext extends AllEntityTypesContext {
		public DateTimeEntityContext dateTimeEntity() {
			return getRuleContext(DateTimeEntityContext.class,0);
		}
		public DateTimeEntityTypeContext(AllEntityTypesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateTimeEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateTimeEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateTimeEntityType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VectorEntityTypeContext extends AllEntityTypesContext {
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public VectorEntityTypeContext(AllEntityTypesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterVectorEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitVectorEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitVectorEntityType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathEntityTypeContext extends AllEntityTypesContext {
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public MathEntityTypeContext(AllEntityTypesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterMathEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitMathEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitMathEntityType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalEntityTypeContext extends AllEntityTypesContext {
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public LogicalEntityTypeContext(AllEntityTypesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalEntityType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AllEntityTypesContext allEntityTypes() throws RecognitionException {
		AllEntityTypesContext _localctx = new AllEntityTypesContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_allEntityTypes);
		try {
			setState(265);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				_localctx = new MathEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(258);
				mathExpression();
				}
				break;
			case 2:
				_localctx = new LogicalEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(259);
				logicalExpression();
				}
				break;
			case 3:
				_localctx = new DateEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(260);
				dateEntity();
				}
				break;
			case 4:
				_localctx = new TimeEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(261);
				timeEntity();
				}
				break;
			case 5:
				_localctx = new DateTimeEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(262);
				dateTimeEntity();
				}
				break;
			case 6:
				_localctx = new StringEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(263);
				stringEntity();
				}
				break;
			case 7:
				_localctx = new VectorEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(264);
				vectorEntity();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentValueContext extends ParserRuleContext {
		public AssignmentValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentValue; }
	 
		public AssignmentValueContext() { }
		public void copyFrom(AssignmentValueContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeAssignmentValueContext extends AssignmentValueContext {
		public DateTimeEntityContext dateTimeEntity() {
			return getRuleContext(DateTimeEntityContext.class,0);
		}
		public DateTimeAssignmentValueContext(AssignmentValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateTimeAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateTimeAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateTimeAssignmentValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GenericAssignmentValueContext extends AssignmentValueContext {
		public GenericEntityContext genericEntity() {
			return getRuleContext(GenericEntityContext.class,0);
		}
		public GenericAssignmentValueContext(AssignmentValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterGenericAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitGenericAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitGenericAssignmentValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateAssignmentValueContext extends AssignmentValueContext {
		public DateEntityContext dateEntity() {
			return getRuleContext(DateEntityContext.class,0);
		}
		public DateAssignmentValueContext(AssignmentValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateAssignmentValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeAssignmentValueContext extends AssignmentValueContext {
		public TimeEntityContext timeEntity() {
			return getRuleContext(TimeEntityContext.class,0);
		}
		public TimeAssignmentValueContext(AssignmentValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterTimeAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitTimeAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitTimeAssignmentValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringAssignmentValueContext extends AssignmentValueContext {
		public StringEntityContext stringEntity() {
			return getRuleContext(StringEntityContext.class,0);
		}
		public StringAssignmentValueContext(AssignmentValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterStringAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitStringAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitStringAssignmentValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalAssignmentValueContext extends AssignmentValueContext {
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public LogicalAssignmentValueContext(AssignmentValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalAssignmentValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VectorAssignmentValueContext extends AssignmentValueContext {
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public VectorAssignmentValueContext(AssignmentValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterVectorAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitVectorAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitVectorAssignmentValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathAssignmentValueContext extends AssignmentValueContext {
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public MathAssignmentValueContext(AssignmentValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterMathAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitMathAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitMathAssignmentValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentValueContext assignmentValue() throws RecognitionException {
		AssignmentValueContext _localctx = new AssignmentValueContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_assignmentValue);
		try {
			setState(275);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				_localctx = new GenericAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(267);
				genericEntity();
				}
				break;
			case 2:
				_localctx = new MathAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(268);
				mathExpression();
				}
				break;
			case 3:
				_localctx = new LogicalAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(269);
				logicalExpression();
				}
				break;
			case 4:
				_localctx = new DateAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(270);
				dateEntity();
				}
				break;
			case 5:
				_localctx = new TimeAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(271);
				timeEntity();
				}
				break;
			case 6:
				_localctx = new DateTimeAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(272);
				dateTimeEntity();
				}
				break;
			case 7:
				_localctx = new StringAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(273);
				stringEntity();
				}
				break;
			case 8:
				_localctx = new VectorAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(274);
				vectorEntity();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GenericEntityContext extends ParserRuleContext {
		public GenericEntityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericEntity; }
	 
		public GenericEntityContext() { }
		public void copyFrom(GenericEntityContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CastExpressionOperationContext extends GenericEntityContext {
		public CastExpressionContext castExpression() {
			return getRuleContext(CastExpressionContext.class,0);
		}
		public CastExpressionOperationContext(GenericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterCastExpressionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitCastExpressionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitCastExpressionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GenericFunctionDecisionExpressionContext extends GenericEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorV2Parser.LPAREN, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<GenericEntityContext> genericEntity() {
			return getRuleContexts(GenericEntityContext.class);
		}
		public GenericEntityContext genericEntity(int i) {
			return getRuleContext(GenericEntityContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorV2Parser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorV2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorV2Parser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorV2Parser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorV2Parser.SEMI, i);
		}
		public GenericFunctionDecisionExpressionContext(GenericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterGenericFunctionDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitGenericFunctionDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitGenericFunctionDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GenericDecisionExpressionContext extends GenericEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorV2Parser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorV2Parser.THEN, i);
		}
		public List<GenericEntityContext> genericEntity() {
			return getRuleContexts(GenericEntityContext.class);
		}
		public GenericEntityContext genericEntity(int i) {
			return getRuleContext(GenericEntityContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorV2Parser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorV2Parser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorV2Parser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorV2Parser.ELSEIF, i);
		}
		public GenericDecisionExpressionContext(GenericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterGenericDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitGenericDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitGenericDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ReferenceTargetOperationContext extends GenericEntityContext {
		public ReferenceTargetContext referenceTarget() {
			return getRuleContext(ReferenceTargetContext.class,0);
		}
		public ReferenceTargetOperationContext(GenericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterReferenceTargetOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitReferenceTargetOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitReferenceTargetOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericEntityContext genericEntity() throws RecognitionException {
		GenericEntityContext _localctx = new GenericEntityContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_genericEntity);
		int _la;
		try {
			int _alt;
			setState(316);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				_localctx = new GenericDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(277);
				match(IF);
				setState(278);
				logicalExpression();
				setState(279);
				match(THEN);
				setState(280);
				genericEntity();
				setState(288);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(281);
					match(ELSEIF);
					setState(282);
					logicalExpression();
					setState(283);
					match(THEN);
					setState(284);
					genericEntity();
					}
					}
					setState(290);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(291);
				match(ELSE);
				setState(292);
				genericEntity();
				setState(293);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new GenericFunctionDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(295);
				match(IF);
				setState(296);
				match(LPAREN);
				setState(297);
				logicalExpression();
				setState(298);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(299);
				genericEntity();
				setState(307);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(300);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(301);
						logicalExpression();
						setState(302);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(303);
						genericEntity();
						}
						} 
					}
					setState(309);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
				}
				setState(310);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(311);
				genericEntity();
				setState(312);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new CastExpressionOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(314);
				castExpression();
				}
				break;
			case 4:
				_localctx = new ReferenceTargetOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(315);
				referenceTarget();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CastExpressionContext extends ParserRuleContext {
		public CastExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_castExpression; }
	 
		public CastExpressionContext() { }
		public void copyFrom(CastExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TypeCastOperationContext extends CastExpressionContext {
		public TypeHintContext typeHint() {
			return getRuleContext(TypeHintContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorV2Parser.LPAREN, 0); }
		public GenericEntityContext genericEntity() {
			return getRuleContext(GenericEntityContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorV2Parser.RPAREN, 0); }
		public TypeCastOperationContext(CastExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterTypeCastOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitTypeCastOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitTypeCastOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CastExpressionContext castExpression() throws RecognitionException {
		CastExpressionContext _localctx = new CastExpressionContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_castExpression);
		try {
			_localctx = new TypeCastOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(318);
			typeHint();
			setState(319);
			match(LPAREN);
			setState(320);
			genericEntity();
			setState(321);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeHintContext extends ParserRuleContext {
		public TypeHintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeHint; }
	 
		public TypeHintContext() { }
		public void copyFrom(TypeHintContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VectorTypeHintContext extends TypeHintContext {
		public TerminalNode VECTOR_TYPE() { return getToken(ExpressionEvaluatorV2Parser.VECTOR_TYPE, 0); }
		public VectorTypeHintContext(TypeHintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterVectorTypeHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitVectorTypeHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitVectorTypeHint(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringTypeHintContext extends TypeHintContext {
		public TerminalNode STRING_TYPE() { return getToken(ExpressionEvaluatorV2Parser.STRING_TYPE, 0); }
		public StringTypeHintContext(TypeHintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterStringTypeHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitStringTypeHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitStringTypeHint(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTypeHintContext extends TypeHintContext {
		public TerminalNode DATE_TYPE() { return getToken(ExpressionEvaluatorV2Parser.DATE_TYPE, 0); }
		public DateTypeHintContext(TypeHintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateTypeHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateTypeHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateTypeHint(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeTypeHintContext extends TypeHintContext {
		public TerminalNode TIME_TYPE() { return getToken(ExpressionEvaluatorV2Parser.TIME_TYPE, 0); }
		public TimeTypeHintContext(TypeHintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterTimeTypeHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitTimeTypeHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitTimeTypeHint(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BooleanTypeHintContext extends TypeHintContext {
		public TerminalNode BOOLEAN_TYPE() { return getToken(ExpressionEvaluatorV2Parser.BOOLEAN_TYPE, 0); }
		public BooleanTypeHintContext(TypeHintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterBooleanTypeHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitBooleanTypeHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitBooleanTypeHint(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumberTypeHintContext extends TypeHintContext {
		public TerminalNode NUMBER_TYPE() { return getToken(ExpressionEvaluatorV2Parser.NUMBER_TYPE, 0); }
		public NumberTypeHintContext(TypeHintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterNumberTypeHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitNumberTypeHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitNumberTypeHint(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeTypeHintContext extends TypeHintContext {
		public TerminalNode DATETIME_TYPE() { return getToken(ExpressionEvaluatorV2Parser.DATETIME_TYPE, 0); }
		public DateTimeTypeHintContext(TypeHintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateTimeTypeHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateTimeTypeHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateTimeTypeHint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeHintContext typeHint() throws RecognitionException {
		TypeHintContext _localctx = new TypeHintContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_typeHint);
		try {
			setState(330);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BOOLEAN_TYPE:
				_localctx = new BooleanTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(323);
				match(BOOLEAN_TYPE);
				}
				break;
			case NUMBER_TYPE:
				_localctx = new NumberTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(324);
				match(NUMBER_TYPE);
				}
				break;
			case STRING_TYPE:
				_localctx = new StringTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(325);
				match(STRING_TYPE);
				}
				break;
			case DATE_TYPE:
				_localctx = new DateTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(326);
				match(DATE_TYPE);
				}
				break;
			case TIME_TYPE:
				_localctx = new TimeTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(327);
				match(TIME_TYPE);
				}
				break;
			case DATETIME_TYPE:
				_localctx = new DateTimeTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(328);
				match(DATETIME_TYPE);
				}
				break;
			case VECTOR_TYPE:
				_localctx = new VectorTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(329);
				match(VECTOR_TYPE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalEntityContext extends ParserRuleContext {
		public LogicalEntityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalEntity; }
	 
		public LogicalEntityContext() { }
		public void copyFrom(LogicalEntityContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalConstantOperationContext extends LogicalEntityContext {
		public TerminalNode TRUE() { return getToken(ExpressionEvaluatorV2Parser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(ExpressionEvaluatorV2Parser.FALSE, 0); }
		public LogicalConstantOperationContext(LogicalEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalConstantOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalConstantOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalConstantOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalReferenceOperationContext extends LogicalEntityContext {
		public ReferenceTargetContext referenceTarget() {
			return getRuleContext(ReferenceTargetContext.class,0);
		}
		public TerminalNode BOOLEAN_TYPE() { return getToken(ExpressionEvaluatorV2Parser.BOOLEAN_TYPE, 0); }
		public LogicalReferenceOperationContext(LogicalEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalReferenceOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalReferenceOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalReferenceOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalFunctionDecisionOperationContext extends LogicalEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorV2Parser.LPAREN, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorV2Parser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorV2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorV2Parser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorV2Parser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorV2Parser.SEMI, i);
		}
		public LogicalFunctionDecisionOperationContext(LogicalEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalFunctionDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalFunctionDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalFunctionDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalDecisionOperationContext extends LogicalEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorV2Parser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorV2Parser.THEN, i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorV2Parser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorV2Parser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorV2Parser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorV2Parser.ELSEIF, i);
		}
		public LogicalDecisionOperationContext(LogicalEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterLogicalDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitLogicalDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitLogicalDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalEntityContext logicalEntity() throws RecognitionException {
		LogicalEntityContext _localctx = new LogicalEntityContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_logicalEntity);
		int _la;
		try {
			int _alt;
			setState(374);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				_localctx = new LogicalConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(332);
				_la = _input.LA(1);
				if ( !(_la==TRUE || _la==FALSE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 2:
				_localctx = new LogicalDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(333);
				match(IF);
				setState(334);
				logicalExpression();
				setState(335);
				match(THEN);
				setState(336);
				logicalExpression();
				setState(344);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(337);
					match(ELSEIF);
					setState(338);
					logicalExpression();
					setState(339);
					match(THEN);
					setState(340);
					logicalExpression();
					}
					}
					setState(346);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(347);
				match(ELSE);
				setState(348);
				logicalExpression();
				setState(349);
				match(ENDIF);
				}
				break;
			case 3:
				_localctx = new LogicalFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(351);
				match(IF);
				setState(352);
				match(LPAREN);
				setState(353);
				logicalExpression();
				setState(354);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(355);
				logicalExpression();
				setState(363);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(356);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(357);
						logicalExpression();
						setState(358);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(359);
						logicalExpression();
						}
						} 
					}
					setState(365);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
				}
				setState(366);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(367);
				logicalExpression();
				setState(368);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new LogicalReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(371);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==BOOLEAN_TYPE) {
					{
					setState(370);
					match(BOOLEAN_TYPE);
					}
				}

				setState(373);
				referenceTarget();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NumericEntityContext extends ParserRuleContext {
		public NumericEntityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericEntity; }
	 
		public NumericEntityContext() { }
		public void copyFrom(NumericEntityContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathFunctionDecisionOperationContext extends NumericEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorV2Parser.LPAREN, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorV2Parser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorV2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorV2Parser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorV2Parser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorV2Parser.SEMI, i);
		}
		public MathFunctionDecisionOperationContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterMathFunctionDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitMathFunctionDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitMathFunctionDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumericReferenceOperationContext extends NumericEntityContext {
		public ReferenceTargetContext referenceTarget() {
			return getRuleContext(ReferenceTargetContext.class,0);
		}
		public TerminalNode NUMBER_TYPE() { return getToken(ExpressionEvaluatorV2Parser.NUMBER_TYPE, 0); }
		public NumericReferenceOperationContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterNumericReferenceOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitNumericReferenceOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitNumericReferenceOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumericConstantOperationContext extends NumericEntityContext {
		public TerminalNode NUMBER() { return getToken(ExpressionEvaluatorV2Parser.NUMBER, 0); }
		public NumericConstantOperationContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterNumericConstantOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitNumericConstantOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitNumericConstantOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathDecisionOperationContext extends NumericEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorV2Parser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorV2Parser.THEN, i);
		}
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorV2Parser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorV2Parser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorV2Parser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorV2Parser.ELSEIF, i);
		}
		public MathDecisionOperationContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterMathDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitMathDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitMathDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericEntityContext numericEntity() throws RecognitionException {
		NumericEntityContext _localctx = new NumericEntityContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_numericEntity);
		int _la;
		try {
			int _alt;
			setState(418);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				_localctx = new MathDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(376);
				match(IF);
				setState(377);
				logicalExpression();
				setState(378);
				match(THEN);
				setState(379);
				mathExpression();
				setState(387);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(380);
					match(ELSEIF);
					setState(381);
					logicalExpression();
					setState(382);
					match(THEN);
					setState(383);
					mathExpression();
					}
					}
					setState(389);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(390);
				match(ELSE);
				setState(391);
				mathExpression();
				setState(392);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new MathFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(394);
				match(IF);
				setState(395);
				match(LPAREN);
				setState(396);
				logicalExpression();
				setState(397);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(398);
				mathExpression();
				setState(406);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(399);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(400);
						logicalExpression();
						setState(401);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(402);
						mathExpression();
						}
						} 
					}
					setState(408);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
				}
				setState(409);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(410);
				mathExpression();
				setState(411);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new NumericConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(413);
				match(NUMBER);
				}
				break;
			case 4:
				_localctx = new NumericReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(415);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NUMBER_TYPE) {
					{
					setState(414);
					match(NUMBER_TYPE);
					}
				}

				setState(417);
				referenceTarget();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StringEntityContext extends ParserRuleContext {
		public StringEntityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringEntity; }
	 
		public StringEntityContext() { }
		public void copyFrom(StringEntityContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringFunctionDecisionOperationContext extends StringEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorV2Parser.LPAREN, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<StringEntityContext> stringEntity() {
			return getRuleContexts(StringEntityContext.class);
		}
		public StringEntityContext stringEntity(int i) {
			return getRuleContext(StringEntityContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorV2Parser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorV2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorV2Parser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorV2Parser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorV2Parser.SEMI, i);
		}
		public StringFunctionDecisionOperationContext(StringEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterStringFunctionDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitStringFunctionDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitStringFunctionDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringReferenceOperationContext extends StringEntityContext {
		public ReferenceTargetContext referenceTarget() {
			return getRuleContext(ReferenceTargetContext.class,0);
		}
		public TerminalNode STRING_TYPE() { return getToken(ExpressionEvaluatorV2Parser.STRING_TYPE, 0); }
		public StringReferenceOperationContext(StringEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterStringReferenceOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitStringReferenceOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitStringReferenceOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringConstantOperationContext extends StringEntityContext {
		public TerminalNode STRING() { return getToken(ExpressionEvaluatorV2Parser.STRING, 0); }
		public StringConstantOperationContext(StringEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterStringConstantOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitStringConstantOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitStringConstantOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringDecisionOperationContext extends StringEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorV2Parser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorV2Parser.THEN, i);
		}
		public List<StringEntityContext> stringEntity() {
			return getRuleContexts(StringEntityContext.class);
		}
		public StringEntityContext stringEntity(int i) {
			return getRuleContext(StringEntityContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorV2Parser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorV2Parser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorV2Parser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorV2Parser.ELSEIF, i);
		}
		public StringDecisionOperationContext(StringEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterStringDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitStringDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitStringDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringEntityContext stringEntity() throws RecognitionException {
		StringEntityContext _localctx = new StringEntityContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_stringEntity);
		int _la;
		try {
			int _alt;
			setState(462);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				_localctx = new StringDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(420);
				match(IF);
				setState(421);
				logicalExpression();
				setState(422);
				match(THEN);
				setState(423);
				stringEntity();
				setState(431);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(424);
					match(ELSEIF);
					setState(425);
					logicalExpression();
					setState(426);
					match(THEN);
					setState(427);
					stringEntity();
					}
					}
					setState(433);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(434);
				match(ELSE);
				setState(435);
				stringEntity();
				setState(436);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new StringFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(438);
				match(IF);
				setState(439);
				match(LPAREN);
				setState(440);
				logicalExpression();
				setState(441);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(442);
				stringEntity();
				setState(450);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,37,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(443);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(444);
						logicalExpression();
						setState(445);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(446);
						stringEntity();
						}
						} 
					}
					setState(452);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,37,_ctx);
				}
				setState(453);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(454);
				stringEntity();
				setState(455);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new StringConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(457);
				match(STRING);
				}
				break;
			case 4:
				_localctx = new StringReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(459);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING_TYPE) {
					{
					setState(458);
					match(STRING_TYPE);
					}
				}

				setState(461);
				referenceTarget();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DateEntityContext extends ParserRuleContext {
		public DateEntityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateEntity; }
	 
		public DateEntityContext() { }
		public void copyFrom(DateEntityContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateDecisionOperationContext extends DateEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorV2Parser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorV2Parser.THEN, i);
		}
		public List<DateEntityContext> dateEntity() {
			return getRuleContexts(DateEntityContext.class);
		}
		public DateEntityContext dateEntity(int i) {
			return getRuleContext(DateEntityContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorV2Parser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorV2Parser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorV2Parser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorV2Parser.ELSEIF, i);
		}
		public DateDecisionOperationContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateFunctionDecisionOperationContext extends DateEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorV2Parser.LPAREN, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<DateEntityContext> dateEntity() {
			return getRuleContexts(DateEntityContext.class);
		}
		public DateEntityContext dateEntity(int i) {
			return getRuleContext(DateEntityContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorV2Parser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorV2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorV2Parser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorV2Parser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorV2Parser.SEMI, i);
		}
		public DateFunctionDecisionOperationContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateFunctionDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateFunctionDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateFunctionDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateReferenceOperationContext extends DateEntityContext {
		public ReferenceTargetContext referenceTarget() {
			return getRuleContext(ReferenceTargetContext.class,0);
		}
		public TerminalNode DATE_TYPE() { return getToken(ExpressionEvaluatorV2Parser.DATE_TYPE, 0); }
		public DateReferenceOperationContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateReferenceOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateReferenceOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateReferenceOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateCurrentValueOperationContext extends DateEntityContext {
		public TerminalNode NOW_DATE() { return getToken(ExpressionEvaluatorV2Parser.NOW_DATE, 0); }
		public DateCurrentValueOperationContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateCurrentValueOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateCurrentValueOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateCurrentValueOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateConstantOperationContext extends DateEntityContext {
		public TerminalNode DATE() { return getToken(ExpressionEvaluatorV2Parser.DATE, 0); }
		public DateConstantOperationContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateConstantOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateConstantOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateConstantOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateEntityContext dateEntity() throws RecognitionException {
		DateEntityContext _localctx = new DateEntityContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_dateEntity);
		int _la;
		try {
			int _alt;
			setState(507);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
			case 1:
				_localctx = new DateDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(464);
				match(IF);
				setState(465);
				logicalExpression();
				setState(466);
				match(THEN);
				setState(467);
				dateEntity();
				setState(475);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(468);
					match(ELSEIF);
					setState(469);
					logicalExpression();
					setState(470);
					match(THEN);
					setState(471);
					dateEntity();
					}
					}
					setState(477);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(478);
				match(ELSE);
				setState(479);
				dateEntity();
				setState(480);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new DateFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(482);
				match(IF);
				setState(483);
				match(LPAREN);
				setState(484);
				logicalExpression();
				setState(485);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(486);
				dateEntity();
				setState(494);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,41,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(487);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(488);
						logicalExpression();
						setState(489);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(490);
						dateEntity();
						}
						} 
					}
					setState(496);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,41,_ctx);
				}
				setState(497);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(498);
				dateEntity();
				setState(499);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new DateConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(501);
				match(DATE);
				}
				break;
			case 4:
				_localctx = new DateCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(502);
				match(NOW_DATE);
				}
				break;
			case 5:
				_localctx = new DateReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(504);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DATE_TYPE) {
					{
					setState(503);
					match(DATE_TYPE);
					}
				}

				setState(506);
				referenceTarget();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TimeEntityContext extends ParserRuleContext {
		public TimeEntityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeEntity; }
	 
		public TimeEntityContext() { }
		public void copyFrom(TimeEntityContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeCurrentValueOperationContext extends TimeEntityContext {
		public TerminalNode NOW_TIME() { return getToken(ExpressionEvaluatorV2Parser.NOW_TIME, 0); }
		public TimeCurrentValueOperationContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterTimeCurrentValueOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitTimeCurrentValueOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitTimeCurrentValueOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeFunctionDecisionOperationContext extends TimeEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorV2Parser.LPAREN, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TimeEntityContext> timeEntity() {
			return getRuleContexts(TimeEntityContext.class);
		}
		public TimeEntityContext timeEntity(int i) {
			return getRuleContext(TimeEntityContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorV2Parser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorV2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorV2Parser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorV2Parser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorV2Parser.SEMI, i);
		}
		public TimeFunctionDecisionOperationContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterTimeFunctionDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitTimeFunctionDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitTimeFunctionDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeConstantOperationContext extends TimeEntityContext {
		public TerminalNode TIME() { return getToken(ExpressionEvaluatorV2Parser.TIME, 0); }
		public TimeConstantOperationContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterTimeConstantOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitTimeConstantOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitTimeConstantOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeDecisionOperationContext extends TimeEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorV2Parser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorV2Parser.THEN, i);
		}
		public List<TimeEntityContext> timeEntity() {
			return getRuleContexts(TimeEntityContext.class);
		}
		public TimeEntityContext timeEntity(int i) {
			return getRuleContext(TimeEntityContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorV2Parser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorV2Parser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorV2Parser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorV2Parser.ELSEIF, i);
		}
		public TimeDecisionOperationContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterTimeDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitTimeDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitTimeDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeReferenceOperationContext extends TimeEntityContext {
		public ReferenceTargetContext referenceTarget() {
			return getRuleContext(ReferenceTargetContext.class,0);
		}
		public TerminalNode TIME_TYPE() { return getToken(ExpressionEvaluatorV2Parser.TIME_TYPE, 0); }
		public TimeReferenceOperationContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterTimeReferenceOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitTimeReferenceOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitTimeReferenceOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TimeEntityContext timeEntity() throws RecognitionException {
		TimeEntityContext _localctx = new TimeEntityContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_timeEntity);
		int _la;
		try {
			int _alt;
			setState(552);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
			case 1:
				_localctx = new TimeDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(509);
				match(IF);
				setState(510);
				logicalExpression();
				setState(511);
				match(THEN);
				setState(512);
				timeEntity();
				setState(520);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(513);
					match(ELSEIF);
					setState(514);
					logicalExpression();
					setState(515);
					match(THEN);
					setState(516);
					timeEntity();
					}
					}
					setState(522);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(523);
				match(ELSE);
				setState(524);
				timeEntity();
				setState(525);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new TimeFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(527);
				match(IF);
				setState(528);
				match(LPAREN);
				setState(529);
				logicalExpression();
				setState(530);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(531);
				timeEntity();
				setState(539);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,45,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(532);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(533);
						logicalExpression();
						setState(534);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(535);
						timeEntity();
						}
						} 
					}
					setState(541);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,45,_ctx);
				}
				setState(542);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(543);
				timeEntity();
				setState(544);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new TimeConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(546);
				match(TIME);
				}
				break;
			case 4:
				_localctx = new TimeCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(547);
				match(NOW_TIME);
				}
				break;
			case 5:
				_localctx = new TimeReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(549);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TIME_TYPE) {
					{
					setState(548);
					match(TIME_TYPE);
					}
				}

				setState(551);
				referenceTarget();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeEntityContext extends ParserRuleContext {
		public DateTimeEntityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateTimeEntity; }
	 
		public DateTimeEntityContext() { }
		public void copyFrom(DateTimeEntityContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeReferenceOperationContext extends DateTimeEntityContext {
		public ReferenceTargetContext referenceTarget() {
			return getRuleContext(ReferenceTargetContext.class,0);
		}
		public TerminalNode DATETIME_TYPE() { return getToken(ExpressionEvaluatorV2Parser.DATETIME_TYPE, 0); }
		public DateTimeReferenceOperationContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateTimeReferenceOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateTimeReferenceOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateTimeReferenceOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeFunctionDecisionOperationContext extends DateTimeEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorV2Parser.LPAREN, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<DateTimeEntityContext> dateTimeEntity() {
			return getRuleContexts(DateTimeEntityContext.class);
		}
		public DateTimeEntityContext dateTimeEntity(int i) {
			return getRuleContext(DateTimeEntityContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorV2Parser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorV2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorV2Parser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorV2Parser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorV2Parser.SEMI, i);
		}
		public DateTimeFunctionDecisionOperationContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateTimeFunctionDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateTimeFunctionDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateTimeFunctionDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeDecisionOperationContext extends DateTimeEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorV2Parser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorV2Parser.THEN, i);
		}
		public List<DateTimeEntityContext> dateTimeEntity() {
			return getRuleContexts(DateTimeEntityContext.class);
		}
		public DateTimeEntityContext dateTimeEntity(int i) {
			return getRuleContext(DateTimeEntityContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorV2Parser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorV2Parser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorV2Parser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorV2Parser.ELSEIF, i);
		}
		public DateTimeDecisionOperationContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateTimeDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateTimeDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateTimeDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeConstantOperationContext extends DateTimeEntityContext {
		public TerminalNode DATETIME() { return getToken(ExpressionEvaluatorV2Parser.DATETIME, 0); }
		public TerminalNode TIME_OFFSET() { return getToken(ExpressionEvaluatorV2Parser.TIME_OFFSET, 0); }
		public DateTimeConstantOperationContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateTimeConstantOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateTimeConstantOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateTimeConstantOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeCurrentValueOperationContext extends DateTimeEntityContext {
		public TerminalNode NOW_DATETIME() { return getToken(ExpressionEvaluatorV2Parser.NOW_DATETIME, 0); }
		public DateTimeCurrentValueOperationContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterDateTimeCurrentValueOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitDateTimeCurrentValueOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitDateTimeCurrentValueOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateTimeEntityContext dateTimeEntity() throws RecognitionException {
		DateTimeEntityContext _localctx = new DateTimeEntityContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_dateTimeEntity);
		int _la;
		try {
			int _alt;
			setState(600);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,52,_ctx) ) {
			case 1:
				_localctx = new DateTimeDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(554);
				match(IF);
				setState(555);
				logicalExpression();
				setState(556);
				match(THEN);
				setState(557);
				dateTimeEntity();
				setState(565);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(558);
					match(ELSEIF);
					setState(559);
					logicalExpression();
					setState(560);
					match(THEN);
					setState(561);
					dateTimeEntity();
					}
					}
					setState(567);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(568);
				match(ELSE);
				setState(569);
				dateTimeEntity();
				setState(570);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new DateTimeFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(572);
				match(IF);
				setState(573);
				match(LPAREN);
				setState(574);
				logicalExpression();
				setState(575);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(576);
				dateTimeEntity();
				setState(584);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(577);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(578);
						logicalExpression();
						setState(579);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(580);
						dateTimeEntity();
						}
						} 
					}
					setState(586);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
				}
				setState(587);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(588);
				dateTimeEntity();
				setState(589);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new DateTimeConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(591);
				match(DATETIME);
				setState(593);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TIME_OFFSET) {
					{
					setState(592);
					match(TIME_OFFSET);
					}
				}

				}
				break;
			case 4:
				_localctx = new DateTimeCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(595);
				match(NOW_DATETIME);
				}
				break;
			case 5:
				_localctx = new DateTimeReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(597);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DATETIME_TYPE) {
					{
					setState(596);
					match(DATETIME_TYPE);
					}
				}

				setState(599);
				referenceTarget();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VectorEntityContext extends ParserRuleContext {
		public VectorEntityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vectorEntity; }
	 
		public VectorEntityContext() { }
		public void copyFrom(VectorEntityContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VectorFunctionDecisionOperationContext extends VectorEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorV2Parser.LPAREN, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<VectorEntityContext> vectorEntity() {
			return getRuleContexts(VectorEntityContext.class);
		}
		public VectorEntityContext vectorEntity(int i) {
			return getRuleContext(VectorEntityContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorV2Parser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorV2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorV2Parser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorV2Parser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorV2Parser.SEMI, i);
		}
		public VectorFunctionDecisionOperationContext(VectorEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterVectorFunctionDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitVectorFunctionDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitVectorFunctionDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VectorReferenceOperationContext extends VectorEntityContext {
		public ReferenceTargetContext referenceTarget() {
			return getRuleContext(ReferenceTargetContext.class,0);
		}
		public TerminalNode VECTOR_TYPE() { return getToken(ExpressionEvaluatorV2Parser.VECTOR_TYPE, 0); }
		public VectorReferenceOperationContext(VectorEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterVectorReferenceOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitVectorReferenceOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitVectorReferenceOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VectorDecisionOperationContext extends VectorEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorV2Parser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorV2Parser.THEN, i);
		}
		public List<VectorEntityContext> vectorEntity() {
			return getRuleContexts(VectorEntityContext.class);
		}
		public VectorEntityContext vectorEntity(int i) {
			return getRuleContext(VectorEntityContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorV2Parser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorV2Parser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorV2Parser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorV2Parser.ELSEIF, i);
		}
		public VectorDecisionOperationContext(VectorEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterVectorDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitVectorDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitVectorDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VectorOfEntitiesOperationContext extends VectorEntityContext {
		public TerminalNode LBRACKET() { return getToken(ExpressionEvaluatorV2Parser.LBRACKET, 0); }
		public List<AllEntityTypesContext> allEntityTypes() {
			return getRuleContexts(AllEntityTypesContext.class);
		}
		public AllEntityTypesContext allEntityTypes(int i) {
			return getRuleContext(AllEntityTypesContext.class,i);
		}
		public TerminalNode RBRACKET() { return getToken(ExpressionEvaluatorV2Parser.RBRACKET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorV2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorV2Parser.COMMA, i);
		}
		public VectorOfEntitiesOperationContext(VectorEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterVectorOfEntitiesOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitVectorOfEntitiesOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitVectorOfEntitiesOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VectorEntityContext vectorEntity() throws RecognitionException {
		VectorEntityContext _localctx = new VectorEntityContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_vectorEntity);
		int _la;
		try {
			int _alt;
			setState(654);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
			case 1:
				_localctx = new VectorDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(602);
				match(IF);
				setState(603);
				logicalExpression();
				setState(604);
				match(THEN);
				setState(605);
				vectorEntity();
				setState(613);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(606);
					match(ELSEIF);
					setState(607);
					logicalExpression();
					setState(608);
					match(THEN);
					setState(609);
					vectorEntity();
					}
					}
					setState(615);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(616);
				match(ELSE);
				setState(617);
				vectorEntity();
				setState(618);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new VectorFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(620);
				match(IF);
				setState(621);
				match(LPAREN);
				setState(622);
				logicalExpression();
				setState(623);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(624);
				vectorEntity();
				setState(632);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,54,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(625);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(626);
						logicalExpression();
						setState(627);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(628);
						vectorEntity();
						}
						} 
					}
					setState(634);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,54,_ctx);
				}
				setState(635);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(636);
				vectorEntity();
				setState(637);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new VectorOfEntitiesOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(639);
				match(LBRACKET);
				setState(640);
				allEntityTypes();
				setState(645);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(641);
					match(COMMA);
					setState(642);
					allEntityTypes();
					}
					}
					setState(647);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(648);
				match(RBRACKET);
				}
				break;
			case 4:
				_localctx = new VectorReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(651);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==VECTOR_TYPE) {
					{
					setState(650);
					match(VECTOR_TYPE);
					}
				}

				setState(653);
				referenceTarget();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VectorOfVariablesContext extends ParserRuleContext {
		public VectorOfVariablesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vectorOfVariables; }
	 
		public VectorOfVariablesContext() { }
		public void copyFrom(VectorOfVariablesContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VectorOfVariablesOperationContext extends VectorOfVariablesContext {
		public TerminalNode LBRACKET() { return getToken(ExpressionEvaluatorV2Parser.LBRACKET, 0); }
		public List<TerminalNode> IDENTIFIER() { return getTokens(ExpressionEvaluatorV2Parser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(ExpressionEvaluatorV2Parser.IDENTIFIER, i);
		}
		public TerminalNode RBRACKET() { return getToken(ExpressionEvaluatorV2Parser.RBRACKET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorV2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorV2Parser.COMMA, i);
		}
		public VectorOfVariablesOperationContext(VectorOfVariablesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterVectorOfVariablesOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitVectorOfVariablesOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitVectorOfVariablesOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VectorOfVariablesContext vectorOfVariables() throws RecognitionException {
		VectorOfVariablesContext _localctx = new VectorOfVariablesContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_vectorOfVariables);
		int _la;
		try {
			_localctx = new VectorOfVariablesOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(656);
			match(LBRACKET);
			setState(657);
			match(IDENTIFIER);
			setState(662);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(658);
				match(COMMA);
				setState(659);
				match(IDENTIFIER);
				}
				}
				setState(664);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(665);
			match(RBRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001<\u029c\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0001"+
		"\u0000\u0005\u0000H\b\u0000\n\u0000\f\u0000K\t\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0001\u0004\u0001Q\b\u0001\u000b\u0001\f\u0001"+
		"R\u0001\u0001\u0001\u0001\u0001\u0002\u0005\u0002X\b\u0002\n\u0002\f\u0002"+
		"[\t\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0003\u0003j\b\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005q\b\u0005\n\u0005\f\u0005"+
		"t\t\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006y\b\u0006\n\u0006"+
		"\f\u0006|\t\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003"+
		"\u0007\u0082\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0098\b\u0007\u0001"+
		"\b\u0001\b\u0001\b\u0005\b\u009d\b\b\n\b\f\b\u00a0\t\b\u0001\t\u0001\t"+
		"\u0001\t\u0003\t\u00a5\b\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003"+
		"\n\u00ac\b\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0005\f\u00b3"+
		"\b\f\n\f\f\f\u00b6\t\f\u0001\r\u0001\r\u0001\r\u0005\r\u00bb\b\r\n\r\f"+
		"\r\u00be\t\r\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u00c3\b\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u00c8\b\u000f\n\u000f"+
		"\f\u000f\u00cb\t\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010"+
		"\u00d0\b\u0010\u0001\u0011\u0001\u0011\u0005\u0011\u00d4\b\u0011\n\u0011"+
		"\f\u0011\u00d7\t\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u00e7\b\u0012"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013"+
		"\u00ee\b\u0013\n\u0013\f\u0013\u00f1\t\u0013\u0003\u0013\u00f3\b\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0003\u0014\u00f9\b\u0014"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0003\u0015\u0101\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u010a\b\u0016\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0003\u0017\u0114\b\u0017\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0005\u0018\u011f\b\u0018\n\u0018\f\u0018\u0122\t\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0005\u0018\u0132\b\u0018\n\u0018\f\u0018\u0135\t\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018"+
		"\u013d\b\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a"+
		"\u0001\u001a\u0003\u001a\u014b\b\u001a\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0005\u001b\u0157\b\u001b\n\u001b\f\u001b\u015a\t\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0005\u001b\u016a\b\u001b\n\u001b\f\u001b\u016d\t\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0003\u001b"+
		"\u0174\b\u001b\u0001\u001b\u0003\u001b\u0177\b\u001b\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0005\u001c\u0182\b\u001c\n\u001c\f\u001c\u0185\t\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0005\u001c\u0195\b\u001c\n\u001c\f\u001c\u0198"+
		"\t\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0003\u001c\u01a0\b\u001c\u0001\u001c\u0003\u001c\u01a3\b\u001c"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d\u01ae\b\u001d\n\u001d"+
		"\f\u001d\u01b1\t\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d\u01c1\b\u001d"+
		"\n\u001d\f\u001d\u01c4\t\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001d\u0001\u001d\u0003\u001d\u01cc\b\u001d\u0001\u001d\u0003"+
		"\u001d\u01cf\b\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u01da"+
		"\b\u001e\n\u001e\f\u001e\u01dd\t\u001e\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0005\u001e"+
		"\u01ed\b\u001e\n\u001e\f\u001e\u01f0\t\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u01f9"+
		"\b\u001e\u0001\u001e\u0003\u001e\u01fc\b\u001e\u0001\u001f\u0001\u001f"+
		"\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f"+
		"\u0001\u001f\u0005\u001f\u0207\b\u001f\n\u001f\f\u001f\u020a\t\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0005\u001f\u021a\b\u001f\n\u001f\f\u001f\u021d\t\u001f"+
		"\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f"+
		"\u0001\u001f\u0003\u001f\u0226\b\u001f\u0001\u001f\u0003\u001f\u0229\b"+
		"\u001f\u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 "+
		"\u0005 \u0234\b \n \f \u0237\t \u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0005 \u0247"+
		"\b \n \f \u024a\t \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0003 \u0252"+
		"\b \u0001 \u0001 \u0003 \u0256\b \u0001 \u0003 \u0259\b \u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0005!\u0264\b!\n!"+
		"\f!\u0267\t!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0005!\u0277\b!\n!\f!\u027a\t!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0005!\u0284\b!\n!"+
		"\f!\u0287\t!\u0001!\u0001!\u0001!\u0003!\u028c\b!\u0001!\u0003!\u028f"+
		"\b!\u0001\"\u0001\"\u0001\"\u0001\"\u0005\"\u0295\b\"\n\"\f\"\u0298\t"+
		"\"\u0001\"\u0001\"\u0001\"\u0000\u0000#\u0000\u0002\u0004\u0006\b\n\f"+
		"\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:"+
		"<>@BD\u0000\u0007\u0001\u0000\b\u000b\u0002\u0000\u0015\u0015\u001f\u001f"+
		"\u0001\u0000\u0010\u0011\u0002\u0000\u000e\u000f\u0013\u0013\u0002\u0000"+
		"\u0012\u0012\u0015\u0015\u0001\u0000\'(\u0001\u0000\f\r\u02e0\u0000I\u0001"+
		"\u0000\u0000\u0000\u0002P\u0001\u0000\u0000\u0000\u0004Y\u0001\u0000\u0000"+
		"\u0000\u0006i\u0001\u0000\u0000\u0000\bk\u0001\u0000\u0000\u0000\nm\u0001"+
		"\u0000\u0000\u0000\fu\u0001\u0000\u0000\u0000\u000e\u0097\u0001\u0000"+
		"\u0000\u0000\u0010\u0099\u0001\u0000\u0000\u0000\u0012\u00a4\u0001\u0000"+
		"\u0000\u0000\u0014\u00ab\u0001\u0000\u0000\u0000\u0016\u00ad\u0001\u0000"+
		"\u0000\u0000\u0018\u00af\u0001\u0000\u0000\u0000\u001a\u00b7\u0001\u0000"+
		"\u0000\u0000\u001c\u00c2\u0001\u0000\u0000\u0000\u001e\u00c4\u0001\u0000"+
		"\u0000\u0000 \u00cc\u0001\u0000\u0000\u0000\"\u00d1\u0001\u0000\u0000"+
		"\u0000$\u00e6\u0001\u0000\u0000\u0000&\u00e8\u0001\u0000\u0000\u0000("+
		"\u00f8\u0001\u0000\u0000\u0000*\u0100\u0001\u0000\u0000\u0000,\u0109\u0001"+
		"\u0000\u0000\u0000.\u0113\u0001\u0000\u0000\u00000\u013c\u0001\u0000\u0000"+
		"\u00002\u013e\u0001\u0000\u0000\u00004\u014a\u0001\u0000\u0000\u00006"+
		"\u0176\u0001\u0000\u0000\u00008\u01a2\u0001\u0000\u0000\u0000:\u01ce\u0001"+
		"\u0000\u0000\u0000<\u01fb\u0001\u0000\u0000\u0000>\u0228\u0001\u0000\u0000"+
		"\u0000@\u0258\u0001\u0000\u0000\u0000B\u028e\u0001\u0000\u0000\u0000D"+
		"\u0290\u0001\u0000\u0000\u0000FH\u0003\u0006\u0003\u0000GF\u0001\u0000"+
		"\u0000\u0000HK\u0001\u0000\u0000\u0000IG\u0001\u0000\u0000\u0000IJ\u0001"+
		"\u0000\u0000\u0000JL\u0001\u0000\u0000\u0000KI\u0001\u0000\u0000\u0000"+
		"LM\u0003\u0016\u000b\u0000MN\u0005\u0000\u0000\u0001N\u0001\u0001\u0000"+
		"\u0000\u0000OQ\u0003\u0006\u0003\u0000PO\u0001\u0000\u0000\u0000QR\u0001"+
		"\u0000\u0000\u0000RP\u0001\u0000\u0000\u0000RS\u0001\u0000\u0000\u0000"+
		"ST\u0001\u0000\u0000\u0000TU\u0005\u0000\u0000\u0001U\u0003\u0001\u0000"+
		"\u0000\u0000VX\u0003\u0006\u0003\u0000WV\u0001\u0000\u0000\u0000X[\u0001"+
		"\u0000\u0000\u0000YW\u0001\u0000\u0000\u0000YZ\u0001\u0000\u0000\u0000"+
		"Z\\\u0001\u0000\u0000\u0000[Y\u0001\u0000\u0000\u0000\\]\u0003\b\u0004"+
		"\u0000]^\u0005\u0000\u0000\u0001^\u0005\u0001\u0000\u0000\u0000_`\u0005"+
		"*\u0000\u0000`a\u0005\u001d\u0000\u0000ab\u0003.\u0017\u0000bc\u0005("+
		"\u0000\u0000cj\u0001\u0000\u0000\u0000de\u0003D\"\u0000ef\u0005\u001d"+
		"\u0000\u0000fg\u0003B!\u0000gh\u0005(\u0000\u0000hj\u0001\u0000\u0000"+
		"\u0000i_\u0001\u0000\u0000\u0000id\u0001\u0000\u0000\u0000j\u0007\u0001"+
		"\u0000\u0000\u0000kl\u0003\n\u0005\u0000l\t\u0001\u0000\u0000\u0000mr"+
		"\u0003\f\u0006\u0000no\u0005\u0007\u0000\u0000oq\u0003\f\u0006\u0000p"+
		"n\u0001\u0000\u0000\u0000qt\u0001\u0000\u0000\u0000rp\u0001\u0000\u0000"+
		"\u0000rs\u0001\u0000\u0000\u0000s\u000b\u0001\u0000\u0000\u0000tr\u0001"+
		"\u0000\u0000\u0000uz\u0003\u000e\u0007\u0000vw\u0005\u0006\u0000\u0000"+
		"wy\u0003\u000e\u0007\u0000xv\u0001\u0000\u0000\u0000y|\u0001\u0000\u0000"+
		"\u0000zx\u0001\u0000\u0000\u0000z{\u0001\u0000\u0000\u0000{\r\u0001\u0000"+
		"\u0000\u0000|z\u0001\u0000\u0000\u0000}\u0081\u0003\u0010\b\u0000~\u007f"+
		"\u0003*\u0015\u0000\u007f\u0080\u0003\u0010\b\u0000\u0080\u0082\u0001"+
		"\u0000\u0000\u0000\u0081~\u0001\u0000\u0000\u0000\u0081\u0082\u0001\u0000"+
		"\u0000\u0000\u0082\u0098\u0001\u0000\u0000\u0000\u0083\u0084\u0003\u0016"+
		"\u000b\u0000\u0084\u0085\u0003*\u0015\u0000\u0085\u0086\u0003\u0016\u000b"+
		"\u0000\u0086\u0098\u0001\u0000\u0000\u0000\u0087\u0088\u0003:\u001d\u0000"+
		"\u0088\u0089\u0003*\u0015\u0000\u0089\u008a\u0003:\u001d\u0000\u008a\u0098"+
		"\u0001\u0000\u0000\u0000\u008b\u008c\u0003<\u001e\u0000\u008c\u008d\u0003"+
		"*\u0015\u0000\u008d\u008e\u0003<\u001e\u0000\u008e\u0098\u0001\u0000\u0000"+
		"\u0000\u008f\u0090\u0003>\u001f\u0000\u0090\u0091\u0003*\u0015\u0000\u0091"+
		"\u0092\u0003>\u001f\u0000\u0092\u0098\u0001\u0000\u0000\u0000\u0093\u0094"+
		"\u0003@ \u0000\u0094\u0095\u0003*\u0015\u0000\u0095\u0096\u0003@ \u0000"+
		"\u0096\u0098\u0001\u0000\u0000\u0000\u0097}\u0001\u0000\u0000\u0000\u0097"+
		"\u0083\u0001\u0000\u0000\u0000\u0097\u0087\u0001\u0000\u0000\u0000\u0097"+
		"\u008b\u0001\u0000\u0000\u0000\u0097\u008f\u0001\u0000\u0000\u0000\u0097"+
		"\u0093\u0001\u0000\u0000\u0000\u0098\u000f\u0001\u0000\u0000\u0000\u0099"+
		"\u009e\u0003\u0012\t\u0000\u009a\u009b\u0007\u0000\u0000\u0000\u009b\u009d"+
		"\u0003\u0012\t\u0000\u009c\u009a\u0001\u0000\u0000\u0000\u009d\u00a0\u0001"+
		"\u0000\u0000\u0000\u009e\u009c\u0001\u0000\u0000\u0000\u009e\u009f\u0001"+
		"\u0000\u0000\u0000\u009f\u0011\u0001\u0000\u0000\u0000\u00a0\u009e\u0001"+
		"\u0000\u0000\u0000\u00a1\u00a2\u0007\u0001\u0000\u0000\u00a2\u00a5\u0003"+
		"\u0012\t\u0000\u00a3\u00a5\u0003\u0014\n\u0000\u00a4\u00a1\u0001\u0000"+
		"\u0000\u0000\u00a4\u00a3\u0001\u0000\u0000\u0000\u00a5\u0013\u0001\u0000"+
		"\u0000\u0000\u00a6\u00a7\u0005#\u0000\u0000\u00a7\u00a8\u0003\b\u0004"+
		"\u0000\u00a8\u00a9\u0005$\u0000\u0000\u00a9\u00ac\u0001\u0000\u0000\u0000"+
		"\u00aa\u00ac\u00036\u001b\u0000\u00ab\u00a6\u0001\u0000\u0000\u0000\u00ab"+
		"\u00aa\u0001\u0000\u0000\u0000\u00ac\u0015\u0001\u0000\u0000\u0000\u00ad"+
		"\u00ae\u0003\u0018\f\u0000\u00ae\u0017\u0001\u0000\u0000\u0000\u00af\u00b4"+
		"\u0003\u001a\r\u0000\u00b0\u00b1\u0007\u0002\u0000\u0000\u00b1\u00b3\u0003"+
		"\u001a\r\u0000\u00b2\u00b0\u0001\u0000\u0000\u0000\u00b3\u00b6\u0001\u0000"+
		"\u0000\u0000\u00b4\u00b2\u0001\u0000\u0000\u0000\u00b4\u00b5\u0001\u0000"+
		"\u0000\u0000\u00b5\u0019\u0001\u0000\u0000\u0000\u00b6\u00b4\u0001\u0000"+
		"\u0000\u0000\u00b7\u00bc\u0003\u001c\u000e\u0000\u00b8\u00b9\u0007\u0003"+
		"\u0000\u0000\u00b9\u00bb\u0003\u001c\u000e\u0000\u00ba\u00b8\u0001\u0000"+
		"\u0000\u0000\u00bb\u00be\u0001\u0000\u0000\u0000\u00bc\u00ba\u0001\u0000"+
		"\u0000\u0000\u00bc\u00bd\u0001\u0000\u0000\u0000\u00bd\u001b\u0001\u0000"+
		"\u0000\u0000\u00be\u00bc\u0001\u0000\u0000\u0000\u00bf\u00c0\u0005\u0011"+
		"\u0000\u0000\u00c0\u00c3\u0003\u001c\u000e\u0000\u00c1\u00c3\u0003\u001e"+
		"\u000f\u0000\u00c2\u00bf\u0001\u0000\u0000\u0000\u00c2\u00c1\u0001\u0000"+
		"\u0000\u0000\u00c3\u001d\u0001\u0000\u0000\u0000\u00c4\u00c9\u0003 \u0010"+
		"\u0000\u00c5\u00c6\u0005\u0017\u0000\u0000\u00c6\u00c8\u0003 \u0010\u0000"+
		"\u00c7\u00c5\u0001\u0000\u0000\u0000\u00c8\u00cb\u0001\u0000\u0000\u0000"+
		"\u00c9\u00c7\u0001\u0000\u0000\u0000\u00c9\u00ca\u0001\u0000\u0000\u0000"+
		"\u00ca\u001f\u0001\u0000\u0000\u0000\u00cb\u00c9\u0001\u0000\u0000\u0000"+
		"\u00cc\u00cf\u0003\"\u0011\u0000\u00cd\u00ce\u0005\u0016\u0000\u0000\u00ce"+
		"\u00d0\u0003\u001c\u000e\u0000\u00cf\u00cd\u0001\u0000\u0000\u0000\u00cf"+
		"\u00d0\u0001\u0000\u0000\u0000\u00d0!\u0001\u0000\u0000\u0000\u00d1\u00d5"+
		"\u0003$\u0012\u0000\u00d2\u00d4\u0007\u0004\u0000\u0000\u00d3\u00d2\u0001"+
		"\u0000\u0000\u0000\u00d4\u00d7\u0001\u0000\u0000\u0000\u00d5\u00d3\u0001"+
		"\u0000\u0000\u0000\u00d5\u00d6\u0001\u0000\u0000\u0000\u00d6#\u0001\u0000"+
		"\u0000\u0000\u00d7\u00d5\u0001\u0000\u0000\u0000\u00d8\u00d9\u0005#\u0000"+
		"\u0000\u00d9\u00da\u0003\u0016\u000b\u0000\u00da\u00db\u0005$\u0000\u0000"+
		"\u00db\u00e7\u0001\u0000\u0000\u0000\u00dc\u00dd\u0005\u0018\u0000\u0000"+
		"\u00dd\u00de\u0005#\u0000\u0000\u00de\u00df\u0003\u0016\u000b\u0000\u00df"+
		"\u00e0\u0005$\u0000\u0000\u00e0\u00e7\u0001\u0000\u0000\u0000\u00e1\u00e2"+
		"\u0005\u0014\u0000\u0000\u00e2\u00e3\u0003\u0016\u000b\u0000\u00e3\u00e4"+
		"\u0005\u0014\u0000\u0000\u00e4\u00e7\u0001\u0000\u0000\u0000\u00e5\u00e7"+
		"\u00038\u001c\u0000\u00e6\u00d8\u0001\u0000\u0000\u0000\u00e6\u00dc\u0001"+
		"\u0000\u0000\u0000\u00e6\u00e1\u0001\u0000\u0000\u0000\u00e6\u00e5\u0001"+
		"\u0000\u0000\u0000\u00e7%\u0001\u0000\u0000\u0000\u00e8\u00e9\u0005*\u0000"+
		"\u0000\u00e9\u00f2\u0005#\u0000\u0000\u00ea\u00ef\u0003,\u0016\u0000\u00eb"+
		"\u00ec\u0007\u0005\u0000\u0000\u00ec\u00ee\u0003,\u0016\u0000\u00ed\u00eb"+
		"\u0001\u0000\u0000\u0000\u00ee\u00f1\u0001\u0000\u0000\u0000\u00ef\u00ed"+
		"\u0001\u0000\u0000\u0000\u00ef\u00f0\u0001\u0000\u0000\u0000\u00f0\u00f3"+
		"\u0001\u0000\u0000\u0000\u00f1\u00ef\u0001\u0000\u0000\u0000\u00f2\u00ea"+
		"\u0001\u0000\u0000\u0000\u00f2\u00f3\u0001\u0000\u0000\u0000\u00f3\u00f4"+
		"\u0001\u0000\u0000\u0000\u00f4\u00f5\u0005$\u0000\u0000\u00f5\'\u0001"+
		"\u0000\u0000\u0000\u00f6\u00f9\u0003&\u0013\u0000\u00f7\u00f9\u0005*\u0000"+
		"\u0000\u00f8\u00f6\u0001\u0000\u0000\u0000\u00f8\u00f7\u0001\u0000\u0000"+
		"\u0000\u00f9)\u0001\u0000\u0000\u0000\u00fa\u0101\u0005\u0019\u0000\u0000"+
		"\u00fb\u0101\u0005\u001a\u0000\u0000\u00fc\u0101\u0005\u001b\u0000\u0000"+
		"\u00fd\u0101\u0005\u001c\u0000\u0000\u00fe\u0101\u0005\u001d\u0000\u0000"+
		"\u00ff\u0101\u0005\u001e\u0000\u0000\u0100\u00fa\u0001\u0000\u0000\u0000"+
		"\u0100\u00fb\u0001\u0000\u0000\u0000\u0100\u00fc\u0001\u0000\u0000\u0000"+
		"\u0100\u00fd\u0001\u0000\u0000\u0000\u0100\u00fe\u0001\u0000\u0000\u0000"+
		"\u0100\u00ff\u0001\u0000\u0000\u0000\u0101+\u0001\u0000\u0000\u0000\u0102"+
		"\u010a\u0003\u0016\u000b\u0000\u0103\u010a\u0003\b\u0004\u0000\u0104\u010a"+
		"\u0003<\u001e\u0000\u0105\u010a\u0003>\u001f\u0000\u0106\u010a\u0003@"+
		" \u0000\u0107\u010a\u0003:\u001d\u0000\u0108\u010a\u0003B!\u0000\u0109"+
		"\u0102\u0001\u0000\u0000\u0000\u0109\u0103\u0001\u0000\u0000\u0000\u0109"+
		"\u0104\u0001\u0000\u0000\u0000\u0109\u0105\u0001\u0000\u0000\u0000\u0109"+
		"\u0106\u0001\u0000\u0000\u0000\u0109\u0107\u0001\u0000\u0000\u0000\u0109"+
		"\u0108\u0001\u0000\u0000\u0000\u010a-\u0001\u0000\u0000\u0000\u010b\u0114"+
		"\u00030\u0018\u0000\u010c\u0114\u0003\u0016\u000b\u0000\u010d\u0114\u0003"+
		"\b\u0004\u0000\u010e\u0114\u0003<\u001e\u0000\u010f\u0114\u0003>\u001f"+
		"\u0000\u0110\u0114\u0003@ \u0000\u0111\u0114\u0003:\u001d\u0000\u0112"+
		"\u0114\u0003B!\u0000\u0113\u010b\u0001\u0000\u0000\u0000\u0113\u010c\u0001"+
		"\u0000\u0000\u0000\u0113\u010d\u0001\u0000\u0000\u0000\u0113\u010e\u0001"+
		"\u0000\u0000\u0000\u0113\u010f\u0001\u0000\u0000\u0000\u0113\u0110\u0001"+
		"\u0000\u0000\u0000\u0113\u0111\u0001\u0000\u0000\u0000\u0113\u0112\u0001"+
		"\u0000\u0000\u0000\u0114/\u0001\u0000\u0000\u0000\u0115\u0116\u0005\u0001"+
		"\u0000\u0000\u0116\u0117\u0003\b\u0004\u0000\u0117\u0118\u0005\u0002\u0000"+
		"\u0000\u0118\u0120\u00030\u0018\u0000\u0119\u011a\u0005\u0004\u0000\u0000"+
		"\u011a\u011b\u0003\b\u0004\u0000\u011b\u011c\u0005\u0002\u0000\u0000\u011c"+
		"\u011d\u00030\u0018\u0000\u011d\u011f\u0001\u0000\u0000\u0000\u011e\u0119"+
		"\u0001\u0000\u0000\u0000\u011f\u0122\u0001\u0000\u0000\u0000\u0120\u011e"+
		"\u0001\u0000\u0000\u0000\u0120\u0121\u0001\u0000\u0000\u0000\u0121\u0123"+
		"\u0001\u0000\u0000\u0000\u0122\u0120\u0001\u0000\u0000\u0000\u0123\u0124"+
		"\u0005\u0003\u0000\u0000\u0124\u0125\u00030\u0018\u0000\u0125\u0126\u0005"+
		"\u0005\u0000\u0000\u0126\u013d\u0001\u0000\u0000\u0000\u0127\u0128\u0005"+
		"\u0001\u0000\u0000\u0128\u0129\u0005#\u0000\u0000\u0129\u012a\u0003\b"+
		"\u0004\u0000\u012a\u012b\u0007\u0005\u0000\u0000\u012b\u0133\u00030\u0018"+
		"\u0000\u012c\u012d\u0007\u0005\u0000\u0000\u012d\u012e\u0003\b\u0004\u0000"+
		"\u012e\u012f\u0007\u0005\u0000\u0000\u012f\u0130\u00030\u0018\u0000\u0130"+
		"\u0132\u0001\u0000\u0000\u0000\u0131\u012c\u0001\u0000\u0000\u0000\u0132"+
		"\u0135\u0001\u0000\u0000\u0000\u0133\u0131\u0001\u0000\u0000\u0000\u0133"+
		"\u0134\u0001\u0000\u0000\u0000\u0134\u0136\u0001\u0000\u0000\u0000\u0135"+
		"\u0133\u0001\u0000\u0000\u0000\u0136\u0137\u0007\u0005\u0000\u0000\u0137"+
		"\u0138\u00030\u0018\u0000\u0138\u0139\u0005$\u0000\u0000\u0139\u013d\u0001"+
		"\u0000\u0000\u0000\u013a\u013d\u00032\u0019\u0000\u013b\u013d\u0003(\u0014"+
		"\u0000\u013c\u0115\u0001\u0000\u0000\u0000\u013c\u0127\u0001\u0000\u0000"+
		"\u0000\u013c\u013a\u0001\u0000\u0000\u0000\u013c\u013b\u0001\u0000\u0000"+
		"\u0000\u013d1\u0001\u0000\u0000\u0000\u013e\u013f\u00034\u001a\u0000\u013f"+
		"\u0140\u0005#\u0000\u0000\u0140\u0141\u00030\u0018\u0000\u0141\u0142\u0005"+
		"$\u0000\u0000\u01423\u0001\u0000\u0000\u0000\u0143\u014b\u00052\u0000"+
		"\u0000\u0144\u014b\u00053\u0000\u0000\u0145\u014b\u00054\u0000\u0000\u0146"+
		"\u014b\u00055\u0000\u0000\u0147\u014b\u00056\u0000\u0000\u0148\u014b\u0005"+
		"7\u0000\u0000\u0149\u014b\u00058\u0000\u0000\u014a\u0143\u0001\u0000\u0000"+
		"\u0000\u014a\u0144\u0001\u0000\u0000\u0000\u014a\u0145\u0001\u0000\u0000"+
		"\u0000\u014a\u0146\u0001\u0000\u0000\u0000\u014a\u0147\u0001\u0000\u0000"+
		"\u0000\u014a\u0148\u0001\u0000\u0000\u0000\u014a\u0149\u0001\u0000\u0000"+
		"\u0000\u014b5\u0001\u0000\u0000\u0000\u014c\u0177\u0007\u0006\u0000\u0000"+
		"\u014d\u014e\u0005\u0001\u0000\u0000\u014e\u014f\u0003\b\u0004\u0000\u014f"+
		"\u0150\u0005\u0002\u0000\u0000\u0150\u0158\u0003\b\u0004\u0000\u0151\u0152"+
		"\u0005\u0004\u0000\u0000\u0152\u0153\u0003\b\u0004\u0000\u0153\u0154\u0005"+
		"\u0002\u0000\u0000\u0154\u0155\u0003\b\u0004\u0000\u0155\u0157\u0001\u0000"+
		"\u0000\u0000\u0156\u0151\u0001\u0000\u0000\u0000\u0157\u015a\u0001\u0000"+
		"\u0000\u0000\u0158\u0156\u0001\u0000\u0000\u0000\u0158\u0159\u0001\u0000"+
		"\u0000\u0000\u0159\u015b\u0001\u0000\u0000\u0000\u015a\u0158\u0001\u0000"+
		"\u0000\u0000\u015b\u015c\u0005\u0003\u0000\u0000\u015c\u015d\u0003\b\u0004"+
		"\u0000\u015d\u015e\u0005\u0005\u0000\u0000\u015e\u0177\u0001\u0000\u0000"+
		"\u0000\u015f\u0160\u0005\u0001\u0000\u0000\u0160\u0161\u0005#\u0000\u0000"+
		"\u0161\u0162\u0003\b\u0004\u0000\u0162\u0163\u0007\u0005\u0000\u0000\u0163"+
		"\u016b\u0003\b\u0004\u0000\u0164\u0165\u0007\u0005\u0000\u0000\u0165\u0166"+
		"\u0003\b\u0004\u0000\u0166\u0167\u0007\u0005\u0000\u0000\u0167\u0168\u0003"+
		"\b\u0004\u0000\u0168\u016a\u0001\u0000\u0000\u0000\u0169\u0164\u0001\u0000"+
		"\u0000\u0000\u016a\u016d\u0001\u0000\u0000\u0000\u016b\u0169\u0001\u0000"+
		"\u0000\u0000\u016b\u016c\u0001\u0000\u0000\u0000\u016c\u016e\u0001\u0000"+
		"\u0000\u0000\u016d\u016b\u0001\u0000\u0000\u0000\u016e\u016f\u0007\u0005"+
		"\u0000\u0000\u016f\u0170\u0003\b\u0004\u0000\u0170\u0171\u0005$\u0000"+
		"\u0000\u0171\u0177\u0001\u0000\u0000\u0000\u0172\u0174\u00052\u0000\u0000"+
		"\u0173\u0172\u0001\u0000\u0000\u0000\u0173\u0174\u0001\u0000\u0000\u0000"+
		"\u0174\u0175\u0001\u0000\u0000\u0000\u0175\u0177\u0003(\u0014\u0000\u0176"+
		"\u014c\u0001\u0000\u0000\u0000\u0176\u014d\u0001\u0000\u0000\u0000\u0176"+
		"\u015f\u0001\u0000\u0000\u0000\u0176\u0173\u0001\u0000\u0000\u0000\u0177"+
		"7\u0001\u0000\u0000\u0000\u0178\u0179\u0005\u0001\u0000\u0000\u0179\u017a"+
		"\u0003\b\u0004\u0000\u017a\u017b\u0005\u0002\u0000\u0000\u017b\u0183\u0003"+
		"\u0016\u000b\u0000\u017c\u017d\u0005\u0004\u0000\u0000\u017d\u017e\u0003"+
		"\b\u0004\u0000\u017e\u017f\u0005\u0002\u0000\u0000\u017f\u0180\u0003\u0016"+
		"\u000b\u0000\u0180\u0182\u0001\u0000\u0000\u0000\u0181\u017c\u0001\u0000"+
		"\u0000\u0000\u0182\u0185\u0001\u0000\u0000\u0000\u0183\u0181\u0001\u0000"+
		"\u0000\u0000\u0183\u0184\u0001\u0000\u0000\u0000\u0184\u0186\u0001\u0000"+
		"\u0000\u0000\u0185\u0183\u0001\u0000\u0000\u0000\u0186\u0187\u0005\u0003"+
		"\u0000\u0000\u0187\u0188\u0003\u0016\u000b\u0000\u0188\u0189\u0005\u0005"+
		"\u0000\u0000\u0189\u01a3\u0001\u0000\u0000\u0000\u018a\u018b\u0005\u0001"+
		"\u0000\u0000\u018b\u018c\u0005#\u0000\u0000\u018c\u018d\u0003\b\u0004"+
		"\u0000\u018d\u018e\u0007\u0005\u0000\u0000\u018e\u0196\u0003\u0016\u000b"+
		"\u0000\u018f\u0190\u0007\u0005\u0000\u0000\u0190\u0191\u0003\b\u0004\u0000"+
		"\u0191\u0192\u0007\u0005\u0000\u0000\u0192\u0193\u0003\u0016\u000b\u0000"+
		"\u0193\u0195\u0001\u0000\u0000\u0000\u0194\u018f\u0001\u0000\u0000\u0000"+
		"\u0195\u0198\u0001\u0000\u0000\u0000\u0196\u0194\u0001\u0000\u0000\u0000"+
		"\u0196\u0197\u0001\u0000\u0000\u0000\u0197\u0199\u0001\u0000\u0000\u0000"+
		"\u0198\u0196\u0001\u0000\u0000\u0000\u0199\u019a\u0007\u0005\u0000\u0000"+
		"\u019a\u019b\u0003\u0016\u000b\u0000\u019b\u019c\u0005$\u0000\u0000\u019c"+
		"\u01a3\u0001\u0000\u0000\u0000\u019d\u01a3\u0005,\u0000\u0000\u019e\u01a0"+
		"\u00053\u0000\u0000\u019f\u019e\u0001\u0000\u0000\u0000\u019f\u01a0\u0001"+
		"\u0000\u0000\u0000\u01a0\u01a1\u0001\u0000\u0000\u0000\u01a1\u01a3\u0003"+
		"(\u0014\u0000\u01a2\u0178\u0001\u0000\u0000\u0000\u01a2\u018a\u0001\u0000"+
		"\u0000\u0000\u01a2\u019d\u0001\u0000\u0000\u0000\u01a2\u019f\u0001\u0000"+
		"\u0000\u0000\u01a39\u0001\u0000\u0000\u0000\u01a4\u01a5\u0005\u0001\u0000"+
		"\u0000\u01a5\u01a6\u0003\b\u0004\u0000\u01a6\u01a7\u0005\u0002\u0000\u0000"+
		"\u01a7\u01af\u0003:\u001d\u0000\u01a8\u01a9\u0005\u0004\u0000\u0000\u01a9"+
		"\u01aa\u0003\b\u0004\u0000\u01aa\u01ab\u0005\u0002\u0000\u0000\u01ab\u01ac"+
		"\u0003:\u001d\u0000\u01ac\u01ae\u0001\u0000\u0000\u0000\u01ad\u01a8\u0001"+
		"\u0000\u0000\u0000\u01ae\u01b1\u0001\u0000\u0000\u0000\u01af\u01ad\u0001"+
		"\u0000\u0000\u0000\u01af\u01b0\u0001\u0000\u0000\u0000\u01b0\u01b2\u0001"+
		"\u0000\u0000\u0000\u01b1\u01af\u0001\u0000\u0000\u0000\u01b2\u01b3\u0005"+
		"\u0003\u0000\u0000\u01b3\u01b4\u0003:\u001d\u0000\u01b4\u01b5\u0005\u0005"+
		"\u0000\u0000\u01b5\u01cf\u0001\u0000\u0000\u0000\u01b6\u01b7\u0005\u0001"+
		"\u0000\u0000\u01b7\u01b8\u0005#\u0000\u0000\u01b8\u01b9\u0003\b\u0004"+
		"\u0000\u01b9\u01ba\u0007\u0005\u0000\u0000\u01ba\u01c2\u0003:\u001d\u0000"+
		"\u01bb\u01bc\u0007\u0005\u0000\u0000\u01bc\u01bd\u0003\b\u0004\u0000\u01bd"+
		"\u01be\u0007\u0005\u0000\u0000\u01be\u01bf\u0003:\u001d\u0000\u01bf\u01c1"+
		"\u0001\u0000\u0000\u0000\u01c0\u01bb\u0001\u0000\u0000\u0000\u01c1\u01c4"+
		"\u0001\u0000\u0000\u0000\u01c2\u01c0\u0001\u0000\u0000\u0000\u01c2\u01c3"+
		"\u0001\u0000\u0000\u0000\u01c3\u01c5\u0001\u0000\u0000\u0000\u01c4\u01c2"+
		"\u0001\u0000\u0000\u0000\u01c5\u01c6\u0007\u0005\u0000\u0000\u01c6\u01c7"+
		"\u0003:\u001d\u0000\u01c7\u01c8\u0005$\u0000\u0000\u01c8\u01cf\u0001\u0000"+
		"\u0000\u0000\u01c9\u01cf\u0005+\u0000\u0000\u01ca\u01cc\u00054\u0000\u0000"+
		"\u01cb\u01ca\u0001\u0000\u0000\u0000\u01cb\u01cc\u0001\u0000\u0000\u0000"+
		"\u01cc\u01cd\u0001\u0000\u0000\u0000\u01cd\u01cf\u0003(\u0014\u0000\u01ce"+
		"\u01a4\u0001\u0000\u0000\u0000\u01ce\u01b6\u0001\u0000\u0000\u0000\u01ce"+
		"\u01c9\u0001\u0000\u0000\u0000\u01ce\u01cb\u0001\u0000\u0000\u0000\u01cf"+
		";\u0001\u0000\u0000\u0000\u01d0\u01d1\u0005\u0001\u0000\u0000\u01d1\u01d2"+
		"\u0003\b\u0004\u0000\u01d2\u01d3\u0005\u0002\u0000\u0000\u01d3\u01db\u0003"+
		"<\u001e\u0000\u01d4\u01d5\u0005\u0004\u0000\u0000\u01d5\u01d6\u0003\b"+
		"\u0004\u0000\u01d6\u01d7\u0005\u0002\u0000\u0000\u01d7\u01d8\u0003<\u001e"+
		"\u0000\u01d8\u01da\u0001\u0000\u0000\u0000\u01d9\u01d4\u0001\u0000\u0000"+
		"\u0000\u01da\u01dd\u0001\u0000\u0000\u0000\u01db\u01d9\u0001\u0000\u0000"+
		"\u0000\u01db\u01dc\u0001\u0000\u0000\u0000\u01dc\u01de\u0001\u0000\u0000"+
		"\u0000\u01dd\u01db\u0001\u0000\u0000\u0000\u01de\u01df\u0005\u0003\u0000"+
		"\u0000\u01df\u01e0\u0003<\u001e\u0000\u01e0\u01e1\u0005\u0005\u0000\u0000"+
		"\u01e1\u01fc\u0001\u0000\u0000\u0000\u01e2\u01e3\u0005\u0001\u0000\u0000"+
		"\u01e3\u01e4\u0005#\u0000\u0000\u01e4\u01e5\u0003\b\u0004\u0000\u01e5"+
		"\u01e6\u0007\u0005\u0000\u0000\u01e6\u01ee\u0003<\u001e\u0000\u01e7\u01e8"+
		"\u0007\u0005\u0000\u0000\u01e8\u01e9\u0003\b\u0004\u0000\u01e9\u01ea\u0007"+
		"\u0005\u0000\u0000\u01ea\u01eb\u0003<\u001e\u0000\u01eb\u01ed\u0001\u0000"+
		"\u0000\u0000\u01ec\u01e7\u0001\u0000\u0000\u0000\u01ed\u01f0\u0001\u0000"+
		"\u0000\u0000\u01ee\u01ec\u0001\u0000\u0000\u0000\u01ee\u01ef\u0001\u0000"+
		"\u0000\u0000\u01ef\u01f1\u0001\u0000\u0000\u0000\u01f0\u01ee\u0001\u0000"+
		"\u0000\u0000\u01f1\u01f2\u0007\u0005\u0000\u0000\u01f2\u01f3\u0003<\u001e"+
		"\u0000\u01f3\u01f4\u0005$\u0000\u0000\u01f4\u01fc\u0001\u0000\u0000\u0000"+
		"\u01f5\u01fc\u0005.\u0000\u0000\u01f6\u01fc\u0005 \u0000\u0000\u01f7\u01f9"+
		"\u00055\u0000\u0000\u01f8\u01f7\u0001\u0000\u0000\u0000\u01f8\u01f9\u0001"+
		"\u0000\u0000\u0000\u01f9\u01fa\u0001\u0000\u0000\u0000\u01fa\u01fc\u0003"+
		"(\u0014\u0000\u01fb\u01d0\u0001\u0000\u0000\u0000\u01fb\u01e2\u0001\u0000"+
		"\u0000\u0000\u01fb\u01f5\u0001\u0000\u0000\u0000\u01fb\u01f6\u0001\u0000"+
		"\u0000\u0000\u01fb\u01f8\u0001\u0000\u0000\u0000\u01fc=\u0001\u0000\u0000"+
		"\u0000\u01fd\u01fe\u0005\u0001\u0000\u0000\u01fe\u01ff\u0003\b\u0004\u0000"+
		"\u01ff\u0200\u0005\u0002\u0000\u0000\u0200\u0208\u0003>\u001f\u0000\u0201"+
		"\u0202\u0005\u0004\u0000\u0000\u0202\u0203\u0003\b\u0004\u0000\u0203\u0204"+
		"\u0005\u0002\u0000\u0000\u0204\u0205\u0003>\u001f\u0000\u0205\u0207\u0001"+
		"\u0000\u0000\u0000\u0206\u0201\u0001\u0000\u0000\u0000\u0207\u020a\u0001"+
		"\u0000\u0000\u0000\u0208\u0206\u0001\u0000\u0000\u0000\u0208\u0209\u0001"+
		"\u0000\u0000\u0000\u0209\u020b\u0001\u0000\u0000\u0000\u020a\u0208\u0001"+
		"\u0000\u0000\u0000\u020b\u020c\u0005\u0003\u0000\u0000\u020c\u020d\u0003"+
		">\u001f\u0000\u020d\u020e\u0005\u0005\u0000\u0000\u020e\u0229\u0001\u0000"+
		"\u0000\u0000\u020f\u0210\u0005\u0001\u0000\u0000\u0210\u0211\u0005#\u0000"+
		"\u0000\u0211\u0212\u0003\b\u0004\u0000\u0212\u0213\u0007\u0005\u0000\u0000"+
		"\u0213\u021b\u0003>\u001f\u0000\u0214\u0215\u0007\u0005\u0000\u0000\u0215"+
		"\u0216\u0003\b\u0004\u0000\u0216\u0217\u0007\u0005\u0000\u0000\u0217\u0218"+
		"\u0003>\u001f\u0000\u0218\u021a\u0001\u0000\u0000\u0000\u0219\u0214\u0001"+
		"\u0000\u0000\u0000\u021a\u021d\u0001\u0000\u0000\u0000\u021b\u0219\u0001"+
		"\u0000\u0000\u0000\u021b\u021c\u0001\u0000\u0000\u0000\u021c\u021e\u0001"+
		"\u0000\u0000\u0000\u021d\u021b\u0001\u0000\u0000\u0000\u021e\u021f\u0007"+
		"\u0005\u0000\u0000\u021f\u0220\u0003>\u001f\u0000\u0220\u0221\u0005$\u0000"+
		"\u0000\u0221\u0229\u0001\u0000\u0000\u0000\u0222\u0229\u0005/\u0000\u0000"+
		"\u0223\u0229\u0005!\u0000\u0000\u0224\u0226\u00056\u0000\u0000\u0225\u0224"+
		"\u0001\u0000\u0000\u0000\u0225\u0226\u0001\u0000\u0000\u0000\u0226\u0227"+
		"\u0001\u0000\u0000\u0000\u0227\u0229\u0003(\u0014\u0000\u0228\u01fd\u0001"+
		"\u0000\u0000\u0000\u0228\u020f\u0001\u0000\u0000\u0000\u0228\u0222\u0001"+
		"\u0000\u0000\u0000\u0228\u0223\u0001\u0000\u0000\u0000\u0228\u0225\u0001"+
		"\u0000\u0000\u0000\u0229?\u0001\u0000\u0000\u0000\u022a\u022b\u0005\u0001"+
		"\u0000\u0000\u022b\u022c\u0003\b\u0004\u0000\u022c\u022d\u0005\u0002\u0000"+
		"\u0000\u022d\u0235\u0003@ \u0000\u022e\u022f\u0005\u0004\u0000\u0000\u022f"+
		"\u0230\u0003\b\u0004\u0000\u0230\u0231\u0005\u0002\u0000\u0000\u0231\u0232"+
		"\u0003@ \u0000\u0232\u0234\u0001\u0000\u0000\u0000\u0233\u022e\u0001\u0000"+
		"\u0000\u0000\u0234\u0237\u0001\u0000\u0000\u0000\u0235\u0233\u0001\u0000"+
		"\u0000\u0000\u0235\u0236\u0001\u0000\u0000\u0000\u0236\u0238\u0001\u0000"+
		"\u0000\u0000\u0237\u0235\u0001\u0000\u0000\u0000\u0238\u0239\u0005\u0003"+
		"\u0000\u0000\u0239\u023a\u0003@ \u0000\u023a\u023b\u0005\u0005\u0000\u0000"+
		"\u023b\u0259\u0001\u0000\u0000\u0000\u023c\u023d\u0005\u0001\u0000\u0000"+
		"\u023d\u023e\u0005#\u0000\u0000\u023e\u023f\u0003\b\u0004\u0000\u023f"+
		"\u0240\u0007\u0005\u0000\u0000\u0240\u0248\u0003@ \u0000\u0241\u0242\u0007"+
		"\u0005\u0000\u0000\u0242\u0243\u0003\b\u0004\u0000\u0243\u0244\u0007\u0005"+
		"\u0000\u0000\u0244\u0245\u0003@ \u0000\u0245\u0247\u0001\u0000\u0000\u0000"+
		"\u0246\u0241\u0001\u0000\u0000\u0000\u0247\u024a\u0001\u0000\u0000\u0000"+
		"\u0248\u0246\u0001\u0000\u0000\u0000\u0248\u0249\u0001\u0000\u0000\u0000"+
		"\u0249\u024b\u0001\u0000\u0000\u0000\u024a\u0248\u0001\u0000\u0000\u0000"+
		"\u024b\u024c\u0007\u0005\u0000\u0000\u024c\u024d\u0003@ \u0000\u024d\u024e"+
		"\u0005$\u0000\u0000\u024e\u0259\u0001\u0000\u0000\u0000\u024f\u0251\u0005"+
		"1\u0000\u0000\u0250\u0252\u00050\u0000\u0000\u0251\u0250\u0001\u0000\u0000"+
		"\u0000\u0251\u0252\u0001\u0000\u0000\u0000\u0252\u0259\u0001\u0000\u0000"+
		"\u0000\u0253\u0259\u0005\"\u0000\u0000\u0254\u0256\u00057\u0000\u0000"+
		"\u0255\u0254\u0001\u0000\u0000\u0000\u0255\u0256\u0001\u0000\u0000\u0000"+
		"\u0256\u0257\u0001\u0000\u0000\u0000\u0257\u0259\u0003(\u0014\u0000\u0258"+
		"\u022a\u0001\u0000\u0000\u0000\u0258\u023c\u0001\u0000\u0000\u0000\u0258"+
		"\u024f\u0001\u0000\u0000\u0000\u0258\u0253\u0001\u0000\u0000\u0000\u0258"+
		"\u0255\u0001\u0000\u0000\u0000\u0259A\u0001\u0000\u0000\u0000\u025a\u025b"+
		"\u0005\u0001\u0000\u0000\u025b\u025c\u0003\b\u0004\u0000\u025c\u025d\u0005"+
		"\u0002\u0000\u0000\u025d\u0265\u0003B!\u0000\u025e\u025f\u0005\u0004\u0000"+
		"\u0000\u025f\u0260\u0003\b\u0004\u0000\u0260\u0261\u0005\u0002\u0000\u0000"+
		"\u0261\u0262\u0003B!\u0000\u0262\u0264\u0001\u0000\u0000\u0000\u0263\u025e"+
		"\u0001\u0000\u0000\u0000\u0264\u0267\u0001\u0000\u0000\u0000\u0265\u0263"+
		"\u0001\u0000\u0000\u0000\u0265\u0266\u0001\u0000\u0000\u0000\u0266\u0268"+
		"\u0001\u0000\u0000\u0000\u0267\u0265\u0001\u0000\u0000\u0000\u0268\u0269"+
		"\u0005\u0003\u0000\u0000\u0269\u026a\u0003B!\u0000\u026a\u026b\u0005\u0005"+
		"\u0000\u0000\u026b\u028f\u0001\u0000\u0000\u0000\u026c\u026d\u0005\u0001"+
		"\u0000\u0000\u026d\u026e\u0005#\u0000\u0000\u026e\u026f\u0003\b\u0004"+
		"\u0000\u026f\u0270\u0007\u0005\u0000\u0000\u0270\u0278\u0003B!\u0000\u0271"+
		"\u0272\u0007\u0005\u0000\u0000\u0272\u0273\u0003\b\u0004\u0000\u0273\u0274"+
		"\u0007\u0005\u0000\u0000\u0274\u0275\u0003B!\u0000\u0275\u0277\u0001\u0000"+
		"\u0000\u0000\u0276\u0271\u0001\u0000\u0000\u0000\u0277\u027a\u0001\u0000"+
		"\u0000\u0000\u0278\u0276\u0001\u0000\u0000\u0000\u0278\u0279\u0001\u0000"+
		"\u0000\u0000\u0279\u027b\u0001\u0000\u0000\u0000\u027a\u0278\u0001\u0000"+
		"\u0000\u0000\u027b\u027c\u0007\u0005\u0000\u0000\u027c\u027d\u0003B!\u0000"+
		"\u027d\u027e\u0005$\u0000\u0000\u027e\u028f\u0001\u0000\u0000\u0000\u027f"+
		"\u0280\u0005%\u0000\u0000\u0280\u0285\u0003,\u0016\u0000\u0281\u0282\u0005"+
		"\'\u0000\u0000\u0282\u0284\u0003,\u0016\u0000\u0283\u0281\u0001\u0000"+
		"\u0000\u0000\u0284\u0287\u0001\u0000\u0000\u0000\u0285\u0283\u0001\u0000"+
		"\u0000\u0000\u0285\u0286\u0001\u0000\u0000\u0000\u0286\u0288\u0001\u0000"+
		"\u0000\u0000\u0287\u0285\u0001\u0000\u0000\u0000\u0288\u0289\u0005&\u0000"+
		"\u0000\u0289\u028f\u0001\u0000\u0000\u0000\u028a\u028c\u00058\u0000\u0000"+
		"\u028b\u028a\u0001\u0000\u0000\u0000\u028b\u028c\u0001\u0000\u0000\u0000"+
		"\u028c\u028d\u0001\u0000\u0000\u0000\u028d\u028f\u0003(\u0014\u0000\u028e"+
		"\u025a\u0001\u0000\u0000\u0000\u028e\u026c\u0001\u0000\u0000\u0000\u028e"+
		"\u027f\u0001\u0000\u0000\u0000\u028e\u028b\u0001\u0000\u0000\u0000\u028f"+
		"C\u0001\u0000\u0000\u0000\u0290\u0291\u0005%\u0000\u0000\u0291\u0296\u0005"+
		"*\u0000\u0000\u0292\u0293\u0005\'\u0000\u0000\u0293\u0295\u0005*\u0000"+
		"\u0000\u0294\u0292\u0001\u0000\u0000\u0000\u0295\u0298\u0001\u0000\u0000"+
		"\u0000\u0296\u0294\u0001\u0000\u0000\u0000\u0296\u0297\u0001\u0000\u0000"+
		"\u0000\u0297\u0299\u0001\u0000\u0000\u0000\u0298\u0296\u0001\u0000\u0000"+
		"\u0000\u0299\u029a\u0005&\u0000\u0000\u029aE\u0001\u0000\u0000\u0000;"+
		"IRYirz\u0081\u0097\u009e\u00a4\u00ab\u00b4\u00bc\u00c2\u00c9\u00cf\u00d5"+
		"\u00e6\u00ef\u00f2\u00f8\u0100\u0109\u0113\u0120\u0133\u013c\u014a\u0158"+
		"\u016b\u0173\u0176\u0183\u0196\u019f\u01a2\u01af\u01c2\u01cb\u01ce\u01db"+
		"\u01ee\u01f8\u01fb\u0208\u021b\u0225\u0228\u0235\u0248\u0251\u0255\u0258"+
		"\u0265\u0278\u0285\u028b\u028e\u0296";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}