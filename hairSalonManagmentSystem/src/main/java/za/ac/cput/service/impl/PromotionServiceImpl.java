package za.ac.cput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.cput.domain.Promotion;
import za.ac.cput.domain.enums.DiscountType;
import za.ac.cput.exception.InvalidOperationException;
import za.ac.cput.exception.ResourceNotFoundException;
import za.ac.cput.factory.PromotionFactory;
import za.ac.cput.repository.PromotionRepository;
import za.ac.cput.service.IPromotionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PromotionServiceImpl implements IPromotionService {

    private final PromotionRepository repository;

    public PromotionServiceImpl(PromotionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Promotion create(Promotion promotion) {
        return repository.save(promotion);
    }

    @Override
    public Promotion read(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Promotion", id));
    }

    @Override
    public Promotion update(Promotion promotion) {
        read(promotion.getPromotionId());
        return repository.save(promotion);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<Promotion> getAll() {
        return repository.findAll();
    }

    @Override
    public Promotion register(String code, String description, DiscountType discountType,
                              BigDecimal discountValue, LocalDate startDate, LocalDate endDate, int usageLimit) {
        Promotion promotion = PromotionFactory.buildPromotion(
                code, description, discountType, discountValue, startDate, endDate, usageLimit);
        if (promotion == null) {
            throw new InvalidOperationException("Invalid promotion details provided.");
        }
        return create(promotion);
    }

    @Override
    public Promotion validate(String code) {
        Promotion promotion = repository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("No promotion found with code: " + code));

        if (!promotion.getDateRange().contains(LocalDate.now())) {
            throw new InvalidOperationException("Promotion " + code + " is not currently active.");
        }
        if (promotion.getUsageCount() >= promotion.getUsageLimit()) {
            throw new InvalidOperationException("Promotion " + code + " has reached its usage limit.");
        }
        return promotion;
    }

    @Override
    public Promotion recordUsage(Promotion promotion) {
        Promotion updated = new Promotion.Builder()
                .setPromotionId(promotion.getPromotionId())
                .setCode(promotion.getCode())
                .setDescription(promotion.getDescription())
                .setDiscountType(promotion.getDiscountType())
                .setDiscountValue(promotion.getDiscountValue())
                .setDateRange(promotion.getDateRange())
                .setUsageLimit(promotion.getUsageLimit())
                .setUsageCount(promotion.getUsageCount() + 1)
                .build();
        return repository.save(updated);
    }

}
