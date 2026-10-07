package com.acme.scm.supply;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
class SupplyPersistenceTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("supply_test")
            .withUsername("scm")
            .withPassword("test-password");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        properties.add("spring.datasource.username", POSTGRES::getUsername);
        properties.add("spring.datasource.password", POSTGRES::getPassword);
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    ProductRepository products;

    @Autowired
    StockItemRepository stockItems;

    @Test
    void stockPersistsAgainstARealProductAndRejectsNegativeInventory() {
        Product product = products.saveAndFlush(new Product("SKU-42", "Widget", null, 1250L));
        StockItem stock = new StockItem(product.getSku());
        stock.adjust(8);
        stockItems.saveAndFlush(stock);

        assertThat(stockItems.findBySkuIgnoreCase("sku-42")).get()
                .extracting(StockItem::getOnHand).isEqualTo(8L);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> stock.adjust(-9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }
}
