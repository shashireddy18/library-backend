package com.library.controller;

import com.library.dto.DashboardStats;
import com.library.dto.IssueRequest;
import com.library.dto.IssueResponse;
import com.library.service.IssueService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class IssueController {
    private final IssueService service;

    public IssueController(IssueService service) {
        this.service = service;
    }

    @GetMapping("/issues")
    public List<IssueResponse> list(@RequestParam(required = false) String status) {
        return service.list(status);
    }

    @PostMapping("/issues")
    @ResponseStatus(HttpStatus.CREATED)
    public IssueResponse issue(@Valid @RequestBody IssueRequest req) {
        return service.issue(req);
    }

    @PutMapping("/issues/{id}/return")
    public IssueResponse returnBook(@PathVariable Long id) {
        return service.returnBook(id);
    }

    @GetMapping("/dashboard")
    public DashboardStats dashboard() {
        return service.stats();
    }
}
