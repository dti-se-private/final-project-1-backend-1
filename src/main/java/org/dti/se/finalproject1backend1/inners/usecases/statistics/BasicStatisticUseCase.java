package org.dti.se.finalproject1backend1.inners.usecases.statistics;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.dti.se.finalproject1backend1.outers.exceptions.statistics.StatisticAggregationInvalidException;
import org.dti.se.finalproject1backend1.outers.repositories.customs.StatisticCustomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BasicStatisticUseCase {

    @Autowired
    StatisticCustomRepository statisticCustomRepository;

    public List<StatisticSeriesResponse> retrieveTransactionAmount(
            Account authenticatedAccount,
            String aggregation,
            String period
    ) {
        return switch (aggregation) {
            case "sum" -> statisticCustomRepository.retrieveTransactionAmountSum(authenticatedAccount, period);
            case "average" -> statisticCustomRepository.retrieveTransactionAmountAverage(authenticatedAccount, period);
            default -> throw new StatisticAggregationInvalidException();
        };
    }

    public List<StatisticSeriesResponse> retrieveParticipantCount(
            Account authenticatedAccount,
            String aggregation,
            String period
    ) {
        return switch (aggregation) {
            case "sum" -> statisticCustomRepository.retrieveParticipantCountSum(authenticatedAccount, period);
            case "average" -> statisticCustomRepository.retrieveParticipantCountAverage(authenticatedAccount, period);
            default -> throw new StatisticAggregationInvalidException();
        };
    }
}