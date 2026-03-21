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
		RULE_mathStart = 0, RULE_logicalStart = 1, RULE_assignmentExpression = 2, 
		RULE_logicalExpression = 3, RULE_logicalOrExpression = 4, RULE_logicalAndExpression = 5, 
		RULE_logicalComparisonExpression = 6, RULE_logicalBitwiseExpression = 7, 
		RULE_logicalNotExpression = 8, RULE_logicalPrimary = 9, RULE_mathExpression = 10, 
		RULE_sumExpression = 11, RULE_multiplicationExpression = 12, RULE_unaryExpression = 13, 
		RULE_rootExpression = 14, RULE_exponentiationExpression = 15, RULE_postfixExpression = 16, 
		RULE_primaryMathExpression = 17, RULE_function = 18, RULE_referenceTarget = 19, 
		RULE_comparisonOperator = 20, RULE_allEntityTypes = 21, RULE_assignmentValue = 22, 
		RULE_genericEntity = 23, RULE_castExpression = 24, RULE_typeHint = 25, 
		RULE_logicalEntity = 26, RULE_numericEntity = 27, RULE_stringEntity = 28, 
		RULE_dateEntity = 29, RULE_timeEntity = 30, RULE_dateTimeEntity = 31, 
		RULE_vectorEntity = 32, RULE_vectorOfVariables = 33;
	private static String[] makeRuleNames() {
		return new String[] {
			"mathStart", "logicalStart", "assignmentExpression", "logicalExpression", 
			"logicalOrExpression", "logicalAndExpression", "logicalComparisonExpression", 
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
			setState(71);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(68);
					assignmentExpression();
					}
					} 
				}
				setState(73);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(74);
			mathExpression();
			setState(75);
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
		enterRule(_localctx, 2, RULE_logicalStart);
		try {
			int _alt;
			_localctx = new LogicalInputContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(80);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(77);
					assignmentExpression();
					}
					} 
				}
				setState(82);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			}
			setState(83);
			logicalExpression();
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
		enterRule(_localctx, 4, RULE_assignmentExpression);
		try {
			setState(96);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				_localctx = new AssignmentOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(86);
				match(IDENTIFIER);
				setState(87);
				match(EQ);
				setState(88);
				assignmentValue();
				setState(89);
				match(SEMI);
				}
				break;
			case LBRACKET:
				_localctx = new DestructuringAssignmentOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(91);
				vectorOfVariables();
				setState(92);
				match(EQ);
				setState(93);
				vectorEntity();
				setState(94);
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
		enterRule(_localctx, 6, RULE_logicalExpression);
		try {
			_localctx = new LogicalOrOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(98);
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
		enterRule(_localctx, 8, RULE_logicalOrExpression);
		int _la;
		try {
			_localctx = new LogicalOrChainOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			logicalAndExpression();
			setState(105);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(101);
				match(OR);
				setState(102);
				logicalAndExpression();
				}
				}
				setState(107);
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
		enterRule(_localctx, 10, RULE_logicalAndExpression);
		int _la;
		try {
			_localctx = new LogicalAndChainOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(108);
			logicalComparisonExpression();
			setState(113);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(109);
				match(AND);
				setState(110);
				logicalComparisonExpression();
				}
				}
				setState(115);
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
		enterRule(_localctx, 12, RULE_logicalComparisonExpression);
		int _la;
		try {
			setState(142);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				_localctx = new LogicalComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(116);
				logicalBitwiseExpression();
				setState(120);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2113929216L) != 0)) {
					{
					setState(117);
					comparisonOperator();
					setState(118);
					logicalBitwiseExpression();
					}
				}

				}
				break;
			case 2:
				_localctx = new MathComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(122);
				mathExpression();
				setState(123);
				comparisonOperator();
				setState(124);
				mathExpression();
				}
				break;
			case 3:
				_localctx = new StringComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(126);
				stringEntity();
				setState(127);
				comparisonOperator();
				setState(128);
				stringEntity();
				}
				break;
			case 4:
				_localctx = new DateComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(130);
				dateEntity();
				setState(131);
				comparisonOperator();
				setState(132);
				dateEntity();
				}
				break;
			case 5:
				_localctx = new TimeComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(134);
				timeEntity();
				setState(135);
				comparisonOperator();
				setState(136);
				timeEntity();
				}
				break;
			case 6:
				_localctx = new DateTimeComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(138);
				dateTimeEntity();
				setState(139);
				comparisonOperator();
				setState(140);
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
		enterRule(_localctx, 14, RULE_logicalBitwiseExpression);
		int _la;
		try {
			_localctx = new LogicalBitwiseOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(144);
			logicalNotExpression();
			setState(149);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3840L) != 0)) {
				{
				{
				setState(145);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 3840L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(146);
				logicalNotExpression();
				}
				}
				setState(151);
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
		enterRule(_localctx, 16, RULE_logicalNotExpression);
		int _la;
		try {
			setState(155);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXCLAMATION:
			case NOT:
				_localctx = new LogicalNotOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(152);
				_la = _input.LA(1);
				if ( !(_la==EXCLAMATION || _la==NOT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(153);
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
				setState(154);
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
		enterRule(_localctx, 18, RULE_logicalPrimary);
		try {
			setState(162);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				_localctx = new LogicalExpressionParenthesisOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(157);
				match(LPAREN);
				setState(158);
				logicalExpression();
				setState(159);
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
				setState(161);
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
		enterRule(_localctx, 20, RULE_mathExpression);
		try {
			_localctx = new SumOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(164);
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
		enterRule(_localctx, 22, RULE_sumExpression);
		int _la;
		try {
			_localctx = new AdditiveOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(166);
			multiplicationExpression();
			setState(171);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS || _la==MINUS) {
				{
				{
				setState(167);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(168);
				multiplicationExpression();
				}
				}
				setState(173);
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
		enterRule(_localctx, 24, RULE_multiplicationExpression);
		int _la;
		try {
			_localctx = new MultiplicativeOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(174);
			unaryExpression();
			setState(179);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 573440L) != 0)) {
				{
				{
				setState(175);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 573440L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(176);
				unaryExpression();
				}
				}
				setState(181);
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
		enterRule(_localctx, 26, RULE_unaryExpression);
		try {
			setState(185);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MINUS:
				_localctx = new UnaryMinusOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(182);
				match(MINUS);
				setState(183);
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
				setState(184);
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
		enterRule(_localctx, 28, RULE_rootExpression);
		try {
			int _alt;
			_localctx = new RootChainOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			exponentiationExpression();
			setState(192);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(188);
					match(ROOT);
					setState(189);
					exponentiationExpression();
					}
					} 
				}
				setState(194);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
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
		enterRule(_localctx, 30, RULE_exponentiationExpression);
		int _la;
		try {
			_localctx = new ExponentiationOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(195);
			postfixExpression();
			setState(198);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXPONENTIATION) {
				{
				setState(196);
				match(EXPONENTIATION);
				setState(197);
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
		enterRule(_localctx, 32, RULE_postfixExpression);
		int _la;
		try {
			_localctx = new PostfixOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			primaryMathExpression();
			setState(204);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PERCENT || _la==EXCLAMATION) {
				{
				{
				setState(201);
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
				setState(206);
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
		enterRule(_localctx, 34, RULE_primaryMathExpression);
		try {
			setState(221);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				_localctx = new MathExpressionParenthesisOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(207);
				match(LPAREN);
				setState(208);
				mathExpression();
				setState(209);
				match(RPAREN);
				}
				break;
			case SQRT:
				_localctx = new SquareRootOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(211);
				match(SQRT);
				setState(212);
				match(LPAREN);
				setState(213);
				mathExpression();
				setState(214);
				match(RPAREN);
				}
				break;
			case MODULUS:
				_localctx = new ModulusOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(216);
				match(MODULUS);
				setState(217);
				mathExpression();
				setState(218);
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
				setState(220);
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
		enterRule(_localctx, 36, RULE_function);
		int _la;
		try {
			_localctx = new FunctionCallOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(223);
			match(IDENTIFIER);
			setState(224);
			match(LPAREN);
			setState(233);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 143794334711558146L) != 0)) {
				{
				setState(225);
				allEntityTypes();
				setState(230);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA || _la==SEMI) {
					{
					{
					setState(226);
					_la = _input.LA(1);
					if ( !(_la==COMMA || _la==SEMI) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(227);
					allEntityTypes();
					}
					}
					setState(232);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(235);
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
		enterRule(_localctx, 38, RULE_referenceTarget);
		try {
			setState(239);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				_localctx = new FunctionReferenceTargetContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(237);
				function();
				}
				break;
			case 2:
				_localctx = new IdentifierReferenceTargetContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(238);
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
		enterRule(_localctx, 40, RULE_comparisonOperator);
		try {
			setState(247);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case GT:
				_localctx = new GreaterThanOperatorContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(241);
				match(GT);
				}
				break;
			case GE:
				_localctx = new GreaterThanOrEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(242);
				match(GE);
				}
				break;
			case LT:
				_localctx = new LessThanOperatorContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(243);
				match(LT);
				}
				break;
			case LE:
				_localctx = new LessThanOrEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(244);
				match(LE);
				}
				break;
			case EQ:
				_localctx = new EqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(245);
				match(EQ);
				}
				break;
			case NEQ:
				_localctx = new NotEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(246);
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
		enterRule(_localctx, 42, RULE_allEntityTypes);
		try {
			setState(256);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				_localctx = new MathEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(249);
				mathExpression();
				}
				break;
			case 2:
				_localctx = new LogicalEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(250);
				logicalExpression();
				}
				break;
			case 3:
				_localctx = new DateEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(251);
				dateEntity();
				}
				break;
			case 4:
				_localctx = new TimeEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(252);
				timeEntity();
				}
				break;
			case 5:
				_localctx = new DateTimeEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(253);
				dateTimeEntity();
				}
				break;
			case 6:
				_localctx = new StringEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(254);
				stringEntity();
				}
				break;
			case 7:
				_localctx = new VectorEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(255);
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
		enterRule(_localctx, 44, RULE_assignmentValue);
		try {
			setState(266);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				_localctx = new GenericAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(258);
				genericEntity();
				}
				break;
			case 2:
				_localctx = new MathAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(259);
				mathExpression();
				}
				break;
			case 3:
				_localctx = new LogicalAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(260);
				logicalExpression();
				}
				break;
			case 4:
				_localctx = new DateAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(261);
				dateEntity();
				}
				break;
			case 5:
				_localctx = new TimeAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(262);
				timeEntity();
				}
				break;
			case 6:
				_localctx = new DateTimeAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(263);
				dateTimeEntity();
				}
				break;
			case 7:
				_localctx = new StringAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(264);
				stringEntity();
				}
				break;
			case 8:
				_localctx = new VectorAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(265);
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
		enterRule(_localctx, 46, RULE_genericEntity);
		int _la;
		try {
			int _alt;
			setState(307);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				_localctx = new GenericDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(268);
				match(IF);
				setState(269);
				logicalExpression();
				setState(270);
				match(THEN);
				setState(271);
				genericEntity();
				setState(279);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(272);
					match(ELSEIF);
					setState(273);
					logicalExpression();
					setState(274);
					match(THEN);
					setState(275);
					genericEntity();
					}
					}
					setState(281);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(282);
				match(ELSE);
				setState(283);
				genericEntity();
				setState(284);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new GenericFunctionDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(286);
				match(IF);
				setState(287);
				match(LPAREN);
				setState(288);
				logicalExpression();
				setState(289);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(290);
				genericEntity();
				setState(298);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(291);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(292);
						logicalExpression();
						setState(293);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(294);
						genericEntity();
						}
						} 
					}
					setState(300);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
				}
				setState(301);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(302);
				genericEntity();
				setState(303);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new CastExpressionOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(305);
				castExpression();
				}
				break;
			case 4:
				_localctx = new ReferenceTargetOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(306);
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
		enterRule(_localctx, 48, RULE_castExpression);
		try {
			_localctx = new TypeCastOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(309);
			typeHint();
			setState(310);
			match(LPAREN);
			setState(311);
			genericEntity();
			setState(312);
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
		enterRule(_localctx, 50, RULE_typeHint);
		try {
			setState(321);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BOOLEAN_TYPE:
				_localctx = new BooleanTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(314);
				match(BOOLEAN_TYPE);
				}
				break;
			case NUMBER_TYPE:
				_localctx = new NumberTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(315);
				match(NUMBER_TYPE);
				}
				break;
			case STRING_TYPE:
				_localctx = new StringTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(316);
				match(STRING_TYPE);
				}
				break;
			case DATE_TYPE:
				_localctx = new DateTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(317);
				match(DATE_TYPE);
				}
				break;
			case TIME_TYPE:
				_localctx = new TimeTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(318);
				match(TIME_TYPE);
				}
				break;
			case DATETIME_TYPE:
				_localctx = new DateTimeTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(319);
				match(DATETIME_TYPE);
				}
				break;
			case VECTOR_TYPE:
				_localctx = new VectorTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(320);
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
		enterRule(_localctx, 52, RULE_logicalEntity);
		int _la;
		try {
			int _alt;
			setState(365);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				_localctx = new LogicalConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(323);
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
				setState(324);
				match(IF);
				setState(325);
				logicalExpression();
				setState(326);
				match(THEN);
				setState(327);
				logicalExpression();
				setState(335);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(328);
					match(ELSEIF);
					setState(329);
					logicalExpression();
					setState(330);
					match(THEN);
					setState(331);
					logicalExpression();
					}
					}
					setState(337);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(338);
				match(ELSE);
				setState(339);
				logicalExpression();
				setState(340);
				match(ENDIF);
				}
				break;
			case 3:
				_localctx = new LogicalFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(342);
				match(IF);
				setState(343);
				match(LPAREN);
				setState(344);
				logicalExpression();
				setState(345);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(346);
				logicalExpression();
				setState(354);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(347);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(348);
						logicalExpression();
						setState(349);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(350);
						logicalExpression();
						}
						} 
					}
					setState(356);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
				}
				setState(357);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(358);
				logicalExpression();
				setState(359);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new LogicalReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(362);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==BOOLEAN_TYPE) {
					{
					setState(361);
					match(BOOLEAN_TYPE);
					}
				}

				setState(364);
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
		enterRule(_localctx, 54, RULE_numericEntity);
		int _la;
		try {
			int _alt;
			setState(409);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
			case 1:
				_localctx = new MathDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(367);
				match(IF);
				setState(368);
				logicalExpression();
				setState(369);
				match(THEN);
				setState(370);
				mathExpression();
				setState(378);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(371);
					match(ELSEIF);
					setState(372);
					logicalExpression();
					setState(373);
					match(THEN);
					setState(374);
					mathExpression();
					}
					}
					setState(380);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(381);
				match(ELSE);
				setState(382);
				mathExpression();
				setState(383);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new MathFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(385);
				match(IF);
				setState(386);
				match(LPAREN);
				setState(387);
				logicalExpression();
				setState(388);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(389);
				mathExpression();
				setState(397);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(390);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(391);
						logicalExpression();
						setState(392);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(393);
						mathExpression();
						}
						} 
					}
					setState(399);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
				}
				setState(400);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(401);
				mathExpression();
				setState(402);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new NumericConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(404);
				match(NUMBER);
				}
				break;
			case 4:
				_localctx = new NumericReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(406);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NUMBER_TYPE) {
					{
					setState(405);
					match(NUMBER_TYPE);
					}
				}

				setState(408);
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
		enterRule(_localctx, 56, RULE_stringEntity);
		int _la;
		try {
			int _alt;
			setState(453);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				_localctx = new StringDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(411);
				match(IF);
				setState(412);
				logicalExpression();
				setState(413);
				match(THEN);
				setState(414);
				stringEntity();
				setState(422);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(415);
					match(ELSEIF);
					setState(416);
					logicalExpression();
					setState(417);
					match(THEN);
					setState(418);
					stringEntity();
					}
					}
					setState(424);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(425);
				match(ELSE);
				setState(426);
				stringEntity();
				setState(427);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new StringFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(429);
				match(IF);
				setState(430);
				match(LPAREN);
				setState(431);
				logicalExpression();
				setState(432);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(433);
				stringEntity();
				setState(441);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,36,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(434);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(435);
						logicalExpression();
						setState(436);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(437);
						stringEntity();
						}
						} 
					}
					setState(443);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,36,_ctx);
				}
				setState(444);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(445);
				stringEntity();
				setState(446);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new StringConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(448);
				match(STRING);
				}
				break;
			case 4:
				_localctx = new StringReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(450);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING_TYPE) {
					{
					setState(449);
					match(STRING_TYPE);
					}
				}

				setState(452);
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
		enterRule(_localctx, 58, RULE_dateEntity);
		int _la;
		try {
			int _alt;
			setState(498);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				_localctx = new DateDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(455);
				match(IF);
				setState(456);
				logicalExpression();
				setState(457);
				match(THEN);
				setState(458);
				dateEntity();
				setState(466);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(459);
					match(ELSEIF);
					setState(460);
					logicalExpression();
					setState(461);
					match(THEN);
					setState(462);
					dateEntity();
					}
					}
					setState(468);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(469);
				match(ELSE);
				setState(470);
				dateEntity();
				setState(471);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new DateFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(473);
				match(IF);
				setState(474);
				match(LPAREN);
				setState(475);
				logicalExpression();
				setState(476);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(477);
				dateEntity();
				setState(485);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(478);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(479);
						logicalExpression();
						setState(480);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(481);
						dateEntity();
						}
						} 
					}
					setState(487);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
				}
				setState(488);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(489);
				dateEntity();
				setState(490);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new DateConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(492);
				match(DATE);
				}
				break;
			case 4:
				_localctx = new DateCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(493);
				match(NOW_DATE);
				}
				break;
			case 5:
				_localctx = new DateReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(495);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DATE_TYPE) {
					{
					setState(494);
					match(DATE_TYPE);
					}
				}

				setState(497);
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
		enterRule(_localctx, 60, RULE_timeEntity);
		int _la;
		try {
			int _alt;
			setState(543);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
			case 1:
				_localctx = new TimeDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(500);
				match(IF);
				setState(501);
				logicalExpression();
				setState(502);
				match(THEN);
				setState(503);
				timeEntity();
				setState(511);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(504);
					match(ELSEIF);
					setState(505);
					logicalExpression();
					setState(506);
					match(THEN);
					setState(507);
					timeEntity();
					}
					}
					setState(513);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(514);
				match(ELSE);
				setState(515);
				timeEntity();
				setState(516);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new TimeFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(518);
				match(IF);
				setState(519);
				match(LPAREN);
				setState(520);
				logicalExpression();
				setState(521);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(522);
				timeEntity();
				setState(530);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,44,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(523);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(524);
						logicalExpression();
						setState(525);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(526);
						timeEntity();
						}
						} 
					}
					setState(532);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,44,_ctx);
				}
				setState(533);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(534);
				timeEntity();
				setState(535);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new TimeConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(537);
				match(TIME);
				}
				break;
			case 4:
				_localctx = new TimeCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(538);
				match(NOW_TIME);
				}
				break;
			case 5:
				_localctx = new TimeReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(540);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TIME_TYPE) {
					{
					setState(539);
					match(TIME_TYPE);
					}
				}

				setState(542);
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
		enterRule(_localctx, 62, RULE_dateTimeEntity);
		int _la;
		try {
			int _alt;
			setState(591);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
			case 1:
				_localctx = new DateTimeDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(545);
				match(IF);
				setState(546);
				logicalExpression();
				setState(547);
				match(THEN);
				setState(548);
				dateTimeEntity();
				setState(556);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(549);
					match(ELSEIF);
					setState(550);
					logicalExpression();
					setState(551);
					match(THEN);
					setState(552);
					dateTimeEntity();
					}
					}
					setState(558);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(559);
				match(ELSE);
				setState(560);
				dateTimeEntity();
				setState(561);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new DateTimeFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(563);
				match(IF);
				setState(564);
				match(LPAREN);
				setState(565);
				logicalExpression();
				setState(566);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(567);
				dateTimeEntity();
				setState(575);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,48,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(568);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(569);
						logicalExpression();
						setState(570);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(571);
						dateTimeEntity();
						}
						} 
					}
					setState(577);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,48,_ctx);
				}
				setState(578);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(579);
				dateTimeEntity();
				setState(580);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new DateTimeConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(582);
				match(DATETIME);
				setState(584);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TIME_OFFSET) {
					{
					setState(583);
					match(TIME_OFFSET);
					}
				}

				}
				break;
			case 4:
				_localctx = new DateTimeCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(586);
				match(NOW_DATETIME);
				}
				break;
			case 5:
				_localctx = new DateTimeReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(588);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DATETIME_TYPE) {
					{
					setState(587);
					match(DATETIME_TYPE);
					}
				}

				setState(590);
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
		enterRule(_localctx, 64, RULE_vectorEntity);
		int _la;
		try {
			int _alt;
			setState(645);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				_localctx = new VectorDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(593);
				match(IF);
				setState(594);
				logicalExpression();
				setState(595);
				match(THEN);
				setState(596);
				vectorEntity();
				setState(604);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(597);
					match(ELSEIF);
					setState(598);
					logicalExpression();
					setState(599);
					match(THEN);
					setState(600);
					vectorEntity();
					}
					}
					setState(606);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(607);
				match(ELSE);
				setState(608);
				vectorEntity();
				setState(609);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new VectorFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(611);
				match(IF);
				setState(612);
				match(LPAREN);
				setState(613);
				logicalExpression();
				setState(614);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(615);
				vectorEntity();
				setState(623);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,53,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(616);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(617);
						logicalExpression();
						setState(618);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(619);
						vectorEntity();
						}
						} 
					}
					setState(625);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,53,_ctx);
				}
				setState(626);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(627);
				vectorEntity();
				setState(628);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new VectorOfEntitiesOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(630);
				match(LBRACKET);
				setState(631);
				allEntityTypes();
				setState(636);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(632);
					match(COMMA);
					setState(633);
					allEntityTypes();
					}
					}
					setState(638);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(639);
				match(RBRACKET);
				}
				break;
			case 4:
				_localctx = new VectorReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(642);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==VECTOR_TYPE) {
					{
					setState(641);
					match(VECTOR_TYPE);
					}
				}

				setState(644);
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
		enterRule(_localctx, 66, RULE_vectorOfVariables);
		int _la;
		try {
			_localctx = new VectorOfVariablesOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(647);
			match(LBRACKET);
			setState(648);
			match(IDENTIFIER);
			setState(653);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(649);
				match(COMMA);
				setState(650);
				match(IDENTIFIER);
				}
				}
				setState(655);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(656);
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
		"\u0004\u0001<\u0293\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0001\u0000\u0005"+
		"\u0000F\b\u0000\n\u0000\f\u0000I\t\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0001\u0005\u0001O\b\u0001\n\u0001\f\u0001R\t\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0003\u0002a\b\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0005\u0004h\b\u0004\n\u0004\f\u0004k\t\u0004\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0005\u0005p\b\u0005\n\u0005\f\u0005s\t"+
		"\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006y\b"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0003\u0006\u008f\b\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0005\u0007\u0094\b\u0007\n\u0007\f\u0007\u0097\t\u0007"+
		"\u0001\b\u0001\b\u0001\b\u0003\b\u009c\b\b\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0003\t\u00a3\b\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0005\u000b\u00aa\b\u000b\n\u000b\f\u000b\u00ad\t\u000b\u0001\f"+
		"\u0001\f\u0001\f\u0005\f\u00b2\b\f\n\f\f\f\u00b5\t\f\u0001\r\u0001\r\u0001"+
		"\r\u0003\r\u00ba\b\r\u0001\u000e\u0001\u000e\u0001\u000e\u0005\u000e\u00bf"+
		"\b\u000e\n\u000e\f\u000e\u00c2\t\u000e\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0003\u000f\u00c7\b\u000f\u0001\u0010\u0001\u0010\u0005\u0010\u00cb\b"+
		"\u0010\n\u0010\f\u0010\u00ce\t\u0010\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011"+
		"\u00de\b\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0005\u0012\u00e5\b\u0012\n\u0012\f\u0012\u00e8\t\u0012\u0003\u0012\u00ea"+
		"\b\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0003\u0013\u00f0"+
		"\b\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0003\u0014\u00f8\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u0101\b\u0015\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0003\u0016\u010b\b\u0016\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0005\u0017\u0116\b\u0017\n\u0017\f\u0017\u0119\t\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0005\u0017\u0129\b\u0017\n\u0017\f\u0017\u012c\t\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003"+
		"\u0017\u0134\b\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0003\u0019\u0142\b\u0019\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0005\u001a\u014e\b\u001a\n\u001a\f\u001a\u0151\t\u001a"+
		"\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a"+
		"\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a"+
		"\u0001\u001a\u0001\u001a\u0005\u001a\u0161\b\u001a\n\u001a\f\u001a\u0164"+
		"\t\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0003"+
		"\u001a\u016b\b\u001a\u0001\u001a\u0003\u001a\u016e\b\u001a\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0005\u001b\u0179\b\u001b\n\u001b\f\u001b\u017c"+
		"\t\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0005\u001b\u018c\b\u001b\n\u001b\f\u001b"+
		"\u018f\t\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0003\u001b\u0197\b\u001b\u0001\u001b\u0003\u001b\u019a\b"+
		"\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0005\u001c\u01a5\b\u001c\n"+
		"\u001c\f\u001c\u01a8\t\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0005\u001c\u01b8"+
		"\b\u001c\n\u001c\f\u001c\u01bb\t\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0003\u001c\u01c3\b\u001c\u0001\u001c"+
		"\u0003\u001c\u01c6\b\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d"+
		"\u01d1\b\u001d\n\u001d\f\u001d\u01d4\t\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0005"+
		"\u001d\u01e4\b\u001d\n\u001d\f\u001d\u01e7\t\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0003\u001d"+
		"\u01f0\b\u001d\u0001\u001d\u0003\u001d\u01f3\b\u001d\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0005\u001e\u01fe\b\u001e\n\u001e\f\u001e\u0201\t\u001e"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001e\u0001\u001e\u0005\u001e\u0211\b\u001e\n\u001e\f\u001e\u0214"+
		"\t\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0003\u001e\u021d\b\u001e\u0001\u001e\u0003\u001e\u0220"+
		"\b\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0005\u001f\u022b\b\u001f\n"+
		"\u001f\f\u001f\u022e\t\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0005\u001f\u023e"+
		"\b\u001f\n\u001f\f\u001f\u0241\t\u001f\u0001\u001f\u0001\u001f\u0001\u001f"+
		"\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u0249\b\u001f\u0001\u001f"+
		"\u0001\u001f\u0003\u001f\u024d\b\u001f\u0001\u001f\u0003\u001f\u0250\b"+
		"\u001f\u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 "+
		"\u0005 \u025b\b \n \f \u025e\t \u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0005 \u026e"+
		"\b \n \f \u0271\t \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0005 \u027b\b \n \f \u027e\t \u0001 \u0001 \u0001 \u0003 \u0283\b "+
		"\u0001 \u0003 \u0286\b \u0001!\u0001!\u0001!\u0001!\u0005!\u028c\b!\n"+
		"!\f!\u028f\t!\u0001!\u0001!\u0001!\u0000\u0000\"\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,."+
		"02468:<>@B\u0000\u0007\u0001\u0000\b\u000b\u0002\u0000\u0015\u0015\u001f"+
		"\u001f\u0001\u0000\u0010\u0011\u0002\u0000\u000e\u000f\u0013\u0013\u0002"+
		"\u0000\u0012\u0012\u0015\u0015\u0001\u0000\'(\u0001\u0000\f\r\u02d7\u0000"+
		"G\u0001\u0000\u0000\u0000\u0002P\u0001\u0000\u0000\u0000\u0004`\u0001"+
		"\u0000\u0000\u0000\u0006b\u0001\u0000\u0000\u0000\bd\u0001\u0000\u0000"+
		"\u0000\nl\u0001\u0000\u0000\u0000\f\u008e\u0001\u0000\u0000\u0000\u000e"+
		"\u0090\u0001\u0000\u0000\u0000\u0010\u009b\u0001\u0000\u0000\u0000\u0012"+
		"\u00a2\u0001\u0000\u0000\u0000\u0014\u00a4\u0001\u0000\u0000\u0000\u0016"+
		"\u00a6\u0001\u0000\u0000\u0000\u0018\u00ae\u0001\u0000\u0000\u0000\u001a"+
		"\u00b9\u0001\u0000\u0000\u0000\u001c\u00bb\u0001\u0000\u0000\u0000\u001e"+
		"\u00c3\u0001\u0000\u0000\u0000 \u00c8\u0001\u0000\u0000\u0000\"\u00dd"+
		"\u0001\u0000\u0000\u0000$\u00df\u0001\u0000\u0000\u0000&\u00ef\u0001\u0000"+
		"\u0000\u0000(\u00f7\u0001\u0000\u0000\u0000*\u0100\u0001\u0000\u0000\u0000"+
		",\u010a\u0001\u0000\u0000\u0000.\u0133\u0001\u0000\u0000\u00000\u0135"+
		"\u0001\u0000\u0000\u00002\u0141\u0001\u0000\u0000\u00004\u016d\u0001\u0000"+
		"\u0000\u00006\u0199\u0001\u0000\u0000\u00008\u01c5\u0001\u0000\u0000\u0000"+
		":\u01f2\u0001\u0000\u0000\u0000<\u021f\u0001\u0000\u0000\u0000>\u024f"+
		"\u0001\u0000\u0000\u0000@\u0285\u0001\u0000\u0000\u0000B\u0287\u0001\u0000"+
		"\u0000\u0000DF\u0003\u0004\u0002\u0000ED\u0001\u0000\u0000\u0000FI\u0001"+
		"\u0000\u0000\u0000GE\u0001\u0000\u0000\u0000GH\u0001\u0000\u0000\u0000"+
		"HJ\u0001\u0000\u0000\u0000IG\u0001\u0000\u0000\u0000JK\u0003\u0014\n\u0000"+
		"KL\u0005\u0000\u0000\u0001L\u0001\u0001\u0000\u0000\u0000MO\u0003\u0004"+
		"\u0002\u0000NM\u0001\u0000\u0000\u0000OR\u0001\u0000\u0000\u0000PN\u0001"+
		"\u0000\u0000\u0000PQ\u0001\u0000\u0000\u0000QS\u0001\u0000\u0000\u0000"+
		"RP\u0001\u0000\u0000\u0000ST\u0003\u0006\u0003\u0000TU\u0005\u0000\u0000"+
		"\u0001U\u0003\u0001\u0000\u0000\u0000VW\u0005*\u0000\u0000WX\u0005\u001d"+
		"\u0000\u0000XY\u0003,\u0016\u0000YZ\u0005(\u0000\u0000Za\u0001\u0000\u0000"+
		"\u0000[\\\u0003B!\u0000\\]\u0005\u001d\u0000\u0000]^\u0003@ \u0000^_\u0005"+
		"(\u0000\u0000_a\u0001\u0000\u0000\u0000`V\u0001\u0000\u0000\u0000`[\u0001"+
		"\u0000\u0000\u0000a\u0005\u0001\u0000\u0000\u0000bc\u0003\b\u0004\u0000"+
		"c\u0007\u0001\u0000\u0000\u0000di\u0003\n\u0005\u0000ef\u0005\u0007\u0000"+
		"\u0000fh\u0003\n\u0005\u0000ge\u0001\u0000\u0000\u0000hk\u0001\u0000\u0000"+
		"\u0000ig\u0001\u0000\u0000\u0000ij\u0001\u0000\u0000\u0000j\t\u0001\u0000"+
		"\u0000\u0000ki\u0001\u0000\u0000\u0000lq\u0003\f\u0006\u0000mn\u0005\u0006"+
		"\u0000\u0000np\u0003\f\u0006\u0000om\u0001\u0000\u0000\u0000ps\u0001\u0000"+
		"\u0000\u0000qo\u0001\u0000\u0000\u0000qr\u0001\u0000\u0000\u0000r\u000b"+
		"\u0001\u0000\u0000\u0000sq\u0001\u0000\u0000\u0000tx\u0003\u000e\u0007"+
		"\u0000uv\u0003(\u0014\u0000vw\u0003\u000e\u0007\u0000wy\u0001\u0000\u0000"+
		"\u0000xu\u0001\u0000\u0000\u0000xy\u0001\u0000\u0000\u0000y\u008f\u0001"+
		"\u0000\u0000\u0000z{\u0003\u0014\n\u0000{|\u0003(\u0014\u0000|}\u0003"+
		"\u0014\n\u0000}\u008f\u0001\u0000\u0000\u0000~\u007f\u00038\u001c\u0000"+
		"\u007f\u0080\u0003(\u0014\u0000\u0080\u0081\u00038\u001c\u0000\u0081\u008f"+
		"\u0001\u0000\u0000\u0000\u0082\u0083\u0003:\u001d\u0000\u0083\u0084\u0003"+
		"(\u0014\u0000\u0084\u0085\u0003:\u001d\u0000\u0085\u008f\u0001\u0000\u0000"+
		"\u0000\u0086\u0087\u0003<\u001e\u0000\u0087\u0088\u0003(\u0014\u0000\u0088"+
		"\u0089\u0003<\u001e\u0000\u0089\u008f\u0001\u0000\u0000\u0000\u008a\u008b"+
		"\u0003>\u001f\u0000\u008b\u008c\u0003(\u0014\u0000\u008c\u008d\u0003>"+
		"\u001f\u0000\u008d\u008f\u0001\u0000\u0000\u0000\u008et\u0001\u0000\u0000"+
		"\u0000\u008ez\u0001\u0000\u0000\u0000\u008e~\u0001\u0000\u0000\u0000\u008e"+
		"\u0082\u0001\u0000\u0000\u0000\u008e\u0086\u0001\u0000\u0000\u0000\u008e"+
		"\u008a\u0001\u0000\u0000\u0000\u008f\r\u0001\u0000\u0000\u0000\u0090\u0095"+
		"\u0003\u0010\b\u0000\u0091\u0092\u0007\u0000\u0000\u0000\u0092\u0094\u0003"+
		"\u0010\b\u0000\u0093\u0091\u0001\u0000\u0000\u0000\u0094\u0097\u0001\u0000"+
		"\u0000\u0000\u0095\u0093\u0001\u0000\u0000\u0000\u0095\u0096\u0001\u0000"+
		"\u0000\u0000\u0096\u000f\u0001\u0000\u0000\u0000\u0097\u0095\u0001\u0000"+
		"\u0000\u0000\u0098\u0099\u0007\u0001\u0000\u0000\u0099\u009c\u0003\u0010"+
		"\b\u0000\u009a\u009c\u0003\u0012\t\u0000\u009b\u0098\u0001\u0000\u0000"+
		"\u0000\u009b\u009a\u0001\u0000\u0000\u0000\u009c\u0011\u0001\u0000\u0000"+
		"\u0000\u009d\u009e\u0005#\u0000\u0000\u009e\u009f\u0003\u0006\u0003\u0000"+
		"\u009f\u00a0\u0005$\u0000\u0000\u00a0\u00a3\u0001\u0000\u0000\u0000\u00a1"+
		"\u00a3\u00034\u001a\u0000\u00a2\u009d\u0001\u0000\u0000\u0000\u00a2\u00a1"+
		"\u0001\u0000\u0000\u0000\u00a3\u0013\u0001\u0000\u0000\u0000\u00a4\u00a5"+
		"\u0003\u0016\u000b\u0000\u00a5\u0015\u0001\u0000\u0000\u0000\u00a6\u00ab"+
		"\u0003\u0018\f\u0000\u00a7\u00a8\u0007\u0002\u0000\u0000\u00a8\u00aa\u0003"+
		"\u0018\f\u0000\u00a9\u00a7\u0001\u0000\u0000\u0000\u00aa\u00ad\u0001\u0000"+
		"\u0000\u0000\u00ab\u00a9\u0001\u0000\u0000\u0000\u00ab\u00ac\u0001\u0000"+
		"\u0000\u0000\u00ac\u0017\u0001\u0000\u0000\u0000\u00ad\u00ab\u0001\u0000"+
		"\u0000\u0000\u00ae\u00b3\u0003\u001a\r\u0000\u00af\u00b0\u0007\u0003\u0000"+
		"\u0000\u00b0\u00b2\u0003\u001a\r\u0000\u00b1\u00af\u0001\u0000\u0000\u0000"+
		"\u00b2\u00b5\u0001\u0000\u0000\u0000\u00b3\u00b1\u0001\u0000\u0000\u0000"+
		"\u00b3\u00b4\u0001\u0000\u0000\u0000\u00b4\u0019\u0001\u0000\u0000\u0000"+
		"\u00b5\u00b3\u0001\u0000\u0000\u0000\u00b6\u00b7\u0005\u0011\u0000\u0000"+
		"\u00b7\u00ba\u0003\u001a\r\u0000\u00b8\u00ba\u0003\u001c\u000e\u0000\u00b9"+
		"\u00b6\u0001\u0000\u0000\u0000\u00b9\u00b8\u0001\u0000\u0000\u0000\u00ba"+
		"\u001b\u0001\u0000\u0000\u0000\u00bb\u00c0\u0003\u001e\u000f\u0000\u00bc"+
		"\u00bd\u0005\u0017\u0000\u0000\u00bd\u00bf\u0003\u001e\u000f\u0000\u00be"+
		"\u00bc\u0001\u0000\u0000\u0000\u00bf\u00c2\u0001\u0000\u0000\u0000\u00c0"+
		"\u00be\u0001\u0000\u0000\u0000\u00c0\u00c1\u0001\u0000\u0000\u0000\u00c1"+
		"\u001d\u0001\u0000\u0000\u0000\u00c2\u00c0\u0001\u0000\u0000\u0000\u00c3"+
		"\u00c6\u0003 \u0010\u0000\u00c4\u00c5\u0005\u0016\u0000\u0000\u00c5\u00c7"+
		"\u0003\u001a\r\u0000\u00c6\u00c4\u0001\u0000\u0000\u0000\u00c6\u00c7\u0001"+
		"\u0000\u0000\u0000\u00c7\u001f\u0001\u0000\u0000\u0000\u00c8\u00cc\u0003"+
		"\"\u0011\u0000\u00c9\u00cb\u0007\u0004\u0000\u0000\u00ca\u00c9\u0001\u0000"+
		"\u0000\u0000\u00cb\u00ce\u0001\u0000\u0000\u0000\u00cc\u00ca\u0001\u0000"+
		"\u0000\u0000\u00cc\u00cd\u0001\u0000\u0000\u0000\u00cd!\u0001\u0000\u0000"+
		"\u0000\u00ce\u00cc\u0001\u0000\u0000\u0000\u00cf\u00d0\u0005#\u0000\u0000"+
		"\u00d0\u00d1\u0003\u0014\n\u0000\u00d1\u00d2\u0005$\u0000\u0000\u00d2"+
		"\u00de\u0001\u0000\u0000\u0000\u00d3\u00d4\u0005\u0018\u0000\u0000\u00d4"+
		"\u00d5\u0005#\u0000\u0000\u00d5\u00d6\u0003\u0014\n\u0000\u00d6\u00d7"+
		"\u0005$\u0000\u0000\u00d7\u00de\u0001\u0000\u0000\u0000\u00d8\u00d9\u0005"+
		"\u0014\u0000\u0000\u00d9\u00da\u0003\u0014\n\u0000\u00da\u00db\u0005\u0014"+
		"\u0000\u0000\u00db\u00de\u0001\u0000\u0000\u0000\u00dc\u00de\u00036\u001b"+
		"\u0000\u00dd\u00cf\u0001\u0000\u0000\u0000\u00dd\u00d3\u0001\u0000\u0000"+
		"\u0000\u00dd\u00d8\u0001\u0000\u0000\u0000\u00dd\u00dc\u0001\u0000\u0000"+
		"\u0000\u00de#\u0001\u0000\u0000\u0000\u00df\u00e0\u0005*\u0000\u0000\u00e0"+
		"\u00e9\u0005#\u0000\u0000\u00e1\u00e6\u0003*\u0015\u0000\u00e2\u00e3\u0007"+
		"\u0005\u0000\u0000\u00e3\u00e5\u0003*\u0015\u0000\u00e4\u00e2\u0001\u0000"+
		"\u0000\u0000\u00e5\u00e8\u0001\u0000\u0000\u0000\u00e6\u00e4\u0001\u0000"+
		"\u0000\u0000\u00e6\u00e7\u0001\u0000\u0000\u0000\u00e7\u00ea\u0001\u0000"+
		"\u0000\u0000\u00e8\u00e6\u0001\u0000\u0000\u0000\u00e9\u00e1\u0001\u0000"+
		"\u0000\u0000\u00e9\u00ea\u0001\u0000\u0000\u0000\u00ea\u00eb\u0001\u0000"+
		"\u0000\u0000\u00eb\u00ec\u0005$\u0000\u0000\u00ec%\u0001\u0000\u0000\u0000"+
		"\u00ed\u00f0\u0003$\u0012\u0000\u00ee\u00f0\u0005*\u0000\u0000\u00ef\u00ed"+
		"\u0001\u0000\u0000\u0000\u00ef\u00ee\u0001\u0000\u0000\u0000\u00f0\'\u0001"+
		"\u0000\u0000\u0000\u00f1\u00f8\u0005\u0019\u0000\u0000\u00f2\u00f8\u0005"+
		"\u001a\u0000\u0000\u00f3\u00f8\u0005\u001b\u0000\u0000\u00f4\u00f8\u0005"+
		"\u001c\u0000\u0000\u00f5\u00f8\u0005\u001d\u0000\u0000\u00f6\u00f8\u0005"+
		"\u001e\u0000\u0000\u00f7\u00f1\u0001\u0000\u0000\u0000\u00f7\u00f2\u0001"+
		"\u0000\u0000\u0000\u00f7\u00f3\u0001\u0000\u0000\u0000\u00f7\u00f4\u0001"+
		"\u0000\u0000\u0000\u00f7\u00f5\u0001\u0000\u0000\u0000\u00f7\u00f6\u0001"+
		"\u0000\u0000\u0000\u00f8)\u0001\u0000\u0000\u0000\u00f9\u0101\u0003\u0014"+
		"\n\u0000\u00fa\u0101\u0003\u0006\u0003\u0000\u00fb\u0101\u0003:\u001d"+
		"\u0000\u00fc\u0101\u0003<\u001e\u0000\u00fd\u0101\u0003>\u001f\u0000\u00fe"+
		"\u0101\u00038\u001c\u0000\u00ff\u0101\u0003@ \u0000\u0100\u00f9\u0001"+
		"\u0000\u0000\u0000\u0100\u00fa\u0001\u0000\u0000\u0000\u0100\u00fb\u0001"+
		"\u0000\u0000\u0000\u0100\u00fc\u0001\u0000\u0000\u0000\u0100\u00fd\u0001"+
		"\u0000\u0000\u0000\u0100\u00fe\u0001\u0000\u0000\u0000\u0100\u00ff\u0001"+
		"\u0000\u0000\u0000\u0101+\u0001\u0000\u0000\u0000\u0102\u010b\u0003.\u0017"+
		"\u0000\u0103\u010b\u0003\u0014\n\u0000\u0104\u010b\u0003\u0006\u0003\u0000"+
		"\u0105\u010b\u0003:\u001d\u0000\u0106\u010b\u0003<\u001e\u0000\u0107\u010b"+
		"\u0003>\u001f\u0000\u0108\u010b\u00038\u001c\u0000\u0109\u010b\u0003@"+
		" \u0000\u010a\u0102\u0001\u0000\u0000\u0000\u010a\u0103\u0001\u0000\u0000"+
		"\u0000\u010a\u0104\u0001\u0000\u0000\u0000\u010a\u0105\u0001\u0000\u0000"+
		"\u0000\u010a\u0106\u0001\u0000\u0000\u0000\u010a\u0107\u0001\u0000\u0000"+
		"\u0000\u010a\u0108\u0001\u0000\u0000\u0000\u010a\u0109\u0001\u0000\u0000"+
		"\u0000\u010b-\u0001\u0000\u0000\u0000\u010c\u010d\u0005\u0001\u0000\u0000"+
		"\u010d\u010e\u0003\u0006\u0003\u0000\u010e\u010f\u0005\u0002\u0000\u0000"+
		"\u010f\u0117\u0003.\u0017\u0000\u0110\u0111\u0005\u0004\u0000\u0000\u0111"+
		"\u0112\u0003\u0006\u0003\u0000\u0112\u0113\u0005\u0002\u0000\u0000\u0113"+
		"\u0114\u0003.\u0017\u0000\u0114\u0116\u0001\u0000\u0000\u0000\u0115\u0110"+
		"\u0001\u0000\u0000\u0000\u0116\u0119\u0001\u0000\u0000\u0000\u0117\u0115"+
		"\u0001\u0000\u0000\u0000\u0117\u0118\u0001\u0000\u0000\u0000\u0118\u011a"+
		"\u0001\u0000\u0000\u0000\u0119\u0117\u0001\u0000\u0000\u0000\u011a\u011b"+
		"\u0005\u0003\u0000\u0000\u011b\u011c\u0003.\u0017\u0000\u011c\u011d\u0005"+
		"\u0005\u0000\u0000\u011d\u0134\u0001\u0000\u0000\u0000\u011e\u011f\u0005"+
		"\u0001\u0000\u0000\u011f\u0120\u0005#\u0000\u0000\u0120\u0121\u0003\u0006"+
		"\u0003\u0000\u0121\u0122\u0007\u0005\u0000\u0000\u0122\u012a\u0003.\u0017"+
		"\u0000\u0123\u0124\u0007\u0005\u0000\u0000\u0124\u0125\u0003\u0006\u0003"+
		"\u0000\u0125\u0126\u0007\u0005\u0000\u0000\u0126\u0127\u0003.\u0017\u0000"+
		"\u0127\u0129\u0001\u0000\u0000\u0000\u0128\u0123\u0001\u0000\u0000\u0000"+
		"\u0129\u012c\u0001\u0000\u0000\u0000\u012a\u0128\u0001\u0000\u0000\u0000"+
		"\u012a\u012b\u0001\u0000\u0000\u0000\u012b\u012d\u0001\u0000\u0000\u0000"+
		"\u012c\u012a\u0001\u0000\u0000\u0000\u012d\u012e\u0007\u0005\u0000\u0000"+
		"\u012e\u012f\u0003.\u0017\u0000\u012f\u0130\u0005$\u0000\u0000\u0130\u0134"+
		"\u0001\u0000\u0000\u0000\u0131\u0134\u00030\u0018\u0000\u0132\u0134\u0003"+
		"&\u0013\u0000\u0133\u010c\u0001\u0000\u0000\u0000\u0133\u011e\u0001\u0000"+
		"\u0000\u0000\u0133\u0131\u0001\u0000\u0000\u0000\u0133\u0132\u0001\u0000"+
		"\u0000\u0000\u0134/\u0001\u0000\u0000\u0000\u0135\u0136\u00032\u0019\u0000"+
		"\u0136\u0137\u0005#\u0000\u0000\u0137\u0138\u0003.\u0017\u0000\u0138\u0139"+
		"\u0005$\u0000\u0000\u01391\u0001\u0000\u0000\u0000\u013a\u0142\u00052"+
		"\u0000\u0000\u013b\u0142\u00053\u0000\u0000\u013c\u0142\u00054\u0000\u0000"+
		"\u013d\u0142\u00055\u0000\u0000\u013e\u0142\u00056\u0000\u0000\u013f\u0142"+
		"\u00057\u0000\u0000\u0140\u0142\u00058\u0000\u0000\u0141\u013a\u0001\u0000"+
		"\u0000\u0000\u0141\u013b\u0001\u0000\u0000\u0000\u0141\u013c\u0001\u0000"+
		"\u0000\u0000\u0141\u013d\u0001\u0000\u0000\u0000\u0141\u013e\u0001\u0000"+
		"\u0000\u0000\u0141\u013f\u0001\u0000\u0000\u0000\u0141\u0140\u0001\u0000"+
		"\u0000\u0000\u01423\u0001\u0000\u0000\u0000\u0143\u016e\u0007\u0006\u0000"+
		"\u0000\u0144\u0145\u0005\u0001\u0000\u0000\u0145\u0146\u0003\u0006\u0003"+
		"\u0000\u0146\u0147\u0005\u0002\u0000\u0000\u0147\u014f\u0003\u0006\u0003"+
		"\u0000\u0148\u0149\u0005\u0004\u0000\u0000\u0149\u014a\u0003\u0006\u0003"+
		"\u0000\u014a\u014b\u0005\u0002\u0000\u0000\u014b\u014c\u0003\u0006\u0003"+
		"\u0000\u014c\u014e\u0001\u0000\u0000\u0000\u014d\u0148\u0001\u0000\u0000"+
		"\u0000\u014e\u0151\u0001\u0000\u0000\u0000\u014f\u014d\u0001\u0000\u0000"+
		"\u0000\u014f\u0150\u0001\u0000\u0000\u0000\u0150\u0152\u0001\u0000\u0000"+
		"\u0000\u0151\u014f\u0001\u0000\u0000\u0000\u0152\u0153\u0005\u0003\u0000"+
		"\u0000\u0153\u0154\u0003\u0006\u0003\u0000\u0154\u0155\u0005\u0005\u0000"+
		"\u0000\u0155\u016e\u0001\u0000\u0000\u0000\u0156\u0157\u0005\u0001\u0000"+
		"\u0000\u0157\u0158\u0005#\u0000\u0000\u0158\u0159\u0003\u0006\u0003\u0000"+
		"\u0159\u015a\u0007\u0005\u0000\u0000\u015a\u0162\u0003\u0006\u0003\u0000"+
		"\u015b\u015c\u0007\u0005\u0000\u0000\u015c\u015d\u0003\u0006\u0003\u0000"+
		"\u015d\u015e\u0007\u0005\u0000\u0000\u015e\u015f\u0003\u0006\u0003\u0000"+
		"\u015f\u0161\u0001\u0000\u0000\u0000\u0160\u015b\u0001\u0000\u0000\u0000"+
		"\u0161\u0164\u0001\u0000\u0000\u0000\u0162\u0160\u0001\u0000\u0000\u0000"+
		"\u0162\u0163\u0001\u0000\u0000\u0000\u0163\u0165\u0001\u0000\u0000\u0000"+
		"\u0164\u0162\u0001\u0000\u0000\u0000\u0165\u0166\u0007\u0005\u0000\u0000"+
		"\u0166\u0167\u0003\u0006\u0003\u0000\u0167\u0168\u0005$\u0000\u0000\u0168"+
		"\u016e\u0001\u0000\u0000\u0000\u0169\u016b\u00052\u0000\u0000\u016a\u0169"+
		"\u0001\u0000\u0000\u0000\u016a\u016b\u0001\u0000\u0000\u0000\u016b\u016c"+
		"\u0001\u0000\u0000\u0000\u016c\u016e\u0003&\u0013\u0000\u016d\u0143\u0001"+
		"\u0000\u0000\u0000\u016d\u0144\u0001\u0000\u0000\u0000\u016d\u0156\u0001"+
		"\u0000\u0000\u0000\u016d\u016a\u0001\u0000\u0000\u0000\u016e5\u0001\u0000"+
		"\u0000\u0000\u016f\u0170\u0005\u0001\u0000\u0000\u0170\u0171\u0003\u0006"+
		"\u0003\u0000\u0171\u0172\u0005\u0002\u0000\u0000\u0172\u017a\u0003\u0014"+
		"\n\u0000\u0173\u0174\u0005\u0004\u0000\u0000\u0174\u0175\u0003\u0006\u0003"+
		"\u0000\u0175\u0176\u0005\u0002\u0000\u0000\u0176\u0177\u0003\u0014\n\u0000"+
		"\u0177\u0179\u0001\u0000\u0000\u0000\u0178\u0173\u0001\u0000\u0000\u0000"+
		"\u0179\u017c\u0001\u0000\u0000\u0000\u017a\u0178\u0001\u0000\u0000\u0000"+
		"\u017a\u017b\u0001\u0000\u0000\u0000\u017b\u017d\u0001\u0000\u0000\u0000"+
		"\u017c\u017a\u0001\u0000\u0000\u0000\u017d\u017e\u0005\u0003\u0000\u0000"+
		"\u017e\u017f\u0003\u0014\n\u0000\u017f\u0180\u0005\u0005\u0000\u0000\u0180"+
		"\u019a\u0001\u0000\u0000\u0000\u0181\u0182\u0005\u0001\u0000\u0000\u0182"+
		"\u0183\u0005#\u0000\u0000\u0183\u0184\u0003\u0006\u0003\u0000\u0184\u0185"+
		"\u0007\u0005\u0000\u0000\u0185\u018d\u0003\u0014\n\u0000\u0186\u0187\u0007"+
		"\u0005\u0000\u0000\u0187\u0188\u0003\u0006\u0003\u0000\u0188\u0189\u0007"+
		"\u0005\u0000\u0000\u0189\u018a\u0003\u0014\n\u0000\u018a\u018c\u0001\u0000"+
		"\u0000\u0000\u018b\u0186\u0001\u0000\u0000\u0000\u018c\u018f\u0001\u0000"+
		"\u0000\u0000\u018d\u018b\u0001\u0000\u0000\u0000\u018d\u018e\u0001\u0000"+
		"\u0000\u0000\u018e\u0190\u0001\u0000\u0000\u0000\u018f\u018d\u0001\u0000"+
		"\u0000\u0000\u0190\u0191\u0007\u0005\u0000\u0000\u0191\u0192\u0003\u0014"+
		"\n\u0000\u0192\u0193\u0005$\u0000\u0000\u0193\u019a\u0001\u0000\u0000"+
		"\u0000\u0194\u019a\u0005,\u0000\u0000\u0195\u0197\u00053\u0000\u0000\u0196"+
		"\u0195\u0001\u0000\u0000\u0000\u0196\u0197\u0001\u0000\u0000\u0000\u0197"+
		"\u0198\u0001\u0000\u0000\u0000\u0198\u019a\u0003&\u0013\u0000\u0199\u016f"+
		"\u0001\u0000\u0000\u0000\u0199\u0181\u0001\u0000\u0000\u0000\u0199\u0194"+
		"\u0001\u0000\u0000\u0000\u0199\u0196\u0001\u0000\u0000\u0000\u019a7\u0001"+
		"\u0000\u0000\u0000\u019b\u019c\u0005\u0001\u0000\u0000\u019c\u019d\u0003"+
		"\u0006\u0003\u0000\u019d\u019e\u0005\u0002\u0000\u0000\u019e\u01a6\u0003"+
		"8\u001c\u0000\u019f\u01a0\u0005\u0004\u0000\u0000\u01a0\u01a1\u0003\u0006"+
		"\u0003\u0000\u01a1\u01a2\u0005\u0002\u0000\u0000\u01a2\u01a3\u00038\u001c"+
		"\u0000\u01a3\u01a5\u0001\u0000\u0000\u0000\u01a4\u019f\u0001\u0000\u0000"+
		"\u0000\u01a5\u01a8\u0001\u0000\u0000\u0000\u01a6\u01a4\u0001\u0000\u0000"+
		"\u0000\u01a6\u01a7\u0001\u0000\u0000\u0000\u01a7\u01a9\u0001\u0000\u0000"+
		"\u0000\u01a8\u01a6\u0001\u0000\u0000\u0000\u01a9\u01aa\u0005\u0003\u0000"+
		"\u0000\u01aa\u01ab\u00038\u001c\u0000\u01ab\u01ac\u0005\u0005\u0000\u0000"+
		"\u01ac\u01c6\u0001\u0000\u0000\u0000\u01ad\u01ae\u0005\u0001\u0000\u0000"+
		"\u01ae\u01af\u0005#\u0000\u0000\u01af\u01b0\u0003\u0006\u0003\u0000\u01b0"+
		"\u01b1\u0007\u0005\u0000\u0000\u01b1\u01b9\u00038\u001c\u0000\u01b2\u01b3"+
		"\u0007\u0005\u0000\u0000\u01b3\u01b4\u0003\u0006\u0003\u0000\u01b4\u01b5"+
		"\u0007\u0005\u0000\u0000\u01b5\u01b6\u00038\u001c\u0000\u01b6\u01b8\u0001"+
		"\u0000\u0000\u0000\u01b7\u01b2\u0001\u0000\u0000\u0000\u01b8\u01bb\u0001"+
		"\u0000\u0000\u0000\u01b9\u01b7\u0001\u0000\u0000\u0000\u01b9\u01ba\u0001"+
		"\u0000\u0000\u0000\u01ba\u01bc\u0001\u0000\u0000\u0000\u01bb\u01b9\u0001"+
		"\u0000\u0000\u0000\u01bc\u01bd\u0007\u0005\u0000\u0000\u01bd\u01be\u0003"+
		"8\u001c\u0000\u01be\u01bf\u0005$\u0000\u0000\u01bf\u01c6\u0001\u0000\u0000"+
		"\u0000\u01c0\u01c6\u0005+\u0000\u0000\u01c1\u01c3\u00054\u0000\u0000\u01c2"+
		"\u01c1\u0001\u0000\u0000\u0000\u01c2\u01c3\u0001\u0000\u0000\u0000\u01c3"+
		"\u01c4\u0001\u0000\u0000\u0000\u01c4\u01c6\u0003&\u0013\u0000\u01c5\u019b"+
		"\u0001\u0000\u0000\u0000\u01c5\u01ad\u0001\u0000\u0000\u0000\u01c5\u01c0"+
		"\u0001\u0000\u0000\u0000\u01c5\u01c2\u0001\u0000\u0000\u0000\u01c69\u0001"+
		"\u0000\u0000\u0000\u01c7\u01c8\u0005\u0001\u0000\u0000\u01c8\u01c9\u0003"+
		"\u0006\u0003\u0000\u01c9\u01ca\u0005\u0002\u0000\u0000\u01ca\u01d2\u0003"+
		":\u001d\u0000\u01cb\u01cc\u0005\u0004\u0000\u0000\u01cc\u01cd\u0003\u0006"+
		"\u0003\u0000\u01cd\u01ce\u0005\u0002\u0000\u0000\u01ce\u01cf\u0003:\u001d"+
		"\u0000\u01cf\u01d1\u0001\u0000\u0000\u0000\u01d0\u01cb\u0001\u0000\u0000"+
		"\u0000\u01d1\u01d4\u0001\u0000\u0000\u0000\u01d2\u01d0\u0001\u0000\u0000"+
		"\u0000\u01d2\u01d3\u0001\u0000\u0000\u0000\u01d3\u01d5\u0001\u0000\u0000"+
		"\u0000\u01d4\u01d2\u0001\u0000\u0000\u0000\u01d5\u01d6\u0005\u0003\u0000"+
		"\u0000\u01d6\u01d7\u0003:\u001d\u0000\u01d7\u01d8\u0005\u0005\u0000\u0000"+
		"\u01d8\u01f3\u0001\u0000\u0000\u0000\u01d9\u01da\u0005\u0001\u0000\u0000"+
		"\u01da\u01db\u0005#\u0000\u0000\u01db\u01dc\u0003\u0006\u0003\u0000\u01dc"+
		"\u01dd\u0007\u0005\u0000\u0000\u01dd\u01e5\u0003:\u001d\u0000\u01de\u01df"+
		"\u0007\u0005\u0000\u0000\u01df\u01e0\u0003\u0006\u0003\u0000\u01e0\u01e1"+
		"\u0007\u0005\u0000\u0000\u01e1\u01e2\u0003:\u001d\u0000\u01e2\u01e4\u0001"+
		"\u0000\u0000\u0000\u01e3\u01de\u0001\u0000\u0000\u0000\u01e4\u01e7\u0001"+
		"\u0000\u0000\u0000\u01e5\u01e3\u0001\u0000\u0000\u0000\u01e5\u01e6\u0001"+
		"\u0000\u0000\u0000\u01e6\u01e8\u0001\u0000\u0000\u0000\u01e7\u01e5\u0001"+
		"\u0000\u0000\u0000\u01e8\u01e9\u0007\u0005\u0000\u0000\u01e9\u01ea\u0003"+
		":\u001d\u0000\u01ea\u01eb\u0005$\u0000\u0000\u01eb\u01f3\u0001\u0000\u0000"+
		"\u0000\u01ec\u01f3\u0005.\u0000\u0000\u01ed\u01f3\u0005 \u0000\u0000\u01ee"+
		"\u01f0\u00055\u0000\u0000\u01ef\u01ee\u0001\u0000\u0000\u0000\u01ef\u01f0"+
		"\u0001\u0000\u0000\u0000\u01f0\u01f1\u0001\u0000\u0000\u0000\u01f1\u01f3"+
		"\u0003&\u0013\u0000\u01f2\u01c7\u0001\u0000\u0000\u0000\u01f2\u01d9\u0001"+
		"\u0000\u0000\u0000\u01f2\u01ec\u0001\u0000\u0000\u0000\u01f2\u01ed\u0001"+
		"\u0000\u0000\u0000\u01f2\u01ef\u0001\u0000\u0000\u0000\u01f3;\u0001\u0000"+
		"\u0000\u0000\u01f4\u01f5\u0005\u0001\u0000\u0000\u01f5\u01f6\u0003\u0006"+
		"\u0003\u0000\u01f6\u01f7\u0005\u0002\u0000\u0000\u01f7\u01ff\u0003<\u001e"+
		"\u0000\u01f8\u01f9\u0005\u0004\u0000\u0000\u01f9\u01fa\u0003\u0006\u0003"+
		"\u0000\u01fa\u01fb\u0005\u0002\u0000\u0000\u01fb\u01fc\u0003<\u001e\u0000"+
		"\u01fc\u01fe\u0001\u0000\u0000\u0000\u01fd\u01f8\u0001\u0000\u0000\u0000"+
		"\u01fe\u0201\u0001\u0000\u0000\u0000\u01ff\u01fd\u0001\u0000\u0000\u0000"+
		"\u01ff\u0200\u0001\u0000\u0000\u0000\u0200\u0202\u0001\u0000\u0000\u0000"+
		"\u0201\u01ff\u0001\u0000\u0000\u0000\u0202\u0203\u0005\u0003\u0000\u0000"+
		"\u0203\u0204\u0003<\u001e\u0000\u0204\u0205\u0005\u0005\u0000\u0000\u0205"+
		"\u0220\u0001\u0000\u0000\u0000\u0206\u0207\u0005\u0001\u0000\u0000\u0207"+
		"\u0208\u0005#\u0000\u0000\u0208\u0209\u0003\u0006\u0003\u0000\u0209\u020a"+
		"\u0007\u0005\u0000\u0000\u020a\u0212\u0003<\u001e\u0000\u020b\u020c\u0007"+
		"\u0005\u0000\u0000\u020c\u020d\u0003\u0006\u0003\u0000\u020d\u020e\u0007"+
		"\u0005\u0000\u0000\u020e\u020f\u0003<\u001e\u0000\u020f\u0211\u0001\u0000"+
		"\u0000\u0000\u0210\u020b\u0001\u0000\u0000\u0000\u0211\u0214\u0001\u0000"+
		"\u0000\u0000\u0212\u0210\u0001\u0000\u0000\u0000\u0212\u0213\u0001\u0000"+
		"\u0000\u0000\u0213\u0215\u0001\u0000\u0000\u0000\u0214\u0212\u0001\u0000"+
		"\u0000\u0000\u0215\u0216\u0007\u0005\u0000\u0000\u0216\u0217\u0003<\u001e"+
		"\u0000\u0217\u0218\u0005$\u0000\u0000\u0218\u0220\u0001\u0000\u0000\u0000"+
		"\u0219\u0220\u0005/\u0000\u0000\u021a\u0220\u0005!\u0000\u0000\u021b\u021d"+
		"\u00056\u0000\u0000\u021c\u021b\u0001\u0000\u0000\u0000\u021c\u021d\u0001"+
		"\u0000\u0000\u0000\u021d\u021e\u0001\u0000\u0000\u0000\u021e\u0220\u0003"+
		"&\u0013\u0000\u021f\u01f4\u0001\u0000\u0000\u0000\u021f\u0206\u0001\u0000"+
		"\u0000\u0000\u021f\u0219\u0001\u0000\u0000\u0000\u021f\u021a\u0001\u0000"+
		"\u0000\u0000\u021f\u021c\u0001\u0000\u0000\u0000\u0220=\u0001\u0000\u0000"+
		"\u0000\u0221\u0222\u0005\u0001\u0000\u0000\u0222\u0223\u0003\u0006\u0003"+
		"\u0000\u0223\u0224\u0005\u0002\u0000\u0000\u0224\u022c\u0003>\u001f\u0000"+
		"\u0225\u0226\u0005\u0004\u0000\u0000\u0226\u0227\u0003\u0006\u0003\u0000"+
		"\u0227\u0228\u0005\u0002\u0000\u0000\u0228\u0229\u0003>\u001f\u0000\u0229"+
		"\u022b\u0001\u0000\u0000\u0000\u022a\u0225\u0001\u0000\u0000\u0000\u022b"+
		"\u022e\u0001\u0000\u0000\u0000\u022c\u022a\u0001\u0000\u0000\u0000\u022c"+
		"\u022d\u0001\u0000\u0000\u0000\u022d\u022f\u0001\u0000\u0000\u0000\u022e"+
		"\u022c\u0001\u0000\u0000\u0000\u022f\u0230\u0005\u0003\u0000\u0000\u0230"+
		"\u0231\u0003>\u001f\u0000\u0231\u0232\u0005\u0005\u0000\u0000\u0232\u0250"+
		"\u0001\u0000\u0000\u0000\u0233\u0234\u0005\u0001\u0000\u0000\u0234\u0235"+
		"\u0005#\u0000\u0000\u0235\u0236\u0003\u0006\u0003\u0000\u0236\u0237\u0007"+
		"\u0005\u0000\u0000\u0237\u023f\u0003>\u001f\u0000\u0238\u0239\u0007\u0005"+
		"\u0000\u0000\u0239\u023a\u0003\u0006\u0003\u0000\u023a\u023b\u0007\u0005"+
		"\u0000\u0000\u023b\u023c\u0003>\u001f\u0000\u023c\u023e\u0001\u0000\u0000"+
		"\u0000\u023d\u0238\u0001\u0000\u0000\u0000\u023e\u0241\u0001\u0000\u0000"+
		"\u0000\u023f\u023d\u0001\u0000\u0000\u0000\u023f\u0240\u0001\u0000\u0000"+
		"\u0000\u0240\u0242\u0001\u0000\u0000\u0000\u0241\u023f\u0001\u0000\u0000"+
		"\u0000\u0242\u0243\u0007\u0005\u0000\u0000\u0243\u0244\u0003>\u001f\u0000"+
		"\u0244\u0245\u0005$\u0000\u0000\u0245\u0250\u0001\u0000\u0000\u0000\u0246"+
		"\u0248\u00051\u0000\u0000\u0247\u0249\u00050\u0000\u0000\u0248\u0247\u0001"+
		"\u0000\u0000\u0000\u0248\u0249\u0001\u0000\u0000\u0000\u0249\u0250\u0001"+
		"\u0000\u0000\u0000\u024a\u0250\u0005\"\u0000\u0000\u024b\u024d\u00057"+
		"\u0000\u0000\u024c\u024b\u0001\u0000\u0000\u0000\u024c\u024d\u0001\u0000"+
		"\u0000\u0000\u024d\u024e\u0001\u0000\u0000\u0000\u024e\u0250\u0003&\u0013"+
		"\u0000\u024f\u0221\u0001\u0000\u0000\u0000\u024f\u0233\u0001\u0000\u0000"+
		"\u0000\u024f\u0246\u0001\u0000\u0000\u0000\u024f\u024a\u0001\u0000\u0000"+
		"\u0000\u024f\u024c\u0001\u0000\u0000\u0000\u0250?\u0001\u0000\u0000\u0000"+
		"\u0251\u0252\u0005\u0001\u0000\u0000\u0252\u0253\u0003\u0006\u0003\u0000"+
		"\u0253\u0254\u0005\u0002\u0000\u0000\u0254\u025c\u0003@ \u0000\u0255\u0256"+
		"\u0005\u0004\u0000\u0000\u0256\u0257\u0003\u0006\u0003\u0000\u0257\u0258"+
		"\u0005\u0002\u0000\u0000\u0258\u0259\u0003@ \u0000\u0259\u025b\u0001\u0000"+
		"\u0000\u0000\u025a\u0255\u0001\u0000\u0000\u0000\u025b\u025e\u0001\u0000"+
		"\u0000\u0000\u025c\u025a\u0001\u0000\u0000\u0000\u025c\u025d\u0001\u0000"+
		"\u0000\u0000\u025d\u025f\u0001\u0000\u0000\u0000\u025e\u025c\u0001\u0000"+
		"\u0000\u0000\u025f\u0260\u0005\u0003\u0000\u0000\u0260\u0261\u0003@ \u0000"+
		"\u0261\u0262\u0005\u0005\u0000\u0000\u0262\u0286\u0001\u0000\u0000\u0000"+
		"\u0263\u0264\u0005\u0001\u0000\u0000\u0264\u0265\u0005#\u0000\u0000\u0265"+
		"\u0266\u0003\u0006\u0003\u0000\u0266\u0267\u0007\u0005\u0000\u0000\u0267"+
		"\u026f\u0003@ \u0000\u0268\u0269\u0007\u0005\u0000\u0000\u0269\u026a\u0003"+
		"\u0006\u0003\u0000\u026a\u026b\u0007\u0005\u0000\u0000\u026b\u026c\u0003"+
		"@ \u0000\u026c\u026e\u0001\u0000\u0000\u0000\u026d\u0268\u0001\u0000\u0000"+
		"\u0000\u026e\u0271\u0001\u0000\u0000\u0000\u026f\u026d\u0001\u0000\u0000"+
		"\u0000\u026f\u0270\u0001\u0000\u0000\u0000\u0270\u0272\u0001\u0000\u0000"+
		"\u0000\u0271\u026f\u0001\u0000\u0000\u0000\u0272\u0273\u0007\u0005\u0000"+
		"\u0000\u0273\u0274\u0003@ \u0000\u0274\u0275\u0005$\u0000\u0000\u0275"+
		"\u0286\u0001\u0000\u0000\u0000\u0276\u0277\u0005%\u0000\u0000\u0277\u027c"+
		"\u0003*\u0015\u0000\u0278\u0279\u0005\'\u0000\u0000\u0279\u027b\u0003"+
		"*\u0015\u0000\u027a\u0278\u0001\u0000\u0000\u0000\u027b\u027e\u0001\u0000"+
		"\u0000\u0000\u027c\u027a\u0001\u0000\u0000\u0000\u027c\u027d\u0001\u0000"+
		"\u0000\u0000\u027d\u027f\u0001\u0000\u0000\u0000\u027e\u027c\u0001\u0000"+
		"\u0000\u0000\u027f\u0280\u0005&\u0000\u0000\u0280\u0286\u0001\u0000\u0000"+
		"\u0000\u0281\u0283\u00058\u0000\u0000\u0282\u0281\u0001\u0000\u0000\u0000"+
		"\u0282\u0283\u0001\u0000\u0000\u0000\u0283\u0284\u0001\u0000\u0000\u0000"+
		"\u0284\u0286\u0003&\u0013\u0000\u0285\u0251\u0001\u0000\u0000\u0000\u0285"+
		"\u0263\u0001\u0000\u0000\u0000\u0285\u0276\u0001\u0000\u0000\u0000\u0285"+
		"\u0282\u0001\u0000\u0000\u0000\u0286A\u0001\u0000\u0000\u0000\u0287\u0288"+
		"\u0005%\u0000\u0000\u0288\u028d\u0005*\u0000\u0000\u0289\u028a\u0005\'"+
		"\u0000\u0000\u028a\u028c\u0005*\u0000\u0000\u028b\u0289\u0001\u0000\u0000"+
		"\u0000\u028c\u028f\u0001\u0000\u0000\u0000\u028d\u028b\u0001\u0000\u0000"+
		"\u0000\u028d\u028e\u0001\u0000\u0000\u0000\u028e\u0290\u0001\u0000\u0000"+
		"\u0000\u028f\u028d\u0001\u0000\u0000\u0000\u0290\u0291\u0005&\u0000\u0000"+
		"\u0291C\u0001\u0000\u0000\u0000:GP`iqx\u008e\u0095\u009b\u00a2\u00ab\u00b3"+
		"\u00b9\u00c0\u00c6\u00cc\u00dd\u00e6\u00e9\u00ef\u00f7\u0100\u010a\u0117"+
		"\u012a\u0133\u0141\u014f\u0162\u016a\u016d\u017a\u018d\u0196\u0199\u01a6"+
		"\u01b9\u01c2\u01c5\u01d2\u01e5\u01ef\u01f2\u01ff\u0212\u021c\u021f\u022c"+
		"\u023f\u0248\u024c\u024f\u025c\u026f\u027c\u0282\u0285\u028d";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}