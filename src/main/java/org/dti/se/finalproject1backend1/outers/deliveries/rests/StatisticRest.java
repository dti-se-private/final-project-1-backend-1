package org.dti.se.finalproject1backend1.outers.deliveries.rests;

import org.dti.se.finalproject1backend1.inners.models.entities.Account;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.statistics.StatisticSeriesResponse;
import org.dti.se.finalproject1backend1.inners.usecases.statistics.BasicStatisticUseCase;
import org.dti.se.finalproject1backend1.outers.exceptions.statistics.StatisticTypeInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/statistics")
public class StatisticRest {
    @Autowired
    BasicStatisticUseCase basicStatisticUseCase;

    @GetMapping("/events")
    public ResponseEntity<ResponseBody<List<StatisticSeriesResponse>>> checkout(
            @AuthenticationPrincipal Account authenticatedAccount,
            @RequestParam(defaultValue = "transactionAmount") String type,
            @RequestParam(defaultValue = "sum") String aggregation,
            @RequestParam(defaultValue = "day") String period
    ) {
        try {
            List<StatisticSeriesResponse> series = switch (type) {
                case "transactionAmount" ->
                        basicStatisticUseCase.retrieveTransactionAmount(authenticatedAccount, aggregation, period);
                case "participantCount" ->
                        basicStatisticUseCase.retrieveParticipantCount(authenticatedAccount, aggregation, period);
                default -> throw new StatisticTypeInvalidException();
            };
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("Retrieve event statistic succeed.")
                    .data(series)
                    .build()
                    .toEntity(HttpStatus.OK);
        } catch (StatisticTypeInvalidException e) {
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message(e.getMessage())
                    .build()
                    .toEntity(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseBody
                    .<List<StatisticSeriesResponse>>builder()
                    .message("Internal server error.")
                    .exception(e)
                    .build()
                    .toEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}