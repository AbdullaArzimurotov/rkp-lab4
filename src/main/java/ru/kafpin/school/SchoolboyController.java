package ru.kafpin.school;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
class SchoolboyController {
    private final SchoolboyService schoolboys;
    private final SchoolboyPatch patch;

    SchoolboyController(SchoolboyService schoolboys, SchoolboyPatch patch) {
        this.schoolboys = schoolboys;
        this.patch = patch;
    }

    @GetMapping("/schoolboys")
    List<SchoolboyView> list() {
        return schoolboys.findAll();
    }

    @GetMapping("/schoolboys/{id}")
    ResponseEntity<SchoolboyView> find(@PathVariable long id) {
        return ResponseEntity.of(schoolboys.find(id));
    }

    @PostMapping("/schoolboys")
    ResponseEntity<SchoolboyView> create(@Valid @RequestBody SchoolboyData data) {
        SchoolboyView created = schoolboys.create(data);
        return ResponseEntity.created(URI.create("/api/schoolboys/" + created.id())).body(created);
    }

    @PutMapping("/schoolboys/{id}")
    ResponseEntity<SchoolboyView> replace(@PathVariable long id, @Valid @RequestBody SchoolboyData data) {
        return ResponseEntity.of(schoolboys.update(id, current -> data));
    }

    @PatchMapping("/schoolboys/{id}")
    ResponseEntity<SchoolboyView> patch(@PathVariable long id, @RequestBody JsonNode changes) {
        return ResponseEntity.of(schoolboys.update(id, current -> patch.apply(current, changes)));
    }

    @DeleteMapping("/schoolboys/{id}")
    ResponseEntity<Void> delete(@PathVariable long id) {
        return schoolboys.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/schoolboys/statistics")
    List<ClassStatistics> statistics() {
        return schoolboys.statistics();
    }

    @GetMapping("/schools")
    Catalog catalog() {
        return new Catalog(schoolboys.schools(), schoolboys.classes());
    }

    record Catalog(List<SchoolInfo> schools, List<ClassInfo> classes) {
    }
}
