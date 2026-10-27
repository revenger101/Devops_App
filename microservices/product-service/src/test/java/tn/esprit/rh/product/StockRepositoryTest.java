package tn.esprit.rh.product;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import tn.esprit.rh.product.entities.Stock;
import tn.esprit.rh.product.repositories.StockRepository;

@DataJpaTest
class StockRepositoryTest {

	@Autowired
	StockRepository stockRepository;

	@Test
	void retrieveStatusStockReturnsOnlyStocksUnderTheirMinimum() {
		stockRepository.save(new Stock("ok", 50, 10));
		stockRepository.save(new Stock("low", 3, 10));

		List<Stock> low = stockRepository.retrieveStatusStock();

		assertEquals(1, low.size());
		assertEquals("low", low.get(0).getLibelleStock());
	}
}
