package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.Application;
import com.mycompany.myapp.domain.Author;
import com.mycompany.myapp.repository.AuthorRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the AuthorResource REST controller.
 *
 * @see AuthorResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class AuthorResourceTest {

    private static final String DEFAULT_DUMMY_NAME = "SAMPLE_TEXT";
    private static final String UPDATED_DUMMY_NAME = "UPDATED_TEXT";

    @Inject
    private AuthorRepository authorRepository;

    private MockMvc restAuthorMockMvc;

    private Author author;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AuthorResource authorResource = new AuthorResource();
        ReflectionTestUtils.setField(authorResource, "authorRepository", authorRepository);
        this.restAuthorMockMvc = MockMvcBuilders.standaloneSetup(authorResource).build();
    }

    @Before
    public void initTest() {
        author = new Author();
        author.setDummyName(DEFAULT_DUMMY_NAME);
    }

    @Test
    @Transactional
    public void createAuthor() throws Exception {
        int databaseSizeBeforeCreate = authorRepository.findAll().size();

        // Create the Author
        restAuthorMockMvc.perform(post("/api/authors")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(author)))
                .andExpect(status().isCreated());

        // Validate the Author in the database
        List<Author> authors = authorRepository.findAll();
        assertThat(authors).hasSize(databaseSizeBeforeCreate + 1);
        Author testAuthor = authors.get(authors.size() - 1);
        assertThat(testAuthor.getDummyName()).isEqualTo(DEFAULT_DUMMY_NAME);
    }

    @Test
    @Transactional
    public void getAllAuthors() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get all the authors
        restAuthorMockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(author.getId().intValue())))
                .andExpect(jsonPath("$.[*].dummyName").value(hasItem(DEFAULT_DUMMY_NAME.toString())));
    }

    @Test
    @Transactional
    public void getAuthor() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

        // Get the author
        restAuthorMockMvc.perform(get("/api/authors/{id}", author.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(author.getId().intValue()))
            .andExpect(jsonPath("$.dummyName").value(DEFAULT_DUMMY_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingAuthor() throws Exception {
        // Get the author
        restAuthorMockMvc.perform(get("/api/authors/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAuthor() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

		int databaseSizeBeforeUpdate = authorRepository.findAll().size();

        // Update the author
        author.setDummyName(UPDATED_DUMMY_NAME);
        restAuthorMockMvc.perform(put("/api/authors")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(author)))
                .andExpect(status().isOk());

        // Validate the Author in the database
        List<Author> authors = authorRepository.findAll();
        assertThat(authors).hasSize(databaseSizeBeforeUpdate);
        Author testAuthor = authors.get(authors.size() - 1);
        assertThat(testAuthor.getDummyName()).isEqualTo(UPDATED_DUMMY_NAME);
    }

    @Test
    @Transactional
    public void deleteAuthor() throws Exception {
        // Initialize the database
        authorRepository.saveAndFlush(author);

		int databaseSizeBeforeDelete = authorRepository.findAll().size();

        // Get the author
        restAuthorMockMvc.perform(delete("/api/authors/{id}", author.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Author> authors = authorRepository.findAll();
        assertThat(authors).hasSize(databaseSizeBeforeDelete - 1);
    }
}
