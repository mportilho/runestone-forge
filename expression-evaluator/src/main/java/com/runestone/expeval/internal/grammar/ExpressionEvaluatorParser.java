// Generated from com/runestone/expeval/internal/grammar/ExpressionEvaluator.g4 by ANTLR 4.13.1
package com.runestone.expeval.internal.grammar;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class ExpressionEvaluatorParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		IF=1, THEN=2, ELSE=3, ELSEIF=4, ENDIF=5, NULL=6, AND=7, OR=8, XOR=9, XNOR=10, 
		NAND=11, NOR=12, TRUE=13, FALSE=14, MULT=15, DIV=16, PLUS=17, MINUS=18, 
		PERCENT=19, MODULO=20, MODULUS=21, CONCAT=22, EXCLAMATION=23, EXPONENTIATION=24, 
		ROOT=25, SQRT=26, GT=27, GE=28, LT=29, LE=30, EQ=31, NEQ=32, NOT=33, REGEX_MATCH=34, 
		REGEX_NOT_MATCH=35, NOW_DATE=36, NOW_TIME=37, NOW_DATETIME=38, LPAREN=39, 
		RPAREN=40, LBRACKET=41, RBRACKET=42, COMMA=43, SEMI=44, PERIOD=45, NULLCOALESCE=46, 
		SAFE_NAV=47, IN=48, NOT_KW=49, BETWEEN=50, IDENTIFIER=51, STRING=52, NUMBER=53, 
		POSITIVE=54, DATE=55, TIME=56, TIME_OFFSET=57, DATETIME=58, BOOLEAN_TYPE=59, 
		NUMBER_TYPE=60, STRING_TYPE=61, DATE_TYPE=62, TIME_TYPE=63, DATETIME_TYPE=64, 
		VECTOR_TYPE=65, LINE_COMMENT=66, BLOCK_COMMENT=67, WS=68, ERROR_CHAR=69;
	public static final int
		RULE_mathStart = 0, RULE_assignmentStart = 1, RULE_logicalStart = 2, RULE_assignmentExpression = 3, 
		RULE_logicalExpression = 4, RULE_logicalOrExpression = 5, RULE_logicalAndExpression = 6, 
		RULE_logicalComparisonExpression = 7, RULE_logicalBitwiseExpression = 8, 
		RULE_logicalNotExpression = 9, RULE_logicalPrimary = 10, RULE_mathExpression = 11, 
		RULE_sumExpression = 12, RULE_multiplicationExpression = 13, RULE_unaryExpression = 14, 
		RULE_rootExpression = 15, RULE_exponentiationExpression = 16, RULE_postfixExpression = 17, 
		RULE_primaryMathExpression = 18, RULE_function = 19, RULE_referenceTarget = 20, 
		RULE_memberChain = 21, RULE_comparisonOperator = 22, RULE_allEntityTypes = 23, 
		RULE_assignmentValue = 24, RULE_genericEntity = 25, RULE_castExpression = 26, 
		RULE_typeHint = 27, RULE_logicalEntity = 28, RULE_numericEntity = 29, 
		RULE_stringConcatExpression = 30, RULE_stringEntity = 31, RULE_dateEntity = 32, 
		RULE_timeEntity = 33, RULE_dateTimeEntity = 34, RULE_vectorEntity = 35, 
		RULE_vectorOfVariables = 36;
	private static String[] makeRuleNames() {
		return new String[] {
			"mathStart", "assignmentStart", "logicalStart", "assignmentExpression", 
			"logicalExpression", "logicalOrExpression", "logicalAndExpression", "logicalComparisonExpression", 
			"logicalBitwiseExpression", "logicalNotExpression", "logicalPrimary", 
			"mathExpression", "sumExpression", "multiplicationExpression", "unaryExpression", 
			"rootExpression", "exponentiationExpression", "postfixExpression", "primaryMathExpression", 
			"function", "referenceTarget", "memberChain", "comparisonOperator", "allEntityTypes", 
			"assignmentValue", "genericEntity", "castExpression", "typeHint", "logicalEntity", 
			"numericEntity", "stringConcatExpression", "stringEntity", "dateEntity", 
			"timeEntity", "dateTimeEntity", "vectorEntity", "vectorOfVariables"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'if'", "'then'", "'else'", "'elsif'", "'endif'", "'null'", "'and'", 
			"'or'", "'xor'", "'xnor'", "'nand'", "'nor'", "'true'", "'false'", "'*'", 
			"'/'", "'+'", "'-'", "'%'", "'mod'", "'|'", "'||'", "'!'", "'^'", null, 
			"'sqrt'", "'>'", "'>='", "'<'", "'<='", "'='", null, null, "'=~'", "'!~'", 
			"'currDate'", "'currTime'", "'currDateTime'", "'('", "')'", "'['", "']'", 
			"','", "';'", "'.'", "'??'", "'?.'", "'in'", "'not'", "'between'", null, 
			null, null, null, null, null, null, null, "'<bool>'", "'<number>'", "'<text>'", 
			"'<date>'", "'<time>'", "'<datetime>'", "'<vector>'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "IF", "THEN", "ELSE", "ELSEIF", "ENDIF", "NULL", "AND", "OR", "XOR", 
			"XNOR", "NAND", "NOR", "TRUE", "FALSE", "MULT", "DIV", "PLUS", "MINUS", 
			"PERCENT", "MODULO", "MODULUS", "CONCAT", "EXCLAMATION", "EXPONENTIATION", 
			"ROOT", "SQRT", "GT", "GE", "LT", "LE", "EQ", "NEQ", "NOT", "REGEX_MATCH", 
			"REGEX_NOT_MATCH", "NOW_DATE", "NOW_TIME", "NOW_DATETIME", "LPAREN", 
			"RPAREN", "LBRACKET", "RBRACKET", "COMMA", "SEMI", "PERIOD", "NULLCOALESCE", 
			"SAFE_NAV", "IN", "NOT_KW", "BETWEEN", "IDENTIFIER", "STRING", "NUMBER", 
			"POSITIVE", "DATE", "TIME", "TIME_OFFSET", "DATETIME", "BOOLEAN_TYPE", 
			"NUMBER_TYPE", "STRING_TYPE", "DATE_TYPE", "TIME_TYPE", "DATETIME_TYPE", 
			"VECTOR_TYPE", "LINE_COMMENT", "BLOCK_COMMENT", "WS", "ERROR_CHAR"
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
	public String getGrammarFileName() { return "ExpressionEvaluator.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ExpressionEvaluatorParser(TokenStream input) {
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
		public TerminalNode EOF() { return getToken(ExpressionEvaluatorParser.EOF, 0); }
		public List<AssignmentExpressionContext> assignmentExpression() {
			return getRuleContexts(AssignmentExpressionContext.class);
		}
		public AssignmentExpressionContext assignmentExpression(int i) {
			return getRuleContext(AssignmentExpressionContext.class,i);
		}
		public MathInputContext(MathStartContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathInput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathInput(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathInput(this);
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
			setState(77);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(74);
					assignmentExpression();
					}
					} 
				}
				setState(79);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(80);
			mathExpression();
			setState(81);
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
		public TerminalNode EOF() { return getToken(ExpressionEvaluatorParser.EOF, 0); }
		public List<AssignmentExpressionContext> assignmentExpression() {
			return getRuleContexts(AssignmentExpressionContext.class);
		}
		public AssignmentExpressionContext assignmentExpression(int i) {
			return getRuleContext(AssignmentExpressionContext.class,i);
		}
		public AssignmentInputContext(AssignmentStartContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterAssignmentInput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitAssignmentInput(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitAssignmentInput(this);
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
			setState(84); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(83);
				assignmentExpression();
				}
				}
				setState(86); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==LBRACKET || _la==IDENTIFIER );
			setState(88);
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
		public TerminalNode EOF() { return getToken(ExpressionEvaluatorParser.EOF, 0); }
		public List<AssignmentExpressionContext> assignmentExpression() {
			return getRuleContexts(AssignmentExpressionContext.class);
		}
		public AssignmentExpressionContext assignmentExpression(int i) {
			return getRuleContext(AssignmentExpressionContext.class,i);
		}
		public LogicalInputContext(LogicalStartContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalInput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalInput(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalInput(this);
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
			setState(93);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(90);
					assignmentExpression();
					}
					} 
				}
				setState(95);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			setState(96);
			logicalExpression();
			setState(97);
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
		public TerminalNode EQ() { return getToken(ExpressionEvaluatorParser.EQ, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(ExpressionEvaluatorParser.SEMI, 0); }
		public DestructuringAssignmentOperationContext(AssignmentExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDestructuringAssignmentOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDestructuringAssignmentOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDestructuringAssignmentOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentOperationContext extends AssignmentExpressionContext {
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public TerminalNode EQ() { return getToken(ExpressionEvaluatorParser.EQ, 0); }
		public AssignmentValueContext assignmentValue() {
			return getRuleContext(AssignmentValueContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(ExpressionEvaluatorParser.SEMI, 0); }
		public AssignmentOperationContext(AssignmentExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterAssignmentOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitAssignmentOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitAssignmentOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentExpressionContext assignmentExpression() throws RecognitionException {
		AssignmentExpressionContext _localctx = new AssignmentExpressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_assignmentExpression);
		try {
			setState(109);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				_localctx = new AssignmentOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(99);
				match(IDENTIFIER);
				setState(100);
				match(EQ);
				setState(101);
				assignmentValue();
				setState(102);
				match(SEMI);
				}
				break;
			case LBRACKET:
				_localctx = new DestructuringAssignmentOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(104);
				vectorOfVariables();
				setState(105);
				match(EQ);
				setState(106);
				vectorEntity();
				setState(107);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalOrOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalOrOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalOrOperation(this);
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
			setState(111);
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
		public List<TerminalNode> OR() { return getTokens(ExpressionEvaluatorParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(ExpressionEvaluatorParser.OR, i);
		}
		public LogicalOrChainOperationContext(LogicalOrExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalOrChainOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalOrChainOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalOrChainOperation(this);
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
			setState(113);
			logicalAndExpression();
			setState(118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(114);
				match(OR);
				setState(115);
				logicalAndExpression();
				}
				}
				setState(120);
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
		public List<TerminalNode> AND() { return getTokens(ExpressionEvaluatorParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(ExpressionEvaluatorParser.AND, i);
		}
		public LogicalAndChainOperationContext(LogicalAndExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalAndChainOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalAndChainOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalAndChainOperation(this);
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
			setState(121);
			logicalComparisonExpression();
			setState(126);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(122);
				match(AND);
				setState(123);
				logicalComparisonExpression();
				}
				}
				setState(128);
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
	public static class DateTimeNotInOperationContext extends LogicalComparisonExpressionContext {
		public DateTimeEntityContext dateTimeEntity() {
			return getRuleContext(DateTimeEntityContext.class,0);
		}
		public TerminalNode NOT_KW() { return getToken(ExpressionEvaluatorParser.NOT_KW, 0); }
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public DateTimeNotInOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeNotInOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeNotInOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeNotInOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathInOperationContext extends LogicalComparisonExpressionContext {
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public MathInOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathInOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathInOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathInOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateNotInOperationContext extends LogicalComparisonExpressionContext {
		public DateEntityContext dateEntity() {
			return getRuleContext(DateEntityContext.class,0);
		}
		public TerminalNode NOT_KW() { return getToken(ExpressionEvaluatorParser.NOT_KW, 0); }
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public DateNotInOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateNotInOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateNotInOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateNotInOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalNotInOperationContext extends LogicalComparisonExpressionContext {
		public LogicalBitwiseExpressionContext logicalBitwiseExpression() {
			return getRuleContext(LogicalBitwiseExpressionContext.class,0);
		}
		public TerminalNode NOT_KW() { return getToken(ExpressionEvaluatorParser.NOT_KW, 0); }
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public LogicalNotInOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalNotInOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalNotInOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalNotInOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeBetweenOperationContext extends LogicalComparisonExpressionContext {
		public List<DateTimeEntityContext> dateTimeEntity() {
			return getRuleContexts(DateTimeEntityContext.class);
		}
		public DateTimeEntityContext dateTimeEntity(int i) {
			return getRuleContext(DateTimeEntityContext.class,i);
		}
		public TerminalNode BETWEEN() { return getToken(ExpressionEvaluatorParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(ExpressionEvaluatorParser.AND, 0); }
		public DateTimeBetweenOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeBetweenOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeBetweenOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeBetweenOperation(this);
			else return visitor.visitChildren(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeComparisonOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeComparisonOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeComparisonOperation(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeComparisonOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeComparisonOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeComparisonOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RegexMatchOperationContext extends LogicalComparisonExpressionContext {
		public StringConcatExpressionContext stringConcatExpression() {
			return getRuleContext(StringConcatExpressionContext.class,0);
		}
		public TerminalNode REGEX_MATCH() { return getToken(ExpressionEvaluatorParser.REGEX_MATCH, 0); }
		public TerminalNode STRING() { return getToken(ExpressionEvaluatorParser.STRING, 0); }
		public RegexMatchOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterRegexMatchOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitRegexMatchOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitRegexMatchOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NullInOperationContext extends LogicalComparisonExpressionContext {
		public TerminalNode NULL() { return getToken(ExpressionEvaluatorParser.NULL, 0); }
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public NullInOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNullInOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNullInOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNullInOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringNotInOperationContext extends LogicalComparisonExpressionContext {
		public StringConcatExpressionContext stringConcatExpression() {
			return getRuleContext(StringConcatExpressionContext.class,0);
		}
		public TerminalNode NOT_KW() { return getToken(ExpressionEvaluatorParser.NOT_KW, 0); }
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public StringNotInOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringNotInOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringNotInOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringNotInOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeNotBetweenOperationContext extends LogicalComparisonExpressionContext {
		public List<TimeEntityContext> timeEntity() {
			return getRuleContexts(TimeEntityContext.class);
		}
		public TimeEntityContext timeEntity(int i) {
			return getRuleContext(TimeEntityContext.class,i);
		}
		public TerminalNode NOT_KW() { return getToken(ExpressionEvaluatorParser.NOT_KW, 0); }
		public TerminalNode BETWEEN() { return getToken(ExpressionEvaluatorParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(ExpressionEvaluatorParser.AND, 0); }
		public TimeNotBetweenOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeNotBetweenOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeNotBetweenOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeNotBetweenOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeNotBetweenOperationContext extends LogicalComparisonExpressionContext {
		public List<DateTimeEntityContext> dateTimeEntity() {
			return getRuleContexts(DateTimeEntityContext.class);
		}
		public DateTimeEntityContext dateTimeEntity(int i) {
			return getRuleContext(DateTimeEntityContext.class,i);
		}
		public TerminalNode NOT_KW() { return getToken(ExpressionEvaluatorParser.NOT_KW, 0); }
		public TerminalNode BETWEEN() { return getToken(ExpressionEvaluatorParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(ExpressionEvaluatorParser.AND, 0); }
		public DateTimeNotBetweenOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeNotBetweenOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeNotBetweenOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeNotBetweenOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringInOperationContext extends LogicalComparisonExpressionContext {
		public StringConcatExpressionContext stringConcatExpression() {
			return getRuleContext(StringConcatExpressionContext.class,0);
		}
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public StringInOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringInOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringInOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringInOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeInOperationContext extends LogicalComparisonExpressionContext {
		public TimeEntityContext timeEntity() {
			return getRuleContext(TimeEntityContext.class,0);
		}
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public TimeInOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeInOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeInOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeInOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeInOperationContext extends LogicalComparisonExpressionContext {
		public DateTimeEntityContext dateTimeEntity() {
			return getRuleContext(DateTimeEntityContext.class,0);
		}
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public DateTimeInOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeInOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeInOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeInOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateBetweenOperationContext extends LogicalComparisonExpressionContext {
		public List<DateEntityContext> dateEntity() {
			return getRuleContexts(DateEntityContext.class);
		}
		public DateEntityContext dateEntity(int i) {
			return getRuleContext(DateEntityContext.class,i);
		}
		public TerminalNode BETWEEN() { return getToken(ExpressionEvaluatorParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(ExpressionEvaluatorParser.AND, 0); }
		public DateBetweenOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateBetweenOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateBetweenOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateBetweenOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalInOperationContext extends LogicalComparisonExpressionContext {
		public LogicalBitwiseExpressionContext logicalBitwiseExpression() {
			return getRuleContext(LogicalBitwiseExpressionContext.class,0);
		}
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public LogicalInOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalInOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalInOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalInOperation(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalComparisonOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalComparisonOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalComparisonOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringBetweenOperationContext extends LogicalComparisonExpressionContext {
		public List<StringConcatExpressionContext> stringConcatExpression() {
			return getRuleContexts(StringConcatExpressionContext.class);
		}
		public StringConcatExpressionContext stringConcatExpression(int i) {
			return getRuleContext(StringConcatExpressionContext.class,i);
		}
		public TerminalNode BETWEEN() { return getToken(ExpressionEvaluatorParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(ExpressionEvaluatorParser.AND, 0); }
		public StringBetweenOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringBetweenOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringBetweenOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringBetweenOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringComparisonOperationContext extends LogicalComparisonExpressionContext {
		public List<StringConcatExpressionContext> stringConcatExpression() {
			return getRuleContexts(StringConcatExpressionContext.class);
		}
		public StringConcatExpressionContext stringConcatExpression(int i) {
			return getRuleContext(StringConcatExpressionContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public StringComparisonOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringComparisonOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringComparisonOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringComparisonOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringNotBetweenOperationContext extends LogicalComparisonExpressionContext {
		public List<StringConcatExpressionContext> stringConcatExpression() {
			return getRuleContexts(StringConcatExpressionContext.class);
		}
		public StringConcatExpressionContext stringConcatExpression(int i) {
			return getRuleContext(StringConcatExpressionContext.class,i);
		}
		public TerminalNode NOT_KW() { return getToken(ExpressionEvaluatorParser.NOT_KW, 0); }
		public TerminalNode BETWEEN() { return getToken(ExpressionEvaluatorParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(ExpressionEvaluatorParser.AND, 0); }
		public StringNotBetweenOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringNotBetweenOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringNotBetweenOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringNotBetweenOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathNotBetweenOperationContext extends LogicalComparisonExpressionContext {
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public TerminalNode NOT_KW() { return getToken(ExpressionEvaluatorParser.NOT_KW, 0); }
		public TerminalNode BETWEEN() { return getToken(ExpressionEvaluatorParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(ExpressionEvaluatorParser.AND, 0); }
		public MathNotBetweenOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathNotBetweenOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathNotBetweenOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathNotBetweenOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateInOperationContext extends LogicalComparisonExpressionContext {
		public DateEntityContext dateEntity() {
			return getRuleContext(DateEntityContext.class,0);
		}
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public DateInOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateInOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateInOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateInOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateNotBetweenOperationContext extends LogicalComparisonExpressionContext {
		public List<DateEntityContext> dateEntity() {
			return getRuleContexts(DateEntityContext.class);
		}
		public DateEntityContext dateEntity(int i) {
			return getRuleContext(DateEntityContext.class,i);
		}
		public TerminalNode NOT_KW() { return getToken(ExpressionEvaluatorParser.NOT_KW, 0); }
		public TerminalNode BETWEEN() { return getToken(ExpressionEvaluatorParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(ExpressionEvaluatorParser.AND, 0); }
		public DateNotBetweenOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateNotBetweenOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateNotBetweenOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateNotBetweenOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathBetweenOperationContext extends LogicalComparisonExpressionContext {
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public TerminalNode BETWEEN() { return getToken(ExpressionEvaluatorParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(ExpressionEvaluatorParser.AND, 0); }
		public MathBetweenOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathBetweenOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathBetweenOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathBetweenOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeBetweenOperationContext extends LogicalComparisonExpressionContext {
		public List<TimeEntityContext> timeEntity() {
			return getRuleContexts(TimeEntityContext.class);
		}
		public TimeEntityContext timeEntity(int i) {
			return getRuleContext(TimeEntityContext.class,i);
		}
		public TerminalNode BETWEEN() { return getToken(ExpressionEvaluatorParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(ExpressionEvaluatorParser.AND, 0); }
		public TimeBetweenOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeBetweenOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeBetweenOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeBetweenOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RegexNotMatchOperationContext extends LogicalComparisonExpressionContext {
		public StringConcatExpressionContext stringConcatExpression() {
			return getRuleContext(StringConcatExpressionContext.class,0);
		}
		public TerminalNode REGEX_NOT_MATCH() { return getToken(ExpressionEvaluatorParser.REGEX_NOT_MATCH, 0); }
		public TerminalNode STRING() { return getToken(ExpressionEvaluatorParser.STRING, 0); }
		public RegexNotMatchOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterRegexNotMatchOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitRegexNotMatchOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitRegexNotMatchOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NullNotInOperationContext extends LogicalComparisonExpressionContext {
		public TerminalNode NULL() { return getToken(ExpressionEvaluatorParser.NULL, 0); }
		public TerminalNode NOT_KW() { return getToken(ExpressionEvaluatorParser.NOT_KW, 0); }
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public NullNotInOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNullNotInOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNullNotInOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNullNotInOperation(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateComparisonOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateComparisonOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateComparisonOperation(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathComparisonOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathComparisonOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathComparisonOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeNotInOperationContext extends LogicalComparisonExpressionContext {
		public TimeEntityContext timeEntity() {
			return getRuleContext(TimeEntityContext.class,0);
		}
		public TerminalNode NOT_KW() { return getToken(ExpressionEvaluatorParser.NOT_KW, 0); }
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public TimeNotInOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeNotInOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeNotInOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeNotInOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathNotInOperationContext extends LogicalComparisonExpressionContext {
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode NOT_KW() { return getToken(ExpressionEvaluatorParser.NOT_KW, 0); }
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public MathNotInOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathNotInOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathNotInOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathNotInOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalComparisonExpressionContext logicalComparisonExpression() throws RecognitionException {
		LogicalComparisonExpressionContext _localctx = new LogicalComparisonExpressionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_logicalComparisonExpression);
		int _la;
		try {
			setState(289);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				_localctx = new LogicalComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(129);
				logicalBitwiseExpression();
				setState(133);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 8455716864L) != 0)) {
					{
					setState(130);
					comparisonOperator();
					setState(131);
					logicalBitwiseExpression();
					}
				}

				}
				break;
			case 2:
				_localctx = new MathComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(135);
				mathExpression();
				setState(136);
				comparisonOperator();
				setState(137);
				mathExpression();
				}
				break;
			case 3:
				_localctx = new StringComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(139);
				stringConcatExpression();
				setState(140);
				comparisonOperator();
				setState(141);
				stringConcatExpression();
				}
				break;
			case 4:
				_localctx = new DateComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(143);
				dateEntity();
				setState(144);
				comparisonOperator();
				setState(145);
				dateEntity();
				}
				break;
			case 5:
				_localctx = new TimeComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(147);
				timeEntity();
				setState(148);
				comparisonOperator();
				setState(149);
				timeEntity();
				}
				break;
			case 6:
				_localctx = new DateTimeComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(151);
				dateTimeEntity();
				setState(152);
				comparisonOperator();
				setState(153);
				dateTimeEntity();
				}
				break;
			case 7:
				_localctx = new RegexMatchOperationContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(155);
				stringConcatExpression();
				setState(156);
				match(REGEX_MATCH);
				setState(157);
				match(STRING);
				}
				break;
			case 8:
				_localctx = new RegexNotMatchOperationContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(159);
				stringConcatExpression();
				setState(160);
				match(REGEX_NOT_MATCH);
				setState(161);
				match(STRING);
				}
				break;
			case 9:
				_localctx = new MathInOperationContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(163);
				mathExpression();
				setState(164);
				match(IN);
				setState(165);
				vectorEntity();
				}
				break;
			case 10:
				_localctx = new StringInOperationContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(167);
				stringConcatExpression();
				setState(168);
				match(IN);
				setState(169);
				vectorEntity();
				}
				break;
			case 11:
				_localctx = new DateInOperationContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(171);
				dateEntity();
				setState(172);
				match(IN);
				setState(173);
				vectorEntity();
				}
				break;
			case 12:
				_localctx = new TimeInOperationContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(175);
				timeEntity();
				setState(176);
				match(IN);
				setState(177);
				vectorEntity();
				}
				break;
			case 13:
				_localctx = new DateTimeInOperationContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(179);
				dateTimeEntity();
				setState(180);
				match(IN);
				setState(181);
				vectorEntity();
				}
				break;
			case 14:
				_localctx = new LogicalInOperationContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(183);
				logicalBitwiseExpression();
				setState(184);
				match(IN);
				setState(185);
				vectorEntity();
				}
				break;
			case 15:
				_localctx = new NullInOperationContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(187);
				match(NULL);
				setState(188);
				match(IN);
				setState(189);
				vectorEntity();
				}
				break;
			case 16:
				_localctx = new MathNotInOperationContext(_localctx);
				enterOuterAlt(_localctx, 16);
				{
				setState(190);
				mathExpression();
				setState(191);
				match(NOT_KW);
				setState(192);
				match(IN);
				setState(193);
				vectorEntity();
				}
				break;
			case 17:
				_localctx = new StringNotInOperationContext(_localctx);
				enterOuterAlt(_localctx, 17);
				{
				setState(195);
				stringConcatExpression();
				setState(196);
				match(NOT_KW);
				setState(197);
				match(IN);
				setState(198);
				vectorEntity();
				}
				break;
			case 18:
				_localctx = new DateNotInOperationContext(_localctx);
				enterOuterAlt(_localctx, 18);
				{
				setState(200);
				dateEntity();
				setState(201);
				match(NOT_KW);
				setState(202);
				match(IN);
				setState(203);
				vectorEntity();
				}
				break;
			case 19:
				_localctx = new TimeNotInOperationContext(_localctx);
				enterOuterAlt(_localctx, 19);
				{
				setState(205);
				timeEntity();
				setState(206);
				match(NOT_KW);
				setState(207);
				match(IN);
				setState(208);
				vectorEntity();
				}
				break;
			case 20:
				_localctx = new DateTimeNotInOperationContext(_localctx);
				enterOuterAlt(_localctx, 20);
				{
				setState(210);
				dateTimeEntity();
				setState(211);
				match(NOT_KW);
				setState(212);
				match(IN);
				setState(213);
				vectorEntity();
				}
				break;
			case 21:
				_localctx = new LogicalNotInOperationContext(_localctx);
				enterOuterAlt(_localctx, 21);
				{
				setState(215);
				logicalBitwiseExpression();
				setState(216);
				match(NOT_KW);
				setState(217);
				match(IN);
				setState(218);
				vectorEntity();
				}
				break;
			case 22:
				_localctx = new NullNotInOperationContext(_localctx);
				enterOuterAlt(_localctx, 22);
				{
				setState(220);
				match(NULL);
				setState(221);
				match(NOT_KW);
				setState(222);
				match(IN);
				setState(223);
				vectorEntity();
				}
				break;
			case 23:
				_localctx = new MathBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 23);
				{
				setState(224);
				mathExpression();
				setState(225);
				match(BETWEEN);
				setState(226);
				mathExpression();
				setState(227);
				match(AND);
				setState(228);
				mathExpression();
				}
				break;
			case 24:
				_localctx = new StringBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 24);
				{
				setState(230);
				stringConcatExpression();
				setState(231);
				match(BETWEEN);
				setState(232);
				stringConcatExpression();
				setState(233);
				match(AND);
				setState(234);
				stringConcatExpression();
				}
				break;
			case 25:
				_localctx = new DateBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 25);
				{
				setState(236);
				dateEntity();
				setState(237);
				match(BETWEEN);
				setState(238);
				dateEntity();
				setState(239);
				match(AND);
				setState(240);
				dateEntity();
				}
				break;
			case 26:
				_localctx = new TimeBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 26);
				{
				setState(242);
				timeEntity();
				setState(243);
				match(BETWEEN);
				setState(244);
				timeEntity();
				setState(245);
				match(AND);
				setState(246);
				timeEntity();
				}
				break;
			case 27:
				_localctx = new DateTimeBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 27);
				{
				setState(248);
				dateTimeEntity();
				setState(249);
				match(BETWEEN);
				setState(250);
				dateTimeEntity();
				setState(251);
				match(AND);
				setState(252);
				dateTimeEntity();
				}
				break;
			case 28:
				_localctx = new MathNotBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 28);
				{
				setState(254);
				mathExpression();
				setState(255);
				match(NOT_KW);
				setState(256);
				match(BETWEEN);
				setState(257);
				mathExpression();
				setState(258);
				match(AND);
				setState(259);
				mathExpression();
				}
				break;
			case 29:
				_localctx = new StringNotBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 29);
				{
				setState(261);
				stringConcatExpression();
				setState(262);
				match(NOT_KW);
				setState(263);
				match(BETWEEN);
				setState(264);
				stringConcatExpression();
				setState(265);
				match(AND);
				setState(266);
				stringConcatExpression();
				}
				break;
			case 30:
				_localctx = new DateNotBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 30);
				{
				setState(268);
				dateEntity();
				setState(269);
				match(NOT_KW);
				setState(270);
				match(BETWEEN);
				setState(271);
				dateEntity();
				setState(272);
				match(AND);
				setState(273);
				dateEntity();
				}
				break;
			case 31:
				_localctx = new TimeNotBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 31);
				{
				setState(275);
				timeEntity();
				setState(276);
				match(NOT_KW);
				setState(277);
				match(BETWEEN);
				setState(278);
				timeEntity();
				setState(279);
				match(AND);
				setState(280);
				timeEntity();
				}
				break;
			case 32:
				_localctx = new DateTimeNotBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 32);
				{
				setState(282);
				dateTimeEntity();
				setState(283);
				match(NOT_KW);
				setState(284);
				match(BETWEEN);
				setState(285);
				dateTimeEntity();
				setState(286);
				match(AND);
				setState(287);
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
		public List<TerminalNode> NAND() { return getTokens(ExpressionEvaluatorParser.NAND); }
		public TerminalNode NAND(int i) {
			return getToken(ExpressionEvaluatorParser.NAND, i);
		}
		public List<TerminalNode> NOR() { return getTokens(ExpressionEvaluatorParser.NOR); }
		public TerminalNode NOR(int i) {
			return getToken(ExpressionEvaluatorParser.NOR, i);
		}
		public List<TerminalNode> XOR() { return getTokens(ExpressionEvaluatorParser.XOR); }
		public TerminalNode XOR(int i) {
			return getToken(ExpressionEvaluatorParser.XOR, i);
		}
		public List<TerminalNode> XNOR() { return getTokens(ExpressionEvaluatorParser.XNOR); }
		public TerminalNode XNOR(int i) {
			return getToken(ExpressionEvaluatorParser.XNOR, i);
		}
		public LogicalBitwiseOperationContext(LogicalBitwiseExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalBitwiseOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalBitwiseOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalBitwiseOperation(this);
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
			setState(291);
			logicalNotExpression();
			setState(296);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 7680L) != 0)) {
				{
				{
				setState(292);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 7680L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(293);
				logicalNotExpression();
				}
				}
				setState(298);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalPrimaryOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalPrimaryOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalPrimaryOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalNotOperationContext extends LogicalNotExpressionContext {
		public LogicalNotExpressionContext logicalNotExpression() {
			return getRuleContext(LogicalNotExpressionContext.class,0);
		}
		public TerminalNode NOT() { return getToken(ExpressionEvaluatorParser.NOT, 0); }
		public TerminalNode EXCLAMATION() { return getToken(ExpressionEvaluatorParser.EXCLAMATION, 0); }
		public LogicalNotOperationContext(LogicalNotExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalNotOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalNotOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalNotOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalNotExpressionContext logicalNotExpression() throws RecognitionException {
		LogicalNotExpressionContext _localctx = new LogicalNotExpressionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_logicalNotExpression);
		int _la;
		try {
			setState(302);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXCLAMATION:
			case NOT:
				_localctx = new LogicalNotOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(299);
				_la = _input.LA(1);
				if ( !(_la==EXCLAMATION || _la==NOT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(300);
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
				setState(301);
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
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public LogicalExpressionParenthesisOperationContext(LogicalPrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalExpressionParenthesisOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalExpressionParenthesisOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalExpressionParenthesisOperation(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalEntityOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalEntityOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalEntityOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalPrimaryContext logicalPrimary() throws RecognitionException {
		LogicalPrimaryContext _localctx = new LogicalPrimaryContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_logicalPrimary);
		try {
			setState(309);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				_localctx = new LogicalExpressionParenthesisOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(304);
				match(LPAREN);
				setState(305);
				logicalExpression();
				setState(306);
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
				setState(308);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterSumOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitSumOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitSumOperation(this);
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
			setState(311);
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
		public List<TerminalNode> PLUS() { return getTokens(ExpressionEvaluatorParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(ExpressionEvaluatorParser.PLUS, i);
		}
		public List<TerminalNode> MINUS() { return getTokens(ExpressionEvaluatorParser.MINUS); }
		public TerminalNode MINUS(int i) {
			return getToken(ExpressionEvaluatorParser.MINUS, i);
		}
		public AdditiveOperationContext(SumExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterAdditiveOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitAdditiveOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitAdditiveOperation(this);
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
			setState(313);
			multiplicationExpression();
			setState(318);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS || _la==MINUS) {
				{
				{
				setState(314);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(315);
				multiplicationExpression();
				}
				}
				setState(320);
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
		public List<TerminalNode> MULT() { return getTokens(ExpressionEvaluatorParser.MULT); }
		public TerminalNode MULT(int i) {
			return getToken(ExpressionEvaluatorParser.MULT, i);
		}
		public List<TerminalNode> DIV() { return getTokens(ExpressionEvaluatorParser.DIV); }
		public TerminalNode DIV(int i) {
			return getToken(ExpressionEvaluatorParser.DIV, i);
		}
		public List<TerminalNode> MODULO() { return getTokens(ExpressionEvaluatorParser.MODULO); }
		public TerminalNode MODULO(int i) {
			return getToken(ExpressionEvaluatorParser.MODULO, i);
		}
		public MultiplicativeOperationContext(MultiplicationExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMultiplicativeOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMultiplicativeOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMultiplicativeOperation(this);
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
			setState(321);
			unaryExpression();
			setState(326);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1146880L) != 0)) {
				{
				{
				setState(322);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1146880L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(323);
				unaryExpression();
				}
				}
				setState(328);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterRootExpressionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitRootExpressionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitRootExpressionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UnaryMinusOperationContext extends UnaryExpressionContext {
		public TerminalNode MINUS() { return getToken(ExpressionEvaluatorParser.MINUS, 0); }
		public UnaryExpressionContext unaryExpression() {
			return getRuleContext(UnaryExpressionContext.class,0);
		}
		public UnaryMinusOperationContext(UnaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterUnaryMinusOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitUnaryMinusOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitUnaryMinusOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryExpressionContext unaryExpression() throws RecognitionException {
		UnaryExpressionContext _localctx = new UnaryExpressionContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_unaryExpression);
		try {
			setState(332);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MINUS:
				_localctx = new UnaryMinusOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(329);
				match(MINUS);
				setState(330);
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
				setState(331);
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
		public List<TerminalNode> ROOT() { return getTokens(ExpressionEvaluatorParser.ROOT); }
		public TerminalNode ROOT(int i) {
			return getToken(ExpressionEvaluatorParser.ROOT, i);
		}
		public RootChainOperationContext(RootExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterRootChainOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitRootChainOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitRootChainOperation(this);
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
			setState(334);
			exponentiationExpression();
			setState(339);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(335);
					match(ROOT);
					setState(336);
					exponentiationExpression();
					}
					} 
				}
				setState(341);
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
		public TerminalNode EXPONENTIATION() { return getToken(ExpressionEvaluatorParser.EXPONENTIATION, 0); }
		public UnaryExpressionContext unaryExpression() {
			return getRuleContext(UnaryExpressionContext.class,0);
		}
		public ExponentiationOperationContext(ExponentiationExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterExponentiationOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitExponentiationOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitExponentiationOperation(this);
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
			setState(342);
			postfixExpression();
			setState(345);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXPONENTIATION) {
				{
				setState(343);
				match(EXPONENTIATION);
				setState(344);
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
		public List<TerminalNode> PERCENT() { return getTokens(ExpressionEvaluatorParser.PERCENT); }
		public TerminalNode PERCENT(int i) {
			return getToken(ExpressionEvaluatorParser.PERCENT, i);
		}
		public List<TerminalNode> EXCLAMATION() { return getTokens(ExpressionEvaluatorParser.EXCLAMATION); }
		public TerminalNode EXCLAMATION(int i) {
			return getToken(ExpressionEvaluatorParser.EXCLAMATION, i);
		}
		public PostfixOperationContext(PostfixExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterPostfixOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitPostfixOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitPostfixOperation(this);
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
			setState(347);
			primaryMathExpression();
			setState(351);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PERCENT || _la==EXCLAMATION) {
				{
				{
				setState(348);
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
				setState(353);
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
		public TerminalNode SQRT() { return getToken(ExpressionEvaluatorParser.SQRT, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public SquareRootOperationContext(PrimaryMathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterSquareRootOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitSquareRootOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitSquareRootOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathExpressionParenthesisOperationContext extends PrimaryMathExpressionContext {
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public MathExpressionParenthesisOperationContext(PrimaryMathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathExpressionParenthesisOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathExpressionParenthesisOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathExpressionParenthesisOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ModulusOperationContext extends PrimaryMathExpressionContext {
		public List<TerminalNode> MODULUS() { return getTokens(ExpressionEvaluatorParser.MODULUS); }
		public TerminalNode MODULUS(int i) {
			return getToken(ExpressionEvaluatorParser.MODULUS, i);
		}
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public ModulusOperationContext(PrimaryMathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterModulusOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitModulusOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitModulusOperation(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNumericEntityOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNumericEntityOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNumericEntityOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryMathExpressionContext primaryMathExpression() throws RecognitionException {
		PrimaryMathExpressionContext _localctx = new PrimaryMathExpressionContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_primaryMathExpression);
		try {
			setState(368);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				_localctx = new MathExpressionParenthesisOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(354);
				match(LPAREN);
				setState(355);
				mathExpression();
				setState(356);
				match(RPAREN);
				}
				break;
			case SQRT:
				_localctx = new SquareRootOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(358);
				match(SQRT);
				setState(359);
				match(LPAREN);
				setState(360);
				mathExpression();
				setState(361);
				match(RPAREN);
				}
				break;
			case MODULUS:
				_localctx = new ModulusOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(363);
				match(MODULUS);
				setState(364);
				mathExpression();
				setState(365);
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
				setState(367);
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
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public List<AllEntityTypesContext> allEntityTypes() {
			return getRuleContexts(AllEntityTypesContext.class);
		}
		public AllEntityTypesContext allEntityTypes(int i) {
			return getRuleContext(AllEntityTypesContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorParser.SEMI, i);
		}
		public FunctionCallOperationContext(FunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterFunctionCallOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitFunctionCallOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitFunctionCallOperation(this);
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
			setState(370);
			match(IDENTIFIER);
			setState(371);
			match(LPAREN);
			setState(380);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -164378147915800510L) != 0) || _la==DATETIME_TYPE || _la==VECTOR_TYPE) {
				{
				setState(372);
				allEntityTypes();
				setState(377);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA || _la==SEMI) {
					{
					{
					setState(373);
					_la = _input.LA(1);
					if ( !(_la==COMMA || _la==SEMI) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(374);
					allEntityTypes();
					}
					}
					setState(379);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(382);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterFunctionReferenceTarget(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitFunctionReferenceTarget(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitFunctionReferenceTarget(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierReferenceTargetContext extends ReferenceTargetContext {
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public List<MemberChainContext> memberChain() {
			return getRuleContexts(MemberChainContext.class);
		}
		public MemberChainContext memberChain(int i) {
			return getRuleContext(MemberChainContext.class,i);
		}
		public IdentifierReferenceTargetContext(ReferenceTargetContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterIdentifierReferenceTarget(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitIdentifierReferenceTarget(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitIdentifierReferenceTarget(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReferenceTargetContext referenceTarget() throws RecognitionException {
		ReferenceTargetContext _localctx = new ReferenceTargetContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_referenceTarget);
		int _la;
		try {
			setState(392);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				_localctx = new FunctionReferenceTargetContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(384);
				function();
				}
				break;
			case 2:
				_localctx = new IdentifierReferenceTargetContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(385);
				match(IDENTIFIER);
				setState(389);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==PERIOD || _la==SAFE_NAV) {
					{
					{
					setState(386);
					memberChain();
					}
					}
					setState(391);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
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
	public static class MemberChainContext extends ParserRuleContext {
		public MemberChainContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_memberChain; }
	 
		public MemberChainContext() { }
		public void copyFrom(MemberChainContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SafeMethodCallAccessContext extends MemberChainContext {
		public TerminalNode SAFE_NAV() { return getToken(ExpressionEvaluatorParser.SAFE_NAV, 0); }
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public List<AllEntityTypesContext> allEntityTypes() {
			return getRuleContexts(AllEntityTypesContext.class);
		}
		public AllEntityTypesContext allEntityTypes(int i) {
			return getRuleContext(AllEntityTypesContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public SafeMethodCallAccessContext(MemberChainContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterSafeMethodCallAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitSafeMethodCallAccess(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitSafeMethodCallAccess(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MethodCallAccessContext extends MemberChainContext {
		public TerminalNode PERIOD() { return getToken(ExpressionEvaluatorParser.PERIOD, 0); }
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public List<AllEntityTypesContext> allEntityTypes() {
			return getRuleContexts(AllEntityTypesContext.class);
		}
		public AllEntityTypesContext allEntityTypes(int i) {
			return getRuleContext(AllEntityTypesContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public MethodCallAccessContext(MemberChainContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMethodCallAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMethodCallAccess(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMethodCallAccess(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PropertyAccessContext extends MemberChainContext {
		public TerminalNode PERIOD() { return getToken(ExpressionEvaluatorParser.PERIOD, 0); }
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public PropertyAccessContext(MemberChainContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterPropertyAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitPropertyAccess(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitPropertyAccess(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SafePropertyAccessContext extends MemberChainContext {
		public TerminalNode SAFE_NAV() { return getToken(ExpressionEvaluatorParser.SAFE_NAV, 0); }
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public SafePropertyAccessContext(MemberChainContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterSafePropertyAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitSafePropertyAccess(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitSafePropertyAccess(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MemberChainContext memberChain() throws RecognitionException {
		MemberChainContext _localctx = new MemberChainContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_memberChain);
		int _la;
		try {
			setState(426);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				_localctx = new PropertyAccessContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(394);
				match(PERIOD);
				setState(395);
				match(IDENTIFIER);
				}
				break;
			case 2:
				_localctx = new SafePropertyAccessContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(396);
				match(SAFE_NAV);
				setState(397);
				match(IDENTIFIER);
				}
				break;
			case 3:
				_localctx = new MethodCallAccessContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(398);
				match(PERIOD);
				setState(399);
				match(IDENTIFIER);
				setState(400);
				match(LPAREN);
				setState(409);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -164378147915800510L) != 0) || _la==DATETIME_TYPE || _la==VECTOR_TYPE) {
					{
					setState(401);
					allEntityTypes();
					setState(406);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(402);
						match(COMMA);
						setState(403);
						allEntityTypes();
						}
						}
						setState(408);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(411);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new SafeMethodCallAccessContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(412);
				match(SAFE_NAV);
				setState(413);
				match(IDENTIFIER);
				setState(414);
				match(LPAREN);
				setState(423);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -164378147915800510L) != 0) || _la==DATETIME_TYPE || _la==VECTOR_TYPE) {
					{
					setState(415);
					allEntityTypes();
					setState(420);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(416);
						match(COMMA);
						setState(417);
						allEntityTypes();
						}
						}
						setState(422);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(425);
				match(RPAREN);
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
		public TerminalNode EQ() { return getToken(ExpressionEvaluatorParser.EQ, 0); }
		public EqualOperatorContext(ComparisonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterEqualOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitEqualOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitEqualOperator(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NotEqualOperatorContext extends ComparisonOperatorContext {
		public TerminalNode NEQ() { return getToken(ExpressionEvaluatorParser.NEQ, 0); }
		public NotEqualOperatorContext(ComparisonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNotEqualOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNotEqualOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNotEqualOperator(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LessThanOperatorContext extends ComparisonOperatorContext {
		public TerminalNode LT() { return getToken(ExpressionEvaluatorParser.LT, 0); }
		public LessThanOperatorContext(ComparisonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLessThanOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLessThanOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLessThanOperator(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GreaterThanOperatorContext extends ComparisonOperatorContext {
		public TerminalNode GT() { return getToken(ExpressionEvaluatorParser.GT, 0); }
		public GreaterThanOperatorContext(ComparisonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterGreaterThanOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitGreaterThanOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitGreaterThanOperator(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LessThanOrEqualOperatorContext extends ComparisonOperatorContext {
		public TerminalNode LE() { return getToken(ExpressionEvaluatorParser.LE, 0); }
		public LessThanOrEqualOperatorContext(ComparisonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLessThanOrEqualOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLessThanOrEqualOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLessThanOrEqualOperator(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GreaterThanOrEqualOperatorContext extends ComparisonOperatorContext {
		public TerminalNode GE() { return getToken(ExpressionEvaluatorParser.GE, 0); }
		public GreaterThanOrEqualOperatorContext(ComparisonOperatorContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterGreaterThanOrEqualOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitGreaterThanOrEqualOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitGreaterThanOrEqualOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_comparisonOperator);
		try {
			setState(434);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case GT:
				_localctx = new GreaterThanOperatorContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(428);
				match(GT);
				}
				break;
			case GE:
				_localctx = new GreaterThanOrEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(429);
				match(GE);
				}
				break;
			case LT:
				_localctx = new LessThanOperatorContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(430);
				match(LT);
				}
				break;
			case LE:
				_localctx = new LessThanOrEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(431);
				match(LE);
				}
				break;
			case EQ:
				_localctx = new EqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(432);
				match(EQ);
				}
				break;
			case NEQ:
				_localctx = new NotEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(433);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeEntityType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringEntityTypeContext extends AllEntityTypesContext {
		public StringConcatExpressionContext stringConcatExpression() {
			return getRuleContext(StringConcatExpressionContext.class,0);
		}
		public StringEntityTypeContext(AllEntityTypesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringEntityType(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateEntityType(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeEntityType(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterVectorEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitVectorEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitVectorEntityType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NullEntityTypeContext extends AllEntityTypesContext {
		public TerminalNode NULL() { return getToken(ExpressionEvaluatorParser.NULL, 0); }
		public NullEntityTypeContext(AllEntityTypesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNullEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNullEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNullEntityType(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathEntityType(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalEntityType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AllEntityTypesContext allEntityTypes() throws RecognitionException {
		AllEntityTypesContext _localctx = new AllEntityTypesContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_allEntityTypes);
		try {
			setState(444);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				_localctx = new MathEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(436);
				mathExpression();
				}
				break;
			case 2:
				_localctx = new LogicalEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(437);
				logicalExpression();
				}
				break;
			case 3:
				_localctx = new DateEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(438);
				dateEntity();
				}
				break;
			case 4:
				_localctx = new TimeEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(439);
				timeEntity();
				}
				break;
			case 5:
				_localctx = new DateTimeEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(440);
				dateTimeEntity();
				}
				break;
			case 6:
				_localctx = new StringEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(441);
				stringConcatExpression();
				}
				break;
			case 7:
				_localctx = new VectorEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(442);
				vectorEntity();
				}
				break;
			case 8:
				_localctx = new NullEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(443);
				match(NULL);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeAssignmentValue(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterGenericAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitGenericAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitGenericAssignmentValue(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateAssignmentValue(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeAssignmentValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringAssignmentValueContext extends AssignmentValueContext {
		public StringConcatExpressionContext stringConcatExpression() {
			return getRuleContext(StringConcatExpressionContext.class,0);
		}
		public StringAssignmentValueContext(AssignmentValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringAssignmentValue(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalAssignmentValue(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterVectorAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitVectorAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitVectorAssignmentValue(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathAssignmentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathAssignmentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathAssignmentValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentValueContext assignmentValue() throws RecognitionException {
		AssignmentValueContext _localctx = new AssignmentValueContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_assignmentValue);
		try {
			setState(454);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
			case 1:
				_localctx = new GenericAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(446);
				genericEntity();
				}
				break;
			case 2:
				_localctx = new MathAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(447);
				mathExpression();
				}
				break;
			case 3:
				_localctx = new LogicalAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(448);
				logicalExpression();
				}
				break;
			case 4:
				_localctx = new DateAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(449);
				dateEntity();
				}
				break;
			case 5:
				_localctx = new TimeAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(450);
				timeEntity();
				}
				break;
			case 6:
				_localctx = new DateTimeAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(451);
				dateTimeEntity();
				}
				break;
			case 7:
				_localctx = new StringAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(452);
				stringConcatExpression();
				}
				break;
			case 8:
				_localctx = new VectorAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(453);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterCastExpressionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitCastExpressionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitCastExpressionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GenericFunctionDecisionExpressionContext extends GenericEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
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
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorParser.SEMI, i);
		}
		public GenericFunctionDecisionExpressionContext(GenericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterGenericFunctionDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitGenericFunctionDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitGenericFunctionDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GenericDecisionExpressionContext extends GenericEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorParser.THEN, i);
		}
		public List<GenericEntityContext> genericEntity() {
			return getRuleContexts(GenericEntityContext.class);
		}
		public GenericEntityContext genericEntity(int i) {
			return getRuleContext(GenericEntityContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorParser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorParser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorParser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorParser.ELSEIF, i);
		}
		public GenericDecisionExpressionContext(GenericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterGenericDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitGenericDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitGenericDecisionExpression(this);
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
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterReferenceTargetOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitReferenceTargetOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitReferenceTargetOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericEntityContext genericEntity() throws RecognitionException {
		GenericEntityContext _localctx = new GenericEntityContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_genericEntity);
		int _la;
		try {
			int _alt;
			setState(495);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				_localctx = new GenericDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(456);
				match(IF);
				setState(457);
				logicalExpression();
				setState(458);
				match(THEN);
				setState(459);
				genericEntity();
				setState(467);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(460);
					match(ELSEIF);
					setState(461);
					logicalExpression();
					setState(462);
					match(THEN);
					setState(463);
					genericEntity();
					}
					}
					setState(469);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(470);
				match(ELSE);
				setState(471);
				genericEntity();
				setState(472);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new GenericFunctionDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(474);
				match(IF);
				setState(475);
				match(LPAREN);
				setState(476);
				logicalExpression();
				setState(477);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(478);
				genericEntity();
				setState(486);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(479);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(480);
						logicalExpression();
						setState(481);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(482);
						genericEntity();
						}
						} 
					}
					setState(488);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
				}
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
				genericEntity();
				setState(491);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new CastExpressionOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(493);
				castExpression();
				}
				break;
			case 4:
				_localctx = new ReferenceTargetOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(494);
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
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public GenericEntityContext genericEntity() {
			return getRuleContext(GenericEntityContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public TypeCastOperationContext(CastExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTypeCastOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTypeCastOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTypeCastOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CastExpressionContext castExpression() throws RecognitionException {
		CastExpressionContext _localctx = new CastExpressionContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_castExpression);
		try {
			_localctx = new TypeCastOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(497);
			typeHint();
			setState(498);
			match(LPAREN);
			setState(499);
			genericEntity();
			setState(500);
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
		public TerminalNode VECTOR_TYPE() { return getToken(ExpressionEvaluatorParser.VECTOR_TYPE, 0); }
		public VectorTypeHintContext(TypeHintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterVectorTypeHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitVectorTypeHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitVectorTypeHint(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringTypeHintContext extends TypeHintContext {
		public TerminalNode STRING_TYPE() { return getToken(ExpressionEvaluatorParser.STRING_TYPE, 0); }
		public StringTypeHintContext(TypeHintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringTypeHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringTypeHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringTypeHint(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTypeHintContext extends TypeHintContext {
		public TerminalNode DATE_TYPE() { return getToken(ExpressionEvaluatorParser.DATE_TYPE, 0); }
		public DateTypeHintContext(TypeHintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTypeHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTypeHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTypeHint(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeTypeHintContext extends TypeHintContext {
		public TerminalNode TIME_TYPE() { return getToken(ExpressionEvaluatorParser.TIME_TYPE, 0); }
		public TimeTypeHintContext(TypeHintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeTypeHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeTypeHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeTypeHint(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BooleanTypeHintContext extends TypeHintContext {
		public TerminalNode BOOLEAN_TYPE() { return getToken(ExpressionEvaluatorParser.BOOLEAN_TYPE, 0); }
		public BooleanTypeHintContext(TypeHintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterBooleanTypeHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitBooleanTypeHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitBooleanTypeHint(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumberTypeHintContext extends TypeHintContext {
		public TerminalNode NUMBER_TYPE() { return getToken(ExpressionEvaluatorParser.NUMBER_TYPE, 0); }
		public NumberTypeHintContext(TypeHintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNumberTypeHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNumberTypeHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNumberTypeHint(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeTypeHintContext extends TypeHintContext {
		public TerminalNode DATETIME_TYPE() { return getToken(ExpressionEvaluatorParser.DATETIME_TYPE, 0); }
		public DateTimeTypeHintContext(TypeHintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeTypeHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeTypeHint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeTypeHint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeHintContext typeHint() throws RecognitionException {
		TypeHintContext _localctx = new TypeHintContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_typeHint);
		try {
			setState(509);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BOOLEAN_TYPE:
				_localctx = new BooleanTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(502);
				match(BOOLEAN_TYPE);
				}
				break;
			case NUMBER_TYPE:
				_localctx = new NumberTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(503);
				match(NUMBER_TYPE);
				}
				break;
			case STRING_TYPE:
				_localctx = new StringTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(504);
				match(STRING_TYPE);
				}
				break;
			case DATE_TYPE:
				_localctx = new DateTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(505);
				match(DATE_TYPE);
				}
				break;
			case TIME_TYPE:
				_localctx = new TimeTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(506);
				match(TIME_TYPE);
				}
				break;
			case DATETIME_TYPE:
				_localctx = new DateTimeTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(507);
				match(DATETIME_TYPE);
				}
				break;
			case VECTOR_TYPE:
				_localctx = new VectorTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(508);
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
		public TerminalNode TRUE() { return getToken(ExpressionEvaluatorParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(ExpressionEvaluatorParser.FALSE, 0); }
		public LogicalConstantOperationContext(LogicalEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalConstantOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalConstantOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalConstantOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalReferenceOperationContext extends LogicalEntityContext {
		public ReferenceTargetContext referenceTarget() {
			return getRuleContext(ReferenceTargetContext.class,0);
		}
		public TerminalNode BOOLEAN_TYPE() { return getToken(ExpressionEvaluatorParser.BOOLEAN_TYPE, 0); }
		public TerminalNode NULLCOALESCE() { return getToken(ExpressionEvaluatorParser.NULLCOALESCE, 0); }
		public LogicalEntityContext logicalEntity() {
			return getRuleContext(LogicalEntityContext.class,0);
		}
		public LogicalReferenceOperationContext(LogicalEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalReferenceOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalReferenceOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalReferenceOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalFunctionDecisionOperationContext extends LogicalEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorParser.SEMI, i);
		}
		public LogicalFunctionDecisionOperationContext(LogicalEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalFunctionDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalFunctionDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalFunctionDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalDecisionOperationContext extends LogicalEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorParser.THEN, i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorParser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorParser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorParser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorParser.ELSEIF, i);
		}
		public LogicalDecisionOperationContext(LogicalEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalEntityContext logicalEntity() throws RecognitionException {
		LogicalEntityContext _localctx = new LogicalEntityContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_logicalEntity);
		int _la;
		try {
			int _alt;
			setState(557);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				_localctx = new LogicalConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(511);
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
				setState(512);
				match(IF);
				setState(513);
				logicalExpression();
				setState(514);
				match(THEN);
				setState(515);
				logicalExpression();
				setState(523);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(516);
					match(ELSEIF);
					setState(517);
					logicalExpression();
					setState(518);
					match(THEN);
					setState(519);
					logicalExpression();
					}
					}
					setState(525);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(526);
				match(ELSE);
				setState(527);
				logicalExpression();
				setState(528);
				match(ENDIF);
				}
				break;
			case 3:
				_localctx = new LogicalFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(530);
				match(IF);
				setState(531);
				match(LPAREN);
				setState(532);
				logicalExpression();
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
				logicalExpression();
				setState(542);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,35,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(535);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(536);
						logicalExpression();
						setState(537);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(538);
						logicalExpression();
						}
						} 
					}
					setState(544);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,35,_ctx);
				}
				setState(545);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(546);
				logicalExpression();
				setState(547);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new LogicalReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(550);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==BOOLEAN_TYPE) {
					{
					setState(549);
					match(BOOLEAN_TYPE);
					}
				}

				setState(552);
				referenceTarget();
				setState(555);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(553);
					match(NULLCOALESCE);
					setState(554);
					logicalEntity();
					}
				}

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
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
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
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorParser.SEMI, i);
		}
		public MathFunctionDecisionOperationContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathFunctionDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathFunctionDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathFunctionDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumericReferenceOperationContext extends NumericEntityContext {
		public ReferenceTargetContext referenceTarget() {
			return getRuleContext(ReferenceTargetContext.class,0);
		}
		public TerminalNode NUMBER_TYPE() { return getToken(ExpressionEvaluatorParser.NUMBER_TYPE, 0); }
		public TerminalNode NULLCOALESCE() { return getToken(ExpressionEvaluatorParser.NULLCOALESCE, 0); }
		public NumericEntityContext numericEntity() {
			return getRuleContext(NumericEntityContext.class,0);
		}
		public NumericReferenceOperationContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNumericReferenceOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNumericReferenceOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNumericReferenceOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumericConstantOperationContext extends NumericEntityContext {
		public TerminalNode NUMBER() { return getToken(ExpressionEvaluatorParser.NUMBER, 0); }
		public NumericConstantOperationContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNumericConstantOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNumericConstantOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNumericConstantOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathDecisionOperationContext extends NumericEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorParser.THEN, i);
		}
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorParser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorParser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorParser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorParser.ELSEIF, i);
		}
		public MathDecisionOperationContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericEntityContext numericEntity() throws RecognitionException {
		NumericEntityContext _localctx = new NumericEntityContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_numericEntity);
		int _la;
		try {
			int _alt;
			setState(605);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
			case 1:
				_localctx = new MathDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(559);
				match(IF);
				setState(560);
				logicalExpression();
				setState(561);
				match(THEN);
				setState(562);
				mathExpression();
				setState(570);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(563);
					match(ELSEIF);
					setState(564);
					logicalExpression();
					setState(565);
					match(THEN);
					setState(566);
					mathExpression();
					}
					}
					setState(572);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(573);
				match(ELSE);
				setState(574);
				mathExpression();
				setState(575);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new MathFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(577);
				match(IF);
				setState(578);
				match(LPAREN);
				setState(579);
				logicalExpression();
				setState(580);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(581);
				mathExpression();
				setState(589);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(582);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(583);
						logicalExpression();
						setState(584);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(585);
						mathExpression();
						}
						} 
					}
					setState(591);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
				}
				setState(592);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(593);
				mathExpression();
				setState(594);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new NumericConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(596);
				match(NUMBER);
				}
				break;
			case 4:
				_localctx = new NumericReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(598);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NUMBER_TYPE) {
					{
					setState(597);
					match(NUMBER_TYPE);
					}
				}

				setState(600);
				referenceTarget();
				setState(603);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(601);
					match(NULLCOALESCE);
					setState(602);
					numericEntity();
					}
				}

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
	public static class StringConcatExpressionContext extends ParserRuleContext {
		public StringConcatExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringConcatExpression; }
	 
		public StringConcatExpressionContext() { }
		public void copyFrom(StringConcatExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringConcatenationOperationContext extends StringConcatExpressionContext {
		public List<StringEntityContext> stringEntity() {
			return getRuleContexts(StringEntityContext.class);
		}
		public StringEntityContext stringEntity(int i) {
			return getRuleContext(StringEntityContext.class,i);
		}
		public List<TerminalNode> CONCAT() { return getTokens(ExpressionEvaluatorParser.CONCAT); }
		public TerminalNode CONCAT(int i) {
			return getToken(ExpressionEvaluatorParser.CONCAT, i);
		}
		public StringConcatenationOperationContext(StringConcatExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringConcatenationOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringConcatenationOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringConcatenationOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringConcatExpressionContext stringConcatExpression() throws RecognitionException {
		StringConcatExpressionContext _localctx = new StringConcatExpressionContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_stringConcatExpression);
		try {
			int _alt;
			_localctx = new StringConcatenationOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(607);
			stringEntity();
			setState(612);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,44,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(608);
					match(CONCAT);
					setState(609);
					stringEntity();
					}
					} 
				}
				setState(614);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,44,_ctx);
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
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<StringConcatExpressionContext> stringConcatExpression() {
			return getRuleContexts(StringConcatExpressionContext.class);
		}
		public StringConcatExpressionContext stringConcatExpression(int i) {
			return getRuleContext(StringConcatExpressionContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorParser.SEMI, i);
		}
		public StringFunctionDecisionOperationContext(StringEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringFunctionDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringFunctionDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringFunctionDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringReferenceOperationContext extends StringEntityContext {
		public ReferenceTargetContext referenceTarget() {
			return getRuleContext(ReferenceTargetContext.class,0);
		}
		public TerminalNode STRING_TYPE() { return getToken(ExpressionEvaluatorParser.STRING_TYPE, 0); }
		public TerminalNode NULLCOALESCE() { return getToken(ExpressionEvaluatorParser.NULLCOALESCE, 0); }
		public StringConcatExpressionContext stringConcatExpression() {
			return getRuleContext(StringConcatExpressionContext.class,0);
		}
		public StringReferenceOperationContext(StringEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringReferenceOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringReferenceOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringReferenceOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringConstantOperationContext extends StringEntityContext {
		public TerminalNode STRING() { return getToken(ExpressionEvaluatorParser.STRING, 0); }
		public StringConstantOperationContext(StringEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringConstantOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringConstantOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringConstantOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringDecisionOperationContext extends StringEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorParser.THEN, i);
		}
		public List<StringConcatExpressionContext> stringConcatExpression() {
			return getRuleContexts(StringConcatExpressionContext.class);
		}
		public StringConcatExpressionContext stringConcatExpression(int i) {
			return getRuleContext(StringConcatExpressionContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorParser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorParser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorParser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorParser.ELSEIF, i);
		}
		public StringDecisionOperationContext(StringEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringEntityContext stringEntity() throws RecognitionException {
		StringEntityContext _localctx = new StringEntityContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_stringEntity);
		int _la;
		try {
			int _alt;
			setState(661);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,49,_ctx) ) {
			case 1:
				_localctx = new StringDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(615);
				match(IF);
				setState(616);
				logicalExpression();
				setState(617);
				match(THEN);
				setState(618);
				stringConcatExpression();
				setState(626);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(619);
					match(ELSEIF);
					setState(620);
					logicalExpression();
					setState(621);
					match(THEN);
					setState(622);
					stringConcatExpression();
					}
					}
					setState(628);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(629);
				match(ELSE);
				setState(630);
				stringConcatExpression();
				setState(631);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new StringFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(633);
				match(IF);
				setState(634);
				match(LPAREN);
				setState(635);
				logicalExpression();
				setState(636);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(637);
				stringConcatExpression();
				setState(645);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,46,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(638);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(639);
						logicalExpression();
						setState(640);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(641);
						stringConcatExpression();
						}
						} 
					}
					setState(647);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,46,_ctx);
				}
				setState(648);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(649);
				stringConcatExpression();
				setState(650);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new StringConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(652);
				match(STRING);
				}
				break;
			case 4:
				_localctx = new StringReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(654);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING_TYPE) {
					{
					setState(653);
					match(STRING_TYPE);
					}
				}

				setState(656);
				referenceTarget();
				setState(659);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(657);
					match(NULLCOALESCE);
					setState(658);
					stringConcatExpression();
					}
				}

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
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorParser.THEN, i);
		}
		public List<DateEntityContext> dateEntity() {
			return getRuleContexts(DateEntityContext.class);
		}
		public DateEntityContext dateEntity(int i) {
			return getRuleContext(DateEntityContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorParser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorParser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorParser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorParser.ELSEIF, i);
		}
		public DateDecisionOperationContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateFunctionDecisionOperationContext extends DateEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
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
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorParser.SEMI, i);
		}
		public DateFunctionDecisionOperationContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateFunctionDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateFunctionDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateFunctionDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateReferenceOperationContext extends DateEntityContext {
		public ReferenceTargetContext referenceTarget() {
			return getRuleContext(ReferenceTargetContext.class,0);
		}
		public TerminalNode DATE_TYPE() { return getToken(ExpressionEvaluatorParser.DATE_TYPE, 0); }
		public TerminalNode NULLCOALESCE() { return getToken(ExpressionEvaluatorParser.NULLCOALESCE, 0); }
		public DateEntityContext dateEntity() {
			return getRuleContext(DateEntityContext.class,0);
		}
		public DateReferenceOperationContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateReferenceOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateReferenceOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateReferenceOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateCurrentValueOperationContext extends DateEntityContext {
		public TerminalNode NOW_DATE() { return getToken(ExpressionEvaluatorParser.NOW_DATE, 0); }
		public DateCurrentValueOperationContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateCurrentValueOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateCurrentValueOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateCurrentValueOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateConstantOperationContext extends DateEntityContext {
		public TerminalNode DATE() { return getToken(ExpressionEvaluatorParser.DATE, 0); }
		public DateConstantOperationContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateConstantOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateConstantOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateConstantOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateEntityContext dateEntity() throws RecognitionException {
		DateEntityContext _localctx = new DateEntityContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_dateEntity);
		int _la;
		try {
			int _alt;
			setState(710);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
			case 1:
				_localctx = new DateDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(663);
				match(IF);
				setState(664);
				logicalExpression();
				setState(665);
				match(THEN);
				setState(666);
				dateEntity();
				setState(674);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(667);
					match(ELSEIF);
					setState(668);
					logicalExpression();
					setState(669);
					match(THEN);
					setState(670);
					dateEntity();
					}
					}
					setState(676);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(677);
				match(ELSE);
				setState(678);
				dateEntity();
				setState(679);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new DateFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(681);
				match(IF);
				setState(682);
				match(LPAREN);
				setState(683);
				logicalExpression();
				setState(684);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(685);
				dateEntity();
				setState(693);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,51,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(686);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(687);
						logicalExpression();
						setState(688);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(689);
						dateEntity();
						}
						} 
					}
					setState(695);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,51,_ctx);
				}
				setState(696);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(697);
				dateEntity();
				setState(698);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new DateConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(700);
				match(DATE);
				}
				break;
			case 4:
				_localctx = new DateCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(701);
				match(NOW_DATE);
				}
				break;
			case 5:
				_localctx = new DateReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(703);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DATE_TYPE) {
					{
					setState(702);
					match(DATE_TYPE);
					}
				}

				setState(705);
				referenceTarget();
				setState(708);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(706);
					match(NULLCOALESCE);
					setState(707);
					dateEntity();
					}
				}

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
		public TerminalNode NOW_TIME() { return getToken(ExpressionEvaluatorParser.NOW_TIME, 0); }
		public TimeCurrentValueOperationContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeCurrentValueOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeCurrentValueOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeCurrentValueOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeFunctionDecisionOperationContext extends TimeEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
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
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorParser.SEMI, i);
		}
		public TimeFunctionDecisionOperationContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeFunctionDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeFunctionDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeFunctionDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeConstantOperationContext extends TimeEntityContext {
		public TerminalNode TIME() { return getToken(ExpressionEvaluatorParser.TIME, 0); }
		public TimeConstantOperationContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeConstantOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeConstantOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeConstantOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeDecisionOperationContext extends TimeEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorParser.THEN, i);
		}
		public List<TimeEntityContext> timeEntity() {
			return getRuleContexts(TimeEntityContext.class);
		}
		public TimeEntityContext timeEntity(int i) {
			return getRuleContext(TimeEntityContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorParser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorParser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorParser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorParser.ELSEIF, i);
		}
		public TimeDecisionOperationContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeReferenceOperationContext extends TimeEntityContext {
		public ReferenceTargetContext referenceTarget() {
			return getRuleContext(ReferenceTargetContext.class,0);
		}
		public TerminalNode TIME_TYPE() { return getToken(ExpressionEvaluatorParser.TIME_TYPE, 0); }
		public TerminalNode NULLCOALESCE() { return getToken(ExpressionEvaluatorParser.NULLCOALESCE, 0); }
		public TimeEntityContext timeEntity() {
			return getRuleContext(TimeEntityContext.class,0);
		}
		public TimeReferenceOperationContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeReferenceOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeReferenceOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeReferenceOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TimeEntityContext timeEntity() throws RecognitionException {
		TimeEntityContext _localctx = new TimeEntityContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_timeEntity);
		int _la;
		try {
			int _alt;
			setState(759);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
			case 1:
				_localctx = new TimeDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(712);
				match(IF);
				setState(713);
				logicalExpression();
				setState(714);
				match(THEN);
				setState(715);
				timeEntity();
				setState(723);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(716);
					match(ELSEIF);
					setState(717);
					logicalExpression();
					setState(718);
					match(THEN);
					setState(719);
					timeEntity();
					}
					}
					setState(725);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(726);
				match(ELSE);
				setState(727);
				timeEntity();
				setState(728);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new TimeFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(730);
				match(IF);
				setState(731);
				match(LPAREN);
				setState(732);
				logicalExpression();
				setState(733);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(734);
				timeEntity();
				setState(742);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,56,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(735);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(736);
						logicalExpression();
						setState(737);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(738);
						timeEntity();
						}
						} 
					}
					setState(744);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,56,_ctx);
				}
				setState(745);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(746);
				timeEntity();
				setState(747);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new TimeConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(749);
				match(TIME);
				}
				break;
			case 4:
				_localctx = new TimeCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(750);
				match(NOW_TIME);
				}
				break;
			case 5:
				_localctx = new TimeReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(752);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TIME_TYPE) {
					{
					setState(751);
					match(TIME_TYPE);
					}
				}

				setState(754);
				referenceTarget();
				setState(757);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(755);
					match(NULLCOALESCE);
					setState(756);
					timeEntity();
					}
				}

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
		public TerminalNode DATETIME_TYPE() { return getToken(ExpressionEvaluatorParser.DATETIME_TYPE, 0); }
		public TerminalNode NULLCOALESCE() { return getToken(ExpressionEvaluatorParser.NULLCOALESCE, 0); }
		public DateTimeEntityContext dateTimeEntity() {
			return getRuleContext(DateTimeEntityContext.class,0);
		}
		public DateTimeReferenceOperationContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeReferenceOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeReferenceOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeReferenceOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeFunctionDecisionOperationContext extends DateTimeEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
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
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorParser.SEMI, i);
		}
		public DateTimeFunctionDecisionOperationContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeFunctionDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeFunctionDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeFunctionDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeDecisionOperationContext extends DateTimeEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorParser.THEN, i);
		}
		public List<DateTimeEntityContext> dateTimeEntity() {
			return getRuleContexts(DateTimeEntityContext.class);
		}
		public DateTimeEntityContext dateTimeEntity(int i) {
			return getRuleContext(DateTimeEntityContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorParser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorParser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorParser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorParser.ELSEIF, i);
		}
		public DateTimeDecisionOperationContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeConstantOperationContext extends DateTimeEntityContext {
		public TerminalNode DATETIME() { return getToken(ExpressionEvaluatorParser.DATETIME, 0); }
		public TerminalNode TIME_OFFSET() { return getToken(ExpressionEvaluatorParser.TIME_OFFSET, 0); }
		public DateTimeConstantOperationContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeConstantOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeConstantOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeConstantOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeCurrentValueOperationContext extends DateTimeEntityContext {
		public TerminalNode NOW_DATETIME() { return getToken(ExpressionEvaluatorParser.NOW_DATETIME, 0); }
		public DateTimeCurrentValueOperationContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeCurrentValueOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeCurrentValueOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeCurrentValueOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateTimeEntityContext dateTimeEntity() throws RecognitionException {
		DateTimeEntityContext _localctx = new DateTimeEntityContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_dateTimeEntity);
		int _la;
		try {
			int _alt;
			setState(811);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,65,_ctx) ) {
			case 1:
				_localctx = new DateTimeDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(761);
				match(IF);
				setState(762);
				logicalExpression();
				setState(763);
				match(THEN);
				setState(764);
				dateTimeEntity();
				setState(772);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(765);
					match(ELSEIF);
					setState(766);
					logicalExpression();
					setState(767);
					match(THEN);
					setState(768);
					dateTimeEntity();
					}
					}
					setState(774);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(775);
				match(ELSE);
				setState(776);
				dateTimeEntity();
				setState(777);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new DateTimeFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(779);
				match(IF);
				setState(780);
				match(LPAREN);
				setState(781);
				logicalExpression();
				setState(782);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(783);
				dateTimeEntity();
				setState(791);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(784);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(785);
						logicalExpression();
						setState(786);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(787);
						dateTimeEntity();
						}
						} 
					}
					setState(793);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
				}
				setState(794);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(795);
				dateTimeEntity();
				setState(796);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new DateTimeConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(798);
				match(DATETIME);
				setState(800);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TIME_OFFSET) {
					{
					setState(799);
					match(TIME_OFFSET);
					}
				}

				}
				break;
			case 4:
				_localctx = new DateTimeCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(802);
				match(NOW_DATETIME);
				}
				break;
			case 5:
				_localctx = new DateTimeReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(804);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DATETIME_TYPE) {
					{
					setState(803);
					match(DATETIME_TYPE);
					}
				}

				setState(806);
				referenceTarget();
				setState(809);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(807);
					match(NULLCOALESCE);
					setState(808);
					dateTimeEntity();
					}
				}

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
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
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
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorParser.SEMI, i);
		}
		public VectorFunctionDecisionOperationContext(VectorEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterVectorFunctionDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitVectorFunctionDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitVectorFunctionDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VectorReferenceOperationContext extends VectorEntityContext {
		public ReferenceTargetContext referenceTarget() {
			return getRuleContext(ReferenceTargetContext.class,0);
		}
		public TerminalNode VECTOR_TYPE() { return getToken(ExpressionEvaluatorParser.VECTOR_TYPE, 0); }
		public TerminalNode NULLCOALESCE() { return getToken(ExpressionEvaluatorParser.NULLCOALESCE, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public VectorReferenceOperationContext(VectorEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterVectorReferenceOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitVectorReferenceOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitVectorReferenceOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VectorDecisionOperationContext extends VectorEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(ExpressionEvaluatorParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(ExpressionEvaluatorParser.THEN, i);
		}
		public List<VectorEntityContext> vectorEntity() {
			return getRuleContexts(VectorEntityContext.class);
		}
		public VectorEntityContext vectorEntity(int i) {
			return getRuleContext(VectorEntityContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorParser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorParser.ENDIF, 0); }
		public List<TerminalNode> ELSEIF() { return getTokens(ExpressionEvaluatorParser.ELSEIF); }
		public TerminalNode ELSEIF(int i) {
			return getToken(ExpressionEvaluatorParser.ELSEIF, i);
		}
		public VectorDecisionOperationContext(VectorEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterVectorDecisionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitVectorDecisionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitVectorDecisionOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VectorOfEntitiesOperationContext extends VectorEntityContext {
		public TerminalNode LBRACKET() { return getToken(ExpressionEvaluatorParser.LBRACKET, 0); }
		public List<AllEntityTypesContext> allEntityTypes() {
			return getRuleContexts(AllEntityTypesContext.class);
		}
		public AllEntityTypesContext allEntityTypes(int i) {
			return getRuleContext(AllEntityTypesContext.class,i);
		}
		public TerminalNode RBRACKET() { return getToken(ExpressionEvaluatorParser.RBRACKET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public VectorOfEntitiesOperationContext(VectorEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterVectorOfEntitiesOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitVectorOfEntitiesOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitVectorOfEntitiesOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VectorEntityContext vectorEntity() throws RecognitionException {
		VectorEntityContext _localctx = new VectorEntityContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_vectorEntity);
		int _la;
		try {
			int _alt;
			setState(869);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,71,_ctx) ) {
			case 1:
				_localctx = new VectorDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(813);
				match(IF);
				setState(814);
				logicalExpression();
				setState(815);
				match(THEN);
				setState(816);
				vectorEntity();
				setState(824);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(817);
					match(ELSEIF);
					setState(818);
					logicalExpression();
					setState(819);
					match(THEN);
					setState(820);
					vectorEntity();
					}
					}
					setState(826);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(827);
				match(ELSE);
				setState(828);
				vectorEntity();
				setState(829);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new VectorFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(831);
				match(IF);
				setState(832);
				match(LPAREN);
				setState(833);
				logicalExpression();
				setState(834);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(835);
				vectorEntity();
				setState(843);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,67,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(836);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(837);
						logicalExpression();
						setState(838);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(839);
						vectorEntity();
						}
						} 
					}
					setState(845);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,67,_ctx);
				}
				setState(846);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(847);
				vectorEntity();
				setState(848);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new VectorOfEntitiesOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(850);
				match(LBRACKET);
				setState(851);
				allEntityTypes();
				setState(856);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(852);
					match(COMMA);
					setState(853);
					allEntityTypes();
					}
					}
					setState(858);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(859);
				match(RBRACKET);
				}
				break;
			case 4:
				_localctx = new VectorReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(862);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==VECTOR_TYPE) {
					{
					setState(861);
					match(VECTOR_TYPE);
					}
				}

				setState(864);
				referenceTarget();
				setState(867);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(865);
					match(NULLCOALESCE);
					setState(866);
					vectorEntity();
					}
				}

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
		public TerminalNode LBRACKET() { return getToken(ExpressionEvaluatorParser.LBRACKET, 0); }
		public List<TerminalNode> IDENTIFIER() { return getTokens(ExpressionEvaluatorParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(ExpressionEvaluatorParser.IDENTIFIER, i);
		}
		public TerminalNode RBRACKET() { return getToken(ExpressionEvaluatorParser.RBRACKET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public VectorOfVariablesOperationContext(VectorOfVariablesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterVectorOfVariablesOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitVectorOfVariablesOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitVectorOfVariablesOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VectorOfVariablesContext vectorOfVariables() throws RecognitionException {
		VectorOfVariablesContext _localctx = new VectorOfVariablesContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_vectorOfVariables);
		int _la;
		try {
			_localctx = new VectorOfVariablesOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(871);
			match(LBRACKET);
			setState(872);
			match(IDENTIFIER);
			setState(877);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(873);
				match(COMMA);
				setState(874);
				match(IDENTIFIER);
				}
				}
				setState(879);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(880);
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
		"\u0004\u0001E\u0373\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0001\u0000\u0005\u0000L\b\u0000\n\u0000\f\u0000"+
		"O\t\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0004\u0001"+
		"U\b\u0001\u000b\u0001\f\u0001V\u0001\u0001\u0001\u0001\u0001\u0002\u0005"+
		"\u0002\\\b\u0002\n\u0002\f\u0002_\t\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003n\b"+
		"\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0005"+
		"\u0005u\b\u0005\n\u0005\f\u0005x\t\u0005\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0005\u0006}\b\u0006\n\u0006\f\u0006\u0080\t\u0006\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0086\b\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0122\b\u0007\u0001\b"+
		"\u0001\b\u0001\b\u0005\b\u0127\b\b\n\b\f\b\u012a\t\b\u0001\t\u0001\t\u0001"+
		"\t\u0003\t\u012f\b\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u0136"+
		"\b\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0005\f\u013d\b\f"+
		"\n\f\f\f\u0140\t\f\u0001\r\u0001\r\u0001\r\u0005\r\u0145\b\r\n\r\f\r\u0148"+
		"\t\r\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u014d\b\u000e\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u0152\b\u000f\n\u000f\f\u000f"+
		"\u0155\t\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u015a\b"+
		"\u0010\u0001\u0011\u0001\u0011\u0005\u0011\u015e\b\u0011\n\u0011\f\u0011"+
		"\u0161\t\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u0171\b\u0012\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u0178\b\u0013"+
		"\n\u0013\f\u0013\u017b\t\u0013\u0003\u0013\u017d\b\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0005\u0014\u0184\b\u0014\n"+
		"\u0014\f\u0014\u0187\t\u0014\u0003\u0014\u0189\b\u0014\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0005\u0015\u0195\b\u0015\n\u0015\f\u0015"+
		"\u0198\t\u0015\u0003\u0015\u019a\b\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005\u0015\u01a3"+
		"\b\u0015\n\u0015\f\u0015\u01a6\t\u0015\u0003\u0015\u01a8\b\u0015\u0001"+
		"\u0015\u0003\u0015\u01ab\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u01b3\b\u0016\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0003\u0017\u01bd\b\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018\u01c7"+
		"\b\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u01d2\b\u0019\n"+
		"\u0019\f\u0019\u01d5\t\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u01e5"+
		"\b\u0019\n\u0019\f\u0019\u01e8\t\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u01f0\b\u0019\u0001\u001a"+
		"\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0003\u001b"+
		"\u01fe\b\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0005\u001c"+
		"\u020a\b\u001c\n\u001c\f\u001c\u020d\t\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0005"+
		"\u001c\u021d\b\u001c\n\u001c\f\u001c\u0220\t\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0003\u001c\u0227\b\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0003\u001c\u022c\b\u001c\u0003\u001c\u022e\b"+
		"\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d\u0239\b\u001d\n"+
		"\u001d\f\u001d\u023c\t\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d\u024c"+
		"\b\u001d\n\u001d\f\u001d\u024f\t\u001d\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0003\u001d\u0257\b\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0003\u001d\u025c\b\u001d\u0003\u001d\u025e\b"+
		"\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u0263\b\u001e\n"+
		"\u001e\f\u001e\u0266\t\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0005"+
		"\u001f\u0271\b\u001f\n\u001f\f\u001f\u0274\t\u001f\u0001\u001f\u0001\u001f"+
		"\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f"+
		"\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f"+
		"\u0005\u001f\u0284\b\u001f\n\u001f\f\u001f\u0287\t\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u028f"+
		"\b\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u0294\b\u001f"+
		"\u0003\u001f\u0296\b\u001f\u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0001 \u0001 \u0005 \u02a1\b \n \f \u02a4\t \u0001 \u0001 \u0001 \u0001"+
		" \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0005 \u02b4\b \n \f \u02b7\t \u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0001 \u0003 \u02c0\b \u0001 \u0001 \u0001 \u0003 \u02c5\b \u0003 \u02c7"+
		"\b \u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0005"+
		"!\u02d2\b!\n!\f!\u02d5\t!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0005!\u02e5\b!\n!"+
		"\f!\u02e8\t!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0003!\u02f1"+
		"\b!\u0001!\u0001!\u0001!\u0003!\u02f6\b!\u0003!\u02f8\b!\u0001\"\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0005\"\u0303"+
		"\b\"\n\"\f\"\u0306\t\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\""+
		"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0005"+
		"\"\u0316\b\"\n\"\f\"\u0319\t\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\""+
		"\u0001\"\u0003\"\u0321\b\"\u0001\"\u0001\"\u0003\"\u0325\b\"\u0001\"\u0001"+
		"\"\u0001\"\u0003\"\u032a\b\"\u0003\"\u032c\b\"\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0005#\u0337\b#\n#\f#\u033a\t#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0005#\u034a\b#\n#\f#\u034d\t#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0005#\u0357\b#\n#\f#\u035a\t#\u0001"+
		"#\u0001#\u0001#\u0003#\u035f\b#\u0001#\u0001#\u0001#\u0003#\u0364\b#\u0003"+
		"#\u0366\b#\u0001$\u0001$\u0001$\u0001$\u0005$\u036c\b$\n$\f$\u036f\t$"+
		"\u0001$\u0001$\u0001$\u0000\u0000%\u0000\u0002\u0004\u0006\b\n\f\u000e"+
		"\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDF"+
		"H\u0000\u0007\u0001\u0000\t\f\u0002\u0000\u0017\u0017!!\u0001\u0000\u0011"+
		"\u0012\u0002\u0000\u000f\u0010\u0014\u0014\u0002\u0000\u0013\u0013\u0017"+
		"\u0017\u0001\u0000+,\u0001\u0000\r\u000e\u03e0\u0000M\u0001\u0000\u0000"+
		"\u0000\u0002T\u0001\u0000\u0000\u0000\u0004]\u0001\u0000\u0000\u0000\u0006"+
		"m\u0001\u0000\u0000\u0000\bo\u0001\u0000\u0000\u0000\nq\u0001\u0000\u0000"+
		"\u0000\fy\u0001\u0000\u0000\u0000\u000e\u0121\u0001\u0000\u0000\u0000"+
		"\u0010\u0123\u0001\u0000\u0000\u0000\u0012\u012e\u0001\u0000\u0000\u0000"+
		"\u0014\u0135\u0001\u0000\u0000\u0000\u0016\u0137\u0001\u0000\u0000\u0000"+
		"\u0018\u0139\u0001\u0000\u0000\u0000\u001a\u0141\u0001\u0000\u0000\u0000"+
		"\u001c\u014c\u0001\u0000\u0000\u0000\u001e\u014e\u0001\u0000\u0000\u0000"+
		" \u0156\u0001\u0000\u0000\u0000\"\u015b\u0001\u0000\u0000\u0000$\u0170"+
		"\u0001\u0000\u0000\u0000&\u0172\u0001\u0000\u0000\u0000(\u0188\u0001\u0000"+
		"\u0000\u0000*\u01aa\u0001\u0000\u0000\u0000,\u01b2\u0001\u0000\u0000\u0000"+
		".\u01bc\u0001\u0000\u0000\u00000\u01c6\u0001\u0000\u0000\u00002\u01ef"+
		"\u0001\u0000\u0000\u00004\u01f1\u0001\u0000\u0000\u00006\u01fd\u0001\u0000"+
		"\u0000\u00008\u022d\u0001\u0000\u0000\u0000:\u025d\u0001\u0000\u0000\u0000"+
		"<\u025f\u0001\u0000\u0000\u0000>\u0295\u0001\u0000\u0000\u0000@\u02c6"+
		"\u0001\u0000\u0000\u0000B\u02f7\u0001\u0000\u0000\u0000D\u032b\u0001\u0000"+
		"\u0000\u0000F\u0365\u0001\u0000\u0000\u0000H\u0367\u0001\u0000\u0000\u0000"+
		"JL\u0003\u0006\u0003\u0000KJ\u0001\u0000\u0000\u0000LO\u0001\u0000\u0000"+
		"\u0000MK\u0001\u0000\u0000\u0000MN\u0001\u0000\u0000\u0000NP\u0001\u0000"+
		"\u0000\u0000OM\u0001\u0000\u0000\u0000PQ\u0003\u0016\u000b\u0000QR\u0005"+
		"\u0000\u0000\u0001R\u0001\u0001\u0000\u0000\u0000SU\u0003\u0006\u0003"+
		"\u0000TS\u0001\u0000\u0000\u0000UV\u0001\u0000\u0000\u0000VT\u0001\u0000"+
		"\u0000\u0000VW\u0001\u0000\u0000\u0000WX\u0001\u0000\u0000\u0000XY\u0005"+
		"\u0000\u0000\u0001Y\u0003\u0001\u0000\u0000\u0000Z\\\u0003\u0006\u0003"+
		"\u0000[Z\u0001\u0000\u0000\u0000\\_\u0001\u0000\u0000\u0000][\u0001\u0000"+
		"\u0000\u0000]^\u0001\u0000\u0000\u0000^`\u0001\u0000\u0000\u0000_]\u0001"+
		"\u0000\u0000\u0000`a\u0003\b\u0004\u0000ab\u0005\u0000\u0000\u0001b\u0005"+
		"\u0001\u0000\u0000\u0000cd\u00053\u0000\u0000de\u0005\u001f\u0000\u0000"+
		"ef\u00030\u0018\u0000fg\u0005,\u0000\u0000gn\u0001\u0000\u0000\u0000h"+
		"i\u0003H$\u0000ij\u0005\u001f\u0000\u0000jk\u0003F#\u0000kl\u0005,\u0000"+
		"\u0000ln\u0001\u0000\u0000\u0000mc\u0001\u0000\u0000\u0000mh\u0001\u0000"+
		"\u0000\u0000n\u0007\u0001\u0000\u0000\u0000op\u0003\n\u0005\u0000p\t\u0001"+
		"\u0000\u0000\u0000qv\u0003\f\u0006\u0000rs\u0005\b\u0000\u0000su\u0003"+
		"\f\u0006\u0000tr\u0001\u0000\u0000\u0000ux\u0001\u0000\u0000\u0000vt\u0001"+
		"\u0000\u0000\u0000vw\u0001\u0000\u0000\u0000w\u000b\u0001\u0000\u0000"+
		"\u0000xv\u0001\u0000\u0000\u0000y~\u0003\u000e\u0007\u0000z{\u0005\u0007"+
		"\u0000\u0000{}\u0003\u000e\u0007\u0000|z\u0001\u0000\u0000\u0000}\u0080"+
		"\u0001\u0000\u0000\u0000~|\u0001\u0000\u0000\u0000~\u007f\u0001\u0000"+
		"\u0000\u0000\u007f\r\u0001\u0000\u0000\u0000\u0080~\u0001\u0000\u0000"+
		"\u0000\u0081\u0085\u0003\u0010\b\u0000\u0082\u0083\u0003,\u0016\u0000"+
		"\u0083\u0084\u0003\u0010\b\u0000\u0084\u0086\u0001\u0000\u0000\u0000\u0085"+
		"\u0082\u0001\u0000\u0000\u0000\u0085\u0086\u0001\u0000\u0000\u0000\u0086"+
		"\u0122\u0001\u0000\u0000\u0000\u0087\u0088\u0003\u0016\u000b\u0000\u0088"+
		"\u0089\u0003,\u0016\u0000\u0089\u008a\u0003\u0016\u000b\u0000\u008a\u0122"+
		"\u0001\u0000\u0000\u0000\u008b\u008c\u0003<\u001e\u0000\u008c\u008d\u0003"+
		",\u0016\u0000\u008d\u008e\u0003<\u001e\u0000\u008e\u0122\u0001\u0000\u0000"+
		"\u0000\u008f\u0090\u0003@ \u0000\u0090\u0091\u0003,\u0016\u0000\u0091"+
		"\u0092\u0003@ \u0000\u0092\u0122\u0001\u0000\u0000\u0000\u0093\u0094\u0003"+
		"B!\u0000\u0094\u0095\u0003,\u0016\u0000\u0095\u0096\u0003B!\u0000\u0096"+
		"\u0122\u0001\u0000\u0000\u0000\u0097\u0098\u0003D\"\u0000\u0098\u0099"+
		"\u0003,\u0016\u0000\u0099\u009a\u0003D\"\u0000\u009a\u0122\u0001\u0000"+
		"\u0000\u0000\u009b\u009c\u0003<\u001e\u0000\u009c\u009d\u0005\"\u0000"+
		"\u0000\u009d\u009e\u00054\u0000\u0000\u009e\u0122\u0001\u0000\u0000\u0000"+
		"\u009f\u00a0\u0003<\u001e\u0000\u00a0\u00a1\u0005#\u0000\u0000\u00a1\u00a2"+
		"\u00054\u0000\u0000\u00a2\u0122\u0001\u0000\u0000\u0000\u00a3\u00a4\u0003"+
		"\u0016\u000b\u0000\u00a4\u00a5\u00050\u0000\u0000\u00a5\u00a6\u0003F#"+
		"\u0000\u00a6\u0122\u0001\u0000\u0000\u0000\u00a7\u00a8\u0003<\u001e\u0000"+
		"\u00a8\u00a9\u00050\u0000\u0000\u00a9\u00aa\u0003F#\u0000\u00aa\u0122"+
		"\u0001\u0000\u0000\u0000\u00ab\u00ac\u0003@ \u0000\u00ac\u00ad\u00050"+
		"\u0000\u0000\u00ad\u00ae\u0003F#\u0000\u00ae\u0122\u0001\u0000\u0000\u0000"+
		"\u00af\u00b0\u0003B!\u0000\u00b0\u00b1\u00050\u0000\u0000\u00b1\u00b2"+
		"\u0003F#\u0000\u00b2\u0122\u0001\u0000\u0000\u0000\u00b3\u00b4\u0003D"+
		"\"\u0000\u00b4\u00b5\u00050\u0000\u0000\u00b5\u00b6\u0003F#\u0000\u00b6"+
		"\u0122\u0001\u0000\u0000\u0000\u00b7\u00b8\u0003\u0010\b\u0000\u00b8\u00b9"+
		"\u00050\u0000\u0000\u00b9\u00ba\u0003F#\u0000\u00ba\u0122\u0001\u0000"+
		"\u0000\u0000\u00bb\u00bc\u0005\u0006\u0000\u0000\u00bc\u00bd\u00050\u0000"+
		"\u0000\u00bd\u0122\u0003F#\u0000\u00be\u00bf\u0003\u0016\u000b\u0000\u00bf"+
		"\u00c0\u00051\u0000\u0000\u00c0\u00c1\u00050\u0000\u0000\u00c1\u00c2\u0003"+
		"F#\u0000\u00c2\u0122\u0001\u0000\u0000\u0000\u00c3\u00c4\u0003<\u001e"+
		"\u0000\u00c4\u00c5\u00051\u0000\u0000\u00c5\u00c6\u00050\u0000\u0000\u00c6"+
		"\u00c7\u0003F#\u0000\u00c7\u0122\u0001\u0000\u0000\u0000\u00c8\u00c9\u0003"+
		"@ \u0000\u00c9\u00ca\u00051\u0000\u0000\u00ca\u00cb\u00050\u0000\u0000"+
		"\u00cb\u00cc\u0003F#\u0000\u00cc\u0122\u0001\u0000\u0000\u0000\u00cd\u00ce"+
		"\u0003B!\u0000\u00ce\u00cf\u00051\u0000\u0000\u00cf\u00d0\u00050\u0000"+
		"\u0000\u00d0\u00d1\u0003F#\u0000\u00d1\u0122\u0001\u0000\u0000\u0000\u00d2"+
		"\u00d3\u0003D\"\u0000\u00d3\u00d4\u00051\u0000\u0000\u00d4\u00d5\u0005"+
		"0\u0000\u0000\u00d5\u00d6\u0003F#\u0000\u00d6\u0122\u0001\u0000\u0000"+
		"\u0000\u00d7\u00d8\u0003\u0010\b\u0000\u00d8\u00d9\u00051\u0000\u0000"+
		"\u00d9\u00da\u00050\u0000\u0000\u00da\u00db\u0003F#\u0000\u00db\u0122"+
		"\u0001\u0000\u0000\u0000\u00dc\u00dd\u0005\u0006\u0000\u0000\u00dd\u00de"+
		"\u00051\u0000\u0000\u00de\u00df\u00050\u0000\u0000\u00df\u0122\u0003F"+
		"#\u0000\u00e0\u00e1\u0003\u0016\u000b\u0000\u00e1\u00e2\u00052\u0000\u0000"+
		"\u00e2\u00e3\u0003\u0016\u000b\u0000\u00e3\u00e4\u0005\u0007\u0000\u0000"+
		"\u00e4\u00e5\u0003\u0016\u000b\u0000\u00e5\u0122\u0001\u0000\u0000\u0000"+
		"\u00e6\u00e7\u0003<\u001e\u0000\u00e7\u00e8\u00052\u0000\u0000\u00e8\u00e9"+
		"\u0003<\u001e\u0000\u00e9\u00ea\u0005\u0007\u0000\u0000\u00ea\u00eb\u0003"+
		"<\u001e\u0000\u00eb\u0122\u0001\u0000\u0000\u0000\u00ec\u00ed\u0003@ "+
		"\u0000\u00ed\u00ee\u00052\u0000\u0000\u00ee\u00ef\u0003@ \u0000\u00ef"+
		"\u00f0\u0005\u0007\u0000\u0000\u00f0\u00f1\u0003@ \u0000\u00f1\u0122\u0001"+
		"\u0000\u0000\u0000\u00f2\u00f3\u0003B!\u0000\u00f3\u00f4\u00052\u0000"+
		"\u0000\u00f4\u00f5\u0003B!\u0000\u00f5\u00f6\u0005\u0007\u0000\u0000\u00f6"+
		"\u00f7\u0003B!\u0000\u00f7\u0122\u0001\u0000\u0000\u0000\u00f8\u00f9\u0003"+
		"D\"\u0000\u00f9\u00fa\u00052\u0000\u0000\u00fa\u00fb\u0003D\"\u0000\u00fb"+
		"\u00fc\u0005\u0007\u0000\u0000\u00fc\u00fd\u0003D\"\u0000\u00fd\u0122"+
		"\u0001\u0000\u0000\u0000\u00fe\u00ff\u0003\u0016\u000b\u0000\u00ff\u0100"+
		"\u00051\u0000\u0000\u0100\u0101\u00052\u0000\u0000\u0101\u0102\u0003\u0016"+
		"\u000b\u0000\u0102\u0103\u0005\u0007\u0000\u0000\u0103\u0104\u0003\u0016"+
		"\u000b\u0000\u0104\u0122\u0001\u0000\u0000\u0000\u0105\u0106\u0003<\u001e"+
		"\u0000\u0106\u0107\u00051\u0000\u0000\u0107\u0108\u00052\u0000\u0000\u0108"+
		"\u0109\u0003<\u001e\u0000\u0109\u010a\u0005\u0007\u0000\u0000\u010a\u010b"+
		"\u0003<\u001e\u0000\u010b\u0122\u0001\u0000\u0000\u0000\u010c\u010d\u0003"+
		"@ \u0000\u010d\u010e\u00051\u0000\u0000\u010e\u010f\u00052\u0000\u0000"+
		"\u010f\u0110\u0003@ \u0000\u0110\u0111\u0005\u0007\u0000\u0000\u0111\u0112"+
		"\u0003@ \u0000\u0112\u0122\u0001\u0000\u0000\u0000\u0113\u0114\u0003B"+
		"!\u0000\u0114\u0115\u00051\u0000\u0000\u0115\u0116\u00052\u0000\u0000"+
		"\u0116\u0117\u0003B!\u0000\u0117\u0118\u0005\u0007\u0000\u0000\u0118\u0119"+
		"\u0003B!\u0000\u0119\u0122\u0001\u0000\u0000\u0000\u011a\u011b\u0003D"+
		"\"\u0000\u011b\u011c\u00051\u0000\u0000\u011c\u011d\u00052\u0000\u0000"+
		"\u011d\u011e\u0003D\"\u0000\u011e\u011f\u0005\u0007\u0000\u0000\u011f"+
		"\u0120\u0003D\"\u0000\u0120\u0122\u0001\u0000\u0000\u0000\u0121\u0081"+
		"\u0001\u0000\u0000\u0000\u0121\u0087\u0001\u0000\u0000\u0000\u0121\u008b"+
		"\u0001\u0000\u0000\u0000\u0121\u008f\u0001\u0000\u0000\u0000\u0121\u0093"+
		"\u0001\u0000\u0000\u0000\u0121\u0097\u0001\u0000\u0000\u0000\u0121\u009b"+
		"\u0001\u0000\u0000\u0000\u0121\u009f\u0001\u0000\u0000\u0000\u0121\u00a3"+
		"\u0001\u0000\u0000\u0000\u0121\u00a7\u0001\u0000\u0000\u0000\u0121\u00ab"+
		"\u0001\u0000\u0000\u0000\u0121\u00af\u0001\u0000\u0000\u0000\u0121\u00b3"+
		"\u0001\u0000\u0000\u0000\u0121\u00b7\u0001\u0000\u0000\u0000\u0121\u00bb"+
		"\u0001\u0000\u0000\u0000\u0121\u00be\u0001\u0000\u0000\u0000\u0121\u00c3"+
		"\u0001\u0000\u0000\u0000\u0121\u00c8\u0001\u0000\u0000\u0000\u0121\u00cd"+
		"\u0001\u0000\u0000\u0000\u0121\u00d2\u0001\u0000\u0000\u0000\u0121\u00d7"+
		"\u0001\u0000\u0000\u0000\u0121\u00dc\u0001\u0000\u0000\u0000\u0121\u00e0"+
		"\u0001\u0000\u0000\u0000\u0121\u00e6\u0001\u0000\u0000\u0000\u0121\u00ec"+
		"\u0001\u0000\u0000\u0000\u0121\u00f2\u0001\u0000\u0000\u0000\u0121\u00f8"+
		"\u0001\u0000\u0000\u0000\u0121\u00fe\u0001\u0000\u0000\u0000\u0121\u0105"+
		"\u0001\u0000\u0000\u0000\u0121\u010c\u0001\u0000\u0000\u0000\u0121\u0113"+
		"\u0001\u0000\u0000\u0000\u0121\u011a\u0001\u0000\u0000\u0000\u0122\u000f"+
		"\u0001\u0000\u0000\u0000\u0123\u0128\u0003\u0012\t\u0000\u0124\u0125\u0007"+
		"\u0000\u0000\u0000\u0125\u0127\u0003\u0012\t\u0000\u0126\u0124\u0001\u0000"+
		"\u0000\u0000\u0127\u012a\u0001\u0000\u0000\u0000\u0128\u0126\u0001\u0000"+
		"\u0000\u0000\u0128\u0129\u0001\u0000\u0000\u0000\u0129\u0011\u0001\u0000"+
		"\u0000\u0000\u012a\u0128\u0001\u0000\u0000\u0000\u012b\u012c\u0007\u0001"+
		"\u0000\u0000\u012c\u012f\u0003\u0012\t\u0000\u012d\u012f\u0003\u0014\n"+
		"\u0000\u012e\u012b\u0001\u0000\u0000\u0000\u012e\u012d\u0001\u0000\u0000"+
		"\u0000\u012f\u0013\u0001\u0000\u0000\u0000\u0130\u0131\u0005\'\u0000\u0000"+
		"\u0131\u0132\u0003\b\u0004\u0000\u0132\u0133\u0005(\u0000\u0000\u0133"+
		"\u0136\u0001\u0000\u0000\u0000\u0134\u0136\u00038\u001c\u0000\u0135\u0130"+
		"\u0001\u0000\u0000\u0000\u0135\u0134\u0001\u0000\u0000\u0000\u0136\u0015"+
		"\u0001\u0000\u0000\u0000\u0137\u0138\u0003\u0018\f\u0000\u0138\u0017\u0001"+
		"\u0000\u0000\u0000\u0139\u013e\u0003\u001a\r\u0000\u013a\u013b\u0007\u0002"+
		"\u0000\u0000\u013b\u013d\u0003\u001a\r\u0000\u013c\u013a\u0001\u0000\u0000"+
		"\u0000\u013d\u0140\u0001\u0000\u0000\u0000\u013e\u013c\u0001\u0000\u0000"+
		"\u0000\u013e\u013f\u0001\u0000\u0000\u0000\u013f\u0019\u0001\u0000\u0000"+
		"\u0000\u0140\u013e\u0001\u0000\u0000\u0000\u0141\u0146\u0003\u001c\u000e"+
		"\u0000\u0142\u0143\u0007\u0003\u0000\u0000\u0143\u0145\u0003\u001c\u000e"+
		"\u0000\u0144\u0142\u0001\u0000\u0000\u0000\u0145\u0148\u0001\u0000\u0000"+
		"\u0000\u0146\u0144\u0001\u0000\u0000\u0000\u0146\u0147\u0001\u0000\u0000"+
		"\u0000\u0147\u001b\u0001\u0000\u0000\u0000\u0148\u0146\u0001\u0000\u0000"+
		"\u0000\u0149\u014a\u0005\u0012\u0000\u0000\u014a\u014d\u0003\u001c\u000e"+
		"\u0000\u014b\u014d\u0003\u001e\u000f\u0000\u014c\u0149\u0001\u0000\u0000"+
		"\u0000\u014c\u014b\u0001\u0000\u0000\u0000\u014d\u001d\u0001\u0000\u0000"+
		"\u0000\u014e\u0153\u0003 \u0010\u0000\u014f\u0150\u0005\u0019\u0000\u0000"+
		"\u0150\u0152\u0003 \u0010\u0000\u0151\u014f\u0001\u0000\u0000\u0000\u0152"+
		"\u0155\u0001\u0000\u0000\u0000\u0153\u0151\u0001\u0000\u0000\u0000\u0153"+
		"\u0154\u0001\u0000\u0000\u0000\u0154\u001f\u0001\u0000\u0000\u0000\u0155"+
		"\u0153\u0001\u0000\u0000\u0000\u0156\u0159\u0003\"\u0011\u0000\u0157\u0158"+
		"\u0005\u0018\u0000\u0000\u0158\u015a\u0003\u001c\u000e\u0000\u0159\u0157"+
		"\u0001\u0000\u0000\u0000\u0159\u015a\u0001\u0000\u0000\u0000\u015a!\u0001"+
		"\u0000\u0000\u0000\u015b\u015f\u0003$\u0012\u0000\u015c\u015e\u0007\u0004"+
		"\u0000\u0000\u015d\u015c\u0001\u0000\u0000\u0000\u015e\u0161\u0001\u0000"+
		"\u0000\u0000\u015f\u015d\u0001\u0000\u0000\u0000\u015f\u0160\u0001\u0000"+
		"\u0000\u0000\u0160#\u0001\u0000\u0000\u0000\u0161\u015f\u0001\u0000\u0000"+
		"\u0000\u0162\u0163\u0005\'\u0000\u0000\u0163\u0164\u0003\u0016\u000b\u0000"+
		"\u0164\u0165\u0005(\u0000\u0000\u0165\u0171\u0001\u0000\u0000\u0000\u0166"+
		"\u0167\u0005\u001a\u0000\u0000\u0167\u0168\u0005\'\u0000\u0000\u0168\u0169"+
		"\u0003\u0016\u000b\u0000\u0169\u016a\u0005(\u0000\u0000\u016a\u0171\u0001"+
		"\u0000\u0000\u0000\u016b\u016c\u0005\u0015\u0000\u0000\u016c\u016d\u0003"+
		"\u0016\u000b\u0000\u016d\u016e\u0005\u0015\u0000\u0000\u016e\u0171\u0001"+
		"\u0000\u0000\u0000\u016f\u0171\u0003:\u001d\u0000\u0170\u0162\u0001\u0000"+
		"\u0000\u0000\u0170\u0166\u0001\u0000\u0000\u0000\u0170\u016b\u0001\u0000"+
		"\u0000\u0000\u0170\u016f\u0001\u0000\u0000\u0000\u0171%\u0001\u0000\u0000"+
		"\u0000\u0172\u0173\u00053\u0000\u0000\u0173\u017c\u0005\'\u0000\u0000"+
		"\u0174\u0179\u0003.\u0017\u0000\u0175\u0176\u0007\u0005\u0000\u0000\u0176"+
		"\u0178\u0003.\u0017\u0000\u0177\u0175\u0001\u0000\u0000\u0000\u0178\u017b"+
		"\u0001\u0000\u0000\u0000\u0179\u0177\u0001\u0000\u0000\u0000\u0179\u017a"+
		"\u0001\u0000\u0000\u0000\u017a\u017d\u0001\u0000\u0000\u0000\u017b\u0179"+
		"\u0001\u0000\u0000\u0000\u017c\u0174\u0001\u0000\u0000\u0000\u017c\u017d"+
		"\u0001\u0000\u0000\u0000\u017d\u017e\u0001\u0000\u0000\u0000\u017e\u017f"+
		"\u0005(\u0000\u0000\u017f\'\u0001\u0000\u0000\u0000\u0180\u0189\u0003"+
		"&\u0013\u0000\u0181\u0185\u00053\u0000\u0000\u0182\u0184\u0003*\u0015"+
		"\u0000\u0183\u0182\u0001\u0000\u0000\u0000\u0184\u0187\u0001\u0000\u0000"+
		"\u0000\u0185\u0183\u0001\u0000\u0000\u0000\u0185\u0186\u0001\u0000\u0000"+
		"\u0000\u0186\u0189\u0001\u0000\u0000\u0000\u0187\u0185\u0001\u0000\u0000"+
		"\u0000\u0188\u0180\u0001\u0000\u0000\u0000\u0188\u0181\u0001\u0000\u0000"+
		"\u0000\u0189)\u0001\u0000\u0000\u0000\u018a\u018b\u0005-\u0000\u0000\u018b"+
		"\u01ab\u00053\u0000\u0000\u018c\u018d\u0005/\u0000\u0000\u018d\u01ab\u0005"+
		"3\u0000\u0000\u018e\u018f\u0005-\u0000\u0000\u018f\u0190\u00053\u0000"+
		"\u0000\u0190\u0199\u0005\'\u0000\u0000\u0191\u0196\u0003.\u0017\u0000"+
		"\u0192\u0193\u0005+\u0000\u0000\u0193\u0195\u0003.\u0017\u0000\u0194\u0192"+
		"\u0001\u0000\u0000\u0000\u0195\u0198\u0001\u0000\u0000\u0000\u0196\u0194"+
		"\u0001\u0000\u0000\u0000\u0196\u0197\u0001\u0000\u0000\u0000\u0197\u019a"+
		"\u0001\u0000\u0000\u0000\u0198\u0196\u0001\u0000\u0000\u0000\u0199\u0191"+
		"\u0001\u0000\u0000\u0000\u0199\u019a\u0001\u0000\u0000\u0000\u019a\u019b"+
		"\u0001\u0000\u0000\u0000\u019b\u01ab\u0005(\u0000\u0000\u019c\u019d\u0005"+
		"/\u0000\u0000\u019d\u019e\u00053\u0000\u0000\u019e\u01a7\u0005\'\u0000"+
		"\u0000\u019f\u01a4\u0003.\u0017\u0000\u01a0\u01a1\u0005+\u0000\u0000\u01a1"+
		"\u01a3\u0003.\u0017\u0000\u01a2\u01a0\u0001\u0000\u0000\u0000\u01a3\u01a6"+
		"\u0001\u0000\u0000\u0000\u01a4\u01a2\u0001\u0000\u0000\u0000\u01a4\u01a5"+
		"\u0001\u0000\u0000\u0000\u01a5\u01a8\u0001\u0000\u0000\u0000\u01a6\u01a4"+
		"\u0001\u0000\u0000\u0000\u01a7\u019f\u0001\u0000\u0000\u0000\u01a7\u01a8"+
		"\u0001\u0000\u0000\u0000\u01a8\u01a9\u0001\u0000\u0000\u0000\u01a9\u01ab"+
		"\u0005(\u0000\u0000\u01aa\u018a\u0001\u0000\u0000\u0000\u01aa\u018c\u0001"+
		"\u0000\u0000\u0000\u01aa\u018e\u0001\u0000\u0000\u0000\u01aa\u019c\u0001"+
		"\u0000\u0000\u0000\u01ab+\u0001\u0000\u0000\u0000\u01ac\u01b3\u0005\u001b"+
		"\u0000\u0000\u01ad\u01b3\u0005\u001c\u0000\u0000\u01ae\u01b3\u0005\u001d"+
		"\u0000\u0000\u01af\u01b3\u0005\u001e\u0000\u0000\u01b0\u01b3\u0005\u001f"+
		"\u0000\u0000\u01b1\u01b3\u0005 \u0000\u0000\u01b2\u01ac\u0001\u0000\u0000"+
		"\u0000\u01b2\u01ad\u0001\u0000\u0000\u0000\u01b2\u01ae\u0001\u0000\u0000"+
		"\u0000\u01b2\u01af\u0001\u0000\u0000\u0000\u01b2\u01b0\u0001\u0000\u0000"+
		"\u0000\u01b2\u01b1\u0001\u0000\u0000\u0000\u01b3-\u0001\u0000\u0000\u0000"+
		"\u01b4\u01bd\u0003\u0016\u000b\u0000\u01b5\u01bd\u0003\b\u0004\u0000\u01b6"+
		"\u01bd\u0003@ \u0000\u01b7\u01bd\u0003B!\u0000\u01b8\u01bd\u0003D\"\u0000"+
		"\u01b9\u01bd\u0003<\u001e\u0000\u01ba\u01bd\u0003F#\u0000\u01bb\u01bd"+
		"\u0005\u0006\u0000\u0000\u01bc\u01b4\u0001\u0000\u0000\u0000\u01bc\u01b5"+
		"\u0001\u0000\u0000\u0000\u01bc\u01b6\u0001\u0000\u0000\u0000\u01bc\u01b7"+
		"\u0001\u0000\u0000\u0000\u01bc\u01b8\u0001\u0000\u0000\u0000\u01bc\u01b9"+
		"\u0001\u0000\u0000\u0000\u01bc\u01ba\u0001\u0000\u0000\u0000\u01bc\u01bb"+
		"\u0001\u0000\u0000\u0000\u01bd/\u0001\u0000\u0000\u0000\u01be\u01c7\u0003"+
		"2\u0019\u0000\u01bf\u01c7\u0003\u0016\u000b\u0000\u01c0\u01c7\u0003\b"+
		"\u0004\u0000\u01c1\u01c7\u0003@ \u0000\u01c2\u01c7\u0003B!\u0000\u01c3"+
		"\u01c7\u0003D\"\u0000\u01c4\u01c7\u0003<\u001e\u0000\u01c5\u01c7\u0003"+
		"F#\u0000\u01c6\u01be\u0001\u0000\u0000\u0000\u01c6\u01bf\u0001\u0000\u0000"+
		"\u0000\u01c6\u01c0\u0001\u0000\u0000\u0000\u01c6\u01c1\u0001\u0000\u0000"+
		"\u0000\u01c6\u01c2\u0001\u0000\u0000\u0000\u01c6\u01c3\u0001\u0000\u0000"+
		"\u0000\u01c6\u01c4\u0001\u0000\u0000\u0000\u01c6\u01c5\u0001\u0000\u0000"+
		"\u0000\u01c71\u0001\u0000\u0000\u0000\u01c8\u01c9\u0005\u0001\u0000\u0000"+
		"\u01c9\u01ca\u0003\b\u0004\u0000\u01ca\u01cb\u0005\u0002\u0000\u0000\u01cb"+
		"\u01d3\u00032\u0019\u0000\u01cc\u01cd\u0005\u0004\u0000\u0000\u01cd\u01ce"+
		"\u0003\b\u0004\u0000\u01ce\u01cf\u0005\u0002\u0000\u0000\u01cf\u01d0\u0003"+
		"2\u0019\u0000\u01d0\u01d2\u0001\u0000\u0000\u0000\u01d1\u01cc\u0001\u0000"+
		"\u0000\u0000\u01d2\u01d5\u0001\u0000\u0000\u0000\u01d3\u01d1\u0001\u0000"+
		"\u0000\u0000\u01d3\u01d4\u0001\u0000\u0000\u0000\u01d4\u01d6\u0001\u0000"+
		"\u0000\u0000\u01d5\u01d3\u0001\u0000\u0000\u0000\u01d6\u01d7\u0005\u0003"+
		"\u0000\u0000\u01d7\u01d8\u00032\u0019\u0000\u01d8\u01d9\u0005\u0005\u0000"+
		"\u0000\u01d9\u01f0\u0001\u0000\u0000\u0000\u01da\u01db\u0005\u0001\u0000"+
		"\u0000\u01db\u01dc\u0005\'\u0000\u0000\u01dc\u01dd\u0003\b\u0004\u0000"+
		"\u01dd\u01de\u0007\u0005\u0000\u0000\u01de\u01e6\u00032\u0019\u0000\u01df"+
		"\u01e0\u0007\u0005\u0000\u0000\u01e0\u01e1\u0003\b\u0004\u0000\u01e1\u01e2"+
		"\u0007\u0005\u0000\u0000\u01e2\u01e3\u00032\u0019\u0000\u01e3\u01e5\u0001"+
		"\u0000\u0000\u0000\u01e4\u01df\u0001\u0000\u0000\u0000\u01e5\u01e8\u0001"+
		"\u0000\u0000\u0000\u01e6\u01e4\u0001\u0000\u0000\u0000\u01e6\u01e7\u0001"+
		"\u0000\u0000\u0000\u01e7\u01e9\u0001\u0000\u0000\u0000\u01e8\u01e6\u0001"+
		"\u0000\u0000\u0000\u01e9\u01ea\u0007\u0005\u0000\u0000\u01ea\u01eb\u0003"+
		"2\u0019\u0000\u01eb\u01ec\u0005(\u0000\u0000\u01ec\u01f0\u0001\u0000\u0000"+
		"\u0000\u01ed\u01f0\u00034\u001a\u0000\u01ee\u01f0\u0003(\u0014\u0000\u01ef"+
		"\u01c8\u0001\u0000\u0000\u0000\u01ef\u01da\u0001\u0000\u0000\u0000\u01ef"+
		"\u01ed\u0001\u0000\u0000\u0000\u01ef\u01ee\u0001\u0000\u0000\u0000\u01f0"+
		"3\u0001\u0000\u0000\u0000\u01f1\u01f2\u00036\u001b\u0000\u01f2\u01f3\u0005"+
		"\'\u0000\u0000\u01f3\u01f4\u00032\u0019\u0000\u01f4\u01f5\u0005(\u0000"+
		"\u0000\u01f55\u0001\u0000\u0000\u0000\u01f6\u01fe\u0005;\u0000\u0000\u01f7"+
		"\u01fe\u0005<\u0000\u0000\u01f8\u01fe\u0005=\u0000\u0000\u01f9\u01fe\u0005"+
		">\u0000\u0000\u01fa\u01fe\u0005?\u0000\u0000\u01fb\u01fe\u0005@\u0000"+
		"\u0000\u01fc\u01fe\u0005A\u0000\u0000\u01fd\u01f6\u0001\u0000\u0000\u0000"+
		"\u01fd\u01f7\u0001\u0000\u0000\u0000\u01fd\u01f8\u0001\u0000\u0000\u0000"+
		"\u01fd\u01f9\u0001\u0000\u0000\u0000\u01fd\u01fa\u0001\u0000\u0000\u0000"+
		"\u01fd\u01fb\u0001\u0000\u0000\u0000\u01fd\u01fc\u0001\u0000\u0000\u0000"+
		"\u01fe7\u0001\u0000\u0000\u0000\u01ff\u022e\u0007\u0006\u0000\u0000\u0200"+
		"\u0201\u0005\u0001\u0000\u0000\u0201\u0202\u0003\b\u0004\u0000\u0202\u0203"+
		"\u0005\u0002\u0000\u0000\u0203\u020b\u0003\b\u0004\u0000\u0204\u0205\u0005"+
		"\u0004\u0000\u0000\u0205\u0206\u0003\b\u0004\u0000\u0206\u0207\u0005\u0002"+
		"\u0000\u0000\u0207\u0208\u0003\b\u0004\u0000\u0208\u020a\u0001\u0000\u0000"+
		"\u0000\u0209\u0204\u0001\u0000\u0000\u0000\u020a\u020d\u0001\u0000\u0000"+
		"\u0000\u020b\u0209\u0001\u0000\u0000\u0000\u020b\u020c\u0001\u0000\u0000"+
		"\u0000\u020c\u020e\u0001\u0000\u0000\u0000\u020d\u020b\u0001\u0000\u0000"+
		"\u0000\u020e\u020f\u0005\u0003\u0000\u0000\u020f\u0210\u0003\b\u0004\u0000"+
		"\u0210\u0211\u0005\u0005\u0000\u0000\u0211\u022e\u0001\u0000\u0000\u0000"+
		"\u0212\u0213\u0005\u0001\u0000\u0000\u0213\u0214\u0005\'\u0000\u0000\u0214"+
		"\u0215\u0003\b\u0004\u0000\u0215\u0216\u0007\u0005\u0000\u0000\u0216\u021e"+
		"\u0003\b\u0004\u0000\u0217\u0218\u0007\u0005\u0000\u0000\u0218\u0219\u0003"+
		"\b\u0004\u0000\u0219\u021a\u0007\u0005\u0000\u0000\u021a\u021b\u0003\b"+
		"\u0004\u0000\u021b\u021d\u0001\u0000\u0000\u0000\u021c\u0217\u0001\u0000"+
		"\u0000\u0000\u021d\u0220\u0001\u0000\u0000\u0000\u021e\u021c\u0001\u0000"+
		"\u0000\u0000\u021e\u021f\u0001\u0000\u0000\u0000\u021f\u0221\u0001\u0000"+
		"\u0000\u0000\u0220\u021e\u0001\u0000\u0000\u0000\u0221\u0222\u0007\u0005"+
		"\u0000\u0000\u0222\u0223\u0003\b\u0004\u0000\u0223\u0224\u0005(\u0000"+
		"\u0000\u0224\u022e\u0001\u0000\u0000\u0000\u0225\u0227\u0005;\u0000\u0000"+
		"\u0226\u0225\u0001\u0000\u0000\u0000\u0226\u0227\u0001\u0000\u0000\u0000"+
		"\u0227\u0228\u0001\u0000\u0000\u0000\u0228\u022b\u0003(\u0014\u0000\u0229"+
		"\u022a\u0005.\u0000\u0000\u022a\u022c\u00038\u001c\u0000\u022b\u0229\u0001"+
		"\u0000\u0000\u0000\u022b\u022c\u0001\u0000\u0000\u0000\u022c\u022e\u0001"+
		"\u0000\u0000\u0000\u022d\u01ff\u0001\u0000\u0000\u0000\u022d\u0200\u0001"+
		"\u0000\u0000\u0000\u022d\u0212\u0001\u0000\u0000\u0000\u022d\u0226\u0001"+
		"\u0000\u0000\u0000\u022e9\u0001\u0000\u0000\u0000\u022f\u0230\u0005\u0001"+
		"\u0000\u0000\u0230\u0231\u0003\b\u0004\u0000\u0231\u0232\u0005\u0002\u0000"+
		"\u0000\u0232\u023a\u0003\u0016\u000b\u0000\u0233\u0234\u0005\u0004\u0000"+
		"\u0000\u0234\u0235\u0003\b\u0004\u0000\u0235\u0236\u0005\u0002\u0000\u0000"+
		"\u0236\u0237\u0003\u0016\u000b\u0000\u0237\u0239\u0001\u0000\u0000\u0000"+
		"\u0238\u0233\u0001\u0000\u0000\u0000\u0239\u023c\u0001\u0000\u0000\u0000"+
		"\u023a\u0238\u0001\u0000\u0000\u0000\u023a\u023b\u0001\u0000\u0000\u0000"+
		"\u023b\u023d\u0001\u0000\u0000\u0000\u023c\u023a\u0001\u0000\u0000\u0000"+
		"\u023d\u023e\u0005\u0003\u0000\u0000\u023e\u023f\u0003\u0016\u000b\u0000"+
		"\u023f\u0240\u0005\u0005\u0000\u0000\u0240\u025e\u0001\u0000\u0000\u0000"+
		"\u0241\u0242\u0005\u0001\u0000\u0000\u0242\u0243\u0005\'\u0000\u0000\u0243"+
		"\u0244\u0003\b\u0004\u0000\u0244\u0245\u0007\u0005\u0000\u0000\u0245\u024d"+
		"\u0003\u0016\u000b\u0000\u0246\u0247\u0007\u0005\u0000\u0000\u0247\u0248"+
		"\u0003\b\u0004\u0000\u0248\u0249\u0007\u0005\u0000\u0000\u0249\u024a\u0003"+
		"\u0016\u000b\u0000\u024a\u024c\u0001\u0000\u0000\u0000\u024b\u0246\u0001"+
		"\u0000\u0000\u0000\u024c\u024f\u0001\u0000\u0000\u0000\u024d\u024b\u0001"+
		"\u0000\u0000\u0000\u024d\u024e\u0001\u0000\u0000\u0000\u024e\u0250\u0001"+
		"\u0000\u0000\u0000\u024f\u024d\u0001\u0000\u0000\u0000\u0250\u0251\u0007"+
		"\u0005\u0000\u0000\u0251\u0252\u0003\u0016\u000b\u0000\u0252\u0253\u0005"+
		"(\u0000\u0000\u0253\u025e\u0001\u0000\u0000\u0000\u0254\u025e\u00055\u0000"+
		"\u0000\u0255\u0257\u0005<\u0000\u0000\u0256\u0255\u0001\u0000\u0000\u0000"+
		"\u0256\u0257\u0001\u0000\u0000\u0000\u0257\u0258\u0001\u0000\u0000\u0000"+
		"\u0258\u025b\u0003(\u0014\u0000\u0259\u025a\u0005.\u0000\u0000\u025a\u025c"+
		"\u0003:\u001d\u0000\u025b\u0259\u0001\u0000\u0000\u0000\u025b\u025c\u0001"+
		"\u0000\u0000\u0000\u025c\u025e\u0001\u0000\u0000\u0000\u025d\u022f\u0001"+
		"\u0000\u0000\u0000\u025d\u0241\u0001\u0000\u0000\u0000\u025d\u0254\u0001"+
		"\u0000\u0000\u0000\u025d\u0256\u0001\u0000\u0000\u0000\u025e;\u0001\u0000"+
		"\u0000\u0000\u025f\u0264\u0003>\u001f\u0000\u0260\u0261\u0005\u0016\u0000"+
		"\u0000\u0261\u0263\u0003>\u001f\u0000\u0262\u0260\u0001\u0000\u0000\u0000"+
		"\u0263\u0266\u0001\u0000\u0000\u0000\u0264\u0262\u0001\u0000\u0000\u0000"+
		"\u0264\u0265\u0001\u0000\u0000\u0000\u0265=\u0001\u0000\u0000\u0000\u0266"+
		"\u0264\u0001\u0000\u0000\u0000\u0267\u0268\u0005\u0001\u0000\u0000\u0268"+
		"\u0269\u0003\b\u0004\u0000\u0269\u026a\u0005\u0002\u0000\u0000\u026a\u0272"+
		"\u0003<\u001e\u0000\u026b\u026c\u0005\u0004\u0000\u0000\u026c\u026d\u0003"+
		"\b\u0004\u0000\u026d\u026e\u0005\u0002\u0000\u0000\u026e\u026f\u0003<"+
		"\u001e\u0000\u026f\u0271\u0001\u0000\u0000\u0000\u0270\u026b\u0001\u0000"+
		"\u0000\u0000\u0271\u0274\u0001\u0000\u0000\u0000\u0272\u0270\u0001\u0000"+
		"\u0000\u0000\u0272\u0273\u0001\u0000\u0000\u0000\u0273\u0275\u0001\u0000"+
		"\u0000\u0000\u0274\u0272\u0001\u0000\u0000\u0000\u0275\u0276\u0005\u0003"+
		"\u0000\u0000\u0276\u0277\u0003<\u001e\u0000\u0277\u0278\u0005\u0005\u0000"+
		"\u0000\u0278\u0296\u0001\u0000\u0000\u0000\u0279\u027a\u0005\u0001\u0000"+
		"\u0000\u027a\u027b\u0005\'\u0000\u0000\u027b\u027c\u0003\b\u0004\u0000"+
		"\u027c\u027d\u0007\u0005\u0000\u0000\u027d\u0285\u0003<\u001e\u0000\u027e"+
		"\u027f\u0007\u0005\u0000\u0000\u027f\u0280\u0003\b\u0004\u0000\u0280\u0281"+
		"\u0007\u0005\u0000\u0000\u0281\u0282\u0003<\u001e\u0000\u0282\u0284\u0001"+
		"\u0000\u0000\u0000\u0283\u027e\u0001\u0000\u0000\u0000\u0284\u0287\u0001"+
		"\u0000\u0000\u0000\u0285\u0283\u0001\u0000\u0000\u0000\u0285\u0286\u0001"+
		"\u0000\u0000\u0000\u0286\u0288\u0001\u0000\u0000\u0000\u0287\u0285\u0001"+
		"\u0000\u0000\u0000\u0288\u0289\u0007\u0005\u0000\u0000\u0289\u028a\u0003"+
		"<\u001e\u0000\u028a\u028b\u0005(\u0000\u0000\u028b\u0296\u0001\u0000\u0000"+
		"\u0000\u028c\u0296\u00054\u0000\u0000\u028d\u028f\u0005=\u0000\u0000\u028e"+
		"\u028d\u0001\u0000\u0000\u0000\u028e\u028f\u0001\u0000\u0000\u0000\u028f"+
		"\u0290\u0001\u0000\u0000\u0000\u0290\u0293\u0003(\u0014\u0000\u0291\u0292"+
		"\u0005.\u0000\u0000\u0292\u0294\u0003<\u001e\u0000\u0293\u0291\u0001\u0000"+
		"\u0000\u0000\u0293\u0294\u0001\u0000\u0000\u0000\u0294\u0296\u0001\u0000"+
		"\u0000\u0000\u0295\u0267\u0001\u0000\u0000\u0000\u0295\u0279\u0001\u0000"+
		"\u0000\u0000\u0295\u028c\u0001\u0000\u0000\u0000\u0295\u028e\u0001\u0000"+
		"\u0000\u0000\u0296?\u0001\u0000\u0000\u0000\u0297\u0298\u0005\u0001\u0000"+
		"\u0000\u0298\u0299\u0003\b\u0004\u0000\u0299\u029a\u0005\u0002\u0000\u0000"+
		"\u029a\u02a2\u0003@ \u0000\u029b\u029c\u0005\u0004\u0000\u0000\u029c\u029d"+
		"\u0003\b\u0004\u0000\u029d\u029e\u0005\u0002\u0000\u0000\u029e\u029f\u0003"+
		"@ \u0000\u029f\u02a1\u0001\u0000\u0000\u0000\u02a0\u029b\u0001\u0000\u0000"+
		"\u0000\u02a1\u02a4\u0001\u0000\u0000\u0000\u02a2\u02a0\u0001\u0000\u0000"+
		"\u0000\u02a2\u02a3\u0001\u0000\u0000\u0000\u02a3\u02a5\u0001\u0000\u0000"+
		"\u0000\u02a4\u02a2\u0001\u0000\u0000\u0000\u02a5\u02a6\u0005\u0003\u0000"+
		"\u0000\u02a6\u02a7\u0003@ \u0000\u02a7\u02a8\u0005\u0005\u0000\u0000\u02a8"+
		"\u02c7\u0001\u0000\u0000\u0000\u02a9\u02aa\u0005\u0001\u0000\u0000\u02aa"+
		"\u02ab\u0005\'\u0000\u0000\u02ab\u02ac\u0003\b\u0004\u0000\u02ac\u02ad"+
		"\u0007\u0005\u0000\u0000\u02ad\u02b5\u0003@ \u0000\u02ae\u02af\u0007\u0005"+
		"\u0000\u0000\u02af\u02b0\u0003\b\u0004\u0000\u02b0\u02b1\u0007\u0005\u0000"+
		"\u0000\u02b1\u02b2\u0003@ \u0000\u02b2\u02b4\u0001\u0000\u0000\u0000\u02b3"+
		"\u02ae\u0001\u0000\u0000\u0000\u02b4\u02b7\u0001\u0000\u0000\u0000\u02b5"+
		"\u02b3\u0001\u0000\u0000\u0000\u02b5\u02b6\u0001\u0000\u0000\u0000\u02b6"+
		"\u02b8\u0001\u0000\u0000\u0000\u02b7\u02b5\u0001\u0000\u0000\u0000\u02b8"+
		"\u02b9\u0007\u0005\u0000\u0000\u02b9\u02ba\u0003@ \u0000\u02ba\u02bb\u0005"+
		"(\u0000\u0000\u02bb\u02c7\u0001\u0000\u0000\u0000\u02bc\u02c7\u00057\u0000"+
		"\u0000\u02bd\u02c7\u0005$\u0000\u0000\u02be\u02c0\u0005>\u0000\u0000\u02bf"+
		"\u02be\u0001\u0000\u0000\u0000\u02bf\u02c0\u0001\u0000\u0000\u0000\u02c0"+
		"\u02c1\u0001\u0000\u0000\u0000\u02c1\u02c4\u0003(\u0014\u0000\u02c2\u02c3"+
		"\u0005.\u0000\u0000\u02c3\u02c5\u0003@ \u0000\u02c4\u02c2\u0001\u0000"+
		"\u0000\u0000\u02c4\u02c5\u0001\u0000\u0000\u0000\u02c5\u02c7\u0001\u0000"+
		"\u0000\u0000\u02c6\u0297\u0001\u0000\u0000\u0000\u02c6\u02a9\u0001\u0000"+
		"\u0000\u0000\u02c6\u02bc\u0001\u0000\u0000\u0000\u02c6\u02bd\u0001\u0000"+
		"\u0000\u0000\u02c6\u02bf\u0001\u0000\u0000\u0000\u02c7A\u0001\u0000\u0000"+
		"\u0000\u02c8\u02c9\u0005\u0001\u0000\u0000\u02c9\u02ca\u0003\b\u0004\u0000"+
		"\u02ca\u02cb\u0005\u0002\u0000\u0000\u02cb\u02d3\u0003B!\u0000\u02cc\u02cd"+
		"\u0005\u0004\u0000\u0000\u02cd\u02ce\u0003\b\u0004\u0000\u02ce\u02cf\u0005"+
		"\u0002\u0000\u0000\u02cf\u02d0\u0003B!\u0000\u02d0\u02d2\u0001\u0000\u0000"+
		"\u0000\u02d1\u02cc\u0001\u0000\u0000\u0000\u02d2\u02d5\u0001\u0000\u0000"+
		"\u0000\u02d3\u02d1\u0001\u0000\u0000\u0000\u02d3\u02d4\u0001\u0000\u0000"+
		"\u0000\u02d4\u02d6\u0001\u0000\u0000\u0000\u02d5\u02d3\u0001\u0000\u0000"+
		"\u0000\u02d6\u02d7\u0005\u0003\u0000\u0000\u02d7\u02d8\u0003B!\u0000\u02d8"+
		"\u02d9\u0005\u0005\u0000\u0000\u02d9\u02f8\u0001\u0000\u0000\u0000\u02da"+
		"\u02db\u0005\u0001\u0000\u0000\u02db\u02dc\u0005\'\u0000\u0000\u02dc\u02dd"+
		"\u0003\b\u0004\u0000\u02dd\u02de\u0007\u0005\u0000\u0000\u02de\u02e6\u0003"+
		"B!\u0000\u02df\u02e0\u0007\u0005\u0000\u0000\u02e0\u02e1\u0003\b\u0004"+
		"\u0000\u02e1\u02e2\u0007\u0005\u0000\u0000\u02e2\u02e3\u0003B!\u0000\u02e3"+
		"\u02e5\u0001\u0000\u0000\u0000\u02e4\u02df\u0001\u0000\u0000\u0000\u02e5"+
		"\u02e8\u0001\u0000\u0000\u0000\u02e6\u02e4\u0001\u0000\u0000\u0000\u02e6"+
		"\u02e7\u0001\u0000\u0000\u0000\u02e7\u02e9\u0001\u0000\u0000\u0000\u02e8"+
		"\u02e6\u0001\u0000\u0000\u0000\u02e9\u02ea\u0007\u0005\u0000\u0000\u02ea"+
		"\u02eb\u0003B!\u0000\u02eb\u02ec\u0005(\u0000\u0000\u02ec\u02f8\u0001"+
		"\u0000\u0000\u0000\u02ed\u02f8\u00058\u0000\u0000\u02ee\u02f8\u0005%\u0000"+
		"\u0000\u02ef\u02f1\u0005?\u0000\u0000\u02f0\u02ef\u0001\u0000\u0000\u0000"+
		"\u02f0\u02f1\u0001\u0000\u0000\u0000\u02f1\u02f2\u0001\u0000\u0000\u0000"+
		"\u02f2\u02f5\u0003(\u0014\u0000\u02f3\u02f4\u0005.\u0000\u0000\u02f4\u02f6"+
		"\u0003B!\u0000\u02f5\u02f3\u0001\u0000\u0000\u0000\u02f5\u02f6\u0001\u0000"+
		"\u0000\u0000\u02f6\u02f8\u0001\u0000\u0000\u0000\u02f7\u02c8\u0001\u0000"+
		"\u0000\u0000\u02f7\u02da\u0001\u0000\u0000\u0000\u02f7\u02ed\u0001\u0000"+
		"\u0000\u0000\u02f7\u02ee\u0001\u0000\u0000\u0000\u02f7\u02f0\u0001\u0000"+
		"\u0000\u0000\u02f8C\u0001\u0000\u0000\u0000\u02f9\u02fa\u0005\u0001\u0000"+
		"\u0000\u02fa\u02fb\u0003\b\u0004\u0000\u02fb\u02fc\u0005\u0002\u0000\u0000"+
		"\u02fc\u0304\u0003D\"\u0000\u02fd\u02fe\u0005\u0004\u0000\u0000\u02fe"+
		"\u02ff\u0003\b\u0004\u0000\u02ff\u0300\u0005\u0002\u0000\u0000\u0300\u0301"+
		"\u0003D\"\u0000\u0301\u0303\u0001\u0000\u0000\u0000\u0302\u02fd\u0001"+
		"\u0000\u0000\u0000\u0303\u0306\u0001\u0000\u0000\u0000\u0304\u0302\u0001"+
		"\u0000\u0000\u0000\u0304\u0305\u0001\u0000\u0000\u0000\u0305\u0307\u0001"+
		"\u0000\u0000\u0000\u0306\u0304\u0001\u0000\u0000\u0000\u0307\u0308\u0005"+
		"\u0003\u0000\u0000\u0308\u0309\u0003D\"\u0000\u0309\u030a\u0005\u0005"+
		"\u0000\u0000\u030a\u032c\u0001\u0000\u0000\u0000\u030b\u030c\u0005\u0001"+
		"\u0000\u0000\u030c\u030d\u0005\'\u0000\u0000\u030d\u030e\u0003\b\u0004"+
		"\u0000\u030e\u030f\u0007\u0005\u0000\u0000\u030f\u0317\u0003D\"\u0000"+
		"\u0310\u0311\u0007\u0005\u0000\u0000\u0311\u0312\u0003\b\u0004\u0000\u0312"+
		"\u0313\u0007\u0005\u0000\u0000\u0313\u0314\u0003D\"\u0000\u0314\u0316"+
		"\u0001\u0000\u0000\u0000\u0315\u0310\u0001\u0000\u0000\u0000\u0316\u0319"+
		"\u0001\u0000\u0000\u0000\u0317\u0315\u0001\u0000\u0000\u0000\u0317\u0318"+
		"\u0001\u0000\u0000\u0000\u0318\u031a\u0001\u0000\u0000\u0000\u0319\u0317"+
		"\u0001\u0000\u0000\u0000\u031a\u031b\u0007\u0005\u0000\u0000\u031b\u031c"+
		"\u0003D\"\u0000\u031c\u031d\u0005(\u0000\u0000\u031d\u032c\u0001\u0000"+
		"\u0000\u0000\u031e\u0320\u0005:\u0000\u0000\u031f\u0321\u00059\u0000\u0000"+
		"\u0320\u031f\u0001\u0000\u0000\u0000\u0320\u0321\u0001\u0000\u0000\u0000"+
		"\u0321\u032c\u0001\u0000\u0000\u0000\u0322\u032c\u0005&\u0000\u0000\u0323"+
		"\u0325\u0005@\u0000\u0000\u0324\u0323\u0001\u0000\u0000\u0000\u0324\u0325"+
		"\u0001\u0000\u0000\u0000\u0325\u0326\u0001\u0000\u0000\u0000\u0326\u0329"+
		"\u0003(\u0014\u0000\u0327\u0328\u0005.\u0000\u0000\u0328\u032a\u0003D"+
		"\"\u0000\u0329\u0327\u0001\u0000\u0000\u0000\u0329\u032a\u0001\u0000\u0000"+
		"\u0000\u032a\u032c\u0001\u0000\u0000\u0000\u032b\u02f9\u0001\u0000\u0000"+
		"\u0000\u032b\u030b\u0001\u0000\u0000\u0000\u032b\u031e\u0001\u0000\u0000"+
		"\u0000\u032b\u0322\u0001\u0000\u0000\u0000\u032b\u0324\u0001\u0000\u0000"+
		"\u0000\u032cE\u0001\u0000\u0000\u0000\u032d\u032e\u0005\u0001\u0000\u0000"+
		"\u032e\u032f\u0003\b\u0004\u0000\u032f\u0330\u0005\u0002\u0000\u0000\u0330"+
		"\u0338\u0003F#\u0000\u0331\u0332\u0005\u0004\u0000\u0000\u0332\u0333\u0003"+
		"\b\u0004\u0000\u0333\u0334\u0005\u0002\u0000\u0000\u0334\u0335\u0003F"+
		"#\u0000\u0335\u0337\u0001\u0000\u0000\u0000\u0336\u0331\u0001\u0000\u0000"+
		"\u0000\u0337\u033a\u0001\u0000\u0000\u0000\u0338\u0336\u0001\u0000\u0000"+
		"\u0000\u0338\u0339\u0001\u0000\u0000\u0000\u0339\u033b\u0001\u0000\u0000"+
		"\u0000\u033a\u0338\u0001\u0000\u0000\u0000\u033b\u033c\u0005\u0003\u0000"+
		"\u0000\u033c\u033d\u0003F#\u0000\u033d\u033e\u0005\u0005\u0000\u0000\u033e"+
		"\u0366\u0001\u0000\u0000\u0000\u033f\u0340\u0005\u0001\u0000\u0000\u0340"+
		"\u0341\u0005\'\u0000\u0000\u0341\u0342\u0003\b\u0004\u0000\u0342\u0343"+
		"\u0007\u0005\u0000\u0000\u0343\u034b\u0003F#\u0000\u0344\u0345\u0007\u0005"+
		"\u0000\u0000\u0345\u0346\u0003\b\u0004\u0000\u0346\u0347\u0007\u0005\u0000"+
		"\u0000\u0347\u0348\u0003F#\u0000\u0348\u034a\u0001\u0000\u0000\u0000\u0349"+
		"\u0344\u0001\u0000\u0000\u0000\u034a\u034d\u0001\u0000\u0000\u0000\u034b"+
		"\u0349\u0001\u0000\u0000\u0000\u034b\u034c\u0001\u0000\u0000\u0000\u034c"+
		"\u034e\u0001\u0000\u0000\u0000\u034d\u034b\u0001\u0000\u0000\u0000\u034e"+
		"\u034f\u0007\u0005\u0000\u0000\u034f\u0350\u0003F#\u0000\u0350\u0351\u0005"+
		"(\u0000\u0000\u0351\u0366\u0001\u0000\u0000\u0000\u0352\u0353\u0005)\u0000"+
		"\u0000\u0353\u0358\u0003.\u0017\u0000\u0354\u0355\u0005+\u0000\u0000\u0355"+
		"\u0357\u0003.\u0017\u0000\u0356\u0354\u0001\u0000\u0000\u0000\u0357\u035a"+
		"\u0001\u0000\u0000\u0000\u0358\u0356\u0001\u0000\u0000\u0000\u0358\u0359"+
		"\u0001\u0000\u0000\u0000\u0359\u035b\u0001\u0000\u0000\u0000\u035a\u0358"+
		"\u0001\u0000\u0000\u0000\u035b\u035c\u0005*\u0000\u0000\u035c\u0366\u0001"+
		"\u0000\u0000\u0000\u035d\u035f\u0005A\u0000\u0000\u035e\u035d\u0001\u0000"+
		"\u0000\u0000\u035e\u035f\u0001\u0000\u0000\u0000\u035f\u0360\u0001\u0000"+
		"\u0000\u0000\u0360\u0363\u0003(\u0014\u0000\u0361\u0362\u0005.\u0000\u0000"+
		"\u0362\u0364\u0003F#\u0000\u0363\u0361\u0001\u0000\u0000\u0000\u0363\u0364"+
		"\u0001\u0000\u0000\u0000\u0364\u0366\u0001\u0000\u0000\u0000\u0365\u032d"+
		"\u0001\u0000\u0000\u0000\u0365\u033f\u0001\u0000\u0000\u0000\u0365\u0352"+
		"\u0001\u0000\u0000\u0000\u0365\u035e\u0001\u0000\u0000\u0000\u0366G\u0001"+
		"\u0000\u0000\u0000\u0367\u0368\u0005)\u0000\u0000\u0368\u036d\u00053\u0000"+
		"\u0000\u0369\u036a\u0005+\u0000\u0000\u036a\u036c\u00053\u0000\u0000\u036b"+
		"\u0369\u0001\u0000\u0000\u0000\u036c\u036f\u0001\u0000\u0000\u0000\u036d"+
		"\u036b\u0001\u0000\u0000\u0000\u036d\u036e\u0001\u0000\u0000\u0000\u036e"+
		"\u0370\u0001\u0000\u0000\u0000\u036f\u036d\u0001\u0000\u0000\u0000\u0370"+
		"\u0371\u0005*\u0000\u0000\u0371I\u0001\u0000\u0000\u0000IMV]mv~\u0085"+
		"\u0121\u0128\u012e\u0135\u013e\u0146\u014c\u0153\u0159\u015f\u0170\u0179"+
		"\u017c\u0185\u0188\u0196\u0199\u01a4\u01a7\u01aa\u01b2\u01bc\u01c6\u01d3"+
		"\u01e6\u01ef\u01fd\u020b\u021e\u0226\u022b\u022d\u023a\u024d\u0256\u025b"+
		"\u025d\u0264\u0272\u0285\u028e\u0293\u0295\u02a2\u02b5\u02bf\u02c4\u02c6"+
		"\u02d3\u02e6\u02f0\u02f5\u02f7\u0304\u0317\u0320\u0324\u0329\u032b\u0338"+
		"\u034b\u0358\u035e\u0363\u0365\u036d";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}