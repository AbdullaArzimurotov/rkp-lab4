package ru.kafpin.school;

import java.util.List;
import java.util.Optional;

public interface SchoolboyStore {
    List<SchoolboyView> findAll();
    Optional<SchoolboyView> find(long id);
    List<SchoolInfo> schools();
    List<ClassInfo> classes();
    Optional<ClassInfo> findClass(long id);
    SchoolboyView create(SchoolboyData data);

    Optional<SchoolboyView> findForUpdate(long id);
    SchoolboyView replace(long id, SchoolboyData data);
    void delete(long id);
    List<ClassStatistics> statistics();
}
