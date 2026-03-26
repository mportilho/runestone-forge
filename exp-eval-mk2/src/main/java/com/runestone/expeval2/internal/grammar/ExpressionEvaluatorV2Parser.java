// Generated from com/runestone/expeval2/internal/grammar/ExpressionEvaluatorV2.g4 by ANTLR 4.13.1
package com.runestone.expeval2.internal.grammar;
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
		IF=1, THEN=2, ELSE=3, ELSEIF=4, ENDIF=5, NULL=6, AND=7, OR=8, XOR=9, XNOR=10, 
		NAND=11, NOR=12, TRUE=13, FALSE=14, MULT=15, DIV=16, PLUS=17, MINUS=18, 
		PERCENT=19, MODULO=20, MODULUS=21, CONCAT=22, EXCLAMATION=23, EXPONENTIATION=24, 
		ROOT=25, SQRT=26, GT=27, GE=28, LT=29, LE=30, EQ=31, NEQ=32, NOT=33, REGEX_MATCH=34, 
		REGEX_NOT_MATCH=35, NOW_DATE=36, NOW_TIME=37, NOW_DATETIME=38, LPAREN=39, 
		RPAREN=40, LBRACKET=41, RBRACKET=42, COMMA=43, SEMI=44, PERIOD=45, NULLCOALESCE=46, 
		SAFE_NAV=47, IDENTIFIER=48, STRING=49, NUMBER=50, POSITIVE=51, DATE=52, 
		TIME=53, TIME_OFFSET=54, DATETIME=55, BOOLEAN_TYPE=56, NUMBER_TYPE=57, 
		STRING_TYPE=58, DATE_TYPE=59, TIME_TYPE=60, DATETIME_TYPE=61, VECTOR_TYPE=62, 
		LINE_COMMENT=63, BLOCK_COMMENT=64, WS=65, ERROR_CHAR=66;
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
			"','", "';'", "'.'", "'??'", "'?.'", null, null, null, null, null, null, 
			null, null, "'<bool>'", "'<number>'", "'<text>'", "'<date>'", "'<time>'", 
			"'<datetime>'", "'<vector>'"
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
			"SAFE_NAV", "IDENTIFIER", "STRING", "NUMBER", "POSITIVE", "DATE", "TIME", 
			"TIME_OFFSET", "DATETIME", "BOOLEAN_TYPE", "NUMBER_TYPE", "STRING_TYPE", 
			"DATE_TYPE", "TIME_TYPE", "DATETIME_TYPE", "VECTOR_TYPE", "LINE_COMMENT", 
			"BLOCK_COMMENT", "WS", "ERROR_CHAR"
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
	public static class RegexMatchOperationContext extends LogicalComparisonExpressionContext {
		public StringConcatExpressionContext stringConcatExpression() {
			return getRuleContext(StringConcatExpressionContext.class,0);
		}
		public TerminalNode REGEX_MATCH() { return getToken(ExpressionEvaluatorV2Parser.REGEX_MATCH, 0); }
		public TerminalNode STRING() { return getToken(ExpressionEvaluatorV2Parser.STRING, 0); }
		public RegexMatchOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterRegexMatchOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitRegexMatchOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitRegexMatchOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RegexNotMatchOperationContext extends LogicalComparisonExpressionContext {
		public StringConcatExpressionContext stringConcatExpression() {
			return getRuleContext(StringConcatExpressionContext.class,0);
		}
		public TerminalNode REGEX_NOT_MATCH() { return getToken(ExpressionEvaluatorV2Parser.REGEX_NOT_MATCH, 0); }
		public TerminalNode STRING() { return getToken(ExpressionEvaluatorV2Parser.STRING, 0); }
		public RegexNotMatchOperationContext(LogicalComparisonExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterRegexNotMatchOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitRegexNotMatchOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitRegexNotMatchOperation(this);
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
			setState(163);
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
			setState(165);
			logicalNotExpression();
			setState(170);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 7680L) != 0)) {
				{
				{
				setState(166);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 7680L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(167);
				logicalNotExpression();
				}
				}
				setState(172);
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
			setState(176);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXCLAMATION:
			case NOT:
				_localctx = new LogicalNotOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(173);
				_la = _input.LA(1);
				if ( !(_la==EXCLAMATION || _la==NOT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(174);
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
				setState(175);
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
			setState(183);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				_localctx = new LogicalExpressionParenthesisOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(178);
				match(LPAREN);
				setState(179);
				logicalExpression();
				setState(180);
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
				setState(182);
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
			setState(185);
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
			setState(187);
			multiplicationExpression();
			setState(192);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS || _la==MINUS) {
				{
				{
				setState(188);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(189);
				multiplicationExpression();
				}
				}
				setState(194);
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
			setState(195);
			unaryExpression();
			setState(200);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1146880L) != 0)) {
				{
				{
				setState(196);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1146880L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(197);
				unaryExpression();
				}
				}
				setState(202);
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
			setState(206);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MINUS:
				_localctx = new UnaryMinusOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(203);
				match(MINUS);
				setState(204);
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
				setState(205);
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
			setState(208);
			exponentiationExpression();
			setState(213);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(209);
					match(ROOT);
					setState(210);
					exponentiationExpression();
					}
					} 
				}
				setState(215);
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
			setState(216);
			postfixExpression();
			setState(219);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXPONENTIATION) {
				{
				setState(217);
				match(EXPONENTIATION);
				setState(218);
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
			setState(221);
			primaryMathExpression();
			setState(225);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PERCENT || _la==EXCLAMATION) {
				{
				{
				setState(222);
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
				setState(227);
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
			setState(242);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				_localctx = new MathExpressionParenthesisOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(228);
				match(LPAREN);
				setState(229);
				mathExpression();
				setState(230);
				match(RPAREN);
				}
				break;
			case SQRT:
				_localctx = new SquareRootOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(232);
				match(SQRT);
				setState(233);
				match(LPAREN);
				setState(234);
				mathExpression();
				setState(235);
				match(RPAREN);
				}
				break;
			case MODULUS:
				_localctx = new ModulusOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(237);
				match(MODULUS);
				setState(238);
				mathExpression();
				setState(239);
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
				setState(241);
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
			setState(244);
			match(IDENTIFIER);
			setState(245);
			match(LPAREN);
			setState(254);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 9202827602038120514L) != 0)) {
				{
				setState(246);
				allEntityTypes();
				setState(251);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA || _la==SEMI) {
					{
					{
					setState(247);
					_la = _input.LA(1);
					if ( !(_la==COMMA || _la==SEMI) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(248);
					allEntityTypes();
					}
					}
					setState(253);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(256);
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
		public List<MemberChainContext> memberChain() {
			return getRuleContexts(MemberChainContext.class);
		}
		public MemberChainContext memberChain(int i) {
			return getRuleContext(MemberChainContext.class,i);
		}
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
		int _la;
		try {
			setState(266);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				_localctx = new FunctionReferenceTargetContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(258);
				function();
				}
				break;
			case 2:
				_localctx = new IdentifierReferenceTargetContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(259);
				match(IDENTIFIER);
				setState(263);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==PERIOD || _la==SAFE_NAV) {
					{
					{
					setState(260);
					memberChain();
					}
					}
					setState(265);
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
		public TerminalNode SAFE_NAV() { return getToken(ExpressionEvaluatorV2Parser.SAFE_NAV, 0); }
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
		public SafeMethodCallAccessContext(MemberChainContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterSafeMethodCallAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitSafeMethodCallAccess(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitSafeMethodCallAccess(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MethodCallAccessContext extends MemberChainContext {
		public TerminalNode PERIOD() { return getToken(ExpressionEvaluatorV2Parser.PERIOD, 0); }
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
		public MethodCallAccessContext(MemberChainContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterMethodCallAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitMethodCallAccess(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitMethodCallAccess(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PropertyAccessContext extends MemberChainContext {
		public TerminalNode PERIOD() { return getToken(ExpressionEvaluatorV2Parser.PERIOD, 0); }
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorV2Parser.IDENTIFIER, 0); }
		public PropertyAccessContext(MemberChainContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterPropertyAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitPropertyAccess(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitPropertyAccess(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SafePropertyAccessContext extends MemberChainContext {
		public TerminalNode SAFE_NAV() { return getToken(ExpressionEvaluatorV2Parser.SAFE_NAV, 0); }
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorV2Parser.IDENTIFIER, 0); }
		public SafePropertyAccessContext(MemberChainContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterSafePropertyAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitSafePropertyAccess(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitSafePropertyAccess(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MemberChainContext memberChain() throws RecognitionException {
		MemberChainContext _localctx = new MemberChainContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_memberChain);
		int _la;
		try {
			setState(300);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				_localctx = new PropertyAccessContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(268);
				match(PERIOD);
				setState(269);
				match(IDENTIFIER);
				}
				break;
			case 2:
				_localctx = new SafePropertyAccessContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(270);
				match(SAFE_NAV);
				setState(271);
				match(IDENTIFIER);
				}
				break;
			case 3:
				_localctx = new MethodCallAccessContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(272);
				match(PERIOD);
				setState(273);
				match(IDENTIFIER);
				setState(274);
				match(LPAREN);
				setState(283);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 9202827602038120514L) != 0)) {
					{
					setState(275);
					allEntityTypes();
					setState(280);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(276);
						match(COMMA);
						setState(277);
						allEntityTypes();
						}
						}
						setState(282);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(285);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new SafeMethodCallAccessContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(286);
				match(SAFE_NAV);
				setState(287);
				match(IDENTIFIER);
				setState(288);
				match(LPAREN);
				setState(297);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 9202827602038120514L) != 0)) {
					{
					setState(289);
					allEntityTypes();
					setState(294);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(290);
						match(COMMA);
						setState(291);
						allEntityTypes();
						}
						}
						setState(296);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(299);
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
		enterRule(_localctx, 44, RULE_comparisonOperator);
		try {
			setState(308);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case GT:
				_localctx = new GreaterThanOperatorContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(302);
				match(GT);
				}
				break;
			case GE:
				_localctx = new GreaterThanOrEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(303);
				match(GE);
				}
				break;
			case LT:
				_localctx = new LessThanOperatorContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(304);
				match(LT);
				}
				break;
			case LE:
				_localctx = new LessThanOrEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(305);
				match(LE);
				}
				break;
			case EQ:
				_localctx = new EqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(306);
				match(EQ);
				}
				break;
			case NEQ:
				_localctx = new NotEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(307);
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
		public StringConcatExpressionContext stringConcatExpression() {
			return getRuleContext(StringConcatExpressionContext.class,0);
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
	public static class NullEntityTypeContext extends AllEntityTypesContext {
		public TerminalNode NULL() { return getToken(ExpressionEvaluatorV2Parser.NULL, 0); }
		public NullEntityTypeContext(AllEntityTypesContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterNullEntityType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitNullEntityType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitNullEntityType(this);
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
		enterRule(_localctx, 46, RULE_allEntityTypes);
		try {
			setState(318);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				_localctx = new MathEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(310);
				mathExpression();
				}
				break;
			case 2:
				_localctx = new LogicalEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(311);
				logicalExpression();
				}
				break;
			case 3:
				_localctx = new DateEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(312);
				dateEntity();
				}
				break;
			case 4:
				_localctx = new TimeEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(313);
				timeEntity();
				}
				break;
			case 5:
				_localctx = new DateTimeEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(314);
				dateTimeEntity();
				}
				break;
			case 6:
				_localctx = new StringEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(315);
				stringConcatExpression();
				}
				break;
			case 7:
				_localctx = new VectorEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(316);
				vectorEntity();
				}
				break;
			case 8:
				_localctx = new NullEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(317);
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
		public StringConcatExpressionContext stringConcatExpression() {
			return getRuleContext(StringConcatExpressionContext.class,0);
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
		enterRule(_localctx, 48, RULE_assignmentValue);
		try {
			setState(328);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
			case 1:
				_localctx = new GenericAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(320);
				genericEntity();
				}
				break;
			case 2:
				_localctx = new MathAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(321);
				mathExpression();
				}
				break;
			case 3:
				_localctx = new LogicalAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(322);
				logicalExpression();
				}
				break;
			case 4:
				_localctx = new DateAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(323);
				dateEntity();
				}
				break;
			case 5:
				_localctx = new TimeAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(324);
				timeEntity();
				}
				break;
			case 6:
				_localctx = new DateTimeAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(325);
				dateTimeEntity();
				}
				break;
			case 7:
				_localctx = new StringAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(326);
				stringConcatExpression();
				}
				break;
			case 8:
				_localctx = new VectorAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(327);
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
		enterRule(_localctx, 50, RULE_genericEntity);
		int _la;
		try {
			int _alt;
			setState(369);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				_localctx = new GenericDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(330);
				match(IF);
				setState(331);
				logicalExpression();
				setState(332);
				match(THEN);
				setState(333);
				genericEntity();
				setState(341);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(334);
					match(ELSEIF);
					setState(335);
					logicalExpression();
					setState(336);
					match(THEN);
					setState(337);
					genericEntity();
					}
					}
					setState(343);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(344);
				match(ELSE);
				setState(345);
				genericEntity();
				setState(346);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new GenericFunctionDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(348);
				match(IF);
				setState(349);
				match(LPAREN);
				setState(350);
				logicalExpression();
				setState(351);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(352);
				genericEntity();
				setState(360);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(353);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(354);
						logicalExpression();
						setState(355);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(356);
						genericEntity();
						}
						} 
					}
					setState(362);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
				}
				setState(363);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(364);
				genericEntity();
				setState(365);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new CastExpressionOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(367);
				castExpression();
				}
				break;
			case 4:
				_localctx = new ReferenceTargetOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(368);
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
		enterRule(_localctx, 52, RULE_castExpression);
		try {
			_localctx = new TypeCastOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(371);
			typeHint();
			setState(372);
			match(LPAREN);
			setState(373);
			genericEntity();
			setState(374);
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
		enterRule(_localctx, 54, RULE_typeHint);
		try {
			setState(383);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BOOLEAN_TYPE:
				_localctx = new BooleanTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(376);
				match(BOOLEAN_TYPE);
				}
				break;
			case NUMBER_TYPE:
				_localctx = new NumberTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(377);
				match(NUMBER_TYPE);
				}
				break;
			case STRING_TYPE:
				_localctx = new StringTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(378);
				match(STRING_TYPE);
				}
				break;
			case DATE_TYPE:
				_localctx = new DateTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(379);
				match(DATE_TYPE);
				}
				break;
			case TIME_TYPE:
				_localctx = new TimeTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(380);
				match(TIME_TYPE);
				}
				break;
			case DATETIME_TYPE:
				_localctx = new DateTimeTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(381);
				match(DATETIME_TYPE);
				}
				break;
			case VECTOR_TYPE:
				_localctx = new VectorTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(382);
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
		public TerminalNode NULLCOALESCE() { return getToken(ExpressionEvaluatorV2Parser.NULLCOALESCE, 0); }
		public LogicalEntityContext logicalEntity() {
			return getRuleContext(LogicalEntityContext.class,0);
		}
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
		enterRule(_localctx, 56, RULE_logicalEntity);
		int _la;
		try {
			int _alt;
			setState(431);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				_localctx = new LogicalConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(385);
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
				setState(386);
				match(IF);
				setState(387);
				logicalExpression();
				setState(388);
				match(THEN);
				setState(389);
				logicalExpression();
				setState(397);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(390);
					match(ELSEIF);
					setState(391);
					logicalExpression();
					setState(392);
					match(THEN);
					setState(393);
					logicalExpression();
					}
					}
					setState(399);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(400);
				match(ELSE);
				setState(401);
				logicalExpression();
				setState(402);
				match(ENDIF);
				}
				break;
			case 3:
				_localctx = new LogicalFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(404);
				match(IF);
				setState(405);
				match(LPAREN);
				setState(406);
				logicalExpression();
				setState(407);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(408);
				logicalExpression();
				setState(416);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,35,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
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
						logicalExpression();
						setState(411);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(412);
						logicalExpression();
						}
						} 
					}
					setState(418);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,35,_ctx);
				}
				setState(419);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(420);
				logicalExpression();
				setState(421);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new LogicalReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(424);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==BOOLEAN_TYPE) {
					{
					setState(423);
					match(BOOLEAN_TYPE);
					}
				}

				setState(426);
				referenceTarget();
				setState(429);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(427);
					match(NULLCOALESCE);
					setState(428);
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
		public TerminalNode NULLCOALESCE() { return getToken(ExpressionEvaluatorV2Parser.NULLCOALESCE, 0); }
		public NumericEntityContext numericEntity() {
			return getRuleContext(NumericEntityContext.class,0);
		}
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
		enterRule(_localctx, 58, RULE_numericEntity);
		int _la;
		try {
			int _alt;
			setState(479);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
			case 1:
				_localctx = new MathDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(433);
				match(IF);
				setState(434);
				logicalExpression();
				setState(435);
				match(THEN);
				setState(436);
				mathExpression();
				setState(444);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(437);
					match(ELSEIF);
					setState(438);
					logicalExpression();
					setState(439);
					match(THEN);
					setState(440);
					mathExpression();
					}
					}
					setState(446);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(447);
				match(ELSE);
				setState(448);
				mathExpression();
				setState(449);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new MathFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(451);
				match(IF);
				setState(452);
				match(LPAREN);
				setState(453);
				logicalExpression();
				setState(454);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(455);
				mathExpression();
				setState(463);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(456);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(457);
						logicalExpression();
						setState(458);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(459);
						mathExpression();
						}
						} 
					}
					setState(465);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
				}
				setState(466);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(467);
				mathExpression();
				setState(468);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new NumericConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(470);
				match(NUMBER);
				}
				break;
			case 4:
				_localctx = new NumericReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(472);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NUMBER_TYPE) {
					{
					setState(471);
					match(NUMBER_TYPE);
					}
				}

				setState(474);
				referenceTarget();
				setState(477);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(475);
					match(NULLCOALESCE);
					setState(476);
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
		public List<TerminalNode> CONCAT() { return getTokens(ExpressionEvaluatorV2Parser.CONCAT); }
		public TerminalNode CONCAT(int i) {
			return getToken(ExpressionEvaluatorV2Parser.CONCAT, i);
		}
		public StringConcatenationOperationContext(StringConcatExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).enterStringConcatenationOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorV2Listener ) ((ExpressionEvaluatorV2Listener)listener).exitStringConcatenationOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorV2Visitor ) return ((ExpressionEvaluatorV2Visitor<? extends T>)visitor).visitStringConcatenationOperation(this);
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
			setState(481);
			stringEntity();
			setState(486);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,44,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(482);
					match(CONCAT);
					setState(483);
					stringEntity();
					}
					} 
				}
				setState(488);
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
		public TerminalNode IF() { return getToken(ExpressionEvaluatorV2Parser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorV2Parser.LPAREN, 0); }
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
		public TerminalNode NULLCOALESCE() { return getToken(ExpressionEvaluatorV2Parser.NULLCOALESCE, 0); }
		public StringConcatExpressionContext stringConcatExpression() {
			return getRuleContext(StringConcatExpressionContext.class,0);
		}
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
		public List<StringConcatExpressionContext> stringConcatExpression() {
			return getRuleContexts(StringConcatExpressionContext.class);
		}
		public StringConcatExpressionContext stringConcatExpression(int i) {
			return getRuleContext(StringConcatExpressionContext.class,i);
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
		enterRule(_localctx, 62, RULE_stringEntity);
		int _la;
		try {
			int _alt;
			setState(535);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,49,_ctx) ) {
			case 1:
				_localctx = new StringDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(489);
				match(IF);
				setState(490);
				logicalExpression();
				setState(491);
				match(THEN);
				setState(492);
				stringConcatExpression();
				setState(500);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(493);
					match(ELSEIF);
					setState(494);
					logicalExpression();
					setState(495);
					match(THEN);
					setState(496);
					stringConcatExpression();
					}
					}
					setState(502);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(503);
				match(ELSE);
				setState(504);
				stringConcatExpression();
				setState(505);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new StringFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(507);
				match(IF);
				setState(508);
				match(LPAREN);
				setState(509);
				logicalExpression();
				setState(510);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(511);
				stringConcatExpression();
				setState(519);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,46,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(512);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(513);
						logicalExpression();
						setState(514);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(515);
						stringConcatExpression();
						}
						} 
					}
					setState(521);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,46,_ctx);
				}
				setState(522);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(523);
				stringConcatExpression();
				setState(524);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new StringConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(526);
				match(STRING);
				}
				break;
			case 4:
				_localctx = new StringReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(528);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING_TYPE) {
					{
					setState(527);
					match(STRING_TYPE);
					}
				}

				setState(530);
				referenceTarget();
				setState(533);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(531);
					match(NULLCOALESCE);
					setState(532);
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
		public TerminalNode NULLCOALESCE() { return getToken(ExpressionEvaluatorV2Parser.NULLCOALESCE, 0); }
		public DateEntityContext dateEntity() {
			return getRuleContext(DateEntityContext.class,0);
		}
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
		enterRule(_localctx, 64, RULE_dateEntity);
		int _la;
		try {
			int _alt;
			setState(584);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
			case 1:
				_localctx = new DateDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(537);
				match(IF);
				setState(538);
				logicalExpression();
				setState(539);
				match(THEN);
				setState(540);
				dateEntity();
				setState(548);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(541);
					match(ELSEIF);
					setState(542);
					logicalExpression();
					setState(543);
					match(THEN);
					setState(544);
					dateEntity();
					}
					}
					setState(550);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(551);
				match(ELSE);
				setState(552);
				dateEntity();
				setState(553);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new DateFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(555);
				match(IF);
				setState(556);
				match(LPAREN);
				setState(557);
				logicalExpression();
				setState(558);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(559);
				dateEntity();
				setState(567);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,51,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(560);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(561);
						logicalExpression();
						setState(562);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(563);
						dateEntity();
						}
						} 
					}
					setState(569);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,51,_ctx);
				}
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
				dateEntity();
				setState(572);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new DateConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(574);
				match(DATE);
				}
				break;
			case 4:
				_localctx = new DateCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(575);
				match(NOW_DATE);
				}
				break;
			case 5:
				_localctx = new DateReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(577);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DATE_TYPE) {
					{
					setState(576);
					match(DATE_TYPE);
					}
				}

				setState(579);
				referenceTarget();
				setState(582);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(580);
					match(NULLCOALESCE);
					setState(581);
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
		public TerminalNode NULLCOALESCE() { return getToken(ExpressionEvaluatorV2Parser.NULLCOALESCE, 0); }
		public TimeEntityContext timeEntity() {
			return getRuleContext(TimeEntityContext.class,0);
		}
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
		enterRule(_localctx, 66, RULE_timeEntity);
		int _la;
		try {
			int _alt;
			setState(633);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
			case 1:
				_localctx = new TimeDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(586);
				match(IF);
				setState(587);
				logicalExpression();
				setState(588);
				match(THEN);
				setState(589);
				timeEntity();
				setState(597);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(590);
					match(ELSEIF);
					setState(591);
					logicalExpression();
					setState(592);
					match(THEN);
					setState(593);
					timeEntity();
					}
					}
					setState(599);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(600);
				match(ELSE);
				setState(601);
				timeEntity();
				setState(602);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new TimeFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(604);
				match(IF);
				setState(605);
				match(LPAREN);
				setState(606);
				logicalExpression();
				setState(607);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(608);
				timeEntity();
				setState(616);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,56,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(609);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(610);
						logicalExpression();
						setState(611);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(612);
						timeEntity();
						}
						} 
					}
					setState(618);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,56,_ctx);
				}
				setState(619);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(620);
				timeEntity();
				setState(621);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new TimeConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(623);
				match(TIME);
				}
				break;
			case 4:
				_localctx = new TimeCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(624);
				match(NOW_TIME);
				}
				break;
			case 5:
				_localctx = new TimeReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(626);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TIME_TYPE) {
					{
					setState(625);
					match(TIME_TYPE);
					}
				}

				setState(628);
				referenceTarget();
				setState(631);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(629);
					match(NULLCOALESCE);
					setState(630);
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
		public TerminalNode DATETIME_TYPE() { return getToken(ExpressionEvaluatorV2Parser.DATETIME_TYPE, 0); }
		public TerminalNode NULLCOALESCE() { return getToken(ExpressionEvaluatorV2Parser.NULLCOALESCE, 0); }
		public DateTimeEntityContext dateTimeEntity() {
			return getRuleContext(DateTimeEntityContext.class,0);
		}
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
		enterRule(_localctx, 68, RULE_dateTimeEntity);
		int _la;
		try {
			int _alt;
			setState(685);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,65,_ctx) ) {
			case 1:
				_localctx = new DateTimeDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(635);
				match(IF);
				setState(636);
				logicalExpression();
				setState(637);
				match(THEN);
				setState(638);
				dateTimeEntity();
				setState(646);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(639);
					match(ELSEIF);
					setState(640);
					logicalExpression();
					setState(641);
					match(THEN);
					setState(642);
					dateTimeEntity();
					}
					}
					setState(648);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(649);
				match(ELSE);
				setState(650);
				dateTimeEntity();
				setState(651);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new DateTimeFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(653);
				match(IF);
				setState(654);
				match(LPAREN);
				setState(655);
				logicalExpression();
				setState(656);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(657);
				dateTimeEntity();
				setState(665);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(658);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(659);
						logicalExpression();
						setState(660);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(661);
						dateTimeEntity();
						}
						} 
					}
					setState(667);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
				}
				setState(668);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(669);
				dateTimeEntity();
				setState(670);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new DateTimeConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(672);
				match(DATETIME);
				setState(674);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TIME_OFFSET) {
					{
					setState(673);
					match(TIME_OFFSET);
					}
				}

				}
				break;
			case 4:
				_localctx = new DateTimeCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(676);
				match(NOW_DATETIME);
				}
				break;
			case 5:
				_localctx = new DateTimeReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(678);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DATETIME_TYPE) {
					{
					setState(677);
					match(DATETIME_TYPE);
					}
				}

				setState(680);
				referenceTarget();
				setState(683);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(681);
					match(NULLCOALESCE);
					setState(682);
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
		public TerminalNode NULLCOALESCE() { return getToken(ExpressionEvaluatorV2Parser.NULLCOALESCE, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
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
		enterRule(_localctx, 70, RULE_vectorEntity);
		int _la;
		try {
			int _alt;
			setState(743);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,71,_ctx) ) {
			case 1:
				_localctx = new VectorDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(687);
				match(IF);
				setState(688);
				logicalExpression();
				setState(689);
				match(THEN);
				setState(690);
				vectorEntity();
				setState(698);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(691);
					match(ELSEIF);
					setState(692);
					logicalExpression();
					setState(693);
					match(THEN);
					setState(694);
					vectorEntity();
					}
					}
					setState(700);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(701);
				match(ELSE);
				setState(702);
				vectorEntity();
				setState(703);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new VectorFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(705);
				match(IF);
				setState(706);
				match(LPAREN);
				setState(707);
				logicalExpression();
				setState(708);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(709);
				vectorEntity();
				setState(717);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,67,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(710);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(711);
						logicalExpression();
						setState(712);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(713);
						vectorEntity();
						}
						} 
					}
					setState(719);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,67,_ctx);
				}
				setState(720);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(721);
				vectorEntity();
				setState(722);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new VectorOfEntitiesOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(724);
				match(LBRACKET);
				setState(725);
				allEntityTypes();
				setState(730);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(726);
					match(COMMA);
					setState(727);
					allEntityTypes();
					}
					}
					setState(732);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(733);
				match(RBRACKET);
				}
				break;
			case 4:
				_localctx = new VectorReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(736);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==VECTOR_TYPE) {
					{
					setState(735);
					match(VECTOR_TYPE);
					}
				}

				setState(738);
				referenceTarget();
				setState(741);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(739);
					match(NULLCOALESCE);
					setState(740);
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
		enterRule(_localctx, 72, RULE_vectorOfVariables);
		int _la;
		try {
			_localctx = new VectorOfVariablesOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(745);
			match(LBRACKET);
			setState(746);
			match(IDENTIFIER);
			setState(751);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(747);
				match(COMMA);
				setState(748);
				match(IDENTIFIER);
				}
				}
				setState(753);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(754);
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
		"\u0004\u0001B\u02f5\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u00a4\b\u0007\u0001\b"+
		"\u0001\b\u0001\b\u0005\b\u00a9\b\b\n\b\f\b\u00ac\t\b\u0001\t\u0001\t\u0001"+
		"\t\u0003\t\u00b1\b\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u00b8"+
		"\b\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0005\f\u00bf\b\f"+
		"\n\f\f\f\u00c2\t\f\u0001\r\u0001\r\u0001\r\u0005\r\u00c7\b\r\n\r\f\r\u00ca"+
		"\t\r\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u00cf\b\u000e\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u00d4\b\u000f\n\u000f\f\u000f"+
		"\u00d7\t\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u00dc\b"+
		"\u0010\u0001\u0011\u0001\u0011\u0005\u0011\u00e0\b\u0011\n\u0011\f\u0011"+
		"\u00e3\t\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u00f3\b\u0012\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u00fa\b\u0013"+
		"\n\u0013\f\u0013\u00fd\t\u0013\u0003\u0013\u00ff\b\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0005\u0014\u0106\b\u0014\n"+
		"\u0014\f\u0014\u0109\t\u0014\u0003\u0014\u010b\b\u0014\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0005\u0015\u0117\b\u0015\n\u0015\f\u0015"+
		"\u011a\t\u0015\u0003\u0015\u011c\b\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005\u0015\u0125"+
		"\b\u0015\n\u0015\f\u0015\u0128\t\u0015\u0003\u0015\u012a\b\u0015\u0001"+
		"\u0015\u0003\u0015\u012d\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u0135\b\u0016\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0003\u0017\u013f\b\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018\u0149"+
		"\b\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u0154\b\u0019\n"+
		"\u0019\f\u0019\u0157\t\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u0167"+
		"\b\u0019\n\u0019\f\u0019\u016a\t\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u0172\b\u0019\u0001\u001a"+
		"\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0003\u001b"+
		"\u0180\b\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0005\u001c"+
		"\u018c\b\u001c\n\u001c\f\u001c\u018f\t\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0005"+
		"\u001c\u019f\b\u001c\n\u001c\f\u001c\u01a2\t\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0003\u001c\u01a9\b\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0003\u001c\u01ae\b\u001c\u0003\u001c\u01b0\b"+
		"\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d\u01bb\b\u001d\n"+
		"\u001d\f\u001d\u01be\t\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d\u01ce"+
		"\b\u001d\n\u001d\f\u001d\u01d1\t\u001d\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0003\u001d\u01d9\b\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0003\u001d\u01de\b\u001d\u0003\u001d\u01e0\b"+
		"\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u01e5\b\u001e\n"+
		"\u001e\f\u001e\u01e8\t\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0005"+
		"\u001f\u01f3\b\u001f\n\u001f\f\u001f\u01f6\t\u001f\u0001\u001f\u0001\u001f"+
		"\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f"+
		"\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f"+
		"\u0005\u001f\u0206\b\u001f\n\u001f\f\u001f\u0209\t\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u0211"+
		"\b\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u0216\b\u001f"+
		"\u0003\u001f\u0218\b\u001f\u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0001 \u0001 \u0005 \u0223\b \n \f \u0226\t \u0001 \u0001 \u0001 \u0001"+
		" \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0005 \u0236\b \n \f \u0239\t \u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0001 \u0003 \u0242\b \u0001 \u0001 \u0001 \u0003 \u0247\b \u0003 \u0249"+
		"\b \u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0005"+
		"!\u0254\b!\n!\f!\u0257\t!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0005!\u0267\b!\n!"+
		"\f!\u026a\t!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0003!\u0273"+
		"\b!\u0001!\u0001!\u0001!\u0003!\u0278\b!\u0003!\u027a\b!\u0001\"\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0005\"\u0285"+
		"\b\"\n\"\f\"\u0288\t\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\""+
		"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0005"+
		"\"\u0298\b\"\n\"\f\"\u029b\t\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\""+
		"\u0001\"\u0003\"\u02a3\b\"\u0001\"\u0001\"\u0003\"\u02a7\b\"\u0001\"\u0001"+
		"\"\u0001\"\u0003\"\u02ac\b\"\u0003\"\u02ae\b\"\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0005#\u02b9\b#\n#\f#\u02bc\t#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0005#\u02cc\b#\n#\f#\u02cf\t#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0005#\u02d9\b#\n#\f#\u02dc\t#\u0001"+
		"#\u0001#\u0001#\u0003#\u02e1\b#\u0001#\u0001#\u0001#\u0003#\u02e6\b#\u0003"+
		"#\u02e8\b#\u0001$\u0001$\u0001$\u0001$\u0005$\u02ee\b$\n$\f$\u02f1\t$"+
		"\u0001$\u0001$\u0001$\u0000\u0000%\u0000\u0002\u0004\u0006\b\n\f\u000e"+
		"\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDF"+
		"H\u0000\u0007\u0001\u0000\t\f\u0002\u0000\u0017\u0017!!\u0001\u0000\u0011"+
		"\u0012\u0002\u0000\u000f\u0010\u0014\u0014\u0002\u0000\u0013\u0013\u0017"+
		"\u0017\u0001\u0000+,\u0001\u0000\r\u000e\u034a\u0000M\u0001\u0000\u0000"+
		"\u0000\u0002T\u0001\u0000\u0000\u0000\u0004]\u0001\u0000\u0000\u0000\u0006"+
		"m\u0001\u0000\u0000\u0000\bo\u0001\u0000\u0000\u0000\nq\u0001\u0000\u0000"+
		"\u0000\fy\u0001\u0000\u0000\u0000\u000e\u00a3\u0001\u0000\u0000\u0000"+
		"\u0010\u00a5\u0001\u0000\u0000\u0000\u0012\u00b0\u0001\u0000\u0000\u0000"+
		"\u0014\u00b7\u0001\u0000\u0000\u0000\u0016\u00b9\u0001\u0000\u0000\u0000"+
		"\u0018\u00bb\u0001\u0000\u0000\u0000\u001a\u00c3\u0001\u0000\u0000\u0000"+
		"\u001c\u00ce\u0001\u0000\u0000\u0000\u001e\u00d0\u0001\u0000\u0000\u0000"+
		" \u00d8\u0001\u0000\u0000\u0000\"\u00dd\u0001\u0000\u0000\u0000$\u00f2"+
		"\u0001\u0000\u0000\u0000&\u00f4\u0001\u0000\u0000\u0000(\u010a\u0001\u0000"+
		"\u0000\u0000*\u012c\u0001\u0000\u0000\u0000,\u0134\u0001\u0000\u0000\u0000"+
		".\u013e\u0001\u0000\u0000\u00000\u0148\u0001\u0000\u0000\u00002\u0171"+
		"\u0001\u0000\u0000\u00004\u0173\u0001\u0000\u0000\u00006\u017f\u0001\u0000"+
		"\u0000\u00008\u01af\u0001\u0000\u0000\u0000:\u01df\u0001\u0000\u0000\u0000"+
		"<\u01e1\u0001\u0000\u0000\u0000>\u0217\u0001\u0000\u0000\u0000@\u0248"+
		"\u0001\u0000\u0000\u0000B\u0279\u0001\u0000\u0000\u0000D\u02ad\u0001\u0000"+
		"\u0000\u0000F\u02e7\u0001\u0000\u0000\u0000H\u02e9\u0001\u0000\u0000\u0000"+
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
		"\u0001\u0000\u0000\u0000cd\u00050\u0000\u0000de\u0005\u001f\u0000\u0000"+
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
		"\u00a4\u0001\u0000\u0000\u0000\u0087\u0088\u0003\u0016\u000b\u0000\u0088"+
		"\u0089\u0003,\u0016\u0000\u0089\u008a\u0003\u0016\u000b\u0000\u008a\u00a4"+
		"\u0001\u0000\u0000\u0000\u008b\u008c\u0003<\u001e\u0000\u008c\u008d\u0003"+
		",\u0016\u0000\u008d\u008e\u0003<\u001e\u0000\u008e\u00a4\u0001\u0000\u0000"+
		"\u0000\u008f\u0090\u0003@ \u0000\u0090\u0091\u0003,\u0016\u0000\u0091"+
		"\u0092\u0003@ \u0000\u0092\u00a4\u0001\u0000\u0000\u0000\u0093\u0094\u0003"+
		"B!\u0000\u0094\u0095\u0003,\u0016\u0000\u0095\u0096\u0003B!\u0000\u0096"+
		"\u00a4\u0001\u0000\u0000\u0000\u0097\u0098\u0003D\"\u0000\u0098\u0099"+
		"\u0003,\u0016\u0000\u0099\u009a\u0003D\"\u0000\u009a\u00a4\u0001\u0000"+
		"\u0000\u0000\u009b\u009c\u0003<\u001e\u0000\u009c\u009d\u0005\"\u0000"+
		"\u0000\u009d\u009e\u00051\u0000\u0000\u009e\u00a4\u0001\u0000\u0000\u0000"+
		"\u009f\u00a0\u0003<\u001e\u0000\u00a0\u00a1\u0005#\u0000\u0000\u00a1\u00a2"+
		"\u00051\u0000\u0000\u00a2\u00a4\u0001\u0000\u0000\u0000\u00a3\u0081\u0001"+
		"\u0000\u0000\u0000\u00a3\u0087\u0001\u0000\u0000\u0000\u00a3\u008b\u0001"+
		"\u0000\u0000\u0000\u00a3\u008f\u0001\u0000\u0000\u0000\u00a3\u0093\u0001"+
		"\u0000\u0000\u0000\u00a3\u0097\u0001\u0000\u0000\u0000\u00a3\u009b\u0001"+
		"\u0000\u0000\u0000\u00a3\u009f\u0001\u0000\u0000\u0000\u00a4\u000f\u0001"+
		"\u0000\u0000\u0000\u00a5\u00aa\u0003\u0012\t\u0000\u00a6\u00a7\u0007\u0000"+
		"\u0000\u0000\u00a7\u00a9\u0003\u0012\t\u0000\u00a8\u00a6\u0001\u0000\u0000"+
		"\u0000\u00a9\u00ac\u0001\u0000\u0000\u0000\u00aa\u00a8\u0001\u0000\u0000"+
		"\u0000\u00aa\u00ab\u0001\u0000\u0000\u0000\u00ab\u0011\u0001\u0000\u0000"+
		"\u0000\u00ac\u00aa\u0001\u0000\u0000\u0000\u00ad\u00ae\u0007\u0001\u0000"+
		"\u0000\u00ae\u00b1\u0003\u0012\t\u0000\u00af\u00b1\u0003\u0014\n\u0000"+
		"\u00b0\u00ad\u0001\u0000\u0000\u0000\u00b0\u00af\u0001\u0000\u0000\u0000"+
		"\u00b1\u0013\u0001\u0000\u0000\u0000\u00b2\u00b3\u0005\'\u0000\u0000\u00b3"+
		"\u00b4\u0003\b\u0004\u0000\u00b4\u00b5\u0005(\u0000\u0000\u00b5\u00b8"+
		"\u0001\u0000\u0000\u0000\u00b6\u00b8\u00038\u001c\u0000\u00b7\u00b2\u0001"+
		"\u0000\u0000\u0000\u00b7\u00b6\u0001\u0000\u0000\u0000\u00b8\u0015\u0001"+
		"\u0000\u0000\u0000\u00b9\u00ba\u0003\u0018\f\u0000\u00ba\u0017\u0001\u0000"+
		"\u0000\u0000\u00bb\u00c0\u0003\u001a\r\u0000\u00bc\u00bd\u0007\u0002\u0000"+
		"\u0000\u00bd\u00bf\u0003\u001a\r\u0000\u00be\u00bc\u0001\u0000\u0000\u0000"+
		"\u00bf\u00c2\u0001\u0000\u0000\u0000\u00c0\u00be\u0001\u0000\u0000\u0000"+
		"\u00c0\u00c1\u0001\u0000\u0000\u0000\u00c1\u0019\u0001\u0000\u0000\u0000"+
		"\u00c2\u00c0\u0001\u0000\u0000\u0000\u00c3\u00c8\u0003\u001c\u000e\u0000"+
		"\u00c4\u00c5\u0007\u0003\u0000\u0000\u00c5\u00c7\u0003\u001c\u000e\u0000"+
		"\u00c6\u00c4\u0001\u0000\u0000\u0000\u00c7\u00ca\u0001\u0000\u0000\u0000"+
		"\u00c8\u00c6\u0001\u0000\u0000\u0000\u00c8\u00c9\u0001\u0000\u0000\u0000"+
		"\u00c9\u001b\u0001\u0000\u0000\u0000\u00ca\u00c8\u0001\u0000\u0000\u0000"+
		"\u00cb\u00cc\u0005\u0012\u0000\u0000\u00cc\u00cf\u0003\u001c\u000e\u0000"+
		"\u00cd\u00cf\u0003\u001e\u000f\u0000\u00ce\u00cb\u0001\u0000\u0000\u0000"+
		"\u00ce\u00cd\u0001\u0000\u0000\u0000\u00cf\u001d\u0001\u0000\u0000\u0000"+
		"\u00d0\u00d5\u0003 \u0010\u0000\u00d1\u00d2\u0005\u0019\u0000\u0000\u00d2"+
		"\u00d4\u0003 \u0010\u0000\u00d3\u00d1\u0001\u0000\u0000\u0000\u00d4\u00d7"+
		"\u0001\u0000\u0000\u0000\u00d5\u00d3\u0001\u0000\u0000\u0000\u00d5\u00d6"+
		"\u0001\u0000\u0000\u0000\u00d6\u001f\u0001\u0000\u0000\u0000\u00d7\u00d5"+
		"\u0001\u0000\u0000\u0000\u00d8\u00db\u0003\"\u0011\u0000\u00d9\u00da\u0005"+
		"\u0018\u0000\u0000\u00da\u00dc\u0003\u001c\u000e\u0000\u00db\u00d9\u0001"+
		"\u0000\u0000\u0000\u00db\u00dc\u0001\u0000\u0000\u0000\u00dc!\u0001\u0000"+
		"\u0000\u0000\u00dd\u00e1\u0003$\u0012\u0000\u00de\u00e0\u0007\u0004\u0000"+
		"\u0000\u00df\u00de\u0001\u0000\u0000\u0000\u00e0\u00e3\u0001\u0000\u0000"+
		"\u0000\u00e1\u00df\u0001\u0000\u0000\u0000\u00e1\u00e2\u0001\u0000\u0000"+
		"\u0000\u00e2#\u0001\u0000\u0000\u0000\u00e3\u00e1\u0001\u0000\u0000\u0000"+
		"\u00e4\u00e5\u0005\'\u0000\u0000\u00e5\u00e6\u0003\u0016\u000b\u0000\u00e6"+
		"\u00e7\u0005(\u0000\u0000\u00e7\u00f3\u0001\u0000\u0000\u0000\u00e8\u00e9"+
		"\u0005\u001a\u0000\u0000\u00e9\u00ea\u0005\'\u0000\u0000\u00ea\u00eb\u0003"+
		"\u0016\u000b\u0000\u00eb\u00ec\u0005(\u0000\u0000\u00ec\u00f3\u0001\u0000"+
		"\u0000\u0000\u00ed\u00ee\u0005\u0015\u0000\u0000\u00ee\u00ef\u0003\u0016"+
		"\u000b\u0000\u00ef\u00f0\u0005\u0015\u0000\u0000\u00f0\u00f3\u0001\u0000"+
		"\u0000\u0000\u00f1\u00f3\u0003:\u001d\u0000\u00f2\u00e4\u0001\u0000\u0000"+
		"\u0000\u00f2\u00e8\u0001\u0000\u0000\u0000\u00f2\u00ed\u0001\u0000\u0000"+
		"\u0000\u00f2\u00f1\u0001\u0000\u0000\u0000\u00f3%\u0001\u0000\u0000\u0000"+
		"\u00f4\u00f5\u00050\u0000\u0000\u00f5\u00fe\u0005\'\u0000\u0000\u00f6"+
		"\u00fb\u0003.\u0017\u0000\u00f7\u00f8\u0007\u0005\u0000\u0000\u00f8\u00fa"+
		"\u0003.\u0017\u0000\u00f9\u00f7\u0001\u0000\u0000\u0000\u00fa\u00fd\u0001"+
		"\u0000\u0000\u0000\u00fb\u00f9\u0001\u0000\u0000\u0000\u00fb\u00fc\u0001"+
		"\u0000\u0000\u0000\u00fc\u00ff\u0001\u0000\u0000\u0000\u00fd\u00fb\u0001"+
		"\u0000\u0000\u0000\u00fe\u00f6\u0001\u0000\u0000\u0000\u00fe\u00ff\u0001"+
		"\u0000\u0000\u0000\u00ff\u0100\u0001\u0000\u0000\u0000\u0100\u0101\u0005"+
		"(\u0000\u0000\u0101\'\u0001\u0000\u0000\u0000\u0102\u010b\u0003&\u0013"+
		"\u0000\u0103\u0107\u00050\u0000\u0000\u0104\u0106\u0003*\u0015\u0000\u0105"+
		"\u0104\u0001\u0000\u0000\u0000\u0106\u0109\u0001\u0000\u0000\u0000\u0107"+
		"\u0105\u0001\u0000\u0000\u0000\u0107\u0108\u0001\u0000\u0000\u0000\u0108"+
		"\u010b\u0001\u0000\u0000\u0000\u0109\u0107\u0001\u0000\u0000\u0000\u010a"+
		"\u0102\u0001\u0000\u0000\u0000\u010a\u0103\u0001\u0000\u0000\u0000\u010b"+
		")\u0001\u0000\u0000\u0000\u010c\u010d\u0005-\u0000\u0000\u010d\u012d\u0005"+
		"0\u0000\u0000\u010e\u010f\u0005/\u0000\u0000\u010f\u012d\u00050\u0000"+
		"\u0000\u0110\u0111\u0005-\u0000\u0000\u0111\u0112\u00050\u0000\u0000\u0112"+
		"\u011b\u0005\'\u0000\u0000\u0113\u0118\u0003.\u0017\u0000\u0114\u0115"+
		"\u0005+\u0000\u0000\u0115\u0117\u0003.\u0017\u0000\u0116\u0114\u0001\u0000"+
		"\u0000\u0000\u0117\u011a\u0001\u0000\u0000\u0000\u0118\u0116\u0001\u0000"+
		"\u0000\u0000\u0118\u0119\u0001\u0000\u0000\u0000\u0119\u011c\u0001\u0000"+
		"\u0000\u0000\u011a\u0118\u0001\u0000\u0000\u0000\u011b\u0113\u0001\u0000"+
		"\u0000\u0000\u011b\u011c\u0001\u0000\u0000\u0000\u011c\u011d\u0001\u0000"+
		"\u0000\u0000\u011d\u012d\u0005(\u0000\u0000\u011e\u011f\u0005/\u0000\u0000"+
		"\u011f\u0120\u00050\u0000\u0000\u0120\u0129\u0005\'\u0000\u0000\u0121"+
		"\u0126\u0003.\u0017\u0000\u0122\u0123\u0005+\u0000\u0000\u0123\u0125\u0003"+
		".\u0017\u0000\u0124\u0122\u0001\u0000\u0000\u0000\u0125\u0128\u0001\u0000"+
		"\u0000\u0000\u0126\u0124\u0001\u0000\u0000\u0000\u0126\u0127\u0001\u0000"+
		"\u0000\u0000\u0127\u012a\u0001\u0000\u0000\u0000\u0128\u0126\u0001\u0000"+
		"\u0000\u0000\u0129\u0121\u0001\u0000\u0000\u0000\u0129\u012a\u0001\u0000"+
		"\u0000\u0000\u012a\u012b\u0001\u0000\u0000\u0000\u012b\u012d\u0005(\u0000"+
		"\u0000\u012c\u010c\u0001\u0000\u0000\u0000\u012c\u010e\u0001\u0000\u0000"+
		"\u0000\u012c\u0110\u0001\u0000\u0000\u0000\u012c\u011e\u0001\u0000\u0000"+
		"\u0000\u012d+\u0001\u0000\u0000\u0000\u012e\u0135\u0005\u001b\u0000\u0000"+
		"\u012f\u0135\u0005\u001c\u0000\u0000\u0130\u0135\u0005\u001d\u0000\u0000"+
		"\u0131\u0135\u0005\u001e\u0000\u0000\u0132\u0135\u0005\u001f\u0000\u0000"+
		"\u0133\u0135\u0005 \u0000\u0000\u0134\u012e\u0001\u0000\u0000\u0000\u0134"+
		"\u012f\u0001\u0000\u0000\u0000\u0134\u0130\u0001\u0000\u0000\u0000\u0134"+
		"\u0131\u0001\u0000\u0000\u0000\u0134\u0132\u0001\u0000\u0000\u0000\u0134"+
		"\u0133\u0001\u0000\u0000\u0000\u0135-\u0001\u0000\u0000\u0000\u0136\u013f"+
		"\u0003\u0016\u000b\u0000\u0137\u013f\u0003\b\u0004\u0000\u0138\u013f\u0003"+
		"@ \u0000\u0139\u013f\u0003B!\u0000\u013a\u013f\u0003D\"\u0000\u013b\u013f"+
		"\u0003<\u001e\u0000\u013c\u013f\u0003F#\u0000\u013d\u013f\u0005\u0006"+
		"\u0000\u0000\u013e\u0136\u0001\u0000\u0000\u0000\u013e\u0137\u0001\u0000"+
		"\u0000\u0000\u013e\u0138\u0001\u0000\u0000\u0000\u013e\u0139\u0001\u0000"+
		"\u0000\u0000\u013e\u013a\u0001\u0000\u0000\u0000\u013e\u013b\u0001\u0000"+
		"\u0000\u0000\u013e\u013c\u0001\u0000\u0000\u0000\u013e\u013d\u0001\u0000"+
		"\u0000\u0000\u013f/\u0001\u0000\u0000\u0000\u0140\u0149\u00032\u0019\u0000"+
		"\u0141\u0149\u0003\u0016\u000b\u0000\u0142\u0149\u0003\b\u0004\u0000\u0143"+
		"\u0149\u0003@ \u0000\u0144\u0149\u0003B!\u0000\u0145\u0149\u0003D\"\u0000"+
		"\u0146\u0149\u0003<\u001e\u0000\u0147\u0149\u0003F#\u0000\u0148\u0140"+
		"\u0001\u0000\u0000\u0000\u0148\u0141\u0001\u0000\u0000\u0000\u0148\u0142"+
		"\u0001\u0000\u0000\u0000\u0148\u0143\u0001\u0000\u0000\u0000\u0148\u0144"+
		"\u0001\u0000\u0000\u0000\u0148\u0145\u0001\u0000\u0000\u0000\u0148\u0146"+
		"\u0001\u0000\u0000\u0000\u0148\u0147\u0001\u0000\u0000\u0000\u01491\u0001"+
		"\u0000\u0000\u0000\u014a\u014b\u0005\u0001\u0000\u0000\u014b\u014c\u0003"+
		"\b\u0004\u0000\u014c\u014d\u0005\u0002\u0000\u0000\u014d\u0155\u00032"+
		"\u0019\u0000\u014e\u014f\u0005\u0004\u0000\u0000\u014f\u0150\u0003\b\u0004"+
		"\u0000\u0150\u0151\u0005\u0002\u0000\u0000\u0151\u0152\u00032\u0019\u0000"+
		"\u0152\u0154\u0001\u0000\u0000\u0000\u0153\u014e\u0001\u0000\u0000\u0000"+
		"\u0154\u0157\u0001\u0000\u0000\u0000\u0155\u0153\u0001\u0000\u0000\u0000"+
		"\u0155\u0156\u0001\u0000\u0000\u0000\u0156\u0158\u0001\u0000\u0000\u0000"+
		"\u0157\u0155\u0001\u0000\u0000\u0000\u0158\u0159\u0005\u0003\u0000\u0000"+
		"\u0159\u015a\u00032\u0019\u0000\u015a\u015b\u0005\u0005\u0000\u0000\u015b"+
		"\u0172\u0001\u0000\u0000\u0000\u015c\u015d\u0005\u0001\u0000\u0000\u015d"+
		"\u015e\u0005\'\u0000\u0000\u015e\u015f\u0003\b\u0004\u0000\u015f\u0160"+
		"\u0007\u0005\u0000\u0000\u0160\u0168\u00032\u0019\u0000\u0161\u0162\u0007"+
		"\u0005\u0000\u0000\u0162\u0163\u0003\b\u0004\u0000\u0163\u0164\u0007\u0005"+
		"\u0000\u0000\u0164\u0165\u00032\u0019\u0000\u0165\u0167\u0001\u0000\u0000"+
		"\u0000\u0166\u0161\u0001\u0000\u0000\u0000\u0167\u016a\u0001\u0000\u0000"+
		"\u0000\u0168\u0166\u0001\u0000\u0000\u0000\u0168\u0169\u0001\u0000\u0000"+
		"\u0000\u0169\u016b\u0001\u0000\u0000\u0000\u016a\u0168\u0001\u0000\u0000"+
		"\u0000\u016b\u016c\u0007\u0005\u0000\u0000\u016c\u016d\u00032\u0019\u0000"+
		"\u016d\u016e\u0005(\u0000\u0000\u016e\u0172\u0001\u0000\u0000\u0000\u016f"+
		"\u0172\u00034\u001a\u0000\u0170\u0172\u0003(\u0014\u0000\u0171\u014a\u0001"+
		"\u0000\u0000\u0000\u0171\u015c\u0001\u0000\u0000\u0000\u0171\u016f\u0001"+
		"\u0000\u0000\u0000\u0171\u0170\u0001\u0000\u0000\u0000\u01723\u0001\u0000"+
		"\u0000\u0000\u0173\u0174\u00036\u001b\u0000\u0174\u0175\u0005\'\u0000"+
		"\u0000\u0175\u0176\u00032\u0019\u0000\u0176\u0177\u0005(\u0000\u0000\u0177"+
		"5\u0001\u0000\u0000\u0000\u0178\u0180\u00058\u0000\u0000\u0179\u0180\u0005"+
		"9\u0000\u0000\u017a\u0180\u0005:\u0000\u0000\u017b\u0180\u0005;\u0000"+
		"\u0000\u017c\u0180\u0005<\u0000\u0000\u017d\u0180\u0005=\u0000\u0000\u017e"+
		"\u0180\u0005>\u0000\u0000\u017f\u0178\u0001\u0000\u0000\u0000\u017f\u0179"+
		"\u0001\u0000\u0000\u0000\u017f\u017a\u0001\u0000\u0000\u0000\u017f\u017b"+
		"\u0001\u0000\u0000\u0000\u017f\u017c\u0001\u0000\u0000\u0000\u017f\u017d"+
		"\u0001\u0000\u0000\u0000\u017f\u017e\u0001\u0000\u0000\u0000\u01807\u0001"+
		"\u0000\u0000\u0000\u0181\u01b0\u0007\u0006\u0000\u0000\u0182\u0183\u0005"+
		"\u0001\u0000\u0000\u0183\u0184\u0003\b\u0004\u0000\u0184\u0185\u0005\u0002"+
		"\u0000\u0000\u0185\u018d\u0003\b\u0004\u0000\u0186\u0187\u0005\u0004\u0000"+
		"\u0000\u0187\u0188\u0003\b\u0004\u0000\u0188\u0189\u0005\u0002\u0000\u0000"+
		"\u0189\u018a\u0003\b\u0004\u0000\u018a\u018c\u0001\u0000\u0000\u0000\u018b"+
		"\u0186\u0001\u0000\u0000\u0000\u018c\u018f\u0001\u0000\u0000\u0000\u018d"+
		"\u018b\u0001\u0000\u0000\u0000\u018d\u018e\u0001\u0000\u0000\u0000\u018e"+
		"\u0190\u0001\u0000\u0000\u0000\u018f\u018d\u0001\u0000\u0000\u0000\u0190"+
		"\u0191\u0005\u0003\u0000\u0000\u0191\u0192\u0003\b\u0004\u0000\u0192\u0193"+
		"\u0005\u0005\u0000\u0000\u0193\u01b0\u0001\u0000\u0000\u0000\u0194\u0195"+
		"\u0005\u0001\u0000\u0000\u0195\u0196\u0005\'\u0000\u0000\u0196\u0197\u0003"+
		"\b\u0004\u0000\u0197\u0198\u0007\u0005\u0000\u0000\u0198\u01a0\u0003\b"+
		"\u0004\u0000\u0199\u019a\u0007\u0005\u0000\u0000\u019a\u019b\u0003\b\u0004"+
		"\u0000\u019b\u019c\u0007\u0005\u0000\u0000\u019c\u019d\u0003\b\u0004\u0000"+
		"\u019d\u019f\u0001\u0000\u0000\u0000\u019e\u0199\u0001\u0000\u0000\u0000"+
		"\u019f\u01a2\u0001\u0000\u0000\u0000\u01a0\u019e\u0001\u0000\u0000\u0000"+
		"\u01a0\u01a1\u0001\u0000\u0000\u0000\u01a1\u01a3\u0001\u0000\u0000\u0000"+
		"\u01a2\u01a0\u0001\u0000\u0000\u0000\u01a3\u01a4\u0007\u0005\u0000\u0000"+
		"\u01a4\u01a5\u0003\b\u0004\u0000\u01a5\u01a6\u0005(\u0000\u0000\u01a6"+
		"\u01b0\u0001\u0000\u0000\u0000\u01a7\u01a9\u00058\u0000\u0000\u01a8\u01a7"+
		"\u0001\u0000\u0000\u0000\u01a8\u01a9\u0001\u0000\u0000\u0000\u01a9\u01aa"+
		"\u0001\u0000\u0000\u0000\u01aa\u01ad\u0003(\u0014\u0000\u01ab\u01ac\u0005"+
		".\u0000\u0000\u01ac\u01ae\u00038\u001c\u0000\u01ad\u01ab\u0001\u0000\u0000"+
		"\u0000\u01ad\u01ae\u0001\u0000\u0000\u0000\u01ae\u01b0\u0001\u0000\u0000"+
		"\u0000\u01af\u0181\u0001\u0000\u0000\u0000\u01af\u0182\u0001\u0000\u0000"+
		"\u0000\u01af\u0194\u0001\u0000\u0000\u0000\u01af\u01a8\u0001\u0000\u0000"+
		"\u0000\u01b09\u0001\u0000\u0000\u0000\u01b1\u01b2\u0005\u0001\u0000\u0000"+
		"\u01b2\u01b3\u0003\b\u0004\u0000\u01b3\u01b4\u0005\u0002\u0000\u0000\u01b4"+
		"\u01bc\u0003\u0016\u000b\u0000\u01b5\u01b6\u0005\u0004\u0000\u0000\u01b6"+
		"\u01b7\u0003\b\u0004\u0000\u01b7\u01b8\u0005\u0002\u0000\u0000\u01b8\u01b9"+
		"\u0003\u0016\u000b\u0000\u01b9\u01bb\u0001\u0000\u0000\u0000\u01ba\u01b5"+
		"\u0001\u0000\u0000\u0000\u01bb\u01be\u0001\u0000\u0000\u0000\u01bc\u01ba"+
		"\u0001\u0000\u0000\u0000\u01bc\u01bd\u0001\u0000\u0000\u0000\u01bd\u01bf"+
		"\u0001\u0000\u0000\u0000\u01be\u01bc\u0001\u0000\u0000\u0000\u01bf\u01c0"+
		"\u0005\u0003\u0000\u0000\u01c0\u01c1\u0003\u0016\u000b\u0000\u01c1\u01c2"+
		"\u0005\u0005\u0000\u0000\u01c2\u01e0\u0001\u0000\u0000\u0000\u01c3\u01c4"+
		"\u0005\u0001\u0000\u0000\u01c4\u01c5\u0005\'\u0000\u0000\u01c5\u01c6\u0003"+
		"\b\u0004\u0000\u01c6\u01c7\u0007\u0005\u0000\u0000\u01c7\u01cf\u0003\u0016"+
		"\u000b\u0000\u01c8\u01c9\u0007\u0005\u0000\u0000\u01c9\u01ca\u0003\b\u0004"+
		"\u0000\u01ca\u01cb\u0007\u0005\u0000\u0000\u01cb\u01cc\u0003\u0016\u000b"+
		"\u0000\u01cc\u01ce\u0001\u0000\u0000\u0000\u01cd\u01c8\u0001\u0000\u0000"+
		"\u0000\u01ce\u01d1\u0001\u0000\u0000\u0000\u01cf\u01cd\u0001\u0000\u0000"+
		"\u0000\u01cf\u01d0\u0001\u0000\u0000\u0000\u01d0\u01d2\u0001\u0000\u0000"+
		"\u0000\u01d1\u01cf\u0001\u0000\u0000\u0000\u01d2\u01d3\u0007\u0005\u0000"+
		"\u0000\u01d3\u01d4\u0003\u0016\u000b\u0000\u01d4\u01d5\u0005(\u0000\u0000"+
		"\u01d5\u01e0\u0001\u0000\u0000\u0000\u01d6\u01e0\u00052\u0000\u0000\u01d7"+
		"\u01d9\u00059\u0000\u0000\u01d8\u01d7\u0001\u0000\u0000\u0000\u01d8\u01d9"+
		"\u0001\u0000\u0000\u0000\u01d9\u01da\u0001\u0000\u0000\u0000\u01da\u01dd"+
		"\u0003(\u0014\u0000\u01db\u01dc\u0005.\u0000\u0000\u01dc\u01de\u0003:"+
		"\u001d\u0000\u01dd\u01db\u0001\u0000\u0000\u0000\u01dd\u01de\u0001\u0000"+
		"\u0000\u0000\u01de\u01e0\u0001\u0000\u0000\u0000\u01df\u01b1\u0001\u0000"+
		"\u0000\u0000\u01df\u01c3\u0001\u0000\u0000\u0000\u01df\u01d6\u0001\u0000"+
		"\u0000\u0000\u01df\u01d8\u0001\u0000\u0000\u0000\u01e0;\u0001\u0000\u0000"+
		"\u0000\u01e1\u01e6\u0003>\u001f\u0000\u01e2\u01e3\u0005\u0016\u0000\u0000"+
		"\u01e3\u01e5\u0003>\u001f\u0000\u01e4\u01e2\u0001\u0000\u0000\u0000\u01e5"+
		"\u01e8\u0001\u0000\u0000\u0000\u01e6\u01e4\u0001\u0000\u0000\u0000\u01e6"+
		"\u01e7\u0001\u0000\u0000\u0000\u01e7=\u0001\u0000\u0000\u0000\u01e8\u01e6"+
		"\u0001\u0000\u0000\u0000\u01e9\u01ea\u0005\u0001\u0000\u0000\u01ea\u01eb"+
		"\u0003\b\u0004\u0000\u01eb\u01ec\u0005\u0002\u0000\u0000\u01ec\u01f4\u0003"+
		"<\u001e\u0000\u01ed\u01ee\u0005\u0004\u0000\u0000\u01ee\u01ef\u0003\b"+
		"\u0004\u0000\u01ef\u01f0\u0005\u0002\u0000\u0000\u01f0\u01f1\u0003<\u001e"+
		"\u0000\u01f1\u01f3\u0001\u0000\u0000\u0000\u01f2\u01ed\u0001\u0000\u0000"+
		"\u0000\u01f3\u01f6\u0001\u0000\u0000\u0000\u01f4\u01f2\u0001\u0000\u0000"+
		"\u0000\u01f4\u01f5\u0001\u0000\u0000\u0000\u01f5\u01f7\u0001\u0000\u0000"+
		"\u0000\u01f6\u01f4\u0001\u0000\u0000\u0000\u01f7\u01f8\u0005\u0003\u0000"+
		"\u0000\u01f8\u01f9\u0003<\u001e\u0000\u01f9\u01fa\u0005\u0005\u0000\u0000"+
		"\u01fa\u0218\u0001\u0000\u0000\u0000\u01fb\u01fc\u0005\u0001\u0000\u0000"+
		"\u01fc\u01fd\u0005\'\u0000\u0000\u01fd\u01fe\u0003\b\u0004\u0000\u01fe"+
		"\u01ff\u0007\u0005\u0000\u0000\u01ff\u0207\u0003<\u001e\u0000\u0200\u0201"+
		"\u0007\u0005\u0000\u0000\u0201\u0202\u0003\b\u0004\u0000\u0202\u0203\u0007"+
		"\u0005\u0000\u0000\u0203\u0204\u0003<\u001e\u0000\u0204\u0206\u0001\u0000"+
		"\u0000\u0000\u0205\u0200\u0001\u0000\u0000\u0000\u0206\u0209\u0001\u0000"+
		"\u0000\u0000\u0207\u0205\u0001\u0000\u0000\u0000\u0207\u0208\u0001\u0000"+
		"\u0000\u0000\u0208\u020a\u0001\u0000\u0000\u0000\u0209\u0207\u0001\u0000"+
		"\u0000\u0000\u020a\u020b\u0007\u0005\u0000\u0000\u020b\u020c\u0003<\u001e"+
		"\u0000\u020c\u020d\u0005(\u0000\u0000\u020d\u0218\u0001\u0000\u0000\u0000"+
		"\u020e\u0218\u00051\u0000\u0000\u020f\u0211\u0005:\u0000\u0000\u0210\u020f"+
		"\u0001\u0000\u0000\u0000\u0210\u0211\u0001\u0000\u0000\u0000\u0211\u0212"+
		"\u0001\u0000\u0000\u0000\u0212\u0215\u0003(\u0014\u0000\u0213\u0214\u0005"+
		".\u0000\u0000\u0214\u0216\u0003<\u001e\u0000\u0215\u0213\u0001\u0000\u0000"+
		"\u0000\u0215\u0216\u0001\u0000\u0000\u0000\u0216\u0218\u0001\u0000\u0000"+
		"\u0000\u0217\u01e9\u0001\u0000\u0000\u0000\u0217\u01fb\u0001\u0000\u0000"+
		"\u0000\u0217\u020e\u0001\u0000\u0000\u0000\u0217\u0210\u0001\u0000\u0000"+
		"\u0000\u0218?\u0001\u0000\u0000\u0000\u0219\u021a\u0005\u0001\u0000\u0000"+
		"\u021a\u021b\u0003\b\u0004\u0000\u021b\u021c\u0005\u0002\u0000\u0000\u021c"+
		"\u0224\u0003@ \u0000\u021d\u021e\u0005\u0004\u0000\u0000\u021e\u021f\u0003"+
		"\b\u0004\u0000\u021f\u0220\u0005\u0002\u0000\u0000\u0220\u0221\u0003@"+
		" \u0000\u0221\u0223\u0001\u0000\u0000\u0000\u0222\u021d\u0001\u0000\u0000"+
		"\u0000\u0223\u0226\u0001\u0000\u0000\u0000\u0224\u0222\u0001\u0000\u0000"+
		"\u0000\u0224\u0225\u0001\u0000\u0000\u0000\u0225\u0227\u0001\u0000\u0000"+
		"\u0000\u0226\u0224\u0001\u0000\u0000\u0000\u0227\u0228\u0005\u0003\u0000"+
		"\u0000\u0228\u0229\u0003@ \u0000\u0229\u022a\u0005\u0005\u0000\u0000\u022a"+
		"\u0249\u0001\u0000\u0000\u0000\u022b\u022c\u0005\u0001\u0000\u0000\u022c"+
		"\u022d\u0005\'\u0000\u0000\u022d\u022e\u0003\b\u0004\u0000\u022e\u022f"+
		"\u0007\u0005\u0000\u0000\u022f\u0237\u0003@ \u0000\u0230\u0231\u0007\u0005"+
		"\u0000\u0000\u0231\u0232\u0003\b\u0004\u0000\u0232\u0233\u0007\u0005\u0000"+
		"\u0000\u0233\u0234\u0003@ \u0000\u0234\u0236\u0001\u0000\u0000\u0000\u0235"+
		"\u0230\u0001\u0000\u0000\u0000\u0236\u0239\u0001\u0000\u0000\u0000\u0237"+
		"\u0235\u0001\u0000\u0000\u0000\u0237\u0238\u0001\u0000\u0000\u0000\u0238"+
		"\u023a\u0001\u0000\u0000\u0000\u0239\u0237\u0001\u0000\u0000\u0000\u023a"+
		"\u023b\u0007\u0005\u0000\u0000\u023b\u023c\u0003@ \u0000\u023c\u023d\u0005"+
		"(\u0000\u0000\u023d\u0249\u0001\u0000\u0000\u0000\u023e\u0249\u00054\u0000"+
		"\u0000\u023f\u0249\u0005$\u0000\u0000\u0240\u0242\u0005;\u0000\u0000\u0241"+
		"\u0240\u0001\u0000\u0000\u0000\u0241\u0242\u0001\u0000\u0000\u0000\u0242"+
		"\u0243\u0001\u0000\u0000\u0000\u0243\u0246\u0003(\u0014\u0000\u0244\u0245"+
		"\u0005.\u0000\u0000\u0245\u0247\u0003@ \u0000\u0246\u0244\u0001\u0000"+
		"\u0000\u0000\u0246\u0247\u0001\u0000\u0000\u0000\u0247\u0249\u0001\u0000"+
		"\u0000\u0000\u0248\u0219\u0001\u0000\u0000\u0000\u0248\u022b\u0001\u0000"+
		"\u0000\u0000\u0248\u023e\u0001\u0000\u0000\u0000\u0248\u023f\u0001\u0000"+
		"\u0000\u0000\u0248\u0241\u0001\u0000\u0000\u0000\u0249A\u0001\u0000\u0000"+
		"\u0000\u024a\u024b\u0005\u0001\u0000\u0000\u024b\u024c\u0003\b\u0004\u0000"+
		"\u024c\u024d\u0005\u0002\u0000\u0000\u024d\u0255\u0003B!\u0000\u024e\u024f"+
		"\u0005\u0004\u0000\u0000\u024f\u0250\u0003\b\u0004\u0000\u0250\u0251\u0005"+
		"\u0002\u0000\u0000\u0251\u0252\u0003B!\u0000\u0252\u0254\u0001\u0000\u0000"+
		"\u0000\u0253\u024e\u0001\u0000\u0000\u0000\u0254\u0257\u0001\u0000\u0000"+
		"\u0000\u0255\u0253\u0001\u0000\u0000\u0000\u0255\u0256\u0001\u0000\u0000"+
		"\u0000\u0256\u0258\u0001\u0000\u0000\u0000\u0257\u0255\u0001\u0000\u0000"+
		"\u0000\u0258\u0259\u0005\u0003\u0000\u0000\u0259\u025a\u0003B!\u0000\u025a"+
		"\u025b\u0005\u0005\u0000\u0000\u025b\u027a\u0001\u0000\u0000\u0000\u025c"+
		"\u025d\u0005\u0001\u0000\u0000\u025d\u025e\u0005\'\u0000\u0000\u025e\u025f"+
		"\u0003\b\u0004\u0000\u025f\u0260\u0007\u0005\u0000\u0000\u0260\u0268\u0003"+
		"B!\u0000\u0261\u0262\u0007\u0005\u0000\u0000\u0262\u0263\u0003\b\u0004"+
		"\u0000\u0263\u0264\u0007\u0005\u0000\u0000\u0264\u0265\u0003B!\u0000\u0265"+
		"\u0267\u0001\u0000\u0000\u0000\u0266\u0261\u0001\u0000\u0000\u0000\u0267"+
		"\u026a\u0001\u0000\u0000\u0000\u0268\u0266\u0001\u0000\u0000\u0000\u0268"+
		"\u0269\u0001\u0000\u0000\u0000\u0269\u026b\u0001\u0000\u0000\u0000\u026a"+
		"\u0268\u0001\u0000\u0000\u0000\u026b\u026c\u0007\u0005\u0000\u0000\u026c"+
		"\u026d\u0003B!\u0000\u026d\u026e\u0005(\u0000\u0000\u026e\u027a\u0001"+
		"\u0000\u0000\u0000\u026f\u027a\u00055\u0000\u0000\u0270\u027a\u0005%\u0000"+
		"\u0000\u0271\u0273\u0005<\u0000\u0000\u0272\u0271\u0001\u0000\u0000\u0000"+
		"\u0272\u0273\u0001\u0000\u0000\u0000\u0273\u0274\u0001\u0000\u0000\u0000"+
		"\u0274\u0277\u0003(\u0014\u0000\u0275\u0276\u0005.\u0000\u0000\u0276\u0278"+
		"\u0003B!\u0000\u0277\u0275\u0001\u0000\u0000\u0000\u0277\u0278\u0001\u0000"+
		"\u0000\u0000\u0278\u027a\u0001\u0000\u0000\u0000\u0279\u024a\u0001\u0000"+
		"\u0000\u0000\u0279\u025c\u0001\u0000\u0000\u0000\u0279\u026f\u0001\u0000"+
		"\u0000\u0000\u0279\u0270\u0001\u0000\u0000\u0000\u0279\u0272\u0001\u0000"+
		"\u0000\u0000\u027aC\u0001\u0000\u0000\u0000\u027b\u027c\u0005\u0001\u0000"+
		"\u0000\u027c\u027d\u0003\b\u0004\u0000\u027d\u027e\u0005\u0002\u0000\u0000"+
		"\u027e\u0286\u0003D\"\u0000\u027f\u0280\u0005\u0004\u0000\u0000\u0280"+
		"\u0281\u0003\b\u0004\u0000\u0281\u0282\u0005\u0002\u0000\u0000\u0282\u0283"+
		"\u0003D\"\u0000\u0283\u0285\u0001\u0000\u0000\u0000\u0284\u027f\u0001"+
		"\u0000\u0000\u0000\u0285\u0288\u0001\u0000\u0000\u0000\u0286\u0284\u0001"+
		"\u0000\u0000\u0000\u0286\u0287\u0001\u0000\u0000\u0000\u0287\u0289\u0001"+
		"\u0000\u0000\u0000\u0288\u0286\u0001\u0000\u0000\u0000\u0289\u028a\u0005"+
		"\u0003\u0000\u0000\u028a\u028b\u0003D\"\u0000\u028b\u028c\u0005\u0005"+
		"\u0000\u0000\u028c\u02ae\u0001\u0000\u0000\u0000\u028d\u028e\u0005\u0001"+
		"\u0000\u0000\u028e\u028f\u0005\'\u0000\u0000\u028f\u0290\u0003\b\u0004"+
		"\u0000\u0290\u0291\u0007\u0005\u0000\u0000\u0291\u0299\u0003D\"\u0000"+
		"\u0292\u0293\u0007\u0005\u0000\u0000\u0293\u0294\u0003\b\u0004\u0000\u0294"+
		"\u0295\u0007\u0005\u0000\u0000\u0295\u0296\u0003D\"\u0000\u0296\u0298"+
		"\u0001\u0000\u0000\u0000\u0297\u0292\u0001\u0000\u0000\u0000\u0298\u029b"+
		"\u0001\u0000\u0000\u0000\u0299\u0297\u0001\u0000\u0000\u0000\u0299\u029a"+
		"\u0001\u0000\u0000\u0000\u029a\u029c\u0001\u0000\u0000\u0000\u029b\u0299"+
		"\u0001\u0000\u0000\u0000\u029c\u029d\u0007\u0005\u0000\u0000\u029d\u029e"+
		"\u0003D\"\u0000\u029e\u029f\u0005(\u0000\u0000\u029f\u02ae\u0001\u0000"+
		"\u0000\u0000\u02a0\u02a2\u00057\u0000\u0000\u02a1\u02a3\u00056\u0000\u0000"+
		"\u02a2\u02a1\u0001\u0000\u0000\u0000\u02a2\u02a3\u0001\u0000\u0000\u0000"+
		"\u02a3\u02ae\u0001\u0000\u0000\u0000\u02a4\u02ae\u0005&\u0000\u0000\u02a5"+
		"\u02a7\u0005=\u0000\u0000\u02a6\u02a5\u0001\u0000\u0000\u0000\u02a6\u02a7"+
		"\u0001\u0000\u0000\u0000\u02a7\u02a8\u0001\u0000\u0000\u0000\u02a8\u02ab"+
		"\u0003(\u0014\u0000\u02a9\u02aa\u0005.\u0000\u0000\u02aa\u02ac\u0003D"+
		"\"\u0000\u02ab\u02a9\u0001\u0000\u0000\u0000\u02ab\u02ac\u0001\u0000\u0000"+
		"\u0000\u02ac\u02ae\u0001\u0000\u0000\u0000\u02ad\u027b\u0001\u0000\u0000"+
		"\u0000\u02ad\u028d\u0001\u0000\u0000\u0000\u02ad\u02a0\u0001\u0000\u0000"+
		"\u0000\u02ad\u02a4\u0001\u0000\u0000\u0000\u02ad\u02a6\u0001\u0000\u0000"+
		"\u0000\u02aeE\u0001\u0000\u0000\u0000\u02af\u02b0\u0005\u0001\u0000\u0000"+
		"\u02b0\u02b1\u0003\b\u0004\u0000\u02b1\u02b2\u0005\u0002\u0000\u0000\u02b2"+
		"\u02ba\u0003F#\u0000\u02b3\u02b4\u0005\u0004\u0000\u0000\u02b4\u02b5\u0003"+
		"\b\u0004\u0000\u02b5\u02b6\u0005\u0002\u0000\u0000\u02b6\u02b7\u0003F"+
		"#\u0000\u02b7\u02b9\u0001\u0000\u0000\u0000\u02b8\u02b3\u0001\u0000\u0000"+
		"\u0000\u02b9\u02bc\u0001\u0000\u0000\u0000\u02ba\u02b8\u0001\u0000\u0000"+
		"\u0000\u02ba\u02bb\u0001\u0000\u0000\u0000\u02bb\u02bd\u0001\u0000\u0000"+
		"\u0000\u02bc\u02ba\u0001\u0000\u0000\u0000\u02bd\u02be\u0005\u0003\u0000"+
		"\u0000\u02be\u02bf\u0003F#\u0000\u02bf\u02c0\u0005\u0005\u0000\u0000\u02c0"+
		"\u02e8\u0001\u0000\u0000\u0000\u02c1\u02c2\u0005\u0001\u0000\u0000\u02c2"+
		"\u02c3\u0005\'\u0000\u0000\u02c3\u02c4\u0003\b\u0004\u0000\u02c4\u02c5"+
		"\u0007\u0005\u0000\u0000\u02c5\u02cd\u0003F#\u0000\u02c6\u02c7\u0007\u0005"+
		"\u0000\u0000\u02c7\u02c8\u0003\b\u0004\u0000\u02c8\u02c9\u0007\u0005\u0000"+
		"\u0000\u02c9\u02ca\u0003F#\u0000\u02ca\u02cc\u0001\u0000\u0000\u0000\u02cb"+
		"\u02c6\u0001\u0000\u0000\u0000\u02cc\u02cf\u0001\u0000\u0000\u0000\u02cd"+
		"\u02cb\u0001\u0000\u0000\u0000\u02cd\u02ce\u0001\u0000\u0000\u0000\u02ce"+
		"\u02d0\u0001\u0000\u0000\u0000\u02cf\u02cd\u0001\u0000\u0000\u0000\u02d0"+
		"\u02d1\u0007\u0005\u0000\u0000\u02d1\u02d2\u0003F#\u0000\u02d2\u02d3\u0005"+
		"(\u0000\u0000\u02d3\u02e8\u0001\u0000\u0000\u0000\u02d4\u02d5\u0005)\u0000"+
		"\u0000\u02d5\u02da\u0003.\u0017\u0000\u02d6\u02d7\u0005+\u0000\u0000\u02d7"+
		"\u02d9\u0003.\u0017\u0000\u02d8\u02d6\u0001\u0000\u0000\u0000\u02d9\u02dc"+
		"\u0001\u0000\u0000\u0000\u02da\u02d8\u0001\u0000\u0000\u0000\u02da\u02db"+
		"\u0001\u0000\u0000\u0000\u02db\u02dd\u0001\u0000\u0000\u0000\u02dc\u02da"+
		"\u0001\u0000\u0000\u0000\u02dd\u02de\u0005*\u0000\u0000\u02de\u02e8\u0001"+
		"\u0000\u0000\u0000\u02df\u02e1\u0005>\u0000\u0000\u02e0\u02df\u0001\u0000"+
		"\u0000\u0000\u02e0\u02e1\u0001\u0000\u0000\u0000\u02e1\u02e2\u0001\u0000"+
		"\u0000\u0000\u02e2\u02e5\u0003(\u0014\u0000\u02e3\u02e4\u0005.\u0000\u0000"+
		"\u02e4\u02e6\u0003F#\u0000\u02e5\u02e3\u0001\u0000\u0000\u0000\u02e5\u02e6"+
		"\u0001\u0000\u0000\u0000\u02e6\u02e8\u0001\u0000\u0000\u0000\u02e7\u02af"+
		"\u0001\u0000\u0000\u0000\u02e7\u02c1\u0001\u0000\u0000\u0000\u02e7\u02d4"+
		"\u0001\u0000\u0000\u0000\u02e7\u02e0\u0001\u0000\u0000\u0000\u02e8G\u0001"+
		"\u0000\u0000\u0000\u02e9\u02ea\u0005)\u0000\u0000\u02ea\u02ef\u00050\u0000"+
		"\u0000\u02eb\u02ec\u0005+\u0000\u0000\u02ec\u02ee\u00050\u0000\u0000\u02ed"+
		"\u02eb\u0001\u0000\u0000\u0000\u02ee\u02f1\u0001\u0000\u0000\u0000\u02ef"+
		"\u02ed\u0001\u0000\u0000\u0000\u02ef\u02f0\u0001\u0000\u0000\u0000\u02f0"+
		"\u02f2\u0001\u0000\u0000\u0000\u02f1\u02ef\u0001\u0000\u0000\u0000\u02f2"+
		"\u02f3\u0005*\u0000\u0000\u02f3I\u0001\u0000\u0000\u0000IMV]mv~\u0085"+
		"\u00a3\u00aa\u00b0\u00b7\u00c0\u00c8\u00ce\u00d5\u00db\u00e1\u00f2\u00fb"+
		"\u00fe\u0107\u010a\u0118\u011b\u0126\u0129\u012c\u0134\u013e\u0148\u0155"+
		"\u0168\u0171\u017f\u018d\u01a0\u01a8\u01ad\u01af\u01bc\u01cf\u01d8\u01dd"+
		"\u01df\u01e6\u01f4\u0207\u0210\u0215\u0217\u0224\u0237\u0241\u0246\u0248"+
		"\u0255\u0268\u0272\u0277\u0279\u0286\u0299\u02a2\u02a6\u02ab\u02ad\u02ba"+
		"\u02cd\u02da\u02e0\u02e5\u02e7\u02ef";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}