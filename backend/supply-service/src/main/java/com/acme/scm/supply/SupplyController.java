package com.acme.scm.supply;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class SupplyController {
    private final SupplierRepository suppliers;
    private final ProductRepository products;
    private final StockItemRepository stockItems;

    public SupplyController(SupplierRepository suppliers, ProductRepository products, StockItemRepository stockItems) {
        this.suppliers = suppliers;
        this.products = products;
        this.stockItems = stockItems;
    }

    @PostMapping("/suppliers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER')")
    SupplierView createSupplier(@Valid @RequestBody CreateSupplier request) {
        if (suppliers.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Supplier email already exists");
        }
        Supplier supplier = suppliers.save(new Supplier(request.name().trim(), request.email().trim()));
        return new SupplierView(supplier.getId(), supplier.getName(), supplier.getEmail(), supplier.isActive());
    }

    @GetMapping("/suppliers")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER', 'SUPPLIER')")
    List<SupplierView> listSuppliers() {
        return suppliers.findAll().stream()
                .map(supplier -> new SupplierView(supplier.getId(), supplier.getName(),
                        supplier.getEmail(), supplier.isActive())).toList();
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCUREMENT_MANAGER')")
    ProductView createProduct(@Valid @RequestBody CreateProduct request) {
        String sku = request.sku().trim().toUpperCase();
        if (products.existsBySkuIgnoreCase(sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product SKU already exists");
        }
        Product product = products.save(new Product(sku, request.name().trim(),
                request.description(), request.unitPriceMinor()));
        return productView(product);
    }

    @GetMapping("/products")
    List<ProductView> listProducts() {
        return products.findAll().stream().filter(Product::isActive).map(SupplyController::productView).toList();
    }

    @GetMapping("/products/{sku}")
    ProductView getProduct(@PathVariable String sku) {
        return products.findBySkuIgnoreCase(sku)
                .filter(Product::isActive)
                .map(SupplyController::productView)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @PostMapping("/inventory/adjustments")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Transactional
    StockView adjustStock(@Valid @RequestBody StockAdjustment request) {
        Product product = products.findBySkuIgnoreCase(request.sku())
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        StockItem stock = stockItems.findBySkuIgnoreCase(product.getSku())
                .orElseGet(() -> stockItems.save(new StockItem(product.getSku())));
        stock.adjust(request.delta());
        return new StockView(stock.getSku(), stock.getOnHand(), stock.getVersion());
    }

    @GetMapping("/inventory/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PROCUREMENT_MANAGER')")
    StockView getStock(@PathVariable String sku) {
        StockItem stock = stockItems.findBySkuIgnoreCase(sku)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock record not found"));
        return new StockView(stock.getSku(), stock.getOnHand(), stock.getVersion());
    }

    private static ProductView productView(Product product) {
        return new ProductView(product.getId(), product.getSku(), product.getName(),
                product.getDescription(), product.getUnitPriceMinor());
    }

    record CreateSupplier(@NotBlank @Size(max = 200) String name, @NotBlank @Email @Size(max = 320) String email) { }
    record SupplierView(UUID id, String name, String email, boolean active) { }
    record CreateProduct(@NotBlank @Size(max = 80) String sku, @NotBlank @Size(max = 240) String name,
                         @Size(max = 2000) String description, @NotNull @Positive Long unitPriceMinor) { }
    record ProductView(UUID id, String sku, String name, String description, long unitPriceMinor) { }
    record StockAdjustment(@NotBlank @Size(max = 80) String sku,
                           @NotNull @Min(-1000000000) @Max(1000000000) Long delta) { }
    record StockView(String sku, long onHand, long version) { }
}
