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
		RPAREN=40, LBRACKET=41, RBRACKET=42, COMMA=43, SEMI=44, DOUBLE_PERIOD=45, 
		PERIOD=46, NULLCOALESCE=47, SAFE_NAV=48, QUESTION=49, AT=50, ARROW=51, 
		IN=52, NIN=53, NOT_KW=54, BETWEEN=55, IDENTIFIER=56, STRING=57, NUMBER=58, 
		POSITIVE=59, DATE=60, TIME=61, TIME_OFFSET=62, DATETIME=63, COLON_OP=64, 
		BOOLEAN_TYPE=65, NUMBER_TYPE=66, STRING_TYPE=67, DATE_TYPE=68, TIME_TYPE=69, 
		DATETIME_TYPE=70, VECTOR_TYPE=71, LINE_COMMENT=72, BLOCK_COMMENT=73, WS=74, 
		ERROR_CHAR=75;
	public static final int
		RULE_mathStart = 0, RULE_assignmentStart = 1, RULE_logicalStart = 2, RULE_assignmentExpression = 3, 
		RULE_logicalExpression = 4, RULE_logicalOrExpression = 5, RULE_logicalAndExpression = 6, 
		RULE_logicalComparisonExpression = 7, RULE_logicalBitwiseExpression = 8, 
		RULE_logicalNotExpression = 9, RULE_logicalPrimary = 10, RULE_mathExpression = 11, 
		RULE_sumExpression = 12, RULE_multiplicationExpression = 13, RULE_unaryExpression = 14, 
		RULE_rootExpression = 15, RULE_exponentiationExpression = 16, RULE_postfixExpression = 17, 
		RULE_primaryMathExpression = 18, RULE_function = 19, RULE_referenceTarget = 20, 
		RULE_collectionFunctionArguments = 21, RULE_lambdaTransform = 22, RULE_memberChain = 23, 
		RULE_subscript = 24, RULE_signedInteger = 25, RULE_filterPredicate = 26, 
		RULE_filterAtom = 27, RULE_filterRelation = 28, RULE_filterValue = 29, 
		RULE_comparisonOperator = 30, RULE_allEntityTypes = 31, RULE_assignmentValue = 32, 
		RULE_genericEntity = 33, RULE_typeHintExpression = 34, RULE_typeHint = 35, 
		RULE_logicalEntity = 36, RULE_numericEntity = 37, RULE_stringConcatExpression = 38, 
		RULE_stringEntity = 39, RULE_dateEntity = 40, RULE_timeEntity = 41, RULE_dateTimeEntity = 42, 
		RULE_vectorEntity = 43, RULE_vectorOfVariables = 44;
	private static String[] makeRuleNames() {
		return new String[] {
			"mathStart", "assignmentStart", "logicalStart", "assignmentExpression", 
			"logicalExpression", "logicalOrExpression", "logicalAndExpression", "logicalComparisonExpression", 
			"logicalBitwiseExpression", "logicalNotExpression", "logicalPrimary", 
			"mathExpression", "sumExpression", "multiplicationExpression", "unaryExpression", 
			"rootExpression", "exponentiationExpression", "postfixExpression", "primaryMathExpression", 
			"function", "referenceTarget", "collectionFunctionArguments", "lambdaTransform", 
			"memberChain", "subscript", "signedInteger", "filterPredicate", "filterAtom", 
			"filterRelation", "filterValue", "comparisonOperator", "allEntityTypes", 
			"assignmentValue", "genericEntity", "typeHintExpression", "typeHint", 
			"logicalEntity", "numericEntity", "stringConcatExpression", "stringEntity", 
			"dateEntity", "timeEntity", "dateTimeEntity", "vectorEntity", "vectorOfVariables"
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
			"','", "';'", "'..'", "'.'", "'??'", "'?.'", "'?'", "'@'", "'->'", "'in'", 
			"'nin'", "'not'", "'between'", null, null, null, null, null, null, null, 
			null, "':'", "'<bool>'", "'<number>'", "'<text>'", "'<date>'", "'<time>'", 
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
			"RPAREN", "LBRACKET", "RBRACKET", "COMMA", "SEMI", "DOUBLE_PERIOD", "PERIOD", 
			"NULLCOALESCE", "SAFE_NAV", "QUESTION", "AT", "ARROW", "IN", "NIN", "NOT_KW", 
			"BETWEEN", "IDENTIFIER", "STRING", "NUMBER", "POSITIVE", "DATE", "TIME", 
			"TIME_OFFSET", "DATETIME", "COLON_OP", "BOOLEAN_TYPE", "NUMBER_TYPE", 
			"STRING_TYPE", "DATE_TYPE", "TIME_TYPE", "DATETIME_TYPE", "VECTOR_TYPE", 
			"LINE_COMMENT", "BLOCK_COMMENT", "WS", "ERROR_CHAR"
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
			setState(93);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
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
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(96);
			mathExpression();
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
			setState(100); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(99);
				assignmentExpression();
				}
				}
				setState(102); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==LBRACKET || _la==IDENTIFIER );
			setState(104);
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
			setState(109);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(106);
					assignmentExpression();
					}
					} 
				}
				setState(111);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			setState(112);
			logicalExpression();
			setState(113);
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
			setState(125);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				_localctx = new AssignmentOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(115);
				match(IDENTIFIER);
				setState(116);
				match(EQ);
				setState(117);
				assignmentValue();
				setState(118);
				match(SEMI);
				}
				break;
			case LBRACKET:
				_localctx = new DestructuringAssignmentOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(120);
				vectorOfVariables();
				setState(121);
				match(EQ);
				setState(122);
				vectorEntity();
				setState(123);
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
			setState(127);
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
		try {
			int _alt;
			_localctx = new LogicalOrChainOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			logicalAndExpression();
			setState(134);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(130);
					match(OR);
					setState(131);
					logicalAndExpression();
					}
					} 
				}
				setState(136);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
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
		try {
			int _alt;
			_localctx = new LogicalAndChainOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(137);
			logicalComparisonExpression();
			setState(142);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(138);
					match(AND);
					setState(139);
					logicalComparisonExpression();
					}
					} 
				}
				setState(144);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
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
		try {
			setState(305);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				_localctx = new LogicalComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(145);
				logicalBitwiseExpression();
				setState(149);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
				case 1:
					{
					setState(146);
					comparisonOperator();
					setState(147);
					logicalBitwiseExpression();
					}
					break;
				}
				}
				break;
			case 2:
				_localctx = new MathComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(151);
				mathExpression();
				setState(152);
				comparisonOperator();
				setState(153);
				mathExpression();
				}
				break;
			case 3:
				_localctx = new StringComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(155);
				stringConcatExpression();
				setState(156);
				comparisonOperator();
				setState(157);
				stringConcatExpression();
				}
				break;
			case 4:
				_localctx = new DateComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(159);
				dateEntity();
				setState(160);
				comparisonOperator();
				setState(161);
				dateEntity();
				}
				break;
			case 5:
				_localctx = new TimeComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(163);
				timeEntity();
				setState(164);
				comparisonOperator();
				setState(165);
				timeEntity();
				}
				break;
			case 6:
				_localctx = new DateTimeComparisonOperationContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(167);
				dateTimeEntity();
				setState(168);
				comparisonOperator();
				setState(169);
				dateTimeEntity();
				}
				break;
			case 7:
				_localctx = new RegexMatchOperationContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(171);
				stringConcatExpression();
				setState(172);
				match(REGEX_MATCH);
				setState(173);
				match(STRING);
				}
				break;
			case 8:
				_localctx = new RegexNotMatchOperationContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(175);
				stringConcatExpression();
				setState(176);
				match(REGEX_NOT_MATCH);
				setState(177);
				match(STRING);
				}
				break;
			case 9:
				_localctx = new MathInOperationContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(179);
				mathExpression();
				setState(180);
				match(IN);
				setState(181);
				vectorEntity();
				}
				break;
			case 10:
				_localctx = new StringInOperationContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(183);
				stringConcatExpression();
				setState(184);
				match(IN);
				setState(185);
				vectorEntity();
				}
				break;
			case 11:
				_localctx = new DateInOperationContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(187);
				dateEntity();
				setState(188);
				match(IN);
				setState(189);
				vectorEntity();
				}
				break;
			case 12:
				_localctx = new TimeInOperationContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(191);
				timeEntity();
				setState(192);
				match(IN);
				setState(193);
				vectorEntity();
				}
				break;
			case 13:
				_localctx = new DateTimeInOperationContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(195);
				dateTimeEntity();
				setState(196);
				match(IN);
				setState(197);
				vectorEntity();
				}
				break;
			case 14:
				_localctx = new LogicalInOperationContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(199);
				logicalBitwiseExpression();
				setState(200);
				match(IN);
				setState(201);
				vectorEntity();
				}
				break;
			case 15:
				_localctx = new NullInOperationContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(203);
				match(NULL);
				setState(204);
				match(IN);
				setState(205);
				vectorEntity();
				}
				break;
			case 16:
				_localctx = new MathNotInOperationContext(_localctx);
				enterOuterAlt(_localctx, 16);
				{
				setState(206);
				mathExpression();
				setState(207);
				match(NOT_KW);
				setState(208);
				match(IN);
				setState(209);
				vectorEntity();
				}
				break;
			case 17:
				_localctx = new StringNotInOperationContext(_localctx);
				enterOuterAlt(_localctx, 17);
				{
				setState(211);
				stringConcatExpression();
				setState(212);
				match(NOT_KW);
				setState(213);
				match(IN);
				setState(214);
				vectorEntity();
				}
				break;
			case 18:
				_localctx = new DateNotInOperationContext(_localctx);
				enterOuterAlt(_localctx, 18);
				{
				setState(216);
				dateEntity();
				setState(217);
				match(NOT_KW);
				setState(218);
				match(IN);
				setState(219);
				vectorEntity();
				}
				break;
			case 19:
				_localctx = new TimeNotInOperationContext(_localctx);
				enterOuterAlt(_localctx, 19);
				{
				setState(221);
				timeEntity();
				setState(222);
				match(NOT_KW);
				setState(223);
				match(IN);
				setState(224);
				vectorEntity();
				}
				break;
			case 20:
				_localctx = new DateTimeNotInOperationContext(_localctx);
				enterOuterAlt(_localctx, 20);
				{
				setState(226);
				dateTimeEntity();
				setState(227);
				match(NOT_KW);
				setState(228);
				match(IN);
				setState(229);
				vectorEntity();
				}
				break;
			case 21:
				_localctx = new LogicalNotInOperationContext(_localctx);
				enterOuterAlt(_localctx, 21);
				{
				setState(231);
				logicalBitwiseExpression();
				setState(232);
				match(NOT_KW);
				setState(233);
				match(IN);
				setState(234);
				vectorEntity();
				}
				break;
			case 22:
				_localctx = new NullNotInOperationContext(_localctx);
				enterOuterAlt(_localctx, 22);
				{
				setState(236);
				match(NULL);
				setState(237);
				match(NOT_KW);
				setState(238);
				match(IN);
				setState(239);
				vectorEntity();
				}
				break;
			case 23:
				_localctx = new MathBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 23);
				{
				setState(240);
				mathExpression();
				setState(241);
				match(BETWEEN);
				setState(242);
				mathExpression();
				setState(243);
				match(AND);
				setState(244);
				mathExpression();
				}
				break;
			case 24:
				_localctx = new StringBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 24);
				{
				setState(246);
				stringConcatExpression();
				setState(247);
				match(BETWEEN);
				setState(248);
				stringConcatExpression();
				setState(249);
				match(AND);
				setState(250);
				stringConcatExpression();
				}
				break;
			case 25:
				_localctx = new DateBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 25);
				{
				setState(252);
				dateEntity();
				setState(253);
				match(BETWEEN);
				setState(254);
				dateEntity();
				setState(255);
				match(AND);
				setState(256);
				dateEntity();
				}
				break;
			case 26:
				_localctx = new TimeBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 26);
				{
				setState(258);
				timeEntity();
				setState(259);
				match(BETWEEN);
				setState(260);
				timeEntity();
				setState(261);
				match(AND);
				setState(262);
				timeEntity();
				}
				break;
			case 27:
				_localctx = new DateTimeBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 27);
				{
				setState(264);
				dateTimeEntity();
				setState(265);
				match(BETWEEN);
				setState(266);
				dateTimeEntity();
				setState(267);
				match(AND);
				setState(268);
				dateTimeEntity();
				}
				break;
			case 28:
				_localctx = new MathNotBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 28);
				{
				setState(270);
				mathExpression();
				setState(271);
				match(NOT_KW);
				setState(272);
				match(BETWEEN);
				setState(273);
				mathExpression();
				setState(274);
				match(AND);
				setState(275);
				mathExpression();
				}
				break;
			case 29:
				_localctx = new StringNotBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 29);
				{
				setState(277);
				stringConcatExpression();
				setState(278);
				match(NOT_KW);
				setState(279);
				match(BETWEEN);
				setState(280);
				stringConcatExpression();
				setState(281);
				match(AND);
				setState(282);
				stringConcatExpression();
				}
				break;
			case 30:
				_localctx = new DateNotBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 30);
				{
				setState(284);
				dateEntity();
				setState(285);
				match(NOT_KW);
				setState(286);
				match(BETWEEN);
				setState(287);
				dateEntity();
				setState(288);
				match(AND);
				setState(289);
				dateEntity();
				}
				break;
			case 31:
				_localctx = new TimeNotBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 31);
				{
				setState(291);
				timeEntity();
				setState(292);
				match(NOT_KW);
				setState(293);
				match(BETWEEN);
				setState(294);
				timeEntity();
				setState(295);
				match(AND);
				setState(296);
				timeEntity();
				}
				break;
			case 32:
				_localctx = new DateTimeNotBetweenOperationContext(_localctx);
				enterOuterAlt(_localctx, 32);
				{
				setState(298);
				dateTimeEntity();
				setState(299);
				match(NOT_KW);
				setState(300);
				match(BETWEEN);
				setState(301);
				dateTimeEntity();
				setState(302);
				match(AND);
				setState(303);
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
			int _alt;
			_localctx = new LogicalBitwiseOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(307);
			logicalNotExpression();
			setState(312);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(308);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 7680L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(309);
					logicalNotExpression();
					}
					} 
				}
				setState(314);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
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
			setState(318);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXCLAMATION:
			case NOT:
				_localctx = new LogicalNotOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(315);
				_la = _input.LA(1);
				if ( !(_la==EXCLAMATION || _la==NOT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(316);
				logicalNotExpression();
				}
				break;
			case IF:
			case TRUE:
			case FALSE:
			case LPAREN:
			case AT:
			case IDENTIFIER:
			case BOOLEAN_TYPE:
				_localctx = new LogicalPrimaryOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(317);
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
			setState(325);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				_localctx = new LogicalExpressionParenthesisOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(320);
				match(LPAREN);
				setState(321);
				logicalExpression();
				setState(322);
				match(RPAREN);
				}
				break;
			case IF:
			case TRUE:
			case FALSE:
			case AT:
			case IDENTIFIER:
			case BOOLEAN_TYPE:
				_localctx = new LogicalEntityOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(324);
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
			setState(327);
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
			int _alt;
			_localctx = new AdditiveOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(329);
			multiplicationExpression();
			setState(334);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(330);
					_la = _input.LA(1);
					if ( !(_la==PLUS || _la==MINUS) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(331);
					multiplicationExpression();
					}
					} 
				}
				setState(336);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
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
			int _alt;
			_localctx = new MultiplicativeOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(337);
			unaryExpression();
			setState(342);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(338);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1146880L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(339);
					unaryExpression();
					}
					} 
				}
				setState(344);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
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
			setState(348);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MINUS:
				_localctx = new UnaryMinusOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(345);
				match(MINUS);
				setState(346);
				unaryExpression();
				}
				break;
			case IF:
			case MODULUS:
			case SQRT:
			case LPAREN:
			case AT:
			case IDENTIFIER:
			case NUMBER:
			case NUMBER_TYPE:
				_localctx = new RootExpressionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(347);
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
			setState(350);
			exponentiationExpression();
			setState(355);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(351);
					match(ROOT);
					setState(352);
					exponentiationExpression();
					}
					} 
				}
				setState(357);
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
		try {
			_localctx = new ExponentiationOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(358);
			postfixExpression();
			setState(361);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				{
				setState(359);
				match(EXPONENTIATION);
				setState(360);
				unaryExpression();
				}
				break;
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
			int _alt;
			_localctx = new PostfixOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(363);
			primaryMathExpression();
			setState(367);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(364);
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
				}
				setState(369);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
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
			setState(384);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				_localctx = new MathExpressionParenthesisOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(370);
				match(LPAREN);
				setState(371);
				mathExpression();
				setState(372);
				match(RPAREN);
				}
				break;
			case SQRT:
				_localctx = new SquareRootOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(374);
				match(SQRT);
				setState(375);
				match(LPAREN);
				setState(376);
				mathExpression();
				setState(377);
				match(RPAREN);
				}
				break;
			case MODULUS:
				_localctx = new ModulusOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(379);
				match(MODULUS);
				setState(380);
				mathExpression();
				setState(381);
				match(MODULUS);
				}
				break;
			case IF:
			case AT:
			case IDENTIFIER:
			case NUMBER:
			case NUMBER_TYPE:
				_localctx = new NumericEntityOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(383);
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
			setState(386);
			match(IDENTIFIER);
			setState(387);
			match(LPAREN);
			setState(396);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -5259075226378674110L) != 0) || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 127L) != 0)) {
				{
				setState(388);
				allEntityTypes();
				setState(393);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA || _la==SEMI) {
					{
					{
					setState(389);
					_la = _input.LA(1);
					if ( !(_la==COMMA || _la==SEMI) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(390);
					allEntityTypes();
					}
					}
					setState(395);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(398);
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
	public static class AtReferenceTargetContext extends ReferenceTargetContext {
		public TerminalNode AT() { return getToken(ExpressionEvaluatorParser.AT, 0); }
		public List<MemberChainContext> memberChain() {
			return getRuleContexts(MemberChainContext.class);
		}
		public MemberChainContext memberChain(int i) {
			return getRuleContext(MemberChainContext.class,i);
		}
		public AtReferenceTargetContext(ReferenceTargetContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterAtReferenceTarget(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitAtReferenceTarget(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitAtReferenceTarget(this);
			else return visitor.visitChildren(this);
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
			setState(415);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				_localctx = new FunctionReferenceTargetContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(400);
				function();
				}
				break;
			case 2:
				_localctx = new IdentifierReferenceTargetContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(401);
				match(IDENTIFIER);
				setState(405);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 389227116232704L) != 0)) {
					{
					{
					setState(402);
					memberChain();
					}
					}
					setState(407);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 3:
				_localctx = new AtReferenceTargetContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(408);
				match(AT);
				setState(412);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 389227116232704L) != 0)) {
					{
					{
					setState(409);
					memberChain();
					}
					}
					setState(414);
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
	public static class CollectionFunctionArgumentsContext extends ParserRuleContext {
		public CollectionFunctionArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_collectionFunctionArguments; }
	 
		public CollectionFunctionArgumentsContext() { }
		public void copyFrom(CollectionFunctionArgumentsContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LambdaCollectionFunctionArgumentsContext extends CollectionFunctionArgumentsContext {
		public LambdaTransformContext lambdaTransform() {
			return getRuleContext(LambdaTransformContext.class,0);
		}
		public LambdaCollectionFunctionArgumentsContext(CollectionFunctionArgumentsContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLambdaCollectionFunctionArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLambdaCollectionFunctionArguments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLambdaCollectionFunctionArguments(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PositionalCollectionFunctionArgumentsContext extends CollectionFunctionArgumentsContext {
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
		public PositionalCollectionFunctionArgumentsContext(CollectionFunctionArgumentsContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterPositionalCollectionFunctionArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitPositionalCollectionFunctionArguments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitPositionalCollectionFunctionArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CollectionFunctionArgumentsContext collectionFunctionArguments() throws RecognitionException {
		CollectionFunctionArgumentsContext _localctx = new CollectionFunctionArgumentsContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_collectionFunctionArguments);
		int _la;
		try {
			setState(426);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				_localctx = new LambdaCollectionFunctionArgumentsContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(417);
				lambdaTransform();
				}
				break;
			case 2:
				_localctx = new PositionalCollectionFunctionArgumentsContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(418);
				allEntityTypes();
				setState(423);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(419);
					match(COMMA);
					setState(420);
					allEntityTypes();
					}
					}
					setState(425);
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
	public static class LambdaTransformContext extends ParserRuleContext {
		public LambdaTransformContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaTransform; }
	 
		public LambdaTransformContext() { }
		public void copyFrom(LambdaTransformContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AtLambdaTransformContext extends LambdaTransformContext {
		public TerminalNode AT() { return getToken(ExpressionEvaluatorParser.AT, 0); }
		public TerminalNode ARROW() { return getToken(ExpressionEvaluatorParser.ARROW, 0); }
		public AllEntityTypesContext allEntityTypes() {
			return getRuleContext(AllEntityTypesContext.class,0);
		}
		public AtLambdaTransformContext(LambdaTransformContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterAtLambdaTransform(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitAtLambdaTransform(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitAtLambdaTransform(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaTransformContext lambdaTransform() throws RecognitionException {
		LambdaTransformContext _localctx = new LambdaTransformContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_lambdaTransform);
		try {
			_localctx = new AtLambdaTransformContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(428);
			match(AT);
			setState(429);
			match(ARROW);
			setState(430);
			allEntityTypes();
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
	public static class CollectionFunctionAccessContext extends MemberChainContext {
		public TerminalNode DOUBLE_PERIOD() { return getToken(ExpressionEvaluatorParser.DOUBLE_PERIOD, 0); }
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public CollectionFunctionArgumentsContext collectionFunctionArguments() {
			return getRuleContext(CollectionFunctionArgumentsContext.class,0);
		}
		public CollectionFunctionAccessContext(MemberChainContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterCollectionFunctionAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitCollectionFunctionAccess(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitCollectionFunctionAccess(this);
			else return visitor.visitChildren(this);
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
	public static class SubscriptAccessContext extends MemberChainContext {
		public SubscriptContext subscript() {
			return getRuleContext(SubscriptContext.class,0);
		}
		public SubscriptAccessContext(MemberChainContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterSubscriptAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitSubscriptAccess(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitSubscriptAccess(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ChildWildcardAccessContext extends MemberChainContext {
		public TerminalNode PERIOD() { return getToken(ExpressionEvaluatorParser.PERIOD, 0); }
		public TerminalNode MULT() { return getToken(ExpressionEvaluatorParser.MULT, 0); }
		public ChildWildcardAccessContext(MemberChainContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterChildWildcardAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitChildWildcardAccess(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitChildWildcardAccess(this);
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
		enterRule(_localctx, 46, RULE_memberChain);
		int _la;
		try {
			setState(474);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				_localctx = new CollectionFunctionAccessContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(432);
				match(DOUBLE_PERIOD);
				setState(433);
				match(IDENTIFIER);
				setState(434);
				match(LPAREN);
				setState(436);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -5259075226378674110L) != 0) || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 127L) != 0)) {
					{
					setState(435);
					collectionFunctionArguments();
					}
				}

				setState(438);
				match(RPAREN);
				}
				break;
			case 2:
				_localctx = new ChildWildcardAccessContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(439);
				match(PERIOD);
				setState(440);
				match(MULT);
				}
				break;
			case 3:
				_localctx = new MethodCallAccessContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(441);
				match(PERIOD);
				setState(442);
				match(IDENTIFIER);
				setState(443);
				match(LPAREN);
				setState(452);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -5259075226378674110L) != 0) || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 127L) != 0)) {
					{
					setState(444);
					allEntityTypes();
					setState(449);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(445);
						match(COMMA);
						setState(446);
						allEntityTypes();
						}
						}
						setState(451);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(454);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new SafeMethodCallAccessContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(455);
				match(SAFE_NAV);
				setState(456);
				match(IDENTIFIER);
				setState(457);
				match(LPAREN);
				setState(466);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -5259075226378674110L) != 0) || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 127L) != 0)) {
					{
					setState(458);
					allEntityTypes();
					setState(463);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(459);
						match(COMMA);
						setState(460);
						allEntityTypes();
						}
						}
						setState(465);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(468);
				match(RPAREN);
				}
				break;
			case 5:
				_localctx = new PropertyAccessContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(469);
				match(PERIOD);
				setState(470);
				match(IDENTIFIER);
				}
				break;
			case 6:
				_localctx = new SafePropertyAccessContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(471);
				match(SAFE_NAV);
				setState(472);
				match(IDENTIFIER);
				}
				break;
			case 7:
				_localctx = new SubscriptAccessContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(473);
				subscript();
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
	public static class SubscriptContext extends ParserRuleContext {
		public SubscriptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subscript; }
	 
		public SubscriptContext() { }
		public void copyFrom(SubscriptContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SliceWithStartSubscriptContext extends SubscriptContext {
		public TerminalNode LBRACKET() { return getToken(ExpressionEvaluatorParser.LBRACKET, 0); }
		public List<SignedIntegerContext> signedInteger() {
			return getRuleContexts(SignedIntegerContext.class);
		}
		public SignedIntegerContext signedInteger(int i) {
			return getRuleContext(SignedIntegerContext.class,i);
		}
		public TerminalNode COLON_OP() { return getToken(ExpressionEvaluatorParser.COLON_OP, 0); }
		public TerminalNode RBRACKET() { return getToken(ExpressionEvaluatorParser.RBRACKET, 0); }
		public SliceWithStartSubscriptContext(SubscriptContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterSliceWithStartSubscript(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitSliceWithStartSubscript(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitSliceWithStartSubscript(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class WildcardSubscriptContext extends SubscriptContext {
		public TerminalNode LBRACKET() { return getToken(ExpressionEvaluatorParser.LBRACKET, 0); }
		public TerminalNode MULT() { return getToken(ExpressionEvaluatorParser.MULT, 0); }
		public TerminalNode RBRACKET() { return getToken(ExpressionEvaluatorParser.RBRACKET, 0); }
		public WildcardSubscriptContext(SubscriptContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterWildcardSubscript(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitWildcardSubscript(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitWildcardSubscript(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SliceToEndSubscriptContext extends SubscriptContext {
		public TerminalNode LBRACKET() { return getToken(ExpressionEvaluatorParser.LBRACKET, 0); }
		public TerminalNode COLON_OP() { return getToken(ExpressionEvaluatorParser.COLON_OP, 0); }
		public SignedIntegerContext signedInteger() {
			return getRuleContext(SignedIntegerContext.class,0);
		}
		public TerminalNode RBRACKET() { return getToken(ExpressionEvaluatorParser.RBRACKET, 0); }
		public SliceToEndSubscriptContext(SubscriptContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterSliceToEndSubscript(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitSliceToEndSubscript(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitSliceToEndSubscript(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FilterSubscriptContext extends SubscriptContext {
		public TerminalNode LBRACKET() { return getToken(ExpressionEvaluatorParser.LBRACKET, 0); }
		public TerminalNode QUESTION() { return getToken(ExpressionEvaluatorParser.QUESTION, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public FilterPredicateContext filterPredicate() {
			return getRuleContext(FilterPredicateContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public TerminalNode RBRACKET() { return getToken(ExpressionEvaluatorParser.RBRACKET, 0); }
		public FilterSubscriptContext(SubscriptContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterFilterSubscript(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitFilterSubscript(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitFilterSubscript(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringKeySubscriptContext extends SubscriptContext {
		public TerminalNode LBRACKET() { return getToken(ExpressionEvaluatorParser.LBRACKET, 0); }
		public TerminalNode STRING() { return getToken(ExpressionEvaluatorParser.STRING, 0); }
		public TerminalNode RBRACKET() { return getToken(ExpressionEvaluatorParser.RBRACKET, 0); }
		public StringKeySubscriptContext(SubscriptContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringKeySubscript(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringKeySubscript(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringKeySubscript(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SliceTimeSubscriptContext extends SubscriptContext {
		public TerminalNode LBRACKET() { return getToken(ExpressionEvaluatorParser.LBRACKET, 0); }
		public TerminalNode TIME() { return getToken(ExpressionEvaluatorParser.TIME, 0); }
		public TerminalNode RBRACKET() { return getToken(ExpressionEvaluatorParser.RBRACKET, 0); }
		public SliceTimeSubscriptContext(SubscriptContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterSliceTimeSubscript(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitSliceTimeSubscript(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitSliceTimeSubscript(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IndexSubscriptContext extends SubscriptContext {
		public TerminalNode LBRACKET() { return getToken(ExpressionEvaluatorParser.LBRACKET, 0); }
		public SignedIntegerContext signedInteger() {
			return getRuleContext(SignedIntegerContext.class,0);
		}
		public TerminalNode RBRACKET() { return getToken(ExpressionEvaluatorParser.RBRACKET, 0); }
		public IndexSubscriptContext(SubscriptContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterIndexSubscript(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitIndexSubscript(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitIndexSubscript(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubscriptContext subscript() throws RecognitionException {
		SubscriptContext _localctx = new SubscriptContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_subscript);
		int _la;
		try {
			setState(509);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				_localctx = new WildcardSubscriptContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(476);
				match(LBRACKET);
				setState(477);
				match(MULT);
				setState(478);
				match(RBRACKET);
				}
				break;
			case 2:
				_localctx = new StringKeySubscriptContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(479);
				match(LBRACKET);
				setState(480);
				match(STRING);
				setState(481);
				match(RBRACKET);
				}
				break;
			case 3:
				_localctx = new SliceWithStartSubscriptContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(482);
				match(LBRACKET);
				setState(483);
				signedInteger();
				setState(484);
				match(COLON_OP);
				setState(486);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS || _la==NUMBER) {
					{
					setState(485);
					signedInteger();
					}
				}

				setState(488);
				match(RBRACKET);
				}
				break;
			case 4:
				_localctx = new SliceToEndSubscriptContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(490);
				match(LBRACKET);
				setState(491);
				match(COLON_OP);
				setState(492);
				signedInteger();
				setState(493);
				match(RBRACKET);
				}
				break;
			case 5:
				_localctx = new SliceTimeSubscriptContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(495);
				match(LBRACKET);
				setState(496);
				match(TIME);
				setState(497);
				match(RBRACKET);
				}
				break;
			case 6:
				_localctx = new IndexSubscriptContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(498);
				match(LBRACKET);
				setState(499);
				signedInteger();
				setState(500);
				match(RBRACKET);
				}
				break;
			case 7:
				_localctx = new FilterSubscriptContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(502);
				match(LBRACKET);
				setState(503);
				match(QUESTION);
				setState(504);
				match(LPAREN);
				setState(505);
				filterPredicate();
				setState(506);
				match(RPAREN);
				setState(507);
				match(RBRACKET);
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
	public static class SignedIntegerContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(ExpressionEvaluatorParser.NUMBER, 0); }
		public TerminalNode MINUS() { return getToken(ExpressionEvaluatorParser.MINUS, 0); }
		public SignedIntegerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_signedInteger; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterSignedInteger(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitSignedInteger(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitSignedInteger(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SignedIntegerContext signedInteger() throws RecognitionException {
		SignedIntegerContext _localctx = new SignedIntegerContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_signedInteger);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(512);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MINUS) {
				{
				setState(511);
				match(MINUS);
				}
			}

			setState(514);
			match(NUMBER);
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
	public static class FilterPredicateContext extends ParserRuleContext {
		public List<FilterAtomContext> filterAtom() {
			return getRuleContexts(FilterAtomContext.class);
		}
		public FilterAtomContext filterAtom(int i) {
			return getRuleContext(FilterAtomContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(ExpressionEvaluatorParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(ExpressionEvaluatorParser.AND, i);
		}
		public List<TerminalNode> OR() { return getTokens(ExpressionEvaluatorParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(ExpressionEvaluatorParser.OR, i);
		}
		public FilterPredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterPredicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterFilterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitFilterPredicate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitFilterPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterPredicateContext filterPredicate() throws RecognitionException {
		FilterPredicateContext _localctx = new FilterPredicateContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_filterPredicate);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(516);
			filterAtom();
			setState(521);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND || _la==OR) {
				{
				{
				setState(517);
				_la = _input.LA(1);
				if ( !(_la==AND || _la==OR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(518);
				filterAtom();
				}
				}
				setState(523);
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
	public static class FilterAtomContext extends ParserRuleContext {
		public FilterAtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterAtom; }
	 
		public FilterAtomContext() { }
		public void copyFrom(FilterAtomContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ParenFilterAtomContext extends FilterAtomContext {
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public FilterPredicateContext filterPredicate() {
			return getRuleContext(FilterPredicateContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public ParenFilterAtomContext(FilterAtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterParenFilterAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitParenFilterAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitParenFilterAtom(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RelationFilterAtomContext extends FilterAtomContext {
		public FilterRelationContext filterRelation() {
			return getRuleContext(FilterRelationContext.class,0);
		}
		public RelationFilterAtomContext(FilterAtomContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterRelationFilterAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitRelationFilterAtom(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitRelationFilterAtom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterAtomContext filterAtom() throws RecognitionException {
		FilterAtomContext _localctx = new FilterAtomContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_filterAtom);
		try {
			setState(529);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				_localctx = new ParenFilterAtomContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(524);
				match(LPAREN);
				setState(525);
				filterPredicate();
				setState(526);
				match(RPAREN);
				}
				break;
			case IF:
			case NULL:
			case AT:
			case IDENTIFIER:
			case STRING:
			case NUMBER:
			case NUMBER_TYPE:
			case STRING_TYPE:
				_localctx = new RelationFilterAtomContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(528);
				filterRelation();
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
	public static class FilterRelationContext extends ParserRuleContext {
		public FilterRelationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterRelation; }
	 
		public FilterRelationContext() { }
		public void copyFrom(FilterRelationContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TruthyFilterRelationContext extends FilterRelationContext {
		public FilterValueContext filterValue() {
			return getRuleContext(FilterValueContext.class,0);
		}
		public TruthyFilterRelationContext(FilterRelationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTruthyFilterRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTruthyFilterRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTruthyFilterRelation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonFilterRelationContext extends FilterRelationContext {
		public List<FilterValueContext> filterValue() {
			return getRuleContexts(FilterValueContext.class);
		}
		public FilterValueContext filterValue(int i) {
			return getRuleContext(FilterValueContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public ComparisonFilterRelationContext(FilterRelationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterComparisonFilterRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitComparisonFilterRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitComparisonFilterRelation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RegexNotMatchFilterRelationContext extends FilterRelationContext {
		public FilterValueContext filterValue() {
			return getRuleContext(FilterValueContext.class,0);
		}
		public TerminalNode REGEX_NOT_MATCH() { return getToken(ExpressionEvaluatorParser.REGEX_NOT_MATCH, 0); }
		public TerminalNode STRING() { return getToken(ExpressionEvaluatorParser.STRING, 0); }
		public RegexNotMatchFilterRelationContext(FilterRelationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterRegexNotMatchFilterRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitRegexNotMatchFilterRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitRegexNotMatchFilterRelation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NinFilterRelationContext extends FilterRelationContext {
		public List<FilterValueContext> filterValue() {
			return getRuleContexts(FilterValueContext.class);
		}
		public FilterValueContext filterValue(int i) {
			return getRuleContext(FilterValueContext.class,i);
		}
		public TerminalNode NIN() { return getToken(ExpressionEvaluatorParser.NIN, 0); }
		public NinFilterRelationContext(FilterRelationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNinFilterRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNinFilterRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNinFilterRelation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RegexMatchFilterRelationContext extends FilterRelationContext {
		public FilterValueContext filterValue() {
			return getRuleContext(FilterValueContext.class,0);
		}
		public TerminalNode REGEX_MATCH() { return getToken(ExpressionEvaluatorParser.REGEX_MATCH, 0); }
		public TerminalNode STRING() { return getToken(ExpressionEvaluatorParser.STRING, 0); }
		public RegexMatchFilterRelationContext(FilterRelationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterRegexMatchFilterRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitRegexMatchFilterRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitRegexMatchFilterRelation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class InFilterRelationContext extends FilterRelationContext {
		public List<FilterValueContext> filterValue() {
			return getRuleContexts(FilterValueContext.class);
		}
		public FilterValueContext filterValue(int i) {
			return getRuleContext(FilterValueContext.class,i);
		}
		public TerminalNode IN() { return getToken(ExpressionEvaluatorParser.IN, 0); }
		public InFilterRelationContext(FilterRelationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterInFilterRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitInFilterRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitInFilterRelation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterRelationContext filterRelation() throws RecognitionException {
		FilterRelationContext _localctx = new FilterRelationContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_filterRelation);
		try {
			setState(552);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				_localctx = new ComparisonFilterRelationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(531);
				filterValue();
				setState(532);
				comparisonOperator();
				setState(533);
				filterValue();
				}
				break;
			case 2:
				_localctx = new RegexMatchFilterRelationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(535);
				filterValue();
				setState(536);
				match(REGEX_MATCH);
				setState(537);
				match(STRING);
				}
				break;
			case 3:
				_localctx = new RegexNotMatchFilterRelationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(539);
				filterValue();
				setState(540);
				match(REGEX_NOT_MATCH);
				setState(541);
				match(STRING);
				}
				break;
			case 4:
				_localctx = new InFilterRelationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(543);
				filterValue();
				setState(544);
				match(IN);
				setState(545);
				filterValue();
				}
				break;
			case 5:
				_localctx = new NinFilterRelationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(547);
				filterValue();
				setState(548);
				match(NIN);
				setState(549);
				filterValue();
				}
				break;
			case 6:
				_localctx = new TruthyFilterRelationContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(551);
				filterValue();
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
	public static class FilterValueContext extends ParserRuleContext {
		public FilterValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterValue; }
	 
		public FilterValueContext() { }
		public void copyFrom(FilterValueContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CurrentElementFilterValueContext extends FilterValueContext {
		public TerminalNode AT() { return getToken(ExpressionEvaluatorParser.AT, 0); }
		public List<MemberChainContext> memberChain() {
			return getRuleContexts(MemberChainContext.class);
		}
		public MemberChainContext memberChain(int i) {
			return getRuleContext(MemberChainContext.class,i);
		}
		public CurrentElementFilterValueContext(FilterValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterCurrentElementFilterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitCurrentElementFilterValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitCurrentElementFilterValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumericFilterValueContext extends FilterValueContext {
		public NumericEntityContext numericEntity() {
			return getRuleContext(NumericEntityContext.class,0);
		}
		public NumericFilterValueContext(FilterValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNumericFilterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNumericFilterValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNumericFilterValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NullFilterValueContext extends FilterValueContext {
		public TerminalNode NULL() { return getToken(ExpressionEvaluatorParser.NULL, 0); }
		public NullFilterValueContext(FilterValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNullFilterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNullFilterValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNullFilterValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExternalRefFilterValueContext extends FilterValueContext {
		public List<TerminalNode> IDENTIFIER() { return getTokens(ExpressionEvaluatorParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(ExpressionEvaluatorParser.IDENTIFIER, i);
		}
		public List<TerminalNode> PERIOD() { return getTokens(ExpressionEvaluatorParser.PERIOD); }
		public TerminalNode PERIOD(int i) {
			return getToken(ExpressionEvaluatorParser.PERIOD, i);
		}
		public ExternalRefFilterValueContext(FilterValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterExternalRefFilterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitExternalRefFilterValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitExternalRefFilterValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringFilterValueContext extends FilterValueContext {
		public StringConcatExpressionContext stringConcatExpression() {
			return getRuleContext(StringConcatExpressionContext.class,0);
		}
		public StringFilterValueContext(FilterValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringFilterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringFilterValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringFilterValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterValueContext filterValue() throws RecognitionException {
		FilterValueContext _localctx = new FilterValueContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_filterValue);
		int _la;
		try {
			setState(572);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				_localctx = new CurrentElementFilterValueContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(554);
				match(AT);
				setState(558);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 389227116232704L) != 0)) {
					{
					{
					setState(555);
					memberChain();
					}
					}
					setState(560);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				_localctx = new ExternalRefFilterValueContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(561);
				match(IDENTIFIER);
				setState(566);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==PERIOD) {
					{
					{
					setState(562);
					match(PERIOD);
					setState(563);
					match(IDENTIFIER);
					}
					}
					setState(568);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 3:
				_localctx = new NumericFilterValueContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(569);
				numericEntity();
				}
				break;
			case 4:
				_localctx = new StringFilterValueContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(570);
				stringConcatExpression();
				}
				break;
			case 5:
				_localctx = new NullFilterValueContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(571);
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
		enterRule(_localctx, 60, RULE_comparisonOperator);
		try {
			setState(580);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case GT:
				_localctx = new GreaterThanOperatorContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(574);
				match(GT);
				}
				break;
			case GE:
				_localctx = new GreaterThanOrEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(575);
				match(GE);
				}
				break;
			case LT:
				_localctx = new LessThanOperatorContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(576);
				match(LT);
				}
				break;
			case LE:
				_localctx = new LessThanOrEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(577);
				match(LE);
				}
				break;
			case EQ:
				_localctx = new EqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(578);
				match(EQ);
				}
				break;
			case NEQ:
				_localctx = new NotEqualOperatorContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(579);
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
		enterRule(_localctx, 62, RULE_allEntityTypes);
		try {
			setState(590);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				_localctx = new MathEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(582);
				mathExpression();
				}
				break;
			case 2:
				_localctx = new LogicalEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(583);
				logicalExpression();
				}
				break;
			case 3:
				_localctx = new DateEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(584);
				dateEntity();
				}
				break;
			case 4:
				_localctx = new TimeEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(585);
				timeEntity();
				}
				break;
			case 5:
				_localctx = new DateTimeEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(586);
				dateTimeEntity();
				}
				break;
			case 6:
				_localctx = new StringEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(587);
				stringConcatExpression();
				}
				break;
			case 7:
				_localctx = new VectorEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(588);
				vectorEntity();
				}
				break;
			case 8:
				_localctx = new NullEntityTypeContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(589);
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
		enterRule(_localctx, 64, RULE_assignmentValue);
		try {
			setState(600);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				_localctx = new GenericAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(592);
				genericEntity();
				}
				break;
			case 2:
				_localctx = new MathAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(593);
				mathExpression();
				}
				break;
			case 3:
				_localctx = new LogicalAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(594);
				logicalExpression();
				}
				break;
			case 4:
				_localctx = new DateAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(595);
				dateEntity();
				}
				break;
			case 5:
				_localctx = new TimeAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(596);
				timeEntity();
				}
				break;
			case 6:
				_localctx = new DateTimeAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(597);
				dateTimeEntity();
				}
				break;
			case 7:
				_localctx = new StringAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(598);
				stringConcatExpression();
				}
				break;
			case 8:
				_localctx = new VectorAssignmentValueContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(599);
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
	public static class TypeHintExpressionOperationContext extends GenericEntityContext {
		public TypeHintExpressionContext typeHintExpression() {
			return getRuleContext(TypeHintExpressionContext.class,0);
		}
		public TypeHintExpressionOperationContext(GenericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTypeHintExpressionOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTypeHintExpressionOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTypeHintExpressionOperation(this);
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
		enterRule(_localctx, 66, RULE_genericEntity);
		int _la;
		try {
			int _alt;
			setState(641);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
			case 1:
				_localctx = new GenericDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(602);
				match(IF);
				setState(603);
				logicalExpression();
				setState(604);
				match(THEN);
				setState(605);
				genericEntity();
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
					genericEntity();
					}
					}
					setState(615);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(616);
				match(ELSE);
				setState(617);
				genericEntity();
				setState(618);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new GenericFunctionDecisionExpressionContext(_localctx);
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
				genericEntity();
				setState(632);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,44,_ctx);
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
						genericEntity();
						}
						} 
					}
					setState(634);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,44,_ctx);
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
				genericEntity();
				setState(637);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new TypeHintExpressionOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(639);
				typeHintExpression();
				}
				break;
			case 4:
				_localctx = new ReferenceTargetOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(640);
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
	public static class TypeHintExpressionContext extends ParserRuleContext {
		public TypeHintExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeHintExpression; }
	 
		public TypeHintExpressionContext() { }
		public void copyFrom(TypeHintExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TypeHintOperationContext extends TypeHintExpressionContext {
		public TypeHintContext typeHint() {
			return getRuleContext(TypeHintContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public GenericEntityContext genericEntity() {
			return getRuleContext(GenericEntityContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public TypeHintOperationContext(TypeHintExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTypeHintOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTypeHintOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTypeHintOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeHintExpressionContext typeHintExpression() throws RecognitionException {
		TypeHintExpressionContext _localctx = new TypeHintExpressionContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_typeHintExpression);
		try {
			_localctx = new TypeHintOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(643);
			typeHint();
			setState(644);
			match(LPAREN);
			setState(645);
			genericEntity();
			setState(646);
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
		enterRule(_localctx, 70, RULE_typeHint);
		try {
			setState(655);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BOOLEAN_TYPE:
				_localctx = new BooleanTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(648);
				match(BOOLEAN_TYPE);
				}
				break;
			case NUMBER_TYPE:
				_localctx = new NumberTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(649);
				match(NUMBER_TYPE);
				}
				break;
			case STRING_TYPE:
				_localctx = new StringTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(650);
				match(STRING_TYPE);
				}
				break;
			case DATE_TYPE:
				_localctx = new DateTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(651);
				match(DATE_TYPE);
				}
				break;
			case TIME_TYPE:
				_localctx = new TimeTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(652);
				match(TIME_TYPE);
				}
				break;
			case DATETIME_TYPE:
				_localctx = new DateTimeTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(653);
				match(DATETIME_TYPE);
				}
				break;
			case VECTOR_TYPE:
				_localctx = new VectorTypeHintContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(654);
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
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
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
		enterRule(_localctx, 72, RULE_logicalEntity);
		int _la;
		try {
			int _alt;
			setState(703);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
			case 1:
				_localctx = new LogicalConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(657);
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
				setState(658);
				match(IF);
				setState(659);
				logicalExpression();
				setState(660);
				match(THEN);
				setState(661);
				logicalExpression();
				setState(669);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(662);
					match(ELSEIF);
					setState(663);
					logicalExpression();
					setState(664);
					match(THEN);
					setState(665);
					logicalExpression();
					}
					}
					setState(671);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(672);
				match(ELSE);
				setState(673);
				logicalExpression();
				setState(674);
				match(ENDIF);
				}
				break;
			case 3:
				_localctx = new LogicalFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(676);
				match(IF);
				setState(677);
				match(LPAREN);
				setState(678);
				logicalExpression();
				setState(679);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(680);
				logicalExpression();
				setState(688);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,48,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(681);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(682);
						logicalExpression();
						setState(683);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(684);
						logicalExpression();
						}
						} 
					}
					setState(690);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,48,_ctx);
				}
				setState(691);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(692);
				logicalExpression();
				setState(693);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new LogicalReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(696);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==BOOLEAN_TYPE) {
					{
					setState(695);
					match(BOOLEAN_TYPE);
					}
				}

				setState(698);
				referenceTarget();
				setState(701);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(699);
					match(NULLCOALESCE);
					setState(700);
					logicalExpression();
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
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
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
		enterRule(_localctx, 74, RULE_numericEntity);
		int _la;
		try {
			int _alt;
			setState(751);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				_localctx = new MathDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(705);
				match(IF);
				setState(706);
				logicalExpression();
				setState(707);
				match(THEN);
				setState(708);
				mathExpression();
				setState(716);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(709);
					match(ELSEIF);
					setState(710);
					logicalExpression();
					setState(711);
					match(THEN);
					setState(712);
					mathExpression();
					}
					}
					setState(718);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(719);
				match(ELSE);
				setState(720);
				mathExpression();
				setState(721);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new MathFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(723);
				match(IF);
				setState(724);
				match(LPAREN);
				setState(725);
				logicalExpression();
				setState(726);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(727);
				mathExpression();
				setState(735);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,53,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(728);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(729);
						logicalExpression();
						setState(730);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(731);
						mathExpression();
						}
						} 
					}
					setState(737);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,53,_ctx);
				}
				setState(738);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(739);
				mathExpression();
				setState(740);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new NumericConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(742);
				match(NUMBER);
				}
				break;
			case 4:
				_localctx = new NumericReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(744);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NUMBER_TYPE) {
					{
					setState(743);
					match(NUMBER_TYPE);
					}
				}

				setState(746);
				referenceTarget();
				setState(749);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(747);
					match(NULLCOALESCE);
					setState(748);
					mathExpression();
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
		enterRule(_localctx, 76, RULE_stringConcatExpression);
		try {
			int _alt;
			_localctx = new StringConcatenationOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(753);
			stringEntity();
			setState(758);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,57,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(754);
					match(CONCAT);
					setState(755);
					stringEntity();
					}
					} 
				}
				setState(760);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,57,_ctx);
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
		enterRule(_localctx, 78, RULE_stringEntity);
		int _la;
		try {
			int _alt;
			setState(807);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,62,_ctx) ) {
			case 1:
				_localctx = new StringDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(761);
				match(IF);
				setState(762);
				logicalExpression();
				setState(763);
				match(THEN);
				setState(764);
				stringConcatExpression();
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
					stringConcatExpression();
					}
					}
					setState(774);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(775);
				match(ELSE);
				setState(776);
				stringConcatExpression();
				setState(777);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new StringFunctionDecisionOperationContext(_localctx);
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
				stringConcatExpression();
				setState(791);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,59,_ctx);
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
						stringConcatExpression();
						}
						} 
					}
					setState(793);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,59,_ctx);
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
				stringConcatExpression();
				setState(796);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new StringConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(798);
				match(STRING);
				}
				break;
			case 4:
				_localctx = new StringReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(800);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING_TYPE) {
					{
					setState(799);
					match(STRING_TYPE);
					}
				}

				setState(802);
				referenceTarget();
				setState(805);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(803);
					match(NULLCOALESCE);
					setState(804);
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
		enterRule(_localctx, 80, RULE_dateEntity);
		int _la;
		try {
			int _alt;
			setState(856);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,67,_ctx) ) {
			case 1:
				_localctx = new DateDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(809);
				match(IF);
				setState(810);
				logicalExpression();
				setState(811);
				match(THEN);
				setState(812);
				dateEntity();
				setState(820);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(813);
					match(ELSEIF);
					setState(814);
					logicalExpression();
					setState(815);
					match(THEN);
					setState(816);
					dateEntity();
					}
					}
					setState(822);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(823);
				match(ELSE);
				setState(824);
				dateEntity();
				setState(825);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new DateFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(827);
				match(IF);
				setState(828);
				match(LPAREN);
				setState(829);
				logicalExpression();
				setState(830);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(831);
				dateEntity();
				setState(839);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,64,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(832);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
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
						dateEntity();
						}
						} 
					}
					setState(841);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,64,_ctx);
				}
				setState(842);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(843);
				dateEntity();
				setState(844);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new DateConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(846);
				match(DATE);
				}
				break;
			case 4:
				_localctx = new DateCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(847);
				match(NOW_DATE);
				}
				break;
			case 5:
				_localctx = new DateReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(849);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DATE_TYPE) {
					{
					setState(848);
					match(DATE_TYPE);
					}
				}

				setState(851);
				referenceTarget();
				setState(854);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(852);
					match(NULLCOALESCE);
					setState(853);
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
		enterRule(_localctx, 82, RULE_timeEntity);
		int _la;
		try {
			int _alt;
			setState(905);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,72,_ctx) ) {
			case 1:
				_localctx = new TimeDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(858);
				match(IF);
				setState(859);
				logicalExpression();
				setState(860);
				match(THEN);
				setState(861);
				timeEntity();
				setState(869);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(862);
					match(ELSEIF);
					setState(863);
					logicalExpression();
					setState(864);
					match(THEN);
					setState(865);
					timeEntity();
					}
					}
					setState(871);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(872);
				match(ELSE);
				setState(873);
				timeEntity();
				setState(874);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new TimeFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(876);
				match(IF);
				setState(877);
				match(LPAREN);
				setState(878);
				logicalExpression();
				setState(879);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(880);
				timeEntity();
				setState(888);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,69,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(881);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(882);
						logicalExpression();
						setState(883);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(884);
						timeEntity();
						}
						} 
					}
					setState(890);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,69,_ctx);
				}
				setState(891);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(892);
				timeEntity();
				setState(893);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new TimeConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(895);
				match(TIME);
				}
				break;
			case 4:
				_localctx = new TimeCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(896);
				match(NOW_TIME);
				}
				break;
			case 5:
				_localctx = new TimeReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(898);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TIME_TYPE) {
					{
					setState(897);
					match(TIME_TYPE);
					}
				}

				setState(900);
				referenceTarget();
				setState(903);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(901);
					match(NULLCOALESCE);
					setState(902);
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
		enterRule(_localctx, 84, RULE_dateTimeEntity);
		int _la;
		try {
			int _alt;
			setState(957);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
			case 1:
				_localctx = new DateTimeDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(907);
				match(IF);
				setState(908);
				logicalExpression();
				setState(909);
				match(THEN);
				setState(910);
				dateTimeEntity();
				setState(918);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(911);
					match(ELSEIF);
					setState(912);
					logicalExpression();
					setState(913);
					match(THEN);
					setState(914);
					dateTimeEntity();
					}
					}
					setState(920);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(921);
				match(ELSE);
				setState(922);
				dateTimeEntity();
				setState(923);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new DateTimeFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(925);
				match(IF);
				setState(926);
				match(LPAREN);
				setState(927);
				logicalExpression();
				setState(928);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(929);
				dateTimeEntity();
				setState(937);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,74,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(930);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(931);
						logicalExpression();
						setState(932);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(933);
						dateTimeEntity();
						}
						} 
					}
					setState(939);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,74,_ctx);
				}
				setState(940);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(941);
				dateTimeEntity();
				setState(942);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new DateTimeConstantOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(944);
				match(DATETIME);
				setState(946);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TIME_OFFSET) {
					{
					setState(945);
					match(TIME_OFFSET);
					}
				}

				}
				break;
			case 4:
				_localctx = new DateTimeCurrentValueOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(948);
				match(NOW_DATETIME);
				}
				break;
			case 5:
				_localctx = new DateTimeReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(950);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DATETIME_TYPE) {
					{
					setState(949);
					match(DATETIME_TYPE);
					}
				}

				setState(952);
				referenceTarget();
				setState(955);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(953);
					match(NULLCOALESCE);
					setState(954);
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
		enterRule(_localctx, 86, RULE_vectorEntity);
		int _la;
		try {
			int _alt;
			setState(1015);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,84,_ctx) ) {
			case 1:
				_localctx = new VectorDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(959);
				match(IF);
				setState(960);
				logicalExpression();
				setState(961);
				match(THEN);
				setState(962);
				vectorEntity();
				setState(970);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ELSEIF) {
					{
					{
					setState(963);
					match(ELSEIF);
					setState(964);
					logicalExpression();
					setState(965);
					match(THEN);
					setState(966);
					vectorEntity();
					}
					}
					setState(972);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(973);
				match(ELSE);
				setState(974);
				vectorEntity();
				setState(975);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new VectorFunctionDecisionOperationContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(977);
				match(IF);
				setState(978);
				match(LPAREN);
				setState(979);
				logicalExpression();
				setState(980);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(981);
				vectorEntity();
				setState(989);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,80,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(982);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(983);
						logicalExpression();
						setState(984);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(985);
						vectorEntity();
						}
						} 
					}
					setState(991);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,80,_ctx);
				}
				setState(992);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(993);
				vectorEntity();
				setState(994);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new VectorOfEntitiesOperationContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(996);
				match(LBRACKET);
				setState(997);
				allEntityTypes();
				setState(1002);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(998);
					match(COMMA);
					setState(999);
					allEntityTypes();
					}
					}
					setState(1004);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1005);
				match(RBRACKET);
				}
				break;
			case 4:
				_localctx = new VectorReferenceOperationContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1008);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==VECTOR_TYPE) {
					{
					setState(1007);
					match(VECTOR_TYPE);
					}
				}

				setState(1010);
				referenceTarget();
				setState(1013);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NULLCOALESCE) {
					{
					setState(1011);
					match(NULLCOALESCE);
					setState(1012);
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
		enterRule(_localctx, 88, RULE_vectorOfVariables);
		int _la;
		try {
			_localctx = new VectorOfVariablesOperationContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1017);
			match(LBRACKET);
			setState(1018);
			match(IDENTIFIER);
			setState(1023);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1019);
				match(COMMA);
				setState(1020);
				match(IDENTIFIER);
				}
				}
				setState(1025);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1026);
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
		"\u0004\u0001K\u0405\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"+
		"(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0001"+
		"\u0000\u0005\u0000\\\b\u0000\n\u0000\f\u0000_\t\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0001\u0004\u0001e\b\u0001\u000b\u0001\f\u0001"+
		"f\u0001\u0001\u0001\u0001\u0001\u0002\u0005\u0002l\b\u0002\n\u0002\f\u0002"+
		"o\t\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0003\u0003~\b\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005\u0085\b\u0005\n\u0005"+
		"\f\u0005\u0088\t\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006"+
		"\u008d\b\u0006\n\u0006\f\u0006\u0090\t\u0006\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u0096\b\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u0132\b\u0007\u0001\b\u0001\b\u0001\b\u0005"+
		"\b\u0137\b\b\n\b\f\b\u013a\t\b\u0001\t\u0001\t\u0001\t\u0003\t\u013f\b"+
		"\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u0146\b\n\u0001\u000b"+
		"\u0001\u000b\u0001\f\u0001\f\u0001\f\u0005\f\u014d\b\f\n\f\f\f\u0150\t"+
		"\f\u0001\r\u0001\r\u0001\r\u0005\r\u0155\b\r\n\r\f\r\u0158\t\r\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0003\u000e\u015d\b\u000e\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0005\u000f\u0162\b\u000f\n\u000f\f\u000f\u0165\t\u000f\u0001"+
		"\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u016a\b\u0010\u0001\u0011\u0001"+
		"\u0011\u0005\u0011\u016e\b\u0011\n\u0011\f\u0011\u0171\t\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0003\u0012\u0181\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0005\u0013\u0188\b\u0013\n\u0013\f\u0013\u018b"+
		"\t\u0013\u0003\u0013\u018d\b\u0013\u0001\u0013\u0001\u0013\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0005\u0014\u0194\b\u0014\n\u0014\f\u0014\u0197"+
		"\t\u0014\u0001\u0014\u0001\u0014\u0005\u0014\u019b\b\u0014\n\u0014\f\u0014"+
		"\u019e\t\u0014\u0003\u0014\u01a0\b\u0014\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0005\u0015\u01a6\b\u0015\n\u0015\f\u0015\u01a9\t\u0015"+
		"\u0003\u0015\u01ab\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u01b5\b\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017\u01c0\b\u0017\n\u0017"+
		"\f\u0017\u01c3\t\u0017\u0003\u0017\u01c5\b\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017"+
		"\u01ce\b\u0017\n\u0017\f\u0017\u01d1\t\u0017\u0003\u0017\u01d3\b\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0003\u0017\u01db\b\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0003\u0018\u01e7\b\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018"+
		"\u01fe\b\u0018\u0001\u0019\u0003\u0019\u0201\b\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0005\u001a\u0208\b\u001a\n"+
		"\u001a\f\u001a\u020b\t\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0003\u001b\u0212\b\u001b\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0003\u001c\u0229\b\u001c\u0001\u001d\u0001\u001d\u0005\u001d\u022d"+
		"\b\u001d\n\u001d\f\u001d\u0230\t\u001d\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0005\u001d\u0235\b\u001d\n\u001d\f\u001d\u0238\t\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001d\u0003\u001d\u023d\b\u001d\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u0245\b\u001e\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0003\u001f\u024f\b\u001f\u0001 \u0001 \u0001 \u0001"+
		" \u0001 \u0001 \u0001 \u0001 \u0003 \u0259\b \u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0005!\u0264\b!\n!\f!\u0267\t!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0005!\u0277\b!\n!\f!\u027a\t!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0003!\u0282\b!\u0001\"\u0001\"\u0001\"\u0001\""+
		"\u0001\"\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0003#\u0290"+
		"\b#\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001"+
		"$\u0005$\u029c\b$\n$\f$\u029f\t$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001"+
		"$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0005$\u02af"+
		"\b$\n$\f$\u02b2\t$\u0001$\u0001$\u0001$\u0001$\u0001$\u0003$\u02b9\b$"+
		"\u0001$\u0001$\u0001$\u0003$\u02be\b$\u0003$\u02c0\b$\u0001%\u0001%\u0001"+
		"%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0005%\u02cb\b%\n%\f%\u02ce"+
		"\t%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001"+
		"%\u0001%\u0001%\u0001%\u0001%\u0005%\u02de\b%\n%\f%\u02e1\t%\u0001%\u0001"+
		"%\u0001%\u0001%\u0001%\u0001%\u0003%\u02e9\b%\u0001%\u0001%\u0001%\u0003"+
		"%\u02ee\b%\u0003%\u02f0\b%\u0001&\u0001&\u0001&\u0005&\u02f5\b&\n&\f&"+
		"\u02f8\t&\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001"+
		"\'\u0001\'\u0005\'\u0303\b\'\n\'\f\'\u0306\t\'\u0001\'\u0001\'\u0001\'"+
		"\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001"+
		"\'\u0001\'\u0001\'\u0005\'\u0316\b\'\n\'\f\'\u0319\t\'\u0001\'\u0001\'"+
		"\u0001\'\u0001\'\u0001\'\u0001\'\u0003\'\u0321\b\'\u0001\'\u0001\'\u0001"+
		"\'\u0003\'\u0326\b\'\u0003\'\u0328\b\'\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0005(\u0333\b(\n(\f(\u0336\t(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0005(\u0346\b(\n(\f(\u0349\t(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0003(\u0352\b(\u0001(\u0001(\u0001(\u0003(\u0357"+
		"\b(\u0003(\u0359\b(\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001"+
		")\u0001)\u0005)\u0364\b)\n)\f)\u0367\t)\u0001)\u0001)\u0001)\u0001)\u0001"+
		")\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0005"+
		")\u0377\b)\n)\f)\u037a\t)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001"+
		")\u0003)\u0383\b)\u0001)\u0001)\u0001)\u0003)\u0388\b)\u0003)\u038a\b"+
		")\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0005"+
		"*\u0395\b*\n*\f*\u0398\t*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001"+
		"*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0005*\u03a8\b*\n*"+
		"\f*\u03ab\t*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0003*\u03b3\b"+
		"*\u0001*\u0001*\u0003*\u03b7\b*\u0001*\u0001*\u0001*\u0003*\u03bc\b*\u0003"+
		"*\u03be\b*\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001"+
		"+\u0005+\u03c9\b+\n+\f+\u03cc\t+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001"+
		"+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0005+\u03dc"+
		"\b+\n+\f+\u03df\t+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001"+
		"+\u0005+\u03e9\b+\n+\f+\u03ec\t+\u0001+\u0001+\u0001+\u0003+\u03f1\b+"+
		"\u0001+\u0001+\u0001+\u0003+\u03f6\b+\u0003+\u03f8\b+\u0001,\u0001,\u0001"+
		",\u0001,\u0005,\u03fe\b,\n,\f,\u0401\t,\u0001,\u0001,\u0001,\u0000\u0000"+
		"-\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a"+
		"\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVX\u0000\b\u0001\u0000\t\f\u0002"+
		"\u0000\u0017\u0017!!\u0001\u0000\u0011\u0012\u0002\u0000\u000f\u0010\u0014"+
		"\u0014\u0002\u0000\u0013\u0013\u0017\u0017\u0001\u0000+,\u0001\u0000\u0007"+
		"\b\u0001\u0000\r\u000e\u0487\u0000]\u0001\u0000\u0000\u0000\u0002d\u0001"+
		"\u0000\u0000\u0000\u0004m\u0001\u0000\u0000\u0000\u0006}\u0001\u0000\u0000"+
		"\u0000\b\u007f\u0001\u0000\u0000\u0000\n\u0081\u0001\u0000\u0000\u0000"+
		"\f\u0089\u0001\u0000\u0000\u0000\u000e\u0131\u0001\u0000\u0000\u0000\u0010"+
		"\u0133\u0001\u0000\u0000\u0000\u0012\u013e\u0001\u0000\u0000\u0000\u0014"+
		"\u0145\u0001\u0000\u0000\u0000\u0016\u0147\u0001\u0000\u0000\u0000\u0018"+
		"\u0149\u0001\u0000\u0000\u0000\u001a\u0151\u0001\u0000\u0000\u0000\u001c"+
		"\u015c\u0001\u0000\u0000\u0000\u001e\u015e\u0001\u0000\u0000\u0000 \u0166"+
		"\u0001\u0000\u0000\u0000\"\u016b\u0001\u0000\u0000\u0000$\u0180\u0001"+
		"\u0000\u0000\u0000&\u0182\u0001\u0000\u0000\u0000(\u019f\u0001\u0000\u0000"+
		"\u0000*\u01aa\u0001\u0000\u0000\u0000,\u01ac\u0001\u0000\u0000\u0000."+
		"\u01da\u0001\u0000\u0000\u00000\u01fd\u0001\u0000\u0000\u00002\u0200\u0001"+
		"\u0000\u0000\u00004\u0204\u0001\u0000\u0000\u00006\u0211\u0001\u0000\u0000"+
		"\u00008\u0228\u0001\u0000\u0000\u0000:\u023c\u0001\u0000\u0000\u0000<"+
		"\u0244\u0001\u0000\u0000\u0000>\u024e\u0001\u0000\u0000\u0000@\u0258\u0001"+
		"\u0000\u0000\u0000B\u0281\u0001\u0000\u0000\u0000D\u0283\u0001\u0000\u0000"+
		"\u0000F\u028f\u0001\u0000\u0000\u0000H\u02bf\u0001\u0000\u0000\u0000J"+
		"\u02ef\u0001\u0000\u0000\u0000L\u02f1\u0001\u0000\u0000\u0000N\u0327\u0001"+
		"\u0000\u0000\u0000P\u0358\u0001\u0000\u0000\u0000R\u0389\u0001\u0000\u0000"+
		"\u0000T\u03bd\u0001\u0000\u0000\u0000V\u03f7\u0001\u0000\u0000\u0000X"+
		"\u03f9\u0001\u0000\u0000\u0000Z\\\u0003\u0006\u0003\u0000[Z\u0001\u0000"+
		"\u0000\u0000\\_\u0001\u0000\u0000\u0000][\u0001\u0000\u0000\u0000]^\u0001"+
		"\u0000\u0000\u0000^`\u0001\u0000\u0000\u0000_]\u0001\u0000\u0000\u0000"+
		"`a\u0003\u0016\u000b\u0000ab\u0005\u0000\u0000\u0001b\u0001\u0001\u0000"+
		"\u0000\u0000ce\u0003\u0006\u0003\u0000dc\u0001\u0000\u0000\u0000ef\u0001"+
		"\u0000\u0000\u0000fd\u0001\u0000\u0000\u0000fg\u0001\u0000\u0000\u0000"+
		"gh\u0001\u0000\u0000\u0000hi\u0005\u0000\u0000\u0001i\u0003\u0001\u0000"+
		"\u0000\u0000jl\u0003\u0006\u0003\u0000kj\u0001\u0000\u0000\u0000lo\u0001"+
		"\u0000\u0000\u0000mk\u0001\u0000\u0000\u0000mn\u0001\u0000\u0000\u0000"+
		"np\u0001\u0000\u0000\u0000om\u0001\u0000\u0000\u0000pq\u0003\b\u0004\u0000"+
		"qr\u0005\u0000\u0000\u0001r\u0005\u0001\u0000\u0000\u0000st\u00058\u0000"+
		"\u0000tu\u0005\u001f\u0000\u0000uv\u0003@ \u0000vw\u0005,\u0000\u0000"+
		"w~\u0001\u0000\u0000\u0000xy\u0003X,\u0000yz\u0005\u001f\u0000\u0000z"+
		"{\u0003V+\u0000{|\u0005,\u0000\u0000|~\u0001\u0000\u0000\u0000}s\u0001"+
		"\u0000\u0000\u0000}x\u0001\u0000\u0000\u0000~\u0007\u0001\u0000\u0000"+
		"\u0000\u007f\u0080\u0003\n\u0005\u0000\u0080\t\u0001\u0000\u0000\u0000"+
		"\u0081\u0086\u0003\f\u0006\u0000\u0082\u0083\u0005\b\u0000\u0000\u0083"+
		"\u0085\u0003\f\u0006\u0000\u0084\u0082\u0001\u0000\u0000\u0000\u0085\u0088"+
		"\u0001\u0000\u0000\u0000\u0086\u0084\u0001\u0000\u0000\u0000\u0086\u0087"+
		"\u0001\u0000\u0000\u0000\u0087\u000b\u0001\u0000\u0000\u0000\u0088\u0086"+
		"\u0001\u0000\u0000\u0000\u0089\u008e\u0003\u000e\u0007\u0000\u008a\u008b"+
		"\u0005\u0007\u0000\u0000\u008b\u008d\u0003\u000e\u0007\u0000\u008c\u008a"+
		"\u0001\u0000\u0000\u0000\u008d\u0090\u0001\u0000\u0000\u0000\u008e\u008c"+
		"\u0001\u0000\u0000\u0000\u008e\u008f\u0001\u0000\u0000\u0000\u008f\r\u0001"+
		"\u0000\u0000\u0000\u0090\u008e\u0001\u0000\u0000\u0000\u0091\u0095\u0003"+
		"\u0010\b\u0000\u0092\u0093\u0003<\u001e\u0000\u0093\u0094\u0003\u0010"+
		"\b\u0000\u0094\u0096\u0001\u0000\u0000\u0000\u0095\u0092\u0001\u0000\u0000"+
		"\u0000\u0095\u0096\u0001\u0000\u0000\u0000\u0096\u0132\u0001\u0000\u0000"+
		"\u0000\u0097\u0098\u0003\u0016\u000b\u0000\u0098\u0099\u0003<\u001e\u0000"+
		"\u0099\u009a\u0003\u0016\u000b\u0000\u009a\u0132\u0001\u0000\u0000\u0000"+
		"\u009b\u009c\u0003L&\u0000\u009c\u009d\u0003<\u001e\u0000\u009d\u009e"+
		"\u0003L&\u0000\u009e\u0132\u0001\u0000\u0000\u0000\u009f\u00a0\u0003P"+
		"(\u0000\u00a0\u00a1\u0003<\u001e\u0000\u00a1\u00a2\u0003P(\u0000\u00a2"+
		"\u0132\u0001\u0000\u0000\u0000\u00a3\u00a4\u0003R)\u0000\u00a4\u00a5\u0003"+
		"<\u001e\u0000\u00a5\u00a6\u0003R)\u0000\u00a6\u0132\u0001\u0000\u0000"+
		"\u0000\u00a7\u00a8\u0003T*\u0000\u00a8\u00a9\u0003<\u001e\u0000\u00a9"+
		"\u00aa\u0003T*\u0000\u00aa\u0132\u0001\u0000\u0000\u0000\u00ab\u00ac\u0003"+
		"L&\u0000\u00ac\u00ad\u0005\"\u0000\u0000\u00ad\u00ae\u00059\u0000\u0000"+
		"\u00ae\u0132\u0001\u0000\u0000\u0000\u00af\u00b0\u0003L&\u0000\u00b0\u00b1"+
		"\u0005#\u0000\u0000\u00b1\u00b2\u00059\u0000\u0000\u00b2\u0132\u0001\u0000"+
		"\u0000\u0000\u00b3\u00b4\u0003\u0016\u000b\u0000\u00b4\u00b5\u00054\u0000"+
		"\u0000\u00b5\u00b6\u0003V+\u0000\u00b6\u0132\u0001\u0000\u0000\u0000\u00b7"+
		"\u00b8\u0003L&\u0000\u00b8\u00b9\u00054\u0000\u0000\u00b9\u00ba\u0003"+
		"V+\u0000\u00ba\u0132\u0001\u0000\u0000\u0000\u00bb\u00bc\u0003P(\u0000"+
		"\u00bc\u00bd\u00054\u0000\u0000\u00bd\u00be\u0003V+\u0000\u00be\u0132"+
		"\u0001\u0000\u0000\u0000\u00bf\u00c0\u0003R)\u0000\u00c0\u00c1\u00054"+
		"\u0000\u0000\u00c1\u00c2\u0003V+\u0000\u00c2\u0132\u0001\u0000\u0000\u0000"+
		"\u00c3\u00c4\u0003T*\u0000\u00c4\u00c5\u00054\u0000\u0000\u00c5\u00c6"+
		"\u0003V+\u0000\u00c6\u0132\u0001\u0000\u0000\u0000\u00c7\u00c8\u0003\u0010"+
		"\b\u0000\u00c8\u00c9\u00054\u0000\u0000\u00c9\u00ca\u0003V+\u0000\u00ca"+
		"\u0132\u0001\u0000\u0000\u0000\u00cb\u00cc\u0005\u0006\u0000\u0000\u00cc"+
		"\u00cd\u00054\u0000\u0000\u00cd\u0132\u0003V+\u0000\u00ce\u00cf\u0003"+
		"\u0016\u000b\u0000\u00cf\u00d0\u00056\u0000\u0000\u00d0\u00d1\u00054\u0000"+
		"\u0000\u00d1\u00d2\u0003V+\u0000\u00d2\u0132\u0001\u0000\u0000\u0000\u00d3"+
		"\u00d4\u0003L&\u0000\u00d4\u00d5\u00056\u0000\u0000\u00d5\u00d6\u0005"+
		"4\u0000\u0000\u00d6\u00d7\u0003V+\u0000\u00d7\u0132\u0001\u0000\u0000"+
		"\u0000\u00d8\u00d9\u0003P(\u0000\u00d9\u00da\u00056\u0000\u0000\u00da"+
		"\u00db\u00054\u0000\u0000\u00db\u00dc\u0003V+\u0000\u00dc\u0132\u0001"+
		"\u0000\u0000\u0000\u00dd\u00de\u0003R)\u0000\u00de\u00df\u00056\u0000"+
		"\u0000\u00df\u00e0\u00054\u0000\u0000\u00e0\u00e1\u0003V+\u0000\u00e1"+
		"\u0132\u0001\u0000\u0000\u0000\u00e2\u00e3\u0003T*\u0000\u00e3\u00e4\u0005"+
		"6\u0000\u0000\u00e4\u00e5\u00054\u0000\u0000\u00e5\u00e6\u0003V+\u0000"+
		"\u00e6\u0132\u0001\u0000\u0000\u0000\u00e7\u00e8\u0003\u0010\b\u0000\u00e8"+
		"\u00e9\u00056\u0000\u0000\u00e9\u00ea\u00054\u0000\u0000\u00ea\u00eb\u0003"+
		"V+\u0000\u00eb\u0132\u0001\u0000\u0000\u0000\u00ec\u00ed\u0005\u0006\u0000"+
		"\u0000\u00ed\u00ee\u00056\u0000\u0000\u00ee\u00ef\u00054\u0000\u0000\u00ef"+
		"\u0132\u0003V+\u0000\u00f0\u00f1\u0003\u0016\u000b\u0000\u00f1\u00f2\u0005"+
		"7\u0000\u0000\u00f2\u00f3\u0003\u0016\u000b\u0000\u00f3\u00f4\u0005\u0007"+
		"\u0000\u0000\u00f4\u00f5\u0003\u0016\u000b\u0000\u00f5\u0132\u0001\u0000"+
		"\u0000\u0000\u00f6\u00f7\u0003L&\u0000\u00f7\u00f8\u00057\u0000\u0000"+
		"\u00f8\u00f9\u0003L&\u0000\u00f9\u00fa\u0005\u0007\u0000\u0000\u00fa\u00fb"+
		"\u0003L&\u0000\u00fb\u0132\u0001\u0000\u0000\u0000\u00fc\u00fd\u0003P"+
		"(\u0000\u00fd\u00fe\u00057\u0000\u0000\u00fe\u00ff\u0003P(\u0000\u00ff"+
		"\u0100\u0005\u0007\u0000\u0000\u0100\u0101\u0003P(\u0000\u0101\u0132\u0001"+
		"\u0000\u0000\u0000\u0102\u0103\u0003R)\u0000\u0103\u0104\u00057\u0000"+
		"\u0000\u0104\u0105\u0003R)\u0000\u0105\u0106\u0005\u0007\u0000\u0000\u0106"+
		"\u0107\u0003R)\u0000\u0107\u0132\u0001\u0000\u0000\u0000\u0108\u0109\u0003"+
		"T*\u0000\u0109\u010a\u00057\u0000\u0000\u010a\u010b\u0003T*\u0000\u010b"+
		"\u010c\u0005\u0007\u0000\u0000\u010c\u010d\u0003T*\u0000\u010d\u0132\u0001"+
		"\u0000\u0000\u0000\u010e\u010f\u0003\u0016\u000b\u0000\u010f\u0110\u0005"+
		"6\u0000\u0000\u0110\u0111\u00057\u0000\u0000\u0111\u0112\u0003\u0016\u000b"+
		"\u0000\u0112\u0113\u0005\u0007\u0000\u0000\u0113\u0114\u0003\u0016\u000b"+
		"\u0000\u0114\u0132\u0001\u0000\u0000\u0000\u0115\u0116\u0003L&\u0000\u0116"+
		"\u0117\u00056\u0000\u0000\u0117\u0118\u00057\u0000\u0000\u0118\u0119\u0003"+
		"L&\u0000\u0119\u011a\u0005\u0007\u0000\u0000\u011a\u011b\u0003L&\u0000"+
		"\u011b\u0132\u0001\u0000\u0000\u0000\u011c\u011d\u0003P(\u0000\u011d\u011e"+
		"\u00056\u0000\u0000\u011e\u011f\u00057\u0000\u0000\u011f\u0120\u0003P"+
		"(\u0000\u0120\u0121\u0005\u0007\u0000\u0000\u0121\u0122\u0003P(\u0000"+
		"\u0122\u0132\u0001\u0000\u0000\u0000\u0123\u0124\u0003R)\u0000\u0124\u0125"+
		"\u00056\u0000\u0000\u0125\u0126\u00057\u0000\u0000\u0126\u0127\u0003R"+
		")\u0000\u0127\u0128\u0005\u0007\u0000\u0000\u0128\u0129\u0003R)\u0000"+
		"\u0129\u0132\u0001\u0000\u0000\u0000\u012a\u012b\u0003T*\u0000\u012b\u012c"+
		"\u00056\u0000\u0000\u012c\u012d\u00057\u0000\u0000\u012d\u012e\u0003T"+
		"*\u0000\u012e\u012f\u0005\u0007\u0000\u0000\u012f\u0130\u0003T*\u0000"+
		"\u0130\u0132\u0001\u0000\u0000\u0000\u0131\u0091\u0001\u0000\u0000\u0000"+
		"\u0131\u0097\u0001\u0000\u0000\u0000\u0131\u009b\u0001\u0000\u0000\u0000"+
		"\u0131\u009f\u0001\u0000\u0000\u0000\u0131\u00a3\u0001\u0000\u0000\u0000"+
		"\u0131\u00a7\u0001\u0000\u0000\u0000\u0131\u00ab\u0001\u0000\u0000\u0000"+
		"\u0131\u00af\u0001\u0000\u0000\u0000\u0131\u00b3\u0001\u0000\u0000\u0000"+
		"\u0131\u00b7\u0001\u0000\u0000\u0000\u0131\u00bb\u0001\u0000\u0000\u0000"+
		"\u0131\u00bf\u0001\u0000\u0000\u0000\u0131\u00c3\u0001\u0000\u0000\u0000"+
		"\u0131\u00c7\u0001\u0000\u0000\u0000\u0131\u00cb\u0001\u0000\u0000\u0000"+
		"\u0131\u00ce\u0001\u0000\u0000\u0000\u0131\u00d3\u0001\u0000\u0000\u0000"+
		"\u0131\u00d8\u0001\u0000\u0000\u0000\u0131\u00dd\u0001\u0000\u0000\u0000"+
		"\u0131\u00e2\u0001\u0000\u0000\u0000\u0131\u00e7\u0001\u0000\u0000\u0000"+
		"\u0131\u00ec\u0001\u0000\u0000\u0000\u0131\u00f0\u0001\u0000\u0000\u0000"+
		"\u0131\u00f6\u0001\u0000\u0000\u0000\u0131\u00fc\u0001\u0000\u0000\u0000"+
		"\u0131\u0102\u0001\u0000\u0000\u0000\u0131\u0108\u0001\u0000\u0000\u0000"+
		"\u0131\u010e\u0001\u0000\u0000\u0000\u0131\u0115\u0001\u0000\u0000\u0000"+
		"\u0131\u011c\u0001\u0000\u0000\u0000\u0131\u0123\u0001\u0000\u0000\u0000"+
		"\u0131\u012a\u0001\u0000\u0000\u0000\u0132\u000f\u0001\u0000\u0000\u0000"+
		"\u0133\u0138\u0003\u0012\t\u0000\u0134\u0135\u0007\u0000\u0000\u0000\u0135"+
		"\u0137\u0003\u0012\t\u0000\u0136\u0134\u0001\u0000\u0000\u0000\u0137\u013a"+
		"\u0001\u0000\u0000\u0000\u0138\u0136\u0001\u0000\u0000\u0000\u0138\u0139"+
		"\u0001\u0000\u0000\u0000\u0139\u0011\u0001\u0000\u0000\u0000\u013a\u0138"+
		"\u0001\u0000\u0000\u0000\u013b\u013c\u0007\u0001\u0000\u0000\u013c\u013f"+
		"\u0003\u0012\t\u0000\u013d\u013f\u0003\u0014\n\u0000\u013e\u013b\u0001"+
		"\u0000\u0000\u0000\u013e\u013d\u0001\u0000\u0000\u0000\u013f\u0013\u0001"+
		"\u0000\u0000\u0000\u0140\u0141\u0005\'\u0000\u0000\u0141\u0142\u0003\b"+
		"\u0004\u0000\u0142\u0143\u0005(\u0000\u0000\u0143\u0146\u0001\u0000\u0000"+
		"\u0000\u0144\u0146\u0003H$\u0000\u0145\u0140\u0001\u0000\u0000\u0000\u0145"+
		"\u0144\u0001\u0000\u0000\u0000\u0146\u0015\u0001\u0000\u0000\u0000\u0147"+
		"\u0148\u0003\u0018\f\u0000\u0148\u0017\u0001\u0000\u0000\u0000\u0149\u014e"+
		"\u0003\u001a\r\u0000\u014a\u014b\u0007\u0002\u0000\u0000\u014b\u014d\u0003"+
		"\u001a\r\u0000\u014c\u014a\u0001\u0000\u0000\u0000\u014d\u0150\u0001\u0000"+
		"\u0000\u0000\u014e\u014c\u0001\u0000\u0000\u0000\u014e\u014f\u0001\u0000"+
		"\u0000\u0000\u014f\u0019\u0001\u0000\u0000\u0000\u0150\u014e\u0001\u0000"+
		"\u0000\u0000\u0151\u0156\u0003\u001c\u000e\u0000\u0152\u0153\u0007\u0003"+
		"\u0000\u0000\u0153\u0155\u0003\u001c\u000e\u0000\u0154\u0152\u0001\u0000"+
		"\u0000\u0000\u0155\u0158\u0001\u0000\u0000\u0000\u0156\u0154\u0001\u0000"+
		"\u0000\u0000\u0156\u0157\u0001\u0000\u0000\u0000\u0157\u001b\u0001\u0000"+
		"\u0000\u0000\u0158\u0156\u0001\u0000\u0000\u0000\u0159\u015a\u0005\u0012"+
		"\u0000\u0000\u015a\u015d\u0003\u001c\u000e\u0000\u015b\u015d\u0003\u001e"+
		"\u000f\u0000\u015c\u0159\u0001\u0000\u0000\u0000\u015c\u015b\u0001\u0000"+
		"\u0000\u0000\u015d\u001d\u0001\u0000\u0000\u0000\u015e\u0163\u0003 \u0010"+
		"\u0000\u015f\u0160\u0005\u0019\u0000\u0000\u0160\u0162\u0003 \u0010\u0000"+
		"\u0161\u015f\u0001\u0000\u0000\u0000\u0162\u0165\u0001\u0000\u0000\u0000"+
		"\u0163\u0161\u0001\u0000\u0000\u0000\u0163\u0164\u0001\u0000\u0000\u0000"+
		"\u0164\u001f\u0001\u0000\u0000\u0000\u0165\u0163\u0001\u0000\u0000\u0000"+
		"\u0166\u0169\u0003\"\u0011\u0000\u0167\u0168\u0005\u0018\u0000\u0000\u0168"+
		"\u016a\u0003\u001c\u000e\u0000\u0169\u0167\u0001\u0000\u0000\u0000\u0169"+
		"\u016a\u0001\u0000\u0000\u0000\u016a!\u0001\u0000\u0000\u0000\u016b\u016f"+
		"\u0003$\u0012\u0000\u016c\u016e\u0007\u0004\u0000\u0000\u016d\u016c\u0001"+
		"\u0000\u0000\u0000\u016e\u0171\u0001\u0000\u0000\u0000\u016f\u016d\u0001"+
		"\u0000\u0000\u0000\u016f\u0170\u0001\u0000\u0000\u0000\u0170#\u0001\u0000"+
		"\u0000\u0000\u0171\u016f\u0001\u0000\u0000\u0000\u0172\u0173\u0005\'\u0000"+
		"\u0000\u0173\u0174\u0003\u0016\u000b\u0000\u0174\u0175\u0005(\u0000\u0000"+
		"\u0175\u0181\u0001\u0000\u0000\u0000\u0176\u0177\u0005\u001a\u0000\u0000"+
		"\u0177\u0178\u0005\'\u0000\u0000\u0178\u0179\u0003\u0016\u000b\u0000\u0179"+
		"\u017a\u0005(\u0000\u0000\u017a\u0181\u0001\u0000\u0000\u0000\u017b\u017c"+
		"\u0005\u0015\u0000\u0000\u017c\u017d\u0003\u0016\u000b\u0000\u017d\u017e"+
		"\u0005\u0015\u0000\u0000\u017e\u0181\u0001\u0000\u0000\u0000\u017f\u0181"+
		"\u0003J%\u0000\u0180\u0172\u0001\u0000\u0000\u0000\u0180\u0176\u0001\u0000"+
		"\u0000\u0000\u0180\u017b\u0001\u0000\u0000\u0000\u0180\u017f\u0001\u0000"+
		"\u0000\u0000\u0181%\u0001\u0000\u0000\u0000\u0182\u0183\u00058\u0000\u0000"+
		"\u0183\u018c\u0005\'\u0000\u0000\u0184\u0189\u0003>\u001f\u0000\u0185"+
		"\u0186\u0007\u0005\u0000\u0000\u0186\u0188\u0003>\u001f\u0000\u0187\u0185"+
		"\u0001\u0000\u0000\u0000\u0188\u018b\u0001\u0000\u0000\u0000\u0189\u0187"+
		"\u0001\u0000\u0000\u0000\u0189\u018a\u0001\u0000\u0000\u0000\u018a\u018d"+
		"\u0001\u0000\u0000\u0000\u018b\u0189\u0001\u0000\u0000\u0000\u018c\u0184"+
		"\u0001\u0000\u0000\u0000\u018c\u018d\u0001\u0000\u0000\u0000\u018d\u018e"+
		"\u0001\u0000\u0000\u0000\u018e\u018f\u0005(\u0000\u0000\u018f\'\u0001"+
		"\u0000\u0000\u0000\u0190\u01a0\u0003&\u0013\u0000\u0191\u0195\u00058\u0000"+
		"\u0000\u0192\u0194\u0003.\u0017\u0000\u0193\u0192\u0001\u0000\u0000\u0000"+
		"\u0194\u0197\u0001\u0000\u0000\u0000\u0195\u0193\u0001\u0000\u0000\u0000"+
		"\u0195\u0196\u0001\u0000\u0000\u0000\u0196\u01a0\u0001\u0000\u0000\u0000"+
		"\u0197\u0195\u0001\u0000\u0000\u0000\u0198\u019c\u00052\u0000\u0000\u0199"+
		"\u019b\u0003.\u0017\u0000\u019a\u0199\u0001\u0000\u0000\u0000\u019b\u019e"+
		"\u0001\u0000\u0000\u0000\u019c\u019a\u0001\u0000\u0000\u0000\u019c\u019d"+
		"\u0001\u0000\u0000\u0000\u019d\u01a0\u0001\u0000\u0000\u0000\u019e\u019c"+
		"\u0001\u0000\u0000\u0000\u019f\u0190\u0001\u0000\u0000\u0000\u019f\u0191"+
		"\u0001\u0000\u0000\u0000\u019f\u0198\u0001\u0000\u0000\u0000\u01a0)\u0001"+
		"\u0000\u0000\u0000\u01a1\u01ab\u0003,\u0016\u0000\u01a2\u01a7\u0003>\u001f"+
		"\u0000\u01a3\u01a4\u0005+\u0000\u0000\u01a4\u01a6\u0003>\u001f\u0000\u01a5"+
		"\u01a3\u0001\u0000\u0000\u0000\u01a6\u01a9\u0001\u0000\u0000\u0000\u01a7"+
		"\u01a5\u0001\u0000\u0000\u0000\u01a7\u01a8\u0001\u0000\u0000\u0000\u01a8"+
		"\u01ab\u0001\u0000\u0000\u0000\u01a9\u01a7\u0001\u0000\u0000\u0000\u01aa"+
		"\u01a1\u0001\u0000\u0000\u0000\u01aa\u01a2\u0001\u0000\u0000\u0000\u01ab"+
		"+\u0001\u0000\u0000\u0000\u01ac\u01ad\u00052\u0000\u0000\u01ad\u01ae\u0005"+
		"3\u0000\u0000\u01ae\u01af\u0003>\u001f\u0000\u01af-\u0001\u0000\u0000"+
		"\u0000\u01b0\u01b1\u0005-\u0000\u0000\u01b1\u01b2\u00058\u0000\u0000\u01b2"+
		"\u01b4\u0005\'\u0000\u0000\u01b3\u01b5\u0003*\u0015\u0000\u01b4\u01b3"+
		"\u0001\u0000\u0000\u0000\u01b4\u01b5\u0001\u0000\u0000\u0000\u01b5\u01b6"+
		"\u0001\u0000\u0000\u0000\u01b6\u01db\u0005(\u0000\u0000\u01b7\u01b8\u0005"+
		".\u0000\u0000\u01b8\u01db\u0005\u000f\u0000\u0000\u01b9\u01ba\u0005.\u0000"+
		"\u0000\u01ba\u01bb\u00058\u0000\u0000\u01bb\u01c4\u0005\'\u0000\u0000"+
		"\u01bc\u01c1\u0003>\u001f\u0000\u01bd\u01be\u0005+\u0000\u0000\u01be\u01c0"+
		"\u0003>\u001f\u0000\u01bf\u01bd\u0001\u0000\u0000\u0000\u01c0\u01c3\u0001"+
		"\u0000\u0000\u0000\u01c1\u01bf\u0001\u0000\u0000\u0000\u01c1\u01c2\u0001"+
		"\u0000\u0000\u0000\u01c2\u01c5\u0001\u0000\u0000\u0000\u01c3\u01c1\u0001"+
		"\u0000\u0000\u0000\u01c4\u01bc\u0001\u0000\u0000\u0000\u01c4\u01c5\u0001"+
		"\u0000\u0000\u0000\u01c5\u01c6\u0001\u0000\u0000\u0000\u01c6\u01db\u0005"+
		"(\u0000\u0000\u01c7\u01c8\u00050\u0000\u0000\u01c8\u01c9\u00058\u0000"+
		"\u0000\u01c9\u01d2\u0005\'\u0000\u0000\u01ca\u01cf\u0003>\u001f\u0000"+
		"\u01cb\u01cc\u0005+\u0000\u0000\u01cc\u01ce\u0003>\u001f\u0000\u01cd\u01cb"+
		"\u0001\u0000\u0000\u0000\u01ce\u01d1\u0001\u0000\u0000\u0000\u01cf\u01cd"+
		"\u0001\u0000\u0000\u0000\u01cf\u01d0\u0001\u0000\u0000\u0000\u01d0\u01d3"+
		"\u0001\u0000\u0000\u0000\u01d1\u01cf\u0001\u0000\u0000\u0000\u01d2\u01ca"+
		"\u0001\u0000\u0000\u0000\u01d2\u01d3\u0001\u0000\u0000\u0000\u01d3\u01d4"+
		"\u0001\u0000\u0000\u0000\u01d4\u01db\u0005(\u0000\u0000\u01d5\u01d6\u0005"+
		".\u0000\u0000\u01d6\u01db\u00058\u0000\u0000\u01d7\u01d8\u00050\u0000"+
		"\u0000\u01d8\u01db\u00058\u0000\u0000\u01d9\u01db\u00030\u0018\u0000\u01da"+
		"\u01b0\u0001\u0000\u0000\u0000\u01da\u01b7\u0001\u0000\u0000\u0000\u01da"+
		"\u01b9\u0001\u0000\u0000\u0000\u01da\u01c7\u0001\u0000\u0000\u0000\u01da"+
		"\u01d5\u0001\u0000\u0000\u0000\u01da\u01d7\u0001\u0000\u0000\u0000\u01da"+
		"\u01d9\u0001\u0000\u0000\u0000\u01db/\u0001\u0000\u0000\u0000\u01dc\u01dd"+
		"\u0005)\u0000\u0000\u01dd\u01de\u0005\u000f\u0000\u0000\u01de\u01fe\u0005"+
		"*\u0000\u0000\u01df\u01e0\u0005)\u0000\u0000\u01e0\u01e1\u00059\u0000"+
		"\u0000\u01e1\u01fe\u0005*\u0000\u0000\u01e2\u01e3\u0005)\u0000\u0000\u01e3"+
		"\u01e4\u00032\u0019\u0000\u01e4\u01e6\u0005@\u0000\u0000\u01e5\u01e7\u0003"+
		"2\u0019\u0000\u01e6\u01e5\u0001\u0000\u0000\u0000\u01e6\u01e7\u0001\u0000"+
		"\u0000\u0000\u01e7\u01e8\u0001\u0000\u0000\u0000\u01e8\u01e9\u0005*\u0000"+
		"\u0000\u01e9\u01fe\u0001\u0000\u0000\u0000\u01ea\u01eb\u0005)\u0000\u0000"+
		"\u01eb\u01ec\u0005@\u0000\u0000\u01ec\u01ed\u00032\u0019\u0000\u01ed\u01ee"+
		"\u0005*\u0000\u0000\u01ee\u01fe\u0001\u0000\u0000\u0000\u01ef\u01f0\u0005"+
		")\u0000\u0000\u01f0\u01f1\u0005=\u0000\u0000\u01f1\u01fe\u0005*\u0000"+
		"\u0000\u01f2\u01f3\u0005)\u0000\u0000\u01f3\u01f4\u00032\u0019\u0000\u01f4"+
		"\u01f5\u0005*\u0000\u0000\u01f5\u01fe\u0001\u0000\u0000\u0000\u01f6\u01f7"+
		"\u0005)\u0000\u0000\u01f7\u01f8\u00051\u0000\u0000\u01f8\u01f9\u0005\'"+
		"\u0000\u0000\u01f9\u01fa\u00034\u001a\u0000\u01fa\u01fb\u0005(\u0000\u0000"+
		"\u01fb\u01fc\u0005*\u0000\u0000\u01fc\u01fe\u0001\u0000\u0000\u0000\u01fd"+
		"\u01dc\u0001\u0000\u0000\u0000\u01fd\u01df\u0001\u0000\u0000\u0000\u01fd"+
		"\u01e2\u0001\u0000\u0000\u0000\u01fd\u01ea\u0001\u0000\u0000\u0000\u01fd"+
		"\u01ef\u0001\u0000\u0000\u0000\u01fd\u01f2\u0001\u0000\u0000\u0000\u01fd"+
		"\u01f6\u0001\u0000\u0000\u0000\u01fe1\u0001\u0000\u0000\u0000\u01ff\u0201"+
		"\u0005\u0012\u0000\u0000\u0200\u01ff\u0001\u0000\u0000\u0000\u0200\u0201"+
		"\u0001\u0000\u0000\u0000\u0201\u0202\u0001\u0000\u0000\u0000\u0202\u0203"+
		"\u0005:\u0000\u0000\u02033\u0001\u0000\u0000\u0000\u0204\u0209\u00036"+
		"\u001b\u0000\u0205\u0206\u0007\u0006\u0000\u0000\u0206\u0208\u00036\u001b"+
		"\u0000\u0207\u0205\u0001\u0000\u0000\u0000\u0208\u020b\u0001\u0000\u0000"+
		"\u0000\u0209\u0207\u0001\u0000\u0000\u0000\u0209\u020a\u0001\u0000\u0000"+
		"\u0000\u020a5\u0001\u0000\u0000\u0000\u020b\u0209\u0001\u0000\u0000\u0000"+
		"\u020c\u020d\u0005\'\u0000\u0000\u020d\u020e\u00034\u001a\u0000\u020e"+
		"\u020f\u0005(\u0000\u0000\u020f\u0212\u0001\u0000\u0000\u0000\u0210\u0212"+
		"\u00038\u001c\u0000\u0211\u020c\u0001\u0000\u0000\u0000\u0211\u0210\u0001"+
		"\u0000\u0000\u0000\u02127\u0001\u0000\u0000\u0000\u0213\u0214\u0003:\u001d"+
		"\u0000\u0214\u0215\u0003<\u001e\u0000\u0215\u0216\u0003:\u001d\u0000\u0216"+
		"\u0229\u0001\u0000\u0000\u0000\u0217\u0218\u0003:\u001d\u0000\u0218\u0219"+
		"\u0005\"\u0000\u0000\u0219\u021a\u00059\u0000\u0000\u021a\u0229\u0001"+
		"\u0000\u0000\u0000\u021b\u021c\u0003:\u001d\u0000\u021c\u021d\u0005#\u0000"+
		"\u0000\u021d\u021e\u00059\u0000\u0000\u021e\u0229\u0001\u0000\u0000\u0000"+
		"\u021f\u0220\u0003:\u001d\u0000\u0220\u0221\u00054\u0000\u0000\u0221\u0222"+
		"\u0003:\u001d\u0000\u0222\u0229\u0001\u0000\u0000\u0000\u0223\u0224\u0003"+
		":\u001d\u0000\u0224\u0225\u00055\u0000\u0000\u0225\u0226\u0003:\u001d"+
		"\u0000\u0226\u0229\u0001\u0000\u0000\u0000\u0227\u0229\u0003:\u001d\u0000"+
		"\u0228\u0213\u0001\u0000\u0000\u0000\u0228\u0217\u0001\u0000\u0000\u0000"+
		"\u0228\u021b\u0001\u0000\u0000\u0000\u0228\u021f\u0001\u0000\u0000\u0000"+
		"\u0228\u0223\u0001\u0000\u0000\u0000\u0228\u0227\u0001\u0000\u0000\u0000"+
		"\u02299\u0001\u0000\u0000\u0000\u022a\u022e\u00052\u0000\u0000\u022b\u022d"+
		"\u0003.\u0017\u0000\u022c\u022b\u0001\u0000\u0000\u0000\u022d\u0230\u0001"+
		"\u0000\u0000\u0000\u022e\u022c\u0001\u0000\u0000\u0000\u022e\u022f\u0001"+
		"\u0000\u0000\u0000\u022f\u023d\u0001\u0000\u0000\u0000\u0230\u022e\u0001"+
		"\u0000\u0000\u0000\u0231\u0236\u00058\u0000\u0000\u0232\u0233\u0005.\u0000"+
		"\u0000\u0233\u0235\u00058\u0000\u0000\u0234\u0232\u0001\u0000\u0000\u0000"+
		"\u0235\u0238\u0001\u0000\u0000\u0000\u0236\u0234\u0001\u0000\u0000\u0000"+
		"\u0236\u0237\u0001\u0000\u0000\u0000\u0237\u023d\u0001\u0000\u0000\u0000"+
		"\u0238\u0236\u0001\u0000\u0000\u0000\u0239\u023d\u0003J%\u0000\u023a\u023d"+
		"\u0003L&\u0000\u023b\u023d\u0005\u0006\u0000\u0000\u023c\u022a\u0001\u0000"+
		"\u0000\u0000\u023c\u0231\u0001\u0000\u0000\u0000\u023c\u0239\u0001\u0000"+
		"\u0000\u0000\u023c\u023a\u0001\u0000\u0000\u0000\u023c\u023b\u0001\u0000"+
		"\u0000\u0000\u023d;\u0001\u0000\u0000\u0000\u023e\u0245\u0005\u001b\u0000"+
		"\u0000\u023f\u0245\u0005\u001c\u0000\u0000\u0240\u0245\u0005\u001d\u0000"+
		"\u0000\u0241\u0245\u0005\u001e\u0000\u0000\u0242\u0245\u0005\u001f\u0000"+
		"\u0000\u0243\u0245\u0005 \u0000\u0000\u0244\u023e\u0001\u0000\u0000\u0000"+
		"\u0244\u023f\u0001\u0000\u0000\u0000\u0244\u0240\u0001\u0000\u0000\u0000"+
		"\u0244\u0241\u0001\u0000\u0000\u0000\u0244\u0242\u0001\u0000\u0000\u0000"+
		"\u0244\u0243\u0001\u0000\u0000\u0000\u0245=\u0001\u0000\u0000\u0000\u0246"+
		"\u024f\u0003\u0016\u000b\u0000\u0247\u024f\u0003\b\u0004\u0000\u0248\u024f"+
		"\u0003P(\u0000\u0249\u024f\u0003R)\u0000\u024a\u024f\u0003T*\u0000\u024b"+
		"\u024f\u0003L&\u0000\u024c\u024f\u0003V+\u0000\u024d\u024f\u0005\u0006"+
		"\u0000\u0000\u024e\u0246\u0001\u0000\u0000\u0000\u024e\u0247\u0001\u0000"+
		"\u0000\u0000\u024e\u0248\u0001\u0000\u0000\u0000\u024e\u0249\u0001\u0000"+
		"\u0000\u0000\u024e\u024a\u0001\u0000\u0000\u0000\u024e\u024b\u0001\u0000"+
		"\u0000\u0000\u024e\u024c\u0001\u0000\u0000\u0000\u024e\u024d\u0001\u0000"+
		"\u0000\u0000\u024f?\u0001\u0000\u0000\u0000\u0250\u0259\u0003B!\u0000"+
		"\u0251\u0259\u0003\u0016\u000b\u0000\u0252\u0259\u0003\b\u0004\u0000\u0253"+
		"\u0259\u0003P(\u0000\u0254\u0259\u0003R)\u0000\u0255\u0259\u0003T*\u0000"+
		"\u0256\u0259\u0003L&\u0000\u0257\u0259\u0003V+\u0000\u0258\u0250\u0001"+
		"\u0000\u0000\u0000\u0258\u0251\u0001\u0000\u0000\u0000\u0258\u0252\u0001"+
		"\u0000\u0000\u0000\u0258\u0253\u0001\u0000\u0000\u0000\u0258\u0254\u0001"+
		"\u0000\u0000\u0000\u0258\u0255\u0001\u0000\u0000\u0000\u0258\u0256\u0001"+
		"\u0000\u0000\u0000\u0258\u0257\u0001\u0000\u0000\u0000\u0259A\u0001\u0000"+
		"\u0000\u0000\u025a\u025b\u0005\u0001\u0000\u0000\u025b\u025c\u0003\b\u0004"+
		"\u0000\u025c\u025d\u0005\u0002\u0000\u0000\u025d\u0265\u0003B!\u0000\u025e"+
		"\u025f\u0005\u0004\u0000\u0000\u025f\u0260\u0003\b\u0004\u0000\u0260\u0261"+
		"\u0005\u0002\u0000\u0000\u0261\u0262\u0003B!\u0000\u0262\u0264\u0001\u0000"+
		"\u0000\u0000\u0263\u025e\u0001\u0000\u0000\u0000\u0264\u0267\u0001\u0000"+
		"\u0000\u0000\u0265\u0263\u0001\u0000\u0000\u0000\u0265\u0266\u0001\u0000"+
		"\u0000\u0000\u0266\u0268\u0001\u0000\u0000\u0000\u0267\u0265\u0001\u0000"+
		"\u0000\u0000\u0268\u0269\u0005\u0003\u0000\u0000\u0269\u026a\u0003B!\u0000"+
		"\u026a\u026b\u0005\u0005\u0000\u0000\u026b\u0282\u0001\u0000\u0000\u0000"+
		"\u026c\u026d\u0005\u0001\u0000\u0000\u026d\u026e\u0005\'\u0000\u0000\u026e"+
		"\u026f\u0003\b\u0004\u0000\u026f\u0270\u0007\u0005\u0000\u0000\u0270\u0278"+
		"\u0003B!\u0000\u0271\u0272\u0007\u0005\u0000\u0000\u0272\u0273\u0003\b"+
		"\u0004\u0000\u0273\u0274\u0007\u0005\u0000\u0000\u0274\u0275\u0003B!\u0000"+
		"\u0275\u0277\u0001\u0000\u0000\u0000\u0276\u0271\u0001\u0000\u0000\u0000"+
		"\u0277\u027a\u0001\u0000\u0000\u0000\u0278\u0276\u0001\u0000\u0000\u0000"+
		"\u0278\u0279\u0001\u0000\u0000\u0000\u0279\u027b\u0001\u0000\u0000\u0000"+
		"\u027a\u0278\u0001\u0000\u0000\u0000\u027b\u027c\u0007\u0005\u0000\u0000"+
		"\u027c\u027d\u0003B!\u0000\u027d\u027e\u0005(\u0000\u0000\u027e\u0282"+
		"\u0001\u0000\u0000\u0000\u027f\u0282\u0003D\"\u0000\u0280\u0282\u0003"+
		"(\u0014\u0000\u0281\u025a\u0001\u0000\u0000\u0000\u0281\u026c\u0001\u0000"+
		"\u0000\u0000\u0281\u027f\u0001\u0000\u0000\u0000\u0281\u0280\u0001\u0000"+
		"\u0000\u0000\u0282C\u0001\u0000\u0000\u0000\u0283\u0284\u0003F#\u0000"+
		"\u0284\u0285\u0005\'\u0000\u0000\u0285\u0286\u0003B!\u0000\u0286\u0287"+
		"\u0005(\u0000\u0000\u0287E\u0001\u0000\u0000\u0000\u0288\u0290\u0005A"+
		"\u0000\u0000\u0289\u0290\u0005B\u0000\u0000\u028a\u0290\u0005C\u0000\u0000"+
		"\u028b\u0290\u0005D\u0000\u0000\u028c\u0290\u0005E\u0000\u0000\u028d\u0290"+
		"\u0005F\u0000\u0000\u028e\u0290\u0005G\u0000\u0000\u028f\u0288\u0001\u0000"+
		"\u0000\u0000\u028f\u0289\u0001\u0000\u0000\u0000\u028f\u028a\u0001\u0000"+
		"\u0000\u0000\u028f\u028b\u0001\u0000\u0000\u0000\u028f\u028c\u0001\u0000"+
		"\u0000\u0000\u028f\u028d\u0001\u0000\u0000\u0000\u028f\u028e\u0001\u0000"+
		"\u0000\u0000\u0290G\u0001\u0000\u0000\u0000\u0291\u02c0\u0007\u0007\u0000"+
		"\u0000\u0292\u0293\u0005\u0001\u0000\u0000\u0293\u0294\u0003\b\u0004\u0000"+
		"\u0294\u0295\u0005\u0002\u0000\u0000\u0295\u029d\u0003\b\u0004\u0000\u0296"+
		"\u0297\u0005\u0004\u0000\u0000\u0297\u0298\u0003\b\u0004\u0000\u0298\u0299"+
		"\u0005\u0002\u0000\u0000\u0299\u029a\u0003\b\u0004\u0000\u029a\u029c\u0001"+
		"\u0000\u0000\u0000\u029b\u0296\u0001\u0000\u0000\u0000\u029c\u029f\u0001"+
		"\u0000\u0000\u0000\u029d\u029b\u0001\u0000\u0000\u0000\u029d\u029e\u0001"+
		"\u0000\u0000\u0000\u029e\u02a0\u0001\u0000\u0000\u0000\u029f\u029d\u0001"+
		"\u0000\u0000\u0000\u02a0\u02a1\u0005\u0003\u0000\u0000\u02a1\u02a2\u0003"+
		"\b\u0004\u0000\u02a2\u02a3\u0005\u0005\u0000\u0000\u02a3\u02c0\u0001\u0000"+
		"\u0000\u0000\u02a4\u02a5\u0005\u0001\u0000\u0000\u02a5\u02a6\u0005\'\u0000"+
		"\u0000\u02a6\u02a7\u0003\b\u0004\u0000\u02a7\u02a8\u0007\u0005\u0000\u0000"+
		"\u02a8\u02b0\u0003\b\u0004\u0000\u02a9\u02aa\u0007\u0005\u0000\u0000\u02aa"+
		"\u02ab\u0003\b\u0004\u0000\u02ab\u02ac\u0007\u0005\u0000\u0000\u02ac\u02ad"+
		"\u0003\b\u0004\u0000\u02ad\u02af\u0001\u0000\u0000\u0000\u02ae\u02a9\u0001"+
		"\u0000\u0000\u0000\u02af\u02b2\u0001\u0000\u0000\u0000\u02b0\u02ae\u0001"+
		"\u0000\u0000\u0000\u02b0\u02b1\u0001\u0000\u0000\u0000\u02b1\u02b3\u0001"+
		"\u0000\u0000\u0000\u02b2\u02b0\u0001\u0000\u0000\u0000\u02b3\u02b4\u0007"+
		"\u0005\u0000\u0000\u02b4\u02b5\u0003\b\u0004\u0000\u02b5\u02b6\u0005("+
		"\u0000\u0000\u02b6\u02c0\u0001\u0000\u0000\u0000\u02b7\u02b9\u0005A\u0000"+
		"\u0000\u02b8\u02b7\u0001\u0000\u0000\u0000\u02b8\u02b9\u0001\u0000\u0000"+
		"\u0000\u02b9\u02ba\u0001\u0000\u0000\u0000\u02ba\u02bd\u0003(\u0014\u0000"+
		"\u02bb\u02bc\u0005/\u0000\u0000\u02bc\u02be\u0003\b\u0004\u0000\u02bd"+
		"\u02bb\u0001\u0000\u0000\u0000\u02bd\u02be\u0001\u0000\u0000\u0000\u02be"+
		"\u02c0\u0001\u0000\u0000\u0000\u02bf\u0291\u0001\u0000\u0000\u0000\u02bf"+
		"\u0292\u0001\u0000\u0000\u0000\u02bf\u02a4\u0001\u0000\u0000\u0000\u02bf"+
		"\u02b8\u0001\u0000\u0000\u0000\u02c0I\u0001\u0000\u0000\u0000\u02c1\u02c2"+
		"\u0005\u0001\u0000\u0000\u02c2\u02c3\u0003\b\u0004\u0000\u02c3\u02c4\u0005"+
		"\u0002\u0000\u0000\u02c4\u02cc\u0003\u0016\u000b\u0000\u02c5\u02c6\u0005"+
		"\u0004\u0000\u0000\u02c6\u02c7\u0003\b\u0004\u0000\u02c7\u02c8\u0005\u0002"+
		"\u0000\u0000\u02c8\u02c9\u0003\u0016\u000b\u0000\u02c9\u02cb\u0001\u0000"+
		"\u0000\u0000\u02ca\u02c5\u0001\u0000\u0000\u0000\u02cb\u02ce\u0001\u0000"+
		"\u0000\u0000\u02cc\u02ca\u0001\u0000\u0000\u0000\u02cc\u02cd\u0001\u0000"+
		"\u0000\u0000\u02cd\u02cf\u0001\u0000\u0000\u0000\u02ce\u02cc\u0001\u0000"+
		"\u0000\u0000\u02cf\u02d0\u0005\u0003\u0000\u0000\u02d0\u02d1\u0003\u0016"+
		"\u000b\u0000\u02d1\u02d2\u0005\u0005\u0000\u0000\u02d2\u02f0\u0001\u0000"+
		"\u0000\u0000\u02d3\u02d4\u0005\u0001\u0000\u0000\u02d4\u02d5\u0005\'\u0000"+
		"\u0000\u02d5\u02d6\u0003\b\u0004\u0000\u02d6\u02d7\u0007\u0005\u0000\u0000"+
		"\u02d7\u02df\u0003\u0016\u000b\u0000\u02d8\u02d9\u0007\u0005\u0000\u0000"+
		"\u02d9\u02da\u0003\b\u0004\u0000\u02da\u02db\u0007\u0005\u0000\u0000\u02db"+
		"\u02dc\u0003\u0016\u000b\u0000\u02dc\u02de\u0001\u0000\u0000\u0000\u02dd"+
		"\u02d8\u0001\u0000\u0000\u0000\u02de\u02e1\u0001\u0000\u0000\u0000\u02df"+
		"\u02dd\u0001\u0000\u0000\u0000\u02df\u02e0\u0001\u0000\u0000\u0000\u02e0"+
		"\u02e2\u0001\u0000\u0000\u0000\u02e1\u02df\u0001\u0000\u0000\u0000\u02e2"+
		"\u02e3\u0007\u0005\u0000\u0000\u02e3\u02e4\u0003\u0016\u000b\u0000\u02e4"+
		"\u02e5\u0005(\u0000\u0000\u02e5\u02f0\u0001\u0000\u0000\u0000\u02e6\u02f0"+
		"\u0005:\u0000\u0000\u02e7\u02e9\u0005B\u0000\u0000\u02e8\u02e7\u0001\u0000"+
		"\u0000\u0000\u02e8\u02e9\u0001\u0000\u0000\u0000\u02e9\u02ea\u0001\u0000"+
		"\u0000\u0000\u02ea\u02ed\u0003(\u0014\u0000\u02eb\u02ec\u0005/\u0000\u0000"+
		"\u02ec\u02ee\u0003\u0016\u000b\u0000\u02ed\u02eb\u0001\u0000\u0000\u0000"+
		"\u02ed\u02ee\u0001\u0000\u0000\u0000\u02ee\u02f0\u0001\u0000\u0000\u0000"+
		"\u02ef\u02c1\u0001\u0000\u0000\u0000\u02ef\u02d3\u0001\u0000\u0000\u0000"+
		"\u02ef\u02e6\u0001\u0000\u0000\u0000\u02ef\u02e8\u0001\u0000\u0000\u0000"+
		"\u02f0K\u0001\u0000\u0000\u0000\u02f1\u02f6\u0003N\'\u0000\u02f2\u02f3"+
		"\u0005\u0016\u0000\u0000\u02f3\u02f5\u0003N\'\u0000\u02f4\u02f2\u0001"+
		"\u0000\u0000\u0000\u02f5\u02f8\u0001\u0000\u0000\u0000\u02f6\u02f4\u0001"+
		"\u0000\u0000\u0000\u02f6\u02f7\u0001\u0000\u0000\u0000\u02f7M\u0001\u0000"+
		"\u0000\u0000\u02f8\u02f6\u0001\u0000\u0000\u0000\u02f9\u02fa\u0005\u0001"+
		"\u0000\u0000\u02fa\u02fb\u0003\b\u0004\u0000\u02fb\u02fc\u0005\u0002\u0000"+
		"\u0000\u02fc\u0304\u0003L&\u0000\u02fd\u02fe\u0005\u0004\u0000\u0000\u02fe"+
		"\u02ff\u0003\b\u0004\u0000\u02ff\u0300\u0005\u0002\u0000\u0000\u0300\u0301"+
		"\u0003L&\u0000\u0301\u0303\u0001\u0000\u0000\u0000\u0302\u02fd\u0001\u0000"+
		"\u0000\u0000\u0303\u0306\u0001\u0000\u0000\u0000\u0304\u0302\u0001\u0000"+
		"\u0000\u0000\u0304\u0305\u0001\u0000\u0000\u0000\u0305\u0307\u0001\u0000"+
		"\u0000\u0000\u0306\u0304\u0001\u0000\u0000\u0000\u0307\u0308\u0005\u0003"+
		"\u0000\u0000\u0308\u0309\u0003L&\u0000\u0309\u030a\u0005\u0005\u0000\u0000"+
		"\u030a\u0328\u0001\u0000\u0000\u0000\u030b\u030c\u0005\u0001\u0000\u0000"+
		"\u030c\u030d\u0005\'\u0000\u0000\u030d\u030e\u0003\b\u0004\u0000\u030e"+
		"\u030f\u0007\u0005\u0000\u0000\u030f\u0317\u0003L&\u0000\u0310\u0311\u0007"+
		"\u0005\u0000\u0000\u0311\u0312\u0003\b\u0004\u0000\u0312\u0313\u0007\u0005"+
		"\u0000\u0000\u0313\u0314\u0003L&\u0000\u0314\u0316\u0001\u0000\u0000\u0000"+
		"\u0315\u0310\u0001\u0000\u0000\u0000\u0316\u0319\u0001\u0000\u0000\u0000"+
		"\u0317\u0315\u0001\u0000\u0000\u0000\u0317\u0318\u0001\u0000\u0000\u0000"+
		"\u0318\u031a\u0001\u0000\u0000\u0000\u0319\u0317\u0001\u0000\u0000\u0000"+
		"\u031a\u031b\u0007\u0005\u0000\u0000\u031b\u031c\u0003L&\u0000\u031c\u031d"+
		"\u0005(\u0000\u0000\u031d\u0328\u0001\u0000\u0000\u0000\u031e\u0328\u0005"+
		"9\u0000\u0000\u031f\u0321\u0005C\u0000\u0000\u0320\u031f\u0001\u0000\u0000"+
		"\u0000\u0320\u0321\u0001\u0000\u0000\u0000\u0321\u0322\u0001\u0000\u0000"+
		"\u0000\u0322\u0325\u0003(\u0014\u0000\u0323\u0324\u0005/\u0000\u0000\u0324"+
		"\u0326\u0003L&\u0000\u0325\u0323\u0001\u0000\u0000\u0000\u0325\u0326\u0001"+
		"\u0000\u0000\u0000\u0326\u0328\u0001\u0000\u0000\u0000\u0327\u02f9\u0001"+
		"\u0000\u0000\u0000\u0327\u030b\u0001\u0000\u0000\u0000\u0327\u031e\u0001"+
		"\u0000\u0000\u0000\u0327\u0320\u0001\u0000\u0000\u0000\u0328O\u0001\u0000"+
		"\u0000\u0000\u0329\u032a\u0005\u0001\u0000\u0000\u032a\u032b\u0003\b\u0004"+
		"\u0000\u032b\u032c\u0005\u0002\u0000\u0000\u032c\u0334\u0003P(\u0000\u032d"+
		"\u032e\u0005\u0004\u0000\u0000\u032e\u032f\u0003\b\u0004\u0000\u032f\u0330"+
		"\u0005\u0002\u0000\u0000\u0330\u0331\u0003P(\u0000\u0331\u0333\u0001\u0000"+
		"\u0000\u0000\u0332\u032d\u0001\u0000\u0000\u0000\u0333\u0336\u0001\u0000"+
		"\u0000\u0000\u0334\u0332\u0001\u0000\u0000\u0000\u0334\u0335\u0001\u0000"+
		"\u0000\u0000\u0335\u0337\u0001\u0000\u0000\u0000\u0336\u0334\u0001\u0000"+
		"\u0000\u0000\u0337\u0338\u0005\u0003\u0000\u0000\u0338\u0339\u0003P(\u0000"+
		"\u0339\u033a\u0005\u0005\u0000\u0000\u033a\u0359\u0001\u0000\u0000\u0000"+
		"\u033b\u033c\u0005\u0001\u0000\u0000\u033c\u033d\u0005\'\u0000\u0000\u033d"+
		"\u033e\u0003\b\u0004\u0000\u033e\u033f\u0007\u0005\u0000\u0000\u033f\u0347"+
		"\u0003P(\u0000\u0340\u0341\u0007\u0005\u0000\u0000\u0341\u0342\u0003\b"+
		"\u0004\u0000\u0342\u0343\u0007\u0005\u0000\u0000\u0343\u0344\u0003P(\u0000"+
		"\u0344\u0346\u0001\u0000\u0000\u0000\u0345\u0340\u0001\u0000\u0000\u0000"+
		"\u0346\u0349\u0001\u0000\u0000\u0000\u0347\u0345\u0001\u0000\u0000\u0000"+
		"\u0347\u0348\u0001\u0000\u0000\u0000\u0348\u034a\u0001\u0000\u0000\u0000"+
		"\u0349\u0347\u0001\u0000\u0000\u0000\u034a\u034b\u0007\u0005\u0000\u0000"+
		"\u034b\u034c\u0003P(\u0000\u034c\u034d\u0005(\u0000\u0000\u034d\u0359"+
		"\u0001\u0000\u0000\u0000\u034e\u0359\u0005<\u0000\u0000\u034f\u0359\u0005"+
		"$\u0000\u0000\u0350\u0352\u0005D\u0000\u0000\u0351\u0350\u0001\u0000\u0000"+
		"\u0000\u0351\u0352\u0001\u0000\u0000\u0000\u0352\u0353\u0001\u0000\u0000"+
		"\u0000\u0353\u0356\u0003(\u0014\u0000\u0354\u0355\u0005/\u0000\u0000\u0355"+
		"\u0357\u0003P(\u0000\u0356\u0354\u0001\u0000\u0000\u0000\u0356\u0357\u0001"+
		"\u0000\u0000\u0000\u0357\u0359\u0001\u0000\u0000\u0000\u0358\u0329\u0001"+
		"\u0000\u0000\u0000\u0358\u033b\u0001\u0000\u0000\u0000\u0358\u034e\u0001"+
		"\u0000\u0000\u0000\u0358\u034f\u0001\u0000\u0000\u0000\u0358\u0351\u0001"+
		"\u0000\u0000\u0000\u0359Q\u0001\u0000\u0000\u0000\u035a\u035b\u0005\u0001"+
		"\u0000\u0000\u035b\u035c\u0003\b\u0004\u0000\u035c\u035d\u0005\u0002\u0000"+
		"\u0000\u035d\u0365\u0003R)\u0000\u035e\u035f\u0005\u0004\u0000\u0000\u035f"+
		"\u0360\u0003\b\u0004\u0000\u0360\u0361\u0005\u0002\u0000\u0000\u0361\u0362"+
		"\u0003R)\u0000\u0362\u0364\u0001\u0000\u0000\u0000\u0363\u035e\u0001\u0000"+
		"\u0000\u0000\u0364\u0367\u0001\u0000\u0000\u0000\u0365\u0363\u0001\u0000"+
		"\u0000\u0000\u0365\u0366\u0001\u0000\u0000\u0000\u0366\u0368\u0001\u0000"+
		"\u0000\u0000\u0367\u0365\u0001\u0000\u0000\u0000\u0368\u0369\u0005\u0003"+
		"\u0000\u0000\u0369\u036a\u0003R)\u0000\u036a\u036b\u0005\u0005\u0000\u0000"+
		"\u036b\u038a\u0001\u0000\u0000\u0000\u036c\u036d\u0005\u0001\u0000\u0000"+
		"\u036d\u036e\u0005\'\u0000\u0000\u036e\u036f\u0003\b\u0004\u0000\u036f"+
		"\u0370\u0007\u0005\u0000\u0000\u0370\u0378\u0003R)\u0000\u0371\u0372\u0007"+
		"\u0005\u0000\u0000\u0372\u0373\u0003\b\u0004\u0000\u0373\u0374\u0007\u0005"+
		"\u0000\u0000\u0374\u0375\u0003R)\u0000\u0375\u0377\u0001\u0000\u0000\u0000"+
		"\u0376\u0371\u0001\u0000\u0000\u0000\u0377\u037a\u0001\u0000\u0000\u0000"+
		"\u0378\u0376\u0001\u0000\u0000\u0000\u0378\u0379\u0001\u0000\u0000\u0000"+
		"\u0379\u037b\u0001\u0000\u0000\u0000\u037a\u0378\u0001\u0000\u0000\u0000"+
		"\u037b\u037c\u0007\u0005\u0000\u0000\u037c\u037d\u0003R)\u0000\u037d\u037e"+
		"\u0005(\u0000\u0000\u037e\u038a\u0001\u0000\u0000\u0000\u037f\u038a\u0005"+
		"=\u0000\u0000\u0380\u038a\u0005%\u0000\u0000\u0381\u0383\u0005E\u0000"+
		"\u0000\u0382\u0381\u0001\u0000\u0000\u0000\u0382\u0383\u0001\u0000\u0000"+
		"\u0000\u0383\u0384\u0001\u0000\u0000\u0000\u0384\u0387\u0003(\u0014\u0000"+
		"\u0385\u0386\u0005/\u0000\u0000\u0386\u0388\u0003R)\u0000\u0387\u0385"+
		"\u0001\u0000\u0000\u0000\u0387\u0388\u0001\u0000\u0000\u0000\u0388\u038a"+
		"\u0001\u0000\u0000\u0000\u0389\u035a\u0001\u0000\u0000\u0000\u0389\u036c"+
		"\u0001\u0000\u0000\u0000\u0389\u037f\u0001\u0000\u0000\u0000\u0389\u0380"+
		"\u0001\u0000\u0000\u0000\u0389\u0382\u0001\u0000\u0000\u0000\u038aS\u0001"+
		"\u0000\u0000\u0000\u038b\u038c\u0005\u0001\u0000\u0000\u038c\u038d\u0003"+
		"\b\u0004\u0000\u038d\u038e\u0005\u0002\u0000\u0000\u038e\u0396\u0003T"+
		"*\u0000\u038f\u0390\u0005\u0004\u0000\u0000\u0390\u0391\u0003\b\u0004"+
		"\u0000\u0391\u0392\u0005\u0002\u0000\u0000\u0392\u0393\u0003T*\u0000\u0393"+
		"\u0395\u0001\u0000\u0000\u0000\u0394\u038f\u0001\u0000\u0000\u0000\u0395"+
		"\u0398\u0001\u0000\u0000\u0000\u0396\u0394\u0001\u0000\u0000\u0000\u0396"+
		"\u0397\u0001\u0000\u0000\u0000\u0397\u0399\u0001\u0000\u0000\u0000\u0398"+
		"\u0396\u0001\u0000\u0000\u0000\u0399\u039a\u0005\u0003\u0000\u0000\u039a"+
		"\u039b\u0003T*\u0000\u039b\u039c\u0005\u0005\u0000\u0000\u039c\u03be\u0001"+
		"\u0000\u0000\u0000\u039d\u039e\u0005\u0001\u0000\u0000\u039e\u039f\u0005"+
		"\'\u0000\u0000\u039f\u03a0\u0003\b\u0004\u0000\u03a0\u03a1\u0007\u0005"+
		"\u0000\u0000\u03a1\u03a9\u0003T*\u0000\u03a2\u03a3\u0007\u0005\u0000\u0000"+
		"\u03a3\u03a4\u0003\b\u0004\u0000\u03a4\u03a5\u0007\u0005\u0000\u0000\u03a5"+
		"\u03a6\u0003T*\u0000\u03a6\u03a8\u0001\u0000\u0000\u0000\u03a7\u03a2\u0001"+
		"\u0000\u0000\u0000\u03a8\u03ab\u0001\u0000\u0000\u0000\u03a9\u03a7\u0001"+
		"\u0000\u0000\u0000\u03a9\u03aa\u0001\u0000\u0000\u0000\u03aa\u03ac\u0001"+
		"\u0000\u0000\u0000\u03ab\u03a9\u0001\u0000\u0000\u0000\u03ac\u03ad\u0007"+
		"\u0005\u0000\u0000\u03ad\u03ae\u0003T*\u0000\u03ae\u03af\u0005(\u0000"+
		"\u0000\u03af\u03be\u0001\u0000\u0000\u0000\u03b0\u03b2\u0005?\u0000\u0000"+
		"\u03b1\u03b3\u0005>\u0000\u0000\u03b2\u03b1\u0001\u0000\u0000\u0000\u03b2"+
		"\u03b3\u0001\u0000\u0000\u0000\u03b3\u03be\u0001\u0000\u0000\u0000\u03b4"+
		"\u03be\u0005&\u0000\u0000\u03b5\u03b7\u0005F\u0000\u0000\u03b6\u03b5\u0001"+
		"\u0000\u0000\u0000\u03b6\u03b7\u0001\u0000\u0000\u0000\u03b7\u03b8\u0001"+
		"\u0000\u0000\u0000\u03b8\u03bb\u0003(\u0014\u0000\u03b9\u03ba\u0005/\u0000"+
		"\u0000\u03ba\u03bc\u0003T*\u0000\u03bb\u03b9\u0001\u0000\u0000\u0000\u03bb"+
		"\u03bc\u0001\u0000\u0000\u0000\u03bc\u03be\u0001\u0000\u0000\u0000\u03bd"+
		"\u038b\u0001\u0000\u0000\u0000\u03bd\u039d\u0001\u0000\u0000\u0000\u03bd"+
		"\u03b0\u0001\u0000\u0000\u0000\u03bd\u03b4\u0001\u0000\u0000\u0000\u03bd"+
		"\u03b6\u0001\u0000\u0000\u0000\u03beU\u0001\u0000\u0000\u0000\u03bf\u03c0"+
		"\u0005\u0001\u0000\u0000\u03c0\u03c1\u0003\b\u0004\u0000\u03c1\u03c2\u0005"+
		"\u0002\u0000\u0000\u03c2\u03ca\u0003V+\u0000\u03c3\u03c4\u0005\u0004\u0000"+
		"\u0000\u03c4\u03c5\u0003\b\u0004\u0000\u03c5\u03c6\u0005\u0002\u0000\u0000"+
		"\u03c6\u03c7\u0003V+\u0000\u03c7\u03c9\u0001\u0000\u0000\u0000\u03c8\u03c3"+
		"\u0001\u0000\u0000\u0000\u03c9\u03cc\u0001\u0000\u0000\u0000\u03ca\u03c8"+
		"\u0001\u0000\u0000\u0000\u03ca\u03cb\u0001\u0000\u0000\u0000\u03cb\u03cd"+
		"\u0001\u0000\u0000\u0000\u03cc\u03ca\u0001\u0000\u0000\u0000\u03cd\u03ce"+
		"\u0005\u0003\u0000\u0000\u03ce\u03cf\u0003V+\u0000\u03cf\u03d0\u0005\u0005"+
		"\u0000\u0000\u03d0\u03f8\u0001\u0000\u0000\u0000\u03d1\u03d2\u0005\u0001"+
		"\u0000\u0000\u03d2\u03d3\u0005\'\u0000\u0000\u03d3\u03d4\u0003\b\u0004"+
		"\u0000\u03d4\u03d5\u0007\u0005\u0000\u0000\u03d5\u03dd\u0003V+\u0000\u03d6"+
		"\u03d7\u0007\u0005\u0000\u0000\u03d7\u03d8\u0003\b\u0004\u0000\u03d8\u03d9"+
		"\u0007\u0005\u0000\u0000\u03d9\u03da\u0003V+\u0000\u03da\u03dc\u0001\u0000"+
		"\u0000\u0000\u03db\u03d6\u0001\u0000\u0000\u0000\u03dc\u03df\u0001\u0000"+
		"\u0000\u0000\u03dd\u03db\u0001\u0000\u0000\u0000\u03dd\u03de\u0001\u0000"+
		"\u0000\u0000\u03de\u03e0\u0001\u0000\u0000\u0000\u03df\u03dd\u0001\u0000"+
		"\u0000\u0000\u03e0\u03e1\u0007\u0005\u0000\u0000\u03e1\u03e2\u0003V+\u0000"+
		"\u03e2\u03e3\u0005(\u0000\u0000\u03e3\u03f8\u0001\u0000\u0000\u0000\u03e4"+
		"\u03e5\u0005)\u0000\u0000\u03e5\u03ea\u0003>\u001f\u0000\u03e6\u03e7\u0005"+
		"+\u0000\u0000\u03e7\u03e9\u0003>\u001f\u0000\u03e8\u03e6\u0001\u0000\u0000"+
		"\u0000\u03e9\u03ec\u0001\u0000\u0000\u0000\u03ea\u03e8\u0001\u0000\u0000"+
		"\u0000\u03ea\u03eb\u0001\u0000\u0000\u0000\u03eb\u03ed\u0001\u0000\u0000"+
		"\u0000\u03ec\u03ea\u0001\u0000\u0000\u0000\u03ed\u03ee\u0005*\u0000\u0000"+
		"\u03ee\u03f8\u0001\u0000\u0000\u0000\u03ef\u03f1\u0005G\u0000\u0000\u03f0"+
		"\u03ef\u0001\u0000\u0000\u0000\u03f0\u03f1\u0001\u0000\u0000\u0000\u03f1"+
		"\u03f2\u0001\u0000\u0000\u0000\u03f2\u03f5\u0003(\u0014\u0000\u03f3\u03f4"+
		"\u0005/\u0000\u0000\u03f4\u03f6\u0003V+\u0000\u03f5\u03f3\u0001\u0000"+
		"\u0000\u0000\u03f5\u03f6\u0001\u0000\u0000\u0000\u03f6\u03f8\u0001\u0000"+
		"\u0000\u0000\u03f7\u03bf\u0001\u0000\u0000\u0000\u03f7\u03d1\u0001\u0000"+
		"\u0000\u0000\u03f7\u03e4\u0001\u0000\u0000\u0000\u03f7\u03f0\u0001\u0000"+
		"\u0000\u0000\u03f8W\u0001\u0000\u0000\u0000\u03f9\u03fa\u0005)\u0000\u0000"+
		"\u03fa\u03ff\u00058\u0000\u0000\u03fb\u03fc\u0005+\u0000\u0000\u03fc\u03fe"+
		"\u00058\u0000\u0000\u03fd\u03fb\u0001\u0000\u0000\u0000\u03fe\u0401\u0001"+
		"\u0000\u0000\u0000\u03ff\u03fd\u0001\u0000\u0000\u0000\u03ff\u0400\u0001"+
		"\u0000\u0000\u0000\u0400\u0402\u0001\u0000\u0000\u0000\u0401\u03ff\u0001"+
		"\u0000\u0000\u0000\u0402\u0403\u0005*\u0000\u0000\u0403Y\u0001\u0000\u0000"+
		"\u0000V]fm}\u0086\u008e\u0095\u0131\u0138\u013e\u0145\u014e\u0156\u015c"+
		"\u0163\u0169\u016f\u0180\u0189\u018c\u0195\u019c\u019f\u01a7\u01aa\u01b4"+
		"\u01c1\u01c4\u01cf\u01d2\u01da\u01e6\u01fd\u0200\u0209\u0211\u0228\u022e"+
		"\u0236\u023c\u0244\u024e\u0258\u0265\u0278\u0281\u028f\u029d\u02b0\u02b8"+
		"\u02bd\u02bf\u02cc\u02df\u02e8\u02ed\u02ef\u02f6\u0304\u0317\u0320\u0325"+
		"\u0327\u0334\u0347\u0351\u0356\u0358\u0365\u0378\u0382\u0387\u0389\u0396"+
		"\u03a9\u03b2\u03b6\u03bb\u03bd\u03ca\u03dd\u03ea\u03f0\u03f5\u03f7\u03ff";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}