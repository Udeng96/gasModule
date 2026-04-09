package com.eseict.gasmodule.serv;

import com.eseict.gasmodule.data.mssql.dto.GasRawInfo;
import com.eseict.gasmodule.repository.mssql.GasRepository;
import com.eseict.gasmodule.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class GasRawService {

    private final GasRepository gasRepository;
    private final Util util;

    @Value("${gas.polling.start-date}")
    private String pollingStartDate;

    public List<GasRawInfo> getNewRawEvents(int cacheIdx) {
        return gasRepository.findNextEvents(util.toLocalDateTime(pollingStartDate), cacheIdx).stream()
                .map(rawEvent -> new GasRawInfo(rawEvent.getIdx(), rawEvent.getDate(), rawEvent.getState(), rawEvent.getDetector()))
                .collect(Collectors.toList());
    }
}
