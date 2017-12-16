package pl.jkan.ecommerce.sales.functional;

import org.junit.Assert;
import org.junit.Test;
import pl.jkan.ecommerce.canonicalmodel.Identifier;
import pl.jkan.ecommerce.sales.application.ConfirmOrderCommand;
import pl.jkan.ecommerce.sales.application.ConfirmOrderHandler;
import pl.jkan.ecommerce.sales.domain.basket.Basket;
import pl.jkan.ecommerce.sales.domain.order.Order;
import pl.jkan.ecommerce.sales.domain.order.OrderItem;
import pl.jkan.ecommerce.sales.domain.productcatalog.Product;
import pl.jkan.ecommerce.sales.infrastructure.InMemoryBasketStorage;
import pl.jkan.ecommerce.sales.infrastructure.InMemoryOrderRepository;
import pl.jkan.ecommerce.sales.infrastructure.InMemoryProductCatalog;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class OrderingTest {

    private final InMemoryProductCatalog productCatalog;
    private final InMemoryBasketStorage basketStorage;
    private final ConfirmOrderHandler confirmOrderHandler;
    private final InMemoryOrderRepository orderRepository;
    private InMemorySystemUserContext systemUserContext;

    private Identifier orderId;

    public OrderingTest() {
        this.systemUserContext = new InMemorySystemUserContext();
        this.productCatalog = new InMemoryProductCatalog();
        this.basketStorage = new InMemoryBasketStorage();
        this.orderRepository = new InMemoryOrderRepository();
        this.confirmOrderHandler = new ConfirmOrderHandler(
                this.orderRepository
        );
    }

    @Test
    public void itAllowOrderSelectedProducts() {
        iAmGuestBuyerIdentifiedWith(new Identifier("customer_1"));
        selectedProduct(new Identifier("p1"));
        selectedProduct(new Identifier("p2"));

        confirmReservation();

        thereIsPendingOrderWithId(orderId);
        orderContainsProduct(new Identifier("p1"));
        orderContainsProduct(new Identifier("p2"));
//        orderIsWaitingForPayment(orderId);
//        thereIsPendingPayment(orderId, 20.00);
    }

    private void thereIsPendingPayment(Identifier orderId, double money) {

    }

    private void orderIsWaitingForPayment(Identifier orderId) {

    }

    private void orderContainsProduct(Identifier p) {
        Order order = this.orderRepository.load(orderId);

        thereIsFollowingProductInOrderedItems(p, order.getItems());
    }

    private void thereIsFollowingProductInOrderedItems(Identifier productId, Collection<OrderItem> items) {
        List<OrderItem> ordered = items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .collect(Collectors.toList())
        ;

        Assert.assertFalse(ordered.isEmpty());
    }

    private void thereIsPendingOrderWithId(Identifier orderId) {

        try {
            Order order = this.orderRepository.load(orderId);
            Assert.assertTrue(order.getId().equals(orderId));
        } catch (RuntimeException e) {
            Assert.fail("There is no such order");
        }
    }

    private void confirmReservation() {
        orderId = Identifier.generateUUID();
        confirmOrderHandler.handle(new ConfirmOrderCommand(orderId));
    }

    private void selectedProduct(Identifier p1) {
        Basket basket = basketStorage.loadForCustomer(new Identifier("customer_1"));
        basket.add(new Product(p1, 10));
    }

    private void iAmGuestBuyerIdentifiedWith(Identifier customer) {
        systemUserContext.authenticate(customer);
    }
}