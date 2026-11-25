package ru.kafpin.school;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Service
@Transactional(readOnly = true)
class SchoolboyService {
    private final SchoolboyStore store;

    SchoolboyService(SchoolboyStore store) {
        this.store = store;
    }

    List<SchoolboyView> findAll() {
        return store.findAll();
    }

    Optional<SchoolboyView> find(long id) {
        return store.find(id);
    }

    List<ClassStatistics> statistics() {
        return store.statistics();
    }

    List<SchoolInfo> schools() {
        return store.schools();
    }

    List<ClassInfo> classes() {
        return store.classes();
    }

    @Transactional
    SchoolboyView create(SchoolboyData data) {
        checkClass(data);
        return store.create(data);
    }

    @Transactional
    Optional<SchoolboyView> update(long id, UnaryOperator<SchoolboyData> change) {
        return store.findForUpdate(id).map(current -> {
            SchoolboyData data = change.apply(current.data());
            checkClass(data);
            return store.replace(id, data);
        });
    }

    @Transactional
    boolean delete(long id) {
        if (store.findForUpdate(id).isEmpty()) {
            return false;
        }
        store.delete(id);
        return true;
    }

    private void checkClass(SchoolboyData data) {
        if (store.findClass(data.classId()).filter(selected -> selected.schoolId() == data.schoolId()).isEmpty()) {
            throw new IllegalArgumentException("Укажите существующий класс выбранной школы.");
        }
    }
}
