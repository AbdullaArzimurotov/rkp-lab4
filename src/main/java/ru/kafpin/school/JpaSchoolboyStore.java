package ru.kafpin.school;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaSchoolboyStore implements SchoolboyStore {
    private final EntityManager entityManager;

    JpaSchoolboyStore(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<SchoolboyView> findAll() {
        return entityManager.createQuery("""
                select p from Schoolboy p
                join fetch p.address a join fetch p.schoolClass c join fetch c.school s
                order by p.id
                """, Schoolboy.class).getResultList().stream().map(Schoolboy::toView).toList();
    }

    @Override
    public Optional<SchoolboyView> find(long id) {
        return Optional.ofNullable(entityManager.find(Schoolboy.class, id)).map(Schoolboy::toView);
    }

    @Override
    public List<SchoolInfo> schools() {
        return entityManager.createQuery("""
                select new ru.kafpin.school.SchoolInfo(s.id, s.name)
                from School s order by s.name
                """, SchoolInfo.class).getResultList();
    }

    @Override
    public List<ClassInfo> classes() {
        return entityManager.createQuery("""
                select new ru.kafpin.school.ClassInfo(c.id, s.id, c.name)
                from SchoolClass c join c.school s order by s.name, c.name
                """, ClassInfo.class).getResultList();
    }

    @Override
    public Optional<ClassInfo> findClass(long id) {
        return Optional.ofNullable(entityManager.find(SchoolClass.class, id))
                .map(selected -> new ClassInfo(selected.id(), selected.schoolId(), selected.name()));
    }

    @Override
    public SchoolboyView create(SchoolboyData data) {
        SchoolClass selected = entityManager.find(SchoolClass.class, data.classId());
        Schoolboy schoolboy = new Schoolboy(data, selected);
        entityManager.persist(schoolboy);
        return schoolboy.toView();
    }

    @Override
    public Optional<SchoolboyView> findForUpdate(long id) {
        return Optional.ofNullable(entityManager.find(Schoolboy.class, id, LockModeType.PESSIMISTIC_WRITE))
                .map(Schoolboy::toView);
    }

    @Override
    public SchoolboyView replace(long id, SchoolboyData data) {
        Schoolboy schoolboy = entityManager.find(Schoolboy.class, id);
        schoolboy.apply(data, entityManager.find(SchoolClass.class, data.classId()));
        return schoolboy.toView();
    }

    @Override
    public void delete(long id) {
        entityManager.remove(entityManager.find(Schoolboy.class, id));
    }

    @Override
    public List<ClassStatistics> statistics() {
        return entityManager.createQuery("""
                select new ru.kafpin.school.ClassStatistics(s.id, s.name, c.id, c.name, count(p))
                from Schoolboy p join p.schoolClass c join c.school s
                group by s.id, s.name, c.id, c.name order by s.id, c.id
                """, ClassStatistics.class).getResultList();
    }
}
