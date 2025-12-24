package com.example.demo.controller.Admin;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.ConsultingRequestDto;
import com.example.demo.dto.UpdateConsultingStatusRequest;
import com.example.demo.service.ConsultingService;

@RestController
@RequestMapping("/api/admin/consulting-requests")
public class AdminConsultingController {

    private final ConsultingService service;

    public AdminConsultingController(ConsultingService service) {
        this.service = service;
    }

    @GetMapping
    public List<ConsultingRequestDto> list(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Integer offset
    ) {
        return service.list(status, limit, offset);
    }

    @PutMapping("/{id}")
    public ConsultingRequestDto updateStatus(@PathVariable("id") long id,
                                             @RequestBody UpdateConsultingStatusRequest req) {
        return service.updateStatus(id, req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> bad(IllegalArgumentException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NoSuchElementException ex) {
        return Map.of("message", ex.getMessage());
    }
}
