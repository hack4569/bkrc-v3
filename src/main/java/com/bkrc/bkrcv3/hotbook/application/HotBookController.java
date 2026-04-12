package com.bkrc.bkrcv3.hotbook.application;

import com.bkrc.bkrcv3.aladin.application.response.AladinBookResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class HotBookController {
    private final HotBookService hotBookService;

    @GetMapping("/v1/hot-books/books/date/{dateStr}")
    public List<AladinBookResponse> readAll(
            @PathVariable("dateStr") String dateStr
    ) {
        return hotBookService.readAll(dateStr);
    }
}
