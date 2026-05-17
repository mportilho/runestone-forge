package com.runestone.expeval.api;

import com.runestone.expeval.api.support.FoldingNavigationFixtures;
import com.runestone.expeval.api.support.FoldingNavigationFixtures.Account;
import com.runestone.expeval.api.support.FoldingNavigationFixtures.AccountKey;
import com.runestone.expeval.api.support.FoldingNavigationFixtures.Book;
import com.runestone.expeval.api.support.FoldingNavigationFixtures.TrackedList;
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
        MathExpression expression = MathExpression.compile("PRICES[0]", pricesEnv(prices));
        prices.resetAccessCount();

        expression.compute();
        expression.compute();
        expression.compute();

        assertThat(prices.accessCount()).isZero();
    }

    @Test
    @DisplayName("slice, wildcard and aggregations are folded")
    void sliceWildcardAndAggregationsAreFolded() {
        TrackedList<BigDecimal> prices = FoldingNavigationFixtures.prices();
        MathExpression slice = MathExpression.compile("PRICES[1:3]..sum()", pricesEnv(prices));
        MathExpression wildcard = MathExpression.compile("PRICES[*]..count()", pricesEnv(prices));
        prices.resetAccessCount();

        assertThat(slice.compute()).isEqualByComparingTo("40");
        assertThat(wildcard.compute()).isEqualByComparingTo("4");
        assertThat(prices.accessCount()).isZero();
    }

    @Test
    @DisplayName("filters using @ and nested object navigation are folded")
    void filtersUsingCurrentElementAreFolded() {
        TrackedList<Book> books = FoldingNavigationFixtures.books();
        MathExpression expression = MathExpression.compile(
                "BOOKS[?(@.author = \"Alice\")].price..sum()",
                booksEnv(books));
        books.resetAccessCount();

        assertThat(expression.compute()).isEqualByComparingTo("14.98");
        assertThat(books.accessCount()).isZero();
    }

    @Test
    @DisplayName("map entry filters with @.key and @.value are folded")
    void mapEntryFiltersAreFolded() {
        Map<AccountKey, Account> accounts = FoldingNavigationFixtures.accounts();
        MathExpression expression = MathExpression.compile(
                "ACCOUNTS[?(@.key.domain = \"ops\" and @.value.balance > 15)]..values()..ds(balance)..sum()",
                accountsEnv(accounts));

        assertThat(expression.compute()).isEqualByComparingTo("30");
    }

    @Test
    @DisplayName("map transforms over constant lists are folded")
    void mapTransformsAreFolded() {
        TrackedList<Book> books = FoldingNavigationFixtures.books();
        MathExpression expression = MathExpression.compile("BOOKS..map(@ -> @.price * 2)..sum()", booksEnv(books));
        books.resetAccessCount();

        assertThat(expression.compute()).isEqualByComparingTo("95.92");
        assertThat(books.accessCount()).isZero();
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
