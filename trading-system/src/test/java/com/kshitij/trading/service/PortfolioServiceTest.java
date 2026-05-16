package com.kshitij.trading.service;

import com.kshitij.trading.domain.entity.Portfolio;
import com.kshitij.trading.domain.entity.Stock;
import com.kshitij.trading.domain.entity.Trader;
import com.kshitij.trading.domain.repository.PortfolioRepository;
import com.kshitij.trading.domain.repository.StockRepository;
import com.kshitij.trading.domain.repository.TraderRepository;
import com.kshitij.trading.dto.OverlapResponse;
import com.kshitij.trading.dto.PortfolioResponse;
import com.kshitij.trading.dto.PortfolioResponse.DirectAddResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Portfolio Service Unit Tests")
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private TraderRepository traderRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private SectorOverlapService sectorOverlapService;

    @InjectMocks
    private PortfolioService portfolioService;

    private Trader testTrader;
    private Stock testStock;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Get holdings - Returns correct quantity when stock exists")
    void getHoldingsForStock_WhenStockExists_ReturnsQuantity() {
        Portfolio portfolio = Portfolio.builder()
                .trader(testTrader)
                .stock(testStock)
                .quantity(100)
                .build();

        when(portfolioRepository.findByTraderAndStockWithLock(testTrader, testStock))
                .thenReturn(Optional.of(portfolio));

        int quantity = portfolioService.getHoldingsForStock(testTrader, testStock);

        assertThat(quantity).isEqualTo(100);
    }

    @Test
    @DisplayName("Get holdings - Returns zero when stock not in portfolio")
    void getHoldingsForStock_WhenStockDoesNotExist_ReturnsZero() {
        when(portfolioRepository.findByTraderAndStockWithLock(testTrader, testStock))
                .thenReturn(Optional.empty());

        int quantity = portfolioService.getHoldingsForStock(testTrader, testStock);

        assertThat(quantity).isEqualTo(0);
    }

    @Test
    @DisplayName("Add holdings - Creates new portfolio entry when not exists")
    void addHoldings_WhenPortfolioNotExists_CreatesNewEntry() {
        when(portfolioRepository.findByTraderAndStockWithLock(testTrader, testStock))
                .thenReturn(Optional.empty());
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        portfolioService.addHoldings(testTrader, testStock, 50);

        verify(portfolioRepository, times(1)).save(any(Portfolio.class));
    }

    @Test
    @DisplayName("Add holdings - Updates existing portfolio entry")
    void addHoldings_WhenPortfolioExists_UpdatesQuantity() {
        Portfolio portfolio = Portfolio.builder()
                .trader(testTrader)
                .stock(testStock)
                .quantity(100)
                .build();

        when(portfolioRepository.findByTraderAndStockWithLock(testTrader, testStock))
                .thenReturn(Optional.of(portfolio));
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        portfolioService.addHoldings(testTrader, testStock, 50);

        assertThat(portfolio.getQuantity()).isEqualTo(150);
        verify(portfolioRepository, times(1)).save(portfolio);
    }

    @Test
    @DisplayName("Remove holdings - Decreases quantity correctly")
    void removeHoldings_WhenSufficientHoldings_DecreasesQuantity() {
        Portfolio portfolio = Portfolio.builder()
                .trader(testTrader)
                .stock(testStock)
                .quantity(100)
                .build();

        when(portfolioRepository.findByTraderAndStockWithLock(testTrader, testStock))
                .thenReturn(Optional.of(portfolio));

        portfolioService.removeHoldings(testTrader, testStock, 30);

        assertThat(portfolio.getQuantity()).isEqualTo(70);
        verify(portfolioRepository, times(1)).save(portfolio);
    }

    @Test
    @DisplayName("Remove holdings - Throws exception when stock not in portfolio")
    void removeHoldings_WhenStockNotInPortfolio_ThrowsException() {
        when(portfolioRepository.findByTraderAndStockWithLock(testTrader, testStock))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.removeHoldings(testTrader, testStock, 30))
                .hasMessageContaining("No holdings found");
    }

    @Test
    @DisplayName("Remove holdings - Throws exception when insufficient quantity")
    void removeHoldings_WhenInsufficientQuantity_ThrowsException() {
        Portfolio portfolio = Portfolio.builder()
                .trader(testTrader)
                .stock(testStock)
                .quantity(10)
                .build();

        when(portfolioRepository.findByTraderAndStockWithLock(testTrader, testStock))
                .thenReturn(Optional.of(portfolio));

        assertThatThrownBy(() -> portfolioService.removeHoldings(testTrader, testStock, 30))
                .hasMessageContaining("Insufficient holdings");
    }

    @Test
    @DisplayName("Direct add to portfolio - Creates new trader and stock if needed")
    void directAddToPortfolio_WithNewTraderAndStock_CreatesBoth() {
        when(traderRepository.findByTraderId("T001")).thenReturn(Optional.empty());
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.empty());
        when(traderRepository.save(any(Trader.class))).thenReturn(testTrader);
        when(stockRepository.save(any(Stock.class))).thenReturn(testStock);
        when(portfolioRepository.findByTraderAndStockWithLock(any(), any())).thenReturn(Optional.empty());
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DirectAddResponse response = portfolioService.directAddToPortfolio("T001", "AAPL", "TECH", 100);

        assertThat(response.getTraderId()).isEqualTo("T001");
        assertThat(response.getStock()).isEqualTo("AAPL");
        assertThat(response.getOldQuantity()).isEqualTo(0);
        assertThat(response.getNewQuantity()).isEqualTo(100);
        verify(traderRepository, times(1)).save(any(Trader.class));
        verify(stockRepository, times(1)).save(any(Stock.class));
    }

    @Test
    @DisplayName("Get portfolio - Returns empty positions for new trader")
    void getPortfolio_ForNewTrader_ReturnsEmptyPositions() {
        when(traderRepository.findByTraderId("T001")).thenReturn(Optional.of(testTrader));
        when(portfolioRepository.findByTrader(testTrader)).thenReturn(List.of());

        PortfolioResponse response = portfolioService.getPortfolio("T001");

        assertThat(response.getTraderId()).isEqualTo("T001");
        assertThat(response.getPositions()).isEmpty();
        assertThat(response.getSectorBreakdown()).isEmpty();
    }

    @Test
    @DisplayName("Calculate overlap - Returns correct response for trader")
    void calculateOverlap_ForTraderWithStocks_ReturnsOverlapResponse() {
        when(traderRepository.findByTraderId("T001")).thenReturn(Optional.of(testTrader));

        Portfolio portfolio1 = Portfolio.builder().stock(testStock).quantity(10).build();
        Stock stock2 = Stock.builder().symbol("MSFT").sector("TECH").build();
        Portfolio portfolio2 = Portfolio.builder().stock(stock2).quantity(20).build();

        when(portfolioRepository.findByTrader(testTrader)).thenReturn(List.of(portfolio1, portfolio2));

        OverlapResponse expectedResponse = new OverlapResponse();
        when(sectorOverlapService.calculateOverlap(Set.of("AAPL", "MSFT"))).thenReturn(expectedResponse);

        OverlapResponse response = portfolioService.calculateOverlap("T001");

        assertThat(response).isSameAs(expectedResponse);
    }
}