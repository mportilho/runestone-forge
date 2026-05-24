package com.runestone.expeval.api;

import com.runestone.expeval.api.support.FoldingNavigationFixtures;
import com.runestone.expeval.api.support.FoldingNavigationFixtures.Account;
import com.runestone.expeval.api.support.FoldingNavigationFixtures.AccountKey;
import com.runestone.expeval.api.support.FoldingNavigationFixtures.Book;
import com.runestone.expeval.api.support.FoldingNavigationFixtures.TrackedList;
import com.runestone.expeval.api.support.FoldingNavigationFixtures.TrackedMap;
import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Collection navigation on folded non-overridable external symbols")
class FoldedSymbolCollectionNavigationTest {

    @Test
    @DisplayName("index access is folded and does not touch the list during compute")
    void indexAccessIsFolded() {
        TrackedList<BigDecimal> prices = FoldingNavigationFixtures.prices();
        MathExpression expression = compile("PRICES[0]", pricesEnv(prices));

        assertThat(prices.accessCount()).isPositive();
        prices.resetAccessCount();

        assertThat(expression.compute()).isEqualByComparingTo("5");
        assertThat(expression.compute()).isEqualByComparingTo("5");
        assertThat(expression.compute()).isEqualByComparingTo("5");

        assertThat(prices.accessCount()).isZero();
    }

    @Test
    @DisplayName("slice, wildcard and aggregations are folded")
    void sliceWildcardAndAggregationsAreFolded() {
        TrackedList<BigDecimal> prices = FoldingNavigationFixtures.prices();
        MathExpression slice = compile("PRICES[1:3]..sum()", pricesEnv(prices));
        MathExpression wildcard = compile("PRICES[*]..count()", pricesEnv(prices));

        assertThat(prices.accessCount()).isPositive();
        prices.resetAccessCount();

        assertThat(slice.compute()).isEqualByComparingTo("40");
        assertThat(wildcard.compute()).isEqualByComparingTo("4");
        assertThat(prices.accessCount()).isZero();
    }

    @Test
    @DisplayName("filters using @ and nested object navigation are folded")
    void filtersUsingCurrentElementAreFolded() {
        TrackedList<Book> books = FoldingNavigationFixtures.books();
        MathExpression expression = compile(
                "BOOKS[?(@.author = \"Alice\")].price..sum()",
                booksEnv(books));

        assertThat(books.accessCount()).isPositive();
        books.resetAccessCount();

        assertThat(expression.compute()).isEqualByComparingTo("14.98");
        assertThat(books.accessCount()).isZero();
    }

    @Test
    @DisplayName("map entry filters with @.key and @.value are folded")
    void mapEntryFiltersAreFolded() {
        TrackedMap<AccountKey, Account> accounts = FoldingNavigationFixtures.accounts();
        MathExpression expression = compile(
                "ACCOUNTS[?(@.key.domain = \"ops\" and @.value.balance > 15)]..values()..ds(balance)..sum()",
                accountsEnv(accounts));

        assertThat(accounts.accessCount()).isPositive();
        accounts.resetAccessCount();

        assertThat(expression.compute()).isEqualByComparingTo("30");
        assertThat(expression.compute()).isEqualByComparingTo("30");
        assertThat(accounts.accessCount()).isZero();
    }

    @Test
    @DisplayName("map transforms over constant lists are folded")
    void mapTransformsAreFolded() {
        TrackedList<Book> books = FoldingNavigationFixtures.books();
        MathExpression expression = compile("BOOKS..map(@ -> @.price * 2)..sum()", booksEnv(books));

        assertThat(books.accessCount()).isPositive();
        books.resetAccessCount();

        assertThat(expression.compute()).isEqualByComparingTo("95.92");
        assertThat(books.accessCount()).isZero();
    }

    private static MathExpression compile(String source, ExpressionEnvironment environment) {
        return MathExpression.compile(source, environment, new ExpressionEngine());
    }

    private static ExpressionEnvironment pricesEnv(TrackedList<BigDecimal> prices) {
        return ExpressionEnvironment.builder()
                .registerExternalSymbol("PRICES", prices, false)
                .build();
    }

    private static ExpressionEnvironment booksEnv(TrackedList<Book> books) {
        return ExpressionEnvironment.builder()
                .registerExternalSymbol("BOOKS", books, false)
                .build();
    }

    private static ExpressionEnvironment accountsEnv(Map<AccountKey, Account> accounts) {
        return ExpressionEnvironment.builder()
                .registerExternalSymbol("ACCOUNTS", accounts, false)
                .build();
    }
}
