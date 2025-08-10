package com.loopers.interfaces.api.dummy;

import com.loopers.application.dummy.DummyDataInsertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dummy")
public class DummyDataController {

    private final DummyDataInsertService dummyDataInsertService;

    @PostMapping("/insertBrand")
    public ResponseEntity<String> insertBrand(@RequestParam(defaultValue = "100") int count) {
        dummyDataInsertService.bulkInsertBrands(count);
        return ResponseEntity.ok("삽입 완료 : " + count);
    }

    @PostMapping("/insertProduct")
    public ResponseEntity<String> insertProduct(@RequestParam(defaultValue = "100000") int count) {
        dummyDataInsertService.bulkInsertProducts(count);
        return ResponseEntity.ok("삽입 완료 : " + count);
    }

    @PostMapping("/insertUser")
    public ResponseEntity<String> insertUser(@RequestParam(defaultValue = "1000") int count) {
        dummyDataInsertService.bulkInsertUsers(count);
        return ResponseEntity.ok("삽입 완료 : " + count);
    }
}
