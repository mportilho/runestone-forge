// Generated from C:/dev/git/runestone-forge/expression-evaluator/src/main/resources/ExpressionEvaluator.g4 by ANTLR 4.13.1
package com.runestone.expeval.grammar.language;
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
		IF=1, THEN=2, ELSE=3, ELSEIF=4, ENDIF=5, AND=6, OR=7, XOR=8, XNOR=9, NAND=10, 
		NOR=11, TRUE=12, FALSE=13, MULT=14, DIV=15, PLUS=16, MINUS=17, PERCENT=18, 
		MODULUS=19, EXCLAMATION=20, EXPONENTIATION=21, ROOT=22, SQRT=23, BINARY_LOGARITHM=24, 
		NATURAL_LOGARITHM=25, COMMOM_LOGARITHM=26, LOGARITHM=27, SUMMATION=28, 
		PRODUCT_SEQUENCE=29, SUMMATION_VARIABLE=30, PRODUCT_SEQUENCE_VARIABLE=31, 
		MINUS_PARENTHESIS=32, PI=33, EULER=34, DEGREE=35, GT=36, GE=37, LT=38, 
		LE=39, EQ=40, NEQ=41, NOT=42, R_UP=43, R_DOWN=44, R_CEILING=45, R_FLOOR=46, 
		R_HALF_UP=47, R_HALF_DOWN=48, R_HALF_EVEN=49, R_UNNECESSARY=50, DATE_OPERATIONS=51, 
		TIME_OPERATIONS=52, NOW_DATE=53, NOW_TIME=54, NOW_DATETIME=55, PLUS_DAYS=56, 
		PLUS_MONTHS=57, PLUS_YEARS=58, PLUS_HOURS=59, PLUS_MINUTES=60, PLUS_SECONDS=61, 
		MINUS_DAYS=62, MINUS_MONTHS=63, MINUS_YEARS=64, MINUS_HOURS=65, MINUS_MINUTES=66, 
		MINUS_SECONDS=67, SET_DAYS=68, SET_MONTHS=69, SET_YEARS=70, SET_HOURS=71, 
		SET_MINUTES=72, SET_SECONDS=73, LPAREN=74, RPAREN=75, LBLACKET=76, RBLACKET=77, 
		QUOTES=78, ASSIGNMENT=79, COMMA=80, SEMI=81, PERIOD=82, CONTAINS=83, CACHE_FUNCTION_PREFIX=84, 
		IDENTIFIER=85, NEGATIVE_IDENTIFIER=86, STRING=87, NUMBER=88, POSITIVE=89, 
		DATE=90, TIME=91, TIME_OFFSET=92, DATETIME=93, BOOLEAN_TYPE=94, NUMBER_TYPE=95, 
		STRING_TYPE=96, DATE_TYPE=97, TIME_TYPE=98, DATETIME_TYPE=99, VECTOR_TYPE=100, 
		COMMENT=101, WS=102;
	public static final int
		RULE_start = 0, RULE_mathStart = 1, RULE_logicalStart = 2, RULE_assignmentExpression = 3, 
		RULE_logicalExpression = 4, RULE_mathExpression = 5, RULE_mathSpecificFunction = 6, 
		RULE_logarithmFunction = 7, RULE_roundingFunction = 8, RULE_sequenceFunction = 9, 
		RULE_dateOperation = 10, RULE_timeOperation = 11, RULE_dateTimeOperation = 12, 
		RULE_function = 13, RULE_comparisonOperator = 14, RULE_logicalOperator = 15, 
		RULE_allEntityTypes = 16, RULE_logicalEntity = 17, RULE_numericEntity = 18, 
		RULE_stringEntity = 19, RULE_dateEntity = 20, RULE_timeEntity = 21, RULE_dateTimeEntity = 22, 
		RULE_vectorEntity = 23, RULE_vectorOfVariables = 24;
	private static String[] makeRuleNames() {
		return new String[] {
			"start", "mathStart", "logicalStart", "assignmentExpression", "logicalExpression", 
			"mathExpression", "mathSpecificFunction", "logarithmFunction", "roundingFunction", 
			"sequenceFunction", "dateOperation", "timeOperation", "dateTimeOperation", 
			"function", "comparisonOperator", "logicalOperator", "allEntityTypes", 
			"logicalEntity", "numericEntity", "stringEntity", "dateEntity", "timeEntity", 
			"dateTimeEntity", "vectorEntity", "vectorOfVariables"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'if'", "'then'", "'else'", "'elsif'", "'endif'", "'and'", "'or'", 
			"'xor'", "'xnor'", "'nand'", "'nor'", "'true'", "'false'", "'*'", "'/'", 
			"'+'", "'-'", "'%'", "'|'", "'!'", "'^'", null, "'sqrt'", "'lb'", "'ln'", 
			"'log10'", "'log'", "'S['", "'P['", "'S'", "'P'", null, null, null, null, 
			"'>'", "'>='", "'<'", "'<='", "'='", null, null, "'up'", "'down'", "'ceiling'", 
			"'floor'", "'halfUp'", "'halfDown'", "'halfEven'", "'unnecessary'", null, 
			null, "'currDate'", "'currTime'", "'currDateTime'", "'plusDays'", "'plusMonths'", 
			"'plusYears'", "'plusHours'", "'plusMinutes'", "'plusSeconds'", "'minusDays'", 
			"'minusMonths'", "'minusYears'", "'minusHours'", "'minusMinutes'", "'minusSeconds'", 
			"'setDays'", "'setMonths'", "'setYears'", "'setHours'", "'setMinutes'", 
			"'setSeconds'", "'('", "')'", "'['", "']'", "'''", "':='", "','", "';'", 
			"'.'", "'contains'", "'$.'", null, null, null, null, null, null, null, 
			null, null, "'<bool>'", "'<number>'", "'<text>'", "'<date>'", "'<time>'", 
			"'<datetime>'", "'<vector>'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "IF", "THEN", "ELSE", "ELSEIF", "ENDIF", "AND", "OR", "XOR", "XNOR", 
			"NAND", "NOR", "TRUE", "FALSE", "MULT", "DIV", "PLUS", "MINUS", "PERCENT", 
			"MODULUS", "EXCLAMATION", "EXPONENTIATION", "ROOT", "SQRT", "BINARY_LOGARITHM", 
			"NATURAL_LOGARITHM", "COMMOM_LOGARITHM", "LOGARITHM", "SUMMATION", "PRODUCT_SEQUENCE", 
			"SUMMATION_VARIABLE", "PRODUCT_SEQUENCE_VARIABLE", "MINUS_PARENTHESIS", 
			"PI", "EULER", "DEGREE", "GT", "GE", "LT", "LE", "EQ", "NEQ", "NOT", 
			"R_UP", "R_DOWN", "R_CEILING", "R_FLOOR", "R_HALF_UP", "R_HALF_DOWN", 
			"R_HALF_EVEN", "R_UNNECESSARY", "DATE_OPERATIONS", "TIME_OPERATIONS", 
			"NOW_DATE", "NOW_TIME", "NOW_DATETIME", "PLUS_DAYS", "PLUS_MONTHS", "PLUS_YEARS", 
			"PLUS_HOURS", "PLUS_MINUTES", "PLUS_SECONDS", "MINUS_DAYS", "MINUS_MONTHS", 
			"MINUS_YEARS", "MINUS_HOURS", "MINUS_MINUTES", "MINUS_SECONDS", "SET_DAYS", 
			"SET_MONTHS", "SET_YEARS", "SET_HOURS", "SET_MINUTES", "SET_SECONDS", 
			"LPAREN", "RPAREN", "LBLACKET", "RBLACKET", "QUOTES", "ASSIGNMENT", "COMMA", 
			"SEMI", "PERIOD", "CONTAINS", "CACHE_FUNCTION_PREFIX", "IDENTIFIER", 
			"NEGATIVE_IDENTIFIER", "STRING", "NUMBER", "POSITIVE", "DATE", "TIME", 
			"TIME_OFFSET", "DATETIME", "BOOLEAN_TYPE", "NUMBER_TYPE", "STRING_TYPE", 
			"DATE_TYPE", "TIME_TYPE", "DATETIME_TYPE", "VECTOR_TYPE", "COMMENT", 
			"WS"
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
	public static class StartContext extends ParserRuleContext {
		public MathStartContext mathStart() {
			return getRuleContext(MathStartContext.class,0);
		}
		public LogicalStartContext logicalStart() {
			return getRuleContext(LogicalStartContext.class,0);
		}
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_start);
		try {
			setState(52);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(50);
				mathStart();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(51);
				logicalStart();
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
	public static class MathStartContext extends ParserRuleContext {
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
		public MathStartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mathStart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathStart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathStart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MathStartContext mathStart() throws RecognitionException {
		MathStartContext _localctx = new MathStartContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_mathStart);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(57);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(54);
					assignmentExpression();
					}
					} 
				}
				setState(59);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			}
			setState(60);
			mathExpression(0);
			setState(61);
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
		public TerminalNode EOF() { return getToken(ExpressionEvaluatorParser.EOF, 0); }
		public List<AssignmentExpressionContext> assignmentExpression() {
			return getRuleContexts(AssignmentExpressionContext.class);
		}
		public AssignmentExpressionContext assignmentExpression(int i) {
			return getRuleContext(AssignmentExpressionContext.class,i);
		}
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public LogicalStartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalStart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalStart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalStart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalStartContext logicalStart() throws RecognitionException {
		LogicalStartContext _localctx = new LogicalStartContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_logicalStart);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(63);
					assignmentExpression();
					}
					} 
				}
				setState(68);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			setState(70);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 65297830903296002L) != 0) || ((((_la - 74)) & ~0x3f) == 0 && ((1L << (_la - 74)) & 66812929L) != 0)) {
				{
				setState(69);
				logicalExpression(0);
				}
			}

			setState(72);
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
	public static class AssignOperationContext extends AssignmentExpressionContext {
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public AllEntityTypesContext allEntityTypes() {
			return getRuleContext(AllEntityTypesContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(ExpressionEvaluatorParser.SEMI, 0); }
		public TerminalNode EQ() { return getToken(ExpressionEvaluatorParser.EQ, 0); }
		public TerminalNode ASSIGNMENT() { return getToken(ExpressionEvaluatorParser.ASSIGNMENT, 0); }
		public AssignOperationContext(AssignmentExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterAssignOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitAssignOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitAssignOperation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DestructuringAssignmentContext extends AssignmentExpressionContext {
		public VectorOfVariablesContext vectorOfVariables() {
			return getRuleContext(VectorOfVariablesContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(ExpressionEvaluatorParser.SEMI, 0); }
		public TerminalNode EQ() { return getToken(ExpressionEvaluatorParser.EQ, 0); }
		public TerminalNode ASSIGNMENT() { return getToken(ExpressionEvaluatorParser.ASSIGNMENT, 0); }
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public DestructuringAssignmentContext(AssignmentExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDestructuringAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDestructuringAssignment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDestructuringAssignment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentExpressionContext assignmentExpression() throws RecognitionException {
		AssignmentExpressionContext _localctx = new AssignmentExpressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_assignmentExpression);
		int _la;
		try {
			setState(87);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				_localctx = new AssignOperationContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(74);
				match(IDENTIFIER);
				setState(75);
				_la = _input.LA(1);
				if ( !(_la==EQ || _la==ASSIGNMENT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(76);
				allEntityTypes();
				setState(77);
				match(SEMI);
				}
				break;
			case LBLACKET:
				_localctx = new DestructuringAssignmentContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(79);
				vectorOfVariables();
				setState(80);
				_la = _input.LA(1);
				if ( !(_la==EQ || _la==ASSIGNMENT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(83);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
				case 1:
					{
					setState(81);
					vectorEntity();
					}
					break;
				case 2:
					{
					setState(82);
					function();
					}
					break;
				}
				setState(85);
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
	public static class DateTimeExpressionContext extends LogicalExpressionContext {
		public List<DateTimeOperationContext> dateTimeOperation() {
			return getRuleContexts(DateTimeOperationContext.class);
		}
		public DateTimeOperationContext dateTimeOperation(int i) {
			return getRuleContext(DateTimeOperationContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public DateTimeExpressionContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringExpressionContext extends LogicalExpressionContext {
		public List<StringEntityContext> stringEntity() {
			return getRuleContexts(StringEntityContext.class);
		}
		public StringEntityContext stringEntity(int i) {
			return getRuleContext(StringEntityContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public StringExpressionContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalValueContext extends LogicalExpressionContext {
		public LogicalEntityContext logicalEntity() {
			return getRuleContext(LogicalEntityContext.class,0);
		}
		public LogicalValueContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicExpressionContext extends LogicalExpressionContext {
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public LogicalOperatorContext logicalOperator() {
			return getRuleContext(LogicalOperatorContext.class,0);
		}
		public LogicExpressionContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateExpressionContext extends LogicalExpressionContext {
		public List<DateOperationContext> dateOperation() {
			return getRuleContexts(DateOperationContext.class);
		}
		public DateOperationContext dateOperation(int i) {
			return getRuleContext(DateOperationContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public DateExpressionContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NotExpressionContext extends LogicalExpressionContext {
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public TerminalNode NOT() { return getToken(ExpressionEvaluatorParser.NOT, 0); }
		public TerminalNode EXCLAMATION() { return getToken(ExpressionEvaluatorParser.EXCLAMATION, 0); }
		public NotExpressionContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNotExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNotExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNotExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicComparisonExpressionContext extends LogicalExpressionContext {
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public LogicComparisonExpressionContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicComparisonExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicComparisonExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicComparisonExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonMathExpressionContext extends LogicalExpressionContext {
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public ComparisonMathExpressionContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterComparisonMathExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitComparisonMathExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitComparisonMathExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeExpressionContext extends LogicalExpressionContext {
		public List<TimeOperationContext> timeOperation() {
			return getRuleContexts(TimeOperationContext.class);
		}
		public TimeOperationContext timeOperation(int i) {
			return getRuleContext(TimeOperationContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public TimeExpressionContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalParenthesisContext extends LogicalExpressionContext {
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public LogicalParenthesisContext(LogicalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalParenthesis(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalParenthesis(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalParenthesis(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalExpressionContext logicalExpression() throws RecognitionException {
		return logicalExpression(0);
	}

	private LogicalExpressionContext logicalExpression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		LogicalExpressionContext _localctx = new LogicalExpressionContext(_ctx, _parentState);
		LogicalExpressionContext _prevctx = _localctx;
		int _startState = 8;
		enterRecursionRule(_localctx, 8, RULE_logicalExpression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				{
				_localctx = new NotExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(90);
				_la = _input.LA(1);
				if ( !(_la==EXCLAMATION || _la==NOT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(91);
				logicalExpression(10);
				}
				break;
			case 2:
				{
				_localctx = new ComparisonMathExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(92);
				mathExpression(0);
				setState(93);
				comparisonOperator();
				setState(94);
				mathExpression(0);
				}
				break;
			case 3:
				{
				_localctx = new StringExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(96);
				stringEntity();
				setState(97);
				comparisonOperator();
				setState(98);
				stringEntity();
				}
				break;
			case 4:
				{
				_localctx = new DateExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(100);
				dateOperation();
				setState(101);
				comparisonOperator();
				setState(102);
				dateOperation();
				}
				break;
			case 5:
				{
				_localctx = new TimeExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(104);
				timeOperation();
				setState(105);
				comparisonOperator();
				setState(106);
				timeOperation();
				}
				break;
			case 6:
				{
				_localctx = new DateTimeExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(108);
				dateTimeOperation();
				setState(109);
				comparisonOperator();
				setState(110);
				dateTimeOperation();
				}
				break;
			case 7:
				{
				_localctx = new LogicalParenthesisContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(112);
				match(LPAREN);
				setState(113);
				logicalExpression(0);
				setState(114);
				match(RPAREN);
				}
				break;
			case 8:
				{
				_localctx = new LogicalValueContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(116);
				logicalEntity();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(129);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(127);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
					case 1:
						{
						_localctx = new LogicExpressionContext(new LogicalExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_logicalExpression);
						setState(119);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(120);
						logicalOperator();
						setState(121);
						logicalExpression(10);
						}
						break;
					case 2:
						{
						_localctx = new LogicComparisonExpressionContext(new LogicalExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_logicalExpression);
						setState(123);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(124);
						comparisonOperator();
						setState(125);
						logicalExpression(9);
						}
						break;
					}
					} 
				}
				setState(131);
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
			unrollRecursionContexts(_parentctx);
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
	public static class ModulusExpressionContext extends MathExpressionContext {
		public List<TerminalNode> MODULUS() { return getTokens(ExpressionEvaluatorParser.MODULUS); }
		public TerminalNode MODULUS(int i) {
			return getToken(ExpressionEvaluatorParser.MODULUS, i);
		}
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public ModulusExpressionContext(MathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterModulusExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitModulusExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitModulusExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathParenthesisContext extends MathExpressionContext {
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public MathParenthesisContext(MathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathParenthesis(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathParenthesis(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathParenthesis(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MultiplicationExpressionContext extends MathExpressionContext {
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public TerminalNode MULT() { return getToken(ExpressionEvaluatorParser.MULT, 0); }
		public TerminalNode DIV() { return getToken(ExpressionEvaluatorParser.DIV, 0); }
		public TerminalNode PERCENT() { return getToken(ExpressionEvaluatorParser.PERCENT, 0); }
		public MultiplicationExpressionContext(MathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMultiplicationExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMultiplicationExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMultiplicationExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathSpecificExpressionContext extends MathExpressionContext {
		public MathSpecificFunctionContext mathSpecificFunction() {
			return getRuleContext(MathSpecificFunctionContext.class,0);
		}
		public MathSpecificExpressionContext(MathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathSpecificExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathSpecificExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathSpecificExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FactorialExpressionContext extends MathExpressionContext {
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode EXCLAMATION() { return getToken(ExpressionEvaluatorParser.EXCLAMATION, 0); }
		public FactorialExpressionContext(MathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterFactorialExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitFactorialExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitFactorialExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NegateMathParenthesisContext extends MathExpressionContext {
		public TerminalNode MINUS_PARENTHESIS() { return getToken(ExpressionEvaluatorParser.MINUS_PARENTHESIS, 0); }
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public NegateMathParenthesisContext(MathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNegateMathParenthesis(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNegateMathParenthesis(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNegateMathParenthesis(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SquareRootExpressionContext extends MathExpressionContext {
		public TerminalNode SQRT() { return getToken(ExpressionEvaluatorParser.SQRT, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public SquareRootExpressionContext(MathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterSquareRootExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitSquareRootExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitSquareRootExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PercentExpressionContext extends MathExpressionContext {
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode PERCENT() { return getToken(ExpressionEvaluatorParser.PERCENT, 0); }
		public PercentExpressionContext(MathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterPercentExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitPercentExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitPercentExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RootExpressionContext extends MathExpressionContext {
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public TerminalNode ROOT() { return getToken(ExpressionEvaluatorParser.ROOT, 0); }
		public RootExpressionContext(MathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterRootExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitRootExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitRootExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SumExpressionContext extends MathExpressionContext {
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public TerminalNode PLUS() { return getToken(ExpressionEvaluatorParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(ExpressionEvaluatorParser.MINUS, 0); }
		public SumExpressionContext(MathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterSumExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitSumExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitSumExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DegreeExpressionContext extends MathExpressionContext {
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode DEGREE() { return getToken(ExpressionEvaluatorParser.DEGREE, 0); }
		public DegreeExpressionContext(MathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDegreeExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDegreeExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDegreeExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumberValueContext extends MathExpressionContext {
		public NumericEntityContext numericEntity() {
			return getRuleContext(NumericEntityContext.class,0);
		}
		public NumberValueContext(MathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNumberValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNumberValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNumberValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExponentiationExpressionContext extends MathExpressionContext {
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public TerminalNode EXPONENTIATION() { return getToken(ExpressionEvaluatorParser.EXPONENTIATION, 0); }
		public ExponentiationExpressionContext(MathExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterExponentiationExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitExponentiationExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitExponentiationExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MathExpressionContext mathExpression() throws RecognitionException {
		return mathExpression(0);
	}

	private MathExpressionContext mathExpression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		MathExpressionContext _localctx = new MathExpressionContext(_ctx, _parentState);
		MathExpressionContext _prevctx = _localctx;
		int _startState = 10;
		enterRecursionRule(_localctx, 10, RULE_mathExpression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(152);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				{
				_localctx = new MathParenthesisContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(133);
				match(LPAREN);
				setState(134);
				mathExpression(0);
				setState(135);
				match(RPAREN);
				}
				break;
			case MINUS_PARENTHESIS:
				{
				_localctx = new NegateMathParenthesisContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(137);
				match(MINUS_PARENTHESIS);
				setState(138);
				mathExpression(0);
				setState(139);
				match(RPAREN);
				}
				break;
			case SQRT:
				{
				_localctx = new SquareRootExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(141);
				match(SQRT);
				setState(142);
				match(LPAREN);
				setState(143);
				mathExpression(0);
				setState(144);
				match(RPAREN);
				}
				break;
			case MODULUS:
				{
				_localctx = new ModulusExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(146);
				match(MODULUS);
				setState(147);
				mathExpression(0);
				setState(148);
				match(MODULUS);
				}
				break;
			case BINARY_LOGARITHM:
			case NATURAL_LOGARITHM:
			case COMMOM_LOGARITHM:
			case LOGARITHM:
			case SUMMATION:
			case PRODUCT_SEQUENCE:
			case R_UP:
			case R_DOWN:
			case R_CEILING:
			case R_FLOOR:
			case R_HALF_UP:
			case R_HALF_DOWN:
			case R_HALF_EVEN:
			case R_UNNECESSARY:
				{
				_localctx = new MathSpecificExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(150);
				mathSpecificFunction();
				}
				break;
			case IF:
			case SUMMATION_VARIABLE:
			case PRODUCT_SEQUENCE_VARIABLE:
			case PI:
			case EULER:
			case CACHE_FUNCTION_PREFIX:
			case IDENTIFIER:
			case NEGATIVE_IDENTIFIER:
			case NUMBER:
			case NUMBER_TYPE:
				{
				_localctx = new NumberValueContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(151);
				numericEntity();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(174);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(172);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
					case 1:
						{
						_localctx = new RootExpressionContext(new MathExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_mathExpression);
						setState(154);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(155);
						match(ROOT);
						setState(156);
						mathExpression(10);
						}
						break;
					case 2:
						{
						_localctx = new ExponentiationExpressionContext(new MathExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_mathExpression);
						setState(157);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(158);
						match(EXPONENTIATION);
						setState(159);
						mathExpression(7);
						}
						break;
					case 3:
						{
						_localctx = new MultiplicationExpressionContext(new MathExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_mathExpression);
						setState(160);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(161);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 311296L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(162);
						mathExpression(7);
						}
						break;
					case 4:
						{
						_localctx = new SumExpressionContext(new MathExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_mathExpression);
						setState(163);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(164);
						_la = _input.LA(1);
						if ( !(_la==PLUS || _la==MINUS) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(165);
						mathExpression(6);
						}
						break;
					case 5:
						{
						_localctx = new PercentExpressionContext(new MathExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_mathExpression);
						setState(166);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(167);
						match(PERCENT);
						}
						break;
					case 6:
						{
						_localctx = new FactorialExpressionContext(new MathExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_mathExpression);
						setState(168);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(169);
						match(EXCLAMATION);
						}
						break;
					case 7:
						{
						_localctx = new DegreeExpressionContext(new MathExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_mathExpression);
						setState(170);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(171);
						match(DEGREE);
						}
						break;
					}
					} 
				}
				setState(176);
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
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MathSpecificFunctionContext extends ParserRuleContext {
		public MathSpecificFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mathSpecificFunction; }
	 
		public MathSpecificFunctionContext() { }
		public void copyFrom(MathSpecificFunctionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SequenceExpressionContext extends MathSpecificFunctionContext {
		public SequenceFunctionContext sequenceFunction() {
			return getRuleContext(SequenceFunctionContext.class,0);
		}
		public SequenceExpressionContext(MathSpecificFunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterSequenceExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitSequenceExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitSequenceExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogarithmExpressionContext extends MathSpecificFunctionContext {
		public LogarithmFunctionContext logarithmFunction() {
			return getRuleContext(LogarithmFunctionContext.class,0);
		}
		public LogarithmExpressionContext(MathSpecificFunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogarithmExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogarithmExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogarithmExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RoundingExpressionContext extends MathSpecificFunctionContext {
		public RoundingFunctionContext roundingFunction() {
			return getRuleContext(RoundingFunctionContext.class,0);
		}
		public RoundingExpressionContext(MathSpecificFunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterRoundingExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitRoundingExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitRoundingExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MathSpecificFunctionContext mathSpecificFunction() throws RecognitionException {
		MathSpecificFunctionContext _localctx = new MathSpecificFunctionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_mathSpecificFunction);
		try {
			setState(180);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BINARY_LOGARITHM:
			case NATURAL_LOGARITHM:
			case COMMOM_LOGARITHM:
			case LOGARITHM:
				_localctx = new LogarithmExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(177);
				logarithmFunction();
				}
				break;
			case R_UP:
			case R_DOWN:
			case R_CEILING:
			case R_FLOOR:
			case R_HALF_UP:
			case R_HALF_DOWN:
			case R_HALF_EVEN:
			case R_UNNECESSARY:
				_localctx = new RoundingExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(178);
				roundingFunction();
				}
				break;
			case SUMMATION:
			case PRODUCT_SEQUENCE:
				_localctx = new SequenceExpressionContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(179);
				sequenceFunction();
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
	public static class LogarithmFunctionContext extends ParserRuleContext {
		public LogarithmFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logarithmFunction; }
	 
		public LogarithmFunctionContext() { }
		public void copyFrom(LogarithmFunctionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VariableLogarithmContext extends LogarithmFunctionContext {
		public TerminalNode LOGARITHM() { return getToken(ExpressionEvaluatorParser.LOGARITHM, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(ExpressionEvaluatorParser.COMMA, 0); }
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public VariableLogarithmContext(LogarithmFunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterVariableLogarithm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitVariableLogarithm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitVariableLogarithm(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FixedLogarithmContext extends LogarithmFunctionContext {
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public TerminalNode BINARY_LOGARITHM() { return getToken(ExpressionEvaluatorParser.BINARY_LOGARITHM, 0); }
		public TerminalNode NATURAL_LOGARITHM() { return getToken(ExpressionEvaluatorParser.NATURAL_LOGARITHM, 0); }
		public TerminalNode COMMOM_LOGARITHM() { return getToken(ExpressionEvaluatorParser.COMMOM_LOGARITHM, 0); }
		public FixedLogarithmContext(LogarithmFunctionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterFixedLogarithm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitFixedLogarithm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitFixedLogarithm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogarithmFunctionContext logarithmFunction() throws RecognitionException {
		LogarithmFunctionContext _localctx = new LogarithmFunctionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_logarithmFunction);
		int _la;
		try {
			setState(194);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BINARY_LOGARITHM:
			case NATURAL_LOGARITHM:
			case COMMOM_LOGARITHM:
				_localctx = new FixedLogarithmContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(182);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 117440512L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(183);
				match(LPAREN);
				setState(184);
				mathExpression(0);
				setState(185);
				match(RPAREN);
				}
				break;
			case LOGARITHM:
				_localctx = new VariableLogarithmContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(187);
				match(LOGARITHM);
				setState(188);
				match(LPAREN);
				setState(189);
				mathExpression(0);
				setState(190);
				match(COMMA);
				setState(191);
				mathExpression(0);
				setState(192);
				match(RPAREN);
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
	public static class RoundingFunctionContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(ExpressionEvaluatorParser.COMMA, 0); }
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public TerminalNode R_UP() { return getToken(ExpressionEvaluatorParser.R_UP, 0); }
		public TerminalNode R_DOWN() { return getToken(ExpressionEvaluatorParser.R_DOWN, 0); }
		public TerminalNode R_CEILING() { return getToken(ExpressionEvaluatorParser.R_CEILING, 0); }
		public TerminalNode R_FLOOR() { return getToken(ExpressionEvaluatorParser.R_FLOOR, 0); }
		public TerminalNode R_HALF_UP() { return getToken(ExpressionEvaluatorParser.R_HALF_UP, 0); }
		public TerminalNode R_HALF_DOWN() { return getToken(ExpressionEvaluatorParser.R_HALF_DOWN, 0); }
		public TerminalNode R_HALF_EVEN() { return getToken(ExpressionEvaluatorParser.R_HALF_EVEN, 0); }
		public TerminalNode R_UNNECESSARY() { return getToken(ExpressionEvaluatorParser.R_UNNECESSARY, 0); }
		public RoundingFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_roundingFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterRoundingFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitRoundingFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitRoundingFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RoundingFunctionContext roundingFunction() throws RecognitionException {
		RoundingFunctionContext _localctx = new RoundingFunctionContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_roundingFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 2243003720663040L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(197);
			match(LPAREN);
			setState(198);
			mathExpression(0);
			setState(199);
			match(COMMA);
			setState(200);
			mathExpression(0);
			setState(201);
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
	public static class SequenceFunctionContext extends ParserRuleContext {
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public TerminalNode RBLACKET() { return getToken(ExpressionEvaluatorParser.RBLACKET, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public TerminalNode SUMMATION() { return getToken(ExpressionEvaluatorParser.SUMMATION, 0); }
		public TerminalNode PRODUCT_SEQUENCE() { return getToken(ExpressionEvaluatorParser.PRODUCT_SEQUENCE, 0); }
		public SequenceFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sequenceFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterSequenceFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitSequenceFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitSequenceFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SequenceFunctionContext sequenceFunction() throws RecognitionException {
		SequenceFunctionContext _localctx = new SequenceFunctionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_sequenceFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(203);
			_la = _input.LA(1);
			if ( !(_la==SUMMATION || _la==PRODUCT_SEQUENCE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(204);
			vectorEntity();
			setState(205);
			match(RBLACKET);
			setState(206);
			match(LPAREN);
			setState(207);
			mathExpression(0);
			setState(208);
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
	public static class DateOperationContext extends ParserRuleContext {
		public DateOperationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateOperation; }
	 
		public DateOperationContext() { }
		public void copyFrom(DateOperationContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateParenthesisContext extends DateOperationContext {
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public DateOperationContext dateOperation() {
			return getRuleContext(DateOperationContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public DateParenthesisContext(DateOperationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateParenthesis(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateParenthesis(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateParenthesis(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateFunctionContext extends DateOperationContext {
		public DateEntityContext dateEntity() {
			return getRuleContext(DateEntityContext.class,0);
		}
		public List<TerminalNode> DATE_OPERATIONS() { return getTokens(ExpressionEvaluatorParser.DATE_OPERATIONS); }
		public TerminalNode DATE_OPERATIONS(int i) {
			return getToken(ExpressionEvaluatorParser.DATE_OPERATIONS, i);
		}
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public DateFunctionContext(DateOperationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateOperationContext dateOperation() throws RecognitionException {
		DateOperationContext _localctx = new DateOperationContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_dateOperation);
		try {
			int _alt;
			setState(226);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				_localctx = new DateParenthesisContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(210);
				match(LPAREN);
				setState(211);
				dateOperation();
				setState(212);
				match(RPAREN);
				}
				break;
			case IF:
			case NOW_DATE:
			case CACHE_FUNCTION_PREFIX:
			case IDENTIFIER:
			case DATE:
			case DATE_TYPE:
				_localctx = new DateFunctionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(214);
				dateEntity();
				setState(224);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
				case 1:
					{
					setState(215);
					match(DATE_OPERATIONS);
					setState(216);
					mathExpression(0);
					setState(221);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
					while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(217);
							match(DATE_OPERATIONS);
							setState(218);
							mathExpression(0);
							}
							} 
						}
						setState(223);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
					}
					}
					break;
				}
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
	public static class TimeOperationContext extends ParserRuleContext {
		public TimeOperationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeOperation; }
	 
		public TimeOperationContext() { }
		public void copyFrom(TimeOperationContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeParenthesisContext extends TimeOperationContext {
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public TimeOperationContext timeOperation() {
			return getRuleContext(TimeOperationContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public TimeParenthesisContext(TimeOperationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeParenthesis(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeParenthesis(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeParenthesis(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeFunctionContext extends TimeOperationContext {
		public TimeEntityContext timeEntity() {
			return getRuleContext(TimeEntityContext.class,0);
		}
		public List<TerminalNode> TIME_OPERATIONS() { return getTokens(ExpressionEvaluatorParser.TIME_OPERATIONS); }
		public TerminalNode TIME_OPERATIONS(int i) {
			return getToken(ExpressionEvaluatorParser.TIME_OPERATIONS, i);
		}
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public TimeFunctionContext(TimeOperationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TimeOperationContext timeOperation() throws RecognitionException {
		TimeOperationContext _localctx = new TimeOperationContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_timeOperation);
		try {
			int _alt;
			setState(244);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				_localctx = new TimeParenthesisContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(228);
				match(LPAREN);
				setState(229);
				timeOperation();
				setState(230);
				match(RPAREN);
				}
				break;
			case IF:
			case NOW_TIME:
			case CACHE_FUNCTION_PREFIX:
			case IDENTIFIER:
			case TIME:
			case TIME_TYPE:
				_localctx = new TimeFunctionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(232);
				timeEntity();
				setState(242);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
				case 1:
					{
					setState(233);
					match(TIME_OPERATIONS);
					setState(234);
					mathExpression(0);
					setState(239);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
					while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(235);
							match(TIME_OPERATIONS);
							setState(236);
							mathExpression(0);
							}
							} 
						}
						setState(241);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
					}
					}
					break;
				}
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
	public static class DateTimeOperationContext extends ParserRuleContext {
		public DateTimeOperationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateTimeOperation; }
	 
		public DateTimeOperationContext() { }
		public void copyFrom(DateTimeOperationContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeFunctionContext extends DateTimeOperationContext {
		public DateTimeEntityContext dateTimeEntity() {
			return getRuleContext(DateTimeEntityContext.class,0);
		}
		public List<MathExpressionContext> mathExpression() {
			return getRuleContexts(MathExpressionContext.class);
		}
		public MathExpressionContext mathExpression(int i) {
			return getRuleContext(MathExpressionContext.class,i);
		}
		public List<TerminalNode> DATE_OPERATIONS() { return getTokens(ExpressionEvaluatorParser.DATE_OPERATIONS); }
		public TerminalNode DATE_OPERATIONS(int i) {
			return getToken(ExpressionEvaluatorParser.DATE_OPERATIONS, i);
		}
		public List<TerminalNode> TIME_OPERATIONS() { return getTokens(ExpressionEvaluatorParser.TIME_OPERATIONS); }
		public TerminalNode TIME_OPERATIONS(int i) {
			return getToken(ExpressionEvaluatorParser.TIME_OPERATIONS, i);
		}
		public DateTimeFunctionContext(DateTimeOperationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeParenthesisContext extends DateTimeOperationContext {
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public DateTimeOperationContext dateTimeOperation() {
			return getRuleContext(DateTimeOperationContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public DateTimeParenthesisContext(DateTimeOperationContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeParenthesis(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeParenthesis(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeParenthesis(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateTimeOperationContext dateTimeOperation() throws RecognitionException {
		DateTimeOperationContext _localctx = new DateTimeOperationContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_dateTimeOperation);
		int _la;
		try {
			int _alt;
			setState(262);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				_localctx = new DateTimeParenthesisContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(246);
				match(LPAREN);
				setState(247);
				dateTimeOperation();
				setState(248);
				match(RPAREN);
				}
				break;
			case IF:
			case NOW_DATETIME:
			case CACHE_FUNCTION_PREFIX:
			case IDENTIFIER:
			case DATETIME:
			case DATETIME_TYPE:
				_localctx = new DateTimeFunctionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(250);
				dateTimeEntity();
				setState(260);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
				case 1:
					{
					setState(251);
					_la = _input.LA(1);
					if ( !(_la==DATE_OPERATIONS || _la==TIME_OPERATIONS) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(252);
					mathExpression(0);
					setState(257);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
					while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(253);
							_la = _input.LA(1);
							if ( !(_la==DATE_OPERATIONS || _la==TIME_OPERATIONS) ) {
							_errHandler.recoverInline(this);
							}
							else {
								if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
								_errHandler.reportMatch(this);
								consume();
							}
							setState(254);
							mathExpression(0);
							}
							} 
						}
						setState(259);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
					}
					}
					break;
				}
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
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public TerminalNode CACHE_FUNCTION_PREFIX() { return getToken(ExpressionEvaluatorParser.CACHE_FUNCTION_PREFIX, 0); }
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
		public FunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionContext function() throws RecognitionException {
		FunctionContext _localctx = new FunctionContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_function);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(265);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CACHE_FUNCTION_PREFIX) {
				{
				setState(264);
				match(CACHE_FUNCTION_PREFIX);
				}
			}

			setState(267);
			match(IDENTIFIER);
			setState(268);
			match(LPAREN);
			setState(279);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 65297830903296002L) != 0) || ((((_la - 74)) & ~0x3f) == 0 && ((1L << (_la - 74)) & 133921797L) != 0)) {
				{
				{
				setState(269);
				allEntityTypes();
				setState(274);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA || _la==SEMI) {
					{
					{
					setState(270);
					_la = _input.LA(1);
					if ( !(_la==COMMA || _la==SEMI) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(271);
					allEntityTypes();
					}
					}
					setState(276);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				}
				setState(281);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(282);
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
	public static class ComparisonOperatorContext extends ParserRuleContext {
		public TerminalNode GT() { return getToken(ExpressionEvaluatorParser.GT, 0); }
		public TerminalNode GE() { return getToken(ExpressionEvaluatorParser.GE, 0); }
		public TerminalNode LT() { return getToken(ExpressionEvaluatorParser.LT, 0); }
		public TerminalNode LE() { return getToken(ExpressionEvaluatorParser.LE, 0); }
		public TerminalNode EQ() { return getToken(ExpressionEvaluatorParser.EQ, 0); }
		public TerminalNode NEQ() { return getToken(ExpressionEvaluatorParser.NEQ, 0); }
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterComparisonOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitComparisonOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitComparisonOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(284);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 4329327034368L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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
	public static class LogicalOperatorContext extends ParserRuleContext {
		public TerminalNode AND() { return getToken(ExpressionEvaluatorParser.AND, 0); }
		public TerminalNode OR() { return getToken(ExpressionEvaluatorParser.OR, 0); }
		public TerminalNode NAND() { return getToken(ExpressionEvaluatorParser.NAND, 0); }
		public TerminalNode NOR() { return getToken(ExpressionEvaluatorParser.NOR, 0); }
		public TerminalNode XOR() { return getToken(ExpressionEvaluatorParser.XOR, 0); }
		public TerminalNode XNOR() { return getToken(ExpressionEvaluatorParser.XNOR, 0); }
		public LogicalOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalOperatorContext logicalOperator() throws RecognitionException {
		LogicalOperatorContext _localctx = new LogicalOperatorContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_logicalOperator);
		int _la;
		try {
			setState(289);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AND:
				enterOuterAlt(_localctx, 1);
				{
				setState(286);
				match(AND);
				}
				break;
			case OR:
				enterOuterAlt(_localctx, 2);
				{
				setState(287);
				match(OR);
				}
				break;
			case XOR:
			case XNOR:
			case NAND:
			case NOR:
				enterOuterAlt(_localctx, 3);
				{
				setState(288);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 3840L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
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
		public MathExpressionContext mathExpression() {
			return getRuleContext(MathExpressionContext.class,0);
		}
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public DateOperationContext dateOperation() {
			return getRuleContext(DateOperationContext.class,0);
		}
		public TimeOperationContext timeOperation() {
			return getRuleContext(TimeOperationContext.class,0);
		}
		public DateTimeOperationContext dateTimeOperation() {
			return getRuleContext(DateTimeOperationContext.class,0);
		}
		public StringEntityContext stringEntity() {
			return getRuleContext(StringEntityContext.class,0);
		}
		public VectorEntityContext vectorEntity() {
			return getRuleContext(VectorEntityContext.class,0);
		}
		public AllEntityTypesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_allEntityTypes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterAllEntityTypes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitAllEntityTypes(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitAllEntityTypes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AllEntityTypesContext allEntityTypes() throws RecognitionException {
		AllEntityTypesContext _localctx = new AllEntityTypesContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_allEntityTypes);
		try {
			setState(298);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(291);
				mathExpression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(292);
				logicalExpression(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(293);
				dateOperation();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(294);
				timeOperation();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(295);
				dateTimeOperation();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(296);
				stringEntity();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(297);
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
	public static class LogicalDecisionExpressionContext extends LogicalEntityContext {
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
		public TerminalNode ELSEIF() { return getToken(ExpressionEvaluatorParser.ELSEIF, 0); }
		public LogicalDecisionExpressionContext(LogicalEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalVariableContext extends LogicalEntityContext {
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public TerminalNode BOOLEAN_TYPE() { return getToken(ExpressionEvaluatorParser.BOOLEAN_TYPE, 0); }
		public LogicalVariableContext(LogicalEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalVariable(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalConstantContext extends LogicalEntityContext {
		public TerminalNode TRUE() { return getToken(ExpressionEvaluatorParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(ExpressionEvaluatorParser.FALSE, 0); }
		public LogicalConstantContext(LogicalEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalConstant(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalFunctionResultContext extends LogicalEntityContext {
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public TerminalNode BOOLEAN_TYPE() { return getToken(ExpressionEvaluatorParser.BOOLEAN_TYPE, 0); }
		public LogicalFunctionResultContext(LogicalEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalFunctionResult(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalFunctionResult(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalFunctionResult(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LogicalFunctionDecisionExpressionContext extends LogicalEntityContext {
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
		public LogicalFunctionDecisionExpressionContext(LogicalEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterLogicalFunctionDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitLogicalFunctionDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitLogicalFunctionDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalEntityContext logicalEntity() throws RecognitionException {
		LogicalEntityContext _localctx = new LogicalEntityContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_logicalEntity);
		int _la;
		try {
			int _alt;
			setState(343);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				_localctx = new LogicalConstantContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(300);
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
				_localctx = new LogicalDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(301);
				match(IF);
				setState(302);
				logicalExpression(0);
				setState(303);
				match(THEN);
				setState(304);
				logicalExpression(0);
				setState(310);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSEIF) {
					{
					setState(305);
					match(ELSEIF);
					setState(306);
					logicalExpression(0);
					setState(307);
					match(THEN);
					setState(308);
					logicalExpression(0);
					}
				}

				setState(312);
				match(ELSE);
				setState(313);
				logicalExpression(0);
				setState(314);
				match(ENDIF);
				}
				break;
			case 3:
				_localctx = new LogicalFunctionDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(316);
				match(IF);
				setState(317);
				match(LPAREN);
				setState(318);
				logicalExpression(0);
				setState(319);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(320);
				logicalExpression(0);
				setState(328);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(321);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(322);
						logicalExpression(0);
						setState(323);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(324);
						logicalExpression(0);
						}
						} 
					}
					setState(330);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
				}
				setState(331);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(332);
				logicalExpression(0);
				setState(333);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new LogicalFunctionResultContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(336);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==BOOLEAN_TYPE) {
					{
					setState(335);
					match(BOOLEAN_TYPE);
					}
				}

				setState(338);
				function();
				}
				break;
			case 5:
				_localctx = new LogicalVariableContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(340);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==BOOLEAN_TYPE) {
					{
					setState(339);
					match(BOOLEAN_TYPE);
					}
				}

				setState(342);
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
	public static class MathFunctionDecisionExpressionContext extends NumericEntityContext {
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
		public MathFunctionDecisionExpressionContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathFunctionDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathFunctionDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathFunctionDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumericConstantContext extends NumericEntityContext {
		public TerminalNode NUMBER() { return getToken(ExpressionEvaluatorParser.NUMBER, 0); }
		public NumericConstantContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNumericConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNumericConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNumericConstant(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumericFunctionResultContext extends NumericEntityContext {
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public TerminalNode NUMBER_TYPE() { return getToken(ExpressionEvaluatorParser.NUMBER_TYPE, 0); }
		public NumericFunctionResultContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNumericFunctionResult(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNumericFunctionResult(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNumericFunctionResult(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PiConstantContext extends NumericEntityContext {
		public TerminalNode PI() { return getToken(ExpressionEvaluatorParser.PI, 0); }
		public PiConstantContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterPiConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitPiConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitPiConstant(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumericVariableContext extends NumericEntityContext {
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public TerminalNode NEGATIVE_IDENTIFIER() { return getToken(ExpressionEvaluatorParser.NEGATIVE_IDENTIFIER, 0); }
		public TerminalNode NUMBER_TYPE() { return getToken(ExpressionEvaluatorParser.NUMBER_TYPE, 0); }
		public NumericVariableContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterNumericVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitNumericVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitNumericVariable(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MathDecisionExpressionContext extends NumericEntityContext {
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
		public TerminalNode ELSEIF() { return getToken(ExpressionEvaluatorParser.ELSEIF, 0); }
		public MathDecisionExpressionContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterMathDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitMathDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitMathDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SummationVariableContext extends NumericEntityContext {
		public TerminalNode SUMMATION_VARIABLE() { return getToken(ExpressionEvaluatorParser.SUMMATION_VARIABLE, 0); }
		public SummationVariableContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterSummationVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitSummationVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitSummationVariable(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ProductSequenceVariableContext extends NumericEntityContext {
		public TerminalNode PRODUCT_SEQUENCE_VARIABLE() { return getToken(ExpressionEvaluatorParser.PRODUCT_SEQUENCE_VARIABLE, 0); }
		public ProductSequenceVariableContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterProductSequenceVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitProductSequenceVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitProductSequenceVariable(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EulerConstantContext extends NumericEntityContext {
		public TerminalNode EULER() { return getToken(ExpressionEvaluatorParser.EULER, 0); }
		public EulerConstantContext(NumericEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterEulerConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitEulerConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitEulerConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericEntityContext numericEntity() throws RecognitionException {
		NumericEntityContext _localctx = new NumericEntityContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_numericEntity);
		int _la;
		try {
			int _alt;
			setState(395);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				_localctx = new MathDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(345);
				match(IF);
				setState(346);
				logicalExpression(0);
				setState(347);
				match(THEN);
				setState(348);
				mathExpression(0);
				setState(354);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSEIF) {
					{
					setState(349);
					match(ELSEIF);
					setState(350);
					logicalExpression(0);
					setState(351);
					match(THEN);
					setState(352);
					mathExpression(0);
					}
				}

				setState(356);
				match(ELSE);
				setState(357);
				mathExpression(0);
				setState(358);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new MathFunctionDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(360);
				match(IF);
				setState(361);
				match(LPAREN);
				setState(362);
				logicalExpression(0);
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
				mathExpression(0);
				setState(372);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(365);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(366);
						logicalExpression(0);
						setState(367);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(368);
						mathExpression(0);
						}
						} 
					}
					setState(374);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
				}
				setState(375);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(376);
				mathExpression(0);
				setState(377);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new EulerConstantContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(379);
				match(EULER);
				}
				break;
			case 4:
				_localctx = new PiConstantContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(380);
				match(PI);
				}
				break;
			case 5:
				_localctx = new SummationVariableContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(381);
				match(SUMMATION_VARIABLE);
				}
				break;
			case 6:
				_localctx = new ProductSequenceVariableContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(382);
				match(PRODUCT_SEQUENCE_VARIABLE);
				}
				break;
			case 7:
				_localctx = new NumericConstantContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(383);
				match(NUMBER);
				}
				break;
			case 8:
				_localctx = new NumericFunctionResultContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(385);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NUMBER_TYPE) {
					{
					setState(384);
					match(NUMBER_TYPE);
					}
				}

				setState(387);
				function();
				}
				break;
			case 9:
				_localctx = new NumericVariableContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(393);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case IDENTIFIER:
				case NUMBER_TYPE:
					{
					setState(389);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==NUMBER_TYPE) {
						{
						setState(388);
						match(NUMBER_TYPE);
						}
					}

					setState(391);
					match(IDENTIFIER);
					}
					break;
				case NEGATIVE_IDENTIFIER:
					{
					setState(392);
					match(NEGATIVE_IDENTIFIER);
					}
					break;
				default:
					throw new NoViableAltException(this);
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
	public static class StringVariableContext extends StringEntityContext {
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public TerminalNode STRING_TYPE() { return getToken(ExpressionEvaluatorParser.STRING_TYPE, 0); }
		public StringVariableContext(StringEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringVariable(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringConstantContext extends StringEntityContext {
		public TerminalNode STRING() { return getToken(ExpressionEvaluatorParser.STRING, 0); }
		public StringConstantContext(StringEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringConstant(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringDecisionExpressionContext extends StringEntityContext {
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
		public List<StringEntityContext> stringEntity() {
			return getRuleContexts(StringEntityContext.class);
		}
		public StringEntityContext stringEntity(int i) {
			return getRuleContext(StringEntityContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorParser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorParser.ENDIF, 0); }
		public TerminalNode ELSEIF() { return getToken(ExpressionEvaluatorParser.ELSEIF, 0); }
		public StringDecisionExpressionContext(StringEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringFunctionDecisionExpressionContext extends StringEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
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
		public TerminalNode RPAREN() { return getToken(ExpressionEvaluatorParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(ExpressionEvaluatorParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(ExpressionEvaluatorParser.SEMI, i);
		}
		public StringFunctionDecisionExpressionContext(StringEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringFunctionDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringFunctionDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringFunctionDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringFunctionResultContext extends StringEntityContext {
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public TerminalNode STRING_TYPE() { return getToken(ExpressionEvaluatorParser.STRING_TYPE, 0); }
		public StringFunctionResultContext(StringEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterStringFunctionResult(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitStringFunctionResult(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitStringFunctionResult(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringEntityContext stringEntity() throws RecognitionException {
		StringEntityContext _localctx = new StringEntityContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_stringEntity);
		int _la;
		try {
			int _alt;
			setState(440);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
			case 1:
				_localctx = new StringDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(397);
				match(IF);
				setState(398);
				logicalExpression(0);
				setState(399);
				match(THEN);
				setState(400);
				stringEntity();
				setState(406);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSEIF) {
					{
					setState(401);
					match(ELSEIF);
					setState(402);
					logicalExpression(0);
					setState(403);
					match(THEN);
					setState(404);
					stringEntity();
					}
				}

				setState(408);
				match(ELSE);
				setState(409);
				stringEntity();
				setState(410);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new StringFunctionDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(412);
				match(IF);
				setState(413);
				match(LPAREN);
				setState(414);
				logicalExpression(0);
				setState(415);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(416);
				stringEntity();
				setState(424);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(417);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(418);
						logicalExpression(0);
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
						stringEntity();
						}
						} 
					}
					setState(426);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
				}
				setState(427);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(428);
				stringEntity();
				setState(429);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new StringConstantContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(431);
				match(STRING);
				}
				break;
			case 4:
				_localctx = new StringFunctionResultContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(433);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING_TYPE) {
					{
					setState(432);
					match(STRING_TYPE);
					}
				}

				setState(435);
				function();
				}
				break;
			case 5:
				_localctx = new StringVariableContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(437);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING_TYPE) {
					{
					setState(436);
					match(STRING_TYPE);
					}
				}

				setState(439);
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
	public static class DateDecisionExpressionContext extends DateEntityContext {
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
		public List<DateOperationContext> dateOperation() {
			return getRuleContexts(DateOperationContext.class);
		}
		public DateOperationContext dateOperation(int i) {
			return getRuleContext(DateOperationContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorParser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorParser.ENDIF, 0); }
		public TerminalNode ELSEIF() { return getToken(ExpressionEvaluatorParser.ELSEIF, 0); }
		public DateDecisionExpressionContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateCurrentValueContext extends DateEntityContext {
		public TerminalNode NOW_DATE() { return getToken(ExpressionEvaluatorParser.NOW_DATE, 0); }
		public DateCurrentValueContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateCurrentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateCurrentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateCurrentValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateConstantContext extends DateEntityContext {
		public TerminalNode DATE() { return getToken(ExpressionEvaluatorParser.DATE, 0); }
		public DateConstantContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateConstant(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateVariableContext extends DateEntityContext {
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public TerminalNode DATE_TYPE() { return getToken(ExpressionEvaluatorParser.DATE_TYPE, 0); }
		public DateVariableContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateVariable(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateFunctionResultContext extends DateEntityContext {
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public TerminalNode DATE_TYPE() { return getToken(ExpressionEvaluatorParser.DATE_TYPE, 0); }
		public DateFunctionResultContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateFunctionResult(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateFunctionResult(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateFunctionResult(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateFunctionDecisionExpressionContext extends DateEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<DateOperationContext> dateOperation() {
			return getRuleContexts(DateOperationContext.class);
		}
		public DateOperationContext dateOperation(int i) {
			return getRuleContext(DateOperationContext.class,i);
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
		public DateFunctionDecisionExpressionContext(DateEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateFunctionDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateFunctionDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateFunctionDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateEntityContext dateEntity() throws RecognitionException {
		DateEntityContext _localctx = new DateEntityContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_dateEntity);
		int _la;
		try {
			int _alt;
			setState(486);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				_localctx = new DateDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(442);
				match(IF);
				setState(443);
				logicalExpression(0);
				setState(444);
				match(THEN);
				setState(445);
				dateOperation();
				setState(451);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSEIF) {
					{
					setState(446);
					match(ELSEIF);
					setState(447);
					logicalExpression(0);
					setState(448);
					match(THEN);
					setState(449);
					dateOperation();
					}
				}

				setState(453);
				match(ELSE);
				setState(454);
				dateOperation();
				setState(455);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new DateFunctionDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(457);
				match(IF);
				setState(458);
				match(LPAREN);
				setState(459);
				logicalExpression(0);
				setState(460);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(461);
				dateOperation();
				setState(469);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,45,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(462);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(463);
						logicalExpression(0);
						setState(464);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(465);
						dateOperation();
						}
						} 
					}
					setState(471);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,45,_ctx);
				}
				setState(472);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(473);
				dateOperation();
				setState(474);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new DateConstantContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(476);
				match(DATE);
				}
				break;
			case 4:
				_localctx = new DateCurrentValueContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(477);
				match(NOW_DATE);
				}
				break;
			case 5:
				_localctx = new DateVariableContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(479);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DATE_TYPE) {
					{
					setState(478);
					match(DATE_TYPE);
					}
				}

				setState(481);
				match(IDENTIFIER);
				}
				break;
			case 6:
				_localctx = new DateFunctionResultContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(483);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DATE_TYPE) {
					{
					setState(482);
					match(DATE_TYPE);
					}
				}

				setState(485);
				function();
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
	public static class TimeConstantContext extends TimeEntityContext {
		public TerminalNode TIME() { return getToken(ExpressionEvaluatorParser.TIME, 0); }
		public TimeConstantContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeConstant(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeVariableContext extends TimeEntityContext {
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public TerminalNode TIME_TYPE() { return getToken(ExpressionEvaluatorParser.TIME_TYPE, 0); }
		public TimeVariableContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeVariable(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeFunctionResultContext extends TimeEntityContext {
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public TerminalNode TIME_TYPE() { return getToken(ExpressionEvaluatorParser.TIME_TYPE, 0); }
		public TimeFunctionResultContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeFunctionResult(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeFunctionResult(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeFunctionResult(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeFunctionDecisionExpressionContext extends TimeEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<TimeOperationContext> timeOperation() {
			return getRuleContexts(TimeOperationContext.class);
		}
		public TimeOperationContext timeOperation(int i) {
			return getRuleContext(TimeOperationContext.class,i);
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
		public TimeFunctionDecisionExpressionContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeFunctionDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeFunctionDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeFunctionDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeDecisionExpressionContext extends TimeEntityContext {
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
		public List<TimeOperationContext> timeOperation() {
			return getRuleContexts(TimeOperationContext.class);
		}
		public TimeOperationContext timeOperation(int i) {
			return getRuleContext(TimeOperationContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorParser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorParser.ENDIF, 0); }
		public TerminalNode ELSEIF() { return getToken(ExpressionEvaluatorParser.ELSEIF, 0); }
		public TimeDecisionExpressionContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeCurrentValueContext extends TimeEntityContext {
		public TerminalNode NOW_TIME() { return getToken(ExpressionEvaluatorParser.NOW_TIME, 0); }
		public TimeCurrentValueContext(TimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterTimeCurrentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitTimeCurrentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitTimeCurrentValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TimeEntityContext timeEntity() throws RecognitionException {
		TimeEntityContext _localctx = new TimeEntityContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_timeEntity);
		int _la;
		try {
			int _alt;
			setState(532);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,53,_ctx) ) {
			case 1:
				_localctx = new TimeDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(488);
				match(IF);
				setState(489);
				logicalExpression(0);
				setState(490);
				match(THEN);
				setState(491);
				timeOperation();
				setState(497);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSEIF) {
					{
					setState(492);
					match(ELSEIF);
					setState(493);
					logicalExpression(0);
					setState(494);
					match(THEN);
					setState(495);
					timeOperation();
					}
				}

				setState(499);
				match(ELSE);
				setState(500);
				timeOperation();
				setState(501);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new TimeFunctionDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(503);
				match(IF);
				setState(504);
				match(LPAREN);
				setState(505);
				logicalExpression(0);
				setState(506);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(507);
				timeOperation();
				setState(515);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,50,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(508);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(509);
						logicalExpression(0);
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
						timeOperation();
						}
						} 
					}
					setState(517);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,50,_ctx);
				}
				setState(518);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(519);
				timeOperation();
				setState(520);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new TimeConstantContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(522);
				match(TIME);
				}
				break;
			case 4:
				_localctx = new TimeCurrentValueContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(523);
				match(NOW_TIME);
				}
				break;
			case 5:
				_localctx = new TimeVariableContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(525);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TIME_TYPE) {
					{
					setState(524);
					match(TIME_TYPE);
					}
				}

				setState(527);
				match(IDENTIFIER);
				}
				break;
			case 6:
				_localctx = new TimeFunctionResultContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(529);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TIME_TYPE) {
					{
					setState(528);
					match(TIME_TYPE);
					}
				}

				setState(531);
				function();
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
	public static class DateTimeCurrentValueContext extends DateTimeEntityContext {
		public TerminalNode NOW_DATETIME() { return getToken(ExpressionEvaluatorParser.NOW_DATETIME, 0); }
		public DateTimeCurrentValueContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeCurrentValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeCurrentValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeCurrentValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeConstantContext extends DateTimeEntityContext {
		public TerminalNode DATETIME() { return getToken(ExpressionEvaluatorParser.DATETIME, 0); }
		public TerminalNode TIME_OFFSET() { return getToken(ExpressionEvaluatorParser.TIME_OFFSET, 0); }
		public DateTimeConstantContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeConstant(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeDecisionExpressionContext extends DateTimeEntityContext {
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
		public List<DateTimeOperationContext> dateTimeOperation() {
			return getRuleContexts(DateTimeOperationContext.class);
		}
		public DateTimeOperationContext dateTimeOperation(int i) {
			return getRuleContext(DateTimeOperationContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(ExpressionEvaluatorParser.ELSE, 0); }
		public TerminalNode ENDIF() { return getToken(ExpressionEvaluatorParser.ENDIF, 0); }
		public TerminalNode ELSEIF() { return getToken(ExpressionEvaluatorParser.ELSEIF, 0); }
		public DateTimeDecisionExpressionContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeVariableContext extends DateTimeEntityContext {
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public TerminalNode DATETIME_TYPE() { return getToken(ExpressionEvaluatorParser.DATETIME_TYPE, 0); }
		public DateTimeVariableContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeVariable(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeFunctionResultContext extends DateTimeEntityContext {
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public TerminalNode DATETIME_TYPE() { return getToken(ExpressionEvaluatorParser.DATETIME_TYPE, 0); }
		public DateTimeFunctionResultContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeFunctionResult(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeFunctionResult(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeFunctionResult(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeFunctionDecisionExpressionContext extends DateTimeEntityContext {
		public TerminalNode IF() { return getToken(ExpressionEvaluatorParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ExpressionEvaluatorParser.LPAREN, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public List<DateTimeOperationContext> dateTimeOperation() {
			return getRuleContexts(DateTimeOperationContext.class);
		}
		public DateTimeOperationContext dateTimeOperation(int i) {
			return getRuleContext(DateTimeOperationContext.class,i);
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
		public DateTimeFunctionDecisionExpressionContext(DateTimeEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterDateTimeFunctionDecisionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitDateTimeFunctionDecisionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitDateTimeFunctionDecisionExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateTimeEntityContext dateTimeEntity() throws RecognitionException {
		DateTimeEntityContext _localctx = new DateTimeEntityContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_dateTimeEntity);
		int _la;
		try {
			int _alt;
			setState(581);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
			case 1:
				_localctx = new DateTimeDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(534);
				match(IF);
				setState(535);
				logicalExpression(0);
				setState(536);
				match(THEN);
				setState(537);
				dateTimeOperation();
				setState(543);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSEIF) {
					{
					setState(538);
					match(ELSEIF);
					setState(539);
					logicalExpression(0);
					setState(540);
					match(THEN);
					setState(541);
					dateTimeOperation();
					}
				}

				setState(545);
				match(ELSE);
				setState(546);
				dateTimeOperation();
				setState(547);
				match(ENDIF);
				}
				break;
			case 2:
				_localctx = new DateTimeFunctionDecisionExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(549);
				match(IF);
				setState(550);
				match(LPAREN);
				setState(551);
				logicalExpression(0);
				setState(552);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(553);
				dateTimeOperation();
				setState(561);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,55,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(554);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(555);
						logicalExpression(0);
						setState(556);
						_la = _input.LA(1);
						if ( !(_la==COMMA || _la==SEMI) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(557);
						dateTimeOperation();
						}
						} 
					}
					setState(563);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,55,_ctx);
				}
				setState(564);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(565);
				dateTimeOperation();
				setState(566);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new DateTimeConstantContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(568);
				match(DATETIME);
				setState(570);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
				case 1:
					{
					setState(569);
					match(TIME_OFFSET);
					}
					break;
				}
				}
				break;
			case 4:
				_localctx = new DateTimeCurrentValueContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(572);
				match(NOW_DATETIME);
				}
				break;
			case 5:
				_localctx = new DateTimeVariableContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(574);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DATETIME_TYPE) {
					{
					setState(573);
					match(DATETIME_TYPE);
					}
				}

				setState(576);
				match(IDENTIFIER);
				}
				break;
			case 6:
				_localctx = new DateTimeFunctionResultContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(578);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DATETIME_TYPE) {
					{
					setState(577);
					match(DATETIME_TYPE);
					}
				}

				setState(580);
				function();
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
	public static class VectorOfEntitiesContext extends VectorEntityContext {
		public TerminalNode LBLACKET() { return getToken(ExpressionEvaluatorParser.LBLACKET, 0); }
		public List<AllEntityTypesContext> allEntityTypes() {
			return getRuleContexts(AllEntityTypesContext.class);
		}
		public AllEntityTypesContext allEntityTypes(int i) {
			return getRuleContext(AllEntityTypesContext.class,i);
		}
		public TerminalNode RBLACKET() { return getToken(ExpressionEvaluatorParser.RBLACKET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public VectorOfEntitiesContext(VectorEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterVectorOfEntities(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitVectorOfEntities(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitVectorOfEntities(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VectorVariableContext extends VectorEntityContext {
		public TerminalNode IDENTIFIER() { return getToken(ExpressionEvaluatorParser.IDENTIFIER, 0); }
		public TerminalNode VECTOR_TYPE() { return getToken(ExpressionEvaluatorParser.VECTOR_TYPE, 0); }
		public VectorVariableContext(VectorEntityContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterVectorVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitVectorVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitVectorVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VectorEntityContext vectorEntity() throws RecognitionException {
		VectorEntityContext _localctx = new VectorEntityContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_vectorEntity);
		int _la;
		try {
			setState(598);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBLACKET:
				_localctx = new VectorOfEntitiesContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(583);
				match(LBLACKET);
				setState(584);
				allEntityTypes();
				setState(589);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(585);
					match(COMMA);
					setState(586);
					allEntityTypes();
					}
					}
					setState(591);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(592);
				match(RBLACKET);
				}
				break;
			case IDENTIFIER:
			case VECTOR_TYPE:
				_localctx = new VectorVariableContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(595);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==VECTOR_TYPE) {
					{
					setState(594);
					match(VECTOR_TYPE);
					}
				}

				setState(597);
				match(IDENTIFIER);
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
	public static class VectorOfVariablesContext extends ParserRuleContext {
		public TerminalNode LBLACKET() { return getToken(ExpressionEvaluatorParser.LBLACKET, 0); }
		public List<TerminalNode> IDENTIFIER() { return getTokens(ExpressionEvaluatorParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(ExpressionEvaluatorParser.IDENTIFIER, i);
		}
		public TerminalNode RBLACKET() { return getToken(ExpressionEvaluatorParser.RBLACKET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ExpressionEvaluatorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ExpressionEvaluatorParser.COMMA, i);
		}
		public VectorOfVariablesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vectorOfVariables; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).enterVectorOfVariables(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionEvaluatorListener ) ((ExpressionEvaluatorListener)listener).exitVectorOfVariables(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionEvaluatorVisitor ) return ((ExpressionEvaluatorVisitor<? extends T>)visitor).visitVectorOfVariables(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VectorOfVariablesContext vectorOfVariables() throws RecognitionException {
		VectorOfVariablesContext _localctx = new VectorOfVariablesContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_vectorOfVariables);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(600);
			match(LBLACKET);
			setState(601);
			match(IDENTIFIER);
			setState(606);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(602);
				match(COMMA);
				setState(603);
				match(IDENTIFIER);
				}
				}
				setState(608);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(609);
			match(RBLACKET);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 4:
			return logicalExpression_sempred((LogicalExpressionContext)_localctx, predIndex);
		case 5:
			return mathExpression_sempred((MathExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean logicalExpression_sempred(LogicalExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 9);
		case 1:
			return precpred(_ctx, 8);
		}
		return true;
	}
	private boolean mathExpression_sempred(MathExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 9);
		case 3:
			return precpred(_ctx, 7);
		case 4:
			return precpred(_ctx, 6);
		case 5:
			return precpred(_ctx, 5);
		case 6:
			return precpred(_ctx, 11);
		case 7:
			return precpred(_ctx, 10);
		case 8:
			return precpred(_ctx, 3);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001f\u0264\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0001\u0000\u0001\u0000\u0003\u00005\b\u0000\u0001\u0001\u0005\u0001"+
		"8\b\u0001\n\u0001\f\u0001;\t\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0002\u0005\u0002A\b\u0002\n\u0002\f\u0002D\t\u0002\u0001\u0002"+
		"\u0003\u0002G\b\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0003\u0003T\b\u0003\u0001\u0003\u0001\u0003\u0003\u0003"+
		"X\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004"+
		"v\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0005\u0004\u0080\b\u0004\n\u0004"+
		"\f\u0004\u0083\t\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u0099\b\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0005\u0005\u00ad\b\u0005\n\u0005\f\u0005\u00b0\t\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0003\u0006\u00b5\b\u0006\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u00c3\b\u0007\u0001"+
		"\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0005\n\u00dc\b\n\n\n\f\n\u00df\t\n"+
		"\u0003\n\u00e1\b\n\u0003\n\u00e3\b\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0005\u000b\u00ee\b\u000b\n\u000b\f\u000b\u00f1\t\u000b\u0003\u000b\u00f3"+
		"\b\u000b\u0003\u000b\u00f5\b\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0005\f\u0100\b\f\n\f\f\f\u0103\t\f"+
		"\u0003\f\u0105\b\f\u0003\f\u0107\b\f\u0001\r\u0003\r\u010a\b\r\u0001\r"+
		"\u0001\r\u0001\r\u0001\r\u0001\r\u0005\r\u0111\b\r\n\r\f\r\u0114\t\r\u0005"+
		"\r\u0116\b\r\n\r\f\r\u0119\t\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0122\b\u000f\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0003\u0010\u012b\b\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0003\u0011\u0137\b\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u0147\b\u0011"+
		"\n\u0011\f\u0011\u014a\t\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0011\u0003\u0011\u0151\b\u0011\u0001\u0011\u0001\u0011\u0003"+
		"\u0011\u0155\b\u0011\u0001\u0011\u0003\u0011\u0158\b\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0003\u0012\u0163\b\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0005\u0012\u0173\b\u0012\n\u0012\f\u0012\u0176\t\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u0182\b\u0012\u0001\u0012\u0001"+
		"\u0012\u0003\u0012\u0186\b\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u018a"+
		"\b\u0012\u0003\u0012\u018c\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0003\u0013\u0197\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u01a7\b\u0013"+
		"\n\u0013\f\u0013\u01aa\t\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u01b2\b\u0013\u0001\u0013\u0001"+
		"\u0013\u0003\u0013\u01b6\b\u0013\u0001\u0013\u0003\u0013\u01b9\b\u0013"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u01c4\b\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0005\u0014\u01d4\b\u0014\n\u0014\f\u0014\u01d7\t\u0014\u0001"+
		"\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0003\u0014\u01e0\b\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u01e4"+
		"\b\u0014\u0001\u0014\u0003\u0014\u01e7\b\u0014\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0003\u0015\u01f2\b\u0015\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005\u0015"+
		"\u0202\b\u0015\n\u0015\f\u0015\u0205\t\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u020e"+
		"\b\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u0212\b\u0015\u0001\u0015"+
		"\u0003\u0015\u0215\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016"+
		"\u0220\b\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016\u0230\b\u0016\n\u0016"+
		"\f\u0016\u0233\t\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0003\u0016\u023b\b\u0016\u0001\u0016\u0001\u0016"+
		"\u0003\u0016\u023f\b\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u0243\b"+
		"\u0016\u0001\u0016\u0003\u0016\u0246\b\u0016\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0005\u0017\u024c\b\u0017\n\u0017\f\u0017\u024f\t\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u0254\b\u0017\u0001\u0017"+
		"\u0003\u0017\u0257\b\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0005\u0018\u025d\b\u0018\n\u0018\f\u0018\u0260\t\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0000\u0002\b\n\u0019\u0000\u0002\u0004\u0006\b\n\f"+
		"\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.0\u0000"+
		"\f\u0002\u0000((OO\u0002\u0000\u0014\u0014**\u0002\u0000\u000e\u000f\u0012"+
		"\u0012\u0001\u0000\u0010\u0011\u0001\u0000\u0018\u001a\u0001\u0000+2\u0001"+
		"\u0000\u001c\u001d\u0001\u000034\u0001\u0000PQ\u0001\u0000$)\u0001\u0000"+
		"\b\u000b\u0001\u0000\f\r\u02b9\u00004\u0001\u0000\u0000\u0000\u00029\u0001"+
		"\u0000\u0000\u0000\u0004B\u0001\u0000\u0000\u0000\u0006W\u0001\u0000\u0000"+
		"\u0000\bu\u0001\u0000\u0000\u0000\n\u0098\u0001\u0000\u0000\u0000\f\u00b4"+
		"\u0001\u0000\u0000\u0000\u000e\u00c2\u0001\u0000\u0000\u0000\u0010\u00c4"+
		"\u0001\u0000\u0000\u0000\u0012\u00cb\u0001\u0000\u0000\u0000\u0014\u00e2"+
		"\u0001\u0000\u0000\u0000\u0016\u00f4\u0001\u0000\u0000\u0000\u0018\u0106"+
		"\u0001\u0000\u0000\u0000\u001a\u0109\u0001\u0000\u0000\u0000\u001c\u011c"+
		"\u0001\u0000\u0000\u0000\u001e\u0121\u0001\u0000\u0000\u0000 \u012a\u0001"+
		"\u0000\u0000\u0000\"\u0157\u0001\u0000\u0000\u0000$\u018b\u0001\u0000"+
		"\u0000\u0000&\u01b8\u0001\u0000\u0000\u0000(\u01e6\u0001\u0000\u0000\u0000"+
		"*\u0214\u0001\u0000\u0000\u0000,\u0245\u0001\u0000\u0000\u0000.\u0256"+
		"\u0001\u0000\u0000\u00000\u0258\u0001\u0000\u0000\u000025\u0003\u0002"+
		"\u0001\u000035\u0003\u0004\u0002\u000042\u0001\u0000\u0000\u000043\u0001"+
		"\u0000\u0000\u00005\u0001\u0001\u0000\u0000\u000068\u0003\u0006\u0003"+
		"\u000076\u0001\u0000\u0000\u00008;\u0001\u0000\u0000\u000097\u0001\u0000"+
		"\u0000\u00009:\u0001\u0000\u0000\u0000:<\u0001\u0000\u0000\u0000;9\u0001"+
		"\u0000\u0000\u0000<=\u0003\n\u0005\u0000=>\u0005\u0000\u0000\u0001>\u0003"+
		"\u0001\u0000\u0000\u0000?A\u0003\u0006\u0003\u0000@?\u0001\u0000\u0000"+
		"\u0000AD\u0001\u0000\u0000\u0000B@\u0001\u0000\u0000\u0000BC\u0001\u0000"+
		"\u0000\u0000CF\u0001\u0000\u0000\u0000DB\u0001\u0000\u0000\u0000EG\u0003"+
		"\b\u0004\u0000FE\u0001\u0000\u0000\u0000FG\u0001\u0000\u0000\u0000GH\u0001"+
		"\u0000\u0000\u0000HI\u0005\u0000\u0000\u0001I\u0005\u0001\u0000\u0000"+
		"\u0000JK\u0005U\u0000\u0000KL\u0007\u0000\u0000\u0000LM\u0003 \u0010\u0000"+
		"MN\u0005Q\u0000\u0000NX\u0001\u0000\u0000\u0000OP\u00030\u0018\u0000P"+
		"S\u0007\u0000\u0000\u0000QT\u0003.\u0017\u0000RT\u0003\u001a\r\u0000S"+
		"Q\u0001\u0000\u0000\u0000SR\u0001\u0000\u0000\u0000TU\u0001\u0000\u0000"+
		"\u0000UV\u0005Q\u0000\u0000VX\u0001\u0000\u0000\u0000WJ\u0001\u0000\u0000"+
		"\u0000WO\u0001\u0000\u0000\u0000X\u0007\u0001\u0000\u0000\u0000YZ\u0006"+
		"\u0004\uffff\uffff\u0000Z[\u0007\u0001\u0000\u0000[v\u0003\b\u0004\n\\"+
		"]\u0003\n\u0005\u0000]^\u0003\u001c\u000e\u0000^_\u0003\n\u0005\u0000"+
		"_v\u0001\u0000\u0000\u0000`a\u0003&\u0013\u0000ab\u0003\u001c\u000e\u0000"+
		"bc\u0003&\u0013\u0000cv\u0001\u0000\u0000\u0000de\u0003\u0014\n\u0000"+
		"ef\u0003\u001c\u000e\u0000fg\u0003\u0014\n\u0000gv\u0001\u0000\u0000\u0000"+
		"hi\u0003\u0016\u000b\u0000ij\u0003\u001c\u000e\u0000jk\u0003\u0016\u000b"+
		"\u0000kv\u0001\u0000\u0000\u0000lm\u0003\u0018\f\u0000mn\u0003\u001c\u000e"+
		"\u0000no\u0003\u0018\f\u0000ov\u0001\u0000\u0000\u0000pq\u0005J\u0000"+
		"\u0000qr\u0003\b\u0004\u0000rs\u0005K\u0000\u0000sv\u0001\u0000\u0000"+
		"\u0000tv\u0003\"\u0011\u0000uY\u0001\u0000\u0000\u0000u\\\u0001\u0000"+
		"\u0000\u0000u`\u0001\u0000\u0000\u0000ud\u0001\u0000\u0000\u0000uh\u0001"+
		"\u0000\u0000\u0000ul\u0001\u0000\u0000\u0000up\u0001\u0000\u0000\u0000"+
		"ut\u0001\u0000\u0000\u0000v\u0081\u0001\u0000\u0000\u0000wx\n\t\u0000"+
		"\u0000xy\u0003\u001e\u000f\u0000yz\u0003\b\u0004\nz\u0080\u0001\u0000"+
		"\u0000\u0000{|\n\b\u0000\u0000|}\u0003\u001c\u000e\u0000}~\u0003\b\u0004"+
		"\t~\u0080\u0001\u0000\u0000\u0000\u007fw\u0001\u0000\u0000\u0000\u007f"+
		"{\u0001\u0000\u0000\u0000\u0080\u0083\u0001\u0000\u0000\u0000\u0081\u007f"+
		"\u0001\u0000\u0000\u0000\u0081\u0082\u0001\u0000\u0000\u0000\u0082\t\u0001"+
		"\u0000\u0000\u0000\u0083\u0081\u0001\u0000\u0000\u0000\u0084\u0085\u0006"+
		"\u0005\uffff\uffff\u0000\u0085\u0086\u0005J\u0000\u0000\u0086\u0087\u0003"+
		"\n\u0005\u0000\u0087\u0088\u0005K\u0000\u0000\u0088\u0099\u0001\u0000"+
		"\u0000\u0000\u0089\u008a\u0005 \u0000\u0000\u008a\u008b\u0003\n\u0005"+
		"\u0000\u008b\u008c\u0005K\u0000\u0000\u008c\u0099\u0001\u0000\u0000\u0000"+
		"\u008d\u008e\u0005\u0017\u0000\u0000\u008e\u008f\u0005J\u0000\u0000\u008f"+
		"\u0090\u0003\n\u0005\u0000\u0090\u0091\u0005K\u0000\u0000\u0091\u0099"+
		"\u0001\u0000\u0000\u0000\u0092\u0093\u0005\u0013\u0000\u0000\u0093\u0094"+
		"\u0003\n\u0005\u0000\u0094\u0095\u0005\u0013\u0000\u0000\u0095\u0099\u0001"+
		"\u0000\u0000\u0000\u0096\u0099\u0003\f\u0006\u0000\u0097\u0099\u0003$"+
		"\u0012\u0000\u0098\u0084\u0001\u0000\u0000\u0000\u0098\u0089\u0001\u0000"+
		"\u0000\u0000\u0098\u008d\u0001\u0000\u0000\u0000\u0098\u0092\u0001\u0000"+
		"\u0000\u0000\u0098\u0096\u0001\u0000\u0000\u0000\u0098\u0097\u0001\u0000"+
		"\u0000\u0000\u0099\u00ae\u0001\u0000\u0000\u0000\u009a\u009b\n\t\u0000"+
		"\u0000\u009b\u009c\u0005\u0016\u0000\u0000\u009c\u00ad\u0003\n\u0005\n"+
		"\u009d\u009e\n\u0007\u0000\u0000\u009e\u009f\u0005\u0015\u0000\u0000\u009f"+
		"\u00ad\u0003\n\u0005\u0007\u00a0\u00a1\n\u0006\u0000\u0000\u00a1\u00a2"+
		"\u0007\u0002\u0000\u0000\u00a2\u00ad\u0003\n\u0005\u0007\u00a3\u00a4\n"+
		"\u0005\u0000\u0000\u00a4\u00a5\u0007\u0003\u0000\u0000\u00a5\u00ad\u0003"+
		"\n\u0005\u0006\u00a6\u00a7\n\u000b\u0000\u0000\u00a7\u00ad\u0005\u0012"+
		"\u0000\u0000\u00a8\u00a9\n\n\u0000\u0000\u00a9\u00ad\u0005\u0014\u0000"+
		"\u0000\u00aa\u00ab\n\u0003\u0000\u0000\u00ab\u00ad\u0005#\u0000\u0000"+
		"\u00ac\u009a\u0001\u0000\u0000\u0000\u00ac\u009d\u0001\u0000\u0000\u0000"+
		"\u00ac\u00a0\u0001\u0000\u0000\u0000\u00ac\u00a3\u0001\u0000\u0000\u0000"+
		"\u00ac\u00a6\u0001\u0000\u0000\u0000\u00ac\u00a8\u0001\u0000\u0000\u0000"+
		"\u00ac\u00aa\u0001\u0000\u0000\u0000\u00ad\u00b0\u0001\u0000\u0000\u0000"+
		"\u00ae\u00ac\u0001\u0000\u0000\u0000\u00ae\u00af\u0001\u0000\u0000\u0000"+
		"\u00af\u000b\u0001\u0000\u0000\u0000\u00b0\u00ae\u0001\u0000\u0000\u0000"+
		"\u00b1\u00b5\u0003\u000e\u0007\u0000\u00b2\u00b5\u0003\u0010\b\u0000\u00b3"+
		"\u00b5\u0003\u0012\t\u0000\u00b4\u00b1\u0001\u0000\u0000\u0000\u00b4\u00b2"+
		"\u0001\u0000\u0000\u0000\u00b4\u00b3\u0001\u0000\u0000\u0000\u00b5\r\u0001"+
		"\u0000\u0000\u0000\u00b6\u00b7\u0007\u0004\u0000\u0000\u00b7\u00b8\u0005"+
		"J\u0000\u0000\u00b8\u00b9\u0003\n\u0005\u0000\u00b9\u00ba\u0005K\u0000"+
		"\u0000\u00ba\u00c3\u0001\u0000\u0000\u0000\u00bb\u00bc\u0005\u001b\u0000"+
		"\u0000\u00bc\u00bd\u0005J\u0000\u0000\u00bd\u00be\u0003\n\u0005\u0000"+
		"\u00be\u00bf\u0005P\u0000\u0000\u00bf\u00c0\u0003\n\u0005\u0000\u00c0"+
		"\u00c1\u0005K\u0000\u0000\u00c1\u00c3\u0001\u0000\u0000\u0000\u00c2\u00b6"+
		"\u0001\u0000\u0000\u0000\u00c2\u00bb\u0001\u0000\u0000\u0000\u00c3\u000f"+
		"\u0001\u0000\u0000\u0000\u00c4\u00c5\u0007\u0005\u0000\u0000\u00c5\u00c6"+
		"\u0005J\u0000\u0000\u00c6\u00c7\u0003\n\u0005\u0000\u00c7\u00c8\u0005"+
		"P\u0000\u0000\u00c8\u00c9\u0003\n\u0005\u0000\u00c9\u00ca\u0005K\u0000"+
		"\u0000\u00ca\u0011\u0001\u0000\u0000\u0000\u00cb\u00cc\u0007\u0006\u0000"+
		"\u0000\u00cc\u00cd\u0003.\u0017\u0000\u00cd\u00ce\u0005M\u0000\u0000\u00ce"+
		"\u00cf\u0005J\u0000\u0000\u00cf\u00d0\u0003\n\u0005\u0000\u00d0\u00d1"+
		"\u0005K\u0000\u0000\u00d1\u0013\u0001\u0000\u0000\u0000\u00d2\u00d3\u0005"+
		"J\u0000\u0000\u00d3\u00d4\u0003\u0014\n\u0000\u00d4\u00d5\u0005K\u0000"+
		"\u0000\u00d5\u00e3\u0001\u0000\u0000\u0000\u00d6\u00e0\u0003(\u0014\u0000"+
		"\u00d7\u00d8\u00053\u0000\u0000\u00d8\u00dd\u0003\n\u0005\u0000\u00d9"+
		"\u00da\u00053\u0000\u0000\u00da\u00dc\u0003\n\u0005\u0000\u00db\u00d9"+
		"\u0001\u0000\u0000\u0000\u00dc\u00df\u0001\u0000\u0000\u0000\u00dd\u00db"+
		"\u0001\u0000\u0000\u0000\u00dd\u00de\u0001\u0000\u0000\u0000\u00de\u00e1"+
		"\u0001\u0000\u0000\u0000\u00df\u00dd\u0001\u0000\u0000\u0000\u00e0\u00d7"+
		"\u0001\u0000\u0000\u0000\u00e0\u00e1\u0001\u0000\u0000\u0000\u00e1\u00e3"+
		"\u0001\u0000\u0000\u0000\u00e2\u00d2\u0001\u0000\u0000\u0000\u00e2\u00d6"+
		"\u0001\u0000\u0000\u0000\u00e3\u0015\u0001\u0000\u0000\u0000\u00e4\u00e5"+
		"\u0005J\u0000\u0000\u00e5\u00e6\u0003\u0016\u000b\u0000\u00e6\u00e7\u0005"+
		"K\u0000\u0000\u00e7\u00f5\u0001\u0000\u0000\u0000\u00e8\u00f2\u0003*\u0015"+
		"\u0000\u00e9\u00ea\u00054\u0000\u0000\u00ea\u00ef\u0003\n\u0005\u0000"+
		"\u00eb\u00ec\u00054\u0000\u0000\u00ec\u00ee\u0003\n\u0005\u0000\u00ed"+
		"\u00eb\u0001\u0000\u0000\u0000\u00ee\u00f1\u0001\u0000\u0000\u0000\u00ef"+
		"\u00ed\u0001\u0000\u0000\u0000\u00ef\u00f0\u0001\u0000\u0000\u0000\u00f0"+
		"\u00f3\u0001\u0000\u0000\u0000\u00f1\u00ef\u0001\u0000\u0000\u0000\u00f2"+
		"\u00e9\u0001\u0000\u0000\u0000\u00f2\u00f3\u0001\u0000\u0000\u0000\u00f3"+
		"\u00f5\u0001\u0000\u0000\u0000\u00f4\u00e4\u0001\u0000\u0000\u0000\u00f4"+
		"\u00e8\u0001\u0000\u0000\u0000\u00f5\u0017\u0001\u0000\u0000\u0000\u00f6"+
		"\u00f7\u0005J\u0000\u0000\u00f7\u00f8\u0003\u0018\f\u0000\u00f8\u00f9"+
		"\u0005K\u0000\u0000\u00f9\u0107\u0001\u0000\u0000\u0000\u00fa\u0104\u0003"+
		",\u0016\u0000\u00fb\u00fc\u0007\u0007\u0000\u0000\u00fc\u0101\u0003\n"+
		"\u0005\u0000\u00fd\u00fe\u0007\u0007\u0000\u0000\u00fe\u0100\u0003\n\u0005"+
		"\u0000\u00ff\u00fd\u0001\u0000\u0000\u0000\u0100\u0103\u0001\u0000\u0000"+
		"\u0000\u0101\u00ff\u0001\u0000\u0000\u0000\u0101\u0102\u0001\u0000\u0000"+
		"\u0000\u0102\u0105\u0001\u0000\u0000\u0000\u0103\u0101\u0001\u0000\u0000"+
		"\u0000\u0104\u00fb\u0001\u0000\u0000\u0000\u0104\u0105\u0001\u0000\u0000"+
		"\u0000\u0105\u0107\u0001\u0000\u0000\u0000\u0106\u00f6\u0001\u0000\u0000"+
		"\u0000\u0106\u00fa\u0001\u0000\u0000\u0000\u0107\u0019\u0001\u0000\u0000"+
		"\u0000\u0108\u010a\u0005T\u0000\u0000\u0109\u0108\u0001\u0000\u0000\u0000"+
		"\u0109\u010a\u0001\u0000\u0000\u0000\u010a\u010b\u0001\u0000\u0000\u0000"+
		"\u010b\u010c\u0005U\u0000\u0000\u010c\u0117\u0005J\u0000\u0000\u010d\u0112"+
		"\u0003 \u0010\u0000\u010e\u010f\u0007\b\u0000\u0000\u010f\u0111\u0003"+
		" \u0010\u0000\u0110\u010e\u0001\u0000\u0000\u0000\u0111\u0114\u0001\u0000"+
		"\u0000\u0000\u0112\u0110\u0001\u0000\u0000\u0000\u0112\u0113\u0001\u0000"+
		"\u0000\u0000\u0113\u0116\u0001\u0000\u0000\u0000\u0114\u0112\u0001\u0000"+
		"\u0000\u0000\u0115\u010d\u0001\u0000\u0000\u0000\u0116\u0119\u0001\u0000"+
		"\u0000\u0000\u0117\u0115\u0001\u0000\u0000\u0000\u0117\u0118\u0001\u0000"+
		"\u0000\u0000\u0118\u011a\u0001\u0000\u0000\u0000\u0119\u0117\u0001\u0000"+
		"\u0000\u0000\u011a\u011b\u0005K\u0000\u0000\u011b\u001b\u0001\u0000\u0000"+
		"\u0000\u011c\u011d\u0007\t\u0000\u0000\u011d\u001d\u0001\u0000\u0000\u0000"+
		"\u011e\u0122\u0005\u0006\u0000\u0000\u011f\u0122\u0005\u0007\u0000\u0000"+
		"\u0120\u0122\u0007\n\u0000\u0000\u0121\u011e\u0001\u0000\u0000\u0000\u0121"+
		"\u011f\u0001\u0000\u0000\u0000\u0121\u0120\u0001\u0000\u0000\u0000\u0122"+
		"\u001f\u0001\u0000\u0000\u0000\u0123\u012b\u0003\n\u0005\u0000\u0124\u012b"+
		"\u0003\b\u0004\u0000\u0125\u012b\u0003\u0014\n\u0000\u0126\u012b\u0003"+
		"\u0016\u000b\u0000\u0127\u012b\u0003\u0018\f\u0000\u0128\u012b\u0003&"+
		"\u0013\u0000\u0129\u012b\u0003.\u0017\u0000\u012a\u0123\u0001\u0000\u0000"+
		"\u0000\u012a\u0124\u0001\u0000\u0000\u0000\u012a\u0125\u0001\u0000\u0000"+
		"\u0000\u012a\u0126\u0001\u0000\u0000\u0000\u012a\u0127\u0001\u0000\u0000"+
		"\u0000\u012a\u0128\u0001\u0000\u0000\u0000\u012a\u0129\u0001\u0000\u0000"+
		"\u0000\u012b!\u0001\u0000\u0000\u0000\u012c\u0158\u0007\u000b\u0000\u0000"+
		"\u012d\u012e\u0005\u0001\u0000\u0000\u012e\u012f\u0003\b\u0004\u0000\u012f"+
		"\u0130\u0005\u0002\u0000\u0000\u0130\u0136\u0003\b\u0004\u0000\u0131\u0132"+
		"\u0005\u0004\u0000\u0000\u0132\u0133\u0003\b\u0004\u0000\u0133\u0134\u0005"+
		"\u0002\u0000\u0000\u0134\u0135\u0003\b\u0004\u0000\u0135\u0137\u0001\u0000"+
		"\u0000\u0000\u0136\u0131\u0001\u0000\u0000\u0000\u0136\u0137\u0001\u0000"+
		"\u0000\u0000\u0137\u0138\u0001\u0000\u0000\u0000\u0138\u0139\u0005\u0003"+
		"\u0000\u0000\u0139\u013a\u0003\b\u0004\u0000\u013a\u013b\u0005\u0005\u0000"+
		"\u0000\u013b\u0158\u0001\u0000\u0000\u0000\u013c\u013d\u0005\u0001\u0000"+
		"\u0000\u013d\u013e\u0005J\u0000\u0000\u013e\u013f\u0003\b\u0004\u0000"+
		"\u013f\u0140\u0007\b\u0000\u0000\u0140\u0148\u0003\b\u0004\u0000\u0141"+
		"\u0142\u0007\b\u0000\u0000\u0142\u0143\u0003\b\u0004\u0000\u0143\u0144"+
		"\u0007\b\u0000\u0000\u0144\u0145\u0003\b\u0004\u0000\u0145\u0147\u0001"+
		"\u0000\u0000\u0000\u0146\u0141\u0001\u0000\u0000\u0000\u0147\u014a\u0001"+
		"\u0000\u0000\u0000\u0148\u0146\u0001\u0000\u0000\u0000\u0148\u0149\u0001"+
		"\u0000\u0000\u0000\u0149\u014b\u0001\u0000\u0000\u0000\u014a\u0148\u0001"+
		"\u0000\u0000\u0000\u014b\u014c\u0007\b\u0000\u0000\u014c\u014d\u0003\b"+
		"\u0004\u0000\u014d\u014e\u0005K\u0000\u0000\u014e\u0158\u0001\u0000\u0000"+
		"\u0000\u014f\u0151\u0005^\u0000\u0000\u0150\u014f\u0001\u0000\u0000\u0000"+
		"\u0150\u0151\u0001\u0000\u0000\u0000\u0151\u0152\u0001\u0000\u0000\u0000"+
		"\u0152\u0158\u0003\u001a\r\u0000\u0153\u0155\u0005^\u0000\u0000\u0154"+
		"\u0153\u0001\u0000\u0000\u0000\u0154\u0155\u0001\u0000\u0000\u0000\u0155"+
		"\u0156\u0001\u0000\u0000\u0000\u0156\u0158\u0005U\u0000\u0000\u0157\u012c"+
		"\u0001\u0000\u0000\u0000\u0157\u012d\u0001\u0000\u0000\u0000\u0157\u013c"+
		"\u0001\u0000\u0000\u0000\u0157\u0150\u0001\u0000\u0000\u0000\u0157\u0154"+
		"\u0001\u0000\u0000\u0000\u0158#\u0001\u0000\u0000\u0000\u0159\u015a\u0005"+
		"\u0001\u0000\u0000\u015a\u015b\u0003\b\u0004\u0000\u015b\u015c\u0005\u0002"+
		"\u0000\u0000\u015c\u0162\u0003\n\u0005\u0000\u015d\u015e\u0005\u0004\u0000"+
		"\u0000\u015e\u015f\u0003\b\u0004\u0000\u015f\u0160\u0005\u0002\u0000\u0000"+
		"\u0160\u0161\u0003\n\u0005\u0000\u0161\u0163\u0001\u0000\u0000\u0000\u0162"+
		"\u015d\u0001\u0000\u0000\u0000\u0162\u0163\u0001\u0000\u0000\u0000\u0163"+
		"\u0164\u0001\u0000\u0000\u0000\u0164\u0165\u0005\u0003\u0000\u0000\u0165"+
		"\u0166\u0003\n\u0005\u0000\u0166\u0167\u0005\u0005\u0000\u0000\u0167\u018c"+
		"\u0001\u0000\u0000\u0000\u0168\u0169\u0005\u0001\u0000\u0000\u0169\u016a"+
		"\u0005J\u0000\u0000\u016a\u016b\u0003\b\u0004\u0000\u016b\u016c\u0007"+
		"\b\u0000\u0000\u016c\u0174\u0003\n\u0005\u0000\u016d\u016e\u0007\b\u0000"+
		"\u0000\u016e\u016f\u0003\b\u0004\u0000\u016f\u0170\u0007\b\u0000\u0000"+
		"\u0170\u0171\u0003\n\u0005\u0000\u0171\u0173\u0001\u0000\u0000\u0000\u0172"+
		"\u016d\u0001\u0000\u0000\u0000\u0173\u0176\u0001\u0000\u0000\u0000\u0174"+
		"\u0172\u0001\u0000\u0000\u0000\u0174\u0175\u0001\u0000\u0000\u0000\u0175"+
		"\u0177\u0001\u0000\u0000\u0000\u0176\u0174\u0001\u0000\u0000\u0000\u0177"+
		"\u0178\u0007\b\u0000\u0000\u0178\u0179\u0003\n\u0005\u0000\u0179\u017a"+
		"\u0005K\u0000\u0000\u017a\u018c\u0001\u0000\u0000\u0000\u017b\u018c\u0005"+
		"\"\u0000\u0000\u017c\u018c\u0005!\u0000\u0000\u017d\u018c\u0005\u001e"+
		"\u0000\u0000\u017e\u018c\u0005\u001f\u0000\u0000\u017f\u018c\u0005X\u0000"+
		"\u0000\u0180\u0182\u0005_\u0000\u0000\u0181\u0180\u0001\u0000\u0000\u0000"+
		"\u0181\u0182\u0001\u0000\u0000\u0000\u0182\u0183\u0001\u0000\u0000\u0000"+
		"\u0183\u018c\u0003\u001a\r\u0000\u0184\u0186\u0005_\u0000\u0000\u0185"+
		"\u0184\u0001\u0000\u0000\u0000\u0185\u0186\u0001\u0000\u0000\u0000\u0186"+
		"\u0187\u0001\u0000\u0000\u0000\u0187\u018a\u0005U\u0000\u0000\u0188\u018a"+
		"\u0005V\u0000\u0000\u0189\u0185\u0001\u0000\u0000\u0000\u0189\u0188\u0001"+
		"\u0000\u0000\u0000\u018a\u018c\u0001\u0000\u0000\u0000\u018b\u0159\u0001"+
		"\u0000\u0000\u0000\u018b\u0168\u0001\u0000\u0000\u0000\u018b\u017b\u0001"+
		"\u0000\u0000\u0000\u018b\u017c\u0001\u0000\u0000\u0000\u018b\u017d\u0001"+
		"\u0000\u0000\u0000\u018b\u017e\u0001\u0000\u0000\u0000\u018b\u017f\u0001"+
		"\u0000\u0000\u0000\u018b\u0181\u0001\u0000\u0000\u0000\u018b\u0189\u0001"+
		"\u0000\u0000\u0000\u018c%\u0001\u0000\u0000\u0000\u018d\u018e\u0005\u0001"+
		"\u0000\u0000\u018e\u018f\u0003\b\u0004\u0000\u018f\u0190\u0005\u0002\u0000"+
		"\u0000\u0190\u0196\u0003&\u0013\u0000\u0191\u0192\u0005\u0004\u0000\u0000"+
		"\u0192\u0193\u0003\b\u0004\u0000\u0193\u0194\u0005\u0002\u0000\u0000\u0194"+
		"\u0195\u0003&\u0013\u0000\u0195\u0197\u0001\u0000\u0000\u0000\u0196\u0191"+
		"\u0001\u0000\u0000\u0000\u0196\u0197\u0001\u0000\u0000\u0000\u0197\u0198"+
		"\u0001\u0000\u0000\u0000\u0198\u0199\u0005\u0003\u0000\u0000\u0199\u019a"+
		"\u0003&\u0013\u0000\u019a\u019b\u0005\u0005\u0000\u0000\u019b\u01b9\u0001"+
		"\u0000\u0000\u0000\u019c\u019d\u0005\u0001\u0000\u0000\u019d\u019e\u0005"+
		"J\u0000\u0000\u019e\u019f\u0003\b\u0004\u0000\u019f\u01a0\u0007\b\u0000"+
		"\u0000\u01a0\u01a8\u0003&\u0013\u0000\u01a1\u01a2\u0007\b\u0000\u0000"+
		"\u01a2\u01a3\u0003\b\u0004\u0000\u01a3\u01a4\u0007\b\u0000\u0000\u01a4"+
		"\u01a5\u0003&\u0013\u0000\u01a5\u01a7\u0001\u0000\u0000\u0000\u01a6\u01a1"+
		"\u0001\u0000\u0000\u0000\u01a7\u01aa\u0001\u0000\u0000\u0000\u01a8\u01a6"+
		"\u0001\u0000\u0000\u0000\u01a8\u01a9\u0001\u0000\u0000\u0000\u01a9\u01ab"+
		"\u0001\u0000\u0000\u0000\u01aa\u01a8\u0001\u0000\u0000\u0000\u01ab\u01ac"+
		"\u0007\b\u0000\u0000\u01ac\u01ad\u0003&\u0013\u0000\u01ad\u01ae\u0005"+
		"K\u0000\u0000\u01ae\u01b9\u0001\u0000\u0000\u0000\u01af\u01b9\u0005W\u0000"+
		"\u0000\u01b0\u01b2\u0005`\u0000\u0000\u01b1\u01b0\u0001\u0000\u0000\u0000"+
		"\u01b1\u01b2\u0001\u0000\u0000\u0000\u01b2\u01b3\u0001\u0000\u0000\u0000"+
		"\u01b3\u01b9\u0003\u001a\r\u0000\u01b4\u01b6\u0005`\u0000\u0000\u01b5"+
		"\u01b4\u0001\u0000\u0000\u0000\u01b5\u01b6\u0001\u0000\u0000\u0000\u01b6"+
		"\u01b7\u0001\u0000\u0000\u0000\u01b7\u01b9\u0005U\u0000\u0000\u01b8\u018d"+
		"\u0001\u0000\u0000\u0000\u01b8\u019c\u0001\u0000\u0000\u0000\u01b8\u01af"+
		"\u0001\u0000\u0000\u0000\u01b8\u01b1\u0001\u0000\u0000\u0000\u01b8\u01b5"+
		"\u0001\u0000\u0000\u0000\u01b9\'\u0001\u0000\u0000\u0000\u01ba\u01bb\u0005"+
		"\u0001\u0000\u0000\u01bb\u01bc\u0003\b\u0004\u0000\u01bc\u01bd\u0005\u0002"+
		"\u0000\u0000\u01bd\u01c3\u0003\u0014\n\u0000\u01be\u01bf\u0005\u0004\u0000"+
		"\u0000\u01bf\u01c0\u0003\b\u0004\u0000\u01c0\u01c1\u0005\u0002\u0000\u0000"+
		"\u01c1\u01c2\u0003\u0014\n\u0000\u01c2\u01c4\u0001\u0000\u0000\u0000\u01c3"+
		"\u01be\u0001\u0000\u0000\u0000\u01c3\u01c4\u0001\u0000\u0000\u0000\u01c4"+
		"\u01c5\u0001\u0000\u0000\u0000\u01c5\u01c6\u0005\u0003\u0000\u0000\u01c6"+
		"\u01c7\u0003\u0014\n\u0000\u01c7\u01c8\u0005\u0005\u0000\u0000\u01c8\u01e7"+
		"\u0001\u0000\u0000\u0000\u01c9\u01ca\u0005\u0001\u0000\u0000\u01ca\u01cb"+
		"\u0005J\u0000\u0000\u01cb\u01cc\u0003\b\u0004\u0000\u01cc\u01cd\u0007"+
		"\b\u0000\u0000\u01cd\u01d5\u0003\u0014\n\u0000\u01ce\u01cf\u0007\b\u0000"+
		"\u0000\u01cf\u01d0\u0003\b\u0004\u0000\u01d0\u01d1\u0007\b\u0000\u0000"+
		"\u01d1\u01d2\u0003\u0014\n\u0000\u01d2\u01d4\u0001\u0000\u0000\u0000\u01d3"+
		"\u01ce\u0001\u0000\u0000\u0000\u01d4\u01d7\u0001\u0000\u0000\u0000\u01d5"+
		"\u01d3\u0001\u0000\u0000\u0000\u01d5\u01d6\u0001\u0000\u0000\u0000\u01d6"+
		"\u01d8\u0001\u0000\u0000\u0000\u01d7\u01d5\u0001\u0000\u0000\u0000\u01d8"+
		"\u01d9\u0007\b\u0000\u0000\u01d9\u01da\u0003\u0014\n\u0000\u01da\u01db"+
		"\u0005K\u0000\u0000\u01db\u01e7\u0001\u0000\u0000\u0000\u01dc\u01e7\u0005"+
		"Z\u0000\u0000\u01dd\u01e7\u00055\u0000\u0000\u01de\u01e0\u0005a\u0000"+
		"\u0000\u01df\u01de\u0001\u0000\u0000\u0000\u01df\u01e0\u0001\u0000\u0000"+
		"\u0000\u01e0\u01e1\u0001\u0000\u0000\u0000\u01e1\u01e7\u0005U\u0000\u0000"+
		"\u01e2\u01e4\u0005a\u0000\u0000\u01e3\u01e2\u0001\u0000\u0000\u0000\u01e3"+
		"\u01e4\u0001\u0000\u0000\u0000\u01e4\u01e5\u0001\u0000\u0000\u0000\u01e5"+
		"\u01e7\u0003\u001a\r\u0000\u01e6\u01ba\u0001\u0000\u0000\u0000\u01e6\u01c9"+
		"\u0001\u0000\u0000\u0000\u01e6\u01dc\u0001\u0000\u0000\u0000\u01e6\u01dd"+
		"\u0001\u0000\u0000\u0000\u01e6\u01df\u0001\u0000\u0000\u0000\u01e6\u01e3"+
		"\u0001\u0000\u0000\u0000\u01e7)\u0001\u0000\u0000\u0000\u01e8\u01e9\u0005"+
		"\u0001\u0000\u0000\u01e9\u01ea\u0003\b\u0004\u0000\u01ea\u01eb\u0005\u0002"+
		"\u0000\u0000\u01eb\u01f1\u0003\u0016\u000b\u0000\u01ec\u01ed\u0005\u0004"+
		"\u0000\u0000\u01ed\u01ee\u0003\b\u0004\u0000\u01ee\u01ef\u0005\u0002\u0000"+
		"\u0000\u01ef\u01f0\u0003\u0016\u000b\u0000\u01f0\u01f2\u0001\u0000\u0000"+
		"\u0000\u01f1\u01ec\u0001\u0000\u0000\u0000\u01f1\u01f2\u0001\u0000\u0000"+
		"\u0000\u01f2\u01f3\u0001\u0000\u0000\u0000\u01f3\u01f4\u0005\u0003\u0000"+
		"\u0000\u01f4\u01f5\u0003\u0016\u000b\u0000\u01f5\u01f6\u0005\u0005\u0000"+
		"\u0000\u01f6\u0215\u0001\u0000\u0000\u0000\u01f7\u01f8\u0005\u0001\u0000"+
		"\u0000\u01f8\u01f9\u0005J\u0000\u0000\u01f9\u01fa\u0003\b\u0004\u0000"+
		"\u01fa\u01fb\u0007\b\u0000\u0000\u01fb\u0203\u0003\u0016\u000b\u0000\u01fc"+
		"\u01fd\u0007\b\u0000\u0000\u01fd\u01fe\u0003\b\u0004\u0000\u01fe\u01ff"+
		"\u0007\b\u0000\u0000\u01ff\u0200\u0003\u0016\u000b\u0000\u0200\u0202\u0001"+
		"\u0000\u0000\u0000\u0201\u01fc\u0001\u0000\u0000\u0000\u0202\u0205\u0001"+
		"\u0000\u0000\u0000\u0203\u0201\u0001\u0000\u0000\u0000\u0203\u0204\u0001"+
		"\u0000\u0000\u0000\u0204\u0206\u0001\u0000\u0000\u0000\u0205\u0203\u0001"+
		"\u0000\u0000\u0000\u0206\u0207\u0007\b\u0000\u0000\u0207\u0208\u0003\u0016"+
		"\u000b\u0000\u0208\u0209\u0005K\u0000\u0000\u0209\u0215\u0001\u0000\u0000"+
		"\u0000\u020a\u0215\u0005[\u0000\u0000\u020b\u0215\u00056\u0000\u0000\u020c"+
		"\u020e\u0005b\u0000\u0000\u020d\u020c\u0001\u0000\u0000\u0000\u020d\u020e"+
		"\u0001\u0000\u0000\u0000\u020e\u020f\u0001\u0000\u0000\u0000\u020f\u0215"+
		"\u0005U\u0000\u0000\u0210\u0212\u0005b\u0000\u0000\u0211\u0210\u0001\u0000"+
		"\u0000\u0000\u0211\u0212\u0001\u0000\u0000\u0000\u0212\u0213\u0001\u0000"+
		"\u0000\u0000\u0213\u0215\u0003\u001a\r\u0000\u0214\u01e8\u0001\u0000\u0000"+
		"\u0000\u0214\u01f7\u0001\u0000\u0000\u0000\u0214\u020a\u0001\u0000\u0000"+
		"\u0000\u0214\u020b\u0001\u0000\u0000\u0000\u0214\u020d\u0001\u0000\u0000"+
		"\u0000\u0214\u0211\u0001\u0000\u0000\u0000\u0215+\u0001\u0000\u0000\u0000"+
		"\u0216\u0217\u0005\u0001\u0000\u0000\u0217\u0218\u0003\b\u0004\u0000\u0218"+
		"\u0219\u0005\u0002\u0000\u0000\u0219\u021f\u0003\u0018\f\u0000\u021a\u021b"+
		"\u0005\u0004\u0000\u0000\u021b\u021c\u0003\b\u0004\u0000\u021c\u021d\u0005"+
		"\u0002\u0000\u0000\u021d\u021e\u0003\u0018\f\u0000\u021e\u0220\u0001\u0000"+
		"\u0000\u0000\u021f\u021a\u0001\u0000\u0000\u0000\u021f\u0220\u0001\u0000"+
		"\u0000\u0000\u0220\u0221\u0001\u0000\u0000\u0000\u0221\u0222\u0005\u0003"+
		"\u0000\u0000\u0222\u0223\u0003\u0018\f\u0000\u0223\u0224\u0005\u0005\u0000"+
		"\u0000\u0224\u0246\u0001\u0000\u0000\u0000\u0225\u0226\u0005\u0001\u0000"+
		"\u0000\u0226\u0227\u0005J\u0000\u0000\u0227\u0228\u0003\b\u0004\u0000"+
		"\u0228\u0229\u0007\b\u0000\u0000\u0229\u0231\u0003\u0018\f\u0000\u022a"+
		"\u022b\u0007\b\u0000\u0000\u022b\u022c\u0003\b\u0004\u0000\u022c\u022d"+
		"\u0007\b\u0000\u0000\u022d\u022e\u0003\u0018\f\u0000\u022e\u0230\u0001"+
		"\u0000\u0000\u0000\u022f\u022a\u0001\u0000\u0000\u0000\u0230\u0233\u0001"+
		"\u0000\u0000\u0000\u0231\u022f\u0001\u0000\u0000\u0000\u0231\u0232\u0001"+
		"\u0000\u0000\u0000\u0232\u0234\u0001\u0000\u0000\u0000\u0233\u0231\u0001"+
		"\u0000\u0000\u0000\u0234\u0235\u0007\b\u0000\u0000\u0235\u0236\u0003\u0018"+
		"\f\u0000\u0236\u0237\u0005K\u0000\u0000\u0237\u0246\u0001\u0000\u0000"+
		"\u0000\u0238\u023a\u0005]\u0000\u0000\u0239\u023b\u0005\\\u0000\u0000"+
		"\u023a\u0239\u0001\u0000\u0000\u0000\u023a\u023b\u0001\u0000\u0000\u0000"+
		"\u023b\u0246\u0001\u0000\u0000\u0000\u023c\u0246\u00057\u0000\u0000\u023d"+
		"\u023f\u0005c\u0000\u0000\u023e\u023d\u0001\u0000\u0000\u0000\u023e\u023f"+
		"\u0001\u0000\u0000\u0000\u023f\u0240\u0001\u0000\u0000\u0000\u0240\u0246"+
		"\u0005U\u0000\u0000\u0241\u0243\u0005c\u0000\u0000\u0242\u0241\u0001\u0000"+
		"\u0000\u0000\u0242\u0243\u0001\u0000\u0000\u0000\u0243\u0244\u0001\u0000"+
		"\u0000\u0000\u0244\u0246\u0003\u001a\r\u0000\u0245\u0216\u0001\u0000\u0000"+
		"\u0000\u0245\u0225\u0001\u0000\u0000\u0000\u0245\u0238\u0001\u0000\u0000"+
		"\u0000\u0245\u023c\u0001\u0000\u0000\u0000\u0245\u023e\u0001\u0000\u0000"+
		"\u0000\u0245\u0242\u0001\u0000\u0000\u0000\u0246-\u0001\u0000\u0000\u0000"+
		"\u0247\u0248\u0005L\u0000\u0000\u0248\u024d\u0003 \u0010\u0000\u0249\u024a"+
		"\u0005P\u0000\u0000\u024a\u024c\u0003 \u0010\u0000\u024b\u0249\u0001\u0000"+
		"\u0000\u0000\u024c\u024f\u0001\u0000\u0000\u0000\u024d\u024b\u0001\u0000"+
		"\u0000\u0000\u024d\u024e\u0001\u0000\u0000\u0000\u024e\u0250\u0001\u0000"+
		"\u0000\u0000\u024f\u024d\u0001\u0000\u0000\u0000\u0250\u0251\u0005M\u0000"+
		"\u0000\u0251\u0257\u0001\u0000\u0000\u0000\u0252\u0254\u0005d\u0000\u0000"+
		"\u0253\u0252\u0001\u0000\u0000\u0000\u0253\u0254\u0001\u0000\u0000\u0000"+
		"\u0254\u0255\u0001\u0000\u0000\u0000\u0255\u0257\u0005U\u0000\u0000\u0256"+
		"\u0247\u0001\u0000\u0000\u0000\u0256\u0253\u0001\u0000\u0000\u0000\u0257"+
		"/\u0001\u0000\u0000\u0000\u0258\u0259\u0005L\u0000\u0000\u0259\u025e\u0005"+
		"U\u0000\u0000\u025a\u025b\u0005P\u0000\u0000\u025b\u025d\u0005U\u0000"+
		"\u0000\u025c\u025a\u0001\u0000\u0000\u0000\u025d\u0260\u0001\u0000\u0000"+
		"\u0000\u025e\u025c\u0001\u0000\u0000\u0000\u025e\u025f\u0001\u0000\u0000"+
		"\u0000\u025f\u0261\u0001\u0000\u0000\u0000\u0260\u025e\u0001\u0000\u0000"+
		"\u0000\u0261\u0262\u0005M\u0000\u0000\u02621\u0001\u0000\u0000\u0000@"+
		"49BFSWu\u007f\u0081\u0098\u00ac\u00ae\u00b4\u00c2\u00dd\u00e0\u00e2\u00ef"+
		"\u00f2\u00f4\u0101\u0104\u0106\u0109\u0112\u0117\u0121\u012a\u0136\u0148"+
		"\u0150\u0154\u0157\u0162\u0174\u0181\u0185\u0189\u018b\u0196\u01a8\u01b1"+
		"\u01b5\u01b8\u01c3\u01d5\u01df\u01e3\u01e6\u01f1\u0203\u020d\u0211\u0214"+
		"\u021f\u0231\u023a\u023e\u0242\u0245\u024d\u0253\u0256\u025e";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}