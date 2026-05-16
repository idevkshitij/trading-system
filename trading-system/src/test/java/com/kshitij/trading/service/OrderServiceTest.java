package com.kshitij.trading.service;

import com.kshitij.trading.domain.entity.Order;
import com.kshitij.trading.domain.entity.Stock;
import com.kshitij.trading.domain.entity.Trader;
import com.kshitij.trading.domain.enums.OrderStatus;
import com.kshitij.trading.domain.enums.Side;
import com.kshitij.trading.domain.repository.OrderRepository;
import com.kshitij.trading.domain.repository.PortfolioRepository;
import com.kshitij.trading.domain.repository.StockRepository;
import com.kshitij.trading.domain.repository.TraderRepository;
import com.kshitij.trading.dto.OrderRequest;
import com.kshitij.trading.dto.OrderResponse;
import com.kshitij.trading.exception.InsufficientHoldingsException;
import com.kshitij.trading.exception.PendingOrderLimitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Unit Tests")
class OrderServiceTest {

    @Mock
    private TraderRepository traderRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PortfolioService portfolioService;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest validBuyRequest;
    private OrderRequest validSellRequest;
    private Trader testTrader;
    private Stock testStock;

    @BeforeEach
    void setUp() {
        validBuyRequest = new OrderRequest();
        validBuyRequest.setTraderId("T001");
        validBuyRequest.setStock("AAPL");
        validBuyRequest.setSector("TECH");
        validBuyRequest.setQuantity(50);
        validBuyRequest.setSide("BUY");

        validSellRequest = new OrderRequest();
        validSellRequest.setTraderId("T001");
        validSellRequest.setStock("AAPL");
        validSellRequest.setSector("TECH");
        validSellRequest.setQuantity(30);
        validSellRequest.setSide("SELL");

        testTrader = Trader.builder()
                .id(1L)
                .traderId("T001")
                .build();

        testStock = Stock.builder()
                .symbol("AAPL")
                .name("Apple Inc.")
                .sector("TECH")
                .build();
    }

    @Test
    @DisplayName("Place BUY order - Should succeed when no pending orders")
    void placeBuyOrder_WithNoPendingOrders_ShouldSucceed() {
        when(traderRepository.findByTraderId("T001")).thenReturn(Optional.of(testTrader));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));
        when(orderRepository.countByTraderAndStatus(testTrader, OrderStatus.PENDING)).thenReturn(0L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderResponse response = orderService.placeOrder(validBuyRequest);

        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("Order placed successfully");
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Place BUY order - Auto-create new trader when not exists")
    void placeBuyOrder_WithNewTrader_ShouldAutoCreateTrader() {
        when(traderRepository.findByTraderId("T001")).thenReturn(Optional.empty());
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));
        when(orderRepository.countByTraderAndStatus(any(Trader.class), eq(OrderStatus.PENDING))).thenReturn(0L);
        when(traderRepository.save(any(Trader.class))).thenReturn(testTrader);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderResponse response = orderService.placeOrder(validBuyRequest);

        assertThat(response.getStatus()).isEqualTo("PENDING");
        verify(traderRepository, times(1)).save(any(Trader.class));
    }

    @Test
    @DisplayName("Place order - Should fail when trader has 3 pending orders")
    void placeOrder_WithThreePendingOrders_ShouldThrowException() {
        when(traderRepository.findByTraderId("T001")).thenReturn(Optional.of(testTrader));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));
        when(orderRepository.countByTraderAndStatus(testTrader, OrderStatus.PENDING)).thenReturn(3L);

        assertThatThrownBy(() -> orderService.placeOrder(validBuyRequest))
                .isInstanceOf(PendingOrderLimitException.class)
                .hasMessageContaining("already has 3 pending orders");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Place SELL order - Should fail when insufficient holdings")
    void placeSellOrder_WithInsufficientHoldings_ShouldThrowException() {
        when(traderRepository.findByTraderId("T001")).thenReturn(Optional.of(testTrader));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));
        when(orderRepository.countByTraderAndStatus(testTrader, OrderStatus.PENDING)).thenReturn(0L);
        when(portfolioService.getHoldingsForStock(testTrader, testStock)).thenReturn(10);

        assertThatThrownBy(() -> orderService.placeOrder(validSellRequest))
                .isInstanceOf(InsufficientHoldingsException.class)
                .hasMessageContaining("Insufficient holdings");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Place SELL order - Should succeed when enough holdings")
    void placeSellOrder_WithEnoughHoldings_ShouldSucceed() {
        when(traderRepository.findByTraderId("T001")).thenReturn(Optional.of(testTrader));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(testStock));
        when(orderRepository.countByTraderAndStatus(testTrader, OrderStatus.PENDING)).thenReturn(0L);
        when(portfolioService.getHoldingsForStock(testTrader, testStock)).thenReturn(100);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderResponse response = orderService.placeOrder(validSellRequest);

        assertThat(response.getStatus()).isEqualTo("PENDING");
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Fill order - Should succeed for PENDING BUY order")
    void fillOrder_WithPendingBuyOrder_ShouldSucceed() {
        Order pendingOrder = Order.builder()
                .id(1L)
                .trader(testTrader)
                .stock(testStock)
                .quantity(50)
                .side(Side.BUY)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(portfolioService.getHoldingsForStock(testTrader, testStock)).thenReturn(0);

        OrderResponse response = orderService.fillOrder(1L);

        assertThat(response.getStatus()).isEqualTo("FILLED");
        assertThat(response.getMessage()).contains("Order filled");
        verify(portfolioService, times(1)).addHoldings(testTrader, testStock, 50);
        verify(orderRepository, times(1)).save(pendingOrder);
        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.FILLED);
    }

    @Test
    @DisplayName("Fill order - Should succeed for PENDING SELL order")
    void fillOrder_WithPendingSellOrder_ShouldSucceed() {
        Order pendingOrder = Order.builder()
                .id(1L)
                .trader(testTrader)
                .stock(testStock)
                .quantity(30)
                .side(Side.SELL)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        OrderResponse response = orderService.fillOrder(1L);

        assertThat(response.getStatus()).isEqualTo("FILLED");
        verify(portfolioService, times(1)).removeHoldings(testTrader, testStock, 30);
        verify(orderRepository, times(1)).save(pendingOrder);
        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.FILLED);
    }

    @Test
    @DisplayName("Fill order - Should fail when order already filled")
    void fillOrder_WithAlreadyFilledOrder_ShouldThrowException() {
        Order filledOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.FILLED)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(filledOrder));

        assertThatThrownBy(() -> orderService.fillOrder(1L))
                .hasMessageContaining("Only PENDING orders can be filled");

        verify(portfolioService, never()).addHoldings(any(), any(), anyInt());
        verify(portfolioService, never()).removeHoldings(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Fill order - Should fail when order already cancelled")
    void fillOrder_WithCancelledOrder_ShouldThrowException() {
        Order cancelledOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.CANCELLED)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(cancelledOrder));

        assertThatThrownBy(() -> orderService.fillOrder(1L))
                .hasMessageContaining("Only PENDING orders can be filled");
    }

    @Test
    @DisplayName("Cancel order - Should succeed for PENDING order")
    void cancelOrder_WithPendingOrder_ShouldSucceed() {
        Order pendingOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        OrderResponse response = orderService.cancelOrder(1L);

        assertThat(response.getStatus()).isEqualTo("CANCELLED");
        assertThat(response.getMessage()).isEqualTo("Order cancelled successfully");
        verify(orderRepository, times(1)).save(pendingOrder);
        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Cancel order - Should fail when order already filled")
    void cancelOrder_WithFilledOrder_ShouldThrowException() {
        Order filledOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.FILLED)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(filledOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .hasMessageContaining("Only PENDING orders can be cancelled");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Cancel order - Should fail when order not found")
    void cancelOrder_WithNonExistentOrder_ShouldThrowException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(999L))
                .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("Fill order - Should fail when order not found")
    void fillOrder_WithNonExistentOrder_ShouldThrowException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.fillOrder(999L))
                .hasMessageContaining("Order not found");
    }
}