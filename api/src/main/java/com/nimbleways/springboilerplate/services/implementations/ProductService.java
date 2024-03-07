package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class ProductService {

    @Autowired
    ProductRepository pr;

    @Autowired
    NotificationService ns;

    public void notifyDelay(int leadTime, Product p) {
        p.setLeadTime(leadTime);
        pr.save(p);
        ns.sendDelayNotification(leadTime, p.getName());
    }

    public void handleSeasonalProduct(Product p) {
        if (LocalDate.now().plusDays(p.getLeadTime()).isAfter(p.getSeasonEndDate())) {
            ns.sendOutOfStockNotification(p.getName());
            p.setAvailable(0);
            pr.save(p);
        } else if (p.getSeasonStartDate().isAfter(LocalDate.now())) {
            ns.sendOutOfStockNotification(p.getName());
            pr.save(p);
        } else {
            notifyDelay(p.getLeadTime(), p);
        }
    }

    public void handleExpiredProduct(Product p) {
        if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
            p.setAvailable(p.getAvailable() - 1);
            pr.save(p);
        } else {
            ns.sendExpirationNotification(p.getName(), p.getExpiryDate());
            p.setAvailable(0);
            pr.save(p);
        }
    }

    public void handleFlashSaleProduct(Product flashSaleProduct) {
        flashSaleProduct.setAvailable(flashSaleProduct.getFlashSaleMaxQuantity());
        if (flashSaleProduct.getAvailable() > 0 && isWithinFlahSalePeriod(flashSaleProduct)) {
            flashSaleProduct.setAvailable(flashSaleProduct.getAvailable() - 1);
            flashSaleProduct.setFlashSaleMaxQuantity(flashSaleProduct.getFlashSaleMaxQuantity() - 1);
            pr.save(flashSaleProduct);
        } else {
            ns.sendOutOfStockNotification(flashSaleProduct.getName());
            flashSaleProduct.setAvailable(0);
            pr.save(flashSaleProduct);
        }
    }


    /**
     * @param product
     * @return true si le produit est actuellement dans la periode de flash sale sinon return false
     */
    private boolean isWithinFlahSalePeriod(Product product) {
        LocalDateTime currentTime = LocalDateTime.now();
        return currentTime.isAfter(product.getFlashSaleStartDate()) &&
                currentTime.isBefore(product.getFlashSaleEndDate());
    }
}