package ru.kafpin.school;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

@Component
class SchoolboyPatch {
    private final ObjectMapper mapper;
    private final Validator validator;

    SchoolboyPatch(ObjectMapper mapper, Validator validator) {
        this.mapper = mapper;
        this.validator = validator;
    }

    SchoolboyData apply(SchoolboyData current, JsonNode changes) {
        if (!changes.isObject() || changes.isEmpty()) {
            throw new IllegalArgumentException("Укажите изменяемые поля в объекте JSON.");
        }

        ObjectNode document = mapper.valueToTree(current);
        merge(document, changes);
        SchoolboyData updated;
        try {
            updated = mapper.treeToValue(document, SchoolboyData.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Проверьте типы изменяемых полей.", exception);
        }
        var errors = validator.validate(updated);
        if (!errors.isEmpty()) {
            throw new ConstraintViolationException(errors);
        }
        return updated;
    }

    private void merge(ObjectNode document, JsonNode changes) {
        changes.fields().forEachRemaining(entry -> {
            String field = entry.getKey();
            JsonNode value = entry.getValue();
            JsonNode previous = document.get(field);
            if (previous == null) {
                throw new IllegalArgumentException("Неизвестное поле: " + field);
            }
            if (previous instanceof ObjectNode nested && value.isObject()) {
                merge(nested, value);
            } else {
                document.set(field, value);
            }
        });
    }
}
