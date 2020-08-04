package com.example.springbootrsql;

import com.example.springbootrsql.data.SampleChildEntity;
import com.example.springbootrsql.data.SampleEntity;
import com.example.springbootrsql.data.SampleEntityRepository;
import com.example.springbootrsql.rsql.CustomRsqlVisitor;
import com.github.tennaito.rsql.jpa.JpaCriteriaQueryVisitor;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;
import io.github.perplexhub.rsql.RSQLJPASupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;


// Setup for initializing H2 database on a test environment
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringbootrsqlApplication.class})
public class RsqlTests {

    @Autowired
    private SampleEntityRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Before
    public void populateDatabase() {
        SampleEntity entity1 = new SampleEntity();
        entity1.setAge(25);
        entity1.setCreationDate(LocalDate.now());
        entity1.setEmail("ONE@ANYMAIL.COM");
        entity1.setName("ENTITY_1");
        repository.save(entity1);

        SampleEntity entity2 = new SampleEntity();
        entity2.setAge(20);
        entity2.setCreationDate(LocalDate.now().minusMonths(3));
        entity2.setEmail("TWO@ANYMAIL.COM");
        entity2.setName("ENTITY_2");
        repository.save(entity2);
    }

    @After
    public void cleanup() {
        repository.findAll().forEach(entity -> repository.delete(entity));
    }

    /**
     * Author: Gabriel Robaina, based on https://www.baeldung.com/rest-api-search-language-rsql-fiql
     * Last commit on lib: Feb 14, 2016
     * Lib: https://github.com/jirutka/rsql-parser
     * Comments:
     * Basic abstraction for parsing the query parameters on a HTTP request.
     * Depends on an additional layer on top of it in order to create a JPA Specification from the parsed AST.
     * The layer is represented by:
     * @see com.example.springbootrsql.rsql.CustomRsqlVisitor
     * @see com.example.springbootrsql.rsql.GenericRsqlSpecBuilder
     * @see com.example.springbootrsql.rsql.GenericRsqlSpecification
     * @see com.example.springbootrsql.rsql.RsqlSearchOperation
     */
    @Test
    public void searchFromRequestParameters_searchEntity1WithRsqlParser_findsSuccessfully() {
        Node rootNode = new RSQLParser().parse("name==ENTITY_1;email==ONE@ANYMAIL.COM");
        Specification<SampleEntity> spec = rootNode.accept(new CustomRsqlVisitor<>());
        List<SampleEntity> foundEntities = repository.findAll(spec);
        assertThat(foundEntities.size()).isEqualTo(1);
        assertThat(foundEntities.get(0).getName()).isEqualTo("ENTITY_1");
    }

    /**
     * Author: Gabriel Robaina
     * Last commit on lib: May 1, 2017
     * Lib: https://github.com/tennaito/rsql-jpa
     * Comments:
     * Abstraction layer on top of rsql-parser. The main downside is having to expose the EntityManager.
     * This method is the most flexible, since you can change the CriteriaQuery before executing it.
     * You can change the sorting of the query, for example, before creating it with the EntityManager.
     */
    @Test
    public void searchFromRequestParameters_searchEntity1WithRsqlJpa_findsSuccessfully() {
        Node rootNode = new RSQLParser().parse("name==ENTITY_1;email==ONE@ANYMAIL.COM");
        RSQLVisitor<CriteriaQuery<SampleEntity>, EntityManager> visitor = new JpaCriteriaQueryVisitor<SampleEntity>();
        CriteriaQuery<SampleEntity> query = rootNode.accept(visitor, entityManager);
        List<SampleEntity> foundEntities = entityManager.createQuery(query).getResultList();
        assertThat(foundEntities.size()).isEqualTo(1);
        assertThat(foundEntities.get(0).getName()).isEqualTo("ENTITY_1");
    }

    /**
     * Author: Gabriel Robaina
     * Last commit on lib: Jul 2, 2020
     * Lib: https://github.com/perplexhub/rsql-jpa-specification
     * Comments:
     * Another abstraction layer on top of rsql-parser. This is the most straightforward one.
     * All you need to do is to call one method in order to execute a simple query. No need to expose an EntityManager.
     * The main downside is the loss of flexibility when comparing to the previous method.
     * This is the one lib preferred for apps with simple queries.
     */
    @Test
    public void searchFromRequestParameters_searchEntity1WithRsqlJpaSpecification_findsSuccessfully() {
        List<SampleEntity> foundEntities = repository.findAll(RSQLJPASupport.toSpecification("name==ENTITY_1;email==ONE@ANYMAIL.COM"));
        assertThat(foundEntities.size()).isEqualTo(1);
        assertThat(foundEntities.get(0).getName()).isEqualTo("ENTITY_1");
    }

    /**
     * Author: Gabriel Robaina
     * Last commit on lib: Jul 2, 2020
     * Lib: https://github.com/perplexhub/rsql-jpa-specification
     * Comments:
     * Proof that the rsql-jpa-specification lib can be used to search for entities based on an attribute of one of its children.
     * This is very usefl when we are working with tags, for example, and we want to find one entity based on the value of one tag.
     */
    @Test
    public void searchFromChildParameters_childrenWithNameProperty_findsSuccessfully() {
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setName("ENTITY_3");
        SampleChildEntity childEntity1 = new SampleChildEntity();
        childEntity1.setName("TOBEFOUND");
        childEntity1.setParent(sampleEntity);
        SampleChildEntity childEntity2 = new SampleChildEntity();
        childEntity2.setName("CANTFIND");
        childEntity2.setParent(sampleEntity);
        sampleEntity.getChildren().add(childEntity1);
        sampleEntity.getChildren().add(childEntity2);
        sampleEntity = repository.save(sampleEntity);
        List<SampleEntity> foundEntities = repository.findAll(RSQLJPASupport.toSpecification("children.name==TOBEFOUND"));
        assertThat(foundEntities.size()).isEqualTo(1);
        assertThat(foundEntities.get(0)).isEqualToIgnoringGivenFields(sampleEntity, "children");
    }
}
