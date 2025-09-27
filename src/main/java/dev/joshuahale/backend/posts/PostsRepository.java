package dev.joshuahale.backend.posts;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class PostsRepository {

    @PersistenceContext
    private EntityManager em;

    public PostsEntity save(PostsEntity post) {
        if (post.getId() == null) {
            em.persist(post);
            return post;
        } else {
            return em.merge(post);
        }
    }

    @Transactional(readOnly = true)
    public Optional<PostsEntity> findById(Long id) {
        return Optional.ofNullable(em.find(PostsEntity.class, id));
    }

    public boolean deleteById(Long id) {
        PostsEntity managed = em.find(PostsEntity.class, id);
        if (managed == null) return false;
        em.remove(managed);
        return true;
    }

    @Transactional(readOnly = true)
    public Optional<PostsEntity> findBySlug(String slug) {
        var query = em.createQuery("""
        select p from PostsEntity p
        where p.slug = :slug
        """, PostsEntity.class);
        return query.setParameter("slug", slug)
                .getResultStream()
                .findFirst();
    }

    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        Long count = em.createQuery("""
        select count(p) from PostsEntity p
        where p.slug = :slug
        """, Long.class)
                .setParameter("slug", slug)
                .getSingleResult();
        return count > 0;
    }

    @Transactional(readOnly = true)
    public List<PostsEntity> listAllOrdered() {
        return em.createQuery("""
        select p from PostsEntity p
        order by p.createdAt desc, p.id desc
    """, PostsEntity.class).getResultList();
    }
}